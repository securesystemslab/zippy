from outputparser import OutputParser, Matcher
import re
import mx
import os
import commands
from os.path import isfile

dacapoSanityWarmup = {
    'avrora': [0, 0, 3, 6],
    'batik': [0 , 0, 5, 5],
    'eclipse': [2 , 4, 5, 10],
    'fop': [4 , 8, 10, 20],
    'h2': [0 , 0, 5, 5],
    'jython': [0 , 0, 5, 10],
    'luindex': [0 , 0, 5, 10],
    'lusearch': [0 , 4, 5, 10],
    'pmd': [0 , 0, 5, 10],
    'sunflow': [0 , 0, 5, 10],
    'tomcat': [0 , 0, 5, 10],
    'tradebeans': [0 , 0, 5, 10],
    'tradesoap': [2 , 4, 5, 10],
    'xalan': [0 , 0, 5, 10],
}

class SanityCheckLevel:
    Fast, Gate, Normal, Extensive = range(4)

def getDacapos(level=SanityCheckLevel.Normal, dacapoArgs=[]):
    checks = []
    
    for (bench, ns) in dacapoSanityWarmup.items():
        if ns[level] > 0:
            checks.append(getDacapo(bench, ns[level], dacapoArgs))
    
    return checks

def getDacapo(name, n, dacapoArgs=[]):
    dacapo = mx.get_env('DACAPO_CP')
    if dacapo is None:
        dacapo = commands._graal_home + r'/lib/dacapo-9.12-bach.jar'
    
    if not isfile(dacapo) or not dacapo.endswith('.jar'):
        mx.abort('Specified DaCapo jar file does not exist or is not a jar file: ' + dacapo)
    
    dacapoSuccess = re.compile(r"^===== DaCapo 9\.12 ([a-zA-Z0-9_]+) PASSED in ([0-9]+) msec =====$")
    dacapoFail = re.compile(r"^===== DaCapo 9\.12 ([a-zA-Z0-9_]+) FAILED (warmup|) =====$")
    dacapoTime = re.compile(r"===== DaCapo 9\.12 (?P<benchmark>[a-zA-Z0-9_]+) PASSED in (?P<time>[0-9]+) msec =====")
    
    dacapoMatcher = Matcher(dacapoTime, {'const:name' : 'benchmark', 'const:score' : 'time'})
    
    return Test("DaCapo-" + name, "DaCapo", ['-jar', dacapo, name, '-n', str(n), ] + dacapoArgs, [dacapoSuccess], [dacapoFail], [dacapoMatcher], ['-Xms1g', '-Xmx2g', '-XX:MaxPermSize=256m'])

class Test:
    def __init__(self, name, group, cmd, succesREs=[], failureREs=[], scoreMatchers=[], vmOpts=[]):
        self.name = name
        self.group = group
        self.succesREs = succesREs
        self.failureREs = failureREs
        self.scoreMatchers = scoreMatchers
        self.vmOpts = vmOpts
        self.cmd = cmd
    
    def test(self, vm, cwd=None, opts=[]):
        parser = OutputParser(nonZeroIsFatal = False)
        jvmError = re.compile(r"(?P<jvmerror>([A-Z]:|/).*[/\\]hs_err_pid[0-9]+\.log)")
        parser.addMatcher(Matcher(jvmError, {'const:jvmError' : 'jvmerror'}))
        
        for succesRE in self.succesREs:
            parser.addMatcher(Matcher(succesRE, {'const:passed' : 'const:1'}))
        for failureRE in self.failureREs:
            parser.addMatcher(Matcher(failureRE, {'const:failed' : 'const:1'}))
        
        result = parser.parse(vm, self.vmOpts + opts + self.cmd, cwd)
        
        parsedLines = result['parsed']
        assert len(parsedLines) == 1, 'Test matchers should not return more than one line'
        
        parsed = parsedLines[0]
        
        if parsed.has_key('jvmError'):
            mx.log('/!\\JVM Error : dumping error log...')
            f = open(parsed['jvmError'], 'rb');
            for line in iter(f.readline, ''):
                mx.log(line.rstrip())
            f.close()
            os.unlink(parsed['jvmError'])
            return False
        
        if parsed.has_key('failed') and parsed['failed'] is 1:
            return False
        
        return result['retcode'] is 0 and parsed.has_key('passed') and parsed['passed'] is '1'
    
    def bench(self, vm, cwd=None, opts=[]):
        parser = OutputParser(nonZeroIsFatal = False)
        
        for scoreMatcher in self.scoreMatchers:
            parser.addMatcher(scoreMatcher)
            
        result = parser.parse(vm, self.vmOpts + opts + self.cmd, cwd)
        if result['retcode'] is not 0:
            return {}
        
        parsed = result['parsed']
        
        ret = {}
        
        for line in parsed:
            assert line.has_key('name') and line.has_key('score')
            ret[line['name']] = line['score']
        
        return ret
        