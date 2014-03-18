/*
 * Copyright (c) 2013, 2014, Oracle and/or its affiliates. All rights reserved.
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

#ifndef SHARE_VM_GRAAL_VMSTRUCTS_GRAAL_HPP
#define SHARE_VM_GRAAL_VMSTRUCTS_GRAAL_HPP

#include "compiler/abstractCompiler.hpp"
#include "graal/graalCodeInstaller.hpp"
#include "graal/graalCompilerToVM.hpp"
#include "graal/graalEnv.hpp"

#define VM_STRUCTS_GRAAL(nonstatic_field, static_field)                       \
  nonstatic_field(ThreadShadow, _pending_deoptimization, int)                 \
  nonstatic_field(ThreadShadow, _pending_failed_speculation, oop)             \
  nonstatic_field(MethodData,   _graal_node_count, int)                       \

#define VM_TYPES_GRAAL(declare_type, declare_toplevel_type)                   \

#define VM_INT_CONSTANTS_GRAAL(declare_constant, declare_preprocessor_constant)                   \
  declare_constant(Deoptimization::Reason_aliasing)                                               \
  declare_constant(GraalEnv::ok)                                                                  \
  declare_constant(GraalEnv::dependencies_failed)                                                 \
  declare_constant(GraalEnv::cache_full)                                                          \
  declare_constant(GraalEnv::code_too_large)                                                      \
                                                                                                  \
  declare_preprocessor_constant("JVM_ACC_SYNTHETIC", JVM_ACC_SYNTHETIC)                           \
  declare_preprocessor_constant("JVM_RECOGNIZED_FIELD_MODIFIERS", JVM_RECOGNIZED_FIELD_MODIFIERS) \
                                                                                                  \
  declare_constant(CompilerToVM::KLASS_TAG)                                                       \
  declare_constant(CompilerToVM::SYMBOL_TAG)                                                      \
                                                                                                  \
  declare_constant(CodeInstaller::VERIFIED_ENTRY)                                                 \
  declare_constant(CodeInstaller::UNVERIFIED_ENTRY)                                               \
  declare_constant(CodeInstaller::OSR_ENTRY)                                                      \
  declare_constant(CodeInstaller::EXCEPTION_HANDLER_ENTRY)                                        \
  declare_constant(CodeInstaller::DEOPT_HANDLER_ENTRY)                                            \
  declare_constant(CodeInstaller::INVOKEINTERFACE)                                                \
  declare_constant(CodeInstaller::INVOKEVIRTUAL)                                                  \
  declare_constant(CodeInstaller::INVOKESTATIC)                                                   \
  declare_constant(CodeInstaller::INVOKESPECIAL)                                                  \
  declare_constant(CodeInstaller::INLINE_INVOKE)                                                  \
  declare_constant(CodeInstaller::POLL_NEAR)                                                      \
  declare_constant(CodeInstaller::POLL_RETURN_NEAR)                                               \
  declare_constant(CodeInstaller::POLL_FAR)                                                       \
  declare_constant(CodeInstaller::POLL_RETURN_FAR)                                                \
  declare_constant(CodeInstaller::INVOKE_INVALID)                                                 \

#endif // SHARE_VM_GRAAL_VMSTRUCTS_GRAAL_HPP
