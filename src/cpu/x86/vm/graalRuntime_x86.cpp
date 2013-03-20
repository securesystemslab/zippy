/*
 * Copyright (c) 1999, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 */

#include "precompiled.hpp"
#include "asm/assembler.hpp"
#include "graal/graalRuntime.hpp"
#include "interpreter/interpreter.hpp"
#include "nativeInst_x86.hpp"
#include "oops/compiledICHolder.hpp"
#include "oops/oop.inline.hpp"
#include "prims/jvmtiExport.hpp"
#include "register_x86.hpp"
#include "runtime/sharedRuntime.hpp"
#include "runtime/signature.hpp"
#include "runtime/vframeArray.hpp"
#include "vmreg_x86.inline.hpp"

static void restore_live_registers(GraalStubAssembler* sasm, bool restore_fpu_registers = true);

// Implementation of GraalStubAssembler

int GraalStubAssembler::call_RT(Register oop_result1, Register metadata_result, address entry, int args_size) {
  // setup registers
  const Register thread = NOT_LP64(rdi) LP64_ONLY(r15_thread); // is callee-saved register (Visual C++ calling conventions)
  assert(!(oop_result1->is_valid() || metadata_result->is_valid()) || oop_result1 != metadata_result, "registers must be different");
  assert(oop_result1 != thread && metadata_result != thread, "registers must be different");
  assert(args_size >= 0, "illegal args_size");
  bool align_stack = false;
#ifdef _LP64
  // At a method handle call, the stack may not be properly aligned
  // when returning with an exception.
  align_stack = (stub_id() == false /*GraalRuntime::handle_exception_from_callee_id*/);
#endif

#ifdef _LP64
  mov(c_rarg0, thread);
  set_num_rt_args(0); // Nothing on stack
#else
  set_num_rt_args(1 + args_size);

  // push java thread (becomes first argument of C function)
  get_thread(thread);
  push(thread);
#endif // _LP64

  int call_offset;
  if (!align_stack) {
    set_last_Java_frame(thread, noreg, rbp, NULL);
  } else {
    address the_pc = pc();
    call_offset = offset();
    set_last_Java_frame(thread, noreg, rbp, the_pc);
    andptr(rsp, -(StackAlignmentInBytes));    // Align stack
  }

  // do the call
  call(RuntimeAddress(entry));
  if (!align_stack) {
    call_offset = offset();
  }
  // verify callee-saved register
#ifdef ASSERT
  guarantee(thread != rax, "change this code");
  push(rax);
  { Label L;
    get_thread(rax);
    cmpptr(thread, rax);
    jcc(Assembler::equal, L);
    int3();
    stop("GraalStubAssembler::call_RT: rdi not callee saved?");
    bind(L);
  }
  pop(rax);
#endif
  reset_last_Java_frame(thread, true, align_stack);

  // discard thread and arguments
  NOT_LP64(addptr(rsp, num_rt_args()*BytesPerWord));

  // check for pending exceptions
  { Label L;
    cmpptr(Address(thread, Thread::pending_exception_offset()), (int32_t)NULL_WORD);
    jcc(Assembler::equal, L);
    // exception pending => remove activation and forward to exception handler
    movptr(rax, Address(thread, Thread::pending_exception_offset()));
    // make sure that the vm_results are cleared
    if (oop_result1->is_valid()) {
      movptr(Address(thread, JavaThread::vm_result_offset()), NULL_WORD);
    }
    if (metadata_result->is_valid()) {
      movptr(Address(thread, JavaThread::vm_result_2_offset()), NULL_WORD);
    }
    // (thomaswue) Deoptimize in case of an exception.
    restore_live_registers(this, false);
    movptr(Address(thread, Thread::pending_exception_offset()), NULL_WORD);
    leave();
    movl(rscratch1, Deoptimization::make_trap_request(Deoptimization::Reason_constraint, Deoptimization::Action_reinterpret));
    jump(RuntimeAddress(SharedRuntime::deopt_blob()->uncommon_trap()));
    bind(L);
  }
  // get oop results if there are any and reset the values in the thread
  if (oop_result1->is_valid()) {
    get_vm_result(oop_result1, thread);
  }
  if (metadata_result->is_valid()) {
    get_vm_result_2(metadata_result, thread);
  }
  return call_offset;
}


int GraalStubAssembler::call_RT(Register oop_result1, Register metadata_result, address entry, Register arg1) {
#ifdef _LP64
  mov(c_rarg1, arg1);
#else
  push(arg1);
#endif // _LP64
  return call_RT(oop_result1, metadata_result, entry, 1);
}


int GraalStubAssembler::call_RT(Register oop_result1, Register metadata_result, address entry, Register arg1, Register arg2) {
#ifdef _LP64
  if (c_rarg1 == arg2) {
    if (c_rarg2 == arg1) {
      xchgq(arg1, arg2);
    } else {
      mov(c_rarg2, arg2);
      mov(c_rarg1, arg1);
    }
  } else {
    mov(c_rarg1, arg1);
    mov(c_rarg2, arg2);
  }
#else
  push(arg2);
  push(arg1);
#endif // _LP64
  return call_RT(oop_result1, metadata_result, entry, 2);
}


int GraalStubAssembler::call_RT(Register oop_result1, Register metadata_result, address entry, Register arg1, Register arg2, Register arg3) {
#ifdef _LP64
  // if there is any conflict use the stack
  if (arg1 == c_rarg2 || arg1 == c_rarg3 ||
      arg2 == c_rarg1 || arg1 == c_rarg3 ||
      arg3 == c_rarg1 || arg1 == c_rarg2) {
    push(arg3);
    push(arg2);
    push(arg1);
    pop(c_rarg1);
    pop(c_rarg2);
    pop(c_rarg3);
  } else {
    mov(c_rarg1, arg1);
    mov(c_rarg2, arg2);
    mov(c_rarg3, arg3);
  }
#else
  push(arg3);
  push(arg2);
  push(arg1);
#endif // _LP64
  return call_RT(oop_result1, metadata_result, entry, 3);
}

