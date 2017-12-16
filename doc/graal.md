
# Building ZipPy with Graal

## System Requirements

ZipPy is developed and tested on Mac OS X (10.11/10.10) and Linux Ubuntu (14.04/16.04).
> Building on **Windows** is currently *not supported*.

## Prerequisites:

* Install the most recent [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) and store its path on `$JAVA_HOME`.
```sh
$ export JAVA_HOME=/path/to/jdk
```

* Install `mx` build tool and append it to your `PATH`:
> `mx` build tool, which is used in all projects built around Graal. `mx` requires Python 2.7 (not exactly ZipPy at this point...).

```sh
$ git clone https://github.com/graalvm/mx.git
$ export PATH=$ZIPPY_HOME/mx:$PATH
```
> Its strongly recommended to add `mx` to your shell config file e.g. `.bashrc` for bash.
>```sh
> $ echo "export PATH=$ZIPPY_HOME/mx:$PATH" >> ~/.bashrc
>```
> For more information about `mx` please refer to the [mx](https://github.com/graalvm/mx).

* Install **JVMCI JDK** by ether:
  * Download and install [JVMCI JDK 8](http://www.oracle.com/technetwork/oracle-labs/program-languages/downloads/index.html) binary.
  ```sh
    $ export JVMCI_HOME=/path/to/jvmci_jdk
  ```
  * Build JVMCI from source (requires standard JDK and a C/C++ compiler tool chain):
  ```sh
    $ cd $ZIPPY_HOME
    $ git clone https://github.com/graalvm/graal-jvmci-8.git
    $ echo "JAVA_HOME=$JAVA_HOME" > $ZIPPY_HOME/graal-jvmci-8/mx.jvmci/env
    $ cd graal-jvmci-8
    $ git checkout jvmci-0.39
    $ mx build
    $ export JVMCI_HOME=$(mx jdkhome)
  ```

### Using Graal JVM (recommended):

1. Clone `ZipPy`:
      ```sh
      $ git clone https://github.com/securesystemslab/zippy.git
      ```

2. Add environment variables to `ZipPy` to make it aware of `Graal JVM`:

  ```sh
    $ echo "JAVA_HOME=$JVMCI_HOME" > $ZIPPY_HOME/zippy/mx.zippy/env
    $ echo "DEFAULT_VM=server" >> $ZIPPY_HOME/zippy/mx.zippy/env
    $ echo "DEFAULT_DYNAMIC_IMPORTS=truffle/compiler" >> $ZIPPY_HOME/zippy/mx.zippy/env
    $ echo "ZIPPY_MUST_USE_GRAAL=1" >> $ZIPPY_HOME/zippy/mx.zippy/env
  ```

3. Pull the required projects:
      ```sh
      $ cd $ZIPPY_HOME/zippy
      $ mx spull
      ```

## Building ZipPy

To build a suite and the suites it depends on, the `mx build` command is used:
```sh
    $ cd $ZIPPY_HOME/zippy
    $ mx spull
    $ mx build
```

## Running ZipPy

After building, running ZipPy can be done with `mx python`.

    $ mx python <file.py>

Sadly, interactive shell and many of the CPython command line options are not yet implemented...

## Unittest

The subproject `edu.uci.python.test` includes a set of tests that we currently use. The `mx junit` command runs all JUnit test it can find in the current _suite_. The following command runs all the unit tests with their class paths matching the pattern, `python.test`.

    $ mx junit

Alternatively, you can run

    $ mx gate --tags pythontest



## Note:

> When using **Graal JVM**:

> On the first build, the build script might prompt you for a VM configuration to build.

> In general, choosing the `server` configuration is advised.

> For details, see [Graal Instructions](https://wiki.openjdk.java.net/display/Graal/Instructions).
