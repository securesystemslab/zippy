/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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
 *
 */

#ifndef SHARE_VM_RUNTIME_GPU_HPP
#define SHARE_VM_RUNTIME_GPU_HPP

#include "runtime/atomic.hpp"
#include "oops/symbol.hpp"
#include "utilities/array.hpp"

// Defines the interface to the graphics processor(s).
class gpu : AllStatic {
 private:
  static int _initialized_gpus;  // number of initialize GPU devices

 public:

  // Notification of a GPU device that has been initialized.
  static void initialized_gpu(const char* name);

  // Gets a comma separated list of supported GPU architecture names.
  static jobject probe_gpus(JNIEnv* env);
  
  // Gets the number of GPU devices that have been initialized.
  static int initialized_gpus() { return _initialized_gpus; }
};

#endif // SHARE_VM_RUNTIME_GPU_HPP
