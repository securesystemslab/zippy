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

  jbyte* data_start() {return (jbyte*) this  + sizeof(*this); }
  int sreg_ofst_start() { return 0; }
  int dreg_ofst_start() { return sreg_ofst_start() + num_s_regs() * sizeof(jint); } 
  int stackslot_ofst_start() { return dreg_ofst_start() + num_d_regs() * sizeof(jlong); } 

  int sreg_ofst(int idx) {
    assert(idx >= 0 && idx < num_s_regs(), "bad sreg index");
    return sreg_ofst_start() + idx * sizeof(jint);
  }

  int dreg_ofst(int idx) {
    assert(idx >= 0 && idx < num_d_regs(), "bad dreg index");
    return dreg_ofst_start() + idx * sizeof(jlong);
  }

  int stackslot_ofst(int stackOffset) {
    assert(stackOffset >= 0 && (unsigned int) stackOffset < num_stack_slots() * sizeof(jlong), "bad stackoffset");
    return stackslot_ofst_start() + stackOffset;
  }

  // the _ptr versions just return a pointer to the indicated d reg or stackslot64
  // some of these are used for oops_do processing
  jint* get_s_reg_ptr(int idx) {
    return((jint*) (data_start() + sreg_ofst(idx)));
  }

  jlong* get_d_reg_ptr(int idx) {
    return((jlong*) (data_start() + dreg_ofst(idx)));
  }

  jlong* get_stackslot64_ptr(int stackOffset) {
    return((jlong*) (data_start() + stackslot_ofst(stackOffset)));
  }

  jint* get_stackslot32_ptr(int stackOffset) {
    return((jint*) (data_start() + stackslot_ofst(stackOffset)));
  }

  void* get_oop_ptr_for_bit(int bit) {
    void* oop_ptr;
    if (bit < num_d_regs()) {
      // d register
      oop_ptr = (void*) get_d_reg_ptr(bit);
    } else {
      // stack slot
      int stackOffset = (bit - num_d_regs()) * 8;  // 8 bytes per stack slot
      oop_ptr = (void*) get_stackslot64_ptr(stackOffset);
    }
    return oop_ptr;
  }

public:
  // Accessors
  jint pc_offset() { return _pc_offset; }
  jint num_s_regs() {return _num_s_regs; }
  jint num_d_regs() {return _num_d_regs; }
  jint num_stack_slots() {return _num_stack_slots; }

  jlong get_oop_for_bit(int bit) {
    return * (jlong *) get_oop_ptr_for_bit(bit);
  }
    
  // do the oops from this frame
  void oops_do(OopClosure* f, HSAILOopMapHelper* oopMapHelper) {
    int oops_per_deopt = num_d_regs() + num_stack_slots();

    // handle the dregister and stackSlot based oops
    for (int bit = 0; bit < oops_per_deopt; bit++) {
      if (oopMapHelper->is_oop(pc_offset(), bit)) {
        void* oop_ptr = get_oop_ptr_for_bit(bit);
        // the oops we are dealing with here in the hsailFrame are always uncompressed
        oop old_oop = oopDesc::load_heap_oop((oop *)oop_ptr);
        f->do_oop((oop*) oop_ptr);
        if (TraceGPUInteraction) {
          oop new_oop = oopDesc::load_heap_oop((oop *)oop_ptr);
          tty->print_cr("bit=%d, oop_ptr=%p, old=%p, new=%p", bit, oop_ptr, (void *)old_oop, (void *)new_oop);
        }
      }
    }
  }
};
  
#endif // GPU_HSAIL_VM_GPU_HSAIL_FRAME_HPP
