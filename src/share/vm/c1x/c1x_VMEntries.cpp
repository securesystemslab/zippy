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
JNIEXPORT jbyteArray JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1code(JNIEnv *env, jobject, jobject method) {
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
JNIEXPORT jint JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1maxStackSize(JNIEnv *, jobject, jobject method) {
  methodOop m = (methodOop)JNIHandles::resolve(method);
  return m->max_stack();
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_maxLocals
* Signature: (Ljava/lang/Object;)I
*/
JNIEXPORT jint JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1maxLocals(JNIEnv *, jobject, jobject method) {
  methodOop m = (methodOop)JNIHandles::resolve(method);
  return m->max_locals();
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_holder
* Signature: (Ljava/lang/Object;)Lcom/sun/cri/ri/RiType;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1holder(JNIEnv *, jobject, jobject method) {
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
JNIEXPORT jstring JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1signature(JNIEnv *env, jobject, jobject method) {
  methodOop m = (methodOop)JNIHandles::resolve(method);
  return env->NewStringUTF(m->signature()->as_utf8());
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_name
* Signature: (Ljava/lang/Object;)Ljava/lang/String;
*/
JNIEXPORT jstring JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1name(JNIEnv *env, jobject, jobject method) {
  methodOop m = (methodOop)JNIHandles::resolve(method);
  return env->NewStringUTF(m->name()->as_utf8());
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiSignature_lookupType
* Signature: (Ljava/lang/String;Lcom/sun/cri/ri/RiType;)Lcom/sun/cri/ri/RiType;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiSignature_1lookupType(JNIEnv *, jobject, jstring name, jobject accessor) {
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
JNIEXPORT jstring JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiSignature_1symbolToString(JNIEnv *env, jobject, jobject symbol) {
  symbolOop s = (symbolOop)JNIHandles::resolve(symbol);
  return env->NewStringUTF(s->as_utf8());
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiType_javaClass
* Signature: (Ljava/lang/Object;)Ljava/lang/Class;
*/
JNIEXPORT jclass JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1javaClass(JNIEnv *, jobject, jobject klass) {
  klassOop k = (klassOop)JNIHandles::resolve(klass);
  oop result = k->klass_part()->java_mirror();
  return (jclass) JNIHandles::make_local(Thread::current(), result);
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiType_name
* Signature: (Ljava/lang/Object;)Ljava/lang/String;
*/
JNIEXPORT jstring JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1name(JNIEnv *env, jobject, jobject klass) {
  klassOop k = (klassOop)JNIHandles::resolve(klass);
  return env->NewStringUTF(k->klass_part()->name()->as_utf8());
}


/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiConstantPool_lookupConstant
* Signature: (Ljava/lang/Object;I)Ljava/lang/Object;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupConstant(JNIEnv *env, jobject, jobject cpHandle, jint index) {

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
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupMethod(JNIEnv *env, jobject, jobject cpHandle, jint index, jbyte byteCode) {
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
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupField(JNIEnv *env, jobject, jobject cpHandle, jint index) {
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
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_findRiType(JNIEnv *, jobject, jobject klass) {
  VM_ENTRY_MARK;
  klassOop o = (klassOop)JNIHandles::resolve(klass);
  return JNIHandles::make_local(THREAD, C1XCompiler::get_RiType(o));
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiRuntime_getConstantPool
* Signature: (Ljava/lang/Object;)Lcom/sun/cri/ri/RiConstantPool;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiRuntime_1getConstantPool(JNIEnv *, jobject, jobject klass) {
  VM_ENTRY_MARK;
  klassOop o = (klassOop)JNIHandles::resolve(klass);
  return JNIHandles::make_local(THREAD, C1XCompiler::get_RiConstantPool(((instanceKlass*)o->klass_part())->constants()));
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiType_isArrayClass
* Signature: (Ljava/lang/Object;)Z
*/
JNIEXPORT jboolean JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1isArrayClass(JNIEnv *, jobject, jobject klass) {
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
JNIEXPORT jboolean JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1isInstanceClass(JNIEnv *, jobject, jobject klass) {
  klassOop o = (klassOop)JNIHandles::resolve(klass);
  return o->klass_part()->oop_is_instanceKlass();
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiType_isInterface
* Signature: (Ljava/lang/Object;)Z
*/
JNIEXPORT jboolean JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1isInterface(JNIEnv *, jobject, jobject klass) {
  klassOop o = (klassOop)JNIHandles::resolve(klass);
  return o->klass_part()->is_interface();
}

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_accessFlags
* Signature: (Ljava/lang/Object;)I
*/
JNIEXPORT jint JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1accessFlags(JNIEnv *, jobject, jobject method) {
  methodOop m = (methodOop)JNIHandles::resolve(method);
  return m->access_flags().as_int();
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
    HotSpotType_klassOop = compute_offset(HotSpotType, "klassOop", "Ljava/lang/Object;");
  }
};




/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    installCode
* Signature: (Lcom/sun/hotspot/c1x/HotSpotTargetMethod;)V
*/
JNIEXPORT void JNICALL Java_com_sun_hotspot_c1x_VMEntries_installCode(JNIEnv *jniEnv, jobject, jobject targetMethod) {
  TypeHelper types(jniEnv);

  methodOop m = (methodOop)JNIHandles::resolve(get_object(jniEnv, targetMethod, "methodOop"));
  jbyteArray code = (jbyteArray)get_object(jniEnv, targetMethod, "code", "[B");
  jintArray relocationOffsetsObj = (jintArray)get_object(jniEnv, targetMethod, "relocationOffsets", "[I");
  jobjectArray relocationDataObj = (jobjectArray)get_object(jniEnv, targetMethod, "relocationData", "[Ljava/lang/Object;");
  jint frameSize = get_int(jniEnv, targetMethod, "frameSize");

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

  int codeSize = ((arrayOop)JNIHandles::resolve(code))->length();
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
      oop obj = relocationObjects[i];
      assert(obj != NULL, "NULL oop needn't be patched");

      if (java_lang_boxing_object::is_instance(obj, T_LONG)) {
          address operand = Assembler::locate_operand(inst, Assembler::call32_operand);
          long dest = obj->long_field(java_lang_boxing_object::value_offset_in_bytes(T_LONG));
          long disp = dest - (long)(operand + 4);
          assert(disp == (int) disp, "disp doesn't fit in 32 bits");
          *((int*)operand) = (int)disp;

          instructions->relocate(inst, runtime_call_Relocation::spec(), Assembler::call32_operand);
          tty->print_cr("relocating (Long)");
      } else if (obj->is_a(types.HotSpotType)) {
          address operand = Assembler::locate_operand(inst, Assembler::imm_operand);

          *((oop**)operand) = obj->obj_field_addr<oop>(types.HotSpotType_klassOop);
          instructions->relocate(inst, oop_Relocation::spec_for_immediate(), Assembler::imm_operand);
          tty->print_cr("relocating (HotSpotType)");
      } else {
          tty->print_cr("unknown relocation type");
      }
    }
  }

  buffer.print();

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
  set_long(env, config, "instanceofStub", (long)Runtime1::entry_for(Runtime1::slow_subtype_check_id));
  set_long(env, config, "debugStub", (long)warning);
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
  {CC"RiSignature_symbolToString",      CC"(Ljava/lang/Object;)Ljava/lang/String;",                                 FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_RiSignature_1symbolToString)},
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
  {CC"installCode",                     CC"(Lcom/sun/hotspot/c1x/HotSpotTargetMethod;)V",                           FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_installCode)},
  {CC"getConfiguration",                CC"()Lcom/sun/hotspot/c1x/HotSpotVMConfig;",                                FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_getConfiguration)}
};

int VMEntries_methods_count() {
  return sizeof(VMEntries_methods) / sizeof(JNINativeMethod);
}
