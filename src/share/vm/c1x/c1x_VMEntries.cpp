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

#include "precompiled.hpp"
#include "c1x/c1x_VMEntries.hpp"
#include "c1x/c1x_Compiler.hpp"
#include "c1x/c1x_JavaAccess.hpp"
#include "c1x/c1x_CodeInstaller.hpp"
#include "c1x/c1x_VMExits.hpp"
#include "c1x/c1x_VmIds.hpp"
#include "c1/c1_Runtime1.hpp"

// public byte[] RiMethod_code(long vmId);
JNIEXPORT jbyteArray JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1code(JNIEnv *env, jobject, jlong vmId) {
  methodHandle method = VmIds::get<methodOop>(vmId);
  int code_size = method->code_size();
  jbyteArray result = env->NewByteArray(code_size);
  env->SetByteArrayRegion(result, 0, code_size, (const jbyte *) method->code_base());
  return result;
}

// public int RiMethod_maxStackSize(long vmId);
JNIEXPORT jint JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1maxStackSize(JNIEnv *, jobject, jlong vmId) {
  return VmIds::get<methodOop>(vmId)->max_stack();
}

// public int RiMethod_maxLocals(long vmId);
JNIEXPORT jint JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1maxLocals(JNIEnv *, jobject, jlong vmId) {
  return VmIds::get<methodOop>(vmId)->max_locals();
}

// public RiType RiMethod_holder(long vmId);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1holder(JNIEnv *, jobject, jlong vmId) {
  VM_ENTRY_MARK
  KlassHandle klass = VmIds::get<methodOop>(vmId)->method_holder();
  Handle name = VmIds::toString<Handle>(klass->name(), CHECK_NULL);
  oop holder = C1XCompiler::createHotSpotTypeResolved(klass, name, CHECK_NULL);
  return JNIHandles::make_local(THREAD, holder);
}

// public String RiMethod_signature(long vmId);
JNIEXPORT jstring JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1signature(JNIEnv *env, jobject, jlong vmId) {
  VM_ENTRY_MARK
  methodOop method = VmIds::get<methodOop>(vmId);
  method->constMethod()->exception_table();
  return VmIds::toString<jstring>(method->signature(), THREAD);
}

// public int RiMethod_accessFlags(long vmId);
JNIEXPORT jint JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1accessFlags(JNIEnv *, jobject, jlong vmId) {
  return VmIds::get<methodOop>(vmId)->access_flags().as_int();
}

// public RiExceptionHandler[] RiMethod_exceptionHandlers(long vmId);
JNIEXPORT jobjectArray JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1exceptionHandlers(JNIEnv *, jobject, jlong vmId) {
  VM_ENTRY_MARK
  methodHandle method = VmIds::get<methodOop>(vmId);
  typeArrayHandle handlers = method->exception_table();
  int handler_count = handlers.is_null() ? 0 : handlers->length() / 4;

  instanceKlass::cast(HotSpotExceptionHandler::klass())->initialize(CHECK_NULL);
  objArrayHandle array = oopFactory::new_objArray(SystemDictionary::RiExceptionHandler_klass(), handler_count, CHECK_NULL);

  for (int i = 0; i < handler_count; i++) {
    // exception handlers are stored as four integers: start bci, end bci, handler bci, catch class constant pool index
    int base = i * 4;
    Handle entry = instanceKlass::cast(HotSpotExceptionHandler::klass())->allocate_instance(CHECK_NULL);
    HotSpotExceptionHandler::set_startBci(entry, handlers->int_at(base + 0));
    HotSpotExceptionHandler::set_endBci(entry, handlers->int_at(base + 1));
    HotSpotExceptionHandler::set_handlerBci(entry, handlers->int_at(base + 2));
    int catch_class_index = handlers->int_at(base + 3);
    HotSpotExceptionHandler::set_catchClassIndex(entry, catch_class_index);

    if (catch_class_index == 0) {
      HotSpotExceptionHandler::set_catchClass(entry, NULL);
    } else {
      constantPoolOop cp = instanceKlass::cast(method->method_holder())->constants();
      ciInstanceKlass* loading_klass = (ciInstanceKlass *) CURRENT_ENV->get_object(method->method_holder());
      bool is_accessible = false;
      ciKlass *klass = CURRENT_ENV->get_klass_by_index(cp, catch_class_index, is_accessible, loading_klass);
      oop catch_class = C1XCompiler::get_RiType(klass, method->method_holder(), CHECK_NULL);

      HotSpotExceptionHandler::set_catchClass(entry, catch_class);
    }
    array->obj_at_put(i, entry());
  }

  return (jobjectArray) JNIHandles::make_local(array());
}

