import subprocess, os

def setup():
    #steps:
    #generate directories if not created
    #compile all code
    primary_work_directory = os.getcwd()
    # build pontos
    os.chdir('../pontos')
    proc = subprocess.Popen(['lein','uberjar'], stdout=subprocess.PIPE)
    for l in proc.stdout:
        print(l.decode().strip())

    #build phokas
    os.chdir('../phokas')
    proc = subprocess.Popen(['chmod u+x deps.sh'], stdout=subprocess.PIPE, shell=True)
    for l in proc.stdout:
        print(l.decode().strip())
    if not os.path.exists('deps.sh'):
        proc = subprocess.Popen(['./deps.sh'], stdout=subprocess.PIPE, shell=True)
        for l in proc.stdout:
            print(l.decode().strip())
    proc = subprocess.Popen(['lein','uberjar'], stdout=subprocess.PIPE)
    for l in proc.stdout:
        print(l.decode().strip())

    #build homer
    os.chdir('../homer')
    proc = subprocess.Popen(['mvn','package'], stdout=subprocess.PIPE)
    for l in proc.stdout:
        print(l.decode().strip())

    #return to the automatos directory
    os.chdir('../automatos')
    return primary_work_directory

setup()
