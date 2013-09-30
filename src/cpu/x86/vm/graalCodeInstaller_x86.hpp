/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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
 */
#ifndef CPU_X86_VM_CODEINSTALLER_X86_HPP
#define CPU_X86_VM_CODEINSTALLER_X86_HPP

#include "compiler/disassembler.hpp"
#include "runtime/javaCalls.hpp"
#include "graal/graalEnv.hpp"
#include "graal/graalCompiler.hpp"
#include "graal/graalCodeInstaller.hpp"
#include "graal/graalJavaAccess.hpp"
#include "graal/graalCompilerToVM.hpp"
#include "graal/graalRuntime.hpp"
#include "asm/register.hpp"
#include "classfile/vmSymbols.hpp"
#include "code/vmreg.hpp"

inline jint CodeInstaller::pd_next_offset(NativeInstruction* inst, jint pc_offset, oop method) {
  if (inst->is_call() || inst->is_jump()) {
    assert(NativeCall::instruction_size == (int)NativeJump::instruction_size, "unexpected size");
    return (pc_offset + NativeCall::instruction_size);
  } else if (inst->is_mov_literal64()) {
    // mov+call instruction pair
    jint offset = pc_offset + NativeMovConstReg::instruction_size;
    u_char* call = (u_char*) (_instructions->start() + offset);
    assert((call[0] == 0x40 || call[0] == 0x41) && call[1] == 0xFF, "expected call with rex/rexb prefix byte");
    offset += 3; /* prefix byte + opcode byte + modrm byte */
    return (offset);
  } else if (inst->is_call_reg()) {
    // the inlined vtable stub contains a "call register" instruction
    assert(method != NULL, "only valid for virtual calls");
    return (pc_offset + ((NativeCallReg *) inst)->next_instruction_offset());
  } else if (inst->is_cond_jump()) {
    address pc = (address) (inst);
    return pc_offset + (jint) (Assembler::locate_next_instruction(pc) - pc);
  } else {
    fatal("unsupported type of instruction for call site");
    return 0;
  }
}

inline void CodeInstaller::pd_site_DataPatch(int pc_offset, oop site) {
  int alignment = CompilationResult_DataPatch::alignment(site);
  bool inlined = CompilationResult_DataPatch::inlined(site) == JNI_TRUE;

  address pc = _instructions->start() + pc_offset;

  oop constant = CompilationResult_DataPatch::constant(site);
  char typeChar;
  if (constant != NULL) {
    oop kind = Constant::kind(constant);
    typeChar = Kind::typeChar(kind);
  } else {
    assert(!inlined, "cannot inline raw constants");
    typeChar = '*';
  }

  switch (typeChar) {
    case 'z':
    case 'b':
    case 's':
    case 'c':
    case 'i':
      fatal("int-sized values not expected in DataPatch");
      break;
    case 'f':
    case 'j':
    case 'd':
    case '*': {
      if (inlined) {
        address operand = Assembler::locate_operand(pc, Assembler::imm_operand);
        *((jlong*) operand) = Constant::primitive(constant);
      } else {
        address operand = Assembler::locate_operand(pc, Assembler::disp32_operand);
        address next_instruction = Assembler::locate_next_instruction(pc);
        int size = _constants->size();
        if (alignment > 0) {
          guarantee(alignment <= _constants->alignment(), "Alignment inside constants section is restricted by alignment of section begin");
          size = align_size_up(size, alignment);
        }
        // we don't care if this is a long/double/etc., the primitive field contains the right bits
        address dest = _constants->start() + size;
        _constants->set_end(dest);
        if (constant != NULL) {
          uint64_t value = Constant::primitive(constant);
          _constants->emit_int64(value);
        } else {
          arrayOop rawConstant = arrayOop(CompilationResult_DataPatch::rawConstant(site));
          int8_t *ptr = (int8_t*) rawConstant->base(T_BYTE);
          for (int i = rawConstant->length(); i > 0; i--, ptr++) {
            _constants->emit_int8(*ptr);
          }
        }

        long disp = dest - next_instruction;
        assert(disp == (jint) disp, "disp doesn't fit in 32 bits");
        *((jint*) operand) = (jint) disp;

        _instructions->relocate(pc, section_word_Relocation::spec((address) dest, CodeBuffer::SECT_CONSTS), Assembler::disp32_operand);
        TRACE_graal_3("relocating (%c) at %p/%p with destination at %p (%d)", typeChar, pc, operand, dest, size);
      }
      break;
    }
    case 'a': {
      address operand = Assembler::locate_operand(pc, Assembler::imm_operand);
      Handle obj = Constant::object(constant);

      jobject value = JNIHandles::make_local(obj());
      *((jobject*) operand) = value;
      _instructions->relocate(pc, oop_Relocation::spec_for_immediate(), Assembler::imm_operand);
      TRACE_graal_3("relocating (oop constant) at %p/%p", pc, operand);
      break;
    }
    default:
      fatal(err_msg("unexpected Kind (%d) in DataPatch", typeChar));
      break;
  }
}

inline void CodeInstaller::pd_relocate_CodeBlob(CodeBlob* cb, NativeInstruction* inst) {
  if (cb->is_nmethod()) {
    nmethod* nm = (nmethod*) cb;
    nativeJump_at((address)inst)->set_jump_destination(nm->verified_entry_point());
  } else {
    nativeJump_at((address)inst)->set_jump_destination(cb->code_begin());
  }
  _instructions->relocate((address)inst, runtime_call_Relocation::spec(), Assembler::call32_operand);
}

