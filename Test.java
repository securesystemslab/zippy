public class Test {
   public static void main(String[] args) {
      Test t = new Test();
      Test2 t2 = new Test2();
      for (int i = 0; i < 5000; ++i) {
	      test2(t2);
      }
      for (int i = 0; i < 100000; ++i) {
	      test(t);
      }
   }

   public static int test(Object t) {
      if (t instanceof Test) {
          test2(t);
      }
      return 3;
   }

   public static int test2(Object t) {
         if (t instanceof Test) {
             return 1;
         }
         return 2;
   }
}


class Test2 extends Test {
}