int GraalStubAssembler::call_RT(Register oop_result1, Register metadata_result, address entry, Register arg1, Register arg2, Register arg3, Register arg4) {
#ifdef _LP64
  #ifdef _WIN64
    // on windows we only have the registers c_rarg0 to c_rarg3 for transferring parameters -> remaining parameters are on the stack
    if (arg1 == c_rarg2 || arg1 == c_rarg3 || 
        arg2 == c_rarg1 || arg2 == c_rarg3 || 
        arg3 == c_rarg1 || arg3 == c_rarg2 || 
        arg4 == c_rarg1 || arg4 == c_rarg2) {
      push(arg4);
      push(arg3);
      push(arg2);
      push(arg1);
      pop(c_rarg1);
      pop(c_rarg2);
      pop(c_rarg3);
    } else {
      mov(c_rarg1, arg1);
      mov(c_rarg2, arg2);
      mov(c_rarg3, arg3);
      push(arg4);
    }
  #else
    // if there is any conflict use the stack
    if (arg1 == c_rarg2 || arg1 == c_rarg3 || arg1 == c_rarg4 ||
        arg2 == c_rarg1 || arg2 == c_rarg3 || arg2 == c_rarg4 ||
        arg3 == c_rarg1 || arg3 == c_rarg2 || arg3 == c_rarg4 ||
        arg4 == c_rarg1 || arg4 == c_rarg2 || arg4 == c_rarg3) {
      push(arg4);
      push(arg3);
      push(arg2);
      push(arg1);
      pop(c_rarg1);
      pop(c_rarg2);
      pop(c_rarg3);
      pop(c_rarg4);
    } else {
      mov(c_rarg1, arg1);
      mov(c_rarg2, arg2);
      mov(c_rarg3, arg3);
      mov(c_rarg4, arg4);
    }
  #endif
#else
  push(arg4);
  push(arg3);
  push(arg2);
  push(arg1);
#endif // _LP64
  return call_RT(oop_result1, metadata_result, entry, 4);
}

// Implementation of GraalStubFrame

class GraalStubFrame: public StackObj {
 private:
  GraalStubAssembler* _sasm;

 public:
  GraalStubFrame(GraalStubAssembler* sasm, const char* name, bool must_gc_arguments);
  ~GraalStubFrame();
};


#define __ _sasm->

GraalStubFrame::GraalStubFrame(GraalStubAssembler* sasm, const char* name, bool must_gc_arguments) {
  _sasm = sasm;
  __ set_info(name, must_gc_arguments);
  __ enter();
}

GraalStubFrame::~GraalStubFrame() {
  __ leave();
  __ ret(0);
}

#undef __


// Implementation of GraalRuntime

const int float_regs_as_doubles_size_in_slots = FloatRegisterImpl::number_of_registers * 2;
const int xmm_regs_as_doubles_size_in_slots = XMMRegisterImpl::number_of_registers * 2;

// Stack layout for saving/restoring  all the registers needed during a runtime
// call (this includes deoptimization)
// Note: note that users of this frame may well have arguments to some runtime
// while these values are on the stack. These positions neglect those arguments
// but the code in save_live_registers will take the argument count into
// account.
//
#ifdef _LP64
  #define SLOT2(x) x,
  #define SLOT_PER_WORD 2
#else
  #define SLOT2(x)
  #define SLOT_PER_WORD 1
#endif // _LP64

enum reg_save_layout {
  // 64bit needs to keep stack 16 byte aligned. So we add some alignment dummies to make that
  // happen and will assert if the stack size we create is misaligned
#ifdef _LP64
  align_dummy_0, align_dummy_1,
#endif // _LP64
#ifdef _WIN64
  // Windows always allocates space for it's argument registers (see
  // frame::arg_reg_save_area_bytes).
  arg_reg_save_1, arg_reg_save_1H,                                                          // 0, 4
  arg_reg_save_2, arg_reg_save_2H,                                                          // 8, 12
  arg_reg_save_3, arg_reg_save_3H,                                                          // 16, 20
  arg_reg_save_4, arg_reg_save_4H,                                                          // 24, 28
#endif // _WIN64
  xmm_regs_as_doubles_off,                                                                  // 32
  float_regs_as_doubles_off = xmm_regs_as_doubles_off + xmm_regs_as_doubles_size_in_slots,  // 160
  fpu_state_off = float_regs_as_doubles_off + float_regs_as_doubles_size_in_slots,          // 224
  // fpu_state_end_off is exclusive
  fpu_state_end_off = fpu_state_off + (FPUStateSizeInWords / SLOT_PER_WORD),                // 352
  marker = fpu_state_end_off, SLOT2(markerH)                                                // 352, 356
  extra_space_offset,                                                                       // 360
#ifdef _LP64
  r15_off = extra_space_offset, r15H_off,                                                   // 360, 364
  r14_off, r14H_off,                                                                        // 368, 372
  r13_off, r13H_off,                                                                        // 376, 380
  r12_off, r12H_off,                                                                        // 384, 388
  r11_off, r11H_off,                                                                        // 392, 396
  r10_off, r10H_off,                                                                        // 400, 404
  r9_off, r9H_off,                                                                          // 408, 412
  r8_off, r8H_off,                                                                          // 416, 420
  rdi_off, rdiH_off,                                                                        // 424, 428
#else
  rdi_off = extra_space_offset,
#endif // _LP64
  rsi_off, SLOT2(rsiH_off)                                                                  // 432, 436
  rbp_off, SLOT2(rbpH_off)                                                                  // 440, 444
  rsp_off, SLOT2(rspH_off)                                                                  // 448, 452
  rbx_off, SLOT2(rbxH_off)                                                                  // 456, 460
  rdx_off, SLOT2(rdxH_off)                                                                  // 464, 468
  rcx_off, SLOT2(rcxH_off)                                                                  // 472, 476
  rax_off, SLOT2(raxH_off)                                                                  // 480, 484
  saved_rbp_off, SLOT2(saved_rbpH_off)                                                      // 488, 492
  return_off, SLOT2(returnH_off)                                                            // 496, 500
  reg_save_frame_size   // As noted: neglects any parameters to runtime                     // 504
};

// Save registers which might be killed by calls into the runtime.
// Tries to smart about FP registers.  In particular we separate
// saving and describing the FPU registers for deoptimization since we
// have to save the FPU registers twice if we describe them and on P4
// saving FPU registers which don't contain anything appears
// expensive.  The deopt blob is the only thing which needs to
// describe FPU registers.  In all other cases it should be sufficient
// to simply save their current value.

