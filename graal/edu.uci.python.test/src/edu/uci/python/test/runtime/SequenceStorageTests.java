/*
 * Copyright (c) 2013, Regents of the University of California
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
package edu.uci.python.test.runtime;

import static org.junit.Assert.*;

import org.junit.*;

import edu.uci.python.runtime.sequence.*;
import edu.uci.python.runtime.sequence.storage.*;

public class SequenceStorageTests {

    @Test
    public void objectsGetAndSet() {
        Object[] array = new Object[]{1, 2, 3, 4, 5, 6};
        ObjectSequenceStorage store = new ObjectSequenceStorage(array);
        assertEquals(4, store.getItemInBound(3));
        store.setItemInBound(5, 10);
        assertEquals(10, store.getItemInBound(5));
    }

    @Test
    public void objectsGetSlice() {
        Object[] array = new Object[]{1, 2, 3, 4, 5, 6};
        ObjectSequenceStorage store = new ObjectSequenceStorage(array);
        ObjectSequenceStorage slice = (ObjectSequenceStorage) store.getSliceInBound(1, 4, 1, 3);

        for (int i = 0; i < 3; i++) {
            assertEquals(i + 2, slice.getItemInBound(i));
        }
    }

    @Test
    public void objectsSetSlice() {
        Object[] array = new Object[]{1, 2, 3, 4, 5, 6};
        ObjectSequenceStorage store = new ObjectSequenceStorage(array);
        ObjectSequenceStorage slice = new ObjectSequenceStorage(new Object[]{42, 42, 42});

        store.setSliceInBound(1, 4, 1, slice);

        for (int i = 1; i < 4; i++) {
            assertEquals(42, store.getItemInBound(i));
        }
    }

    @Test
    public void objectsSetSliceOutOfBound() {
        Object[] array = new Object[]{1, 2, 3, 4, 5, 6};
        ObjectSequenceStorage store = new ObjectSequenceStorage(array);
        ObjectSequenceStorage slice = new ObjectSequenceStorage(new Object[]{42, 42, 42});

        store.setSliceInBound(5, 8, 1, slice);

        for (int i = 5; i < 8; i++) {
            assertEquals(42, store.getItemInBound(i));
        }
    }

    @Test
    public void objectsDel() {
        Object[] array = new Object[]{1, 2, 3, 4, 4, 5, 6};
        ObjectSequenceStorage store = new ObjectSequenceStorage(array);
        store.delItemInBound(4);

        for (int i = 0; i < store.length(); i++) {
            assertEquals(i + 1, store.getItemInBound(i));
        }
    }
}
