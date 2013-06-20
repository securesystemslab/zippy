'''
Created on Apr 25, 2013

@author: MYQ
'''
import sys
import filecmp

if __name__ == '__main__':
   print filecmp.cmp(sys.argv[1], sys.argv[2])   
#    print filecmp.dircmp(sys.argv[1], sys.argv[2]).diff_files