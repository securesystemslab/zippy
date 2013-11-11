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

#include "precompiled.hpp"
#include "runtime/gpu.hpp"
#include "utilities/ostream.hpp"

void gpu::probe_gpu() {
  set_available(gpu::Windows::probe_gpu());
  if (TraceGPUInteraction) {
    tty->print_cr("probe_gpu(): %d", gpu::is_available());
  }
}

bool gpu::Windows::probe_gpu() {
    
  /*
   * We will check the HSA environment in the libraries,
   * so nothing to do here.
   * The HSA library linkage is checked in a later step.
   */  
  bool gpu_device_exists = true;
  set_target_il_type(gpu::HSAIL);

  return gpu_device_exists;
}
