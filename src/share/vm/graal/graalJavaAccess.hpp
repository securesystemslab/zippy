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

void graal_compute_offsets();

#include "classfile/systemDictionary.hpp"
#include "oops/instanceMirrorKlass.hpp"

/* This macro defines the structure of the CiTargetMethod - classes.
 * It will generate classes with accessors similar to javaClasses.hpp, but with specializations for oops, Handles and jni handles.
 *
 * The public interface of these classes will look like this:

 * class CiStackSlot : AllStatic {
 * public:
 *   static klassOop klass();
 *   static jint  index(oop obj);
 *   static jint  index(Handle obj);
 *   static jint  index(jobject obj);
 *   static void set_index(oop obj, jint x);
 *   static void set_index(Handle obj, jint x);
 *   static void set_index(jobject obj, jint x);
 * };
 *
 */

#define COMPILER_CLASSES_DO(start_class, end_class, char_field, int_field, boolean_field, long_field, float_field, oop_field, static_oop_field)   \
  start_class(HotSpotTypeResolved)                                                      \
    oop_field(HotSpotTypeResolved, javaMirror, "Ljava/lang/Class;")                     \
    oop_field(HotSpotTypeResolved, simpleName, "Ljava/lang/String;")                    \
    int_field(HotSpotTypeResolved, accessFlags)                                         \
    boolean_field(HotSpotTypeResolved, hasFinalizer)                                    \
    boolean_field(HotSpotTypeResolved, hasSubclass)                                     \
    boolean_field(HotSpotTypeResolved, hasFinalizableSubclass)                          \
    int_field(HotSpotTypeResolved, superCheckOffset)                                    \
    boolean_field(HotSpotTypeResolved, isArrayClass)                                    \
    boolean_field(HotSpotTypeResolved, isInstanceClass)                                 \
    boolean_field(HotSpotTypeResolved, isInterface)                                     \
    int_field(HotSpotTypeResolved, instanceSize)                                        \
  end_class                                                                             \
  start_class(HotSpotKlassOop)                                                          \
    oop_field(HotSpotKlassOop, javaMirror, "Ljava/lang/Class;")                         \
    end_class                                                                           \
  start_class(HotSpotMethodResolved)                                                    \
    oop_field(HotSpotMethodResolved, name, "Ljava/lang/String;")                        \
    oop_field(HotSpotMethodResolved, holder, "Lcom/oracle/graal/api/meta/RiResolvedType;")  \
    oop_field(HotSpotMethodResolved, javaMirror, "Ljava/lang/Object;")                  \
    int_field(HotSpotMethodResolved, codeSize)                                          \
    int_field(HotSpotMethodResolved, accessFlags)                                       \
    int_field(HotSpotMethodResolved, maxLocals)                                         \
    int_field(HotSpotMethodResolved, maxStackSize)                                      \
    boolean_field(HotSpotMethodResolved, canBeInlined)                                  \
  end_class                                                                             \
  start_class(HotSpotMethodData)                                                        \
    oop_field(HotSpotMethodData, hotspotMirror, "Ljava/lang/Object;")                   \
    int_field(HotSpotMethodData, normalDataSize)                                        \
    int_field(HotSpotMethodData, extraDataSize)                                         \
  end_class                                                                             \
  start_class(HotSpotType)                                                              \
    oop_field(HotSpotType, name, "Ljava/lang/String;")                                  \
  end_class                                                                             \
  start_class(HotSpotField)                                                             \
    oop_field(HotSpotField, constant, "Lcom/oracle/graal/api/meta/Constant;")             \
    int_field(HotSpotField, offset)                                                     \
    int_field(HotSpotField, accessFlags)                                                \
  end_class                                                                             \
  start_class(HotSpotCompiledMethod)                                                    \
    long_field(HotSpotCompiledMethod, nmethod)                                          \
    oop_field(HotSpotCompiledMethod, method, "Lcom/oracle/graal/api/meta/RiResolvedMethod;")\
  end_class                                                                             \
  start_class(HotSpotCodeInfo)                                                          \
    long_field(HotSpotCodeInfo, start)                                                  \
    oop_field(HotSpotCodeInfo, code, "[B")                                              \
  end_class                                                                             \
  start_class(HotSpotProxy)                                                             \
    static_oop_field(HotSpotProxy, DUMMY_CONSTANT_OBJ, "Ljava/lang/Long;")              \
  end_class                                                                             \
  start_class(HotSpotTargetMethod)                                                      \
    oop_field(HotSpotTargetMethod, targetMethod, "Lcom/oracle/graal/api/code/CiTargetMethod;") \
    oop_field(HotSpotTargetMethod, method, "Lcom/oracle/graal/hotspot/ri/HotSpotMethodResolved;") \
    oop_field(HotSpotTargetMethod, name, "Ljava/lang/String;")                          \
    oop_field(HotSpotTargetMethod, sites, "[Lcom/oracle/graal/api/code/CiTargetMethod$Site;") \
    oop_field(HotSpotTargetMethod, exceptionHandlers, "[Lcom/oracle/graal/api/code/CiTargetMethod$ExceptionHandler;") \
  end_class                                                                             \
  start_class(HotSpotExceptionHandler)                                                  \
    int_field(HotSpotExceptionHandler, startBci)                                        \
    int_field(HotSpotExceptionHandler, endBci)                                          \
    int_field(HotSpotExceptionHandler, handlerBci)                                      \
    int_field(HotSpotExceptionHandler, catchClassIndex)                                 \
    oop_field(HotSpotExceptionHandler, catchClass, "Lcom/oracle/graal/api/meta/RiType;")    \
  end_class                                                                             \
  start_class(CiTargetMethod)                                                           \
    int_field(CiTargetMethod, frameSize)                                                \
    int_field(CiTargetMethod, customStackAreaOffset)                                    \
    oop_field(CiTargetMethod, targetCode, "[B")                                         \
    oop_field(CiTargetMethod, assumptions, "Lcom/oracle/graal/api/code/CiAssumptions;")     \
    int_field(CiTargetMethod, targetCodeSize)                                           \
  end_class                                                                             \
  start_class(CiAssumptions)                                                            \
    oop_field(CiAssumptions, list, "[Lcom/oracle/graal/api/code/CiAssumptions$Assumption;") \
  end_class                                                                             \
  start_class(CiAssumptions_MethodContents)                                             \
    oop_field(CiAssumptions_MethodContents, method, "Lcom/oracle/graal/api/meta/RiResolvedMethod;") \
  end_class                                                                             \
  start_class(CiAssumptions_ConcreteSubtype)                                            \
    oop_field(CiAssumptions_ConcreteSubtype, context, "Lcom/oracle/graal/api/meta/RiResolvedType;") \
    oop_field(CiAssumptions_ConcreteSubtype, subtype, "Lcom/oracle/graal/api/meta/RiResolvedType;") \
  end_class                                                                             \
  start_class(CiAssumptions_ConcreteMethod)                                             \
    oop_field(CiAssumptions_ConcreteMethod, method, "Lcom/oracle/graal/api/meta/RiResolvedMethod;") \
    oop_field(CiAssumptions_ConcreteMethod, context, "Lcom/oracle/graal/api/meta/RiResolvedType;") \
    oop_field(CiAssumptions_ConcreteMethod, impl, "Lcom/oracle/graal/api/meta/RiResolvedMethod;") \
  end_class                                                                             \
  start_class(CiTargetMethod_Site)                                                      \
    int_field(CiTargetMethod_Site, pcOffset)                                            \
  end_class                                                                             \
  start_class(CiTargetMethod_Call)                                                      \
    oop_field(CiTargetMethod_Call, target, "Ljava/lang/Object;")                        \
    oop_field(CiTargetMethod_Call, debugInfo, "Lcom/oracle/graal/api/code/CiDebugInfo;")    \
  end_class                                                                             \
  start_class(CiTargetMethod_DataPatch)                                                 \
    oop_field(CiTargetMethod_DataPatch, constant, "Lcom/oracle/graal/api/meta/Constant;") \
    int_field(CiTargetMethod_DataPatch, alignment)                                      \
  end_class                                                                             \
  start_class(CiTargetMethod_Safepoint)                                                 \
    oop_field(CiTargetMethod_Safepoint, debugInfo, "Lcom/oracle/graal/api/code/CiDebugInfo;") \
  end_class                                                                             \
  start_class(CiTargetMethod_ExceptionHandler)                                          \
    int_field(CiTargetMethod_ExceptionHandler, handlerPos)                              \
  end_class                                                                             \
  start_class(CiTargetMethod_Mark)                                                      \
    oop_field(CiTargetMethod_Mark, id, "Ljava/lang/Object;")                            \
    oop_field(CiTargetMethod_Mark, references, "[Lcom/oracle/graal/api/code/CiTargetMethod$Mark;") \
  end_class                                                                             \
  start_class(CiDebugInfo)                                                              \
    oop_field(CiDebugInfo, codePos, "Lcom/oracle/graal/api/code/CiCodePos;")                \
    oop_field(CiDebugInfo, registerRefMap, "Lcom/oracle/graal/api/code/CiBitMap;")          \
    oop_field(CiDebugInfo, frameRefMap, "Lcom/oracle/graal/api/code/CiBitMap;")             \
  end_class                                                                             \
  start_class(GraalBitMap)                                                              \
    int_field(GraalBitMap, size)                                                        \
    long_field(GraalBitMap, low)                                                        \
    oop_field(GraalBitMap, extra, "[J")                                                 \
  end_class                                                                             \
  start_class(CiFrame)                                                                  \
    oop_field(CiFrame, values, "[Lcom/oracle/graal/api/meta/Value;")                      \
    int_field(CiFrame, numLocals)                                                       \
    int_field(CiFrame, numStack)                                                        \
    int_field(CiFrame, numLocks)                                                        \
    long_field(CiFrame, leafGraphId)                                                    \
    boolean_field(CiFrame, rethrowException)                                            \
    boolean_field(CiFrame, duringCall)                                                  \
  end_class                                                                             \
  start_class(CiCodePos)                                                                \
    oop_field(CiCodePos, caller, "Lcom/oracle/graal/api/code/CiCodePos;")                   \
    oop_field(CiCodePos, method, "Lcom/oracle/graal/api/meta/RiResolvedMethod;")            \
    int_field(CiCodePos, bci)                                                           \
  end_class                                                                             \
  start_class(CiConstant)                                                               \
    oop_field(CiConstant, kind, "Lcom/oracle/graal/api/meta/Kind;")                       \
    oop_field(CiConstant, object, "Ljava/lang/Object;")                                 \
    long_field(CiConstant, primitive)                                                   \
  end_class                                                                             \
  start_class(CiKind)                                                                   \
    char_field(CiKind, typeChar)                                                        \
    static_oop_field(CiKind, Boolean, "Lcom/oracle/graal/api/meta/Kind;");                \
    static_oop_field(CiKind, Byte, "Lcom/oracle/graal/api/meta/Kind;");                   \
    static_oop_field(CiKind, Char, "Lcom/oracle/graal/api/meta/Kind;");                   \
    static_oop_field(CiKind, Short, "Lcom/oracle/graal/api/meta/Kind;");                  \
    static_oop_field(CiKind, Int, "Lcom/oracle/graal/api/meta/Kind;");                    \
    static_oop_field(CiKind, Long, "Lcom/oracle/graal/api/meta/Kind;");                   \
  end_class                                                                             \
  start_class(CiRuntimeCall)                                                            \
    static_oop_field(CiRuntimeCall, UnwindException, "Lcom/oracle/graal/api/code/CiRuntimeCall;"); \
    static_oop_field(CiRuntimeCall, RegisterFinalizer, "Lcom/oracle/graal/api/code/CiRuntimeCall;"); \
    static_oop_field(CiRuntimeCall, SetDeoptInfo, "Lcom/oracle/graal/api/code/CiRuntimeCall;");    \
    static_oop_field(CiRuntimeCall, CreateNullPointerException, "Lcom/oracle/graal/api/code/CiRuntimeCall;"); \
    static_oop_field(CiRuntimeCall, CreateOutOfBoundsException, "Lcom/oracle/graal/api/code/CiRuntimeCall;"); \
    static_oop_field(CiRuntimeCall, JavaTimeMillis, "Lcom/oracle/graal/api/code/CiRuntimeCall;");  \
    static_oop_field(CiRuntimeCall, JavaTimeNanos, "Lcom/oracle/graal/api/code/CiRuntimeCall;");   \
    static_oop_field(CiRuntimeCall, Debug, "Lcom/oracle/graal/api/code/CiRuntimeCall;");           \
    static_oop_field(CiRuntimeCall, ArithmeticFrem, "Lcom/oracle/graal/api/code/CiRuntimeCall;");  \
    static_oop_field(CiRuntimeCall, ArithmeticDrem, "Lcom/oracle/graal/api/code/CiRuntimeCall;");  \
    static_oop_field(CiRuntimeCall, ArithmeticCos, "Lcom/oracle/graal/api/code/CiRuntimeCall;");   \
    static_oop_field(CiRuntimeCall, ArithmeticTan, "Lcom/oracle/graal/api/code/CiRuntimeCall;");   \
    static_oop_field(CiRuntimeCall, ArithmeticSin, "Lcom/oracle/graal/api/code/CiRuntimeCall;");   \
    static_oop_field(CiRuntimeCall, Deoptimize, "Lcom/oracle/graal/api/code/CiRuntimeCall;");      \
    static_oop_field(CiRuntimeCall, GenericCallback, "Lcom/oracle/graal/api/code/CiRuntimeCall;"); \
  end_class                                                                             \
  start_class(RiMethod)                                                                 \
  end_class                                                                             \
  start_class(CiValue)                                                                  \
    oop_field(CiValue, kind, "Lcom/oracle/graal/api/meta/Kind;")                          \
    static_oop_field(CiValue, IllegalValue, "Lcom/oracle/graal/api/meta/Value;");         \
  end_class                                                                             \
  start_class(CiRegisterValue)                                                          \
    oop_field(CiRegisterValue, reg, "Lcom/oracle/graal/api/code/CiRegister;")               \
  end_class                                                                             \
  start_class(CiRegister)                                                               \
    int_field(CiRegister, number)                                                       \
  end_class                                                                             \
  start_class(CiStackSlot)                                                              \
    int_field(CiStackSlot, offset)                                                      \
    boolean_field(CiStackSlot, addFrameSize)                                            \
  end_class                                                                             \
  start_class(CiVirtualObject)                                                          \
    int_field(CiVirtualObject, id)                                                      \
    oop_field(CiVirtualObject, type, "Lcom/oracle/graal/api/meta/RiType;")                  \
    oop_field(CiVirtualObject, values, "[Lcom/oracle/graal/api/meta/Value;")              \
  end_class                                                                             \
  start_class(CiMonitorValue)                                                           \
    oop_field(CiMonitorValue, owner, "Lcom/oracle/graal/api/meta/Value;")                 \
    oop_field(CiMonitorValue, lockData, "Lcom/oracle/graal/api/meta/Value;")              \
    boolean_field(CiMonitorValue, eliminated)                                           \
  end_class                                                                             \
  /* end*/

