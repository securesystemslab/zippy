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




// public byte[] RiMethod_code(long vmId);
JNIEXPORT jbyteArray JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1code(JNIEnv *env, jobject, jlong vmId) {
  methodOop method = VmIds::get<methodOop>(vmId);
  int code_size = method->code_size();
  jbyteArray result = env->NewByteArray(code_size);
  env->SetByteArrayRegion(result, 0, code_size, (const jbyte *)method->code_base());
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
  klassOop klass = VmIds::get<methodOop>(vmId)->method_holder();
  jlong klassVmId = VmIds::add<klassOop>(klass);
  Handle name = VmIds::toString<Handle>(klass->klass_part()->name(), CHECK_NULL);
  oop holder = VMExits::createRiType(klassVmId, name, THREAD);
  return JNIHandles::make_local(THREAD, holder);
}

// public String RiMethod_signature(long vmId);
JNIEXPORT jstring JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1signature(JNIEnv *env, jobject, jlong vmId) {
  VM_ENTRY_MARK
  methodOop method = VmIds::get<methodOop>(vmId);
  return VmIds::toString<jstring>(method->signature(), THREAD);
}

// public int RiMethod_accessFlags(long vmId);
JNIEXPORT jint JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1accessFlags(JNIEnv *, jobject, jlong vmId) {
  return VmIds::get<methodOop>(vmId)->access_flags().as_int();
}

// public RiType RiSignature_lookupType(String returnType, long accessingClassVmId);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiSignature_1lookupType(JNIEnv *, jobject, jstring jname, jlong accessingClassVmId) {
  VM_ENTRY_MARK;

  symbolOop nameSymbol = VmIds::toSymbol(jname);
  Handle name = JNIHandles::resolve(jname);

  oop result;
  if (nameSymbol == vmSymbols::int_signature()) {
    result = VMExits::createRiTypePrimitive((int)T_INT, THREAD);
  } else if (nameSymbol == vmSymbols::long_signature()) {
    result = VMExits::createRiTypePrimitive((int)T_LONG, THREAD);
  } else if (nameSymbol == vmSymbols::bool_signature()) {
    result = VMExits::createRiTypePrimitive((int)T_BOOLEAN, THREAD);
  } else if (nameSymbol == vmSymbols::char_signature()) {
    result = VMExits::createRiTypePrimitive((int)T_CHAR, THREAD);
  } else if (nameSymbol == vmSymbols::short_signature()) {
    result = VMExits::createRiTypePrimitive((int)T_SHORT, THREAD);
  } else if (nameSymbol == vmSymbols::byte_signature()) {
    result = VMExits::createRiTypePrimitive((int)T_BYTE, THREAD);
  } else if (nameSymbol == vmSymbols::double_signature()) {
    result = VMExits::createRiTypePrimitive((int)T_DOUBLE, THREAD);
  } else if (nameSymbol == vmSymbols::float_signature()) {
    result = VMExits::createRiTypePrimitive((int)T_FLOAT, THREAD);
  } else {
    Handle classloader;
    Handle protectionDomain;
    if (accessingClassVmId != 0) {
      classloader = VmIds::get<klassOop>(accessingClassVmId)->klass_part()->class_loader();
      protectionDomain = VmIds::get<klassOop>(accessingClassVmId)->klass_part()->protection_domain();
    }
    klassOop resolved_type = SystemDictionary::resolve_or_null(nameSymbol, classloader, protectionDomain, THREAD);
    if (resolved_type != NULL) {
      result = VMExits::createRiType(VmIds::add<klassOop>(resolved_type), name, THREAD);
    } else {
      result = VMExits::createRiTypeUnresolved(name, accessingClassVmId, THREAD);
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
    result = VMExits::createCiConstantInt(cp->int_at(index), CHECK_0);
  } else if (tag.is_long()) {
    result = VMExits::createCiConstantLong(cp->long_at(index), CHECK_0);
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
    result = VMExits::createCiConstantObject(VmIds::add<oop>(string), CHECK_0);
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
    result = VMExits::createCiConstantObject(VmIds::add<oop>(obj), CHECK_0);
  } else {
    ShouldNotReachHere();
  }

  return JNIHandles::make_local(THREAD, result);
}

