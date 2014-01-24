#!/usr/bin/python

import sys, os
from subprocess import call, Popen, PIPE

path = sys.argv[1]
dirs = os.listdir(path)

for file in dirs:
    output = open(file[0:len(file)-3],'w')
    #command = "gunzip -c " + path + "/" + file
    #print command
    #call(["gunzip", "-c", path + "/" + file, ">", file[0:len(file)-3]])
    call(['gunzip', '-c', path + '/' + file], stdout = output)
    output.close()
    Popen(['xmllint', '--noout', file[0:len(file)-3]], stdout=PIPE)
    call(['rm', file[0:len(file)-3]])

#gunzip
#xmllint --noout
#rm


