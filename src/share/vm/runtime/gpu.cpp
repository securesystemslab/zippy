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

bool gpu::_available = false;    // does the hardware exist?
bool gpu::_gpu_linkage = false;  // is the driver library to access the GPU installed
bool gpu::_initialized = false;  // is the GPU device initialized
gpu::TargetGPUIL gpu::_targetIL = gpu::NONE; // No GPU detected yet.

void gpu::init() {
#if defined(TARGET_OS_FAMILY_bsd) || defined(TARGET_OS_FAMILY_linux) || defined(TARGET_OS_FAMILY_windows)
  gpu::probe_gpu();
  if (gpu::get_target_il_type() == gpu::PTX) {
    set_gpu_linkage(gpu::Ptx::probe_linkage());
  } else if (gpu::get_target_il_type() == gpu::HSAIL) {
    set_gpu_linkage(gpu::Hsail::probe_linkage());
  } else {
    set_gpu_linkage(false);
  }
#endif
}

void gpu::initialize_gpu() {
  if (gpu::has_gpu_linkage()) {
    if (gpu::get_target_il_type() == gpu::PTX) {
      set_initialized(gpu::Ptx::initialize_gpu());
    } else if (gpu::get_target_il_type() == gpu::HSAIL) {
      set_initialized(gpu::Hsail::initialize_gpu());
    }
  }
}

void * gpu::generate_kernel(unsigned char *code, int code_len, const char *name) {
  if (gpu::has_gpu_linkage()) {
    if (gpu::get_target_il_type() == gpu::PTX) {
      return (gpu::Ptx::generate_kernel(code, code_len, name));
    } else if (gpu::get_target_il_type() == gpu::HSAIL) {
      return (gpu::Hsail::generate_kernel(code, code_len, name));
    }
  }
  return NULL;
}

bool gpu::execute_kernel(address kernel, PTXKernelArguments & ptxka, JavaValue& ret) {
    if (gpu::has_gpu_linkage()) {
        if (gpu::get_target_il_type() == gpu::PTX) {
            return (gpu::Ptx::execute_kernel(kernel, ptxka, ret));
        }
        // Add kernel execution functionality of other GPUs here
    }
    return false;
}

// This is HSAIL specific to work with Sumatra JDK
bool gpu::execute_kernel_void_1d(address kernel, int dimX, jobject args, methodHandle& mh) {
    if (gpu::has_gpu_linkage()) {
        if (gpu::get_target_il_type() == gpu::HSAIL) {
            return (gpu::Hsail::execute_kernel_void_1d(kernel, dimX, args, mh));
        }
    }
    return false;
    
}


bool gpu::execute_warp(int dimX, int dimY, int dimZ,
                       address kernel, PTXKernelArguments & ptxka, JavaValue& ret) {
    if (gpu::has_gpu_linkage()) {
        if (gpu::get_target_il_type() == gpu::PTX) {
            return (gpu::Ptx::execute_warp(dimX, dimY, dimZ, kernel, ptxka, ret));
        }
        // Add kernel execution functionality of other GPUs here
    }
    return false;
}

int gpu::available_processors() {
    if (gpu::has_gpu_linkage()) {
        if (gpu::get_target_il_type() == gpu::PTX) {
            return (gpu::Ptx::total_cores());
        }
        // Add kernel execution functionality of other GPUs here
    }
    return 0;
}