// public boolean RiMethod_hasBalancedMonitors(long vmId);
JNIEXPORT jint JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1hasBalancedMonitors(JNIEnv *, jobject, jlong vmId) {
  ciMethod* cimethod;
  {
    VM_ENTRY_MARK;
    cimethod = (ciMethod*)CURRENT_ENV->get_object(VmIds::get<methodOop>(vmId));
  }
  return cimethod->has_balanced_monitors();
}

// public boolean RiMethod_uniqueConcreteMethod(long vmId);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1uniqueConcreteMethod(JNIEnv *, jobject, jlong vmId) {
  VM_ENTRY_MARK;
  ciMethod* cimethod = (ciMethod*)CURRENT_ENV->get_object(VmIds::get<methodOop>(vmId));
  if (cimethod->holder()->is_interface()) {
    // Cannot trust interfaces. Because of:
    // interface I { void foo(); }
    // class A { public void foo() {} }
    // class B extends A implements I { }
    // class C extends B { public void foo() { } }
    // class D extends B { }
    // Would lead to identify C.foo() as the unique concrete method for I.foo() without seeing A.foo().
    return false;
  }
  klassOop klass = (klassOop)cimethod->holder()->get_oop();
  methodOop method = (methodOop)cimethod->get_oop();
  methodOop unique_concrete = NULL;
  {
    MutexLocker locker(Compile_lock);
    unique_concrete = Dependencies::find_unique_concrete_method(klass, method);
  }
  if (unique_concrete == NULL) {
    return NULL;
  }

  Handle name = VmIds::toString<Handle>(unique_concrete->name(), CHECK_NULL);
  oop method_resolved = VMExits::createRiMethodResolved(VmIds::add<methodOop>(unique_concrete), name, CHECK_NULL);
  return JNIHandles::make_local(THREAD, method_resolved);
}

// public RiType RiSignature_lookupType(String returnType, HotSpotTypeResolved accessingClass);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiSignature_1lookupType(JNIEnv *env, jobject, jstring jname, jobject accessingClass) {
  VM_ENTRY_MARK;

  symbolOop nameSymbol = VmIds::toSymbol(jname);
  Handle name = JNIHandles::resolve(jname);

  oop result;
  if (nameSymbol == vmSymbols::int_signature()) {
    result = VMExits::createRiTypePrimitive((int) T_INT, THREAD);
  } else if (nameSymbol == vmSymbols::long_signature()) {
    result = VMExits::createRiTypePrimitive((int) T_LONG, THREAD);
  } else if (nameSymbol == vmSymbols::bool_signature()) {
    result = VMExits::createRiTypePrimitive((int) T_BOOLEAN, THREAD);
  } else if (nameSymbol == vmSymbols::char_signature()) {
    result = VMExits::createRiTypePrimitive((int) T_CHAR, THREAD);
  } else if (nameSymbol == vmSymbols::short_signature()) {
    result = VMExits::createRiTypePrimitive((int) T_SHORT, THREAD);
  } else if (nameSymbol == vmSymbols::byte_signature()) {
    result = VMExits::createRiTypePrimitive((int) T_BYTE, THREAD);
  } else if (nameSymbol == vmSymbols::double_signature()) {
    result = VMExits::createRiTypePrimitive((int) T_DOUBLE, THREAD);
  } else if (nameSymbol == vmSymbols::float_signature()) {
    result = VMExits::createRiTypePrimitive((int) T_FLOAT, THREAD);
  } else {
    klassOop resolved_type = NULL;
    // if the name isn't in the symbol table then the class isn't loaded anyway...
    if (nameSymbol != NULL) {
      Handle classloader;
      Handle protectionDomain;
      if (JNIHandles::resolve(accessingClass) != NULL) {
        classloader = java_lang_Class::as_klassOop(HotSpotTypeResolved::javaMirror(accessingClass))->klass_part()->class_loader();
        protectionDomain = java_lang_Class::as_klassOop(HotSpotTypeResolved::javaMirror(accessingClass))->klass_part()->protection_domain();
      }
      resolved_type = SystemDictionary::resolve_or_null(nameSymbol, classloader, protectionDomain, THREAD);
      if (HAS_PENDING_EXCEPTION) {
        CLEAR_PENDING_EXCEPTION;
        resolved_type = NULL;
      }
    }
    if (resolved_type != NULL) {
      result = C1XCompiler::createHotSpotTypeResolved(resolved_type, name, CHECK_NULL);
    } else {
      result = VMExits::createRiTypeUnresolved(name, THREAD);
    }
  }

  return JNIHandles::make_local(THREAD, result);
}

