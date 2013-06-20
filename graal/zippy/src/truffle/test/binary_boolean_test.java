package truffle.test;

import org.junit.Test;

public class binary_boolean_test {
    @Test
    public void binaryBooleanTest() {
        System.out.println("------------binaryBooleanTest----------------");

        String expected = "0 0 0 9.9292 \n"
                        + "1 2 12953285437432947239 2.4343 \n";

        test.runTest("benchmarks/UnitTest/binary_boolean_test.py", expected);
    }
}
