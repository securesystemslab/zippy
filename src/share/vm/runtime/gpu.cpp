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
#include "runtime/handles.hpp"

int  Gpu::_initialized_gpus_count = 0;
Gpu* Gpu::_initialized_gpus[MAX_GPUS];

void Gpu::initialized_gpu(Gpu* gpu) {
  // GPUs are always initialized on the same thread so no need for locking
  guarantee(_initialized_gpus_count < MAX_GPUS, "oob");
  _initialized_gpus[_initialized_gpus_count++] = gpu;
  if (TraceGPUInteraction) {
    tty->print_cr("[GPU] registered initialization of %s (total initialized: %d)", gpu->name(), _initialized_gpus_count);
  }
}

void Gpu::safepoint_event(SafepointEvent event) {
  for (int i = 0; i < _initialized_gpus_count; i++) {
    if (event == SafepointBegin) {
      _initialized_gpus[i]->notice_safepoints();
    } else {
      _initialized_gpus[i]->ignore_safepoints();
    }
  }
}