// public Object RiConstantPool_lookupConstant(long vmId, int cpi);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupConstant(JNIEnv *env, jobject, jlong vmId, jint index) {
  VM_ENTRY_MARK;

  constantPoolOop cp = VmIds::get<constantPoolOop>(vmId);

  oop result = NULL;
  constantTag tag = cp->tag_at(index);
  if (tag.is_int()) {
    result = VMExits::createCiConstant(CiKind::Int(), cp->int_at(index), CHECK_0);
  } else if (tag.is_long()) {
    result = VMExits::createCiConstant(CiKind::Long(), cp->long_at(index), CHECK_0);
  } else if (tag.is_float()) {
    result = VMExits::createCiConstantFloat(cp->float_at(index), CHECK_0);
  } else if (tag.is_double()) {
    result = VMExits::createCiConstantDouble(cp->double_at(index), CHECK_0);
  } else if (tag.is_string() || tag.is_unresolved_string()) {
    oop string = NULL;
    if (cp->is_pseudo_string_at(index)) {
      string = cp->pseudo_string_at(index);
    } else {
      string = cp->string_at(index, THREAD);
      if (HAS_PENDING_EXCEPTION) {
        CLEAR_PENDING_EXCEPTION;
        // TODO: Gracefully exit compilation.
        fatal("out of memory during compilation!");
        return NULL;
      }
    }
    result = VMExits::createCiConstantObject(string, CHECK_0);
  } else if (tag.is_klass() || tag.is_unresolved_klass()) {
    bool ignore;
    ciInstanceKlass* accessor = (ciInstanceKlass*) ciEnv::current()->get_object(cp->pool_holder());
    ciKlass* klass = ciEnv::current()->get_klass_by_index(cp, index, ignore, accessor);
    result = C1XCompiler::get_RiType(klass, cp->pool_holder(), CHECK_NULL);
  } else if (tag.is_object()) {
    oop obj = cp->object_at(index);
    assert(obj->is_instance(), "must be an instance");
    result = VMExits::createCiConstantObject(obj, CHECK_NULL);
  } else {
    ShouldNotReachHere();
  }

  return JNIHandles::make_local(THREAD, result);
}

// public RiMethod RiConstantPool_lookupMethod(long vmId, int cpi, byte byteCode);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupMethod(JNIEnv *env, jobject, jlong vmId, jint index, jbyte byteCode) {
  VM_ENTRY_MARK;

  index = C1XCompiler::to_cp_index_u2(index);
  constantPoolHandle cp = VmIds::get<constantPoolOop>(vmId);

  Bytecodes::Code bc = (Bytecodes::Code) (((int) byteCode) & 0xFF);
  ciInstanceKlass* loading_klass = (ciInstanceKlass *) CURRENT_ENV->get_object(cp->pool_holder());
  ciMethod *cimethod = CURRENT_ENV->get_method_by_index(cp, index, bc, loading_klass);
  if (cimethod->is_loaded()) {
    methodOop method = (methodOop) cimethod->get_oop();
    Handle name = VmIds::toString<Handle>(method->name(), CHECK_NULL);
    return JNIHandles::make_local(THREAD, VMExits::createRiMethodResolved(VmIds::add<methodOop>(method), name, THREAD));
  } else {
    Handle name = VmIds::toString<Handle>((symbolOop) cimethod->name()->get_oop(), CHECK_NULL);
    Handle signature = VmIds::toString<Handle>((symbolOop) cimethod->signature()->as_symbol()->get_oop(), CHECK_NULL);
    Handle holder = C1XCompiler::get_RiType(cimethod->holder(), cp->klass(), THREAD);
    return JNIHandles::make_local(THREAD, VMExits::createRiMethodUnresolved(name, signature, holder, THREAD));
  }
}

// public RiSignature RiConstantPool_lookupSignature(long vmId, int cpi);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupSignature(JNIEnv *env, jobject, jlong vmId, jint index) {
  fatal("currently unsupported");
  return NULL;
}

// public RiType RiConstantPool_lookupType(long vmId, int cpi);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupType(JNIEnv *env, jobject, jlong vmId, jint index) {
  VM_ENTRY_MARK;

  constantPoolOop cp = VmIds::get<constantPoolOop>(vmId);

  ciInstanceKlass* loading_klass = (ciInstanceKlass *) CURRENT_ENV->get_object(cp->pool_holder());
  bool is_accessible = false;
  ciKlass *klass = CURRENT_ENV->get_klass_by_index(cp, index, is_accessible, loading_klass);
  return JNIHandles::make_local(THREAD, C1XCompiler::get_RiType(klass, cp->klass(), THREAD));
}

