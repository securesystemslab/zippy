import mx
import commands

class OutputParser:
    
    def __init__(self, nonZeroIsFatal=True):
        self.matchers = []
        self.nonZeroIsFatal = nonZeroIsFatal
        
    def addMatcher(self, matcher):
        self.matchers.append(matcher)
    
    def parse(self, cmd, cwd=None):
        ret = [{}]
        
        def parseLine(line):
            line = line.strip()
            anyMatch = False
            for matcher in self.matchers:
                parsed = matcher.parse(line)
                if parsed:
                    anyMatch = True
                    if matcher.startNewLine and len(ret[0]) > 0:
                        ret.append({})
                    ret[len(ret)-1].update(parsed)
            if anyMatch :
                mx.log(line)
            else :
                mx.log('# ' + line)
        
        commands.vm(cmd, nonZeroIsFatal=self.nonZeroIsFatal, out=parseLine, err=parseLine, cwd=cwd)
        return ret

class Matcher:
    
    def __init__(self, regex, valuesToParse, startNewLine=False):
        assert isinstance(valuesToParse, dict)
        self.regex = regex
        self.valuesToParse = valuesToParse
        self.startNewLine = startNewLine
        
    def parse(self, line):
        match = self.regex.search(line)
        if not match:
            return False
        ret = {}
        for key, value in self.valuesToParse.items():
            ret[self.parsestr(match, key)] = self.parsestr(match, value)
        return ret
        
    def parsestr(self, match, key):
        if isinstance(key, tuple):
            if len(key) != 2:
                raise Exception('Tuple arguments must have a lenght of 2')
            tup1, tup2 = key
            # check if key is a function
            if hasattr(tup1, '__call__'):
                return tup1(self.parsestr(match, tup2))
            elif hasattr(tup2, '__call__'):
                return tup2(self.parsestr(match, tup1))
            else:
                raise Exception('Tuple must contain a function pointer')
        elif key.startswith('const:'):
            return key.split(':')[1]
        else:
            return match.group(key)
