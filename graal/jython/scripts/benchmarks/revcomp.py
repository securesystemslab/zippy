# The Computer Language Benchmarks Game
# http://shootout.alioth.debian.org/
#
# contributed by Jacob Lee, Steven Bethard, et al
# modified by Justin Peel

import sys, string

def show(seq,
         table=string.maketrans('ACBDGHKMNSRUTWVYacbdghkmnsrutwvy',
                                'TGVHCDMKNSYAAWBRTGVHCDMKNSYAAWBR'),nl='\n'):
   s = (''.join(seq)).translate(table, nl)[::-1]
   sys.stdout.writelines(s[i:i+60]+nl for i in xrange(0,len(s),60))

def main():
   seq = []
   add_line = seq.append
   localstdin = sys.stdin
   print localstdin.next(),
   for line in localstdin:
      if line[0] in '>;':
         show(seq)
         print line,
         del seq[:]
      else:
         add_line(line)
   
   show(seq)
      
main()

