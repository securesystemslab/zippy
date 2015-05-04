package edu.uci.python.runtime.object;

import org.github.jamm.*;

public class PythonObjectAllocationInstrumentor {

    public static long FixedStorageAllocationSize = 0;
    public static long FlexibleStorageAllocationSize = 0;

    private long fixedObjectStorageSize = 0;
    private long flexibleObjectStorageSize = 0;

    private static PythonObjectAllocationInstrumentor INSTANCE;

    public static PythonObjectAllocationInstrumentor getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PythonObjectAllocationInstrumentor();
        }

        return INSTANCE;
    }

    private PythonObjectAllocationInstrumentor() {
    }

    public void instrumentFixed(FixedPythonObjectStorage obj) {
        if (fixedObjectStorageSize == 0) {
            MemoryMeter mm = new MemoryMeter();
            fixedObjectStorageSize = mm.measure(obj);
        }

        FixedStorageAllocationSize += fixedObjectStorageSize;
    }

    public void instrumentFlexible(FlexiblePythonObjectStorage obj) {
        if (flexibleObjectStorageSize == 0) {
            MemoryMeter mm = new MemoryMeter();
            flexibleObjectStorageSize = mm.measure(obj);
        }

        FlexibleStorageAllocationSize += flexibleObjectStorageSize;
    }

    public void printAllocations() {
        System.out.println("[ZipPy] allocated FixedObjectStorage " + FixedStorageAllocationSize + " byte");
        System.out.println("[ZipPy] allocated FlexibleObjectStorage " + FlexibleStorageAllocationSize + " byte");
    }

}
