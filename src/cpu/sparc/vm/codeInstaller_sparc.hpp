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

inline jint CodeInstaller::pd_next_offset(NativeInstruction* inst, jint pc_offset, oop method) {
  fatal("CodeInstaller::pd_next_offset - sparc unimp");
  return 0;
}

inline void CodeInstaller::pd_site_DataPatch(oop constant, oop kind, bool inlined,
                                             address instruction, int alignment, char typeChar) {
  fatal("CodeInstaller::pd_site_DataPatch - sparc unimp");
}

inline void CodeInstaller::pd_relocate_CodeBlob(CodeBlob* cb, NativeInstruction* inst) {
  fatal("CodeInstaller::pd_relocate_CodeBlob - sparc unimp");
}

inline void CodeInstaller::pd_relocate_ForeignCall(NativeInstruction* inst, jlong foreign_call_destination) {
  fatal("CodeInstaller::pd_relocate_ForeignCall - sparc unimp");
}

inline void CodeInstaller::pd_relocate_JavaMethod(oop method, jint pc_offset) {
  fatal("CodeInstaller::pd_relocate_JavaMethod - sparc unimp");
}

inline int32_t* CodeInstaller::pd_locate_operand(address instruction) {
  fatal("CodeInstaller::pd_locate_operand - sparc unimp");
  return (int32_t*)0;
}

#endif // CPU_SPARC_VM_CODEINSTALLER_SPARC_HPP
