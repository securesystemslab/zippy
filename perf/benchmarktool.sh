#!/usr/bin/python

import subprocess
import os
import re
import sys
import argparse

DEFAULT_DACAPO_OPTS = " -XX:MaxPermSize=512m -Xms1g -Xmx2g "
DEFAULT_SCIMARK_OPTS = " -Xms32m -Xmx100m "

def runBash(cmd):
    p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    return p.stdout

def s2msString(floatString):
    return str(round(float(floatString)*1000))

# Raw String Notation (r"") : \ instead of \\
graalTime = re.compile(r"Total compilation time\s+:\s+([0-9]+\.[0-9]+) s")
graalSubTime = re.compile(r"([a-zA-Z0-9_ ]+)\s+:\s+([0-9]+\.[0-9]+) s \([0-9 ][0-9]\.[0-9]{2}%\)")

def matchGraalTime(string, csvOutput, csvOutputLine, writeHeaderAt):
    match1 = graalTime.search(string)
    match2 = graalSubTime.search(string)
    if match1:
        if csvOutputLine == writeHeaderAt:
            csvOutput[0].append('graal total')
        print('Graal total time: '+ s2msString(match1.group(1)))
        csvOutput[csvOutputLine].append(s2msString(match1.group(1)))
    elif match2:
        if csvOutputLine == writeHeaderAt:
            csvOutput[0].append(match2.group(1).strip())
        print('Graal specific time: '+match2.group(1)+': '+s2msString(match2.group(2)))
        csvOutput[csvOutputLine].append(s2msString(match2.group(2)))
    else:
        print('# '+string)

def writeout(outputFile, csvOutput):
    for csvLine in csvOutput :
        outputFile.write(';'.join(csvLine)+';'+os.linesep)

def main():
    # Check for environment variables
    if os.getenv('JDK7') is None:
        print('Environment variable JDK7 is not defined.')
        return 1
    if os.getenv('DACAPO') is None:
        print('Environment variable DACAPO is not defined. It must point to a directory with the DaCapo benchmark jar.')
        return 1
    if os.getenv('SCIMARK') is None:
        print('Environment variable SCIMARK is not defined. It must point to a directory with the SciMark benchmark jar.')
        return 1
    if os.getenv('REFERENCE_JDK') is None:
        print('Environment variable REFERENCE_JDK is not defined.')
        return 1
    
    # Option parsing
    parser = argparse.ArgumentParser(description='Automated DaCapo and Scimark bechmarks')
    parser.add_argument('-a', '-all', help='run all benchmarks for all compilers', action='store_true')
    parser.add_argument('-c', type=str, help='compiler to use', default='graal', choices=['client', 'server', 'graal'], required=False)
    parser.add_argument('-n', type=int, help='number of DaCapo benchmarks to run', default=20)
    parser.add_argument('-o', type=str, help='graalVM options(quoted!)', default='')
    parser.add_argument('-runonly', type=str, help='run specified benchmark only', default='all')
    options = parser.parse_args()
    compilerFlags = {'graal' : '-client -graal -G:+Time -XX:-GraalBailoutIsFatal -G:QuietBailout ',
        'client' : '-client',
        'server' : '-server'}

    if options.a: 
        compilers = ['graal', 'client', 'server']
    else:
        compilers = [options.c]
    
    for compiler in compilers:
    
        outputFile = open(compiler+'.csv', 'w')
    
        # DaCapo Benchmarks
        if compiler == 'graal':
            vm = os.environ['JDK7']
        else :
            vm = os.environ['REFERENCE_JDK']
        
        cmd = vm + '/bin/java ' + compilerFlags[compiler] + ' -d64 ' + DEFAULT_DACAPO_OPTS + options.o + ' -classpath ' + \
            os.environ['DACAPO'] + '/dacapo-9.12-bach.jar Harness -n ' + str(options.n) + ' '
        benchmarks = runBash('java -jar ' + os.environ['DACAPO'] + '/dacapo-9.12-bach.jar -l').read().decode().split(' ')
    
        benchmarkTime = re.compile(r"===== DaCapo 9\.12 ([a-zA-Z0-9_]+) ((PASSED)|(completed warmup [0-9]+)) in ([0-9]+) msec =====")
    
        csvOutput = [['benchmark', 'type', 'time']]
        csvOutputLine = 0
        for benchmark in benchmarks:
            if options.runonly != 'all' and benchmark != options.runonly:
                continue
            nRuns = 0
            dcOutput = runBash(cmd + benchmark)
            while True:
                line = dcOutput.readline().decode()
                if not line:
                    break
                line = line.strip()
                match = benchmarkTime.search(line)
                if match:
                    csvOutputLine = csvOutputLine + 1
                    nRuns = nRuns + 1
                    csvOutput.append(list())
                    csvOutput[csvOutputLine].append(str(nRuns))
                    print('Benchmark type: '+match.group(1))
                    print('Benchmark time: '+match.group(5))
                    csvOutput[csvOutputLine].append(match.group(1))
                    csvOutput[csvOutputLine].append(match.group(5))
                else:
                    matchGraalTime(line, csvOutput, csvOutputLine, options.n)
    
        writeout(outputFile, csvOutput)
    
        if options.runonly != 'all' and options.runonly != 'scimark':
            outputFile.close()
            return 0
        
        # Scimark Benchmarks
        writeout(outputFile, [['']])
        cmd = vm + '/bin/java ' + compilerFlags[compiler] + ' -d64 ' + DEFAULT_SCIMARK_OPTS + options.o + \
            ' -Xbootclasspath/a:' + os.environ['SCIMARK'] + '/scimark2lib.jar jnt.scimark2.commandline'
    
        benchmarkScore = re.compile(r"([a-zA-Z0-9_\(\),= ]+):\s+([0-9]+\.[0-9]+)$")
    
        csvOutput = [['run']]
        csvOutputLine = 0
        scOutput = runBash(cmd)
        csvOutputLine = csvOutputLine + 1
        csvOutput.append(list())
        csvOutput[csvOutputLine].append(str(csvOutputLine))
        while True:
            line = scOutput.readline().decode()
            if not line:
                break
            line = line.strip()
            match = benchmarkScore.search(line)
            if match:
                if csvOutputLine == 1:
                    csvOutput[0].append(match.group(1).strip())
                print('Scimark '+match.group(1)+' score: '+match.group(2))
                csvOutput[csvOutputLine].append(match.group(2))
            else:
                matchGraalTime(line,csvOutput,csvOutputLine, 1)
    
        writeout(outputFile, csvOutput)
        outputFile.close()
        
    return 0

#This idiom means the below code only runs when executed from command line
if __name__ == '__main__':
    sys.exit(main())
