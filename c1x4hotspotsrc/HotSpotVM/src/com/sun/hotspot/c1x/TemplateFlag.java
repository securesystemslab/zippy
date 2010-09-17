package com.sun.hotspot.c1x;

enum TemplateFlag {
    NULL_CHECK, UNRESOLVED, READ_BARRIER, WRITE_BARRIER, STORE_CHECK, BOUNDS_CHECK, GIVEN_LENGTH, STATIC_METHOD, SYNCHRONIZED;

    private static long FIRST_FLAG = 0x0000000100000000L;
    public static long FLAGS_MASK = 0x0000FFFF00000000L;
    public static long INDEX_MASK = 0x00000000FFFFFFFFL;

    public long bits() {
        assert ((FIRST_FLAG << ordinal()) & FLAGS_MASK) != 0;
        return FIRST_FLAG << ordinal();
    }
}