#define START_CLASS(name)                       \
class name : AllStatic {                      \
  private:                                      \
    friend class GraalCompiler;                   \
    static void check(oop obj) { assert(obj != NULL, "NULL field access of class " #name); assert(obj->is_a(SystemDictionary::name##_klass()), "wrong class, " #name " expected"); } \
    static void compute_offsets();              \
  public:                                       \
    static klassOop klass() { return SystemDictionary::name##_klass(); }

#define END_CLASS };

#define FIELD(name, type, accessor)             \
    static int _##name##_offset;                \
    static type name(oop obj)                   { check(obj); return obj->accessor(_##name##_offset); } \
    static type name(Handle& obj)                { check(obj()); return obj->accessor(_##name##_offset); } \
    static type name(jobject obj)               { check(JNIHandles::resolve(obj)); return JNIHandles::resolve(obj)->accessor(_##name##_offset); } \
    static void set_##name(oop obj, type x)     { check(obj); obj->accessor##_put(_##name##_offset, x); } \
    static void set_##name(Handle& obj, type x)  { check(obj()); obj->accessor##_put(_##name##_offset, x); } \
    static void set_##name(jobject obj, type x) { check(JNIHandles::resolve(obj)); JNIHandles::resolve(obj)->accessor##_put(_##name##_offset, x); }

#define CHAR_FIELD(klass, name) FIELD(name, jchar, char_field)
#define INT_FIELD(klass, name) FIELD(name, jint, int_field)
#define BOOLEAN_FIELD(klass, name) FIELD(name, jboolean, bool_field)
#define LONG_FIELD(klass, name) FIELD(name, jlong, long_field)
#define FLOAT_FIELD(klass, name) FIELD(name, jfloat, float_field)
#define OOP_FIELD(klass, name, signature) FIELD(name, oop, obj_field)
#define STATIC_OOP_FIELD(klassName, name, signature)                \
    static int _##name##_offset;                                    \
    static oop name() {                                             \
      instanceKlass* ik = instanceKlass::cast(klassName::klass());  \
      address addr = ik->static_field_addr(_##name##_offset - instanceMirrorKlass::offset_of_static_fields());       \
      if (UseCompressedOops) {                                      \
        return oopDesc::load_decode_heap_oop((narrowOop *)addr);    \
      } else {                                                      \
        return oopDesc::load_decode_heap_oop((oop*)addr);           \
      }                                                             \
    }                                                               \
    static void set_##name(oop x) {                                 \
      instanceKlass* ik = instanceKlass::cast(klassName::klass());  \
      address addr = ik->static_field_addr(_##name##_offset - instanceMirrorKlass::offset_of_static_fields());       \
      if (UseCompressedOops) {                                      \
        oop_store((narrowOop *)addr, x);       \
      } else {                                                      \
        oop_store((oop*)addr, x);              \
      }                                                             \
    }
COMPILER_CLASSES_DO(START_CLASS, END_CLASS, CHAR_FIELD, INT_FIELD, BOOLEAN_FIELD, LONG_FIELD, FLOAT_FIELD, OOP_FIELD, STATIC_OOP_FIELD)
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

void compute_offset(int &dest_offset, klassOop klass_oop, const char* name, const char* signature, bool static_field);