inline void CodeInstaller::pd_relocate_ForeignCall(NativeInstruction* inst, jlong foreign_call_destination) {
  address pc = (address) inst;
  if (inst->is_call()) {
    // NOTE: for call without a mov, the offset must fit a 32-bit immediate
    //       see also CompilerToVM.getMaxCallTargetOffset()
    NativeCall* call = nativeCall_at(pc);
    call->set_destination((address) foreign_call_destination);
    _instructions->relocate(call->instruction_address(), runtime_call_Relocation::spec(), Assembler::call32_operand);
  } else if (inst->is_mov_literal64()) {
    NativeMovConstReg* mov = nativeMovConstReg_at(pc);
    mov->set_data((intptr_t) foreign_call_destination);
    _instructions->relocate(mov->instruction_address(), runtime_call_Relocation::spec(), Assembler::imm_operand);
  } else if (inst->is_jump()) {
    NativeJump* jump = nativeJump_at(pc);
    jump->set_jump_destination((address) foreign_call_destination);
    _instructions->relocate(jump->instruction_address(), runtime_call_Relocation::spec(), Assembler::call32_operand);
  } else if (inst->is_cond_jump()) {
    address old_dest = nativeGeneralJump_at(pc)->jump_destination();
    address disp = Assembler::locate_operand(pc, Assembler::call32_operand);
    *(jint*) disp += ((address) foreign_call_destination) - old_dest;
    _instructions->relocate(pc, runtime_call_Relocation::spec(), Assembler::call32_operand);
  } else {
    fatal("unsupported relocation for foreign call");
  }

  TRACE_graal_3("relocating (foreign call)  at %p", inst);
}

inline void CodeInstaller::pd_relocate_JavaMethod(oop hotspot_method, jint pc_offset) {
#ifdef ASSERT
  Method* method = NULL;
  // we need to check, this might also be an unresolved method
  if (hotspot_method->is_a(HotSpotResolvedJavaMethod::klass())) {
    method = getMethodFromHotSpotMethod(hotspot_method);
  }
#endif
  switch (_next_call_type) {
    case MARK_INLINE_INVOKE:
      break;
    case MARK_INVOKEVIRTUAL:
    case MARK_INVOKEINTERFACE: {
      assert(method == NULL || !method->is_static(), "cannot call static method with invokeinterface");

      NativeCall* call = nativeCall_at(_instructions->start() + pc_offset);
      call->set_destination(SharedRuntime::get_resolve_virtual_call_stub());
      _instructions->relocate(call->instruction_address(),
                                             virtual_call_Relocation::spec(_invoke_mark_pc),
                                             Assembler::call32_operand);
      break;
    }
    case MARK_INVOKESTATIC: {
      assert(method == NULL || method->is_static(), "cannot call non-static method with invokestatic");

      NativeCall* call = nativeCall_at(_instructions->start() + pc_offset);
      call->set_destination(SharedRuntime::get_resolve_static_call_stub());
      _instructions->relocate(call->instruction_address(),
                                             relocInfo::static_call_type, Assembler::call32_operand);
      break;
    }
    case MARK_INVOKESPECIAL: {
      assert(method == NULL || !method->is_static(), "cannot call static method with invokespecial");
      NativeCall* call = nativeCall_at(_instructions->start() + pc_offset);
      call->set_destination(SharedRuntime::get_resolve_opt_virtual_call_stub());
      _instructions->relocate(call->instruction_address(),
                              relocInfo::opt_virtual_call_type, Assembler::call32_operand);
      break;
    }
    default:
      break;
  }
}

inline void CodeInstaller::pd_relocate_poll(address pc, jint mark) {
  switch (mark) {
    case MARK_POLL_NEAR: {
      NativeInstruction* ni = nativeInstruction_at(pc);
      int32_t* disp = (int32_t*) Assembler::locate_operand(pc, Assembler::disp32_operand);
      // int32_t* disp = (int32_t*) Assembler::locate_operand(instruction, Assembler::disp32_operand);
      int32_t offset = *disp; // The Java code installed the polling page offset into the disp32 operand
      intptr_t new_disp = (intptr_t) (os::get_polling_page() + offset) - (intptr_t) ni;
      *disp = (int32_t)new_disp;
    }
    case MARK_POLL_FAR:
      _instructions->relocate(pc, relocInfo::poll_type);
      break;
    case MARK_POLL_RETURN_NEAR: {
      NativeInstruction* ni = nativeInstruction_at(pc);
      int32_t* disp = (int32_t*) Assembler::locate_operand(pc, Assembler::disp32_operand);
      // int32_t* disp = (int32_t*) Assembler::locate_operand(instruction, Assembler::disp32_operand);
      int32_t offset = *disp; // The Java code installed the polling page offset into the disp32 operand
      intptr_t new_disp = (intptr_t) (os::get_polling_page() + offset) - (intptr_t) ni;
      *disp = (int32_t)new_disp;
    }
    case MARK_POLL_RETURN_FAR:
      _instructions->relocate(pc, relocInfo::poll_return_type);
      break;
    default:
      fatal("invalid mark value");
      break;
  }
}

#endif // CPU_X86_VM_CODEINSTALLER_X86_HPP

