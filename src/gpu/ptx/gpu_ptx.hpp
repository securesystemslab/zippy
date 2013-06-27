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

#ifndef GPU_PTX_HPP
#define GPU_PTX_HPP

class Ptx {
  friend class gpu;

 protected:
  static void probe_linkage();
#ifdef __APPLE__
  static bool probe_linkage_apple();
#endif
  static bool initialize_gpu();
  static void * generate_kernel(unsigned char *code, int code_len, const char *name);
  
private:
  typedef int (*cuda_cu_init_func_t)(unsigned int, int);
  typedef int (*cuda_cu_ctx_create_func_t)(void *, int, int);
  typedef int (*cuda_cu_ctx_detach_func_t)(int *);
  typedef int (*cuda_cu_ctx_synchronize_func_t)(int *);
  typedef int (*cuda_cu_device_get_count_func_t)(int *);
  typedef int (*cuda_cu_device_get_name_func_t)(char *, int, int);
  typedef int (*cuda_cu_device_get_func_t)(int *, int);
  typedef int (*cuda_cu_device_compute_capability_func_t)(int *, int *, int);
  typedef int (*cuda_cu_launch_kernel_func_t)(int *, int *, int);
  typedef int (*cuda_cu_module_get_function_func_t)(void *, void *, const char *);
  typedef int (*cuda_cu_module_load_data_ex_func_t)(void *, void *, unsigned int, int *, void **);

  static cuda_cu_init_func_t                      _cuda_cu_init;
  static cuda_cu_ctx_create_func_t                _cuda_cu_ctx_create;
  static cuda_cu_ctx_detach_func_t                _cuda_cu_ctx_detach;
  static cuda_cu_ctx_synchronize_func_t           _cuda_cu_ctx_synchronize;
  static cuda_cu_device_get_count_func_t          _cuda_cu_device_get_count;
  static cuda_cu_device_get_name_func_t           _cuda_cu_device_get_name;
  static cuda_cu_device_get_func_t                _cuda_cu_device_get;
  static cuda_cu_device_compute_capability_func_t _cuda_cu_device_compute_capability;
  static cuda_cu_launch_kernel_func_t             _cuda_cu_launch_kernel;
  static cuda_cu_module_get_function_func_t       _cuda_cu_module_get_function;
  static cuda_cu_module_load_data_ex_func_t       _cuda_cu_module_load_data_ex;

protected:
  static void * _device_context;
};

#endif // GPU_PTX_HPP
