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
  set_last_Java_frame(thread, rsp, noreg, NULL);

  // do the call
  call(RuntimeAddress(entry));
  call_offset = offset();
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
  reset_last_Java_frame(thread, true, false);

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
    movl(Address(thread, ThreadShadow::pending_deoptimization_offset()), Deoptimization::make_trap_request(Deoptimization::Reason_constraint, Deoptimization::Action_reinterpret));
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
  map->set_callee_saved(VMRegImpl::stack2reg(rbp_off + num_rt_args), rbp->as_VMReg());
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
  map->set_callee_saved(VMRegImpl::stack2reg(rbpH_off + num_rt_args), rbp->as_VMReg()->next());
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

OopMap* save_live_registers(GraalStubAssembler* sasm, int num_rt_args,
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

OopMapSet* GraalRuntime::generate_code_for(StubID id, GraalStubAssembler* sasm) {

  // for better readability
  const bool must_gc_arguments = true;
  const bool dont_gc_arguments = false;

  // default value; overwritten for some optimized stubs that are called from methods that do not use the fpu
  bool save_fpu_registers = true;

  // stub code & info for the different stubs
  OopMapSet* oop_maps = NULL;
  switch (id) {

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
