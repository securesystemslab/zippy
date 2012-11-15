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

#ifndef SHARE_VM_UTILITIES_MACHINE_CODE_PRINTER_HPP
#define SHARE_VM_UTILITIES_MACHINE_CODE_PRINTER_HPP

class MachineCodePrinter : public AllStatic {
private:
  static fileStream* _st;
  static volatile int _write_lock;

public:
  static void initialize();
  static void print(nmethod* nm);
  static void print(CodeBlob* cb);
  static void print(StubQueue* stub_queue);
  static void flush();

private:
  static void lock();
  static void unlock();
};

#endif // SHARE_VM_UTILITIES_MACHINE_CODE_PRINTER_HPP