static OopMap* generate_oop_map(GraalStubAssembler* sasm, int num_rt_args,
                                bool save_fpu_registers = true) {

  // In 64bit all the args are in regs so there are no additional stack slots
  LP64_ONLY(num_rt_args = 0);
  LP64_ONLY(assert((reg_save_frame_size * VMRegImpl::stack_slot_size) % 16 == 0, "must be 16 byte aligned");)
  int frame_size_in_slots = reg_save_frame_size + num_rt_args; // args + thread
  sasm->set_frame_size(frame_size_in_slots / VMRegImpl::slots_per_word );

  // record saved value locations in an OopMap
  // locations are offsets from sp after runtime call; num_rt_args is number of arguments in call, including thread
  OopMap* map = new OopMap(frame_size_in_slots, 0);
  map->set_callee_saved(VMRegImpl::stack2reg(rax_off + num_rt_args), rax->as_VMReg());
  map->set_callee_saved(VMRegImpl::stack2reg(rcx_off + num_rt_args), rcx->as_VMReg());
  map->set_callee_saved(VMRegImpl::stack2reg(rdx_off + num_rt_args), rdx->as_VMReg());
  map->set_callee_saved(VMRegImpl::stack2reg(rbx_off + num_rt_args), rbx->as_VMReg());
  map->set_callee_saved(VMRegImpl::stack2reg(rsi_off + num_rt_args), rsi->as_VMReg());
  map->set_callee_saved(VMRegImpl::stack2reg(rdi_off + num_rt_args), rdi->as_VMReg());
#ifdef _LP64
  map->set_callee_saved(VMRegImpl::stack2reg(r8_off + num_rt_args),  r8->as_VMReg());
  map->set_callee_saved(VMRegImpl::stack2reg(r9_off + num_rt_args),  r9->as_VMReg());
  map->set_callee_saved(VMRegImpl::stack2reg(r10_off + num_rt_args), r10->as_VMReg());
  map->set_callee_saved(VMRegImpl::stack2reg(r11_off + num_rt_args), r11->as_VMReg());
  map->set_callee_saved(VMRegImpl::stack2reg(r12_off + num_rt_args), r12->as_VMReg());
  map->set_callee_saved(VMRegImpl::stack2reg(r13_off + num_rt_args), r13->as_VMReg());
  map->set_callee_saved(VMRegImpl::stack2reg(r14_off + num_rt_args), r14->as_VMReg());
  map->set_callee_saved(VMRegImpl::stack2reg(r15_off + num_rt_args), r15->as_VMReg());

  // This is stupid but needed.
  map->set_callee_saved(VMRegImpl::stack2reg(raxH_off + num_rt_args), rax->as_VMReg()->next());
  map->set_callee_saved(VMRegImpl::stack2reg(rcxH_off + num_rt_args), rcx->as_VMReg()->next());
  map->set_callee_saved(VMRegImpl::stack2reg(rdxH_off + num_rt_args), rdx->as_VMReg()->next());
  map->set_callee_saved(VMRegImpl::stack2reg(rbxH_off + num_rt_args), rbx->as_VMReg()->next());
  map->set_callee_saved(VMRegImpl::stack2reg(rsiH_off + num_rt_args), rsi->as_VMReg()->next());
  map->set_callee_saved(VMRegImpl::stack2reg(rdiH_off + num_rt_args), rdi->as_VMReg()->next());

  map->set_callee_saved(VMRegImpl::stack2reg(r8H_off + num_rt_args),  r8->as_VMReg()->next());
  map->set_callee_saved(VMRegImpl::stack2reg(r9H_off + num_rt_args),  r9->as_VMReg()->next());
  map->set_callee_saved(VMRegImpl::stack2reg(r10H_off + num_rt_args), r10->as_VMReg()->next());
  map->set_callee_saved(VMRegImpl::stack2reg(r11H_off + num_rt_args), r11->as_VMReg()->next());
  map->set_callee_saved(VMRegImpl::stack2reg(r12H_off + num_rt_args), r12->as_VMReg()->next());
  map->set_callee_saved(VMRegImpl::stack2reg(r13H_off + num_rt_args), r13->as_VMReg()->next());
  map->set_callee_saved(VMRegImpl::stack2reg(r14H_off + num_rt_args), r14->as_VMReg()->next());
  map->set_callee_saved(VMRegImpl::stack2reg(r15H_off + num_rt_args), r15->as_VMReg()->next());
#endif // _LP64

  if (save_fpu_registers) {
    if (UseSSE < 2) {
      int fpu_off = float_regs_as_doubles_off;
      for (int n = 0; n < FloatRegisterImpl::number_of_registers; n++) {
        VMReg fpu_name_0 = as_FloatRegister(n)->as_VMReg();
        map->set_callee_saved(VMRegImpl::stack2reg(fpu_off +     num_rt_args), fpu_name_0);
        // %%% This is really a waste but we'll keep things as they were for now
        if (true) {
          map->set_callee_saved(VMRegImpl::stack2reg(fpu_off + 1 + num_rt_args), fpu_name_0->next());
        }
        fpu_off += 2;
      }
      assert(fpu_off == fpu_state_off, "incorrect number of fpu stack slots");
    }

    if (UseSSE >= 2) {
      int xmm_off = xmm_regs_as_doubles_off;
      for (int n = 0; n < XMMRegisterImpl::number_of_registers; n++) {
        VMReg xmm_name_0 = as_XMMRegister(n)->as_VMReg();
        map->set_callee_saved(VMRegImpl::stack2reg(xmm_off +     num_rt_args), xmm_name_0);
        // %%% This is really a waste but we'll keep things as they were for now
        if (true) {
          map->set_callee_saved(VMRegImpl::stack2reg(xmm_off + 1 + num_rt_args), xmm_name_0->next());
        }
        xmm_off += 2;
      }
      assert(xmm_off == float_regs_as_doubles_off, "incorrect number of xmm registers");

    } else if (UseSSE == 1) {
      int xmm_off = xmm_regs_as_doubles_off;
      for (int n = 0; n < XMMRegisterImpl::number_of_registers; n++) {
        VMReg xmm_name_0 = as_XMMRegister(n)->as_VMReg();
        map->set_callee_saved(VMRegImpl::stack2reg(xmm_off +     num_rt_args), xmm_name_0);
        xmm_off += 2;
      }
      assert(xmm_off == float_regs_as_doubles_off, "incorrect number of xmm registers");
    }
  }

  return map;
}

#define __ sasm->

