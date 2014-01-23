from subprocess import Popen, PIPE

gproc = Popen(['gunzip', '-c','bad.xml.gz'], stdout=PIPE)
gproc.wait()
for line in gproc.stdout:
    print line
xproc = Popen(['xmllint', '--noout'], stdin=gproc.stdout, stdout=PIPE)
xproc.wait()
for line in xproc.stdout:
    print line
