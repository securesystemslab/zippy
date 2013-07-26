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

#ifdef __APPLE__
#include <ApplicationServices/ApplicationServices.h>
#include <IOKit/IOKitLib.h>
#endif

void gpu::probe_gpu() {
#ifdef __APPLE__
  set_available(gpu::Bsd::probe_gpu_apple());
  if (TraceGPUInteraction) {
    tty->print_cr("gpu_bsd::probe_gpu(APPLE): %d", gpu::is_available());
  }
#else
  if (TraceGPUInteraction) {
    tty->print_cr("gpu_bsd::probe_gpu(not APPLE)");
  }
  set_available(false);
#endif
}

#ifdef __APPLE__
/*
 * This is rudimentary at best, but until we decide on a CUDA Compiler Compatibility
 * level, this will have to suffice.
 */
bool gpu::Bsd::probe_gpu_apple() {
  CGError             err = CGDisplayNoErr;
  CGDisplayCount      displayCount = 0;
  CFDataRef           vendorID, deviceID, model;
  CGDirectDisplayID   *displays;
  IOOptionBits        options = kIORegistryIterateRecursively | kIORegistryIterateParents;
  io_registry_entry_t displayPort;

  err = CGGetActiveDisplayList(0, NULL, &displayCount);
  displays = (CGDirectDisplayID *)calloc((size_t)displayCount, sizeof(CGDirectDisplayID));
  err = CGGetActiveDisplayList(displayCount, displays, &displayCount);

  for (CGDisplayCount i = 0; i < displayCount; i++) {
	displayPort = CGDisplayIOServicePort(displays[i]);
	vendorID = (CFDataRef)IORegistryEntrySearchCFProperty(displayPort, kIOServicePlane, CFSTR("vendor-id"),
                                               kCFAllocatorDefault, options);
	deviceID = (CFDataRef)IORegistryEntrySearchCFProperty(displayPort, kIOServicePlane, CFSTR("device-id"),
                                               kCFAllocatorDefault, options);
	model = (CFDataRef)IORegistryEntrySearchCFProperty(displayPort, kIOServicePlane, CFSTR("model"),
                                            kCFAllocatorDefault, options);
    if (TraceGPUInteraction) {
      tty->print_cr("vendor: 0x%08X", *((UInt32*)CFDataGetBytePtr(vendorID)));
      tty->print_cr("device: 0x%08X", *((UInt32*)CFDataGetBytePtr(deviceID)));
      tty->print_cr("model: %s", CFDataGetBytePtr(model));
    }
    UInt32 vendor = *((UInt32*)CFDataGetBytePtr(vendorID));
    if (vendor != 0x10DE) {
      return false;
    } else {
      /*
       * see https://developer.nvidia.com/cuda-gpus
       * see http://en.wikipedia.org/wiki/CUDA#Supported_GPUs
       * see http://www.pcidatabase.com/reports.php?type=csv
       *
       * Only supporting GK104, GK106, GK107 and GK110 GPUs for now,
       * which is CUDA Computer Capability 3.0 and greater.
       */
      switch (*((UInt32*)CFDataGetBytePtr(deviceID))) {
        case 0x11C0:
          return true;  // NVIDIA GeForce GTX 660
        default:
          return false;
      }
    }
  }
  return false;
}
#endif
