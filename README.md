![zippy-logo-200-rounded.jpg](http://ssllab.org/zippy_logo.jpeg)
# ZipPy [![Build Status](https://travis-ci.org/securesystemslab/zippy.svg?branch=master)](https://travis-ci.org/securesystemslab/zippy) #

|                 | Standard JVM  | Graal JVM   |
| :------------------: |:-------------:| :----------:|
| Linux Ubuntu 14.04.5  | [![Build Status](https://badges.herokuapp.com/travis/securesystemslab/zippy?env=ZIPPY_JDK_TYPE=STANDARD_LINUX&label=Standard%20JVM)](https://travis-ci.org/securesystemslab/zippy)  | [![Build Status](https://badges.herokuapp.com/travis/securesystemslab/zippy?env=ZIPPY_JDK_TYPE=GRAALJVM_LINUX&label=Graal%20JVM)](https://travis-ci.org/securesystemslab/zippy) |
| Mac OSX 10.12 | [![Build Status](https://badges.herokuapp.com/travis/securesystemslab/zippy?env=ZIPPY_JDK_TYPE=STANDARD_OSX&label=Standard%20JVM)](https://travis-ci.org/securesystemslab/zippy)  | [![Build Status](https://badges.herokuapp.com/travis/securesystemslab/zippy?env=ZIPPY_JDK_TYPE=GRAALJVM_OSX&label=Graal%20JVM)](https://travis-ci.org/securesystemslab/zippy) |

ZipPy is a fast and lightweight [Python 3](https://www.python.org/) implementation built using the [Truffle](http://openjdk.java.net/projects/graal/) framework. ZipPy leverages the underlying Java JIT compiler and compiles Python programs to highly optimized machine code at runtime.

ZipPy is currently maintained by [Secure Systems and Software Laboratory](https://ssllab.org) at the ​[University of California, Irvine](http://www.uci.edu/).

### Short instructions (Using Standard JDK):

##### Prerequisites:

1. Install the most recent [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

#### Getting ZipPy:

1. Create a working directory ($ZIPPY_HOME)
2. Clone mxtool:

        $ cd $ZIPPY_HOME
        $ git clone https://github.com/graalvm/mx.git

3. Append the `mx` build tool directory to your `PATH`.

        $ export PATH=$ZIPPY_HOME/mx:$PATH

4. Clone ZipPy:

        $ git clone https://github.com/securesystemslab/zippy.git

5. Get all ZipPy's dependencies:

        $ cd $ZIPPY_HOME/zippy
        $ mx spull

6. Create a file `$ZIPPY_HOME/zippy/mx.zippy/env` and add JDK path

        JAVA_HOME=/path/to/jdk8
        DEFAULT_VM=server

> For instructions on [using Graal JVM (recommended)](https://github.com/securesystemslab/zippy/tree/master/doc/graal.md).

> For more information please visit the [ZipPy Wiki](https://github.com/securesystemslab/zippy/wiki).


### Build:

    $ cd $ZIPPY_HOME/zippy
    $ mx build

### Run:

    $ cd $ZIPPY_HOME/zippy
    $ mx python <file.py>

### Test:

    $ cd $ZIPPY_HOME/zippy
    $ mx unittest python.test

For more details and instructions for downloading and building the system, please visit the [ZipPy Wiki](https://github.com/securesystemslab/zippy/wiki).
