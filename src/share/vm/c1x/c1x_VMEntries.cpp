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




// public byte[] RiMethod_code(HotSpotProxy method);
JNIEXPORT jbyteArray JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1code(JNIEnv *env, jobject, jlong vmId) {
  methodOop method = C1XObjects::get<methodOop>(vmId);
  int code_size = method->code_size();
  jbyteArray result = env->NewByteArray(code_size);
  env->SetByteArrayRegion(result, 0, code_size, (const jbyte *)method->code_base());
  return result;
}

// public int RiMethod_maxStackSize(HotSpotProxy method);
JNIEXPORT jint JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1maxStackSize(JNIEnv *, jobject, jlong vmId) {
  return C1XObjects::get<methodOop>(vmId)->max_stack();
}

// public int RiMethod_maxLocals(HotSpotProxy method);
JNIEXPORT jint JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1maxLocals(JNIEnv *, jobject, jlong vmId) {
  return C1XObjects::get<methodOop>(vmId)->max_locals();
}

// public RiType RiMethod_holder(HotSpotProxy method);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1holder(JNIEnv *, jobject, jlong vmId) {
  VM_ENTRY_MARK
  klassOop klass = C1XObjects::get<methodOop>(vmId)->method_holder();
  jlong klassVmId = C1XObjects::add<klassOop>(klass);
  Handle name = C1XObjects::toString<Handle>(klass->klass_part()->name(), CHECK_NULL);
  oop holder = VMExits::createRiType(klassVmId, name, THREAD);
  return JNIHandles::make_local(THREAD, holder);
}

// public String RiMethod_signature(HotSpotProxy method);
JNIEXPORT jstring JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1signature(JNIEnv *env, jobject, jlong vmId) {
  VM_ENTRY_MARK
  methodOop method = C1XObjects::get<methodOop>(vmId);
  return C1XObjects::toString<jstring>(method->signature(), THREAD);
}

// public int RiMethod_accessFlags(HotSpotProxy method);
JNIEXPORT jint JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1accessFlags(JNIEnv *, jobject, jlong vmId) {
  return C1XObjects::get<methodOop>(vmId)->access_flags().as_int();
}

// public RiType RiSignature_lookupType(String returnType, HotSpotProxy accessingClass);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiSignature_1lookupType(JNIEnv *, jobject, jstring jname, jlong accessingClassVmId) {
  VM_ENTRY_MARK;

  symbolOop nameSymbol = C1XObjects::toSymbol(jname);
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
      classloader = C1XObjects::get<klassOop>(accessingClassVmId)->klass_part()->class_loader();
      protectionDomain = C1XObjects::get<klassOop>(accessingClassVmId)->klass_part()->protection_domain();
    }
    klassOop resolved_type = SystemDictionary::resolve_or_null(nameSymbol, classloader, protectionDomain, THREAD);
    if (resolved_type != NULL) {
      result = VMExits::createRiType(C1XObjects::add<klassOop>(resolved_type), name, THREAD);
    } else {
      result = VMExits::createRiTypeUnresolved(name, accessingClassVmId, THREAD);
    }
  }

  return JNIHandles::make_local(THREAD, result);
}

// public Object RiConstantPool_lookupConstant(HotSpotProxy constantPool, int cpi);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupConstant(JNIEnv *env, jobject, jlong vmId, jint index) {
  VM_ENTRY_MARK;

  constantPoolOop cp = C1XObjects::get<constantPoolOop>(vmId);

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
    result = VMExits::createCiConstantObject(C1XObjects::add<oop>(string), CHECK_0);
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
    result = VMExits::createCiConstantObject(C1XObjects::add<oop>(obj), CHECK_0);
  } else {
    ShouldNotReachHere();
  }

  return JNIHandles::make_local(THREAD, result);
}

