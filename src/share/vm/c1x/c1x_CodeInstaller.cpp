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
# include "incls/_c1x_CodeInstaller.cpp.incl"


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


// constructor used to create a method
CodeInstaller::CodeInstaller(oop target_method) {
  VM_ENTRY_MARK;
  _env = CURRENT_ENV;

  initialize_fields(target_method);
  assert(_hotspot_method != NULL && _name == NULL, "installMethod needs NON-NULL method and NULL name");


  // TODO: This is a hack.. Produce correct entries.
  _offsets.set_value(CodeOffsets::Exceptions, 0);
  _offsets.set_value(CodeOffsets::Deopt, 0);

  methodOop method = VmIds::get<methodOop>(HotSpotMethod::vmId(_hotspot_method));
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
CodeInstaller::CodeInstaller(oop target_method, jlong& id) {
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
  id = VmIds::addStub(blob->instructions_begin());
}

void CodeInstaller::initialize_fields(oop target_method) {
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

  _next_call_type = MARK_INVOKE_INVALID;
}

void CodeInstaller::site_Safepoint(CodeBuffer& buffer, jint pc_offset, oop site) {

}

void CodeInstaller::record_frame(jint pc_offset, oop code_pos, oop frame) {
  oop caller_pos = CiCodePos::caller(code_pos);
  if (caller_pos != NULL) {
    oop caller_frame = CiDebugInfo_Frame::caller(frame);
    record_frame(pc_offset, caller_pos, caller_frame);
  } else {
    assert(frame == NULL || CiDebugInfo_Frame::caller(frame) == NULL, "unexpected layout - different nesting of Frame and CiCodePos");
  }

  assert(frame == NULL || code_pos == CiDebugInfo_Frame::codePos(frame), "unexpected CiCodePos layout");

  oop hotspot_method = CiCodePos::method(code_pos);
  methodOop method = VmIds::get<methodOop>(HotSpotMethod::vmId(hotspot_method));
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

void CodeInstaller::site_Call(CodeBuffer& buffer, jint pc_offset, oop site) {
  oop runtime_call = CiTargetMethod_Call::runtimeCall(site);
  oop hotspot_method = CiTargetMethod_Call::method(site);
  oop symbol = CiTargetMethod_Call::symbol(site);
  oop global_stub = CiTargetMethod_Call::globalStubID(site);

  oop debug_info = CiTargetMethod_Call::debugInfo(site);
  arrayOop stack_map = (arrayOop)CiTargetMethod_Call::stackMap(site);
  arrayOop register_map = (arrayOop)CiTargetMethod_Call::registerMap(site);

  assert((runtime_call ? 1 : 0) + (hotspot_method ? 1 : 0) + (symbol ? 1 : 0) + (global_stub ? 1 : 0) == 1, "Call site needs exactly one type");

  address instruction = _instructions->start() + pc_offset;
  address operand = Assembler::locate_operand(instruction, Assembler::call32_operand);
  address next_instruction = Assembler::locate_next_instruction(instruction);

  if (runtime_call != NULL) {
    if (runtime_call == CiRuntimeCall::Debug()) {
      tty->print_cr("CiRuntimeCall::Debug()");
    } else {
      runtime_call->print();
    }
    tty->print_cr("runtime_call");
  } else if (global_stub != NULL) {
    assert(java_lang_boxing_object::is_instance(global_stub, T_LONG), "global_stub needs to be of type Long");

    jlong stub_id = global_stub->long_field(java_lang_boxing_object::value_offset_in_bytes(T_LONG));
    address dest = VmIds::getStub(stub_id);
    long disp = dest - next_instruction;
    assert(disp == (jint) disp, "disp doesn't fit in 32 bits");
    *((jint*)operand) = (jint)disp;

    _instructions->relocate(instruction, runtime_call_Relocation::spec(), Assembler::call32_operand);
    tty->print_cr("relocating (stub)  %016x/%016x", instruction, operand);
  } else if (symbol != NULL) {
    tty->print_cr("symbol");
  } else { // method != NULL
    assert(hotspot_method->is_a(SystemDictionary::HotSpotMethod_klass()), "unexpected RiMethod subclass");
    methodOop method = VmIds::get<methodOop>(HotSpotMethod::vmId(hotspot_method));

    jint next_pc_offset = next_instruction - _instructions->start();

    assert(debug_info != NULL, "debug info expected");
    _debug_recorder->add_safepoint(next_pc_offset, create_oop_map(_frame_size, _parameter_count, debug_info));
    oop code_pos = CiDebugInfo::codePos(debug_info);
    oop frame = CiDebugInfo::frame(debug_info);
    record_frame(next_pc_offset, code_pos, frame);

    switch(_next_call_type) {
      case MARK_INVOKEVIRTUAL:
      case MARK_INVOKEINTERFACE: {
        assert(!method->is_static(), "cannot call static method with invokeinterface");

        address dest = SharedRuntime::get_resolve_virtual_call_stub();
        long disp = dest - next_instruction;
        assert(disp == (jint) disp, "disp doesn't fit in 32 bits");
        *((jint*)operand) = (jint)disp;

        _instructions->relocate(instruction, virtual_call_Relocation::spec(_invoke_mark_pc), Assembler::call32_operand);
        break;
      }
      case MARK_INVOKESTATIC: {
        assert(method->is_static(), "cannot call non-static method with invokestatic");

        address dest = SharedRuntime::get_resolve_static_call_stub();
        long disp = dest - next_instruction;
        assert(disp == (jint) disp, "disp doesn't fit in 32 bits");
        *((jint*)operand) = (jint)disp;

        _instructions->relocate(instruction, relocInfo::static_call_type, Assembler::call32_operand);
        break;
      }
      case MARK_INVOKESPECIAL: {
        assert(!method->is_static(), "cannot call static method with invokespecial");

        address dest = SharedRuntime::get_resolve_opt_virtual_call_stub();
        long disp = dest - next_instruction;
        assert(disp == (jint) disp, "disp doesn't fit in 32 bits");
        *((jint*)operand) = (jint)disp;

        _instructions->relocate(instruction, relocInfo::opt_virtual_call_type, Assembler::call32_operand);
        break;
      }
      case MARK_INVOKE_INVALID:
      default:
        ShouldNotReachHere();
        break;
    }
    _next_call_type = MARK_INVOKE_INVALID;
    _debug_recorder->end_safepoint(pc_offset);
  }
}

void CodeInstaller::site_DataPatch(CodeBuffer& buffer, jint pc_offset, oop site) {
  oop constant = CiTargetMethod_DataPatch::constant(site);
  oop kind = CiConstant::kind(constant);

  address instruction = _instructions->start() + pc_offset;

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
      address operand = Assembler::locate_operand(instruction, Assembler::disp32_operand);
      address next_instruction = Assembler::locate_next_instruction(instruction);
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
    case 'a': {
      address operand = Assembler::locate_operand(instruction, Assembler::imm_operand);
      oop obj = CiConstant::object(constant);

      if (obj->is_a(HotSpotTypeResolved::klass())) {
        *((jobject*)operand) = JNIHandles::make_local(VmIds::get<klassOop>(HotSpotTypeResolved::vmId(obj)));
        _instructions->relocate(instruction, oop_Relocation::spec_for_immediate(), Assembler::imm_operand);
        tty->print_cr("relocating (HotSpotType) at %016x/%016x", instruction, operand);
      } else {
        assert(java_lang_boxing_object::is_instance(obj, T_LONG), "unexpected DataPatch object type");
        jlong id = obj->long_field(java_lang_boxing_object::value_offset_in_bytes(T_LONG));

        assert((id & VmIds::TYPE_MASK) == VmIds::CONSTANT, "unexpected DataPatch type");

        address operand = Assembler::locate_operand(instruction, Assembler::imm_operand);

        if (id == VmIds::DUMMY_CONSTANT) {
          *((jobject*)operand) = (jobject)Universe::non_oop_word();
        } else {
          *((jobject*)operand) = JNIHandles::make_local(VmIds::get<oop>(id));
        }
        _instructions->relocate(instruction, oop_Relocation::spec_for_immediate(), Assembler::imm_operand);
        tty->print_cr("relocating (oop constant) at %016x/%016x", instruction, operand);
      }
      break;
    }
    default:
      fatal("unexpected CiKind in DataPatch");
      break;
  }
}

void CodeInstaller::site_ExceptionHandler(CodeBuffer& buffer, jint pc_offset, oop site) {

}

void CodeInstaller::site_Mark(CodeBuffer& buffer, jint pc_offset, oop site) {
  oop id_obj = CiTargetMethod_Mark::id(site);
  arrayOop references = (arrayOop)CiTargetMethod_Mark::references(site);

  if (id_obj != NULL) {
    assert(java_lang_boxing_object::is_instance(id_obj, T_INT), "Integer id expected");
    jint id = id_obj->int_field(java_lang_boxing_object::value_offset_in_bytes(T_INT));

    address instruction = _instructions->start() + pc_offset;

    switch (id) {
      case MARK_UNVERIFIED_ENTRY:
        _offsets.set_value(CodeOffsets::Entry, pc_offset);
        break;
      case MARK_VERIFIED_ENTRY:
        _offsets.set_value(CodeOffsets::Verified_Entry, pc_offset);
        break;
      case MARK_OSR_ENTRY:
        _offsets.set_value(CodeOffsets::OSR_Entry, pc_offset);
        break;
      case MARK_STATIC_CALL_STUB: {
        assert(references->length() == 1, "static call stub needs one reference");
        oop ref = ((oop*)references->base(T_OBJECT))[0];
        address call_pc = _instructions->start() + CiTargetMethod_Site::pcOffset(ref);
        _instructions->relocate(instruction, static_stub_Relocation::spec(call_pc));
        break;
      }
      case MARK_INVOKE_INVALID:
      case MARK_INVOKEINTERFACE:
      case MARK_INVOKESTATIC:
      case MARK_INVOKESPECIAL:
      case MARK_INVOKEVIRTUAL:
        _next_call_type = (MarkId)id;
        _invoke_mark_pc = instruction;
        break;
      case MARK_IMPLICIT_NULL_EXCEPTION_TARGET:
        break;
      default:
        ShouldNotReachHere();
        break;
    }
  }
}

// perform data and call relocation on the CodeBuffer
void CodeInstaller::initialize_buffer(CodeBuffer& buffer) {
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
        switch (id & VmIds::TYPE_MASK) {
          case VmIds::CONSTANT: {
            address operand = Assembler::locate_operand(inst, Assembler::imm_operand);

            *((jobject*)operand) = JNIHandles::make_local(VmIds::get<oop>(id));
            instructions->relocate(inst, oop_Relocation::spec_for_immediate(), Assembler::imm_operand);
            tty->print_cr("relocating (HotSpotType) %02x at %016x/%016x", inst_byte, inst, operand);
            break;
          }
          case VmIds::STUB: {
            address operand = Assembler::locate_operand(inst, Assembler::call32_operand);

            long dest = (long)VmIds::getStub(id);
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

        *((jobject*)operand) = JNIHandles::make_local(VmIds::get<klassOop>(obj->obj_field(_types.HotSpotTypeResolved_klassOop)));
        instructions->relocate(inst, oop_Relocation::spec_for_immediate(), Assembler::imm_operand);
        tty->print_cr("relocating (HotSpotType) %02x at %016x/%016x", inst_byte, inst, operand);
      } else {
        tty->print_cr("unknown relocation type");
        obj->print();
      }
    }
  }*/
}