static OopMap* save_live_registers(GraalStubAssembler* sasm, int num_rt_args,
                                   bool save_fpu_registers = true) {
  __ block_comment("save_live_registers");

  __ pusha();         // integer registers

  // assert(float_regs_as_doubles_off % 2 == 0, "misaligned offset");
  // assert(xmm_regs_as_doubles_off % 2 == 0, "misaligned offset");

  __ subptr(rsp, extra_space_offset * VMRegImpl::stack_slot_size);

#ifdef ASSERT
  __ movptr(Address(rsp, marker * VMRegImpl::stack_slot_size), (int32_t)0xfeedbeef);
#endif

  if (save_fpu_registers) {
    if (UseSSE < 2) {
      // save FPU stack
      __ fnsave(Address(rsp, fpu_state_off * VMRegImpl::stack_slot_size));
      __ fwait();

#ifdef ASSERT
      Label ok;
      __ cmpw(Address(rsp, fpu_state_off * VMRegImpl::stack_slot_size), StubRoutines::fpu_cntrl_wrd_std());
      __ jccb(Assembler::equal, ok);
      __ stop("corrupted control word detected");
      __ bind(ok);
#endif

      // Reset the control word to guard against exceptions being unmasked
      // since fstp_d can cause FPU stack underflow exceptions.  Write it
      // into the on stack copy and then reload that to make sure that the
      // current and future values are correct.
      __ movw(Address(rsp, fpu_state_off * VMRegImpl::stack_slot_size), StubRoutines::fpu_cntrl_wrd_std());
      __ frstor(Address(rsp, fpu_state_off * VMRegImpl::stack_slot_size));

      // Save the FPU registers in de-opt-able form
      __ fstp_d(Address(rsp, float_regs_as_doubles_off * VMRegImpl::stack_slot_size +  0));
      __ fstp_d(Address(rsp, float_regs_as_doubles_off * VMRegImpl::stack_slot_size +  8));
      __ fstp_d(Address(rsp, float_regs_as_doubles_off * VMRegImpl::stack_slot_size + 16));
      __ fstp_d(Address(rsp, float_regs_as_doubles_off * VMRegImpl::stack_slot_size + 24));
      __ fstp_d(Address(rsp, float_regs_as_doubles_off * VMRegImpl::stack_slot_size + 32));
      __ fstp_d(Address(rsp, float_regs_as_doubles_off * VMRegImpl::stack_slot_size + 40));
      __ fstp_d(Address(rsp, float_regs_as_doubles_off * VMRegImpl::stack_slot_size + 48));
      __ fstp_d(Address(rsp, float_regs_as_doubles_off * VMRegImpl::stack_slot_size + 56));
    }

    if (UseSSE >= 2) {
      // save XMM registers
      // XMM registers can contain float or double values, but this is not known here,
      // so always save them as doubles.
      // note that float values are _not_ converted automatically, so for float values
      // the second word contains only garbage data.
      __ movdbl(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size +  0), xmm0);
      __ movdbl(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size +  8), xmm1);
      __ movdbl(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 16), xmm2);
      __ movdbl(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 24), xmm3);
      __ movdbl(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 32), xmm4);
      __ movdbl(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 40), xmm5);
      __ movdbl(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 48), xmm6);
      __ movdbl(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 56), xmm7);
#ifdef _LP64
      __ movdbl(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 64), xmm8);
      __ movdbl(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 72), xmm9);
      __ movdbl(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 80), xmm10);
      __ movdbl(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 88), xmm11);
      __ movdbl(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 96), xmm12);
      __ movdbl(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 104), xmm13);
      __ movdbl(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 112), xmm14);
      __ movdbl(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 120), xmm15);
#endif // _LP64
    } else if (UseSSE == 1) {
      // save XMM registers as float because double not supported without SSE2
      __ movflt(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size +  0), xmm0);
      __ movflt(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size +  8), xmm1);
      __ movflt(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 16), xmm2);
      __ movflt(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 24), xmm3);
      __ movflt(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 32), xmm4);
      __ movflt(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 40), xmm5);
      __ movflt(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 48), xmm6);
      __ movflt(Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 56), xmm7);
    }
  }

  // FPU stack must be empty now
  __ verify_FPU(0, "save_live_registers");

  return generate_oop_map(sasm, num_rt_args, save_fpu_registers);
}


static void restore_fpu(GraalStubAssembler* sasm, bool restore_fpu_registers = true) {
  if (restore_fpu_registers) {
    if (UseSSE >= 2) {
      // restore XMM registers
      __ movdbl(xmm0, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size +  0));
      __ movdbl(xmm1, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size +  8));
      __ movdbl(xmm2, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 16));
      __ movdbl(xmm3, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 24));
      __ movdbl(xmm4, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 32));
      __ movdbl(xmm5, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 40));
      __ movdbl(xmm6, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 48));
      __ movdbl(xmm7, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 56));
#ifdef _LP64
      __ movdbl(xmm8, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 64));
      __ movdbl(xmm9, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 72));
      __ movdbl(xmm10, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 80));
      __ movdbl(xmm11, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 88));
      __ movdbl(xmm12, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 96));
      __ movdbl(xmm13, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 104));
      __ movdbl(xmm14, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 112));
      __ movdbl(xmm15, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 120));
#endif // _LP64
    } else if (UseSSE == 1) {
      // restore XMM registers
      __ movflt(xmm0, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size +  0));
      __ movflt(xmm1, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size +  8));
      __ movflt(xmm2, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 16));
      __ movflt(xmm3, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 24));
      __ movflt(xmm4, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 32));
      __ movflt(xmm5, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 40));
      __ movflt(xmm6, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 48));
      __ movflt(xmm7, Address(rsp, xmm_regs_as_doubles_off * VMRegImpl::stack_slot_size + 56));
    }

    if (UseSSE < 2) {
      __ frstor(Address(rsp, fpu_state_off * VMRegImpl::stack_slot_size));
    } else {
      // check that FPU stack is really empty
      __ verify_FPU(0, "restore_live_registers");
    }

  } else {
    // check that FPU stack is really empty
    __ verify_FPU(0, "restore_live_registers");
  }

#ifdef ASSERT
  {
    Label ok;
    __ cmpptr(Address(rsp, marker * VMRegImpl::stack_slot_size), (int32_t)0xfeedbeef);
    __ jcc(Assembler::equal, ok);
    __ stop("bad offsets in frame");
    __ bind(ok);
  }
