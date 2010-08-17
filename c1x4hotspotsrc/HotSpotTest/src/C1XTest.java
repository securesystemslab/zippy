import java.io.IOException;

public class C1XTest {

	public static void main(String[] args) throws IOException {
		/*
		 * new C1XCompiledTest.C(); System.out.println("--- instanceof A");
		 * System.out.println(new C1XCompiledTest().doCalc(10, 3,
		 * C1XTest.class)); System.out.println("B instanceof A");
		 * System.out.println(new C1XCompiledTest().doCalc(10, 3, new
		 * C1XCompiledTest.B())); System.out.println("A instanceof A");
		 * System.out.println(new C1XCompiledTest().doCalc(10, 3, new
		 * C1XCompiledTest.A())); System.out.println("end");
		 */
		new C1XCompiledTest.B();
		System.out.println(new C1XCompiledTest().doCalc(10, 3, C1XTest.class));
		/*
		 * for (int l = 0; l < 4; l++) { try { System.out.println(new
		 * C1XCompiledTest().doCalc(new int[l])); } catch (Throwable t) {
		 * t.printStackTrace(); }}
		 */
	}
}
