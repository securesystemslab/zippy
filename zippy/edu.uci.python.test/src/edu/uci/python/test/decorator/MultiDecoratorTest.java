package edu.uci.python.test.decorator;

import static edu.uci.python.test.PythonTests.assertPrints;

import org.junit.Test;

public class MultiDecoratorTest {

    @Test
    public void decorator_test_wrapper() {
        String source = "\n" + //
                        "def a(x):\n" + //
                        "  def c():\n" + //
                        "    print(\"a\")\n" + //
                        "    x()\n" + //
                        "  return c\n" + //
                        "def d(x):\n" + //
                        "  def e():\n" + //
                        "    print(\"d\")\n" + //
                        "    x()\n" + //
                        "  return e\n" + //
                        "@a\n" + //
                        "@d\n" + //
                        "def b():\n" + //
                        "  print(\"b\")\n" + //
                        "b()\n";
        assertPrints("a\nd\nb\n", source);
    }

    @Test
    public void decorator_test_wrapper_b_arg() {
        String source = "\n" + //
                        "def a(x):\n" + //
                        "  def c(*args):\n" + //
                        "    print(\"a\")\n" + //
                        "    x(*args)\n" + //
                        "  return c\n" + //
                        "def d(x):\n" + //
                        "  def e(*args):\n" + //
                        "    print(\"d\")\n" + //
                        "    x(*args)\n" + //
                        "  return e\n" + //
                        "@a\n" + //
                        "@d\n" + //
                        "def b(y):\n" + //
                        "  print(y)\n" + //
                        "b(\"b\")\n";
        assertPrints("a\nd\nb\n", source);
    }

    @Test
    public void decorator_test_wrapper_arg() {
        String source = "\n" + //
                        "def a(z):\n" + //
                        "  def c(x):\n" + //
                        "    def d(*args):\n" + //
                        "      print(z)\n" + //
                        "      x(*args)\n" + //
                        "    return d\n" + //
                        "  return c\n" + //

                        "def e(z):\n" + //
                        "  def f(x):\n" + //
                        "    def g(*args):\n" + //
                        "      print(z)\n" + //
                        "      x(*args)\n" + //
                        "    return g\n" + //
                        "  return f\n" + //

                        "@a(\"a\")\n" + //
                        "@e(\"e\")\n" + //
                        "def b(y):\n" + //
                        "  print(y)\n" + //

                        "b(\"b\")\n";
        assertPrints("a\ne\nb\n", source);
    }

    @Test
    public void decorator_test_wrapper_arg2() {
        String source = "\n" + //
                        "def a(z):\n" + //
                        "  print(\"a\")\n" + //
                        "  def c(x):\n" + //
                        "    def d(*args):\n" + //
                        "      print(z)\n" + //
                        "      x(*args)\n" + //
                        "    return d\n" + //
                        "  return c\n" + //

                        "def e(z):\n" + //
                        "  print(\"e\")\n" + //
                        "  def f(x):\n" + //
                        "    def g(*args):\n" + //
                        "      print(z)\n" + //
                        "      x(*args)\n" + //
                        "    return g\n" + //
                        "  return f\n" + //

                        "@a(\"c\")\n" + //
                        "@e(\"f\")\n" + //
                        "def b(y):\n" + //
                        "  print(y)\n" + //

                        "b(\"b\")\n";
        assertPrints("a\ne\nc\nf\nb\n", source);
    }

}
