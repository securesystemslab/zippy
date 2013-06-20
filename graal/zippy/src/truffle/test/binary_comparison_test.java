package truffle.test;

import org.junit.Test;

public class binary_comparison_test {
    @Test
    public void binaryComparisonTest() {
        System.out.println("------------binaryComparisonTest----------------");

        String expected = "True True True True True True \n"
                        + "True True True True True True \n"
                        + "True True True False False True \n"
                        + "True True False False \n"
                        + "True \n";

        test.runTest("benchmarks/UnitTest/binary_comparison_test.py", expected);
    }
}
