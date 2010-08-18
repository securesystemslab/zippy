/*
 * Copyright (c) 2010 Sun Microsystems, Inc. All rights reserved.
 *
 * Sun Microsystems, Inc. has intellectual property rights relating to technology embodied in the product that is
 * described in this document. In particular, and without limitation, these intellectual property rights may include one
 * or more of the U.S. patents listed at http://www.sun.com/patents and one or more additional patents or pending patent
 * applications in the U.S. and in other countries.
 *
 * U.S. Government Rights - Commercial software. Government users are subject to the Sun Microsystems, Inc. standard
 * license agreement and applicable provisions of the FAR and its supplements.
 *
 * Use is subject to license terms. Sun, Sun Microsystems, the Sun logo, Java and Solaris are trademarks or registered
 * trademarks of Sun Microsystems, Inc. in the U.S. and other countries. All SPARC trademarks are used under license and
 * are trademarks or registered trademarks of SPARC International, Inc. in the U.S. and other countries.
 *
 * UNIX is a registered trademark in the U.S. and other countries, exclusively licensed through X/Open Company, Ltd.
 */
package com.sun.hotspot.c1x;

import java.util.*;

import com.sun.cri.ci.*;
import com.sun.cri.ci.CiTargetMethod.*;

/**
 * CiTargetMethod augmented with HotSpot-specific information.
 *
 * @author Lukas Stadler
 */
public class HotSpotTargetMethod implements CompilerObject {

    public final CiTargetMethod targetMethod;
    public final HotSpotMethod method;                  // used only for methods
    public final String name;                           // used only for stubs

    public final Site[] sites;

    private HotSpotTargetMethod(HotSpotMethod method, CiTargetMethod targetMethod) {
        this.method = method;
        this.targetMethod = targetMethod;
        this.name = null;

        sites = getSortedSites(targetMethod);
    }

    private HotSpotTargetMethod(CiTargetMethod targetMethod, String name) {
        this.method = null;
        this.targetMethod = targetMethod;
        this.name = name;

        sites = getSortedSites(targetMethod);
    }

    private Site[] getSortedSites(CiTargetMethod target) {
        List<?>[] lists = new List<?>[] {target.directCalls, target.indirectCalls, target.safepoints, target.dataReferences, target.exceptionHandlers, target.marks};
        int count = 0;
        for (List<?> list : lists) {
            count += list.size();
        }
        Site[] result = new Site[count];
        int pos = 0;
        for (List<?> list : lists) {
            for (Object elem : list) {
                result[pos++] = (Site) elem;
            }
        }
        Arrays.sort(result, new Comparator<Site>() {

            public int compare(Site s1, Site s2) {
                if (s1.pcOffset == s2.pcOffset && (s1 instanceof Mark ^ s2 instanceof Mark)) {
                    return s1 instanceof Mark ? -1 : 1;
                }
                return s1.pcOffset - s2.pcOffset;
            }
        });
        for(Site site : result)
            System.out.println(site.pcOffset + ": " + site);
        return result;
    }

    public static void installMethod(HotSpotMethod method, CiTargetMethod targetMethod) {
        Compiler.getVMEntries().installMethod(new HotSpotTargetMethod(method, targetMethod));
    }

    public static Object installStub(CiTargetMethod targetMethod, String name) {
        return Compiler.getVMEntries().installStub(new HotSpotTargetMethod(targetMethod, name));
    }

}
