package truffle.test;

import org.junit.Assert;
import org.junit.Test;
import org.python.util.jython;

public class test {
    public static void runTest(String fileName, String expectedOut) {
        String opt1 = "-interpretast";
        String opt2 = "-specialize";
        String[] args = { opt1, opt2, fileName };

        StringBuilder output = new StringBuilder();

        //jython.run(args, output);

        Assert.assertEquals(expectedOut, output.toString());
    }

    @Test
    public void unaryTest() {
        System.out.println("------------unaryTest----------------");
        String expected = "3 -129 -346 \n"
                        + "37857431053781905 -129547839057329057230 -3455473924052745730 \n"
                        + "3.45 -54353.65636 \n"
                        + "false true false false \n";

        runTest("benchmarks/UnitTest/unary_test.py", expected);
    }

    @Test
    public void controlFlowTest() {
        System.out.println("------------controFlowTest----------------");
        String expected = "taken \n"
                        + "4 \n"
                        + "3 \n"
                        + "2 \n"
                        + "1 \n"
                        + "0 \n";

        runTest("benchmarks/UnitTest/controlflow_test.py", expected);
    }

    @Test
    public void localVarTest() {
        System.out.println("------------localVarTest----------------");
        String expected = "1 43724832472947924729 4234.994839 \n"
                        + "3 44162075767671819461 4.3284938261754985E12 \n";

        runTest("benchmarks/UnitTest/localvar_test.py", expected);
    }

    @Test
    public void binaryBooleanTest() {
        System.out.println("------------binaryBooleanTest----------------");
        String expected = "0 0 0 9.9292 \n"
                        + "1 2 12953285437432947239 2.4343 \n";

        runTest("benchmarks/UnitTest/binary_boolean_test.py", expected);
    }

    @Test
    public void binaryComparisonTest() {
        System.out.println("------------binaryComparisonTest----------------");
        String expected = "True True True True True True \n"
                        + "True True True True True True \n"
                        + "True True True False False True \n"
                        + "True True False False \n"
                        + "True \n";

        runTest("benchmarks/UnitTest/binary_comparison_test.py", expected);
    }
    
    @Test
    public void binaryBitwiseTest() {
        System.out.println("------------binaryBitwiseTest----------------");
        String expected = "8 2 680564733841876926926749214863536422912 0 -256 -1 \n"
                               + "0 441 415 \n"
                               + "0 943824320482304948 544382094820482034324155 \n";

        test.runTest("benchmarks/UnitTest/binary_bitwise_test.py", expected);
    }
    
    @Test
    public void collectionsTest() {
        System.out.println("------------collectionsTest----------------");
        String expected = "{two : 22, one : 1, three : 3, four : 4, zero : 0} \n"
                            + "1 \n"
                            + "[00, 11, 22, 33, 44, 55, 66, 77, 88, 99] \n" 
                            + "00 \n"
                            + "[11, 22, 33, 44] \n" 
                            + "[00, 22, 44, 66, 88] \n" 
                            + "(22, 44, 66) \n"
                            + "(22, 44) \n"
                            + "(66,) \n";

        test.runTest("benchmarks/collections_test.py", expected);
    }

       
    @Test
    public void sliceTest() {
        System.out.println("------------sliceTest----------------");
        String expected = 
          "[] \n"
        + "[1] \n"
        + "[3] \n"
        + "[0, 1, 2, 3, 4] \n"
        + "[] \n"
        + "[0, 1, 2, 3, 4] \n"
        + "[1, 2, 3, 4] \n"
        + "[0, 1, 2] \n"
        + "[0, 1, 2, 3, 4] \n"
        + "[0, 2, 4] \n"
        + "[1, 3] \n"
        + "[4, 3, 2, 1, 0] \n"
        + "[4, 2, 0] \n"
        + "[3, 1] \n"
        + "[] \n"
        + "[3] \n"
        + "[3] \n"
        + "[3, 1] \n"
        + "[4] \n"
        + "[] \n"
        + "[0, 1, 2, 3, 4] \n"
        + "[4, 3, 2, 1, 0] \n"
        + "[] \n"
        + "[0, 2, 4] \n"

        + "() \n"
        + "(1,) \n"
        + "(3,) \n"
        + "(0, 1, 2, 3, 4) \n"
        + "() \n"
        + "(0, 1, 2, 3, 4) \n"
        + "(1, 2, 3, 4) \n"
        + "(0, 1, 2) \n"
        + "(0, 1, 2, 3, 4) \n"
        + "(0, 2, 4) \n"
        + "(1, 3) \n"
        + "(4, 3, 2, 1, 0) \n"
        + "(4, 2, 0) \n"
        + "(3, 1) \n"
        + "() \n"
        + "(3,) \n"
        + "(3,) \n"
        + "(3, 1) \n"
        + "(4,) \n"
        + "() \n"
        + "(0, 1, 2, 3, 4) \n"
        + "(4, 3, 2, 1, 0) \n"
        + "() \n"
        + "(0, 2, 4) \n"

        + " \n"
        + "1 \n"
        + "3 \n"
        + "01234 \n"
        + " \n"
        + "01234 \n"
        + "1234 \n"
        + "012 \n"
        + "01234 \n"
        + "024 \n"
        + "13 \n"
        + "43210 \n"
        + "420 \n"
        + "31 \n"
        + " \n"
        + "3 \n"
        + "3 \n"
        + "31 \n"
        + "4 \n"
        + " \n"
        + "01234 \n"
        + "43210 \n"
        + " \n"
        + "024 \n";

        
        
        test.runTest("benchmarks/slice_test.py", expected);
    }
    

    


}