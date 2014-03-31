import marshal
import time, struct, sys

# sys.argv[2] is the absolute path of the benchmark to be unmarshalled
# sys.argv[1] is the real argument to be passed to the benchmark

benchmark = sys.argv[2]
f = open(benchmark, "r+b")
magic = f.read(4)
moddate = f.read(4)
#modtime = time.asctime(time.localtime(struct.unpack('L', moddate)[0]))
#print "magic %s" % (magic.encode('hex'))
#print "moddate %s (%s)" % (moddate.encode('hex'), modtime)

exec marshal.load(f)
