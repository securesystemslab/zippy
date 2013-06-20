# The Computer Language Benchmarks Game
# $Id: domain.py,v 1.17 2008-08-29 00:12:31 igouy-guest Exp $


__author__ =  'Isaac Gouy'


class FileNameParts(object):
   """ 
   self.filename = 'binarytrees.gcc' | self.filename = 'binarytrees.gcc-4.gcc'
   self.name = 'binarytrees' | self.name = 'binarytrees'
   self.imp = 'gcc' | self.imp = 'gcc'
   self.id = None | self.id = '4'
   self.programName = 'binarytrees.gcc' | self.programName = 'binarytrees.gcc-4.gcc'
   self.simpleName = 'binarytrees.1.gcc' | self.simpleName = 'binarytrees.4.gcc'
   """
   def __init__(self,filename):
      self.filename = filename
      part = filename.split('.')
      self.name = part[0]

      if part[1].isdigit(): # binarytrees.1.gcc binarytrees.1.gcc_log
         self.id = part[1]
         if len(part) == 4:
            self.imp = part[2]
         elif len(part) == 3:
            a,_,b = part[2].rpartition('_')
            if a:
               self.imp = a
            else:
               self.imp = b

      else: # binarytrees.gcc binarytrees.gcc-4.gcc
         a,_,b = part[1].rpartition('-') 
         if a:
            self.imp = part[2]
            self.id = b
         else:
            self.imp = b
            self.id = '1'

      self.__simpleName = None


   def _programName(self):
      return self.filename

   def _programName_getter(self):
      return self._programName()

   programName = property(_programName_getter)


   def _datName(self):
      return self.simpleName + '_dat'

   datName = property(_datName)


   def _baseName(self):
      if self.isNumbered():
         impid = '-'.join( (self.imp,self.id) )
         return '.'.join( (self.name,impid) )
      else:
         return self.name

   baseName = property(_baseName)


   def _runName(self):
      return self.programName + '_run'

   runName = property(_runName)


   def _logName(self):
      return self.simpleName + '.log'

   logName = property(_logName)


   def _codeName(self):
      return self.simpleName + '_code'

   codeName = property(_codeName)


   def _highlightName(self):
      return self.simpleName + '.code'

   highlightName = property(_highlightName)


   def _simpleName(self):
      if not self.__simpleName: 
         self.__simpleName = '.'.join( (self.name,self.id,self.imp) )
      return self.__simpleName

   simpleName = property(_simpleName)


   def __str__(self):
      return '%s,%s,%s' % (self.name, self.id, self.imp)

   def isNumbered(self):
      return self.id != '0' and self.id != '1'




class LinkNameParts(FileNameParts):
   """ 
   self.filename = 'binarytrees.gcc' | self.filename = 'binarytrees.gcc-4.gcc'
   imp = 'icc'
   self.programName = 'binarytrees.icc' | self.programName = 'binarytrees.icc-4.icc'
   """
   def __init__(self,filename,imp): 
      FileNameParts.__init__(self,filename)  
      self.imp = imp

      if self.isNumbered():
         impid = '-'.join( (self.imp,self.id) )
         self.__programName = '.'.join( (self.name,impid,self.imp) )
      else:
         self.__programName = '.'.join( (self.name,self.imp) )


   def _programName(self):
      return self.__programName 




class Record(object):

   _OK = 0
   _TIMEDOUT = -1
   _ERROR = -2
   # -3 .. -7 have other uses in the website PHP scripts
   _BADOUT = -10
   _MISSING = -11
   _EMPTY = -12


   def __init__(self,arg='0'):
      self.arg = 0
      self.elapsed = 0.0 
      self.userSysTime = 0.0 
      self.maxMem = 0
      self.gz = 0
      self.status = self._EMPTY 
      self.cpuLoad = '%' 
      self.argString = arg

   def fromString(self,s):
      a = s.split(',')
      self.arg = int(a[0])
      self.gz = int(a[1])
      self.userSysTime = float(a[2])
      self.maxMem = int(a[3])
      self.status = int(a[4]) 
      self.cpuLoad = a[5]
      self.elapsed = float(a[6])
      return self

   def __str__(self):
      return '%d,%d,%.3f,%d,%d,%s,%.3f' % (
         self.arg, self.gz, self.userSysTime, self.maxMem, self.status, self.cpuLoad, self.elapsed)


   def __cmp__(self,other):
      return \
        -1 if self.arg < other.arg else (
         1 if self.arg > other.arg else (
        -1 if self.status > other.status else (
         1 if self.status < other.status else (
        -1 if self.userSysTime < other.userSysTime else (
         1 if self.userSysTime > other.userSysTime else (
         0 )) )) ))

   def setOkay(self):
      self.status = self._OK

   def setError(self):
      self.status = self._ERROR

   def setTimedout(self):
      self.status = self._TIMEDOUT

   def setBadOutput(self):
      self.status = self._BADOUT

   def setMissing(self):
      self.status = self._MISSING

   def isOkay(self):
      return self.status == self._OK

   def hasError(self):
      return self.status == self._ERROR

   def isEmpty(self):
      return self.status == self._EMPTY

   def hasTimedout(self):
      return self.status == self._TIMEDOUT

   def hasBadOutput(self):
      return self.status == self._BADOUT

   def isMissing(self):
      return self.status == self._MISSING

   def hasExceeded(self,cutoff):
      return self.userSysTime > cutoff

   def statusStr(self):
      return 'OK ' if self.isOkay() else (
         'PROGRAM FAILED ' if self.hasError() else (
         'EMPTY ' if self.isEmpty() else (
         'TIMED OUT ' if self.hasTimedout() else (
         'UNEXPECTED OUTPUT ' if self.hasBadOutput() else 
         'MAKE ERROR ' ))))

   def _getArgString(self):
      return str(self.arg)

   def _setArgString(self,arg):
      self.arg = int(arg)

   argString = property(_getArgString,_setArgString)

