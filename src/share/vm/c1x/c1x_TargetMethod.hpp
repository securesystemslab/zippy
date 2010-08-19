/*
 * Copyright 2000-2010 Sun Microsystems, Inc.  All Rights Reserved.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */

void c1x_compute_offsets();

// defines the structure of the CiTargetMethod - classes
// this will generate classes with accessors similar to javaClasses.hpp

#define COMPILER_CLASSES_DO(start_class, end_class, char_field, int_field, long_field, oop_field, static_oop_field)   \
  start_class(HotSpotTypeResolved)                                                      \
    long_field(HotSpotTypeResolved, vmId)                                               \
  end_class                                                                             \
  start_class(HotSpotMethod)                                                            \
    long_field(HotSpotMethod, vmId)                                                     \
  end_class                                                                             \
  start_class(HotSpotTargetMethod)                                                      \
    oop_field(HotSpotTargetMethod, targetMethod, "Lcom/sun/cri/ci/CiTargetMethod;")     \
    oop_field(HotSpotTargetMethod, method, "Lcom/sun/hotspot/c1x/HotSpotMethod;")       \
    oop_field(HotSpotTargetMethod, name, "Ljava/lang/String;")                          \
    oop_field(HotSpotTargetMethod, sites, "[Lcom/sun/cri/ci/CiTargetMethod$Site;")      \
  end_class                                                                             \
  start_class(CiTargetMethod)                                                           \
    int_field(CiTargetMethod, frameSize)                                                \
    oop_field(CiTargetMethod, targetCode, "[B")                                         \
    int_field(CiTargetMethod, targetCodeSize)                                           \
    int_field(CiTargetMethod, referenceRegisterCount)                                   \
  end_class                                                                             \
  start_class(CiTargetMethod_Site)                                                      \
    int_field(CiTargetMethod_Site, pcOffset)                                            \
  end_class                                                                             \
  start_class(CiTargetMethod_Call)                                                      \
    oop_field(CiTargetMethod_Call, runtimeCall, "Lcom/sun/cri/ci/CiRuntimeCall;")       \
    oop_field(CiTargetMethod_Call, method, "Lcom/sun/cri/ri/RiMethod;")                 \
    oop_field(CiTargetMethod_Call, symbol, "Ljava/lang/String;")                        \
    oop_field(CiTargetMethod_Call, globalStubID, "Ljava/lang/Object;")                  \
    oop_field(CiTargetMethod_Call, debugInfo, "Lcom/sun/cri/ci/CiDebugInfo;")           \
    oop_field(CiTargetMethod_Call, stackMap, "[B")                                      \
    oop_field(CiTargetMethod_Call, registerMap, "[B")                                   \
  end_class                                                                             \
  start_class(CiTargetMethod_DataPatch)                                                 \
    oop_field(CiTargetMethod_DataPatch, constant, "Lcom/sun/cri/ci/CiConstant;")        \
  end_class                                                                             \
  start_class(CiTargetMethod_Safepoint)                                                 \
    oop_field(CiTargetMethod_Safepoint, debugInfo, "Lcom/sun/cri/ci/CiDebugInfo;")      \
  end_class                                                                             \
  start_class(CiTargetMethod_ExceptionHandler)                                          \
    int_field(CiTargetMethod_ExceptionHandler, handlerPos)                              \
    oop_field(CiTargetMethod_ExceptionHandler, exceptionType, "Lcom/sun/cri/ri/RiType;")\
  end_class                                                                             \
  start_class(CiTargetMethod_Mark)                                                      \
    oop_field(CiTargetMethod_Mark, id, "Ljava/lang/Object;")                            \
    oop_field(CiTargetMethod_Mark, references, "[Lcom/sun/cri/ci/CiTargetMethod$Mark;") \
  end_class                                                                             \
  start_class(CiDebugInfo)                                                              \
    oop_field(CiDebugInfo, codePos, "Lcom/sun/cri/ci/CiCodePos;")                       \
    oop_field(CiDebugInfo, frame, "Lcom/sun/cri/ci/CiDebugInfo$Frame;")                 \
    oop_field(CiDebugInfo, registerRefMap, "[B")                                        \
    oop_field(CiDebugInfo, frameRefMap, "[B")                                           \
  end_class                                                                             \
  start_class(CiDebugInfo_Frame)                                                        \
    oop_field(CiDebugInfo_Frame, caller, "Lcom/sun/cri/ci/CiDebugInfo$Frame;")          \
    oop_field(CiDebugInfo_Frame, codePos, "Lcom/sun/cri/ci/CiCodePos;")                 \
    oop_field(CiDebugInfo_Frame, values, "[Lcom/sun/cri/ci/CiValue;")                   \
    int_field(CiDebugInfo_Frame, numLocals)                                             \
    int_field(CiDebugInfo_Frame, numStack)                                              \
    int_field(CiDebugInfo_Frame, numLocks)                                              \
  end_class                                                                             \
  start_class(CiCodePos)                                                                \
    oop_field(CiCodePos, caller, "Lcom/sun/cri/ci/CiCodePos;")                          \
    oop_field(CiCodePos, method, "Lcom/sun/cri/ri/RiMethod;")                           \
    int_field(CiCodePos, bci)                                                           \
  end_class                                                                             \
  start_class(CiConstant)                                                               \
    oop_field(CiConstant, kind, "Lcom/sun/cri/ci/CiKind;")                              \
    oop_field(CiConstant, object, "Ljava/lang/Object;")                                 \
    long_field(CiConstant, primitive)                                                   \
  end_class                                                                             \
  start_class(CiKind)                                                                   \
    char_field(CiKind, typeChar)                                                        \
  end_class                                                                             \
  start_class(CiRuntimeCall)                                                            \
    static_oop_field(CiRuntimeCall, Debug, "Lcom/sun/cri/ci/CiRuntimeCall;");           \
  end_class                                                                             \
  start_class(RiMethod)                                                                 \
  end_class                                                                             \
  start_class(CiRegisterValue)                                                          \
  end_class                                                                             \
  start_class(CiStackSlot)                                                              \
  end_class                                                                             \
  /* end*/