// public RiMethod RiConstantPool_lookupMethod(HotSpotProxy constantPool, int cpi, byte byteCode
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupMethod(JNIEnv *env, jobject, jlong vmId, jint index, jbyte byteCode) {
  VM_ENTRY_MARK;

  constantPoolOop cp = C1XObjects::get<constantPoolOop>(vmId);

  Bytecodes::Code bc = (Bytecodes::Code)(((int)byteCode) & 0xFF);
  ciInstanceKlass* loading_klass = (ciInstanceKlass *)CURRENT_ENV->get_object(cp->pool_holder());
  ciMethod *cimethod = CURRENT_ENV->get_method_by_index(cp, index, bc, loading_klass);
  methodOop method = (methodOop)cimethod->get_oop();
  Handle name = C1XObjects::toString<Handle>(method->name(), CHECK_NULL);
  return JNIHandles::make_local(THREAD, VMExits::createRiMethod(C1XObjects::add<methodOop>(method), name, THREAD));
}

// public RiSignature RiConstantPool_lookupSignature(HotSpotProxy constantPool, int cpi);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupSignature(JNIEnv *env, jobject, jlong vmId, jint index) {
  fatal("currently unsupported");
  return NULL;
}

// public RiType RiConstantPool_lookupType(HotSpotProxy constantPool, int cpi);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupType(JNIEnv *env, jobject, jlong vmId, jint index) {
  VM_ENTRY_MARK;

  constantPoolOop cp = C1XObjects::get<constantPoolOop>(vmId);

  ciInstanceKlass* loading_klass = (ciInstanceKlass *)CURRENT_ENV->get_object(cp->pool_holder());
  bool is_accessible = false;
  ciKlass *klass = CURRENT_ENV->get_klass_by_index(cp, index, is_accessible, loading_klass);
  return JNIHandles::make_local(THREAD, C1XCompiler::get_RiType(klass, cp->klass(), THREAD));

}

// public RiField RiConstantPool_lookupField(HotSpotProxy constantPool, int cpi);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupField(JNIEnv *env, jobject, jlong vmId, jint index) {
  VM_ENTRY_MARK;

  constantPoolOop cp = C1XObjects::get<constantPoolOop>(vmId);

  ciInstanceKlass* loading_klass = (ciInstanceKlass *)CURRENT_ENV->get_object(cp->pool_holder());
  ciField *field = CURRENT_ENV->get_field_by_index(loading_klass, index);
  return JNIHandles::make_local(THREAD, C1XCompiler::get_RiField(field, THREAD));
}

// public RiConstantPool RiType_constantPool(HotSpotProxy type);
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1constantPool(JNIEnv *, jobject, jlong vmId) {
  VM_ENTRY_MARK;

  constantPoolOop constantPool = instanceKlass::cast(C1XObjects::get<klassOop>(vmId))->constants();
  return JNIHandles::make_local(VMExits::createRiConstantPool(C1XObjects::add<constantPoolOop>(constantPool), THREAD));
}

// public boolean RiType_isArrayClass(HotSpotProxy klass);
JNIEXPORT jboolean JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1isArrayClass(JNIEnv *, jobject, jlong vmId) {
  return C1XObjects::get<klassOop>(vmId)->klass_part()->oop_is_javaArray();
}

// public boolean RiType_isInstanceClass(HotSpotProxy klass);
JNIEXPORT jboolean JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1isInstanceClass(JNIEnv *, jobject, jlong vmId) {
  return C1XObjects::get<klassOop>(vmId)->klass_part()->oop_is_instance();
}

// public boolean RiType_isInterface(HotSpotProxy klass);
JNIEXPORT jboolean JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1isInterface(JNIEnv *, jobject, jlong vmId) {
  return C1XObjects::get<klassOop>(vmId)->klass_part()->is_interface();
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

  set_long(env, config, "instanceofStub", C1XObjects::addStub(Runtime1::entry_for(Runtime1::slow_subtype_check_id)));
  set_long(env, config, "debugStub", C1XObjects::addStub((address)warning));
  set_long(env, config, "resolveStaticCallStub", C1XObjects::addStub(SharedRuntime::get_resolve_static_call_stub()));
  jintArray arrayOffsets = env->NewIntArray(basicTypeCount);
  for (int i=0; i<basicTypeCount; i++) {
    jint offset = arrayOopDesc::base_offset_in_bytes(basicTypes[i]);
    env->SetIntArrayRegion(arrayOffsets, i, 1, &offset);
  }
  set_int_array(env, config, "arrayOffsets", arrayOffsets);
  set_int(env, config, "arrayClassElementOffset", objArrayKlass::element_klass_offset_in_bytes() + sizeof(oopDesc));
  return config;
}

