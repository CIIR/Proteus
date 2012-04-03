import glob
import sys
import time

while len(glob.glob(sys.argv[1] + "*")) > 0:
    time.sleep(1)

print "DONE"
