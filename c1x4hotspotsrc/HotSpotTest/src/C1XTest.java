import java.io.IOException;

public class C1XTest {

	public static long sum = 10;
	
	public static final long value() {
		return sum;
	}
	
	public static void main3(String[] args) {/*
		long s = 0;
		s += value();
		System.out.println(s);*/
	}

	public static void main2(String[] args) {
		Other.I[] array = new Other.I[] { new Other.A(), new Other.B(),
				new Other.C(), new Other.A(), new Other.B(), new Other.C() };

		int sum = 0;
		for (int i = 0; i < 20; i++)
			for (Other.I o : array) {
				sum += o.v();
			}
		System.out.println(sum);
	}

	public static void main(String[] args) throws IOException, Exception {
		for (int i = 0; i < 10000; i++) {
			System.out.print((i & 0xfff) != 0 ? "" : ".");
			test1();
		}
		System.out.println();
		for (int i = 0; i < 100; i++) {
			Thread.sleep(1000);
		}
		for (int i = 0; i < 10000; i++) {
			System.out.print((i & 0xfff) != 0 ? "" : ".");
			test2();
		}
		System.out.println();
		for (int i = 0; i < 100; i++) {
			Thread.sleep(1000);
		}
		for (int i = 0; i < 10000; i++) {
			System.out.print((i & 0xfff) != 0 ? "" : ".");
			test3();
		}
		System.out.println();
		for (int i = 0; i < 100; i++) {
			Thread.sleep(1000);
		}
		for (int i = 0; i < 10000; i++) {
			System.out.print((i & 0xfff) != 0 ? "" : ".");
			test4();
		}
		System.out.println();
		for (int i = 0; i < 100; i++) {
			Thread.sleep(1000);
		}
		for (int i = 0; i < 10000; i++) {
			System.out.print((i & 0xfff) != 0 ? "" : ".");
			test5();
		}
		System.out.println();
		for (int i = 0; i < 100; i++) {
			Thread.sleep(1000);
		}
		for (int i = 0; i < 10000; i++) {
			System.out.print((i & 0xfff) != 0 ? "" : ".");
			test6();
		}
		System.out.println();
		for (int i = 0; i < 100; i++) {
			Thread.sleep(1000);
		}
		for (int i = 0; i < 10000; i++) {
			System.out.print((i & 0xfff) != 0 ? "" : ".");
			test7();
		}
		System.out.println();
		for (int i = 0; i < 100; i++) {
			Thread.sleep(1000);
		}
		for (int i = 0; i < 10000; i++) {
			System.out.print((i & 0xfff) != 0 ? "" : ".");
			test8();
		}
		System.out.println();
		for (int i = 0; i < 10; i++) {
			Thread.sleep(1000);
		}
		System.out.println(sum);
	}

	public static void test1() {
		long t1 = System.nanoTime();
		long t2 = System.nanoTime();
		sum += t2 - t1;
		System.out.print("");
		System.out.print("");
	}

	public static void test2() {
		long t1 = System.nanoTime();
		long t2 = System.nanoTime();
		sum += t2 - t1;
		System.out.print("");
		System.out.print("");
	}

	public static void test3() {
		long t1 = System.nanoTime();
		long t2 = System.nanoTime();
		sum += t2 - t1;
		System.out.print("");
		System.out.print("");
	}

	public static void test4() {
		long t1 = System.nanoTime();
		long t2 = System.nanoTime();
		sum += t2 - t1;
		System.out.print("");
		System.out.print("");
	}

	public static void test5() {
		long t1 = System.nanoTime();
		long t2 = System.nanoTime();
		sum += t2 - t1;
		System.out.print("");
		System.out.print("");
	}

	public static void test6() {
		long t1 = System.nanoTime();
		long t2 = System.nanoTime();
		sum += t2 - t1;
		System.out.print("");
		System.out.print("");
	}

	public static void test7() {
		long t1 = System.nanoTime();
		long t2 = System.nanoTime();
		sum += t2 - t1;
		System.out.print("");
		System.out.print("");
	}

	public static void test8() {
		long t1 = System.nanoTime();
		long t2 = System.nanoTime();
		sum += t2 - t1;
		System.out.print("");
		System.out.print("");
	}

}