#define C1X_REGISTER_COUNT 32

VMReg get_hotspot_reg(jint c1x_reg) {
  Register cpu_registers[] = { rax, rcx, rdx, rbx, rsp, rbp, rsi, rdi, r8, r9, r10, r11, r12, r13, r14, r15 };
  XMMRegister xmm_registers[] = { xmm0, xmm1, xmm2,  xmm3,  xmm4,  xmm5,  xmm6,  xmm7, xmm8, xmm9, xmm10, xmm11, xmm12, xmm13, xmm14, xmm15 };

  if (c1x_reg < 16) {
    return cpu_registers[c1x_reg]->as_VMReg();
  } else {
    assert(c1x_reg < C1X_REGISTER_COUNT, "invalid register number");
    return xmm_registers[c1x_reg - 16]->as_VMReg();
  }

}

static OopMap* create_oop_map(jint frame_size, jint parameter_count, oop debug_info) {
  OopMap* map = new OopMap(frame_size, parameter_count);
  arrayOop register_map = (arrayOop)CiDebugInfo::registerRefMap(debug_info);
  arrayOop frame_map = (arrayOop)CiDebugInfo::frameRefMap(debug_info);

  jint register_count = VMRegImpl::stack2reg(0)->value();
  tty->print_cr("register count: %i", register_count);

  for (jint i=0; i<C1X_REGISTER_COUNT; i++) {
    unsigned char byte = ((unsigned char*)register_map->base(T_BYTE))[i / 8];
    bool is_oop = (byte & (1 << (i % 8))) != 0;
    VMReg reg = get_hotspot_reg(i);
    if (is_oop) {
      map->set_oop(reg);
    } else {
      map->set_value(reg);
    }
  }

  for (jint i=0; i<frame_size; i++) {
    unsigned char byte = ((unsigned char*)frame_map->base(T_BYTE))[i / 8];
    bool is_oop = (byte & (1 << (i % 8))) != 0;
    VMReg reg = VMRegImpl::stack2reg(i);
    if (is_oop) {
      map->set_oop(reg);
    } else {
      map->set_value(reg);
    }
  }

  // TODO parameters?
  return map;
}

static ScopeValue* get_hotspot_value(oop value) {
  fatal("not implemented");
  if (value->is_a(CiRegisterValue::klass())) {
    tty->print("register value");
    value->print();
  } else if (value->is_a(CiStackSlot::klass())) {
    tty->print("stack value");
    value->print();
  } else {
    ShouldNotReachHere();
  }
}


class CodeInstaller {
private:
  ciEnv*        _env;

  oop           _citarget_method;
  oop           _hotspot_method;
  oop           _name;
  arrayOop      _sites;
  CodeOffsets   _offsets;

  arrayOop      _code;
  jint          _code_size;
  jint          _frame_size;
  jint          _parameter_count;
  jint          _constants_size;
  jint          _total_size;

  CodeSection*  _instructions;
  CodeSection*  _constants;

  OopRecorder*  _oop_recorder;
  DebugInformationRecorder* _debug_recorder;
  Dependencies* _dependencies;

public:

  // constructor used to create a method
  CodeInstaller(oop target_method) {
    VM_ENTRY_MARK;
    _env = CURRENT_ENV;

    initialize_fields(target_method);
    assert(_hotspot_method != NULL && _name == NULL, "installMethod needs NON-NULL method and NULL name");


    // TODO: This is a hack.. Produce correct entries.
    _offsets.set_value(CodeOffsets::Exceptions, 0);
    _offsets.set_value(CodeOffsets::Deopt, 0);

    methodOop method = C1XObjects::get<methodOop>(HotSpotMethod::vmId(_hotspot_method));
    ciMethod *ciMethodObject = (ciMethod *)_env->get_object(method);
    _parameter_count = method->size_of_parameters();

    // (very) conservative estimate: each site needs a relocation
    CodeBuffer buffer("temp c1x method", _total_size, _sites->length() * relocInfo::length_limit);
    initialize_buffer(buffer);
    ExceptionHandlerTable handler_table;
    ImplicitExceptionTable inc_table;
    {
      ThreadToNativeFromVM t((JavaThread*)THREAD);
      _env->register_method(ciMethodObject, -1, &_offsets, 0, &buffer, _frame_size, _debug_recorder->_oopmaps, &handler_table, &inc_table, NULL, _env->comp_level(), false, false);
    }
  }

