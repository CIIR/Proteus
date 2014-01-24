#!/usr/bin/python

import sys, os
from subprocess import call, Popen, PIPE

path = sys.argv[1]
dirs = os.listdir(path)

for file in dirs:
    print 'opening ' + file
    gproc = Popen(['gunzip', '-c',path + '/' + file], stdout=PIPE)
    gout = gproc.communicate()[0]
    xproc = Popen(['xmllint', '--noout', '-'], stdin=PIPE, stdout=PIPE)
    xout = xproc.communicate(input=gout)[0]
    print xout
