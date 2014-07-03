/*
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
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

#ifndef SHARE_VM_GRAAL_GRAALGLOBALS_HPP
#define SHARE_VM_GRAAL_GRAALGLOBALS_HPP

#include "runtime/globals.hpp"
#ifdef TARGET_ARCH_x86
# include "graalGlobals_x86.hpp"
#endif
#ifdef TARGET_ARCH_sparc
# include "graalGlobals_sparc.hpp"
#endif
#ifdef TARGET_ARCH_arm
# include "graalGlobals_arm.hpp"
#endif
#ifdef TARGET_ARCH_ppc
# include "graalGlobals_ppc.hpp"
#endif

//
// Defines all global flags used by the Graal compiler. Only flags that need
// to be accessible to the Graal C++ code should be defined here. All other
// Graal flags should be defined in GraalOptions.java.
//
#define GRAAL_FLAGS(develop, develop_pd, product, product_pd, notproduct)   \
                                                                            \
  product(bool, DebugGraal, true,                                           \
          "Enable JVMTI for the compiler thread")                           \
                                                                            \
  product(bool, UseGraalClassLoader, false,                                 \
          "Load Graal classes with separate class loader")                  \
                                                                            \
  COMPILERGRAAL_PRESENT(product(bool, BootstrapGraal, true,                 \
          "Bootstrap Graal before running Java main method"))               \
                                                                            \
  COMPILERGRAAL_PRESENT(product(bool, PrintBootstrap, true,                 \
          "Print Graal bootstrap progress and summary"))                    \
                                                                            \
  COMPILERGRAAL_PRESENT(product(intx, GraalThreads, 1,                      \
          "Force number of Graal compiler threads to use"))                 \
                                                                            \
  COMPILERGRAAL_PRESENT(product(bool, UseGraalCompilationQueue, false,      \
          "Use non-native compilation queue for Graal"))                    \
                                                                            \
  product(bool, ForceGraalInitialization, false,                            \
          "Force VM to initialize the compiler even if not used")           \
                                                                            \
  product(intx, TraceGraal, 0,                                              \
          "Trace level for Graal")                                          \
                                                                            \
  product(intx, GraalCounterSize, 0,                                        \
          "Reserved size for benchmark counters")                           \
                                                                            \
  product(bool, GraalCountersExcludeCompiler, true,                         \
          "Exclude Graal compiler threads from benchmark counters")         \
                                                                            \
  product(bool, GraalDeferredInitBarriers, true,                            \
          "Defer write barriers of young objects")                          \
                                                                            \
  product(bool, GraalHProfEnabled, false,                                   \
          "Is Heap  Profiler enabled")                                      \
                                                                            \
  develop(bool, GraalUseFastLocking, true,                                  \
          "Use fast inlined locking code")                                  \
                                                                            \
  develop(bool, GraalUseFastNewTypeArray, true,                             \
          "Use fast inlined type array allocation")                         \
                                                                            \
  develop(bool, GraalUseFastNewObjectArray, true,                           \
          "Use fast inlined object array allocation")                       \
                                                                            \
  product(intx, GraalNMethodSizeLimit, (80*K)*wordSize,                     \
          "Maximum size of a compiled method.")                             \
                                                                            \
  notproduct(bool, GraalPrintSimpleStubs, false,                            \
          "Print simple Graal stubs")                                       \
                                                                            \
  develop(bool, TraceUncollectedSpeculations, false,                        \
          "Print message when a failed speculation was not collected")      \
                                                                            \
  product(bool, UseHSAILDeoptimization, true,                               \
          "Code gen and runtime support for deoptimizing HSAIL kernels")    \
                                                                            \
  product(bool, UseHSAILSafepoints, true,                                   \
          "Code gen and runtime support for safepoints in HSAIL kernels")   \
                                                                            \
  product(bool, GPUOffload, false,                                          \
          "Offload execution to GPU whenever possible")                     \
                                                                            \
  product(bool, TraceGPUInteraction, false,                                 \
          "Trace external GPU Interaction")                                 \
                                                                            \


// Read default values for Graal globals

GRAAL_FLAGS(DECLARE_DEVELOPER_FLAG, DECLARE_PD_DEVELOPER_FLAG, DECLARE_PRODUCT_FLAG, DECLARE_PD_PRODUCT_FLAG, DECLARE_NOTPRODUCT_FLAG)

#endif // SHARE_VM_GRAAL_GRAALGLOBALS_HPP