#endif // ASSERT

  __ addptr(rsp, extra_space_offset * VMRegImpl::stack_slot_size);
}


static void restore_live_registers(GraalStubAssembler* sasm, bool restore_fpu_registers/* = true*/) {
  __ block_comment("restore_live_registers");

  restore_fpu(sasm, restore_fpu_registers);
  __ popa();
}


static void restore_live_registers_except_rax(GraalStubAssembler* sasm, bool restore_fpu_registers = true) {
  __ block_comment("restore_live_registers_except_rax");

  restore_fpu(sasm, restore_fpu_registers);

#ifdef _LP64
  __ movptr(r15, Address(rsp, 0));
  __ movptr(r14, Address(rsp, wordSize));
  __ movptr(r13, Address(rsp, 2 * wordSize));
  __ movptr(r12, Address(rsp, 3 * wordSize));
  __ movptr(r11, Address(rsp, 4 * wordSize));
  __ movptr(r10, Address(rsp, 5 * wordSize));
  __ movptr(r9,  Address(rsp, 6 * wordSize));
  __ movptr(r8,  Address(rsp, 7 * wordSize));
  __ movptr(rdi, Address(rsp, 8 * wordSize));
  __ movptr(rsi, Address(rsp, 9 * wordSize));
  __ movptr(rbp, Address(rsp, 10 * wordSize));
  // skip rsp
  __ movptr(rbx, Address(rsp, 12 * wordSize));
  __ movptr(rdx, Address(rsp, 13 * wordSize));
  __ movptr(rcx, Address(rsp, 14 * wordSize));

  __ addptr(rsp, 16 * wordSize);
#else

  __ pop(rdi);
  __ pop(rsi);
  __ pop(rbp);
  __ pop(rbx); // skip this value
  __ pop(rbx);
  __ pop(rdx);
  __ pop(rcx);
  __ addptr(rsp, BytesPerWord);
#endif // _LP64
}

OopMapSet* GraalRuntime::generate_handle_exception(StubID id, GraalStubAssembler *sasm) {
  __ block_comment("generate_handle_exception");

  // incoming parameters
  const Register exception_oop = rax;
  const Register exception_pc  = rdx;
  // other registers used in this stub
  const Register thread = NOT_LP64(rdi) LP64_ONLY(r15_thread);

  // Save registers, if required.
  OopMapSet* oop_maps = new OopMapSet();
  OopMap* oop_map = NULL;
  switch (id) {
    case graal_handle_exception_nofpu_id:
      // At this point all registers MAY be live.
      oop_map = save_live_registers(sasm, 1 /*thread*/, id == graal_handle_exception_nofpu_id);
      break;
    default:  ShouldNotReachHere();
  }

#ifdef TIERED
  // C2 can leave the fpu stack dirty
  if (UseSSE < 2) {
    __ empty_FPU_stack();
  }
#endif // TIERED

  // verify that only rax, and rdx is valid at this time
#ifdef ASSERT
  __ movptr(rbx, 0xDEAD);
  __ movptr(rcx, 0xDEAD);
  __ movptr(rsi, 0xDEAD);
  __ movptr(rdi, 0xDEAD);
#endif

  // verify that rax, contains a valid exception
  __ verify_not_null_oop(exception_oop);

  // load address of JavaThread object for thread-local data
  NOT_LP64(__ get_thread(thread);)

#ifdef ASSERT
  // check that fields in JavaThread for exception oop and issuing pc are
  // empty before writing to them
  Label oop_empty;
  __ cmpptr(Address(thread, JavaThread::exception_oop_offset()), (int32_t) NULL_WORD);
  __ jcc(Assembler::equal, oop_empty);
  __ stop("exception oop already set");
  __ bind(oop_empty);

  Label pc_empty;
  __ cmpptr(Address(thread, JavaThread::exception_pc_offset()), 0);
  __ jcc(Assembler::equal, pc_empty);
  __ stop("exception pc already set");
  __ bind(pc_empty);
#endif

  // save exception oop and issuing pc into JavaThread
  // (exception handler will load it from here)
  __ movptr(Address(thread, JavaThread::exception_oop_offset()), exception_oop);
  __ movptr(Address(thread, JavaThread::exception_pc_offset()),  exception_pc);

  // patch throwing pc into return address (has bci & oop map)
  __ movptr(Address(rbp, 1*BytesPerWord), exception_pc);

  // compute the exception handler.
  // the exception oop and the throwing pc are read from the fields in JavaThread
  int call_offset = __ call_RT(noreg, noreg, CAST_FROM_FN_PTR(address, exception_handler_for_pc));
  oop_maps->add_gc_map(call_offset, oop_map);

  // rax: handler address
  //      will be the deopt blob if nmethod was deoptimized while we looked up
  //      handler regardless of whether handler existed in the nmethod.

  // only rax, is valid at this time, all other registers have been destroyed by the runtime call
#ifdef ASSERT
  __ movptr(rbx, 0xDEAD);
  __ movptr(rcx, 0xDEAD);
  __ movptr(rdx, 0xDEAD);
  __ movptr(rsi, 0xDEAD);
  __ movptr(rdi, 0xDEAD);
#endif

  // patch the return address, this stub will directly return to the exception handler
  __ movptr(Address(rbp, 1*BytesPerWord), rax);

  switch (id) {
    case graal_handle_exception_nofpu_id:
      // Restore the registers that were saved at the beginning.
      restore_live_registers(sasm, id == graal_handle_exception_nofpu_id);
      break;
    default:  ShouldNotReachHere();
  }

  return oop_maps;
}