  // constructor used to create a stub
  CodeInstaller(oop target_method, jlong& id) {
    VM_ENTRY_MARK;
    _env = CURRENT_ENV;

    initialize_fields(target_method);
    assert(_hotspot_method == NULL && _name != NULL, "installMethod needs NON-NULL name and NULL method");

    // (very) conservative estimate: each site needs a relocation
    CodeBuffer buffer("temp c1x stub", _total_size, _sites->length() * relocInfo::length_limit);
    initialize_buffer(buffer);

    const char* cname = java_lang_String::as_utf8_string(_name);
    BufferBlob* blob = BufferBlob::create(strdup(cname), &buffer);          // this is leaking strings... but only a limited number of stubs will be created
    Disassembler::decode((CodeBlob*)blob);
    id = C1XObjects::addStub(blob->instructions_begin());
  }

private:
  void initialize_fields(oop target_method) {
    _citarget_method = HotSpotTargetMethod::targetMethod(target_method);
    _hotspot_method = HotSpotTargetMethod::method(target_method);
    _name = HotSpotTargetMethod::name(target_method);
    _sites = (arrayOop)HotSpotTargetMethod::sites(target_method);

    _code = (arrayOop)CiTargetMethod::targetCode(_citarget_method);
    _code_size = CiTargetMethod::targetCodeSize(_citarget_method);
    _frame_size = CiTargetMethod::frameSize(_citarget_method);

    // (very) conservative estimate: each site needs a constant section entry
    _constants_size = _sites->length() * BytesPerLong;
    _total_size = align_size_up(_code_size, HeapWordSize) + _constants_size;
  }

  void site_Safepoint(CodeBuffer& buffer, jint pc_offset, oop site) {

  }

  void record_frame(jint pc_offset, oop code_pos, oop frame) {
    oop caller_pos = CiCodePos::caller(code_pos);
    if (caller_pos != NULL) {
      oop caller_frame = CiDebugInfo_Frame::caller(frame);
      record_frame(pc_offset, caller_pos, caller_frame);
    } else {
      assert(frame == NULL || CiDebugInfo_Frame::caller(frame) == NULL, "unexpected layout - different nesting of Frame and CiCodePos");
    }

    assert(frame == NULL || code_pos == CiDebugInfo_Frame::codePos(frame), "unexpected CiCodePos layout");

    oop hotspot_method = CiCodePos::method(code_pos);
    methodOop method = C1XObjects::get<methodOop>(HotSpotMethod::vmId(hotspot_method));
    ciMethod *cimethod = (ciMethod *)_env->get_object(method);
    jint bci = CiCodePos::bci(code_pos);

    if (frame != NULL) {
      jint local_count = CiDebugInfo_Frame::numLocals(frame);
      jint expression_count = CiDebugInfo_Frame::numStack(frame);
      jint monitor_count = CiDebugInfo_Frame::numLocks(frame);
      arrayOop values = (arrayOop)CiDebugInfo_Frame::values(frame);

      assert(local_count + expression_count + monitor_count == values->length(), "unexpected values length");
      assert(monitor_count == 0, "monitors not supported");

      GrowableArray<ScopeValue*>* locals = new GrowableArray<ScopeValue*>();
      GrowableArray<ScopeValue*>* expressions = new GrowableArray<ScopeValue*>();
      GrowableArray<MonitorValue*>* monitors = new GrowableArray<MonitorValue*>();

      for (jint i=0; i<values->length(); i++) {
        ScopeValue* value = get_hotspot_value(((oop*)values->base(T_OBJECT))[i]);

        if (i < local_count) {
          locals->append(value);
        } else if (i < local_count + expression_count) {
          expressions->append(value);
        } else {
          ShouldNotReachHere();
          // monitors->append(value);
        }
      }
      DebugToken* locals_token = _debug_recorder->create_scope_values(locals);
      DebugToken* expressions_token = _debug_recorder->create_scope_values(expressions);
      DebugToken* monitors_token = _debug_recorder->create_monitor_values(monitors);

      _debug_recorder->describe_scope(pc_offset, cimethod, bci, false, false, false, locals_token, expressions_token, monitors_token);
    } else {
      _debug_recorder->describe_scope(pc_offset, cimethod, bci, false, false, false, NULL, NULL, NULL);
    }
  }

