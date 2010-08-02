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
* Signature: (Ljava/lang/reflect/Method;)[B
*/
JNIEXPORT jbyteArray JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1code(JNIEnv *env, jobject, jobject method) {
  methodOop m = C1XObjects::getInternalMethod(method);
  int code_size = m->code_size();
  jbyteArray result = env->NewByteArray(code_size);
  env->SetByteArrayRegion(result, 0, code_size, (const jbyte *)m->code_base());
  return result;
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_maxStackSize
* Signature: (Ljava/lang/reflect/Method;)I
*/
JNIEXPORT jint JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1maxStackSize(JNIEnv *, jobject, jobject method) {
  return C1XObjects::getInternalMethod(method)->max_stack();
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_maxLocals
* Signature: (Ljava/lang/reflect/Method;)I
*/
JNIEXPORT jint JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1maxLocals(JNIEnv *, jobject, jobject method) {
  return C1XObjects::getInternalMethod(method)->max_locals();
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_holder
* Signature: (Ljava/lang/reflect/Method;)Lcom/sun/cri/ri/RiType;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1holder(JNIEnv *, jobject, jobject method) {
  VM_ENTRY_MARK
  oop holder = VMExits::createRiType(C1XObjects::getInternalMethod(method)->method_holder(), THREAD);
  return JNIHandles::make_local(THREAD, holder);
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_signature
* Signature: (Ljava/lang/reflect/Method;)Lcom/sun/cri/ri/RiSignature;
*/
JNIEXPORT jstring JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1signature(JNIEnv *env, jobject, jobject method) {
  VM_ENTRY_MARK
  return (jstring)JNIHandles::make_local(java_lang_String::create_from_symbol(C1XObjects::getInternalMethod(method)->signature(), Thread::current())());
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_name
* Signature: (Ljava/lang/reflect/Method;)Ljava/lang/String;
*/
JNIEXPORT jstring JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1name(JNIEnv *env, jobject, jobject method) {
  VM_ENTRY_MARK
  return (jstring)JNIHandles::make_local(java_lang_String::create_from_symbol(C1XObjects::getInternalMethod(method)->name(), Thread::current())());
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_accessFlags
* Signature: (Ljava/lang/reflect/Method;)I
*/
JNIEXPORT jint JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1accessFlags(JNIEnv *, jobject, jobject method) {
  return C1XObjects::getInternalMethod(method)->access_flags().as_int();
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiSignature_lookupType
* Signature: (Ljava/lang/String;Lcom/sun/cri/ri/RiType;)Lcom/sun/cri/ri/RiType;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiSignature_1lookupType(JNIEnv *, jobject, jstring name, jobject accessor) {
  VM_ENTRY_MARK
  klassOop k = C1XObjects::getInternalClass(accessor);
  oop n = JNIHandles::resolve(name);
  if (n == NULL) {
    THROW_MSG_(vmSymbols::java_lang_NullPointerException(), "Name must not be null.", NULL);
  }
  oop result = C1XCompiler::get_RiType(n, k, THREAD);
  return JNIHandles::make_local(THREAD, result);
}


/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiConstantPool_lookupConstant
* Signature: (Ljava/lang/Class;I)Ljava/lang/Object;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupConstant(JNIEnv *env, jobject, jobject cpHandle, jint index) {
  VM_ENTRY_MARK;
  klassOop klass = C1XObjects::getInternalClass(cpHandle);
  constantPoolOop cp = instanceKlass::cast(klass)->constants();

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
    result = VMExits::createCiConstantObject(string, CHECK_0);
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
    result = VMExits::createCiConstantObject(obj, CHECK_0);
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
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupMethod(JNIEnv *env, jobject, jobject cpHandle, jint index, jbyte byteCode) {
  VM_ENTRY_MARK;
  klassOop klass = C1XObjects::getInternalClass(cpHandle);
  constantPoolOop cp = instanceKlass::cast(klass)->constants();
//  assert(cp->tag_at(cp->cache() != NULL ? cp->cache()->entry_at(index)->constant_pool_index() : index).is_method(), "reading field from non-field cp index");
  Bytecodes::Code bc = (Bytecodes::Code)(((int)byteCode) & 0xFF);
  ciInstanceKlass* loading_klass = (ciInstanceKlass *)CURRENT_ENV->get_object(cp->pool_holder());
  ciMethod *method = CURRENT_ENV->get_method_by_index(cp, index, bc, loading_klass);
  return JNIHandles::make_local(THREAD, VMExits::createRiMethod((methodOop)method->get_oop(), THREAD));
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiConstantPool_lookupSignature
* Signature: (Ljava/lang/Object;I)Lcom/sun/cri/ri/RiSignature;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupSignature(JNIEnv *env, jobject, jobject cpHandle, jint index) {
  fatal("currently unsupported");
  return NULL;
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiConstantPool_lookupType
* Signature: (Ljava/lang/Object;I)Lcom/sun/cri/ri/RiType;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupType(JNIEnv *env, jobject, jobject cpHandle, jint index) {
  VM_ENTRY_MARK;
  klassOop cpKlass = C1XObjects::getInternalClass(cpHandle);
  constantPoolOop cp = instanceKlass::cast(cpKlass)->constants();
  ciInstanceKlass* loading_klass = (ciInstanceKlass *)CURRENT_ENV->get_object(cpKlass);
  bool is_accessible = false;
  ciKlass *klass = CURRENT_ENV->get_klass_by_index(cp, index, is_accessible, loading_klass);
  return JNIHandles::make_local(THREAD, C1XCompiler::get_RiType(klass, cpKlass, THREAD));

}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiConstantPool_lookupField
* Signature: (Ljava/lang/Object;I)Lcom/sun/cri/ri/RiField;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupField(JNIEnv *env, jobject, jobject cpHandle, jint index) {
  VM_ENTRY_MARK;
  klassOop klass = C1XObjects::getInternalClass(cpHandle);
  constantPoolOop cp = instanceKlass::cast(klass)->constants();
//  assert(cp->tag_at(cp->cache() != NULL ? cp->cache()->entry_at(index)->constant_pool_index() : index).is_field(), "reading field from non-field cp index");
  ciInstanceKlass* loading_klass = (ciInstanceKlass *)CURRENT_ENV->get_object(cp->pool_holder());
  ciField *field = CURRENT_ENV->get_field_by_index(loading_klass, index);
  return JNIHandles::make_local(THREAD, C1XCompiler::get_RiField(field, THREAD));
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiType_name
* Signature: (Ljava/lang/Class;)Ljava/lang/String;
*/
JNIEXPORT jstring JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1name(JNIEnv *env, jobject, jobject klass) {
  VM_ENTRY_MARK
  klassOop k = C1XObjects::getInternalClass(klass);
  return (jstring)JNIHandles::make_local(java_lang_String::create_from_symbol(k->klass_part()->name(), Thread::current())());
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiType_isArrayClass
* Signature: (Ljava/lang/Class;)Z
*/
JNIEXPORT jboolean JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1isArrayClass(JNIEnv *, jobject, jobject klass) {
  klassOop o = C1XObjects::getInternalClass(klass);
  return o->klass_part()->oop_is_array();
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiType_isInstanceClass
* Signature: (Ljava/lang/Class;)Z
*/
JNIEXPORT jboolean JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1isInstanceClass(JNIEnv *, jobject, jobject klass) {
  klassOop o = C1XObjects::getInternalClass(klass);
  return o->klass_part()->oop_is_instanceKlass();
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiType_isInterface
* Signature: (Ljava/lang/Class;)Z
*/
JNIEXPORT jboolean JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1isInterface(JNIEnv *, jobject, jobject klass) {
  klassOop o = C1XObjects::getInternalClass(klass);
  return o->klass_part()->is_interface();
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

jboolean get_boolean(JNIEnv* env, jobject obj, const char* name) { return env->GetBooleanField(obj, getFieldID(env, obj, name, "Z")); }
jint get_int(JNIEnv* env, jobject obj, const char* name) { return env->GetIntField(obj, getFieldID(env, obj, name, "I")); }
jobject get_object(JNIEnv* env, jobject obj, const char* name) { return env->GetObjectField(obj, getFieldID(env, obj, name, "Ljava/lang/Object;")); }
jobject get_object(JNIEnv* env, jobject obj, const char* name, const char* sig) { return env->GetObjectField(obj, getFieldID(env, obj, name, sig)); }


// Helpful routine for computing field offsets at run time rather than hardcoding them
int compute_offset(klassOop klass_oop, const char* name, const char* sig) {
  JavaThread* THREAD = JavaThread::current();
  fieldDescriptor fd;
  instanceKlass* ik = instanceKlass::cast(klass_oop);
  symbolOop name_symbol = SymbolTable::probe(name, (int)strlen(name));
  symbolOop signature_symbol   = SymbolTable::probe(sig, (int)strlen(sig));
  if (name_symbol == NULL || signature_symbol == NULL || !ik->find_local_field(name_symbol, signature_symbol, &fd)) {
    ResourceMark rm;
    tty->print_cr("Invalid layout of %s at %s", ik->external_name(), name_symbol->as_C_string());
    fatal("Invalid layout of c1x4hotspot class");
  }
  return fd.offset();
}


class TypeHelper {
public:
  jclass        jniHotSpotType;
  jclass        jniHotSpotTargetMethod;
  klassOop      HotSpotType;
  klassOop      HotSpotTargetMethod;
  int           HotSpotType_klassOop;

  TypeHelper(JNIEnv* jniEnv) {
    jniHotSpotType = jniEnv->FindClass("com/sun/hotspot/c1x/HotSpotType");
    jniHotSpotTargetMethod = jniEnv->FindClass("com/sun/hotspot/c1x/HotSpotTargetMethod");
  }

  void initialize() {
    HotSpotType = java_lang_Class::as_klassOop(JNIHandles::resolve(jniHotSpotType));
    HotSpotTargetMethod = java_lang_Class::as_klassOop(JNIHandles::resolve(jniHotSpotTargetMethod));
    HotSpotType_klassOop = compute_offset(HotSpotType, "klass", "Ljava/lang/Class;");
  }
};




/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    installCode
* Signature: (Lcom/sun/hotspot/c1x/HotSpotTargetMethod;)V
*/
JNIEXPORT void JNICALL Java_com_sun_hotspot_c1x_VMEntries_installCode(JNIEnv *jniEnv, jobject, jobject targetMethod) {
  TypeHelper types(jniEnv);

  methodOop m = C1XObjects::getInternalMethod(get_object(jniEnv, targetMethod, "method", "Ljava/lang/reflect/Method;"));
  jbyteArray code = (jbyteArray)get_object(jniEnv, targetMethod, "code", "[B");
  jint codeSize = get_int(jniEnv, targetMethod, "codeSize");
  jintArray relocationOffsetsObj = (jintArray)get_object(jniEnv, targetMethod, "relocationOffsets", "[I");
  jobjectArray relocationDataObj = (jobjectArray)get_object(jniEnv, targetMethod, "relocationData", "[Ljava/lang/Object;");
  jint frameSize = get_int(jniEnv, targetMethod, "frameSize");

  assert(codeSize > 0 && codeSize <= ((arrayOop)JNIHandles::resolve(code))->length(), "invalid codeSize");

  CodeOffsets offsets;

  // TODO: This is a hack.. Produce correct entries.
  offsets.set_value(CodeOffsets::Exceptions, 0);
  offsets.set_value(CodeOffsets::Deopt, 0);

  offsets.set_value(CodeOffsets::Entry, get_int(jniEnv, targetMethod, "unverifiedEntrypoint"));
  offsets.set_value(CodeOffsets::Verified_Entry, get_int(jniEnv, targetMethod, "verifiedEntrypoint"));

  VM_ENTRY_MARK;
  ciEnv *env = CURRENT_ENV;

  types.initialize();

  env->set_oop_recorder(new OopRecorder(env->arena()));
  env->set_debug_info(new DebugInformationRecorder(env->oop_recorder()));
  env->set_dependencies(new Dependencies(env));
  ciMethod *ciMethodObject = (ciMethod *)env->get_object(m);

  int relocationCount = relocationOffsetsObj == NULL ? 0 : ((arrayOop)JNIHandles::resolve(relocationOffsetsObj))->length();

  CodeBuffer buffer("c1x nmethod", codeSize, relocationCount * relocInfo::length_limit);
  buffer.initialize_oop_recorder(env->oop_recorder());

  // copy the code into the newly created CodeBuffer
  CodeSection* instructions = buffer.insts();
  memcpy(instructions->start(), ((arrayOop)JNIHandles::resolve(code))->base(T_BYTE), codeSize);
  instructions->set_end(instructions->start() + codeSize);

  if (relocationCount > 0) {
    jint* relocationOffsets = (jint*)((arrayOop)JNIHandles::resolve(relocationOffsetsObj))->base(T_INT);
    oop* relocationObjects = (oop*)((arrayOop)JNIHandles::resolve(relocationDataObj))->base(T_OBJECT);

    for (int i=0; i<relocationCount; i++) {
      address inst = (address)instructions->start() + relocationOffsets[i];
      u_char inst_byte = *inst;
      oop obj = relocationObjects[i];
      assert(obj != NULL, "NULL oop needn't be patched");

      if (java_lang_boxing_object::is_instance(obj, T_LONG)) {
          address operand = Assembler::locate_operand(inst, Assembler::call32_operand);
          long dest = obj->long_field(java_lang_boxing_object::value_offset_in_bytes(T_LONG));
          long disp = dest - (long)(operand + 4);
          assert(disp == (int) disp, "disp doesn't fit in 32 bits");
          *((int*)operand) = (int)disp;

          instructions->relocate(inst, runtime_call_Relocation::spec(), Assembler::call32_operand);
          tty->print_cr("relocating (Long) %02x at %016x/%016x", inst_byte, inst, operand);
      } else if (obj->is_a(types.HotSpotType)) {
          address operand = Assembler::locate_operand(inst, Assembler::imm_operand);

          *((jobject*)operand) = JNIHandles::make_local(C1XObjects::getInternalClass(obj->obj_field(types.HotSpotType_klassOop)));
          instructions->relocate(inst, oop_Relocation::spec_for_immediate(), Assembler::imm_operand);
          tty->print_cr("relocating (HotSpotType) %02x at %016x/%016x", inst_byte, inst, operand);
      } else {
          tty->print_cr("unknown relocation type");
      }
    }
  }

  buffer.print();

  address entry = instructions->start() + offsets.value(CodeOffsets::Verified_Entry);


  OopMapSet oop_map_set;
  ExceptionHandlerTable handler_table;
  ImplicitExceptionTable inc_table;
  {
    ThreadToNativeFromVM t((JavaThread*)THREAD);
    env->register_method(ciMethodObject, -1, &offsets, 0, &buffer, frameSize, &oop_map_set, &handler_table, &inc_table, NULL, env->comp_level(), false, false);

  }
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    getConfiguration
* Signature: ()Lcom/sun/hotspot/c1x/HotSpotVMConfig;
*/
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
  set_long(env, config, "instanceofStub", (long)Runtime1::entry_for(Runtime1::slow_subtype_check_id));
  set_long(env, config, "debugStub", (long)warning);
  return config;
}


JNINativeMethod VMEntries_methods[] = {
  {CC"RiMethod_code",                   CC"(Ljava/lang/reflect/Method;)[B",                                         FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1code)},
  {CC"RiMethod_maxStackSize",           CC"(Ljava/lang/reflect/Method;)I",                                          FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1maxStackSize)},
  {CC"RiMethod_maxLocals",              CC"(Ljava/lang/reflect/Method;)I",                                          FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1maxLocals)},
  {CC"RiMethod_holder",                 CC"(Ljava/lang/reflect/Method;)Lcom/sun/cri/ri/RiType;",                    FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1holder)},
  {CC"RiMethod_signature",              CC"(Ljava/lang/reflect/Method;)Ljava/lang/String;",                         FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1signature)},
  {CC"RiMethod_name",                   CC"(Ljava/lang/reflect/Method;)Ljava/lang/String;",                         FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1name)},
  {CC"RiMethod_accessFlags",            CC"(Ljava/lang/reflect/Method;)I",                                          FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1accessFlags)},
  {CC"RiSignature_lookupType",          CC"(Ljava/lang/String;Ljava/lang/Class;)Lcom/sun/cri/ri/RiType;",           FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiSignature_1lookupType)},
  {CC"RiType_name",                     CC"(Ljava/lang/Class;)Ljava/lang/String;",                                  FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiType_1name)},
  {CC"RiConstantPool_lookupConstant",   CC"(Ljava/lang/Class;I)Ljava/lang/Object;",                                 FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupConstant)},
  {CC"RiConstantPool_lookupMethod",     CC"(Ljava/lang/Class;IB)Lcom/sun/cri/ri/RiMethod;",                         FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupMethod)},
  {CC"RiConstantPool_lookupSignature",  CC"(Ljava/lang/Class;I)Lcom/sun/cri/ri/RiSignature;",                       FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupSignature)},
  {CC"RiConstantPool_lookupType",       CC"(Ljava/lang/Class;I)Lcom/sun/cri/ri/RiType;",                            FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupType)},
  {CC"RiConstantPool_lookupField",      CC"(Ljava/lang/Class;I)Lcom/sun/cri/ri/RiField;",                           FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupField)},
  {CC"RiType_isArrayClass",             CC"(Ljava/lang/Class;)Z",                                                   FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiType_1isArrayClass)},
  {CC"RiType_isInstanceClass",          CC"(Ljava/lang/Class;)Z",                                                   FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiType_1isInstanceClass)},
  {CC"RiType_isInterface",              CC"(Ljava/lang/Class;)Z",                                                   FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiType_1isInterface)},
  {CC"installCode",                     CC"(Lcom/sun/hotspot/c1x/HotSpotTargetMethod;)V",                           FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_installCode)},
  {CC"getConfiguration",                CC"()Lcom/sun/hotspot/c1x/HotSpotVMConfig;",                                FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_getConfiguration)}
};

int VMEntries_methods_count() {
  return sizeof(VMEntries_methods) / sizeof(JNINativeMethod);
}
