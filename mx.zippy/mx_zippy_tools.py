

def _sanitize_vmArgs(jdk, vmArgs):
    '''
    jdk dependent analysis of vmArgs to remove those that are not appropriate for the
    chosen jdk. It is easier to allow clients to set anything they want and filter them
    out here.
    '''
    jvmci_jdk = jdk.tag == 'jvmci'
    jvmci_disabled = '-XX:-EnableJVMCI' in vmArgs

    xargs = []
    i = 0
    while i < len(vmArgs):
        vmArg = vmArgs[i]
        if vmArg != '-XX:-EnableJVMCI':
            if vmArg.startswith("-") and '-Dgraal' in vmArg or 'JVMCI' in vmArg:
                if not jvmci_jdk or jvmci_disabled:
                    i = i + 1
                    continue
        xargs.append(vmArg)
        i = i + 1
    return xargs


usage_message = """
usage: mx python [option] file [arg] ...
Options and arguments
    -h          : print this help message and exit (also --help)
    arg ...     : arguments passed to program in sys.argv[1:]

ZipPy specific options:
    -print-ast  : print ast before and after interpretation
"""
# TODO: add more options

def _zippy_help_options(args):
    if len(args) == 0 or '-h' in args or '--help' in args:
        print(usage_message)
        return True
    return False

def _extract_zippy_internal_options(args):
    internal = []
    noneInternal = []
    for arg in args:
        # Debug flags
        if arg == '-print-ast':
            internal += ["-Dedu.uci.python.PrintAST=true"                               ] # false

        elif arg == '-visualize-ast':
            internal += ["-Dedu.uci.python.VisualizedAST=true"                          ] # false

        elif arg == '-visualize-ast-verbose':
            internal += ["-Dedu.uci.python.VisualizedAST=true"                          ] # false
            internal += ["-Dedu.uci.python.VisualizedASTverbose=true"                   ] # false

        elif arg.startswith('-print-ast='):
            internal += ["-Dedu.uci.python.PrintASTFilter="+ arg.replace('-print-ast=') ] # null

        elif arg == '-debug-trace':
            internal += ["-Dedu.uci.python.TraceJythonRuntime=true"                     ] # false
            internal += ["-Dedu.uci.python.TraceImports=true"                           ] # false
            internal += ["-Dedu.uci.python.TraceSequenceStorageGeneralization=true"     ] # false
            internal += ["-Dedu.uci.python.TraceObjectLayoutCreation=true"              ] # false
            internal += ["-Dedu.uci.python.TraceNodesWithoutSourceSection=true"         ] # false
            internal += ["-Dedu.uci.python.TraceNodesUsingExistingProbe=true"           ] # false

        elif arg == '-debug-junit':
            internal += ["-Dedu.uci.python.CatchZippyExceptionForUnitTesting=true"      ] # false

        # Object storage allocation """
        elif arg == '-instrument-storageAlloc':
            internal += ["-Dedu.uci.python.InstrumentObjectStorageAllocation=true"      ] # false

        # Translation flags """
        elif arg == '-print-function':
            internal += ["-Dedu.uci.python.UsePrintFunction=true"                       ] # false

        # Runtime flags
        elif arg == '-no-sequence-unboxing':
            internal += ["-Dedu.uci.python.disableUnboxSequenceStorage=true"          ] # true
            internal += ["-Dedu.uci.python.disableUnboxSequenceIteration=true"        ] # true

        elif arg == '-no-intrinsify-calls':
            internal += ["-Dedu.uci.python.disableIntrinsifyBuiltinCalls=true"        ] # true

        elif arg == '-flexible-object-storage':
            internal += ["-Dedu.uci.python.FlexibleObjectStorage=true"                ] # false

        elif arg == '-flexible-storage-evolution':
            internal += ["-Dedu.uci.python.FlexibleObjectStorageEvolution=true"       ] # false
            internal += ["-Dedu.uci.python.FlexibleObjectStorage=true"                ] # false

        # Generators
        elif arg == '-no-inline-generator':
            internal += ["-Dedu.uci.python.disableInlineGeneratorCalls=true"          ] # true
        elif arg == '-no-optimize-genexp':
            internal += ["-Dedu.uci.python.disableOptimizeGeneratorExpressions=true"  ] # true
        elif arg == '-no-generator-peeling':
            internal += ["-Dedu.uci.python.disableInlineGeneratorCalls=true"          ] # true
            internal += ["-Dedu.uci.python.disableOptimizeGeneratorExpressions=true"  ] # true

        elif arg == '-trace-generator-peeling':
            internal += ["-Dedu.uci.python.TraceGeneratorInlining=true"               ] # false

        # Other
        elif arg == '-force-long':
                internal += ["-Dedu.uci.python.forceLongType=true"                        ] # false

        else:
            noneInternal += [arg]

    return internal, noneInternal



# Graal/Truffle heuristics parameters
def _graal_heuristics_options(_mx_graal):
    result = []
    if _mx_graal:
        # result += ['-Dgraal.InliningDepthError=500']
        # result += ['-Dgraal.EscapeAnalysisIterations=3']
        # result += ['-XX:JVMCINMethodSizeLimit=1000000']
        result += ['-XX:+UseJVMCICompiler', '-Djvmci.Compiler=graal']
        # result += ['-Xms10g', '-Xmx16g']
        result += ['-Dgraal.TraceTruffleCompilation=true']
        result += ['-Dgraal.TruffleCompileImmediately=true']
        # result += ['-Dgraal.TraceTrufflePerformanceWarnings=true']
        # result += ['-Dgraal.TruffleCompilationExceptionsArePrinted=true']
        # result += ['-Dgraal.TruffleInliningMaxCallerSize=150']
        # result += ['-Dgraal.InliningDepthError=10']
        # result += ['-Dgraal.MaximumLoopExplosionCount=1000']
        # result += ['-Dgraal.TruffleCompilationThreshold=100000']
    return result