// public RiField RiConstantPool_lookupField(long vmId, int cpi);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupField(JNIEnv *env, jobject, jlong vmId, jint index, jbyte byteCode) {
  VM_ENTRY_MARK;

  index = C1XCompiler::to_cp_index_u2(index);
  constantPoolOop cp = VmIds::get<constantPoolOop>(vmId);

  ciInstanceKlass* loading_klass = (ciInstanceKlass *) CURRENT_ENV->get_object(cp->pool_holder());
  ciField *field = CURRENT_ENV->get_field_by_index(loading_klass, index);
  Bytecodes::Code code = (Bytecodes::Code)(((int) byteCode) & 0xFF);
  Handle field_handle = C1XCompiler::get_RiField(field, loading_klass, cp->pool_holder(), code, THREAD);
  bool is_constant = field->is_constant();
  if (is_constant && field->is_static()) {
    ciConstant constant = field->constant_value();
    oop constant_object = NULL;
    switch (constant.basic_type()) {
      case T_OBJECT:
      case T_ARRAY:
        {
          ciObject* obj = constant.as_object();
          if (obj->is_null_object()) {
            constant_object = VMExits::createCiConstantObject(NULL, CHECK_0);
          } else if (obj->can_be_constant()) {
            constant_object = VMExits::createCiConstantObject(constant.as_object()->get_oop(), CHECK_0);
          }
        }
        break;
      case T_DOUBLE:
        constant_object = VMExits::createCiConstantDouble(constant.as_double(), CHECK_0);
        break;
      case T_FLOAT:
        constant_object = VMExits::createCiConstantFloat(constant.as_float(), CHECK_0);
        break;
      case T_LONG:
        constant_object = VMExits::createCiConstant(CiKind::Long(), constant.as_long(), CHECK_0);
        break;
      case T_INT:
        constant_object = VMExits::createCiConstant(CiKind::Int(), constant.as_int(), CHECK_0);
        break;
      case T_SHORT:
        constant_object = VMExits::createCiConstant(CiKind::Short(), constant.as_int(), CHECK_0);
        break;
      case T_CHAR:
        constant_object = VMExits::createCiConstant(CiKind::Char(), constant.as_int(), CHECK_0);
        break;
      case T_BYTE:
        constant_object = VMExits::createCiConstant(CiKind::Byte(), constant.as_int(), CHECK_0);
        break;
      case T_BOOLEAN:
        constant_object = VMExits::createCiConstant(CiKind::Boolean(), constant.as_int(), CHECK_0);
        break;
      default:
        constant.print();
        fatal("Unhandled constant");
    }
    if (constant_object != NULL) {
      HotSpotField::set_constant(field_handle, constant_object);
    }
  }
  return JNIHandles::make_local(THREAD, field_handle());
}

// public RiConstantPool RiType_constantPool(HotSpotTypeResolved klass);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1constantPool(JNIEnv *, jobject, jobject klass) {
  VM_ENTRY_MARK;

  assert(JNIHandles::resolve(klass) != NULL, "");
  constantPoolOop constantPool = ((instanceKlass*)java_lang_Class::as_klassOop(HotSpotTypeResolved::javaMirror(klass))->klass_part())->constants();
  return JNIHandles::make_local(VMExits::createRiConstantPool(VmIds::add<constantPoolOop>(constantPool), THREAD));
}

// public RiMethod RiType_resolveMethodImpl(HotSpotTypeResolved klass, String name, String signature);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_3resolveMethodImpl(JNIEnv *, jobject, jobject resolved_type, jstring name, jstring signature) {
  VM_ENTRY_MARK;

  assert(JNIHandles::resolve(resolved_type) != NULL, "");
  klassOop klass = java_lang_Class::as_klassOop(HotSpotTypeResolved::javaMirror(resolved_type));
  symbolOop name_symbol = VmIds::toSymbol(name);
  symbolOop signature_symbol = VmIds::toSymbol(signature);
  methodOop method = klass->klass_part()->lookup_method(name_symbol, signature_symbol);
  if (method == NULL) {
    if (TraceC1X >= 3) {
      ResourceMark rm;
      tty->print_cr("Could not resolve method %s %s on klass %d", name_symbol->as_C_string(), signature_symbol->as_C_string(), klass->klass_part()->name()->as_C_string());
    }
    return NULL;
  }
  return JNIHandles::make_local(THREAD, VMExits::createRiMethodResolved(VmIds::add<methodOop>(method), Handle(JNIHandles::resolve(name)), THREAD));
}

// public boolean RiType_isSubtypeOf(HotSpotTypeResolved klass, RiType other);
JNIEXPORT jboolean JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_2isSubtypeOf(JNIEnv *, jobject, jobject klass, jobject jother) {
  oop other = JNIHandles::resolve(jother);
  assert(other->is_a(HotSpotTypeResolved::klass()), "resolved hotspot type expected");
  assert(JNIHandles::resolve(klass) != NULL, "");
  klassOop thisKlass = java_lang_Class::as_klassOop(HotSpotTypeResolved::javaMirror(klass));
  klassOop otherKlass = java_lang_Class::as_klassOop(HotSpotTypeResolved::javaMirror(other));
  if (thisKlass->klass_part()->oop_is_instance_slow()) {
    return instanceKlass::cast(thisKlass)->is_subtype_of(otherKlass);
  } else if (thisKlass->klass_part()->oop_is_array()) {
    return arrayKlass::cast(thisKlass)->is_subtype_of(otherKlass);
  } else {
    fatal("unexpected class type");
  }
}

