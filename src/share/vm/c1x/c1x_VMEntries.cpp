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


# include "incls/_precompiled.incl"
# include "incls/_c1x_VMEntries.cpp.incl"

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_code
* Signature: (Ljava/lang/Object;)[B
*/
JNIEXPORT jbyteArray JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1code(JNIEnv *env, jclass, jobject method) {
  methodOop m = (methodOop)JNIHandles::resolve(method);
  int code_size = m->code_size();
  jbyteArray result = env->NewByteArray(code_size);
  env->SetByteArrayRegion(result, 0, code_size, (const jbyte *)m->code_base());
  return result;
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_maxStackSize
* Signature: (Ljava/lang/Object;)I
*/
JNIEXPORT jint JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1maxStackSize(JNIEnv *, jclass, jobject method) {
  methodOop m = (methodOop)JNIHandles::resolve(method);
  return m->max_stack();
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_maxLocals
* Signature: (Ljava/lang/Object;)I
*/
JNIEXPORT jint JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1maxLocals(JNIEnv *, jclass, jobject method) {
  methodOop m = (methodOop)JNIHandles::resolve(method);
  return m->max_locals();
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_holder
* Signature: (Ljava/lang/Object;)Lcom/sun/cri/ri/RiType;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1holder(JNIEnv *, jclass, jobject method) {
  VM_ENTRY_MARK
  methodOop m = (methodOop)JNIHandles::resolve(method);
  klassOop k = m->method_holder();
  return JNIHandles::make_local(Thread::current(), C1XCompiler::get_RiType(k));
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_signature
* Signature: (Ljava/lang/Object;)Lcom/sun/cri/ri/RiSignature;
*/
JNIEXPORT jstring JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1signature(JNIEnv *env, jclass, jobject method) {
  methodOop m = (methodOop)JNIHandles::resolve(method);
  return env->NewStringUTF(m->signature()->as_utf8());
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_name
* Signature: (Ljava/lang/Object;)Ljava/lang/String;
*/
JNIEXPORT jstring JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1name(JNIEnv *env, jclass, jobject method) {
  methodOop m = (methodOop)JNIHandles::resolve(method);
  return env->NewStringUTF(m->name()->as_utf8());
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiSignature_lookupType
* Signature: (Ljava/lang/String;Lcom/sun/cri/ri/RiType;)Lcom/sun/cri/ri/RiType;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiSignature_1lookupType(JNIEnv *, jclass, jstring name, jobject accessor) {
  VM_ENTRY_MARK
  klassOop k = (klassOop)JNIHandles::resolve(accessor);
  oop n = JNIHandles::resolve_external_guard(name);
  if (n == NULL) {
    THROW_MSG_(vmSymbols::java_lang_NullPointerException(), "Name must not be null.", NULL);
  }
  char* utf8 = java_lang_String::as_utf8_string(n);
  symbolOop symbol = oopFactory::new_symbol(utf8, java_lang_String::length(n), THREAD);
  oop result = C1XCompiler::get_RiType(symbol, k);
  return JNIHandles::make_local(THREAD, result);
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiSignature_symbolToString
* Signature: (Ljava/lang/Object;)Ljava/lang/String;
*/
JNIEXPORT jstring JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiSignature_1symbolToString(JNIEnv *env, jclass, jobject symbol) {
  symbolOop s = (symbolOop)JNIHandles::resolve(symbol);
  return env->NewStringUTF(s->as_utf8());
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiType_javaClass
* Signature: (Ljava/lang/Object;)Ljava/lang/Class;
*/
JNIEXPORT jclass JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1javaClass(JNIEnv *, jclass, jobject klass) {
  klassOop k = (klassOop)JNIHandles::resolve(klass);
  oop result = k->klass_part()->java_mirror();
  return (jclass) JNIHandles::make_local(Thread::current(), result);
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiType_name
* Signature: (Ljava/lang/Object;)Ljava/lang/String;
*/
JNIEXPORT jstring JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1name(JNIEnv *env, jclass, jobject klass) {
  klassOop k = (klassOop)JNIHandles::resolve(klass);
  return env->NewStringUTF(k->klass_part()->name()->as_utf8());
}


/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiConstantPool_lookupConstant
* Signature: (Ljava/lang/Object;I)Ljava/lang/Object;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupConstant(JNIEnv *env, jclass, jobject cpHandle, jint index) {

  VM_ENTRY_MARK;
  constantPoolOop cp = (constantPoolOop)JNIHandles::resolve(cpHandle);

  oop result = NULL;
  constantTag tag = cp->tag_at(index);
  if (tag.is_int()) {
    result = VMExits::createCiConstantInt(cp->int_at(index));
  } else if (tag.is_long()) {
    result = VMExits::createCiConstantLong(cp->long_at(index));
  } else if (tag.is_float()) {
    result = VMExits::createCiConstantFloat(cp->float_at(index));
  } else if (tag.is_double()) {
    result = VMExits::createCiConstantDouble(cp->double_at(index));
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
    result = VMExits::createCiConstantObject(string);
  } else if (tag.is_klass() || tag.is_unresolved_klass()) {
    
    // TODO: Return RiType object
    ShouldNotReachHere();
    // 4881222: allow ldc to take a class type
    //bool ignore;
    //ciKlass* klass = get_klass_by_index_impl(cpool, index, ignore, accessor);
    //if (HAS_PENDING_EXCEPTION) {
    //  CLEAR_PENDING_EXCEPTION;
    //  record_out_of_memory_failure();
    //  return ciConstant();
    //}
    //assert (klass->is_instance_klass() || klass->is_array_klass(),
    //  "must be an instance or array klass ");
    //return ciConstant(T_OBJECT, klass);
  } else if (tag.is_object()) {
    oop obj = cp->object_at(index);
    assert(obj->is_instance(), "must be an instance");
    result = VMExits::createCiConstantObject(obj);
  } else {
    ShouldNotReachHere();
  }

  return JNIHandles::make_local(THREAD, result);
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiConstantPool_lookupMethod
* Signature: (Ljava/lang/Object;I)Lcom/sun/cri/ri/RiMethod;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupMethod(JNIEnv *env, jclass, jobject cpHandle, jint index, jbyte byteCode) {
  VM_ENTRY_MARK;
  constantPoolOop cp = (constantPoolOop)JNIHandles::resolve(cpHandle);
  Bytecodes::Code bc = (Bytecodes::Code)(((int)byteCode) & 0xFF);
  ciInstanceKlass* loading_klass = (ciInstanceKlass *)CURRENT_ENV->get_object(cp->pool_holder());
  ciMethod *method = CURRENT_ENV->get_method_by_index(cp, index, bc, loading_klass);
  return JNIHandles::make_local(THREAD, C1XCompiler::get_RiMethod(method));
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiConstantPool_lookupSignature
* Signature: (Ljava/lang/Object;I)Lcom/sun/cri/ri/RiSignature;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupSignature(JNIEnv *env, jclass, jobject cpHandle, jint index) {
  fatal("currently unsupported");
  return NULL;
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiConstantPool_lookupType
* Signature: (Ljava/lang/Object;I)Lcom/sun/cri/ri/RiType;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupType(JNIEnv *env, jclass, jobject cpHandle, jint index) {
  VM_ENTRY_MARK;
  constantPoolOop cp = (constantPoolOop)JNIHandles::resolve(cpHandle);
  ciInstanceKlass* loading_klass = (ciInstanceKlass *)CURRENT_ENV->get_object(cp->pool_holder());
  bool is_accessible = false;
  ciKlass *klass = CURRENT_ENV->get_klass_by_index(cp, index, is_accessible, loading_klass);
  return JNIHandles::make_local(THREAD, C1XCompiler::get_RiType(klass));

}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiConstantPool_lookupField
* Signature: (Ljava/lang/Object;I)Lcom/sun/cri/ri/RiField;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupField(JNIEnv *env, jclass, jobject cpHandle, jint index) {
  VM_ENTRY_MARK;
  constantPoolOop cp = (constantPoolOop)JNIHandles::resolve(cpHandle);
  ciInstanceKlass* loading_klass = (ciInstanceKlass *)CURRENT_ENV->get_object(cp->pool_holder());
  ciField *field = CURRENT_ENV->get_field_by_index(loading_klass, index);
  return JNIHandles::make_local(THREAD, C1XCompiler::get_RiField(field));
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    findRiType
* Signature: (Ljava/lang/Object;)Lcom/sun/cri/ri/RiType;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_findRiType(JNIEnv *, jclass, jobject klass) {
  VM_ENTRY_MARK;
  klassOop o = (klassOop)JNIHandles::resolve(klass);
  return JNIHandles::make_local(THREAD, C1XCompiler::get_RiType(o));
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiRuntime_getConstantPool
* Signature: (Ljava/lang/Object;)Lcom/sun/cri/ri/RiConstantPool;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiRuntime_1getConstantPool(JNIEnv *, jclass, jobject klass) {
  VM_ENTRY_MARK;
  klassOop o = (klassOop)JNIHandles::resolve(klass);
  return JNIHandles::make_local(THREAD, C1XCompiler::get_RiConstantPool(((instanceKlass*)o->klass_part())->constants()));
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiType_isArrayClass
* Signature: (Ljava/lang/Object;)Z
*/
JNIEXPORT jboolean JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1isArrayClass(JNIEnv *, jclass, jobject klass) {
  klassOop o = (klassOop)JNIHandles::resolve(klass);
  o->print();
  bool result = o->klass_part()->oop_is_array();
  tty->print_cr("result=%d", (int)result);
  return result;
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiType_isInstanceClass
* Signature: (Ljava/lang/Object;)Z
*/
JNIEXPORT jboolean JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1isInstanceClass(JNIEnv *, jclass, jobject klass) {
  klassOop o = (klassOop)JNIHandles::resolve(klass);
  return o->klass_part()->oop_is_instanceKlass();
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiType_isInterface
* Signature: (Ljava/lang/Object;)Z
*/
JNIEXPORT jboolean JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1isInterface(JNIEnv *, jclass, jobject klass) {
  klassOop o = (klassOop)JNIHandles::resolve(klass);
  return o->klass_part()->is_interface();
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_accessFlags
* Signature: (Ljava/lang/Object;)I
*/
JNIEXPORT jint JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1accessFlags(JNIEnv *, jclass, jobject method) {
  methodOop m = (methodOop)JNIHandles::resolve(method);
  return m->access_flags().as_int();
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    installCode
* Signature: (Ljava/lang/Object;[BI)V
*/
JNIEXPORT void JNICALL Java_com_sun_hotspot_c1x_VMEntries_installCode(JNIEnv *jniEnv, jclass, jobject method, jbyteArray code, jint frameSize) {

  methodOop m = (methodOop)JNIHandles::resolve(method);
  jboolean isCopy = false;
  jbyte *codeBytes = jniEnv->GetByteArrayElements(code, &isCopy);
  // TODO: Check if we need to disallocate?
  int codeSize = jniEnv->GetArrayLength(code);
  VM_ENTRY_MARK;
  ciEnv *env = CURRENT_ENV;

  env->set_oop_recorder(new OopRecorder(env->arena()));
  env->set_debug_info(new DebugInformationRecorder(env->oop_recorder()));
  env->set_dependencies(new Dependencies(env));
  ciMethod *ciMethodObject = (ciMethod *)env->get_object(m);
  CodeOffsets offsets;

  // TODO: This is a hack.. Produce correct entries.
  offsets.set_value(CodeOffsets::Exceptions, 0);
  offsets.set_value(CodeOffsets::Deopt, 0);

  CodeBuffer buffer((address)codeBytes, codeSize);
  buffer.print();
  CodeSection *inst_section = buffer.insts();
  inst_section->set_end(inst_section->start() + codeSize);
  buffer.initialize_oop_recorder(env->oop_recorder());
  OopMapSet oop_map_set;
  ExceptionHandlerTable handler_table;
  ImplicitExceptionTable inc_table;
  {
    ThreadToNativeFromVM t((JavaThread*)THREAD);
    env->register_method(ciMethodObject, -1, &offsets, 0, &buffer, frameSize, &oop_map_set, &handler_table, &inc_table, NULL, env->comp_level(), false, false);

  }
}

// helpers used to set fields in the HotSpotVMConfig object
#define SET_CONFIG_BOOLEAN(name, value) { jfieldID id = jniEnv->GetFieldID(klass, #name, "Z"); jniEnv->SetBooleanField(config, id, value); }
#define SET_CONFIG_INT(name, value) { jfieldID id = jniEnv->GetFieldID(klass, #name, "I"); jniEnv->SetIntField(config, id, value); }

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    getConfiguration
* Signature: ()Lcom/sun/hotspot/c1x/HotSpotVMConfig;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_getConfiguration(JNIEnv *jniEnv, jclass) {
  jclass klass = jniEnv->FindClass("com/sun/hotspot/c1x/HotSpotVMConfig");
  assert(klass != NULL, "HotSpot vm config class not found");
  jobject config = jniEnv->AllocObject(klass);
  jfieldID id = jniEnv->GetFieldID(klass, "windowsOs", "Z");
#ifdef _WIN64
  SET_CONFIG_BOOLEAN(windowsOs, true)
#else
  SET_CONFIG_BOOLEAN(windowsOs, false)
#endif
  return config;
}


JNINativeMethod VMEntries_methods[] = {
  {CC"RiMethod_code",                   CC"(Ljava/lang/Object;)[B",                                                 FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1code)},
  {CC"RiMethod_maxStackSize",           CC"(Ljava/lang/Object;)I",                                                  FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1maxStackSize)},
  {CC"RiMethod_maxLocals",              CC"(Ljava/lang/Object;)I",                                                  FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1maxLocals)},
  {CC"RiMethod_holder",                 CC"(Ljava/lang/Object;)Lcom/sun/cri/ri/RiType;",                            FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1holder)},
  {CC"RiMethod_signature",              CC"(Ljava/lang/Object;)Ljava/lang/String;",                                 FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1signature)},
  {CC"RiMethod_name",                   CC"(Ljava/lang/Object;)Ljava/lang/String;",                                 FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1name)},
  {CC"RiSignature_lookupType",          CC"(Ljava/lang/String;Ljava/lang/Object;)Lcom/sun/cri/ri/RiType;",          FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiSignature_1lookupType)},
  {CC"RiType_javaClass",                CC"(Ljava/lang/Object;)Ljava/lang/Class;",                                  FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiType_1javaClass)},
  {CC"RiType_name",                     CC"(Ljava/lang/Object;)Ljava/lang/String;",                                 FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiType_1name)},
  {CC"RiConstantPool_lookupConstant",   CC"(Ljava/lang/Object;I)Ljava/lang/Object;",                                FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupConstant)},
  {CC"RiConstantPool_lookupMethod",     CC"(Ljava/lang/Object;IB)Lcom/sun/cri/ri/RiMethod;",                        FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupMethod)},
  {CC"RiConstantPool_lookupSignature",  CC"(Ljava/lang/Object;I)Lcom/sun/cri/ri/RiSignature;",                      FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupSignature)},
  {CC"RiConstantPool_lookupType",       CC"(Ljava/lang/Object;I)Lcom/sun/cri/ri/RiType;",                           FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupType)},
  {CC"RiConstantPool_lookupField",      CC"(Ljava/lang/Object;I)Lcom/sun/cri/ri/RiField;",                          FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupField)},
  {CC"RiRuntime_getConstantPool",       CC"(Ljava/lang/Object;)Lcom/sun/cri/ri/RiConstantPool;",                    FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiRuntime_1getConstantPool)},
  {CC"RiType_isArrayClass",             CC"(Ljava/lang/Object;)Z",                                                  FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiType_1isArrayClass)},
  {CC"RiType_isInstanceClass",          CC"(Ljava/lang/Object;)Z",                                                  FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiType_1isInstanceClass)},
  {CC"RiType_isInterface",              CC"(Ljava/lang/Object;)Z",                                                  FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiType_1isInterface)},
  {CC"RiMethod_accessFlags",            CC"(Ljava/lang/Object;)I",                                                  FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1accessFlags)},
  {CC"installCode",                     CC"(Ljava/lang/Object;[BI)V",                                               FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_installCode)},
  {CC"getConfiguration",                CC"()Lcom/sun/hotspot/c1x/HotSpotVMConfig;",                                FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_getConfiguration)}
};

int VMEntries_methods_count() {
  return sizeof(VMEntries_methods) / sizeof(JNINativeMethod);
}