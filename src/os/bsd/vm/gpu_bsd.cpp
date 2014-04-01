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

#include "runtime/gpu.hpp"
#include "utilities/ostream.hpp"

jobject gpu::probe_gpus(JNIEnv* env) {
#ifdef __APPLE__
  /*
   * Let the CUDA driver initialization be the gate to GPU for now, pending
   * a better detection solution for NVIDA PTX and AMD HSAIL.
   */
  if (gpu::Ptx::register_natives(env)) {
    if (TraceGPUInteraction) {
      tty->print_cr("Assuming NVidia/PTX support (APPLE)");
    }
    return env->NewStringUTF("PTX");
  }
#else
  if (TraceGPUInteraction) {
    tty->print_cr("Assuming no GPU (not APPLE)");
  }
#endif
  return env->NewStringUTF("");
}