// public RiType RiType_componentType(HotSpotResolvedType klass);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1componentType(JNIEnv *, jobject, jobject klass) {
  ciArrayKlass* array_klass;
  {
    VM_ENTRY_MARK;
    assert(JNIHandles::resolve(klass) != NULL, "");
    array_klass = (ciArrayKlass *) CURRENT_ENV->get_object(java_lang_Class::as_klassOop(HotSpotTypeResolved::javaMirror(klass)));
  }
  ciType* element_type = array_klass->element_type();

  VM_ENTRY_MARK;
  assert(JNIHandles::resolve(klass) != NULL, "");
  return JNIHandles::make_local(C1XCompiler::get_RiType(element_type, java_lang_Class::as_klassOop(HotSpotTypeResolved::javaMirror(klass)), THREAD));
}


// public RiType RiType_uniqueConcreteSubtype(HotSpotResolvedType klass);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1uniqueConcreteSubtype(JNIEnv *, jobject, jobject klass) {

  Thread* THREAD = Thread::current();
  KlassHandle klass_handle(java_lang_Class::as_klassOop(HotSpotTypeResolved::javaMirror(klass)));
  ciInstanceKlass* k = NULL;
  {
    VM_ENTRY_MARK;
    k = (ciInstanceKlass *) CURRENT_ENV->get_object(klass_handle());
  }

  if (k->is_abstract()) {
    ciInstanceKlass* sub = k->unique_concrete_subklass();
    if (sub != NULL && sub->is_leaf_type()) {
      VM_ENTRY_MARK;
      return JNIHandles::make_local(C1XCompiler::get_RiType(sub, klass_handle, THREAD));
    }
  } else if (k->is_leaf_type()) {
    assert(!k->is_interface(), "");
    return klass;
  }

  return NULL;
}

// public RiType RiType_arrayOf(HotSpotTypeResolved klass);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1arrayOf(JNIEnv *, jobject, jobject klass) {
  VM_ENTRY_MARK;

  KlassHandle klass_handle(java_lang_Class::as_klassOop(HotSpotTypeResolved::javaMirror(klass)));
  KlassHandle array = klass_handle->array_klass(THREAD);
  Handle name = VmIds::toString<Handle>(array->name(), CHECK_NULL);
  return JNIHandles::make_local(THREAD, C1XCompiler::createHotSpotTypeResolved(array, name, THREAD));
}

// public RiType getPrimitiveArrayType(CiKind kind);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_getPrimitiveArrayType(JNIEnv *env, jobject, jobject kind) {
  VM_ENTRY_MARK;
  BasicType type = C1XCompiler::kindToBasicType(CiKind::typeChar(kind));
  assert(type != T_OBJECT, "primitive type expecteds");
  ciKlass* klass = ciTypeArrayKlass::make(type);
  return JNIHandles::make_local(THREAD, C1XCompiler::get_RiType(klass, KlassHandle(NULL, THREAD), THREAD));
}

// public RiType getType(Class<?> javaClass);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_getType(JNIEnv *env, jobject, jobject javaClass) {
  VM_ENTRY_MARK;
  oop javaClassOop = JNIHandles::resolve(javaClass);
  if (javaClassOop == NULL) {
    fatal("argument to VMEntries.getType must not be NULL");
  } else if (java_lang_Class::is_primitive(javaClassOop)) {
    BasicType basicType = java_lang_Class::primitive_type(javaClassOop);
    return JNIHandles::make_local(THREAD, VMExits::createRiTypePrimitive((int) basicType, THREAD));
  } else {
    KlassHandle klass = java_lang_Class::as_klassOop(javaClassOop);
    Handle name = java_lang_String::create_from_symbol(klass->name(), CHECK_NULL);

    oop type = C1XCompiler::createHotSpotTypeResolved(klass, name, CHECK_NULL);
    return JNIHandles::make_local(THREAD, type);
  }
}


// helpers used to set fields in the HotSpotVMConfig object
jfieldID getFieldID(JNIEnv* env, jobject obj, const char* name, const char* sig) {
  jfieldID id = env->GetFieldID(env->GetObjectClass(obj), name, sig);
  if (id == NULL) {
    TRACE_C1X_1("field not found: %s (%s)", name, sig);
    fatal("field not found");
  }
  return id;
}

