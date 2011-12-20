from outputparser import OutputParser, Matcher
import re
import mx
import os
from os.path import isfile

dacapoVMOpts = ['-Xms1g', '-Xmx2g']

dacapoSanityWarmup = {
    'avrora': [0, 0, 3, 6],
    'batik': [0 , 0, 5, 5],
    'eclipse': [1 , 4, 5, 10],
    'fop': [4 , 8, 10, 20],
    'h2': [0 , 0, 5, 5],
    'jython': [0 , 0, 5, 10],
    'luindex': [0 , 0, 5, 10],
    'lusearch': [0 , 4, 5, 10],
    'pmd': [0 , 0, 5, 10],
    'sunflow': [0 , 0, 5, 10],
    'tomcat': [0 , 0, 5, 10],
    'tradebeans': [0 , 0, 5, 10],
    'tradesoap': [0 , 4, 5, 10],
    'xalan': [0 , 0, 5, 10],
}

def getDacapoCmd(bench, vmOpts=dacapoVMOpts,n=5):
    dacapo = mx.check_get_env('DACAPO_CP')
    if not isfile(dacapo) or not dacapo.endswith('.jar'):
        mx.abort('Specified DaCapo jar file does not exist or is not a jar file: ' + dacapo)
    return vmOpts + ['-cp', dacapo, 'Harness', '-n', str(n), bench]

class SanityCheckLevel:
    Fast, Gate, Normal, Extensive = range(4)

def getSanityChecks(level=SanityCheckLevel.Normal):
    checks = []
    
    dacapoSuccess = re.compile(r"^===== DaCapo 9\.12 ([a-zA-Z0-9_]+) PASSED in ([0-9]+) msec =====$")
    for (bench, ns) in dacapoSanityWarmup.items():
        if ns[level] > 0:
            checks.append({'cmd' : getDacapoCmd(bench, vmOpts=dacapoVMOpts + ['-esa'], n=ns[level]), 'success' : dacapoSuccess})
    
    bootstrapSuccess = re.compile(r"in [0-9]+ ms$");
    checks.append({'cmd' : ['-esa', '-version'], 'success' : bootstrapSuccess})
    
    return checks

def runSanityCheck(cmd, successRE, cwd=None):
    parser = OutputParser(nonZeroIsFatal=False)
    jvmError = re.compile(r"\b(?P<jvmerror>([A-Z]:|/).*[/\\]hs_err_pid[0-9]+\.log)\b")
    parser.addMatcher(Matcher(successRE, {'const:passed' : 'const:1'}))
    parser.addMatcher(Matcher(jvmError, {'const:jvmError' : 'jvmerror'}))
    
    result = parser.parse(cmd, cwd)
    assert len(result) == 1, 'Sanity check matchers should not return more than one line'
    parsed = result[0]
    
    if parsed.has_key('jvmError'):
        mx.log('JVM Error : dumping error log...')
        f = open(parsed['jvmError'], 'rb');
        for line in iter(f.readline(), ''):
            mx.log(line)
        f.close()
        os.unlink(parsed['jvmError'])
        return False
    return parsed.has_key('passed')
    