/*
 * Copyright (c) 2011, 2014, Oracle and/or its affiliates. All rights reserved.
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

#ifndef SHARE_VM_GRAAL_GRAAL_CODE_INSTALLER_HPP
#define SHARE_VM_GRAAL_GRAAL_CODE_INSTALLER_HPP

#include "graal/graalEnv.hpp"

/*
 * This class handles the conversion from a InstalledCode to a CodeBlob or an nmethod.
 */
class CodeInstaller {
  friend class VMStructs;
private:
  enum MarkId {
    VERIFIED_ENTRY             = 1,
    UNVERIFIED_ENTRY           = 2,
    OSR_ENTRY                  = 3,
    EXCEPTION_HANDLER_ENTRY    = 4,
    DEOPT_HANDLER_ENTRY        = 5,
    INVOKEINTERFACE            = 6,
    INVOKEVIRTUAL              = 7,
    INVOKESTATIC               = 8,
    INVOKESPECIAL              = 9,
    INLINE_INVOKE              = 10,
    POLL_NEAR                  = 11,
    POLL_RETURN_NEAR           = 12,
    POLL_FAR                   = 13,
    POLL_RETURN_FAR            = 14,
    INVOKE_INVALID             = -1
  };

  Arena         _arena;

  oop           _dataSection;
  arrayOop      _sites;
  arrayOop      _exception_handlers;
  CodeOffsets   _offsets;

  arrayOop      _code;
  jint          _code_size;
  jint          _total_frame_size;
  jint          _custom_stack_area_offset;
  jint          _parameter_count;
  jint          _constants_size;
#ifndef PRODUCT
  arrayOop      _comments;
#endif

  MarkId        _next_call_type;
  address       _invoke_mark_pc;

  CodeSection*  _instructions;
  CodeSection*  _constants;

  OopRecorder*              _oop_recorder;
  DebugInformationRecorder* _debug_recorder;
  Dependencies*             _dependencies;
  ExceptionHandlerTable     _exception_handler_table;

  jint pd_next_offset(NativeInstruction* inst, jint pc_offset, oop method);
  void pd_patch_OopData(int pc_offset, oop data);
  void pd_patch_DataSectionReference(int pc_offset, oop data);
  void pd_relocate_CodeBlob(CodeBlob* cb, NativeInstruction* inst);
  void pd_relocate_ForeignCall(NativeInstruction* inst, jlong foreign_call_destination);
  void pd_relocate_JavaMethod(oop method, jint pc_offset);
  void pd_relocate_poll(address pc, jint mark);


public:

  CodeInstaller() {};
  GraalEnv::CodeInstallResult install(Handle& compiled_code, CodeBlob*& cb, Handle installed_code, Handle speculation_log);

  static address runtime_call_target_address(oop runtime_call);
  static VMReg get_hotspot_reg(jint graalRegisterNumber);
  static bool is_general_purpose_reg(VMReg hotspotRegister);

protected:

  virtual ScopeValue* get_scope_value(oop value, int total_frame_size, GrowableArray<ScopeValue*>* objects, ScopeValue* &second, OopRecorder* oop_recorder);
  virtual MonitorValue* get_monitor_value(oop value, int total_frame_size, GrowableArray<ScopeValue*>* objects, OopRecorder* oop_recorder);

private:
  // extract the fields of the CompilationResult
  void initialize_fields(oop target_method);
  void initialize_assumptions(oop target_method);
  
  int estimate_stub_entries();
  
  // perform data and call relocation on the CodeBuffer
  bool initialize_buffer(CodeBuffer& buffer);

  void assumption_MethodContents(Handle assumption);
  void assumption_NoFinalizableSubclass(Handle assumption);
  void assumption_ConcreteSubtype(Handle assumption);
  void assumption_ConcreteMethod(Handle assumption);
  void assumption_CallSiteTargetValue(Handle assumption);

  void site_Safepoint(CodeBuffer& buffer, jint pc_offset, oop site);
  void site_Infopoint(CodeBuffer& buffer, jint pc_offset, oop site);
  void site_Call(CodeBuffer& buffer, jint pc_offset, oop site);
  void site_DataPatch(CodeBuffer& buffer, jint pc_offset, oop site);
  void site_Mark(CodeBuffer& buffer, jint pc_offset, oop site);

  void record_scope(jint pc_offset, oop code_pos, GrowableArray<ScopeValue*>* objects);

  void process_exception_handlers();
  int estimateStubSpace(int static_call_stubs);
};

/**
 * Gets the Method metaspace object from a HotSpotResolvedJavaMethod Java object.
 */
Method* getMethodFromHotSpotMethod(oop hotspot_method);



#endif // SHARE_VM_GRAAL_GRAAL_CODE_INSTALLER_HPP
