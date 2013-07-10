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

#ifndef CPU_SPARC_VM_CODEINSTALLER_SPARC_HPP
#define CPU_SPARC_VM_CODEINSTALLER_SPARC_HPP

#include "graal/graalCompiler.hpp"
#include "graal/graalCompilerToVM.hpp"
#include "graal/graalJavaAccess.hpp"

inline jint CodeInstaller::pd_next_offset(NativeInstruction* inst, jint pc_offset, oop method) {
  assert(inst->is_call() || inst->is_jump(), "sanity");
  return pc_offset + NativeCall::instruction_size;
}

inline void CodeInstaller::pd_site_DataPatch(int pc_offset, oop site) {
  oop constant = CompilationResult_DataPatch::constant(site);
  int alignment = CompilationResult_DataPatch::alignment(site);
  bool inlined = CompilationResult_DataPatch::inlined(site) == JNI_TRUE;

  oop kind = Constant::kind(constant);
  char typeChar = Kind::typeChar(kind);

  address pc = _instructions->start() + pc_offset;

  tty->print_cr("CodeInstaller::pd_site_DataPatch: typeChar=%c, inlined=%d", typeChar, inlined);

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
    case 'd': {
      if (inlined) {
        fatal(err_msg("inlined: type=%c, constant=%lx", inlined, Constant::primitive(constant)));
      } else {
        int size = _constants->size();
        if (alignment > 0) {
          guarantee(alignment <= _constants->alignment(), "Alignment inside constants section is restricted by alignment of section begin");
          size = align_size_up(size, alignment);
        }
        // we don't care if this is a long/double/etc., the primitive field contains the right bits
        address dest = _constants->start() + size;
        _constants->set_end(dest);
        uint64_t value = Constant::primitive(constant);
        _constants->emit_int64(value);

        NativeMovRegMem* load = nativeMovRegMem_at(pc);
        int disp = _constants_size + pc_offset - size - BytesPerInstWord;
        load->set_offset(-disp);

//        assert(disp == (jint) disp, "disp doesn't fit in 32 bits");
//        *((jint*) operand) = (jint) disp;

//        _instructions->relocate(instruction, section_word_Relocation::spec((address) dest, CodeBuffer::SECT_CONSTS) /*, Assembler::disp32_operand*/);
//        TRACE_graal_3("relocating (%c) at %p/%p with destination at %p (%d)", typeChar, instruction, operand, dest, size);
      }
      break;
    }
    case 'a': {
//      address operand = Assembler::locate_operand(instruction, Assembler::imm_operand);
//      Handle obj = Constant::object(constant);
//
//      jobject value = JNIHandles::make_local(obj());
//      *((jobject*) operand) = value;
//      _instructions->relocate(instruction, oop_Relocation::spec_for_immediate(), Assembler::imm_operand);
//      TRACE_graal_3("relocating (oop constant) at %p/%p", instruction, operand);
      break;
    }
    default:
      fatal(err_msg("unexpected Kind (%d) in DataPatch", typeChar));
      break;
  }
}

inline void CodeInstaller::pd_relocate_CodeBlob(CodeBlob* cb, NativeInstruction* inst) {
  fatal("CodeInstaller::pd_relocate_CodeBlob - sparc unimp");
}

inline void CodeInstaller::pd_relocate_ForeignCall(NativeInstruction* inst, jlong foreign_call_destination) {
  fatal("CodeInstaller::pd_relocate_ForeignCall - sparc unimp");
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
//      _instructions->relocate(call->instruction_address(),
//                                             virtual_call_Relocation::spec(_invoke_mark_pc),
//                                             Assembler::call32_operand);
      fatal("NYI");
      break;
    }
    case MARK_INVOKESTATIC: {
      assert(method == NULL || method->is_static(), "cannot call non-static method with invokestatic");
      NativeCall* call = nativeCall_at(_instructions->start() + pc_offset);
      call->set_destination(SharedRuntime::get_resolve_static_call_stub());
      _instructions->relocate(call->instruction_address(),
                                             relocInfo::static_call_type /*, Assembler::call32_operand*/);
      break;
    }
    case MARK_INVOKESPECIAL: {
      assert(method == NULL || !method->is_static(), "cannot call static method with invokespecial");
      NativeCall* call = nativeCall_at(_instructions->start() + pc_offset);
      call->set_destination(SharedRuntime::get_resolve_opt_virtual_call_stub());
//      _instructions->relocate(call->instruction_address(),
//                              relocInfo::opt_virtual_call_type, Assembler::call32_operand);
      fatal("NYI");
      break;
    }
    default:
      fatal("invalid _next_call_type value");
      break;
  }
}

inline int32_t* CodeInstaller::pd_locate_operand(address instruction) {
  fatal("CodeInstaller::pd_locate_operand - sparc unimp");
  return (int32_t*)0;
}

#endif // CPU_SPARC_VM_CODEINSTALLER_SPARC_HPP
