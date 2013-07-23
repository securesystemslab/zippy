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

// gpu defines the interface to the graphics processor; this includes traditional
// GPU services such as graphics kernel load and execute.


class gpu: AllStatic {
public:
  static void init(void);

  static void probe_gpu();

  static void probe_linkage();
  
  static void initialize_gpu();
  
  static void * generate_kernel(unsigned char *code, int code_len, const char *name);

  static bool execute_kernel(address kernel);

  static void set_available(bool value) {
    _available = value;
  }

  static bool is_available() { return _available; }

  static void set_initialized(bool value) {
    _initialized = value;
  }

  static bool is_initialized() { return _initialized; }

  static void set_gpu_linkage(bool value) {
    _gpu_linkage = value;
  }

  static bool has_gpu_linkage() { return _gpu_linkage; }

protected:
  static bool _available;
  static bool _gpu_linkage;
  static bool _initialized;

  // Platform dependent stuff
#ifdef TARGET_OS_FAMILY_linux
#endif
#ifdef TARGET_OS_FAMILY_solaris
#endif
#ifdef TARGET_OS_FAMILY_windows
#endif
#ifdef TARGET_OS_FAMILY_bsd
# include "gpu_bsd.hpp"
#endif

# include "ptx/gpu_ptx.hpp"

};


#endif // SHARE_VM_RUNTIME_GPU_HPP
