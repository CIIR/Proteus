import subprocess, os, sys, time
#add a book to an index

#- Pontos: djvu, etc to toktei (dasmith)
#- Phokas: toktei to mbtei (dasmith)
# - Homer: Subproject built to contain all the indexing code and web front end for Proteus. Uses Galago 3.8-SNAPSHOT. Requires Java 8.

def main():
    #arguments: optional parameter, djvu file location, output directory
    mode = 'update'
    primary_work_directory = os.getcwd()

    if sys.argv[1] == '-all' or sys.argv[1] == '-list':
        mode = sys.argv[1][1:]
        djvu_directory_location = sys.argv[2]
        output_directory = sys.argv[3]
    else:
        djvu_directory_location = sys.argv[1]
        output_directory = sys.argv[2]        

    current_time = time.time()

    if mode == 'all' or mode == 'update':
        data = create_file_list(mode, djvu_directory_location, primary_work_directory)
        djvu_list_location = data[0]
        djvu_file_paths = data[1]     
    else:
        djvu_list_location = djvu_directory_location
        djvu_file_paths = []
        reader = open(djvu_list_location,'r')
        for line in reader:
            djvu_file_paths.append(line)
        reader.close()

    writer = open('timestamp.txt','w')
    writer.write(str(current_time))
    writer.close()
    

    primary_work_directory = os.getcwd()
       

    djvu_to_rawtei(djvu_list_location, output_directory, primary_work_directory)
    if mode == 'all' or mode == 'update':
        os.remove(djvu_list_location)
    #convert toktei to mbtei using Phokas
    rawtei_to_toktei(djvu_file_paths, output_directory, primary_work_directory)
    toktei_to_mbtei(djvu_file_paths, output_directory, primary_work_directory)
    #index using Homer
    build_index(djvu_file_paths, output_directory, primary_work_directory)

def create_file_list(mode, djvu_directory_location, primary_work_directory):
    #create temp list file using djvu_file_location

    djvu_dirs = os.listdir(djvu_directory_location)
    if os.path.exists('timestamp.txt'):
        reader = open('timestamp.txt','r')
        timestamp = float(reader.readline().strip())
        reader.close()
    else:
        timestamp = 0

    temp_list_location = primary_work_directory + '/book_list.txt'
    djvu_file_paths = []
    temp_list_writer = open(temp_list_location,'w')
    for djvu_dir in djvu_dirs:
        for f in os.listdir(djvu_directory_location + '/' + djvu_dir):
            if '_djvu.xml.bz2' in f:
                if (not mode == 'update') or (timestamp < os.stat(djvu_directory_location + '/' + djvu_dir + '/' + f).st_mtime):
                    temp_list_writer.write(djvu_dir + '/' + f)
                    djvu_file_paths.append(djvu_dir + '/' + f)
    temp_list_writer.close()
    djvu_list_location = temp_list_location
    return [temp_list_location,djvu_file_paths]

def djvu_to_rawtei(djvu_list_location, output_directory, primary_work_directory):
    #convert djvu to toktei using Pontos
    #proc = subprocess.Popen("pontos-0.1.0-SNAPSHOT [options] <list of djvu or input files>.gz", stdout=subprocess.PIPE)

    
    command = 'cat %s | java -jar ../pontos/pontos-0.1.0-SNAPSHOT-standalone.jar --input /home/wem/book/djvu/ --output %s' % (djvu_list_location,output_directory)
    print(command)
    #proc = subprocess.Popen('pontos-0.1.0-SNAPSHOT [options] <list of djvu or input files>.gz', stdout=subprocess.PIPE)
    proc = subprocess.Popen(command, stdout=subprocess.PIPE, shell=True)
    for l in proc.stdout:
        print(l.decode().strip())

def rawtei_to_toktei(djvu_file_paths, output_directory, primary_work_directory):

    #file_name = djvu_file_location.replace('_djvu.xml.bz2','.toktei.gz')

    temp_list_location = primary_work_directory + '/book_list.txt'
    temp_list_writer = open(temp_list_location,'w')
    for path in djvu_file_paths:
        path = path.replace('_djvu.xml.bz2','.toktei.gz')
        temp_list_writer.write(output_directory + path)
    temp_list_writer.close()
    
    command = 'cat %s | java -jar ../phokas/phokas-1.0.0-SNAPSHOT-standalone.jar' % (temp_list_location)
    print(command)
    proc = subprocess.Popen(command, stdout=subprocess.PIPE, shell=True)
    for l in proc.stdout:
        print(l.decode().strip())
    os.remove(temp_list_location)

def toktei_to_mbtei(djvu_file_paths, output_directory, primary_work_directory):

    #file_name = djvu_file_location.replace('_djvu.xml.bz2','.mbtei.gz')

    temp_list_location = primary_work_directory + '/book_list.txt'
    temp_list_writer = open(temp_list_location,'w')
    for path in djvu_file_paths:
        path = path.replace('_djvu.xml.bz2','.mbtei.gz')
        temp_list_writer.write(output_directory + path)
    temp_list_writer.close()
    
    command = 'cat %s | java -jar ../phokas/phokas-1.0.0-SNAPSHOT-standalone.jar' % (temp_list_location)
    print(command)
    proc = subprocess.Popen(command, stdout=subprocess.PIPE, shell=True)
    for l in proc.stdout:
        print(l.decode().strip())
    os.remove(temp_list_location)

def build_index(djvu_file_paths, output_directory, primary_work_directory):
    temp_list_location = primary_work_directory + '/book_list.txt'
    temp_list_writer = open(temp_list_location,'w')
    for path in djvu_file_paths:
        path = path.replace('_djvu.xml.bz2','.toktei.gz')
        temp_list_writer.write(output_directory + path)
    temp_list_writer.close()
    
    command = 'java -jar ../homer/target/homer-0.4-SNAPSHOT.jar build ../homer/scripts/pages.conf --server=false --indexPath=demo.pages --inputPath=%s' % (temp_list_location)
    print(command)
    proc = subprocess.Popen(command, stdout=subprocess.PIPE, shell=True)
    for l in proc.stdout:
        print(l.decode().strip())

    command = 'java -jar ../homer/target/homer-0.4-SNAPSHOT.jar build ../homer/scripts/books.conf --server=false --indexPath=demo.books --inputPath=%s' % (temp_list_location)
    print(command)
    proc = subprocess.Popen(command, stdout=subprocess.PIPE, shell=True)
    for l in proc.stdout:
        print(l.decode().strip())

    os.remove(temp_list_location)

main()
