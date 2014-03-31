
# list
l = [0,1,2,3,4]

# simple slice
print l[0:0]             # []
print l[1:2]             # [1]  
print l[-2:-1]           # [3] 
print l[-100:100]        # [0, 1, 2, 3, 4] 
print l[100:-100]        # []
print l[:]               # [0, 1, 2, 3, 4]  
print l[1:None]          # [1, 2, 3, 4]
print l[None:3]          # [0, 1, 2]

# extended slice
print l[::]              # [0, 1, 2, 3, 4]
print l[::2]             # [0, 2, 4]    
print l[1::2]            # [1, 3]
print l[::-1]            # [4, 3, 2, 1, 0]
print l[::-2]            # [4, 2, 0]
print l[3::-2]           # [3, 1]
print l[3:3:-2]          # []
print l[3:2:-2]          # [3]
print l[3:1:-2]          # [3]
print l[3:0:-2]          # [3, 1]
print l[::-100]          # [4]
print l[100:-100:]       # []
print l[-100:100:]       # [0, 1, 2, 3, 4]
print l[100:-100:-1]     # [4, 3, 2, 1, 0]
print l[-100:100:-1]     # []
print l[-100L:100L:2L]   # [0, 2, 4]


# tuple
t = (0,1,2,3,4)

# simple slice
print t[0:0]             # ()
print t[1:2]             # (1,)  
print t[-2:-1]           # (3,) 
print t[-100:100]        # (0, 1, 2, 3, 4) 
print t[100:-100]        # ()
print t[:]               # (0, 1, 2, 3, 4)  
print t[1:None]          # (1, 2, 3, 4)
print t[None:3]          # (0, 1, 2)

# extended slice
print t[::]              # (0, 1, 2, 3, 4)
print t[::2]             # (0, 2, 4)    
print t[1::2]            # (1, 3)
print t[::-1]            # (4, 3, 2, 1, 0)
print t[::-2]            # (4, 2, 0)
print t[3::-2]           # (3, 1)
print t[3:3:-2]          # ()
print t[3:2:-2]          # (3,)
print t[3:1:-2]          # (3,)
print t[3:0:-2]          # (3, 1)
print t[::-100]          # (4,)
print t[100:-100:]       # ()
print t[-100:100:]       # (0, 1, 2, 3, 4)
print t[100:-100:-1]     # (4, 3, 2, 1, 0)
print t[-100:100:-1]     # ()
print t[-100L:100L:2L]   # (0, 2, 4)


# string
s = "01234"

# simple slice
print s[0:0]             # ''
print s[1:2]             # '1,'  
print s[-2:-1]           # '3,' 
print s[-100:100]        # '0, 1, 2, 3, 4' 
print s[100:-100]        # ''
print s[:]               # '0, 1, 2, 3, 4'  
print s[1:None]          # '1, 2, 3, 4'
print s[None:3]          # '0, 1, 2'

# exsended slice
print s[::]              # '0, 1, 2, 3, 4'
print s[::2]             # '0, 2, 4'    
print s[1::2]            # '1, 3'
print s[::-1]            # '4, 3, 2, 1, 0'
print s[::-2]            # '4, 2, 0'
print s[3::-2]           # '3, 1'
print s[3:3:-2]          # ''
print s[3:2:-2]          # '3,'
print s[3:1:-2]          # '3,'
print s[3:0:-2]          # '3, 1'
print s[::-100]          # '4,'
print s[100:-100:]       # ''
print s[-100:100:]       # '0, 1, 2, 3, 4'
print s[100:-100:-1]     # '4, 3, 2, 1, 0'
print s[-100:100:-1]     # ''
print s[-100L:100L:2L]   # '0, 2, 4'


# multiple assignment
a, b = [1, 2]