void set_boolean(JNIEnv* env, jobject obj, const char* name, bool value) { env->SetBooleanField(obj, getFieldID(env, obj, name, "Z"), value); }
void set_int(JNIEnv* env, jobject obj, const char* name, int value) { env->SetIntField(obj, getFieldID(env, obj, name, "I"), value); }
void set_long(JNIEnv* env, jobject obj, const char* name, long value) { env->SetLongField(obj, getFieldID(env, obj, name, "J"), value); }
void set_object(JNIEnv* env, jobject obj, const char* name, jobject value) { env->SetObjectField(obj, getFieldID(env, obj, name, "Ljava/lang/Object;"), value); }
void set_int_array(JNIEnv* env, jobject obj, const char* name, jarray value) { env->SetObjectField(obj, getFieldID(env, obj, name, "[I"), value); }

jboolean get_boolean(JNIEnv* env, jobject obj, const char* name) { return env->GetBooleanField(obj, getFieldID(env, obj, name, "Z")); }
jint get_int(JNIEnv* env, jobject obj, const char* name) { return env->GetIntField(obj, getFieldID(env, obj, name, "I")); }
jlong get_long(JNIEnv* env, jobject obj, const char* name) { return env->GetLongField(obj, getFieldID(env, obj, name, "J")); }
jobject get_object(JNIEnv* env, jobject obj, const char* name) { return env->GetObjectField(obj, getFieldID(env, obj, name, "Ljava/lang/Object;")); }
jobject get_object(JNIEnv* env, jobject obj, const char* name, const char* sig) { return env->GetObjectField(obj, getFieldID(env, obj, name, sig)); }


BasicType basicTypes[] = { T_BOOLEAN, T_BYTE, T_SHORT, T_CHAR, T_INT, T_FLOAT, T_LONG, T_DOUBLE, T_OBJECT };
int basicTypeCount = sizeof(basicTypes) / sizeof(BasicType);

