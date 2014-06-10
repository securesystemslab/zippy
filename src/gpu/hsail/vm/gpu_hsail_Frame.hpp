/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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

#ifndef GPU_HSAIL_VM_GPU_HSAIL_FRAME_HPP
#define GPU_HSAIL_VM_GPU_HSAIL_FRAME_HPP

#include "graal/graalEnv.hpp"
#include "code/debugInfo.hpp"
#include "code/location.hpp"

class HSAILFrame VALUE_OBJ_CLASS_SPEC {
  friend class VMStructs;
private:
  jint  _pc_offset;  // The HSAIL "pc_offset" where the exception happens
  jbyte _num_s_regs;
  jbyte _num_d_regs;
  jshort _num_stack_slots; 

public:
  // Accessors
  jint pc_offset() { return _pc_offset; }
  jint num_s_regs() {return _num_s_regs; }
  jint num_d_regs() {return _num_d_regs; }
  jint num_stack_slots() {return _num_stack_slots; }
  jbyte* data_start() {return (jbyte*) this  + sizeof(*this); }
  jlong get_d_reg(int idx) {
    int ofst = num_s_regs() * 4 + idx * 8;
    return(*(jlong*) (data_start() + ofst));
  }
  jint get_s_reg(int idx) {
    int ofst = idx * 4;
    return(*(jint*) (data_start() + ofst));
  }
  void put_d_reg(int idx, jlong val) {
    int ofst = num_s_regs() * 4 + idx * 8;
    (*(jlong*) (data_start() + ofst)) = val;
  }
  jint get_stackslot32(int stackOffset) {
    int ofst = num_s_regs() * 4 + num_d_regs() * 8 + stackOffset;
    return(*(jint*) (data_start() + ofst));
  }
  jlong get_stackslot64(int stackOffset) {
    int ofst = num_s_regs() * 4 + num_d_regs() * 8 + stackOffset;
    return(*(jlong*) (data_start() + ofst));
  }
  void put_stackslot64(int stackOffset, jlong val) {
    int ofst = num_s_regs() * 4 + num_d_regs() * 8 + stackOffset;
    (*(jlong*) (data_start() + ofst)) = val;
  }
};
  
#endif // GPU_HSAIL_VM_GPU_HSAIL_FRAME_HPP
