package edu.uci.python.test.decorator;

import static edu.uci.python.test.PythonTests.assertPrints;

import org.junit.Test;

import edu.uci.python.runtime.PythonOptions;

public class SingleDecoratorTest {

    @Test
    public void decorator_test_wrapper() {
        String source = "\n" + //
                        "def a(x):\n" + //
                        "  def c():\n" + //
                        "    print(\"a\")\n" + //
                        "    x()\n" + //
                        "  return c\n" + //
                        "@a\n" + //
                        "def b():\n" + //
                        "  print(\"b\")\n" + //
                        "b()\n";
        assertPrints("a\nb\n", source);
    }

    @Test
    public void decorator_test_wrapper_arg() {
        String source = "\n" + //
                        "def a(x):\n" + //
                        "  def c(*args):\n" + //
                        "    print(\"a\")\n" + //
                        "    x(*args)\n" + //
                        "  return c\n" + //
                        "@a\n" + //
                        "def b(y):\n" + //
                        "  print(y)\n" + //
                        "b(\"b\")\n";
        assertPrints("a\nb\n", source);
    }

    @Test
    public void decorator_test_wrapper_dec_arg_a() {
        String source = "\n" + //
                        "def a(z):\n" + //
                        "  def c(x):\n" + //
                        "    print(z)\n" + //
                        "    def d(*args):\n" + //
                        "      x(*args)\n" + //
                        "    return d\n" + //
                        "  return c\n" + //
                        "@a(\"a\")\n" + //
                        "def b(y):\n" + //
                        "  print(y)\n" + //
                        "\n";
        assertPrints("a\n", source);
    }

    @Test
    public void decorator_test_wrapper_dec_arg_ab() {
        PythonOptions.GPUenabled = true;
        String source = "\n" + //
                        "def a(z):\n" + //
                        "  def c(x):\n" + //
                        "    def d(*args):\n" + //
                        "      print(z)\n" + //
                        "      x(*args)\n" + //
                        "    return d\n" + //
                        "  return c\n" + //
                        "@a(\"a\")\n" + //
                        "def b(y):\n" + //
                        "  print(y)\n" + //
                        "b(\"b\")\n";
        assertPrints("a\nb\n", source);
    }

    @Test
    public void decorator_test_wrapper_dec_arg_nothing() {
        String source = "\n" + //
                        "def a(z):\n" + //
                        "  def c(x):\n" + //
                        "    def d(*args):\n" + //
                        "      print(z)\n" + //
                        "      x(*args)\n" + //
                        "    return d\n" + //
                        "  return c\n" + //
                        "@a(\"a\")\n" + //
                        "def b(y):\n" + //
                        "  print(y)\n" + //
                        "\n";
        assertPrints("", source);
    }

}
