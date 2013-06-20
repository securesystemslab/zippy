package truffle.test;

import org.junit.Test;

public class controlflow_test {
    @Test
    public void controlFlowTest() {
        System.out.println("------------controlFlowTest----------------");

        String expected = "taken \n"
                        + "4 \n"
                        + "3 \n"
                        + "2 \n"
                        + "1 \n"
                        + "0 \n";

        test.runTest("benchmarks/UnitTest/controlflow_test.py", expected);
    }
}
