

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



def _zippy_internal_options():
    result = []
    """ Debug flags """
    # result += ["-Dedu.uci.python.PrintAST=true"                             ] # false
    # result += ["-Dedu.uci.python.VisualizedAST=true"                        ] # false
    # result += ["-Dedu.uci.python.PrintASTFilter="                           ] # null
    # result += ["-Dedu.uci.python.TraceJythonRuntime=true"                   ] # false
    # result += ["-Dedu.uci.python.TraceImports=true"                         ] # false
    # result += ["-Dedu.uci.python.TraceSequenceStorageGeneralization=true"   ] # false
    # result += ["-Dedu.uci.python.TraceObjectLayoutCreation=true"            ] # false

    """ Object storage allocation """
    # result += ["-Dedu.uci.python.InstrumentObjectStorageAllocation=true"    ] # false

    """ Translation flags """
    # result += ["-Dedu.uci.python.UsePrintFunction=true"                     ] # false

    """ Runtime flags """
    # result += ["-Dedu.uci.python.disableUnboxSequenceStorage=true"          ] # true
    # result += ["-Dedu.uci.python.disableUnboxSequenceIteration=true"        ] # true
    # result += ["-Dedu.uci.python.disableIntrinsifyBuiltinCalls=true"        ] # true
    # result += ["-Dedu.uci.python.FlexibleObjectStorageEvolution=true"       ] # false
    # result += ["-Dedu.uci.python.FlexibleObjectStorage=true"                ] # false

    """ Generators """
    # result += ["-Dedu.uci.python.disableInlineGeneratorCalls=true"          ] # true
    # result += ["-Dedu.uci.python.disableOptimizeGeneratorExpressions=true"  ] # true
    # result += ["-Dedu.uci.python.TraceGeneratorInlining=true"               ] # false
    # result += ["-Dedu.uci.python.TraceNodesWithoutSourceSection=true"       ] # false
    # result += ["-Dedu.uci.python.TraceNodesUsingExistingProbe=true"         ] # false
    # result += ["-Dedu.uci.python.CatchZippyExceptionForUnitTesting=true"    ] # false

    """ Other """
    # result += ["-Dedu.uci.python.forceLongType=true"                        ] # false
    return result



# Graal/Truffle heuristics parameters
def _graal_heuristics_options(_mx_graal):
    result = []
    if _mx_graal:
        # result += ['-Dgraal.InliningDepthError=500']
        # result += ['-Dgraal.EscapeAnalysisIterations=3']
        # result += ['-XX:JVMCINMethodSizeLimit=1000000']
        result += ['-XX:+UseJVMCICompiler', '-Djvmci.Compiler=graal']
        result += ['-Xms10g', '-Xmx16g']
        # result += ['-Dgraal.TraceTruffleCompilation=true']
        # result += ['-Dgraal.TruffleCompileImmediately=true']
        # result += ['-Dgraal.TraceTrufflePerformanceWarnings=true']
        # result += ['-Dgraal.TruffleCompilationExceptionsArePrinted=true']
        # result += ['-Dgraal.TruffleInliningMaxCallerSize=150']
        # result += ['-Dgraal.InliningDepthError=10']
        # result += ['-Dgraal.MaximumLoopExplosionCount=1000']
        # result += ['-Dgraal.TruffleCompilationThreshold=100000']
    return result
