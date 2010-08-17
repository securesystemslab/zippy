public class C1XCompiledTest {

	static class A {
		int j;
	}

	static class B extends A {
		int k;
	}

	static class C extends B {
		int l;
	}

	static long c = 5;
	static long j = 1000;

	public long doCalc(int[] a) {
		return a.length;
	}

	public long doCalc(int a, int b, Object o) {
		if (o instanceof A) {
			return 0;
		} else {
			long k = 5;
			for (int i = 0; i < 10; i++) {
				k += c;
			}
			j += k;
			return a + b + k;
		}
	}
}
