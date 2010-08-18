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

class C1XCompiler : public AbstractCompiler {

private:

  bool _initialized;

public:

  C1XCompiler() { _initialized = false; }

  virtual const char* name() { return "C1X"; }

  // Native / OSR not supported
  virtual bool supports_native()                 { return false; }
  virtual bool supports_osr   ()                 { return false; }

  // Pretend to be C1
  bool is_c1   ()                                { return true; }
  bool is_c2   ()                                { return false; }

  // Initialization
  virtual void initialize();

  // Compilation entry point for methods
  virtual void compile_method(ciEnv* env, ciMethod* target, int entry_bci);

  // Print compilation timers and statistics
  virtual void print_timers();

  static oop get_RiType(ciType *klass, klassOop accessor, TRAPS);
  static oop get_RiField(ciField *ciField, TRAPS);

  static void compute_offsets();

  // this needs to correspond to HotSpotXirGenerator.java
  enum MarkId {
    MARK_VERIFIED_ENTRY     = 1,
    MARK_UNVERIFIED_ENTRY   = 2,
    MARK_STATIC_CALL_STUB   = 1000
  };

/*
  static oop get_RiMethod(ciMethod *ciMethod, TRAPS);
  static oop get_RiType(klassOop klass, TRAPS);
  static oop get_RiMethod(methodOop method, TRAPS);
  static oop get_unresolved_RiType(oop klass, klassOop accessingType, TRAPS);
  static oop get_RiConstantPool(constantPoolOop cpOop, TRAPS);
*/
};

class C1XObjects : public AllStatic {

private:
  static GrowableArray<address>* _stubs;
  static GrowableArray<jobject>* _localHandles;

  static oop getObject(jlong id);

public:
  // this enum needs to have the same values as the one in HotSpotProxy.java
  enum CompilerObjectType {
    STUB          = 0x100000000000000l,
    METHOD        = 0x200000000000000l,
    CLASS         = 0x300000000000000l,
    SYMBOL        = 0x400000000000000l,
    CONSTANT_POOL = 0x500000000000000l,
    CONSTANT      = 0x600000000000000l,
    TYPE_MASK     = 0xf00000000000000l
  };

  static void initializeObjects();
  static void cleanupLocalObjects();

  static jlong addStub(address stub);
  static jlong add(Handle obj, CompilerObjectType type);
  template <typename T> static jlong add(T obj);

  static address getStub(jlong id);
  template <typename T> static T get(jlong id);

  template <typename T> static T toString(symbolOop symbol, TRAPS);
  static symbolOop toSymbol(jstring string);
};

template <> inline jlong C1XObjects::add<methodOop>(methodOop obj){
  assert(obj != NULL && obj->is_method(), "trying to add NULL or mistyped object");
  return add(Handle(obj), METHOD);
}
template <> inline jlong C1XObjects::add<klassOop>(klassOop obj) {
  assert(obj != NULL && obj->is_klass(), "trying to add NULL or mistyped object");
  return add(Handle(obj), CLASS);
}
template <> inline jlong C1XObjects::add<symbolOop>(symbolOop obj) {
  assert(obj != NULL && obj->is_symbol(), "trying to add NULL or mistyped object");
  return add(Handle(obj), SYMBOL);
}
template <> inline jlong C1XObjects::add<constantPoolOop>(constantPoolOop obj) {
  assert(obj != NULL && obj->is_constantPool(), "trying to add NULL or mistyped object");
  return add(Handle(obj), CONSTANT_POOL);
}
template <> inline jlong C1XObjects::add<oop>(oop obj) {
  assert(obj != NULL && obj->is_oop(), "trying to add NULL or mistyped object");
  return add(Handle(obj), CONSTANT);
}

template <> inline methodOop C1XObjects::get<methodOop>(jlong id){
  assert((id & TYPE_MASK) == METHOD, "METHOD expected");
  assert(getObject(id)->is_method(), "methodOop expected");
  return (methodOop)getObject(id);
}
template <> inline klassOop C1XObjects::get<klassOop>(jlong id) {
  assert((id & TYPE_MASK) == CLASS, "CLASS expected");
  assert(getObject(id)->is_klass(), "klassOop expected");
  return (klassOop)getObject(id);
}
template <> inline symbolOop C1XObjects::get<symbolOop>(jlong id) {
  assert((id & TYPE_MASK) == SYMBOL, "SYMBOL expected");
  assert(getObject(id)->is_symbol(), "symbolOop expected");
  return (symbolOop)getObject(id);
}
template <> inline constantPoolOop C1XObjects::get<constantPoolOop>(jlong id) {
  assert((id & TYPE_MASK) == CONSTANT_POOL, "CONSTANT_POOL expected");
  assert(getObject(id)->is_constantPool(), "constantPoolOop expected");
  return (constantPoolOop)getObject(id);
}
template <> inline oop C1XObjects::get<oop>(jlong id) {
  assert((id & TYPE_MASK) == CONSTANT, "CONSTANT expected");
  assert(getObject(id)->is_oop(true), "oop expected");
  return (oop)getObject(id);
}

template <> inline Handle C1XObjects::toString<Handle>(symbolOop symbol, TRAPS) {
  return java_lang_String::create_from_symbol(symbol, THREAD);
}
template <> inline oop C1XObjects::toString<oop>(symbolOop symbol, TRAPS) {
  return toString<Handle>(symbol, THREAD)();
}
template <> inline jstring C1XObjects::toString<jstring>(symbolOop symbol, TRAPS) {
  return (jstring)JNIHandles::make_local(toString<oop>(symbol, THREAD));
}
template <> inline jobject C1XObjects::toString<jobject>(symbolOop symbol, TRAPS) {
  return JNIHandles::make_local(toString<oop>(symbol, THREAD));
}

inline symbolOop C1XObjects::toSymbol(jstring string) {
  return java_lang_String::as_symbol_or_null(JNIHandles::resolve(string));
}

// Tracing macros

#define IF_TRACE_C1X_1 if (TraceC1X >= 1) 
#define IF_TRACE_C1X_2 if (TraceC1X >= 2) 
#define IF_TRACE_C1X_3 if (TraceC1X >= 3) 
#define IF_TRACE_C1X_4 if (TraceC1X >= 4) 
#define IF_TRACE_C1X_5 if (TraceC1X >= 5) 

#define TRACE_C1X_1 if (TraceC1X >= 1) tty->print("TraceC1X-1: "); if (TraceC1X >= 1) tty->print_cr
#define TRACE_C1X_2 if (TraceC1X >= 2) tty->print("   TraceC1X-2: "); if (TraceC1X >= 2) tty->print_cr
#define TRACE_C1X_3 if (TraceC1X >= 3) tty->print("      TraceC1X-3: "); if (TraceC1X >= 3) tty->print_cr
#define TRACE_C1X_4 if (TraceC1X >= 4) tty->print("         TraceC1X-4: "); if (TraceC1X >= 4) tty->print_cr
#define TRACE_C1X_5 if (TraceC1X >= 5) tty->print("            TraceC1X-5: "); if (TraceC1X >= 5) tty->print_cr


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


