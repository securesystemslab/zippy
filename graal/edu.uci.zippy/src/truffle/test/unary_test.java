package truffle.test;

import org.junit.Test;

public class unary_test {
    @Test
    public void unaryTest() {
        System.out.println("------------unaryTest----------------");

        String expected = "3 -129 -346 \n"
                        + "37857431053781905 -129547839057329057230 -3455473924052745730 \n"
                        + "3.45 -54353.65636 \n"
                        + "false true false false \n";

        test.runTest("benchmarks/UnitTest/unary_test.py", expected);
    }
}
