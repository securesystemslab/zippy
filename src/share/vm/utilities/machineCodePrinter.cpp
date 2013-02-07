/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
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
#include "code/stubs.hpp"
#include "compiler/disassembler.hpp"
#include "runtime/thread.hpp"
#include "utilities/machineCodePrinter.hpp"
#include "utilities/ostream.hpp"

fileStream* MachineCodePrinter::_st = NULL;
volatile int MachineCodePrinter::_write_lock = 0;

void MachineCodePrinter::initialize() {
  _st = new (ResourceObj::C_HEAP, mtInternal) fileStream("machineCode.txt");
}

void MachineCodePrinter::print(nmethod* nm) {
  lock();
  Disassembler::decode(nm, _st);
  unlock();
}

void MachineCodePrinter::print(CodeBlob* cb) {
  lock();
  Disassembler::decode(cb, _st);
  unlock();
}

void MachineCodePrinter::print(StubQueue* stub_queue) {
  lock();
  stub_queue->print_on(_st);
  unlock();
}

void MachineCodePrinter::flush() {
  _st->flush();
}

void MachineCodePrinter::lock() {
  Thread::SpinAcquire(&_write_lock, "Put");
}

void MachineCodePrinter::unlock() {
  Thread::SpinRelease(&_write_lock);
}