#define START_CLASS(name)                       \
  class name : AllStatic {                      \
  private:                                      \
    friend class C1XCompiler;                   \
    static void check(oop obj) { assert(obj != NULL, "NULL field access"); assert(obj->is_a(SystemDictionary::name##_klass()), "wrong class, " #name " expected"); } \
    static void compute_offsets();              \
  public:                                       \
    static klassOop klass() { return SystemDictionary::name##_klass(); }

#define END_CLASS };

#define FIELD(name, type, accessor)             \
    static int _##name##_offset;                \
    static type name(oop obj)                   { check(obj); return obj->accessor(_##name##_offset); } \
    static type name(jobject obj)               { check(JNIHandles::resolve(obj)); return JNIHandles::resolve(obj)->accessor(_##name##_offset); } \
    static void set_##name(oop obj, type x)     { check(obj); obj->accessor##_put(_##name##_offset, x); } \
    static void set_##name(jobject obj, type x) { check(JNIHandles::resolve(obj)); JNIHandles::resolve(obj)->accessor##_put(_##name##_offset, x); }

#define CHAR_FIELD(klass, name) FIELD(name, jchar, char_field)
#define INT_FIELD(klass, name) FIELD(name, jint, int_field)
#define LONG_FIELD(klass, name) FIELD(name, jlong, long_field)
#define OOP_FIELD(klass, name, signature) FIELD(name, oop, obj_field)
#define STATIC_OOP_FIELD(klassName, name, signature) \
    static int _##name##_offset;                \
    static oop name()             { return klassName::klass()->obj_field(_##name##_offset); } \
    static void set_##name(oop x) { klassName::klass()->obj_field_put(_##name##_offset, x); }

COMPILER_CLASSES_DO(START_CLASS, END_CLASS, CHAR_FIELD, INT_FIELD, LONG_FIELD, OOP_FIELD, STATIC_OOP_FIELD)
#undef START_CLASS
#undef END_CLASS
#undef FIELD
#undef CHAR_FIELD
#undef INT_FIELD
#undef LONG_FIELD
#undef OOP_FIELD
#undef STATIC_OOP_FIELD


