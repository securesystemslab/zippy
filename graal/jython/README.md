Hawk, a New Interpreter for Jython
==================================

This is a fork of [Jython][1] 2.5 with an new interpreter called Hawk.

Hawk is an AST interpreter that explores type specializations at runtime. 
Its goal is to make better use of JVM as a platform for Python.

## How to use it :

### 1. Download the Code

Clone and check out branch `graal`

```sh
hg clone https://bitbucket.org/ndrzmansn/jython -r graal
```
**NOTE:** Hawk has hard dependency on [GraalVM][0]. It should be cloned at the root directory of a [GraalVM][0] repo.

```plain
GraalVM
 |- graal_dir1
 |- graal_dir2
 |- graal_dir3
 |- graal_dir4
 |- jython (hawk)

```

### 2. Import to Eclipse & Annotation Processing

**2.1** First we need to initialize Jython building directories.

```sh
cd $jython_dir
ant
```
This is needed because Jython has some self bootstrapping source code generation proceess.
`ant` will fail to build Jython at this point, but it will resolve some internal dependencies when imported to eclipse.


**2.2** Import the cloned project to an existing `graal` workspace.

The cloned source includes eclipse project setting files that uses **Truffle** codegen as the enabled annotation proccessor.

Please refresh **Jython** in eclipse to trigger annotation based source code generation.


### 3. Build Jython with Hawk

If the generated source files appear in `$jython_dir/src_gen`, we can go ahead and build `Jython` in terminal.

```sh
cd $jython_dir
ant
```

If successful, a `jython-dev.jar` should be created under `$jython_dir/dist/`.

### 4. Run It

**4.1** Run a single benchmark using Hawk

```sh
cd $jython_dir/scripts
./jython.py -x binarytrees.py 3 -O "-interpretast -specialize"
```

`jython.py` is our command line interface for Jython. It is under `$jython_dir/scripts`.

`jython.py` assumes the current directory to be `$jython_dir/scripts`. So it should be fired there.

`-x`: execute a Python script.  
The option should be followed by a script name located under `$jython_dir/scripts/benchmarks/`.  
A numeric argument (`3`) is **needed** for each benchmark.

`-O`: arguments passed to Jython.  
The option is followed by a string containing options that will be passed to Jython.

`-interpretast`: Enable AST Interpreter. This is a Jython argument.

`-specialize`: Enable type specialized AST Interpreter (Hawk). This is a Jython argument.


**4.2** Run a single benchmark with stock Jython

```sh
./jython.py -x binarytrees.py 3
```

If no interpreter flag is passed to Jython, it runs the specified Python scripts using its default classfile compiler.


**4.3** Run the benchmark suite

```sh
./jython.py -r -O "-interpretast -specialize"
```

`-r`: Run all benchmarks with arguments specified in `$jython_dir/scripts/benchmarks/.run`.


**4.4** Run Jython on GraalVM

```sh
./jython.py -x binarytrees.py 3 -O "-interpretast -specialize" -g
```

`-g`: Run Jython on a pre-built GraalVM.  
This command searches the root directory of a Graal repo for existing JDK directories.  
It assumes that there are at least one JDK directory exists in the parent directory of Jython, and takes one of them as the Java executable.

**Note:** Currently, not all benchmarks can successfully run on Graal.


### 5. Source Code

All Hawk source code is under `$jython_dir/src`.

It includes following packages:

```
org.python.ast
org.python.ast.datatypes
org.python.ast.nodes
org.python.ast.nodes.expressions
org.python.ast.nodes.literals
org.python.ast.nodes.statements
org.python.ast.nodes.utils
org.python.core.truffle
```

[0]: http://openjdk.java.net/projects/graal/
[1]: https://bitbucket.org/jython/jython