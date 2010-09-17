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
# include "incls/_c1x_VmIds.cpp.incl"

// VmIds implementation

GrowableArray<address>* VmIds::_stubs = NULL;
GrowableArray<jobject>* VmIds::_localHandles = NULL;

void VmIds::initializeObjects() {
  if (_stubs == NULL) {
    assert(_localHandles == NULL, "inconsistent state");
    _stubs = new (ResourceObj::C_HEAP) GrowableArray<address> (64, true);
    _localHandles = new (ResourceObj::C_HEAP) GrowableArray<jobject> (64, true);
  }
  assert(_localHandles->length() == 0, "invalid state");
}

void VmIds::cleanupLocalObjects() {
  for (int i = 0; i < _localHandles->length(); i++) {
    JNIHandles::destroy_global(_localHandles->at(i));
  }
  _localHandles->clear();
}

jlong VmIds::addStub(address stub) {
  assert(!_stubs->contains(stub), "duplicate stub");
  return _stubs->append(stub) | STUB;
}

jlong VmIds::add(Handle obj, CompilerObjectType type) {
  assert(!obj.is_null(), "cannot add NULL handle");
  int idx = -1;
  for (int i = 0; i < _localHandles->length(); i++)
    if (JNIHandles::resolve_non_null(_localHandles->at(i)) == obj()) {
      idx = i;
      break;
    }
  if (idx == -1) {
    if (JavaThread::current()->thread_state() == _thread_in_vm) {
      idx = _localHandles->append(JNIHandles::make_global(obj));
    } else {
      VM_ENTRY_MARK;
      idx = _localHandles->append(JNIHandles::make_global(obj));
    }
  }
  return idx | type;
}

address VmIds::getStub(jlong id) {
  assert((id & TYPE_MASK) == STUB, "wrong id type, STUB expected");
  assert((id & ~TYPE_MASK) >= 0 && (id & ~TYPE_MASK) < _stubs->length(), "STUB index out of bounds");
  return _stubs->at(id & ~TYPE_MASK);
}

oop VmIds::getObject(jlong id) {
  assert((id & TYPE_MASK) != STUB, "wrong id type");
  assert((id & ~TYPE_MASK) >= 0 && (id & ~TYPE_MASK) < _localHandles->length(), "index out of bounds");
  return JNIHandles::resolve_non_null(_localHandles->at(id & ~TYPE_MASK));
}

