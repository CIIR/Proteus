from subprocess import Popen, PIPE

Popen(['xmllint', '--noout', 'bad.xml'], stdout=PIPE)
