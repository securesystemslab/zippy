/*
 * Copyright (c) 2015, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.uci.python.runtime.object;

import java.util.*;

public class ConservativeObjectLayout extends ObjectLayout {

    private final Class<?> storageClass;

    public ConservativeObjectLayout(String originHint, Class<?> storageClass) {
        super(originHint);
        this.storageClass = storageClass;
    }

    protected ConservativeObjectLayout(String originHint, ObjectLayout parent, Map<String, Class<?>> storageTypes, Class<?> objectStorageClass) {
        super(originHint, parent, storageTypes, objectStorageClass);
        this.storageClass = objectStorageClass;
    }

    public static final ConservativeObjectLayout empty(Class<?> storageClass) {
        return new ConservativeObjectLayout("(empty)", storageClass);
    }

    @Override
    protected ObjectLayout withNewAttribute(String name, Class<?> type) {
        final Map<String, Class<?>> storageTypes = getStorageTypes();
        storageTypes.put(name, type);
        return new ConservativeObjectLayout(getOriginHint() + "+" + name, getParentLayout(), storageTypes, storageClass);
    }

    @Override
    public ObjectLayout withGeneralisedVariable(String name) {
        final Map<String, Class<?>> storageTypes = getStorageTypes();
        storageTypes.put(name, Object.class);
        return new ConservativeObjectLayout(getOriginHint() + "!" + name, getParentLayout(), storageTypes, storageClass);
    }

}