// public RiMethod RiConstantPool_lookupMethod(long vmId, int cpi, byte byteCode);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupMethod(JNIEnv *env, jobject, jlong vmId, jint index, jbyte byteCode) {
  VM_ENTRY_MARK;

  constantPoolOop cp = VmIds::get<constantPoolOop>(vmId);

  Bytecodes::Code bc = (Bytecodes::Code)(((int)byteCode) & 0xFF);
  ciInstanceKlass* loading_klass = (ciInstanceKlass *)CURRENT_ENV->get_object(cp->pool_holder());
  ciMethod *cimethod = CURRENT_ENV->get_method_by_index(cp, index, bc, loading_klass);
  methodOop method = (methodOop)cimethod->get_oop();
  Handle name = VmIds::toString<Handle>(method->name(), CHECK_NULL);
  return JNIHandles::make_local(THREAD, VMExits::createRiMethod(VmIds::add<methodOop>(method), name, THREAD));
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

  ciInstanceKlass* loading_klass = (ciInstanceKlass *)CURRENT_ENV->get_object(cp->pool_holder());
  bool is_accessible = false;
  ciKlass *klass = CURRENT_ENV->get_klass_by_index(cp, index, is_accessible, loading_klass);
  return JNIHandles::make_local(THREAD, C1XCompiler::get_RiType(klass, cp->klass(), THREAD));

}

// public RiField RiConstantPool_lookupField(long vmId, int cpi);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupField(JNIEnv *env, jobject, jlong vmId, jint index) {
  VM_ENTRY_MARK;

  constantPoolOop cp = VmIds::get<constantPoolOop>(vmId);

  ciInstanceKlass* loading_klass = (ciInstanceKlass *)CURRENT_ENV->get_object(cp->pool_holder());
  ciField *field = CURRENT_ENV->get_field_by_index(loading_klass, index);
  return JNIHandles::make_local(THREAD, C1XCompiler::get_RiField(field, THREAD));
}

// public RiConstantPool RiType_constantPool(long vmId);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1constantPool(JNIEnv *, jobject, jlong vmId) {
  VM_ENTRY_MARK;

  constantPoolOop constantPool = instanceKlass::cast(VmIds::get<klassOop>(vmId))->constants();
  return JNIHandles::make_local(VMExits::createRiConstantPool(VmIds::add<constantPoolOop>(constantPool), THREAD));
}

// public boolean RiType_isArrayClass(long vmId);
JNIEXPORT jboolean JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1isArrayClass(JNIEnv *, jobject, jlong vmId) {
  return VmIds::get<klassOop>(vmId)->klass_part()->oop_is_javaArray();
}

// public boolean RiType_isInstanceClass(long vmId);
JNIEXPORT jboolean JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1isInstanceClass(JNIEnv *, jobject, jlong vmId) {
  return VmIds::get<klassOop>(vmId)->klass_part()->oop_is_instance();
}

// public boolean RiType_isInterface(long vmId);
JNIEXPORT jboolean JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1isInterface(JNIEnv *, jobject, jlong vmId) {
  return VmIds::get<klassOop>(vmId)->klass_part()->is_interface();
}

// public long RiType_instanceSize(long vmId);
JNIEXPORT jlong JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1instanceSize(JNIEnv *, jobject, jlong vmId) {
  return align_object_size(instanceKlass::cast(VmIds::get<klassOop>(vmId))->size_helper() * HeapWordSize);
}