  void site_Call(CodeBuffer& buffer, jint pc_offset, oop site) {
    oop runtime_call = CiTargetMethod_Call::runtimeCall(site);
    oop hotspot_method = CiTargetMethod_Call::method(site);
    oop symbol = CiTargetMethod_Call::symbol(site);
    oop global_stub = CiTargetMethod_Call::globalStubID(site);

    oop debug_info = CiTargetMethod_Call::debugInfo(site);
    arrayOop stack_map = (arrayOop)CiTargetMethod_Call::stackMap(site);
    arrayOop register_map = (arrayOop)CiTargetMethod_Call::registerMap(site);

    assert((runtime_call ? 1 : 0) + (hotspot_method ? 1 : 0) + (symbol ? 1 : 0) + (global_stub ? 1 : 0) == 1, "Call site needs exactly one type");

    if (runtime_call != NULL) {
      tty->print_cr("runtime_call");
    } else if (global_stub != NULL) {
      tty->print_cr("global_stub_id");
    } else if (symbol != NULL) {
      tty->print_cr("symbol");
    } else { // method != NULL
      assert(hotspot_method->is_a(SystemDictionary::HotSpotMethod_klass()), "unexpected RiMethod subclass");
      methodOop method = C1XObjects::get<methodOop>(HotSpotMethod::vmId(hotspot_method));

      address instruction = _instructions->start() + pc_offset;
      address operand = Assembler::locate_operand(instruction, Assembler::call32_operand);
      address next_instruction = Assembler::locate_next_instruction(instruction);
      jint next_pc_offset = next_instruction - _instructions->start();

      assert(debug_info != NULL, "debug info expected");
      _debug_recorder->add_safepoint(next_pc_offset, create_oop_map(_frame_size, _parameter_count, debug_info));
      oop code_pos = CiDebugInfo::codePos(debug_info);
      oop frame = CiDebugInfo::frame(debug_info);
      record_frame(next_pc_offset, code_pos, frame);

      if (method->is_static()) {
        tty->print_cr("static method");


        address dest = SharedRuntime::get_resolve_static_call_stub();
        long disp = dest - next_instruction;
        assert(disp == (jint) disp, "disp doesn't fit in 32 bits");
        *((jint*)operand) = (jint)disp;

        _instructions->relocate(instruction, relocInfo::static_call_type, Assembler::call32_operand);
        tty->print_cr("relocating (Long) %016x/%016x", instruction, operand);
      } else {
        tty->print_cr("non-static method");
        ShouldNotReachHere();
      }

      _debug_recorder->end_safepoint(pc_offset);
    }
  }

  void site_DataPatch(CodeBuffer& buffer, jint pc_offset, oop site) {
    oop constant = CiTargetMethod_DataPatch::constant(site);
    oop kind = CiConstant::kind(constant);

    address instruction = _instructions->start() + pc_offset;
    address operand = Assembler::locate_operand(instruction, Assembler::disp32_operand);
    address next_instruction = Assembler::locate_next_instruction(instruction);

    switch(CiKind::typeChar(kind)) {
      case 'z':
      case 'b':
      case 's':
      case 'c':
      case 'i':
        fatal("int-sized values not expected in DataPatch");
        break;
      case 'f':
      case 'l':
      case 'd': {
        // we don't care if this is a long/double/etc., the primitive field contains the right bits
        address dest = _constants->end();
        *(jlong*)dest = CiConstant::primitive(constant);
        _constants->set_end(dest + BytesPerLong);

        long disp = dest - next_instruction;
        assert(disp == (jint) disp, "disp doesn't fit in 32 bits");
        *((jint*)operand) = (jint)disp;

        _instructions->relocate(instruction, section_word_Relocation::spec((address)dest, CodeBuffer::SECT_CONSTS), Assembler::disp32_operand);
        tty->print_cr("relocating (Float/Long/Double) at %016x/%016x", instruction, operand);
        break;
      }
      case 'a':
        CiConstant::object(constant)->print();
        tty->print_cr("DataPatch of object type");
        break;
      default:
        fatal("unexpected CiKind in DataPatch");
        break;
    }
  }

