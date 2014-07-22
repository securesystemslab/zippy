/*
 * Copyright (c) 2011, 2014, Oracle and/or its affiliates. All rights reserved.
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

#include "precompiled.hpp"
#include "code/compiledIC.hpp"
#include "compiler/compileBroker.hpp"
#include "compiler/disassembler.hpp"
#include "runtime/javaCalls.hpp"
#include "graal/graalEnv.hpp"
#include "graal/graalCompiler.hpp"
#include "graal/graalCodeInstaller.hpp"
#include "graal/graalJavaAccess.hpp"
#include "graal/graalCompilerToVM.hpp"
#include "graal/graalRuntime.hpp"
#include "asm/register.hpp"
#include "classfile/vmSymbols.hpp"
#include "code/vmreg.hpp"

#ifdef TARGET_ARCH_x86
# include "vmreg_x86.inline.hpp"
#endif
#ifdef TARGET_ARCH_sparc
# include "vmreg_sparc.inline.hpp"
#endif
#ifdef TARGET_ARCH_zero
# include "vmreg_zero.inline.hpp"
#endif
#ifdef TARGET_ARCH_arm
# include "vmreg_arm.inline.hpp"
#endif
#ifdef TARGET_ARCH_ppc
# include "vmreg_ppc.inline.hpp"
#endif

Method* getMethodFromHotSpotMethod(oop hotspot_method) {
  assert(hotspot_method != NULL && hotspot_method->is_a(HotSpotResolvedJavaMethod::klass()), "sanity");
  return asMethod(HotSpotResolvedJavaMethod::metaspaceMethod(hotspot_method));
}

// convert Graal register indices (as used in oop maps) to HotSpot registers
VMReg get_hotspot_reg(jint graal_reg) {
  if (graal_reg < RegisterImpl::number_of_registers) {
    return as_Register(graal_reg)->as_VMReg();
  } else {
    int remainder = graal_reg - RegisterImpl::number_of_registers;
#ifdef TARGET_ARCH_x86
    if (remainder < XMMRegisterImpl::number_of_registers) {
      return as_XMMRegister(remainder)->as_VMReg();
    }
#endif
    ShouldNotReachHere();
    return NULL;
  }
}

const int MapWordBits = 64;

static bool is_bit_set(oop bitset, int i) {
  jint words_idx = i / MapWordBits;
  arrayOop words = (arrayOop) BitSet::words(bitset);
  assert(words_idx >= 0 && words_idx < words->length(), "unexpected index");
  jlong word = ((jlong*) words->base(T_LONG))[words_idx];
  return (word & (1LL << (i % MapWordBits))) != 0;
}

static int bitset_size(oop bitset) {
  arrayOop arr = (arrayOop) BitSet::words(bitset);
  return arr->length() * MapWordBits;
}

static void set_vmreg_oops(OopMap* map, VMReg reg, oop bitset, int idx) {
  bool is_oop = is_bit_set(bitset, 3 * idx);
  if (is_oop) {
    bool narrow1 = is_bit_set(bitset, 3 * idx + 1);
    bool narrow2 = is_bit_set(bitset, 3 * idx + 2);
    if (narrow1 || narrow2) {
      if (narrow1) {
        map->set_narrowoop(reg);
      }
      if (narrow2) {
        map->set_narrowoop(reg->next());
      }
    } else {
      map->set_oop(reg);
    }
  } else {
    map->set_value(reg);
  }
}

// creates a HotSpot oop map out of the byte arrays provided by DebugInfo
static OopMap* create_oop_map(jint total_frame_size, jint parameter_count, oop debug_info) {
  OopMap* map = new OopMap(total_frame_size, parameter_count);
  oop reference_map = DebugInfo::referenceMap(debug_info);
  oop register_map = HotSpotReferenceMap::registerRefMap(reference_map);
  oop frame_map = HotSpotReferenceMap::frameRefMap(reference_map);
  oop callee_save_info = (oop) DebugInfo::calleeSaveInfo(debug_info);

  if (register_map != NULL) {
    for (jint i = 0; i < RegisterImpl::number_of_registers; i++) {
      set_vmreg_oops(map, as_Register(i)->as_VMReg(), register_map, i);
    }
#ifdef TARGET_ARCH_x86
    for (jint i = 0; i < XMMRegisterImpl::number_of_registers; i++) {
      VMReg reg = as_XMMRegister(i)->as_VMReg();
      int idx = RegisterImpl::number_of_registers + 4 * i;
      for (jint j = 0; j < 4; j++) {
        set_vmreg_oops(map, reg->next(2 * j), register_map, idx + j);
      }
    }
#endif
  }

  for (jint i = 0; i < bitset_size(frame_map) / 3; i++) {
    // HotSpot stack slots are 4 bytes
    VMReg reg = VMRegImpl::stack2reg(i * VMRegImpl::slots_per_word);
    set_vmreg_oops(map, reg, frame_map, i);
  }

  if (callee_save_info != NULL) {
    objArrayOop registers = (objArrayOop) RegisterSaveLayout::registers(callee_save_info);
    arrayOop slots = (arrayOop) RegisterSaveLayout::slots(callee_save_info);
    for (jint i = 0; i < slots->length(); i++) {
      oop graal_reg = registers->obj_at(i);
      jint graal_reg_number = code_Register::number(graal_reg);
      VMReg hotspot_reg = get_hotspot_reg(graal_reg_number);
      // HotSpot stack slots are 4 bytes
      jint graal_slot = ((jint*) slots->base(T_INT))[i];
      jint hotspot_slot = graal_slot * VMRegImpl::slots_per_word;
      VMReg hotspot_slot_as_reg = VMRegImpl::stack2reg(hotspot_slot);
      map->set_callee_saved(hotspot_slot_as_reg, hotspot_reg);
#ifdef _LP64
      // (copied from generate_oop_map() in c1_Runtime1_x86.cpp)
      VMReg hotspot_slot_hi_as_reg = VMRegImpl::stack2reg(hotspot_slot + 1);
      map->set_callee_saved(hotspot_slot_hi_as_reg, hotspot_reg->next());
#endif
    }
  }

  return map;
}

static void record_metadata_reference(oop obj, jlong prim, bool compressed, OopRecorder* oop_recorder) {
  if (obj->is_a(HotSpotResolvedObjectType::klass())) {
    Klass* klass = java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaClass(obj));
    if (compressed) {
      assert(Klass::decode_klass((narrowKlass) prim) == klass, err_msg("%s @ %p != %p", klass->name()->as_C_string(), klass, prim));
    } else {
      assert((Klass*) prim == klass, err_msg("%s @ %p != %p", klass->name()->as_C_string(), klass, prim));
    }
    int index = oop_recorder->find_index(klass);
    TRACE_graal_3("metadata[%d of %d] = %s", index, oop_recorder->metadata_count(), klass->name()->as_C_string());
  } else if (obj->is_a(HotSpotResolvedJavaMethod::klass())) {
    Method* method = (Method*) (address) HotSpotResolvedJavaMethod::metaspaceMethod(obj);
    assert(!compressed, err_msg("unexpected compressed method pointer %s @ %p = %p", method->name()->as_C_string(), method, prim));
    int index = oop_recorder->find_index(method);
    TRACE_graal_3("metadata[%d of %d] = %s", index, oop_recorder->metadata_count(), method->name()->as_C_string());
  } else {
    assert(java_lang_String::is_instance(obj),
        err_msg("unexpected metadata reference (%s) for constant %ld (%p)", obj->klass()->name()->as_C_string(), prim, prim));
  }
}

// Records any Metadata values embedded in a Constant (e.g., the value returned by HotSpotResolvedObjectType.klass()).
static void record_metadata_in_constant(oop constant, OopRecorder* oop_recorder) {
  if (constant->is_a(HotSpotMetaspaceConstant::klass())) {
    oop obj = HotSpotMetaspaceConstant::metaspaceObject(constant);
    jlong prim = HotSpotMetaspaceConstant::primitive(constant);
    assert(Kind::typeChar(Constant::kind(constant)) == 'j', "must have word kind");
    assert(obj != NULL, "must have an object");
    assert(prim != 0, "must have a primitive value");

    record_metadata_reference(obj, prim, false, oop_recorder);
  }
}

static void record_metadata_in_patch(oop data, OopRecorder* oop_recorder) {
  record_metadata_reference(MetaspaceData::annotation(data), MetaspaceData::value(data), MetaspaceData::compressed(data) != 0, oop_recorder);
}

ScopeValue* CodeInstaller::get_scope_value(oop value, int total_frame_size, GrowableArray<ScopeValue*>* objects, ScopeValue* &second, OopRecorder* oop_recorder) {
  second = NULL;
  if (value == Value::ILLEGAL()) {
    return new LocationValue(Location::new_stk_loc(Location::invalid, 0));
  }

  oop lirKind = Value::lirKind(value);
  oop platformKind = LIRKind::platformKind(lirKind);
  jint referenceMask = LIRKind::referenceMask(lirKind);
  assert(referenceMask == 0 || referenceMask == 1, "unexpected referenceMask");
  bool reference = referenceMask == 1;

  BasicType type = GraalRuntime::kindToBasicType(Kind::typeChar(platformKind));

  if (value->is_a(RegisterValue::klass())) {
    jint number = code_Register::number(RegisterValue::reg(value));
    if (number < RegisterImpl::number_of_registers) {
      Location::Type locationType;
      if (type == T_INT) {
        locationType = reference ? Location::narrowoop : Location::int_in_long;
      } else if (type == T_FLOAT) {
        locationType = Location::int_in_long;
      } else if (type == T_LONG) {
        locationType = reference ? Location::oop : Location::lng;
      } else {
        assert(type == T_OBJECT && reference, "unexpected type in cpu register");
        locationType = Location::oop;
      }
      ScopeValue* value = new LocationValue(Location::new_reg_loc(locationType, as_Register(number)->as_VMReg()));
      if (type == T_LONG && !reference) {
        second = value;
      }
      return value;
    } else {
      assert(type == T_FLOAT || type == T_DOUBLE, "only float and double expected in xmm register");
      Location::Type locationType;
      if (type == T_FLOAT) {
        // this seems weird, but the same value is used in c1_LinearScan
        locationType = Location::normal;
      } else {
        locationType = Location::dbl;
      }
#ifdef TARGET_ARCH_x86
      ScopeValue* value = new LocationValue(Location::new_reg_loc(locationType, as_XMMRegister(number - 16)->as_VMReg()));
      if (type == T_DOUBLE && !reference) {
        second = value;
      }
      return value;
#else
#ifdef TARGET_ARCH_sparc
      ScopeValue* value = new LocationValue(Location::new_reg_loc(locationType, as_FloatRegister(number)->as_VMReg()));
      if (type == T_DOUBLE && !reference) {
        second = value;
      }
      return value;
#else
      ShouldNotReachHere("Platform currently does not support floating point values.");
#endif
#endif
    }
  } else if (value->is_a(StackSlot::klass())) {
      Location::Type locationType;
    if (type == T_LONG) {
      locationType = reference ? Location::oop : Location::lng;
    } else if (type == T_INT) {
      locationType = reference ? Location::narrowoop : Location::normal;
    } else if (type == T_FLOAT) {
      locationType = Location::normal;
    } else if (type == T_DOUBLE) {
      locationType = Location::dbl;
    } else {
      assert(type == T_OBJECT && reference, "unexpected type in stack slot");
      locationType = Location::oop;
    }
    jint offset = StackSlot::offset(value);
    if (StackSlot::addFrameSize(value)) {
      offset += total_frame_size;
    }
    ScopeValue* value = new LocationValue(Location::new_stk_loc(locationType, offset));
    if (type == T_DOUBLE || (type == T_LONG && !reference)) {
      second = value;
    }
    return value;
  } else if (value->is_a(Constant::klass())){
    record_metadata_in_constant(value, oop_recorder);
    if (value->is_a(PrimitiveConstant::klass())) {
      assert(!reference, "unexpected primitive constant type");
      if (type == T_INT || type == T_FLOAT) {
        jint prim = (jint)PrimitiveConstant::primitive(value);
        return new ConstantIntValue(prim);
      } else {
        assert(type == T_LONG || type == T_DOUBLE, "unexpected primitive constant type");
        jlong prim = PrimitiveConstant::primitive(value);
        second = new ConstantIntValue(0);
        return new ConstantLongValue(prim);
      }
    } else {
        assert(reference, "unexpected object constant type");
      if (value->is_a(NullConstant::klass())) {
        return new ConstantOopWriteValue(NULL);
      } else {
        assert(value->is_a(HotSpotObjectConstant::klass()), "unexpected constant type");
        oop obj = HotSpotObjectConstant::object(value);
        assert(obj != NULL, "null value must be in NullConstant");
        return new ConstantOopWriteValue(JNIHandles::make_local(obj));
      }
    }
  } else if (value->is_a(VirtualObject::klass())) {
    oop type = VirtualObject::type(value);
    int id = VirtualObject::id(value);
    oop javaMirror = HotSpotResolvedObjectType::javaClass(type);
    Klass* klass = java_lang_Class::as_Klass(javaMirror);
    bool isLongArray = klass == Universe::longArrayKlassObj();

    for (jint i = 0; i < objects->length(); i++) {
      ObjectValue* obj = (ObjectValue*) objects->at(i);
      if (obj->id() == id) {
        return obj;
      }
    }

    ObjectValue* sv = new ObjectValue(id, new ConstantOopWriteValue(JNIHandles::make_local(Thread::current(), javaMirror)));
    objects->append(sv);

    arrayOop values = (arrayOop) VirtualObject::values(value);
    for (jint i = 0; i < values->length(); i++) {
      ScopeValue* cur_second = NULL;
      oop object = ((objArrayOop) (values))->obj_at(i);
      ScopeValue* value = get_scope_value(object, total_frame_size, objects, cur_second, oop_recorder);

      if (isLongArray && cur_second == NULL) {
        // we're trying to put ints into a long array... this isn't really valid, but it's used for some optimizations.
        // add an int 0 constant
#ifdef VM_LITTLE_ENDIAN
        cur_second = new ConstantIntValue(0);
#else
        cur_second = value;
        value = new ConstantIntValue(0);
#endif
      }

      if (cur_second != NULL) {
        sv->field_values()->append(cur_second);
      }
      sv->field_values()->append(value);
    }
    return sv;
  } else {
    value->klass()->print();
    value->print();
  }
  ShouldNotReachHere();
  return NULL;
}

MonitorValue* CodeInstaller::get_monitor_value(oop value, int total_frame_size, GrowableArray<ScopeValue*>* objects, OopRecorder* oop_recorder) {
  guarantee(value->is_a(HotSpotMonitorValue::klass()), "Monitors must be of type MonitorValue");

  ScopeValue* second = NULL;
  ScopeValue* owner_value = get_scope_value(HotSpotMonitorValue::owner(value), total_frame_size, objects, second, oop_recorder);
  assert(second == NULL, "monitor cannot occupy two stack slots");

  ScopeValue* lock_data_value = get_scope_value(HotSpotMonitorValue::slot(value), total_frame_size, objects, second, oop_recorder);
  assert(second == lock_data_value, "monitor is LONG value that occupies two stack slots");
  assert(lock_data_value->is_location(), "invalid monitor location");
  Location lock_data_loc = ((LocationValue*)lock_data_value)->location();

  bool eliminated = false;
  if (HotSpotMonitorValue::eliminated(value)) {
    eliminated = true;
  }

  return new MonitorValue(owner_value, lock_data_loc, eliminated);
}

void CodeInstaller::initialize_assumptions(oop compiled_code) {
  _oop_recorder = new OopRecorder(&_arena);
  _dependencies = new Dependencies(&_arena, _oop_recorder);
  Handle assumptions_handle = CompilationResult::assumptions(HotSpotCompiledCode::comp(compiled_code));
  if (!assumptions_handle.is_null()) {
    objArrayHandle assumptions(Thread::current(), (objArrayOop)Assumptions::list(assumptions_handle()));
    int length = assumptions->length();
    for (int i = 0; i < length; ++i) {
      Handle assumption = assumptions->obj_at(i);
      if (!assumption.is_null()) {
        if (assumption->klass() == Assumptions_MethodContents::klass()) {
          assumption_MethodContents(assumption);
        } else if (assumption->klass() == Assumptions_NoFinalizableSubclass::klass()) {
          assumption_NoFinalizableSubclass(assumption);
        } else if (assumption->klass() == Assumptions_ConcreteSubtype::klass()) {
          assumption_ConcreteSubtype(assumption);
        } else if (assumption->klass() == Assumptions_ConcreteMethod::klass()) {
          assumption_ConcreteMethod(assumption);
        } else if (assumption->klass() == Assumptions_CallSiteTargetValue::klass()) {
          assumption_CallSiteTargetValue(assumption);
        } else {
          assumption->print();
          fatal("unexpected Assumption subclass");
        }
      }
    }
  }
}

// constructor used to create a method
GraalEnv::CodeInstallResult CodeInstaller::install(Handle& compiled_code, CodeBlob*& cb, Handle installed_code, Handle speculation_log) {
  BufferBlob* buffer_blob = GraalRuntime::initialize_buffer_blob();
  if (buffer_blob == NULL) {
    return GraalEnv::cache_full;
  }

  CodeBuffer buffer(buffer_blob);
  jobject compiled_code_obj = JNIHandles::make_local(compiled_code());
  initialize_assumptions(JNIHandles::resolve(compiled_code_obj));

  // Get instructions and constants CodeSections early because we need it.
  _instructions = buffer.insts();
  _constants = buffer.consts();

  {
    No_Safepoint_Verifier no_safepoint;
    initialize_fields(JNIHandles::resolve(compiled_code_obj));
    if (!initialize_buffer(buffer)) {
      return GraalEnv::code_too_large;
    }
    process_exception_handlers();
  }

  int stack_slots = _total_frame_size / HeapWordSize; // conversion to words

  GraalEnv::CodeInstallResult result;
  if (compiled_code->is_a(HotSpotCompiledRuntimeStub::klass())) {
    oop stubName = HotSpotCompiledRuntimeStub::stubName(compiled_code);
    char* name = strdup(java_lang_String::as_utf8_string(stubName));
    cb = RuntimeStub::new_runtime_stub(name,
                                       &buffer,
                                       CodeOffsets::frame_never_safe,
                                       stack_slots,
                                       _debug_recorder->_oopmaps,
                                       false);
    result = GraalEnv::ok;
  } else {
    nmethod* nm = NULL;
    methodHandle method = getMethodFromHotSpotMethod(HotSpotCompiledNmethod::method(compiled_code));
    jint entry_bci = HotSpotCompiledNmethod::entryBCI(compiled_code);
    jint id = HotSpotCompiledNmethod::id(compiled_code);
    CompileTask* task = (CompileTask*) (address) HotSpotCompiledNmethod::ctask(compiled_code);
    if (id == -1) {
      // Make sure a valid compile_id is associated with every compile
      id = CompileBroker::assign_compile_id_unlocked(Thread::current(), method, entry_bci);
    }
    result = GraalEnv::register_method(method, nm, entry_bci, &_offsets, _custom_stack_area_offset, &buffer, stack_slots, _debug_recorder->_oopmaps, &_exception_handler_table,
        GraalCompiler::instance(), _debug_recorder, _dependencies, task, id, false, installed_code, speculation_log);
    cb = nm;
  }

  if (cb != NULL) {
    // Make sure the pre-calculated constants section size was correct.
    guarantee((cb->code_begin() - cb->content_begin()) == _constants_size, err_msg("%d != %d", cb->code_begin() - cb->content_begin(), _constants_size));
  }
  return result;
}

void CodeInstaller::initialize_fields(oop compiled_code) {
  oop comp_result = HotSpotCompiledCode::comp(compiled_code);
  if (compiled_code->is_a(HotSpotCompiledNmethod::klass())) {
    oop hotspotJavaMethod = HotSpotCompiledNmethod::method(compiled_code);
    methodHandle method = getMethodFromHotSpotMethod(hotspotJavaMethod);
    _parameter_count = method->size_of_parameters();
    TRACE_graal_1("installing code for %s", method->name_and_sig_as_C_string());
  } else {
    assert(compiled_code->is_a(HotSpotCompiledRuntimeStub::klass()), "CCE");
    // TODO (ds) not sure if this is correct - only used in OopMap constructor for non-product builds
    _parameter_count = 0;
  }
  _sites = (arrayOop) HotSpotCompiledCode::sites(compiled_code);
  _exception_handlers = (arrayOop) HotSpotCompiledCode::exceptionHandlers(compiled_code);

  _code = (arrayOop) CompilationResult::targetCode(comp_result);
  _code_size = CompilationResult::targetCodeSize(comp_result);
  _total_frame_size = CompilationResult::totalFrameSize(comp_result);
  _custom_stack_area_offset = CompilationResult::customStackAreaOffset(comp_result);

  // Pre-calculate the constants section size.  This is required for PC-relative addressing.
  _dataSection = HotSpotCompiledCode::dataSection(compiled_code);
  guarantee(DataSection::sectionAlignment(_dataSection) <= _constants->alignment(), "Alignment inside constants section is restricted by alignment of section begin");
  arrayOop data = (arrayOop) DataSection::data(_dataSection);
  _constants_size = data->length();
  if (_constants_size > 0) {
    _constants_size = align_size_up(_constants_size, _constants->alignment());
  }

#ifndef PRODUCT
  _comments = (arrayOop) HotSpotCompiledCode::comments(compiled_code);
#endif

  _next_call_type = INVOKE_INVALID;
}

int CodeInstaller::estimate_stub_entries() {
  // Estimate the number of static call stubs that might be emitted.
  int static_call_stubs = 0;
  for (int i = 0; i < _sites->length(); i++) {
    oop site = ((objArrayOop) (_sites))->obj_at(i);
    if (site->is_a(CompilationResult_Mark::klass())) {
      oop id_obj = CompilationResult_Mark::id(site);
      if (id_obj != NULL) {
        assert(java_lang_boxing_object::is_instance(id_obj, T_INT), "Integer id expected");
        jint id = id_obj->int_field(java_lang_boxing_object::value_offset_in_bytes(T_INT));
        if (id == INVOKESTATIC || id == INVOKESPECIAL) {
          static_call_stubs++;
        }
      }
    }
  }
  return static_call_stubs;
}

// perform data and call relocation on the CodeBuffer
bool CodeInstaller::initialize_buffer(CodeBuffer& buffer) {
  int locs_buffer_size = _sites->length() * (relocInfo::length_limit + sizeof(relocInfo));
  char* locs_buffer = NEW_RESOURCE_ARRAY(char, locs_buffer_size);
  buffer.insts()->initialize_shared_locs((relocInfo*)locs_buffer, locs_buffer_size / sizeof(relocInfo));
  // Allocate enough space in the stub section for the static call
  // stubs.  Stubs have extra relocs but they are managed by the stub
  // section itself so they don't need to be accounted for in the
  // locs_buffer above.
  buffer.initialize_stubs_size(estimate_stub_entries() * CompiledStaticCall::to_interp_stub_size());
  buffer.initialize_consts_size(_constants_size);

  _debug_recorder = new DebugInformationRecorder(_oop_recorder);
  _debug_recorder->set_oopmaps(new OopMapSet());

  buffer.initialize_oop_recorder(_oop_recorder);

  // copy the code into the newly created CodeBuffer
  address end_pc = _instructions->start() + _code_size;
  if (!_instructions->allocates2(end_pc)) {
    return false;
  }
  memcpy(_instructions->start(), _code->base(T_BYTE), _code_size);
  _instructions->set_end(end_pc);

  // copy the constant data into the newly created CodeBuffer
  address end_data = _constants->start() + _constants_size;
  arrayOop data = (arrayOop) DataSection::data(_dataSection);
  memcpy(_constants->start(), data->base(T_BYTE), data->length());
  _constants->set_end(end_data);

  objArrayOop patches = (objArrayOop) DataSection::patches(_dataSection);
  for (int i = 0; i < patches->length(); i++) {
    oop patch = patches->obj_at(i);
    oop data = CompilationResult_DataPatch::data(patch);
    if (data->is_a(MetaspaceData::klass())) {
      record_metadata_in_patch(data, _oop_recorder);
    } else if (data->is_a(OopData::klass())) {
      Handle obj = OopData::object(data);
      jobject value = JNIHandles::make_local(obj());
      int oop_index = _oop_recorder->find_index(value);

      address dest = _constants->start() + CompilationResult_Site::pcOffset(patch);
      assert(!OopData::compressed(data), err_msg("unexpected compressed oop in data section"));
      _constants->relocate(dest, oop_Relocation::spec(oop_index));
    } else {
      ShouldNotReachHere();
    }
  }
  jint last_pc_offset = -1;
  for (int i = 0; i < _sites->length(); i++) {
    oop site = ((objArrayOop) (_sites))->obj_at(i);
    jint pc_offset = CompilationResult_Site::pcOffset(site);

    if (site->is_a(CompilationResult_Call::klass())) {
      TRACE_graal_4("call at %i", pc_offset);
      site_Call(buffer, pc_offset, site);
    } else if (site->is_a(CompilationResult_Infopoint::klass())) {
      // three reasons for infopoints denote actual safepoints
      oop reason = CompilationResult_Infopoint::reason(site);
      if (InfopointReason::SAFEPOINT() == reason || InfopointReason::CALL() == reason || InfopointReason::IMPLICIT_EXCEPTION() == reason) {
        TRACE_graal_4("safepoint at %i", pc_offset);
        site_Safepoint(buffer, pc_offset, site);
      } else {
        // if the infopoint is not an actual safepoint, it must have one of the other reasons
        // (safeguard against new safepoint types that require handling above)
        assert(InfopointReason::METHOD_START() == reason || InfopointReason::METHOD_END() == reason || InfopointReason::LINE_NUMBER() == reason, "");
        site_Infopoint(buffer, pc_offset, site);
      }
    } else if (site->is_a(CompilationResult_DataPatch::klass())) {
      TRACE_graal_4("datapatch at %i", pc_offset);
      site_DataPatch(buffer, pc_offset, site);
    } else if (site->is_a(CompilationResult_Mark::klass())) {
      TRACE_graal_4("mark at %i", pc_offset);
      site_Mark(buffer, pc_offset, site);
    } else {
      fatal("unexpected Site subclass");
    }
    last_pc_offset = pc_offset;
  }

#ifndef PRODUCT
  if (_comments != NULL) {
    for (int i = 0; i < _comments->length(); i++) {
      oop comment = ((objArrayOop) (_comments))->obj_at(i);
      assert(comment->is_a(HotSpotCompiledCode_Comment::klass()), "cce");
      jint offset = HotSpotCompiledCode_Comment::pcOffset(comment);
      char* text = java_lang_String::as_utf8_string(HotSpotCompiledCode_Comment::text(comment));
      buffer.block_comment(offset, text);
    }
  }
#endif
  return true;
}

void CodeInstaller::assumption_MethodContents(Handle assumption) {
  Handle method_handle = Assumptions_MethodContents::method(assumption());
  methodHandle method = getMethodFromHotSpotMethod(method_handle());
  _dependencies->assert_evol_method(method());
}

void CodeInstaller::assumption_NoFinalizableSubclass(Handle assumption) {
  Handle receiverType_handle = Assumptions_NoFinalizableSubclass::receiverType(assumption());
  Klass* receiverType = java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaClass(receiverType_handle));
  _dependencies->assert_has_no_finalizable_subclasses(receiverType);
}

void CodeInstaller::assumption_ConcreteSubtype(Handle assumption) {
  Handle context_handle = Assumptions_ConcreteSubtype::context(assumption());
  Handle subtype_handle = Assumptions_ConcreteSubtype::subtype(assumption());
  Klass* context = java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaClass(context_handle));
  Klass* subtype = java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaClass(subtype_handle));

  if (context != subtype) {
    assert(context->is_abstract(), "");
    _dependencies->assert_abstract_with_unique_concrete_subtype(context, subtype);
  } else {
    _dependencies->assert_leaf_type(subtype);
  }
}

void CodeInstaller::assumption_ConcreteMethod(Handle assumption) {
  Handle impl_handle = Assumptions_ConcreteMethod::impl(assumption());
  Handle context_handle = Assumptions_ConcreteMethod::context(assumption());

  methodHandle impl = getMethodFromHotSpotMethod(impl_handle());
  Klass* context = java_lang_Class::as_Klass(HotSpotResolvedObjectType::javaClass(context_handle));

  _dependencies->assert_unique_concrete_method(context, impl());
}

void CodeInstaller::assumption_CallSiteTargetValue(Handle assumption) {
  Handle callSite = Assumptions_CallSiteTargetValue::callSite(assumption());
  Handle methodHandle = Assumptions_CallSiteTargetValue::methodHandle(assumption());

  _dependencies->assert_call_site_target_value(callSite(), methodHandle());
}

void CodeInstaller::process_exception_handlers() {
  // allocate some arrays for use by the collection code.
  const int num_handlers = 5;
  GrowableArray<intptr_t>* bcis = new GrowableArray<intptr_t> (num_handlers);
  GrowableArray<intptr_t>* scope_depths = new GrowableArray<intptr_t> (num_handlers);
  GrowableArray<intptr_t>* pcos = new GrowableArray<intptr_t> (num_handlers);

  if (_exception_handlers != NULL) {
    for (int i = 0; i < _exception_handlers->length(); i++) {
      oop exc=((objArrayOop) (_exception_handlers))->obj_at(i);
      jint pc_offset = CompilationResult_Site::pcOffset(exc);
      jint handler_offset = CompilationResult_ExceptionHandler::handlerPos(exc);

      // Subtable header
      _exception_handler_table.add_entry(HandlerTableEntry(1, pc_offset, 0));

      // Subtable entry
      _exception_handler_table.add_entry(HandlerTableEntry(-1, handler_offset, 0));
    }
  }
}

// If deoptimization happens, the interpreter should reexecute these bytecodes.
// This function mainly helps the compilers to set up the reexecute bit.
static bool bytecode_should_reexecute(Bytecodes::Code code) {
  switch (code) {
    case Bytecodes::_invokedynamic:
    case Bytecodes::_invokevirtual:
    case Bytecodes::_invokeinterface:
    case Bytecodes::_invokespecial:
    case Bytecodes::_invokestatic:
      return false;
    default:
      return true;
    }
  return true;
}

void CodeInstaller::record_scope(jint pc_offset, oop position, GrowableArray<ScopeValue*>* objects) {
  oop frame = NULL;
  if (position->is_a(BytecodeFrame::klass())) {
    frame = position;
  }
  oop caller_frame = BytecodePosition::caller(position);
  if (caller_frame != NULL) {
    record_scope(pc_offset, caller_frame, objects);
  }

  oop hotspot_method = BytecodePosition::method(position);
  Method* method = getMethodFromHotSpotMethod(hotspot_method);
  jint bci = BytecodePosition::bci(position);
  if (bci == BytecodeFrame::BEFORE_BCI()) {
    bci = SynchronizationEntryBCI;
  }

  if (TraceGraal >= 2) {
    tty->print_cr("Recording scope pc_offset=%d bci=%d method=%s", pc_offset, bci, method->name_and_sig_as_C_string());
  }

  bool reexecute = false;
  if (frame != NULL) {
    if (bci == SynchronizationEntryBCI){
       reexecute = false;
    } else {
      Bytecodes::Code code = Bytecodes::java_code_at(method, method->bcp_from(bci));
      reexecute = bytecode_should_reexecute(code);
      if (frame != NULL) {
        reexecute = (BytecodeFrame::duringCall(frame) == JNI_FALSE);
      }
    }
  }

  DebugToken* locals_token = NULL;
  DebugToken* expressions_token = NULL;
  DebugToken* monitors_token = NULL;
  bool throw_exception = false;

  if (frame != NULL) {
    jint local_count = BytecodeFrame::numLocals(frame);
    jint expression_count = BytecodeFrame::numStack(frame);
    jint monitor_count = BytecodeFrame::numLocks(frame);
    arrayOop values = (arrayOop) BytecodeFrame::values(frame);

    assert(local_count + expression_count + monitor_count == values->length(), "unexpected values length");

    GrowableArray<ScopeValue*>* locals = new GrowableArray<ScopeValue*> ();
    GrowableArray<ScopeValue*>* expressions = new GrowableArray<ScopeValue*> ();
    GrowableArray<MonitorValue*>* monitors = new GrowableArray<MonitorValue*> ();

    if (TraceGraal >= 2) {
      tty->print_cr("Scope at bci %d with %d values", bci, values->length());
      tty->print_cr("%d locals %d expressions, %d monitors", local_count, expression_count, monitor_count);
    }

    for (jint i = 0; i < values->length(); i++) {
      ScopeValue* second = NULL;
      oop value=((objArrayOop) (values))->obj_at(i);
      if (i < local_count) {
        ScopeValue* first = get_scope_value(value, _total_frame_size, objects, second, _oop_recorder);
        if (second != NULL) {
          locals->append(second);
        }
        locals->append(first);
      } else if (i < local_count + expression_count) {
        ScopeValue* first = get_scope_value(value, _total_frame_size, objects, second, _oop_recorder);
        if (second != NULL) {
          expressions->append(second);
        }
        expressions->append(first);
      } else {
        monitors->append(get_monitor_value(value, _total_frame_size, objects, _oop_recorder));
      }
      if (second != NULL) {
        i++;
        assert(i < values->length(), "double-slot value not followed by Value.ILLEGAL");
        assert(((objArrayOop) (values))->obj_at(i) == Value::ILLEGAL(), "double-slot value not followed by Value.ILLEGAL");
      }
    }

    _debug_recorder->dump_object_pool(objects);

    locals_token = _debug_recorder->create_scope_values(locals);
    expressions_token = _debug_recorder->create_scope_values(expressions);
    monitors_token = _debug_recorder->create_monitor_values(monitors);

    throw_exception = BytecodeFrame::rethrowException(frame) == JNI_TRUE;
  }

  _debug_recorder->describe_scope(pc_offset, method, NULL, bci, reexecute, throw_exception, false, false, locals_token, expressions_token, monitors_token);
}

void CodeInstaller::site_Safepoint(CodeBuffer& buffer, jint pc_offset, oop site) {
  oop debug_info = CompilationResult_Infopoint::debugInfo(site);
  assert(debug_info != NULL, "debug info expected");

  // address instruction = _instructions->start() + pc_offset;
  // jint next_pc_offset = Assembler::locate_next_instruction(instruction) - _instructions->start();
  _debug_recorder->add_safepoint(pc_offset, create_oop_map(_total_frame_size, _parameter_count, debug_info));

  oop frame = DebugInfo::bytecodePosition(debug_info);
  if (frame != NULL) {
    record_scope(pc_offset, frame, new GrowableArray<ScopeValue*>());
  } else {
    // Stubs do not record scope info, just oop maps
  }

  _debug_recorder->end_safepoint(pc_offset);
}

void CodeInstaller::site_Infopoint(CodeBuffer& buffer, jint pc_offset, oop site) {
  oop debug_info = CompilationResult_Infopoint::debugInfo(site);
  assert(debug_info != NULL, "debug info expected");

  _debug_recorder->add_non_safepoint(pc_offset);

  oop position = DebugInfo::bytecodePosition(debug_info);
  if (position != NULL) {
    record_scope(pc_offset, position, NULL);
  }

  _debug_recorder->end_non_safepoint(pc_offset);
}

void CodeInstaller::site_Call(CodeBuffer& buffer, jint pc_offset, oop site) {
  oop target = CompilationResult_Call::target(site);
  InstanceKlass* target_klass = InstanceKlass::cast(target->klass());

  oop hotspot_method = NULL; // JavaMethod
  oop foreign_call = NULL;

  if (target_klass->is_subclass_of(SystemDictionary::HotSpotForeignCallLinkage_klass())) {
    foreign_call = target;
  } else {
    hotspot_method = target;
  }

  oop debug_info = CompilationResult_Call::debugInfo(site);

  assert(!!hotspot_method ^ !!foreign_call, "Call site needs exactly one type");

  NativeInstruction* inst = nativeInstruction_at(_instructions->start() + pc_offset);
  jint next_pc_offset = CodeInstaller::pd_next_offset(inst, pc_offset, hotspot_method);
  
  if (debug_info != NULL) {
    oop frame = DebugInfo::bytecodePosition(debug_info);
    _debug_recorder->add_safepoint(next_pc_offset, create_oop_map(_total_frame_size, _parameter_count, debug_info));
    if (frame != NULL) {
      record_scope(next_pc_offset, frame, new GrowableArray<ScopeValue*>());
    } else {
      // Stubs do not record scope info, just oop maps
    }
  }

  if (foreign_call != NULL) {
    jlong foreign_call_destination = HotSpotForeignCallLinkage::address(foreign_call);
    CodeInstaller::pd_relocate_ForeignCall(inst, foreign_call_destination);
  } else { // method != NULL
    assert(hotspot_method != NULL, "unexpected JavaMethod");
    assert(debug_info != NULL, "debug info expected");

    TRACE_graal_3("method call");
    CodeInstaller::pd_relocate_JavaMethod(hotspot_method, pc_offset);
    if (_next_call_type == INVOKESTATIC || _next_call_type == INVOKESPECIAL) {
      // Need a static call stub for transitions from compiled to interpreted.
      CompiledStaticCall::emit_to_interp_stub(buffer, _instructions->start() + pc_offset);
    }
  }

  _next_call_type = INVOKE_INVALID;

  if (debug_info != NULL) {
    _debug_recorder->end_safepoint(next_pc_offset);
  }
}

void CodeInstaller::site_DataPatch(CodeBuffer& buffer, jint pc_offset, oop site) {
  oop data = CompilationResult_DataPatch::data(site);
  if (data->is_a(MetaspaceData::klass())) {
    record_metadata_in_patch(data, _oop_recorder);
  } else if (data->is_a(OopData::klass())) {
    pd_patch_OopData(pc_offset, data);
  } else if (data->is_a(DataSectionReference::klass())) {
    pd_patch_DataSectionReference(pc_offset, data);
  } else {
    fatal("unknown data patch type");
  }
}

void CodeInstaller::site_Mark(CodeBuffer& buffer, jint pc_offset, oop site) {
  oop id_obj = CompilationResult_Mark::id(site);

  if (id_obj != NULL) {
    assert(java_lang_boxing_object::is_instance(id_obj, T_INT), "Integer id expected");
    jint id = id_obj->int_field(java_lang_boxing_object::value_offset_in_bytes(T_INT));

    address pc = _instructions->start() + pc_offset;

    switch (id) {
      case UNVERIFIED_ENTRY:
        _offsets.set_value(CodeOffsets::Entry, pc_offset);
        break;
      case VERIFIED_ENTRY:
        _offsets.set_value(CodeOffsets::Verified_Entry, pc_offset);
        break;
      case OSR_ENTRY:
        _offsets.set_value(CodeOffsets::OSR_Entry, pc_offset);
        break;
      case EXCEPTION_HANDLER_ENTRY:
        _offsets.set_value(CodeOffsets::Exceptions, pc_offset);
        break;
      case DEOPT_HANDLER_ENTRY:
        _offsets.set_value(CodeOffsets::Deopt, pc_offset);
        break;
      case INVOKEVIRTUAL:
      case INVOKEINTERFACE:
      case INLINE_INVOKE:
      case INVOKESTATIC:
      case INVOKESPECIAL:
        _next_call_type = (MarkId) id;
        _invoke_mark_pc = pc;
        break;
      case POLL_NEAR:
      case POLL_FAR:
      case POLL_RETURN_NEAR:
      case POLL_RETURN_FAR:
        pd_relocate_poll(pc, id);
        break;
      default:
        ShouldNotReachHere();
        break;
    }
  }
}

