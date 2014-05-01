/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
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

#ifndef SHARE_VM_GRAAL_GRAAL_JAVA_ACCESS_HPP
#define SHARE_VM_GRAAL_GRAAL_JAVA_ACCESS_HPP

void graal_compute_offsets();

#include "classfile/systemDictionary.hpp"
#include "oops/instanceMirrorKlass.hpp"

/* This macro defines the structure of the CompilationResult - classes.
 * It will generate classes with accessors similar to javaClasses.hpp, but with specializations for oops, Handles and jni handles.
 *
 * The public interface of these classes will look like this:

 * class StackSlot : AllStatic {
 * public:
 *   static Klass* klass();
 *   static jint  index(oop obj);
 *   static jint  index(Handle obj);
 *   static jint  index(jobject obj);
 *   static void set_index(oop obj, jint x);
 *   static void set_index(Handle obj, jint x);
 *   static void set_index(jobject obj, jint x);
 * };
 *
 */

#define COMPILER_CLASSES_DO(start_class, end_class, char_field, int_field, boolean_field, long_field, float_field, oop_field, static_oop_field, static_int_field) \
  start_class(HotSpotResolvedObjectType)                                                                                                                       \
    oop_field(HotSpotResolvedObjectType, javaClass, "Ljava/lang/Class;")                                                                                       \
  end_class                                                                                                                                                    \
  start_class(HotSpotResolvedJavaMethod)                                                                                                                       \
    oop_field(HotSpotResolvedJavaMethod, name, "Ljava/lang/String;")                                                                                           \
    oop_field(HotSpotResolvedJavaMethod, holder, "Lcom/oracle/graal/hotspot/meta/HotSpotResolvedObjectType;")                                                  \
    long_field(HotSpotResolvedJavaMethod, metaspaceMethod)                                                                                                     \
  end_class                                                                                                                                                    \
  start_class(HotSpotJavaType)                                                                                                                                 \
    oop_field(HotSpotJavaType, name, "Ljava/lang/String;")                                                                                                     \
  end_class                                                                                                                                                    \
  start_class(InstalledCode)                                                                                                                                   \
    long_field(InstalledCode, address)                                                                                                                         \
    long_field(InstalledCode, version)                                                                                                                         \
  end_class                                                                                                                                                    \
  start_class(HotSpotInstalledCode)                                                                                                                            \
    int_field(HotSpotInstalledCode, size)                                                                                                                      \
    long_field(HotSpotInstalledCode, codeStart)                                                                                                                \
    int_field(HotSpotInstalledCode, codeSize)                                                                                                                  \
  end_class                                                                                                                                                    \
  start_class(HotSpotNmethod)                                                                                                                                  \
    boolean_field(HotSpotNmethod, isDefault)                                                                                                                   \
    boolean_field(HotSpotNmethod, isExternal)                                                                                                                  \
    oop_field(HotSpotNmethod, name, "Ljava/lang/String;")                                                                                                      \
  end_class                                                                                                                                                    \
  start_class(HotSpotCompiledCode)                                                                                                                             \
    oop_field(HotSpotCompiledCode, comp, "Lcom/oracle/graal/api/code/CompilationResult;")                                                                      \
    oop_field(HotSpotCompiledCode, sites, "[Lcom/oracle/graal/api/code/CompilationResult$Site;")                                                               \
    oop_field(HotSpotCompiledCode, exceptionHandlers, "[Lcom/oracle/graal/api/code/CompilationResult$ExceptionHandler;")                                       \
    oop_field(HotSpotCompiledCode, comments, "[Lcom/oracle/graal/hotspot/HotSpotCompiledCode$Comment;")                                                        \
    oop_field(HotSpotCompiledCode, dataSection, "Lcom/oracle/graal/hotspot/data/DataSection;")                                                                 \
  end_class                                                                                                                                                    \
  start_class(HotSpotCompiledCode_Comment)                                                                                                                     \
    oop_field(HotSpotCompiledCode_Comment, text, "Ljava/lang/String;")                                                                                         \
    int_field(HotSpotCompiledCode_Comment, pcOffset)                                                                                                           \
  end_class                                                                                                                                                    \
  start_class(HotSpotCompiledNmethod)                                                                                                                          \
    oop_field(HotSpotCompiledNmethod, method, "Lcom/oracle/graal/hotspot/meta/HotSpotResolvedJavaMethod;")                                                     \
    int_field(HotSpotCompiledNmethod, entryBCI)                                                                                                                \
    int_field(HotSpotCompiledNmethod, id)                                                                                                                      \
    long_field(HotSpotCompiledNmethod, ctask)                                                                                                                  \
  end_class                                                                                                                                                    \
  start_class(HotSpotCompiledRuntimeStub)                                                                                                                      \
    oop_field(HotSpotCompiledRuntimeStub, stubName, "Ljava/lang/String;")                                                                                      \
  end_class                                                                                                                                                    \
  start_class(HotSpotForeignCallLinkage)                                                                                                                       \
    long_field(HotSpotForeignCallLinkage, address)                                                                                                             \
  end_class                                                                                                                                                    \
  start_class(DataSection)                                                                                                                                     \
    int_field(DataSection, sectionAlignment)                                                                                                                   \
    oop_field(DataSection, data, "[B")                                                                                                                         \
    oop_field(DataSection, patches, "[Lcom/oracle/graal/api/code/CompilationResult$DataPatch;")                                                                \
  end_class                                                                                                                                                    \
  start_class(DataSectionReference)                                                                                                                            \
    int_field(DataSectionReference, offset)                                                                                                                    \
  end_class                                                                                                                                                    \
  start_class(MetaspaceData)                                                                                                                                   \
    long_field(MetaspaceData, value)                                                                                                                           \
    oop_field(MetaspaceData, annotation, "Ljava/lang/Object;")                                                                                                 \
    boolean_field(MetaspaceData, compressed)                                                                                                                   \
  end_class                                                                                                                                                    \
  start_class(OopData)                                                                                                                                         \
    oop_field(OopData, object, "Ljava/lang/Object;")                                                                                                           \
    boolean_field(OopData, compressed)                                                                                                                         \
  end_class                                                                                                                                                    \
  start_class(ExternalCompilationResult)                                                                                                                       \
    long_field(ExternalCompilationResult, entryPoint)                                                                                                          \
  end_class                                                                                                                                                    \
  start_class(CompilationResult)                                                                                                                               \
    int_field(CompilationResult, totalFrameSize)                                                                                                               \
    int_field(CompilationResult, customStackAreaOffset)                                                                                                        \
    oop_field(CompilationResult, targetCode, "[B")                                                                                                             \
    oop_field(CompilationResult, assumptions, "Lcom/oracle/graal/api/code/Assumptions;")                                                                       \
    int_field(CompilationResult, targetCodeSize)                                                                                                               \
  end_class                                                                                                                                                    \
  start_class(Assumptions)                                                                                                                                     \
    oop_field(Assumptions, list, "[Lcom/oracle/graal/api/code/Assumptions$Assumption;")                                                                        \
  end_class                                                                                                                                                    \
  start_class(Assumptions_MethodContents)                                                                                                                      \
    oop_field(Assumptions_MethodContents, method, "Lcom/oracle/graal/api/meta/ResolvedJavaMethod;")                                                            \
  end_class                                                                                                                                                    \
  start_class(Assumptions_NoFinalizableSubclass)                                                                                                               \
    oop_field(Assumptions_NoFinalizableSubclass, receiverType, "Lcom/oracle/graal/api/meta/ResolvedJavaType;")                                                 \
  end_class                                                                                                                                                    \
  start_class(Assumptions_ConcreteSubtype)                                                                                                                     \
    oop_field(Assumptions_ConcreteSubtype, context, "Lcom/oracle/graal/api/meta/ResolvedJavaType;")                                                            \
    oop_field(Assumptions_ConcreteSubtype, subtype, "Lcom/oracle/graal/api/meta/ResolvedJavaType;")                                                            \
  end_class                                                                                                                                                    \
  start_class(Assumptions_ConcreteMethod)                                                                                                                      \
    oop_field(Assumptions_ConcreteMethod, method, "Lcom/oracle/graal/api/meta/ResolvedJavaMethod;")                                                            \
    oop_field(Assumptions_ConcreteMethod, context, "Lcom/oracle/graal/api/meta/ResolvedJavaType;")                                                             \
    oop_field(Assumptions_ConcreteMethod, impl, "Lcom/oracle/graal/api/meta/ResolvedJavaMethod;")                                                              \
  end_class                                                                                                                                                    \
  start_class(Assumptions_CallSiteTargetValue)                                                                                                                 \
    oop_field(Assumptions_CallSiteTargetValue, callSite, "Ljava/lang/invoke/CallSite;")                                                                        \
    oop_field(Assumptions_CallSiteTargetValue, methodHandle, "Ljava/lang/invoke/MethodHandle;")                                                                \
  end_class                                                                                                                                                    \
  start_class(CompilationResult_Site)                                                                                                                          \
    int_field(CompilationResult_Site, pcOffset)                                                                                                                \
  end_class                                                                                                                                                    \
  start_class(CompilationResult_Call)                                                                                                                          \
    oop_field(CompilationResult_Call, target, "Lcom/oracle/graal/api/meta/InvokeTarget;")                                                                      \
    oop_field(CompilationResult_Call, debugInfo, "Lcom/oracle/graal/api/code/DebugInfo;")                                                                      \
  end_class                                                                                                                                                    \
  start_class(CompilationResult_DataPatch)                                                                                                                     \
    oop_field(CompilationResult_DataPatch, data, "Lcom/oracle/graal/api/code/CompilationResult$Data;")                                                         \
  end_class                                                                                                                                                    \
  start_class(InfopointReason)                                                                                                                                 \
    static_oop_field(InfopointReason, UNKNOWN, "Lcom/oracle/graal/api/code/InfopointReason;")                                                                  \
    static_oop_field(InfopointReason, SAFEPOINT, "Lcom/oracle/graal/api/code/InfopointReason;")                                                                \
    static_oop_field(InfopointReason, CALL, "Lcom/oracle/graal/api/code/InfopointReason;")                                                                     \
    static_oop_field(InfopointReason, IMPLICIT_EXCEPTION, "Lcom/oracle/graal/api/code/InfopointReason;")                                                       \
    static_oop_field(InfopointReason, METHOD_START, "Lcom/oracle/graal/api/code/InfopointReason;")                                                             \
    static_oop_field(InfopointReason, METHOD_END, "Lcom/oracle/graal/api/code/InfopointReason;")                                                               \
    static_oop_field(InfopointReason, LINE_NUMBER, "Lcom/oracle/graal/api/code/InfopointReason;")                                                              \
  end_class                                                                                                                                                    \
  start_class(CompilationResult_Infopoint)                                                                                                                     \
    oop_field(CompilationResult_Infopoint, debugInfo, "Lcom/oracle/graal/api/code/DebugInfo;")                                                                 \
    oop_field(CompilationResult_Infopoint, reason, "Lcom/oracle/graal/api/code/InfopointReason;")                                                              \
  end_class                                                                                                                                                    \
  start_class(CompilationResult_ExceptionHandler)                                                                                                              \
    int_field(CompilationResult_ExceptionHandler, handlerPos)                                                                                                  \
  end_class                                                                                                                                                    \
  start_class(CompilationResult_Mark)                                                                                                                          \
    oop_field(CompilationResult_Mark, id, "Ljava/lang/Object;")                                                                                                \
  end_class                                                                                                                                                    \
  start_class(DebugInfo)                                                                                                                                       \
    oop_field(DebugInfo, bytecodePosition, "Lcom/oracle/graal/api/code/BytecodePosition;")                                                                     \
    oop_field(DebugInfo, referenceMap, "Lcom/oracle/graal/api/code/ReferenceMap;")                                                                             \
    oop_field(DebugInfo, calleeSaveInfo, "Lcom/oracle/graal/api/code/RegisterSaveLayout;")                                                                     \
  end_class                                                                                                                                                    \
  start_class(HotSpotReferenceMap)                                                                                                                             \
    oop_field(HotSpotReferenceMap, registerRefMap, "Ljava/util/BitSet;")                                                                                       \
    oop_field(HotSpotReferenceMap, frameRefMap, "Ljava/util/BitSet;")                                                                                          \
  end_class                                                                                                                                                    \
  start_class(RegisterSaveLayout)                                                                                                                              \
    oop_field(RegisterSaveLayout, registers, "[Lcom/oracle/graal/api/code/Register;")                                                                          \
    oop_field(RegisterSaveLayout, slots, "[I")                                                                                                                 \
  end_class                                                                                                                                                    \
  start_class(BitSet)                                                                                                                                          \
    oop_field(BitSet, words, "[J")                                                                                                                             \
  end_class                                                                                                                                                    \
  start_class(BytecodeFrame)                                                                                                                                   \
    oop_field(BytecodeFrame, values, "[Lcom/oracle/graal/api/meta/Value;")                                                                                     \
    int_field(BytecodeFrame, numLocals)                                                                                                                        \
    int_field(BytecodeFrame, numStack)                                                                                                                         \
    int_field(BytecodeFrame, numLocks)                                                                                                                         \
    boolean_field(BytecodeFrame, rethrowException)                                                                                                             \
    boolean_field(BytecodeFrame, duringCall)                                                                                                                   \
    static_int_field(BytecodeFrame, BEFORE_BCI)                                                                                                                \
  end_class                                                                                                                                                    \
  start_class(BytecodePosition)                                                                                                                                \
    oop_field(BytecodePosition, caller, "Lcom/oracle/graal/api/code/BytecodePosition;")                                                                        \
    oop_field(BytecodePosition, method, "Lcom/oracle/graal/api/meta/ResolvedJavaMethod;")                                                                      \
    int_field(BytecodePosition, bci)                                                                                                                           \
  end_class                                                                                                                                                    \
  start_class(Constant)                                                                                                                                        \
    oop_field(Constant, kind, "Lcom/oracle/graal/api/meta/Kind;")                                                                                              \
  end_class                                                                                                                                                    \
  start_class(PrimitiveConstant)                                                                                                                               \
    long_field(PrimitiveConstant, primitive)                                                                                                                   \
  end_class                                                                                                                                                    \
  start_class(NullConstant)                                                                                                                                    \
  end_class                                                                                                                                                    \
  start_class(HotSpotObjectConstant)                                                                                                                           \
    oop_field(HotSpotObjectConstant, object, "Ljava/lang/Object;")                                                                                             \
  end_class                                                                                                                                                    \
  start_class(HotSpotMetaspaceConstant)                                                                                                                        \
    long_field(HotSpotMetaspaceConstant, primitive)                                                                                                            \
    oop_field(HotSpotMetaspaceConstant, metaspaceObject, "Ljava/lang/Object;")                                                                                 \
  end_class                                                                                                                                                    \
  start_class(Kind)                                                                                                                                            \
    char_field(Kind, typeChar)                                                                                                                                 \
    static_oop_field(Kind, Boolean, "Lcom/oracle/graal/api/meta/Kind;");                                                                                       \
    static_oop_field(Kind, Byte, "Lcom/oracle/graal/api/meta/Kind;");                                                                                          \
    static_oop_field(Kind, Char, "Lcom/oracle/graal/api/meta/Kind;");                                                                                          \
    static_oop_field(Kind, Short, "Lcom/oracle/graal/api/meta/Kind;");                                                                                         \
    static_oop_field(Kind, Int, "Lcom/oracle/graal/api/meta/Kind;");                                                                                           \
    static_oop_field(Kind, Long, "Lcom/oracle/graal/api/meta/Kind;");                                                                                          \
  end_class                                                                                                                                                    \
  start_class(Value)                                                                                                                                           \
    oop_field(Value, kind, "Lcom/oracle/graal/api/meta/Kind;")                                                                                                 \
    static_oop_field(Value, ILLEGAL, "Lcom/oracle/graal/api/meta/AllocatableValue;");                                                                          \
  end_class                                                                                                                                                    \
  start_class(RegisterValue)                                                                                                                                   \
    oop_field(RegisterValue, reg, "Lcom/oracle/graal/api/code/Register;")                                                                                      \
  end_class                                                                                                                                                    \
  start_class(code_Register)                                                                                                                                   \
    int_field(code_Register, number)                                                                                                                           \
  end_class                                                                                                                                                    \
  start_class(StackSlot)                                                                                                                                       \
    int_field(StackSlot, offset)                                                                                                                               \
    boolean_field(StackSlot, addFrameSize)                                                                                                                     \
  end_class                                                                                                                                                    \
  start_class(VirtualObject)                                                                                                                                   \
    int_field(VirtualObject, id)                                                                                                                               \
    oop_field(VirtualObject, type, "Lcom/oracle/graal/api/meta/ResolvedJavaType;")                                                                             \
    oop_field(VirtualObject, values, "[Lcom/oracle/graal/api/meta/Value;")                                                                                     \
  end_class                                                                                                                                                    \
  start_class(HotSpotMonitorValue)                                                                                                                             \
    oop_field(HotSpotMonitorValue, owner, "Lcom/oracle/graal/api/meta/Value;")                                                                                 \
    oop_field(HotSpotMonitorValue, slot, "Lcom/oracle/graal/api/code/StackSlot;")                                                                              \
    boolean_field(HotSpotMonitorValue, eliminated)                                                                                                             \
  end_class                                                                                                                                                    \
  start_class(SpeculationLog)                                                                                                                                  \
    oop_field(SpeculationLog, lastFailed, "Ljava/lang/Object;")                                                                                                \
  end_class                                                                                                                                                    \
  start_class(HotSpotStackFrameReference)                                                                                                                      \
    oop_field(HotSpotStackFrameReference, compilerToVM, "Lcom/oracle/graal/hotspot/bridge/CompilerToVM;")                                                      \
    long_field(HotSpotStackFrameReference, stackPointer)                                                                                                       \
    int_field(HotSpotStackFrameReference, frameNumber)                                                                                                         \
    int_field(HotSpotStackFrameReference, bci)                                                                                                                 \
    long_field(HotSpotStackFrameReference, metaspaceMethod)                                                                                                    \
    oop_field(HotSpotStackFrameReference, locals, "[Ljava/lang/Object;")                                                                                       \
    oop_field(HotSpotStackFrameReference, localIsVirtual, "[Z")                                                                                                \
  end_class                                                                                                                                                    \
  /* end*/

