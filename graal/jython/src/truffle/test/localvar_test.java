package truffle.test;

import org.junit.Test;

public class localvar_test {
    @Test
    public void localVarTest() {
        System.out.println("------------localVarTest----------------");

        String expected = "1 43724832472947924729 4234.994839 \n"
                        + "3 44162075767671819461 4.3284938261754985E12 \n";

        test.runTest("benchmarks/UnitTest/localvar_test.py", expected);
    }
}
