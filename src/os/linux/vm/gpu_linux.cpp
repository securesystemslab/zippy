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
#include "ptx/vm/gpu_ptx.hpp"
#include "hsail/vm/gpu_hsail.hpp"
#include "utilities/ostream.hpp"

/*
 * Probe for CUDA device on PCI bus using /proc/bus/pci/devices. Do
 * not rely on CUDA tool kit being installed. We will check if CUDA
 * library is installed later.
 */

static unsigned int nvidia_vendor_id = 0x10de;
static unsigned int amd_vendor_id = 0x1002;

#define PCI_DRIVER_NAME_START_POS 255

jobject gpu::probe_gpus(JNIEnv* env) {
  bool hsail = false;
  bool ptx = false;

  if (Hsail::register_natives(env)) {
    hsail = true;
  }

  /*
   * Open /proc/bus/pci/devices to look for the first GPU device. For
   * now, we will just find the first GPU device. Will need to revisit
   * this to support execution on multiple GPU devices, if they exist.
   */
  FILE *pci_devices = fopen("/proc/bus/pci/devices", "r");
  char contents[4096];
  unsigned int bus_num_devfn_ign;
  unsigned int vendor;
  unsigned int device;
  const char *driver_name_string = "nvidia";
  const int driver_name_string_len = strlen(driver_name_string);

  if (pci_devices == NULL) {
    tty->print_cr("*** Failed to open /proc/bus/pci/devices");
    return NULL;
  }

  while (fgets(contents, sizeof(contents)-1, pci_devices)) {
    sscanf(contents, "%04x%04x%04x", &bus_num_devfn_ign, &vendor, &device);
    if (vendor == nvidia_vendor_id) {
      /* Check if this device is registered to be using nvidia driver */
      if (strncmp(&contents[PCI_DRIVER_NAME_START_POS],
                  driver_name_string, driver_name_string_len) == 0) {
        if (TraceGPUInteraction) {
          tty->print_cr("Found supported nVidia device [vendor=0x%04x, device=0x%04x]", vendor, device);
        }
        if (!ptx && Ptx::register_natives(env)) {
          ptx = true;
        }
      }
    }
  }

  // Close file pointer.
  fclose(pci_devices);

  const char* gpus = "";
  if (ptx && hsail) {
    gpus = "PTX,HSAIL";
  } else if (ptx) {
    gpus = "PTX";
  } else if (hsail) {
    gpus = "HSAIL";
  }
  return env->NewStringUTF(gpus);
}
