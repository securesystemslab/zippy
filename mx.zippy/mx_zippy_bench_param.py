py = ".py"
pathBench = "zippy/benchmarks/src/benchmarks/"
pathMicro = "zippy/benchmarks/src/micro/"

pythonGeneratorBenchmarks = {
    'euler31-timed'             : ['200',       ['200',      ]],
    'euler11-timed'             : ['10000',     ['10000',    ]],
    'ai-nqueen-timed'           : ['10',        ['10',       ]],
    'pads-eratosthenes-timed'   : ['100000',    ['100000',   ]],
    'pads-integerpartitions'    : ['700',       ['700',      ]],
    'pads-lyndon'               : ['100000000', ['100000000',]],
    'python-graph-bench'        : ['200',       ['200',      ]],
    'simplejson-bench'          : ['10000',     ['10000',    ]],
    # 'whoosh-bench'    : '5000',
    # 'pymaging-bench'  : '5000',
    # 'sympy-bench'     : '20000',
}

pythonObjectBenchmarks = {
    'richards3-timed'           : ['200',       ['200',     ]] ,
    'bm-float-timed'            : ['1000',      ['1000',    ]] ,
    'pypy-chaos-timed'          : ['1000',      ['1000',    ]] ,
    'pypy-go-timed'             : ['50',        ['50',      ]] ,
    'pypy-deltablue'            : ['2000',      ['2000',    ]] ,
}

pythonBenchmarks = {
    'binarytrees3t'             : ['18',        ['18'       ]],
    'fannkuchredux3t'           : ['11',        ['11'       ]],
    'fasta3t'                   : ['25000000',  ['25000000' ]],
    'mandelbrot3t'              : ['4000',      ['4000'     ]],
    'meteor3t'                  : ['2098',      ['2098'     ]],
    'nbody3t'                   : ['5000000',   ['5000000'  ]],
    'spectralnorm3t'            : ['3000',      ['3000'     ]],
    'pidigits-timed'            : ['0',         ['0'        ]],
    'euler31-timed'             : ['200',       ['200'      ]],
    'euler11-timed'             : ['10000',     ['10000'    ]],
    'ai-nqueen-timed'           : ['10',        ['10'       ]],
    'pads-eratosthenes-timed'   : ['100000',    ['100000'   ]],
    'pads-integerpartitions'    : ['700',       ['700'      ]],
    'pads-lyndon'               : ['100000000', ['100000000']],
    'richards3-timed'           : ['200',       ['200'      ]],
    'bm-float-timed'            : ['1000',      ['1000'     ]],
    'pypy-chaos-timed'          : ['1000',      ['1000'     ]],
    'pypy-go-timed'             : ['50',        ['50'       ]],
    'pypy-deltablue'            : ['2000',      ['2000'     ]],
    'python-graph-bench'        : ['200',       ['200'      ]],
    'simplejson-bench'          : ['10000',     ['10000'    ]],
    # 'whoosh-bench'    : '5000',
    # type not supported to adopt to Jython! <scoring.WeightScorer...
    # 'pymaging-bench'  : '5000',
    # Multiple super class is not supported yet! + File "JYTHON.jar/Lib/abc.py", line 32, in abstractmethod AttributeError: 'str' object has no attribute '__isabstractmethod__'
    # 'sympy-bench'     : '20000',
    # ImportError: No module named core
}

pythonMicroBenchmarks = {
    'arith-binop'                   : ['',      ['0'    ]],
    'for-range'                     : ['',      ['0'    ]],
    'function-call'                 : ['',      ['0'    ]],
    'list-comp'                     : ['',      ['0'    ]],
    'list-indexing'                 : ['',      ['0'    ]],
    'list-iterating'                : ['',      ['0'    ]],
    'builtin-len'                   : ['',      ['0'    ]],
    'builtin-len-tuple'             : ['',      ['0'    ]],
    'math-sqrt'                     : ['',      ['0'    ]],
    'generator'                     : ['',      ['0'    ]],
    'generator-notaligned'          : ['',      ['0'    ]],
    'generator-expression'          : ['',      ['0'    ]],
    'genexp-builtin-call'           : ['',      ['0'    ]],
    'attribute-access'              : ['',      ['0'    ]],
    'attribute-access-polymorphic'  : ['',      ['0'    ]],
    'attribute-bool'                : ['',      ['0'    ]],
    'call-method-polymorphic'       : ['',      ['0'    ]],
    'boolean-logic'                 : ['',      ['0'    ]],
    'object-allocate'               : ['',      ['0'    ]],
    'special-add'                   : ['',      ['0'    ]],
    'special-add-int'               : ['',      ['0'    ]],
    'special-len'                   : ['',      ['0'    ]],
    'object-layout-change'          : ['',      ['0'    ]],
}

# XXX: testing
# pythonBenchmarks = {
#     'binarytrees3t'                 : ['8',    ['8'   ], '10',    ['10'   ]],
#     'mandelbrot3t'                  : ['300',  ['300' ], '500',   ['500'  ]],
#     'ai-nqueen-timed'               : ['5',    ['5'   ], '6',     ['6'    ]],
# }
# pythonMicroBenchmarks = {
#     'arith-binop'                   : ['',      ['0'    ]],
#     'for-range'                     : ['',      ['0'    ]],
# }

# helper list

benchmarks_list = {
"normal"    : [pathBench, pythonBenchmarks],
"micro"     : [pathMicro, pythonMicroBenchmarks],
"generator" : [pathBench, pythonGeneratorBenchmarks],
"object"    : [pathBench, pythonObjectBenchmarks],
}
