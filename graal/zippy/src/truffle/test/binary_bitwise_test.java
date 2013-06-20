package truffle.test;

import org.junit.Test;

public class binary_bitwise_test {
    @Test
    public void binaryBitwiseTest() {
        System.out.println("------------binaryBitwiseTest----------------");

        String expected = "8 2 680564733841876926926749214863536422912 0 -256 -1 \n"
                               + "0 441 415 \n"
                               + "0 943824320482304948 544382094820482034324155 \n";

        test.runTest("benchmarks/UnitTest/binary_bitwise_test.py", expected);
    }
}