void GraalRuntime::generate_unwind_exception(GraalStubAssembler *sasm) {
  // incoming parameters
  const Register exception_oop = rax;
  // callee-saved copy of exception_oop during runtime call
  const Register exception_oop_callee_saved = NOT_LP64(rsi) LP64_ONLY(r14);
  // other registers used in this stub
  const Register exception_pc = rdx;
  const Register handler_addr = rbx;
  const Register thread = NOT_LP64(rdi) LP64_ONLY(r15_thread);

  // verify that only rax is valid at this time
#ifdef ASSERT
  __ movptr(rbx, 0xDEAD);
  __ movptr(rcx, 0xDEAD);
  __ movptr(rdx, 0xDEAD);
  __ movptr(rsi, 0xDEAD);
  __ movptr(rdi, 0xDEAD);
#endif

#ifdef ASSERT
  // check that fields in JavaThread for exception oop and issuing pc are empty
  NOT_LP64(__ get_thread(thread);)
  Label oop_empty;
  __ cmpptr(Address(thread, JavaThread::exception_oop_offset()), 0);
  __ jcc(Assembler::equal, oop_empty);
  __ stop("exception oop must be empty");
  __ bind(oop_empty);

  Label pc_empty;
  __ cmpptr(Address(thread, JavaThread::exception_pc_offset()), 0);
  __ jcc(Assembler::equal, pc_empty);
  __ stop("exception pc must be empty");
  __ bind(pc_empty);
#endif

  // clear the FPU stack in case any FPU results are left behind
  __ empty_FPU_stack();

  // save exception_oop in callee-saved register to preserve it during runtime calls
  __ verify_not_null_oop(exception_oop);
  __ movptr(exception_oop_callee_saved, exception_oop);

  NOT_LP64(__ get_thread(thread);)
  // Get return address (is on top of stack after leave).
  __ movptr(exception_pc, Address(rsp, 0));

  // search the exception handler address of the caller (using the return address)
  __ call_VM_leaf(CAST_FROM_FN_PTR(address, SharedRuntime::exception_handler_for_return_address), thread, exception_pc);
  // rax: exception handler address of the caller

  // Only RAX and RSI are valid at this time, all other registers have been destroyed by the call.
#ifdef ASSERT
  __ movptr(rbx, 0xDEAD);
  __ movptr(rcx, 0xDEAD);
  __ movptr(rdx, 0xDEAD);
  __ movptr(rdi, 0xDEAD);
#endif

  // move result of call into correct register
  __ movptr(handler_addr, rax);

  // Restore exception oop to RAX (required convention of exception handler).
  __ movptr(exception_oop, exception_oop_callee_saved);

  // verify that there is really a valid exception in rax
  __ verify_not_null_oop(exception_oop);

  // get throwing pc (= return address).
  // rdx has been destroyed by the call, so it must be set again
  // the pop is also necessary to simulate the effect of a ret(0)
  __ pop(exception_pc);

  // Restore SP from BP if the exception PC is a method handle call site.
  NOT_LP64(__ get_thread(thread);)
  __ cmpl(Address(thread, JavaThread::is_method_handle_return_offset()), 0);
  __ cmovptr(Assembler::notEqual, rsp, rbp_mh_SP_save);

  // continue at exception handler (return address removed)
  // note: do *not* remove arguments when unwinding the
  //       activation since the caller assumes having
  //       all arguments on the stack when entering the
  //       runtime to determine the exception handler
  //       (GC happens at call site with arguments!)
  // rax: exception oop
  // rdx: throwing pc
  // rbx: exception handler
  __ jmp(handler_addr);
}