// public HotSpotVMConfig getConfiguration();
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_getConfiguration(JNIEnv *env, jobject) {
  jclass klass = env->FindClass("com/sun/hotspot/c1x/HotSpotVMConfig");
  assert(klass != NULL, "HotSpot vm config class not found");
  jobject config = env->AllocObject(klass);
#ifdef _WIN64
  set_boolean(env, config, "windowsOs", true);
#else
  set_boolean(env, config, "windowsOs", false);
#endif
  set_boolean(env, config, "verifyPointers", VerifyOops);
  set_boolean(env, config, "useFastLocking", UseFastLocking);
  set_int(env, config, "codeEntryAlignment", CodeEntryAlignment);
  set_int(env, config, "vmPageSize", os::vm_page_size());
  set_int(env, config, "stackShadowPages", StackShadowPages);
  set_int(env, config, "hubOffset", oopDesc::klass_offset_in_bytes());
  set_int(env, config, "arrayLengthOffset", arrayOopDesc::length_offset_in_bytes());
  set_int(env, config, "klassStateOffset", instanceKlass::init_state_offset_in_bytes() + sizeof(oopDesc));
  set_int(env, config, "klassStateFullyInitialized", (int)instanceKlass::fully_initialized);
  set_int(env, config, "threadTlabTopOffset", in_bytes(JavaThread::tlab_top_offset()));
  set_int(env, config, "threadTlabEndOffset", in_bytes(JavaThread::tlab_end_offset()));
  set_int(env, config, "instanceHeaderPrototypeOffset", Klass::prototype_header_offset_in_bytes() + klassOopDesc::klass_part_offset_in_bytes());
  set_int(env, config, "threadExceptionOopOffset", in_bytes(JavaThread::exception_oop_offset()));
  set_int(env, config, "threadExceptionPcOffset", in_bytes(JavaThread::exception_pc_offset()));
  set_int(env, config, "threadMultiNewArrayStorage", in_bytes(JavaThread::c1x_multinewarray_storage_offset()));

  set_long(env, config, "debugStub", VmIds::addStub((address)warning));
  set_long(env, config, "instanceofStub", VmIds::addStub(Runtime1::entry_for(Runtime1::slow_subtype_check_id)));
  set_long(env, config, "verifyPointerStub", VmIds::addStub(Runtime1::entry_for(Runtime1::c1x_verify_pointer_id)));
  set_long(env, config, "newInstanceStub", VmIds::addStub(Runtime1::entry_for(Runtime1::fast_new_instance_init_check_id)));
  set_long(env, config, "unresolvedNewInstanceStub", VmIds::addStub(Runtime1::entry_for(Runtime1::new_instance_id)));
  set_long(env, config, "newTypeArrayStub", VmIds::addStub(Runtime1::entry_for(Runtime1::new_type_array_id)));
  set_long(env, config, "newObjectArrayStub", VmIds::addStub(Runtime1::entry_for(Runtime1::new_object_array_id)));
  set_long(env, config, "newMultiArrayStub", VmIds::addStub(Runtime1::entry_for(Runtime1::new_multi_array_id)));
  set_long(env, config, "loadKlassStub", VmIds::addStub(Runtime1::entry_for(Runtime1::load_klass_patching_id)));
  set_long(env, config, "accessFieldStub", VmIds::addStub(Runtime1::entry_for(Runtime1::access_field_patching_id)));
  set_long(env, config, "resolveStaticCallStub", VmIds::addStub(SharedRuntime::get_resolve_static_call_stub()));
  set_long(env, config, "inlineCacheMissStub", VmIds::addStub(SharedRuntime::get_ic_miss_stub()));
  set_long(env, config, "unwindExceptionStub", VmIds::addStub(Runtime1::entry_for(Runtime1::c1x_unwind_exception_call_id)));
  set_long(env, config, "handleExceptionStub", VmIds::addStub(Runtime1::entry_for(Runtime1::handle_exception_nofpu_id)));
  set_long(env, config, "handleDeoptStub", VmIds::addStub(SharedRuntime::deopt_blob()->unpack()));
  set_long(env, config, "throwClassCastException", VmIds::addStub(Runtime1::entry_for(Runtime1::throw_class_cast_exception_id)));
  set_long(env, config, "throwArrayStoreException", VmIds::addStub(Runtime1::entry_for(Runtime1::throw_array_store_exception_id)));
  set_long(env, config, "throwArrayIndexException", VmIds::addStub(Runtime1::entry_for(Runtime1::throw_range_check_failed_id)));
  set_long(env, config, "monitorEnterStub", VmIds::addStub(Runtime1::entry_for(Runtime1::monitorenter_id)));
  set_long(env, config, "monitorExitStub", VmIds::addStub(Runtime1::entry_for(Runtime1::monitorexit_id)));
  set_long(env, config, "fastMonitorEnterStub", VmIds::addStub(Runtime1::entry_for(Runtime1::c1x_monitorenter_id)));
  set_long(env, config, "fastMonitorExitStub", VmIds::addStub(Runtime1::entry_for(Runtime1::c1x_monitorexit_id)));
  set_long(env, config, "safepointPollingAddress", (jlong)(os::get_polling_page() + (SafepointPollOffset % os::vm_page_size())));

  BarrierSet* bs = Universe::heap()->barrier_set();
  switch (bs->kind()) {
    case BarrierSet::CardTableModRef:
    case BarrierSet::CardTableExtension: {
      jlong base = (jlong)((CardTableModRefBS*)bs)->byte_map_base;
      assert(base != 0, "unexpected byte_map_base");
      set_long(env, config, "cardtableStartAddress", base);
      set_int(env, config, "cardtableShift", CardTableModRefBS::card_shift);
      break;
    }
    case BarrierSet::ModRef:
    case BarrierSet::Other:
      set_long(env, config, "cardtableStartAddress", 0);
      set_int(env, config, "cardtableShift", 0);
      // No post barriers
      break;
#ifndef SERIALGC
    case BarrierSet::G1SATBCT:
    case BarrierSet::G1SATBCTLogging:
#endif // SERIALGC
    default:
      ShouldNotReachHere();
    }

  jintArray arrayOffsets = env->NewIntArray(basicTypeCount);
  for (int i=0; i<basicTypeCount; i++) {
    jint offset = arrayOopDesc::base_offset_in_bytes(basicTypes[i]);
    env->SetIntArrayRegion(arrayOffsets, i, 1, &offset);
  }
  set_int_array(env, config, "arrayOffsets", arrayOffsets);
  set_int(env, config, "arrayClassElementOffset", objArrayKlass::element_klass_offset_in_bytes() + sizeof(oopDesc));
  return config;
}

// public void installMethod(HotSpotTargetMethod targetMethod);
JNIEXPORT void JNICALL Java_com_sun_hotspot_c1x_VMEntries_installMethod(JNIEnv *jniEnv, jobject, jobject targetMethod) {
  VM_ENTRY_MARK;
  CodeInstaller installer(JNIHandles::resolve(targetMethod));
}

// public HotSpotProxy installStub(HotSpotTargetMethod targetMethod, String name);
JNIEXPORT jlong JNICALL Java_com_sun_hotspot_c1x_VMEntries_installStub(JNIEnv *jniEnv, jobject, jobject targetMethod) {
  VM_ENTRY_MARK;
  jlong id;
  CodeInstaller installer(JNIHandles::resolve(targetMethod), id);
  return id;
}

// public void recordBailout(String reason);
JNIEXPORT void JNICALL Java_com_sun_hotspot_c1x_VMEntries_recordBailout(JNIEnv *jniEnv, jobject) {
  if (C1XBailoutIsFatal) fatal("Bailout in C1X");
}




#define CC (char*)  /*cast a literal from (const char*)*/
#define FN_PTR(f) CAST_FROM_FN_PTR(void*, &f)