// helpers used to set fields in the HotSpotVMConfig object
jfieldID getFieldID(JNIEnv* env, jobject obj, const char* name, const char* sig) {
  jfieldID id = env->GetFieldID(env->GetObjectClass(obj), name, sig);
  if (id == NULL) {
    tty->print_cr("field not found: %s (%s)", name, sig);
    assert(id != NULL, "field not found");
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
  tty->print_cr("Java_com_sun_hotspot_c1x_VMEntries_getConfiguration");
  jclass klass = env->FindClass("com/sun/hotspot/c1x/HotSpotVMConfig");
  assert(klass != NULL, "HotSpot vm config class not found");
  jobject config = env->AllocObject(klass);
#ifdef _WIN64
  set_boolean(env, config, "windowsOs", true);
#else
  set_boolean(env, config, "windowsOs", false);
#endif
  set_int(env, config, "codeEntryAlignment", CodeEntryAlignment);
  set_int(env, config, "vmPageSize", os::vm_page_size());
  set_int(env, config, "stackShadowPages", StackShadowPages);
  set_int(env, config, "hubOffset", oopDesc::klass_offset_in_bytes());
  set_int(env, config, "arrayLengthOffset", arrayOopDesc::length_offset_in_bytes());
  set_int(env, config, "threadTlabTopOffset", in_bytes(JavaThread::tlab_top_offset()));
  set_int(env, config, "threadTlabEndOffset", in_bytes(JavaThread::tlab_end_offset()));
  set_int(env, config, "instanceHeaderPrototypeOffset", Klass::prototype_header_offset_in_bytes() + klassOopDesc::klass_part_offset_in_bytes());

  set_long(env, config, "instanceofStub", VmIds::addStub(Runtime1::entry_for(Runtime1::slow_subtype_check_id)));
  set_long(env, config, "debugStub", VmIds::addStub((address)warning));
  set_long(env, config, "resolveStaticCallStub", VmIds::addStub(SharedRuntime::get_resolve_static_call_stub()));
  set_long(env, config, "newInstanceStub", VmIds::addStub(Runtime1::entry_for(Runtime1::fast_new_instance_id)));
  set_long(env, config, "throwImplicitNullStub", VmIds::addStub(Runtime1::entry_for(Runtime1::throw_null_pointer_exception_id)));

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
  CodeInstaller installer(JNIHandles::resolve(targetMethod));
}

// public HotSpotProxy installStub(HotSpotTargetMethod targetMethod, String name);
JNIEXPORT jlong JNICALL Java_com_sun_hotspot_c1x_VMEntries_installStub(JNIEnv *jniEnv, jobject, jobject targetMethod) {
  jlong id;
  CodeInstaller installer(JNIHandles::resolve(targetMethod), id);
  return id;
}




#define CC (char*)  /*cast a literal from (const char*)*/
#define FN_PTR(f) CAST_FROM_FN_PTR(void*, &f)

#define PROXY           "J"
#define TYPE            "Lcom/sun/cri/ri/RiType;"
#define METHOD          "Lcom/sun/cri/ri/RiMethod;"
#define SIGNATURE       "Lcom/sun/cri/ri/RiSignature;"
#define FIELD           "Lcom/sun/cri/ri/RiField;"
#define CONSTANT_POOL   "Lcom/sun/cri/ri/RiConstantPool;"
#define TARGET_METHOD   "Lcom/sun/hotspot/c1x/HotSpotTargetMethod;"
#define CONFIG          "Lcom/sun/hotspot/c1x/HotSpotVMConfig;"
#define HS_METHOD       "Lcom/sun/hotspot/c1x/HotSpotMethod;"
#define CI_CONSTANT     "Lcom/sun/cri/ci/CiConstant;"
#define STRING          "Ljava/lang/String;"
#define OBJECT          "Ljava/lang/Object;"

JNINativeMethod VMEntries_methods[] = {
  {CC"RiMethod_code",                   CC"("PROXY")[B",                    FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1code)},
  {CC"RiMethod_maxStackSize",           CC"("PROXY")I",                     FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1maxStackSize)},
  {CC"RiMethod_maxLocals",              CC"("PROXY")I",                     FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1maxLocals)},
  {CC"RiMethod_holder",                 CC"("PROXY")"TYPE,                  FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1holder)},
  {CC"RiMethod_signature",              CC"("PROXY")"STRING,                FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1signature)},
  {CC"RiMethod_accessFlags",            CC"("PROXY")I",                     FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1accessFlags)},
  {CC"RiSignature_lookupType",          CC"("STRING PROXY")"TYPE,           FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiSignature_1lookupType)},
  {CC"RiConstantPool_lookupConstant",   CC"("PROXY"I)"CI_CONSTANT,          FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupConstant)},
  {CC"RiConstantPool_lookupMethod",     CC"("PROXY"IB)"METHOD,              FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupMethod)},
  {CC"RiConstantPool_lookupSignature",  CC"("PROXY"I)"SIGNATURE,            FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupSignature)},
  {CC"RiConstantPool_lookupType",       CC"("PROXY"I)"TYPE,                 FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupType)},
  {CC"RiConstantPool_lookupField",      CC"("PROXY"I)"FIELD,                FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupField)},
  {CC"RiType_constantPool",             CC"("PROXY")"CONSTANT_POOL,         FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiType_1constantPool)},
  {CC"RiType_isArrayClass",             CC"("PROXY")Z",                     FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiType_1isArrayClass)},
  {CC"RiType_isInstanceClass",          CC"("PROXY")Z",                     FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiType_1isInstanceClass)},
  {CC"RiType_isInterface",              CC"("PROXY")Z",                     FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiType_1isInterface)},
  {CC"RiType_instanceSize",             CC"("PROXY")J",                     FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiType_1instanceSize)},
  {CC"getConfiguration",                CC"()"CONFIG,                       FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_getConfiguration)},
  {CC"installMethod",                   CC"("TARGET_METHOD")V",             FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_installMethod)},
  {CC"installStub",                     CC"("TARGET_METHOD")"PROXY,         FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_installStub)}
};

int VMEntries_methods_count() {
  return sizeof(VMEntries_methods) / sizeof(JNINativeMethod);
}