OopMapSet* GraalRuntime::generate_code_for(StubID id, GraalStubAssembler* sasm) {

  // for better readability
  const bool must_gc_arguments = true;
  const bool dont_gc_arguments = false;

  // default value; overwritten for some optimized stubs that are called from methods that do not use the fpu
  bool save_fpu_registers = true;

  // stub code & info for the different stubs
  OopMapSet* oop_maps = NULL;
  switch (id) {

    case graal_new_instance_id:
      {
        Register klass = rdx; // Incoming
        Register obj   = rax; // Result
        __ set_info("new_instance", dont_gc_arguments);
        __ enter();
        OopMap* map = save_live_registers(sasm, 2);
        int call_offset = __ call_RT(obj, noreg, CAST_FROM_FN_PTR(address, new_instance), klass);
        oop_maps = new OopMapSet();
        oop_maps->add_gc_map(call_offset, map);
        restore_live_registers_except_rax(sasm);
        __ verify_oop(obj);
        __ leave();
        __ ret(0);

        // rax,: new instance
      }

      break;

    case graal_new_array_id:
      {
        Register length   = rbx; // Incoming
        Register klass    = rdx; // Incoming
        Register obj      = rax; // Result

        __ set_info("new_array", dont_gc_arguments);

        __ enter();
        OopMap* map = save_live_registers(sasm, 3);
        int call_offset;
        call_offset = __ call_RT(obj, noreg, CAST_FROM_FN_PTR(address, new_array), klass, length);

        oop_maps = new OopMapSet();
        oop_maps->add_gc_map(call_offset, map);
        restore_live_registers_except_rax(sasm);

        __ verify_oop(obj);
        __ leave();
        __ ret(0);

        // rax,: new array
      }
      break;

    case graal_new_multi_array_id:
      { GraalStubFrame f(sasm, "new_multi_array", dont_gc_arguments);
        // rax,: klass
        // rbx,: rank
        // rcx: address of 1st dimension
        OopMap* map = save_live_registers(sasm, 4);
        int call_offset = __ call_RT(rax, noreg, CAST_FROM_FN_PTR(address, new_multi_array), rax, rbx, rcx);

        oop_maps = new OopMapSet();
        oop_maps->add_gc_map(call_offset, map);
        restore_live_registers_except_rax(sasm);

        // rax,: new multi array
        __ verify_oop(rax);
      }
      break;

    case graal_register_finalizer_id:
      {
        __ set_info("register_finalizer", dont_gc_arguments);

        // This is called via call_runtime so the arguments
        // will be place in C abi locations

#ifdef _LP64
        __ verify_oop(j_rarg0);
        __ mov(rax, j_rarg0);
#else
        // The object is passed on the stack and we haven't pushed a
        // frame yet so it's one work away from top of stack.
        __ movptr(rax, Address(rsp, 1 * BytesPerWord));
        __ verify_oop(rax);
#endif // _LP64

        // load the klass and check the has finalizer flag
        Label register_finalizer;
        Register t = rsi;
        __ load_klass(t, rax);
        __ movl(t, Address(t, Klass::access_flags_offset()));
        __ testl(t, JVM_ACC_HAS_FINALIZER);
        __ jcc(Assembler::notZero, register_finalizer);
        __ ret(0);

        __ bind(register_finalizer);
        __ enter();
        OopMap* oop_map = save_live_registers(sasm, 2 /*num_rt_args */);
        int call_offset = __ call_RT(noreg, noreg, CAST_FROM_FN_PTR(address, SharedRuntime::register_finalizer), rax);
        oop_maps = new OopMapSet();
        oop_maps->add_gc_map(call_offset, oop_map);

        // Now restore all the live registers
        restore_live_registers(sasm);

        __ leave();
        __ ret(0);
      }
      break;

    case graal_handle_exception_nofpu_id:
      { GraalStubFrame f(sasm, "handle_exception", dont_gc_arguments);
        oop_maps = generate_handle_exception(id, sasm);
      }
      break;

    case graal_slow_subtype_check_id:
      {
        // Typical calling sequence:
        // __ push(klass_RInfo);  // object klass or other subclass
        // __ push(sup_k_RInfo);  // array element klass or other superclass
        // __ call(slow_subtype_check);
        // Note that the subclass is pushed first, and is therefore deepest.
        // Previous versions of this code reversed the names 'sub' and 'super'.
        // This was operationally harmless but made the code unreadable.
        enum layout {
          rax_off, SLOT2(raxH_off)
          rcx_off, SLOT2(rcxH_off)
          rsi_off, SLOT2(rsiH_off)
          rdi_off, SLOT2(rdiH_off)
          // saved_rbp_off, SLOT2(saved_rbpH_off)
          return_off, SLOT2(returnH_off)
          sup_k_off, SLOT2(sup_kH_off)
          klass_off, SLOT2(superH_off)
          framesize,
          result_off = klass_off  // deepest argument is also the return value
        };

        __ set_info("slow_subtype_check", dont_gc_arguments);
        __ push(rdi);
        __ push(rsi);
        __ push(rcx);
        __ push(rax);

        // This is called by pushing args and not with C abi
        __ movptr(rsi, Address(rsp, (klass_off) * VMRegImpl::stack_slot_size)); // subclass
        __ movptr(rax, Address(rsp, (sup_k_off) * VMRegImpl::stack_slot_size)); // superclass

        Label miss;
        Label success;
        __ check_klass_subtype_fast_path(rsi, rax, rcx, &success, &miss, NULL);

        __ check_klass_subtype_slow_path(rsi, rax, rcx, rdi, NULL, &miss);

        // fallthrough on success:
        __ bind(success);
        __ movptr(Address(rsp, (result_off) * VMRegImpl::stack_slot_size), 1); // result
        __ pop(rax);
        __ pop(rcx);
        __ pop(rsi);
        __ pop(rdi);
        __ ret(0);

        __ bind(miss);
        __ movptr(Address(rsp, (result_off) * VMRegImpl::stack_slot_size), NULL_WORD); // result
        __ pop(rax);
        __ pop(rcx);
        __ pop(rsi);
        __ pop(rdi);
        __ ret(0);
      }
      break;

    case graal_unwind_exception_call_id: {
      // remove the frame from the stack
      __ movptr(rsp, rbp);
      __ pop(rbp);
      // exception_oop is passed using ordinary java calling conventions
      __ movptr(rax, j_rarg0);

      Label nonNullExceptionOop;
      __ testptr(rax, rax);
      __ jcc(Assembler::notZero, nonNullExceptionOop);
      {
        __ enter();
        oop_maps = new OopMapSet();
        OopMap* oop_map = save_live_registers(sasm, 0);
        int call_offset = __ call_RT(rax, noreg, (address)graal_create_null_exception, 0);
        oop_maps->add_gc_map(call_offset, oop_map);
        __ leave();
      }
      __ bind(nonNullExceptionOop);

      __ set_info("unwind_exception", dont_gc_arguments);
      // note: no stubframe since we are about to leave the current
      //       activation and we are calling a leaf VM function only.
      generate_unwind_exception(sasm);
      __ should_not_reach_here();
      break;
    }

    case graal_OSR_migration_end_id: {
    __ enter();
    save_live_registers(sasm, 0);
    __ movptr(c_rarg0, j_rarg0);
    __ call(RuntimeAddress(CAST_FROM_FN_PTR(address, SharedRuntime::OSR_migration_end)));
    restore_live_registers(sasm);
    __ leave();
    __ ret(0);
      break;
    }

    case graal_set_deopt_info_id: {
    __ movptr(Address(r15_thread, JavaThread::graal_deopt_info_offset()), rscratch1);
    __ ret(0);
      break;
    }

    case graal_create_null_pointer_exception_id: {
		__ enter();
		oop_maps = new OopMapSet();
		OopMap* oop_map = save_live_registers(sasm, 0);
		int call_offset = __ call_RT(rax, noreg, (address)graal_create_null_exception, 0);
		oop_maps->add_gc_map(call_offset, oop_map);
		__ leave();
		__ ret(0);
      break;
    }

    case graal_create_out_of_bounds_exception_id: {
		__ enter();
		oop_maps = new OopMapSet();
		OopMap* oop_map = save_live_registers(sasm, 0);
		int call_offset = __ call_RT(rax, noreg, (address)graal_create_out_of_bounds_exception, j_rarg0);
		oop_maps->add_gc_map(call_offset, oop_map);
		__ leave();
		__ ret(0);
      break;
    }

    case graal_vm_error_id: {
      __ enter();
      oop_maps = new OopMapSet();
      OopMap* oop_map = save_live_registers(sasm, 0);
      int call_offset = __ call_RT(noreg, noreg, (address)graal_vm_error, j_rarg0, j_rarg1, j_rarg2);
      oop_maps->add_gc_map(call_offset, oop_map);
      restore_live_registers(sasm);
      __ leave();
      __ ret(0);
      break;
    }

    case graal_log_printf_id: {
      __ enter();
      oop_maps = new OopMapSet();
      OopMap* oop_map = save_live_registers(sasm, 0);
      int call_offset = __ call_RT(noreg, noreg, (address)graal_log_printf, j_rarg0, j_rarg1, j_rarg2, j_rarg3);
      oop_maps->add_gc_map(call_offset, oop_map);
      restore_live_registers(sasm);
      __ leave();
      __ ret(0);
      break;
    }

    case graal_log_primitive_id: {
      __ enter();
      oop_maps = new OopMapSet();
      OopMap* oop_map = save_live_registers(sasm, 0);
      int call_offset = __ call_RT(noreg, noreg, (address)graal_log_primitive, j_rarg0, j_rarg1, j_rarg2);
      oop_maps->add_gc_map(call_offset, oop_map);
      restore_live_registers(sasm);
      __ leave();
      __ ret(0);
      break;
    }

    case graal_log_object_id: {
      __ enter();
      oop_maps = new OopMapSet();
      OopMap* oop_map = save_live_registers(sasm, 0);
      int call_offset = __ call_RT(noreg, noreg, (address)graal_log_object, j_rarg0, j_rarg1);
      oop_maps->add_gc_map(call_offset, oop_map);
      restore_live_registers(sasm);
      __ leave();
      __ ret(0);
      break;
    }

    case graal_verify_oop_id: {
      // We use enter & leave so that a better stack trace is produced in the hs_err file
      __ enter();
      __ verify_oop(r13, "Graal verify oop");
      __ leave();
      __ ret(0);
      break;
    }

    case graal_arithmetic_frem_id: {
      __ subptr(rsp, 8);
      __ movflt(Address(rsp, 0), xmm1);
      __ fld_s(Address(rsp, 0));
      __ movflt(Address(rsp, 0), xmm0);
      __ fld_s(Address(rsp, 0));
      Label L;
      __ bind(L);
      __ fprem();
      __ fwait();
      __ fnstsw_ax();
      __ testl(rax, 0x400);
      __ jcc(Assembler::notZero, L);
      __ fxch(1);
      __ fpop();
      __ fstp_s(Address(rsp, 0));
      __ movflt(xmm0, Address(rsp, 0));
      __ addptr(rsp, 8);
      __ ret(0);
      break;
    }
    case graal_arithmetic_drem_id: {
      __ subptr(rsp, 8);
      __ movdbl(Address(rsp, 0), xmm1);
      __ fld_d(Address(rsp, 0));
      __ movdbl(Address(rsp, 0), xmm0);
      __ fld_d(Address(rsp, 0));
      Label L;
      __ bind(L);
      __ fprem();
      __ fwait();
      __ fnstsw_ax();
      __ testl(rax, 0x400);
      __ jcc(Assembler::notZero, L);
      __ fxch(1);
      __ fpop();
      __ fstp_d(Address(rsp, 0));
      __ movdbl(xmm0, Address(rsp, 0));
      __ addptr(rsp, 8);
      __ ret(0);
      break;
    }
    case graal_monitorenter_id: {
      Register obj = j_rarg0;
      Register lock = j_rarg1;
      {
        GraalStubFrame f(sasm, "graal_monitorenter", dont_gc_arguments);
        OopMap* map = save_live_registers(sasm, 2, save_fpu_registers);

        // Called with store_parameter and not C abi
        int call_offset = __ call_RT(noreg, noreg, CAST_FROM_FN_PTR(address, graal_monitorenter), obj, lock);

        oop_maps = new OopMapSet();
        oop_maps->add_gc_map(call_offset, map);
        restore_live_registers(sasm, save_fpu_registers);
      }
      __ ret(0);
      break;
    }
    case graal_monitorexit_id: {
      Register obj = j_rarg0;
      Register lock = j_rarg1;
      {
        GraalStubFrame f(sasm, "graal_monitorexit", dont_gc_arguments);
        OopMap* map = save_live_registers(sasm, 2, save_fpu_registers);

        // note: really a leaf routine but must setup last java sp
        //       => use call_RT for now (speed can be improved by
        //       doing last java sp setup manually)
        int call_offset = __ call_RT(noreg, noreg, CAST_FROM_FN_PTR(address, graal_monitorexit), obj, lock);

        oop_maps = new OopMapSet();
        oop_maps->add_gc_map(call_offset, map);
        restore_live_registers(sasm, save_fpu_registers);
      }
      __ ret(0);
      break;
   }
   case graal_wb_pre_call_id: {
      Register obj = j_rarg0;
      {
        GraalStubFrame f(sasm, "graal_wb_pre_call", dont_gc_arguments);
        OopMap* map = save_live_registers(sasm, 2, save_fpu_registers);

        // note: really a leaf routine but must setup last java sp
        //       => use call_RT for now (speed can be improved by
        //       doing last java sp setup manually)
        int call_offset = __ call_RT(noreg, noreg, CAST_FROM_FN_PTR(address, graal_wb_pre_call), obj);

        oop_maps = new OopMapSet();
        oop_maps->add_gc_map(call_offset, map);
        restore_live_registers(sasm);
      }
      __ ret(0);
      break;
   }
   case graal_wb_post_call_id: {
      Register obj = j_rarg0;
      Register caddr = j_rarg1;
      {
        GraalStubFrame f(sasm, "graal_wb_post_call", dont_gc_arguments);
        OopMap* map = save_live_registers(sasm, 2, save_fpu_registers);

        // note: really a leaf routine but must setup last java sp
        //       => use call_RT for now (speed can be improved by
        //       doing last java sp setup manually)
        int call_offset = __ call_RT(noreg, noreg, CAST_FROM_FN_PTR(address, graal_wb_post_call), obj, caddr);

        oop_maps = new OopMapSet();
        oop_maps->add_gc_map(call_offset, map);
        restore_live_registers(sasm);
      }
      __ ret(0);
      break;
   }

   case graal_identity_hash_code_id: {
      Register obj = j_rarg0; // Incoming
      __ set_info("identity_hash_code", dont_gc_arguments);
      __ enter();
      OopMap* map = save_live_registers(sasm, 1);
      int call_offset = __ call_RT(noreg, noreg, CAST_FROM_FN_PTR(address, graal_identity_hash_code), obj);
      oop_maps = new OopMapSet();
      oop_maps->add_gc_map(call_offset, map);
      restore_live_registers_except_rax(sasm);
      __ leave();
      __ ret(0);
      break;
    }
    case graal_thread_is_interrupted_id: {
      Register thread = j_rarg0;
      Register clear_interrupted = j_rarg1;

      __ set_info("identity_hash_code", dont_gc_arguments);
      __ enter();
      OopMap* map = save_live_registers(sasm, 1);
      int call_offset = __ call_RT(noreg, noreg, CAST_FROM_FN_PTR(address, graal_thread_is_interrupted), thread, clear_interrupted);
      oop_maps = new OopMapSet();
      oop_maps->add_gc_map(call_offset, map);
      restore_live_registers_except_rax(sasm);
      __ leave();
      __ ret(0);
      break;
    }

    default:
      { GraalStubFrame f(sasm, "unimplemented entry", dont_gc_arguments);
        __ movptr(rax, (int)id);
        __ call_RT(noreg, noreg, CAST_FROM_FN_PTR(address, unimplemented_entry), rax);
        __ should_not_reach_here();
      }
      break;
  }
  return oop_maps;
}

#undef __
