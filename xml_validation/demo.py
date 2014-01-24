from subprocess import Popen, PIPE

gproc = Popen(['gunzip', '-c','bad.xml.gz'], stdout=PIPE)
gout = gproc.communicate()[0]
xproc = Popen(['xmllint', '--noout', '-'], stdin=PIPE, stdout=PIPE)
xout = xproc.communicate(input=gout)[0]