#define PROXY           "J"
#define TYPE            "Lcom/sun/cri/ri/RiType;"
#define RESOLVED_TYPE   "Lcom/sun/hotspot/c1x/HotSpotTypeResolved;"
#define METHOD          "Lcom/sun/cri/ri/RiMethod;"
#define SIGNATURE       "Lcom/sun/cri/ri/RiSignature;"
#define FIELD           "Lcom/sun/cri/ri/RiField;"
#define CONSTANT_POOL   "Lcom/sun/cri/ri/RiConstantPool;"
#define EXCEPTION_HANDLERS "[Lcom/sun/cri/ri/RiExceptionHandler;"
#define TARGET_METHOD   "Lcom/sun/hotspot/c1x/HotSpotTargetMethod;"
#define CONFIG          "Lcom/sun/hotspot/c1x/HotSpotVMConfig;"
#define HS_METHOD       "Lcom/sun/hotspot/c1x/HotSpotMethod;"
#define CI_CONSTANT     "Lcom/sun/cri/ci/CiConstant;"
#define CI_KIND         "Lcom/sun/cri/ci/CiKind;"
#define STRING          "Ljava/lang/String;"
#define OBJECT          "Ljava/lang/Object;"
#define CLASS           "Ljava/lang/Class;"

JNINativeMethod VMEntries_methods[] = {
  {CC"RiMethod_code",                   CC"("PROXY")[B",                            FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1code)},
  {CC"RiMethod_maxStackSize",           CC"("PROXY")I",                             FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1maxStackSize)},
  {CC"RiMethod_maxLocals",              CC"("PROXY")I",                             FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1maxLocals)},
  {CC"RiMethod_holder",                 CC"("PROXY")"TYPE,                          FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1holder)},
  {CC"RiMethod_signature",              CC"("PROXY")"STRING,                        FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1signature)},
  {CC"RiMethod_accessFlags",            CC"("PROXY")I",                             FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1accessFlags)},
  {CC"RiMethod_exceptionHandlers",      CC"("PROXY")"EXCEPTION_HANDLERS,            FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1exceptionHandlers)},
  {CC"RiMethod_hasBalancedMonitors",    CC"("PROXY")Z",                             FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1hasBalancedMonitors)},
  {CC"RiMethod_uniqueConcreteMethod",   CC"("PROXY")"METHOD,                        FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1uniqueConcreteMethod)},
  {CC"RiSignature_lookupType",          CC"("STRING RESOLVED_TYPE")"TYPE,           FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiSignature_1lookupType)},
  {CC"RiConstantPool_lookupConstant",   CC"("PROXY"I)"OBJECT,                       FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupConstant)},
  {CC"RiConstantPool_lookupMethod",     CC"("PROXY"IB)"METHOD,                      FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupMethod)},
  {CC"RiConstantPool_lookupSignature",  CC"("PROXY"I)"SIGNATURE,                    FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupSignature)},
  {CC"RiConstantPool_lookupType",       CC"("PROXY"I)"TYPE,                         FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupType)},
  {CC"RiConstantPool_lookupField",      CC"("PROXY"IB)"FIELD,                       FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupField)},
  {CC"RiType_constantPool",             CC"("RESOLVED_TYPE")"CONSTANT_POOL,         FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiType_1constantPool)},
  {CC"RiType_resolveMethodImpl",        CC"("RESOLVED_TYPE STRING STRING")"METHOD,  FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiType_3resolveMethodImpl)},
  {CC"RiType_isSubtypeOf",              CC"("RESOLVED_TYPE TYPE")Z",                FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiType_2isSubtypeOf)},
  {CC"RiType_componentType",            CC"("RESOLVED_TYPE")"TYPE,                  FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiType_1componentType)},
  {CC"RiType_uniqueConcreteSubtype",    CC"("RESOLVED_TYPE")"TYPE,                  FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiType_1uniqueConcreteSubtype)},
  {CC"RiType_arrayOf",                  CC"("RESOLVED_TYPE")"TYPE,                  FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiType_1arrayOf)},
  {CC"getPrimitiveArrayType",           CC"("CI_KIND")"TYPE,                        FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_getPrimitiveArrayType)},
  {CC"getType",                         CC"("CLASS")"TYPE,                          FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_getType)},
  {CC"getConfiguration",                CC"()"CONFIG,                               FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_getConfiguration)},
  {CC"installMethod",                   CC"("TARGET_METHOD")V",                     FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_installMethod)},
  {CC"installStub",                     CC"("TARGET_METHOD")"PROXY,                 FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_installStub)},
  {CC"recordBailout",                   CC"("STRING")V",                            FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_recordBailout)}
};

int VMEntries_methods_count() {
  return sizeof(VMEntries_methods) / sizeof(JNINativeMethod);
}
