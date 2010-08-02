package com.sun.hotspot.c1x;

import java.lang.reflect.*;
import java.util.*;

import com.sun.cri.ci.*;
import com.sun.cri.ci.CiTargetMethod.*;
import com.sun.cri.ri.*;

public class HotSpotTargetMethod {

    public final Method method;
    private byte[] code;
    private int codeSize;
    private int frameSize;

    public int verifiedEntrypoint;
    public int unverifiedEntrypoint;

    public int relocationOffsets[];
    public Object relocationData[];

    private HotSpotTargetMethod(HotSpotVMConfig config, RiMethod method, CiTargetMethod targetMethod) {
        this.method= ((HotSpotMethod) method).method;
        code = targetMethod.targetCode();
        codeSize = targetMethod.targetCodeSize();
        frameSize = targetMethod.frameSize();
        verifiedEntrypoint = targetMethod.entrypointCodeOffsets.get(HotSpotRuntime.Entrypoints.VERIFIED);
        unverifiedEntrypoint = targetMethod.entrypointCodeOffsets.get(HotSpotRuntime.Entrypoints.UNVERIFIED);

        Map<Integer, Object> relocations = new TreeMap<Integer, Object>();
        if (!targetMethod.dataReferences.isEmpty()) {
            for (DataPatch patch : targetMethod.dataReferences) {
                if (patch.data.kind == CiKind.Object) {
                    if (patch.data.asObject() instanceof RiType) {
                        relocations.put(patch.pcOffset, patch.data.asObject());
                    } else {
                        throw new RuntimeException("unexpected data reference");
                    }
                }
            }
        }

        if (!targetMethod.directCalls.isEmpty()) {
            for (CiTargetMethod.Call call : targetMethod.directCalls) {
                if (call.globalStubID instanceof Long) {
                    relocations.put(call.pcOffset, (Long)call.globalStubID);
                } else if (call.globalStubID instanceof CiRuntimeCall) {
                    switch ((CiRuntimeCall) call.globalStubID) {
                        case Debug:
                            // relocations.put(call.pcOffset, config.debugStub);
                            System.out.println("debug call");
                            break;
                        case UnwindException:
                        case RegisterFinalizer:
                        case HandleException:
                        case OSRMigrationEnd:
                        case JavaTimeMillis:
                        case JavaTimeNanos:
                        case ArithmethicLrem:
                        case ArithmeticLdiv:
                        case ArithmeticFrem:
                        case ArithmeticDrem:
                        case ArithmeticCos:
                        case ArithmeticTan:
                        case ArithmeticLog:
                        case ArithmeticLog10:
                        case ArithmeticSin:
                        default:
                            throw new RuntimeException("unexpected runtime call: " + call.globalStubID);
                    }
                }
            }
        }
        relocationOffsets = new int[relocations.size()];
        relocationData = new Object[relocations.size()];
        int i=0;
        for( Map.Entry<Integer, Object> entry: relocations.entrySet()) {
            relocationOffsets[i] = entry.getKey();
            relocationData[i++] = entry.getValue();
        }
    }

    public static void installCode(HotSpotVMConfig config, RiMethod method, CiTargetMethod targetMethod) {
        Compiler.getVMEntries().installCode(new HotSpotTargetMethod(config, method, targetMethod));
    }

}