  void site_ExceptionHandler(CodeBuffer& buffer, jint pc_offset, oop site) {

  }

  void site_Mark(CodeBuffer& buffer, jint pc_offset, oop site) {
    oop id_obj = CiTargetMethod_Mark::id(site);
    arrayOop references = (arrayOop)CiTargetMethod_Mark::references(site);

    if (id_obj != NULL) {
      assert(java_lang_boxing_object::is_instance(id_obj, T_INT), "Integer id expected");
      jint id = id_obj->int_field(java_lang_boxing_object::value_offset_in_bytes(T_INT));

      address instruction = _instructions->start() + pc_offset;

      switch (id) {
        case C1XCompiler::MARK_UNVERIFIED_ENTRY:
          _offsets.set_value(CodeOffsets::Entry, pc_offset);
          break;
        case C1XCompiler::MARK_VERIFIED_ENTRY:
          _offsets.set_value(CodeOffsets::Verified_Entry, pc_offset);
          break;
        case C1XCompiler::MARK_STATIC_CALL_STUB: {
          assert(references->length() == 1, "static call stub needs one reference");
          oop ref = ((oop*)references->base(T_OBJECT))[0];
          address call_pc = _instructions->start() + CiTargetMethod_Site::pcOffset(ref);
          _instructions->relocate(instruction, static_stub_Relocation::spec(call_pc));
          break;
        }
      }
    }
  }

