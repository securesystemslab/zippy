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

#ifndef GPU_HSAIL_VM_GPU_HSAIL_OOPMAPHELPER_HPP
#define GPU_HSAIL_VM_GPU_HSAIL_OOPMAPHELPER_HPP

#include "graal/graalEnv.hpp"
#include "code/debugInfo.hpp"
#include "code/location.hpp"

// Takes the jobject for the array of ints created by the java side
// and decodes the information based on pc_offset to find oops
class HSAILOopMapHelper : public StackObj {
private:
  jobject _oop_map_array_jobject;
  typeArrayOop _oop_map_array;
  int _last_pcoffset;
  int _last_idx;

  enum {
    SAVEAREACOUNTS_OFST=0,
    SPAN_OFST=1,
    HEADERSIZE=2
  }; 
  int mapPcOffsetToIndex(int pcOffset) {
    if (pcOffset == _last_pcoffset) {
      return _last_idx;
    }
    int span = _oop_map_array->int_at(SPAN_OFST);
    for (int idx = HEADERSIZE; idx < _oop_map_array->length(); idx += span) {
      int ofst = _oop_map_array->int_at(idx);
      if (ofst == pcOffset) {
        _last_pcoffset = pcOffset;
        _last_idx = idx + 1;
        return _last_idx;
      }
    }
    ShouldNotReachHere();
    return -1;
  }

public:
  HSAILOopMapHelper(jobject oop_map_array_jobject) {
    _oop_map_array_jobject = oop_map_array_jobject;
    _last_pcoffset = -1;
    resolve_arrays();
  }
 
  void resolve_arrays() {
    _oop_map_array = (typeArrayOop) JNIHandles::resolve(_oop_map_array_jobject);
  }

  static int get_save_area_counts(jobject oop_map_array_jobject) {
    typeArrayOop oop_map_array_resolved = (typeArrayOop) JNIHandles::resolve(oop_map_array_jobject);
    return oop_map_array_resolved->int_at(SAVEAREACOUNTS_OFST);
  }

  bool is_oop(int pcOffset, int bit){
    int bits_int_idx = mapPcOffsetToIndex(pcOffset) + (bit / 32);
    int bitpos = bit % 32;
    int bits = _oop_map_array->int_at(bits_int_idx);
    return ((bits & (1 << bitpos)) != 0);
  }

};

#endif // GPU_HSAIL_VM_GPU_HSAIL_OOPMAPHELPER_HPP