#define START_CLASS(name)                                                                                                                                      \
class name : AllStatic {                                                                                                                                       \
  private:                                                                                                                                                     \
    friend class GraalCompiler;                                                                                                                                \
    static void check(oop obj) {                                                                                                                               \
        assert(obj != NULL, "NULL field access of class " #name);                                                                                              \
        assert(obj->is_a(SystemDictionary::name##_klass()), "wrong class, " #name " expected");                                                                \
    }                                                                                                                                                          \
    static void compute_offsets();                                                                                                                             \
  public:                                                                                                                                                      \
    static Klass* klass() { return SystemDictionary::name##_klass(); }

#define END_CLASS };

#define FIELD(name, type, accessor)                                                                                                                            \
    static int _##name##_offset;                                                                                                                               \
    static type name(oop obj)                   { check(obj); return obj->accessor(_##name##_offset); }                                                        \
    static type name(Handle& obj)                { check(obj()); return obj->accessor(_##name##_offset); }                                                     \
    static type name(jobject obj)               { check(JNIHandles::resolve(obj)); return JNIHandles::resolve(obj)->accessor(_##name##_offset); }              \
    static void set_##name(oop obj, type x)     { check(obj); obj->accessor##_put(_##name##_offset, x); }                                                      \
    static void set_##name(Handle& obj, type x)  { check(obj()); obj->accessor##_put(_##name##_offset, x); }                                                   \
    static void set_##name(jobject obj, type x) { check(JNIHandles::resolve(obj)); JNIHandles::resolve(obj)->accessor##_put(_##name##_offset, x); }

#define CHAR_FIELD(klass, name) FIELD(name, jchar, char_field)
#define INT_FIELD(klass, name) FIELD(name, jint, int_field)
#define BOOLEAN_FIELD(klass, name) FIELD(name, jboolean, bool_field)
#define LONG_FIELD(klass, name) FIELD(name, jlong, long_field)
#define FLOAT_FIELD(klass, name) FIELD(name, jfloat, float_field)
#define OOP_FIELD(klass, name, signature) FIELD(name, oop, obj_field)
#define STATIC_OOP_FIELD(klassName, name, signature)                                                                                                           \
    static int _##name##_offset;                                                                                                                               \
    static oop name() {                                                                                                                                        \
      InstanceKlass* ik = InstanceKlass::cast(klassName::klass());                                                                                             \
      address addr = ik->static_field_addr(_##name##_offset - InstanceMirrorKlass::offset_of_static_fields());                                                 \
      if (UseCompressedOops) {                                                                                                                                 \
        return oopDesc::load_decode_heap_oop((narrowOop *)addr);                                                                                               \
      } else {                                                                                                                                                 \
        return oopDesc::load_decode_heap_oop((oop*)addr);                                                                                                      \
      }                                                                                                                                                        \
    }                                                                                                                                                          \
    static void set_##name(oop x) {                                                                                                                            \
      InstanceKlass* ik = InstanceKlass::cast(klassName::klass());                                                                                             \
      address addr = ik->static_field_addr(_##name##_offset - InstanceMirrorKlass::offset_of_static_fields());                                                 \
      if (UseCompressedOops) {                                                                                                                                 \
        oop_store((narrowOop *)addr, x);                                                                                                                       \
      } else {                                                                                                                                                 \
        oop_store((oop*)addr, x);                                                                                                                              \
      }                                                                                                                                                        \
    }
#define STATIC_INT_FIELD(klassName, name)                                                                                                                      \
    static int _##name##_offset;                                                                                                                               \
    static int name() {                                                                                                                                        \
      InstanceKlass* ik = InstanceKlass::cast(klassName::klass());                                                                                             \
      address addr = ik->static_field_addr(_##name##_offset - InstanceMirrorKlass::offset_of_static_fields());                                                 \
      return *((jint *)addr);                                                                                                                                  \
    }                                                                                                                                                          \
    static void set_##name(int x) {                                                                                                                            \
      InstanceKlass* ik = InstanceKlass::cast(klassName::klass());                                                                                             \
      address addr = ik->static_field_addr(_##name##_offset - InstanceMirrorKlass::offset_of_static_fields());                                                 \
      *((jint *)addr) = x;                                                                                                                                     \
    }
COMPILER_CLASSES_DO(START_CLASS, END_CLASS, CHAR_FIELD, INT_FIELD, BOOLEAN_FIELD, LONG_FIELD, FLOAT_FIELD, OOP_FIELD, STATIC_OOP_FIELD, STATIC_INT_FIELD)
#undef START_CLASS
#undef END_CLASS
#undef FIELD
#undef CHAR_FIELD
#undef INT_FIELD
#undef BOOLEAN_FIELD
#undef LONG_FIELD
#undef FLOAT_FIELD
#undef OOP_FIELD
#undef STATIC_OOP_FIELD
#undef STATIC_INT_FIELD

void compute_offset(int &dest_offset, Klass* klass, const char* name, const char* signature, bool static_field);

#endif // SHARE_VM_GRAAL_GRAAL_JAVA_ACCESS_HPP