  // perform data and call relocation on the CodeBuffer
  void initialize_buffer(CodeBuffer& buffer) {
    _oop_recorder = new OopRecorder(_env->arena());
    _env->set_oop_recorder(_oop_recorder);
    _debug_recorder = new DebugInformationRecorder(_env->oop_recorder());
    _debug_recorder->set_oopmaps(new OopMapSet());
    _dependencies = new Dependencies(_env);

    _env->set_oop_recorder(_oop_recorder);
    _env->set_debug_info(_debug_recorder);
    _env->set_dependencies(_dependencies);
    buffer.initialize_oop_recorder(_oop_recorder);

    buffer.initialize_consts_size(_constants_size);
    _instructions = buffer.insts();
    _constants = buffer.consts();

    // copy the code into the newly created CodeBuffer
    memcpy(_instructions->start(), _code->base(T_BYTE), _code_size);
    _instructions->set_end(_instructions->start() + _code_size);

    oop* sites = (oop*)_sites->base(T_OBJECT);
    for (int i=0; i<_sites->length(); i++) {
      oop site = sites[i];
      jint pc_offset = CiTargetMethod_Site::pcOffset(site);

      if (site->is_a(CiTargetMethod_Safepoint::klass())) {
        tty->print_cr("safepoint at %i", pc_offset);
        site_Safepoint(buffer, pc_offset, site);
      } else if (site->is_a(CiTargetMethod_Call::klass())) {
        tty->print_cr("call at %i", pc_offset);
        site_Call(buffer, pc_offset, site);
      } else if (site->is_a(CiTargetMethod_DataPatch::klass())) {
        tty->print_cr("datapatch at %i", pc_offset);
        site_DataPatch(buffer, pc_offset, site);
      } else if (site->is_a(CiTargetMethod_ExceptionHandler::klass())) {
        tty->print_cr("exception handler at %i", pc_offset);
        site_ExceptionHandler(buffer, pc_offset, site);
      } else if (site->is_a(CiTargetMethod_Mark::klass())) {
        tty->print_cr("mark at %i", pc_offset);
        site_Mark(buffer, pc_offset, site);
      } else {
        ShouldNotReachHere();
      }
    }

/*
    if (_relocation_count > 0) {
      jint* relocation_offsets = (jint*)((arrayOop)JNIHandles::resolve(_relocation_offsets))->base(T_INT);
      oop* relocation_objects = (oop*)((arrayOop)JNIHandles::resolve(_relocation_data))->base(T_OBJECT);

      for (int i = 0; i < _relocation_count; i++) {
        address inst = (address)instructions->start() + relocation_offsets[i];
        u_char inst_byte = *inst;
        oop obj = relocation_objects[i];
        assert(obj != NULL, "NULL oop needn't be patched");

        if (obj->is_a(SystemDictionary::HotSpotProxy_klass())) {
          jlong id = com_sun_hotspot_c1x_HotSpotProxy::get_id(obj);
          switch (id & C1XObjects::TYPE_MASK) {
            case C1XObjects::CONSTANT: {
              address operand = Assembler::locate_operand(inst, Assembler::imm_operand);

              *((jobject*)operand) = JNIHandles::make_local(C1XObjects::get<oop>(id));
              instructions->relocate(inst, oop_Relocation::spec_for_immediate(), Assembler::imm_operand);
              tty->print_cr("relocating (HotSpotType) %02x at %016x/%016x", inst_byte, inst, operand);
              break;
            }
            case C1XObjects::STUB: {
              address operand = Assembler::locate_operand(inst, Assembler::call32_operand);

              long dest = (long)C1XObjects::getStub(id);
              long disp = dest - (long)(operand + 4);
              assert(disp == (int) disp, "disp doesn't fit in 32 bits");
              *((int*)operand) = (int)disp;

              instructions->relocate(inst, runtime_call_Relocation::spec(), Assembler::call32_operand);
              tty->print_cr("relocating (Long) %02x at %016x/%016x", inst_byte, inst, operand);
              break;
            }
          }
        } else if (java_lang_boxing_object::is_instance(obj)) {
          address operand = Assembler::locate_operand(inst, Assembler::disp32_operand);
          long dest = (long)constants->end();
          if (java_lang_boxing_object::is_instance(obj, T_LONG)) {
            // tty->print("relocate: %l\n", obj->long_field(java_lang_boxing_object::value_offset_in_bytes(T_LONG)));
            *(jlong*)constants->end() = obj->long_field(java_lang_boxing_object::value_offset_in_bytes(T_LONG));
          } else if (java_lang_boxing_object::is_instance(obj, T_DOUBLE)) {
            // tty->print("relocate: %f\n", obj->double_field(java_lang_boxing_object::value_offset_in_bytes(T_DOUBLE)));
            *(jdouble*)constants->end() = obj->double_field(java_lang_boxing_object::value_offset_in_bytes(T_DOUBLE));
          } else if (java_lang_boxing_object::is_instance(obj, T_FLOAT)) {
            // tty->print("relocate: %f\n", obj->double_field(java_lang_boxing_object::value_offset_in_bytes(T_DOUBLE)));
            *(jfloat*)constants->end() = obj->float_field(java_lang_boxing_object::value_offset_in_bytes(T_FLOAT));
          }
          constants->set_end(constants->end() + 8);

          long disp = dest - (long)(operand + 4);
          assert(disp == (int) disp, "disp doesn't fit in 32 bits");
          *((int*)operand) = (int)disp;

          instructions->relocate(inst, section_word_Relocation::spec((address)dest, CodeBuffer::SECT_CONSTS), Assembler::disp32_operand);
          tty->print_cr("relocating (Long/Double) %02x at %016x/%016x", inst_byte, inst, operand);
        } else if (obj->is_a(_types.HotSpotTypeResolved)) {
          address operand = Assembler::locate_operand(inst, Assembler::imm_operand);

          *((jobject*)operand) = JNIHandles::make_local(C1XObjects::get<klassOop>(obj->obj_field(_types.HotSpotTypeResolved_klassOop)));
          instructions->relocate(inst, oop_Relocation::spec_for_immediate(), Assembler::imm_operand);
          tty->print_cr("relocating (HotSpotType) %02x at %016x/%016x", inst_byte, inst, operand);
        } else {
          tty->print_cr("unknown relocation type");
          obj->print();
        }
      }
    }*/
  }
};

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
  {CC"getConfiguration",                CC"()"CONFIG,                       FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_getConfiguration)},
  {CC"installMethod",                   CC"("TARGET_METHOD")V",             FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_installMethod)},
  {CC"installStub",                     CC"("TARGET_METHOD")"PROXY,         FN_PTR(Java_com_sun_hotspot_c1x_VMEntries_installStub)}
};

int VMEntries_methods_count() {
  return sizeof(VMEntries_methods) / sizeof(JNINativeMethod);
}
