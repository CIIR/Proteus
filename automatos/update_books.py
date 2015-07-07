import subprocess, os, sys, time
#add a book to an index

#- Pontos: djvu, etc to toktei (dasmith)
#- Phokas: toktei to mbtei (dasmith)
# - Homer: Subproject built to contain all the indexing code and web front end for Proteus. Uses Galago 3.8-SNAPSHOT. Requires Java 8.

def main():
    #arguments: optional parameter, djvu file location, output directory
    mode = 'update'
    djvu_list_location = ''
    primary_work_directory = os.getcwd()

    if sys.argv[1] == '-all':
        mode = sys.argv[1][1:]
        djvu_directory_location = sys.argv[2]
        output_directory = sys.argv[3]
    elif sys.argv[1] == '-list':
        mode = sys.argv[1][1:]
        djvu_list_location = sys.argv[2]
        djvu_directory_location = sys.argv[3]
        output_directory = sys.argv[4]
    else:
        djvu_directory_location = sys.argv[1]
        output_directory = sys.argv[2]        

    #clean directory paths
    if(djvu_directory_location[-1] == '/'):
        djvu_directory_location = djvu_directory_location[:-1]
    if(djvu_directory_location[-1] == '/'):
        djvu_directory_location = output_directory[:-1]

    current_time = time.time()

    if mode == 'all' or mode == 'update':
        data = create_djvu_file_list(mode, djvu_directory_location, primary_work_directory)
        djvu_list_location = data[0]
        djvu_file_paths = data[1]     
    elif mode == 'list':
        djvu_file_paths = []
        reader = open(djvu_list_location,'r')
        for line in reader:
            #check that each file exists
            if os.path.exists(djvu_directory_location + '/' + line):
                djvu_file_paths.append(line)
            else:
                print ('ERROR: %s does not exist' % (djvu_directory_location + '/' + line))
        reader.close()

    writer = open('timestamp.txt','w')
    writer.write(str(current_time))
    writer.close()
    

    primary_work_directory = os.getcwd()
       

    djvu_to_rawtei(djvu_list_location, output_directory, primary_work_directory, djvu_directory_location)
    if mode == 'all' or mode == 'update':
        os.remove(djvu_list_location)
    #convert toktei to mbtei using Phokas
    rawtei_to_toktei(djvu_file_paths, output_directory, primary_work_directory)
    toktei_to_mbtei(djvu_file_paths, output_directory, primary_work_directory)
    #index using Homer
    build_index(djvu_file_paths, output_directory, primary_work_directory)

def create_djvu_file_list(mode, djvu_directory_location, primary_work_directory):
    #create temp list file using djvu_file_location

    candidate_files = os.listdir(djvu_directory_location)
    if os.path.exists('timestamp.txt'):
        reader = open('timestamp.txt','r')
        timestamp = float(reader.readline().strip())
        reader.close()
    else:
        timestamp = 0

    temp_list_location = primary_work_directory + '/book_list.txt'
    djvu_file_paths = []
    temp_list_writer = open(temp_list_location,'w')
    actual_files = []
    #find all files within the directory hierarchy
    print('Files Discovered:')
    for cf in candidate_files:   
        print(cf)
        if os.path.isdir(djvu_directory_location + '/' + cf):
            for fn in os.listdir(djvu_directory_location + '/' + cf):
                print(cf + '/' + fn)
                if not os.path.isdir(djvu_directory_location + '/' + cf + '/' + fn):
                    actual_files.append(cf + '/' + fn)
        else:
            actual_files.append(cf)
    print('Files to be Converted:')
    for fn in actual_files:
        if '_djvu.xml.bz2' in fn:
            if (not mode == 'update') or (timestamp < os.stat(djvu_directory_location + '/' + fn).st_mtime):
                print(fn)
                temp_list_writer.write(fn+'\n')
                djvu_file_paths.append(fn)
    temp_list_writer.close()
    djvu_list_location = temp_list_location
    return [temp_list_location,djvu_file_paths]

def create_tei_file_list(output_directory_location, primary_work_directory):
    #create temp list file using output_directory_location

    tei_directory_location = output_directory_location
    candidate_files = os.listdir(tei_directory_location)

    temp_list_location = primary_work_directory + '/temp_tei_list.list'
    tei_file_paths = []
    temp_list_writer = open(temp_list_location,'w')
    actual_files = []
    #find all files within the directory hierarchy
    print('Files Discovered:')
    for cf in candidate_files:   
        print(cf)
        if os.path.isdir(tei_directory_location + '/' + cf):
            for fn in os.listdir(tei_directory_location + '/' + cf):
                print(cf + '/' + fn)
                if not os.path.isdir(tei_directory_location + '/' + cf + '/' + fn):
                    actual_files.append(cf + '/' + fn)
        else:
            actual_files.append(cf)
    print('Files to be Indexed:')
    for fn in actual_files:
        if '.toktei.gz' in fn:
            print(fn)
            temp_list_writer.write(output_directory_location + '/' + fn+'\n')
            tei_file_paths.append(fn)
    temp_list_writer.close()
    return temp_list_location

def djvu_to_rawtei(djvu_list_location, output_directory, primary_work_directory, input_directory):
    #convert djvu to toktei using Pontos
    #proc = subprocess.Popen("pontos-0.1.0-SNAPSHOT [options] <list of djvu or input files>.gz", stdout=subprocess.PIPE)

    
    command = 'cat %s | java -jar ../pontos/pontos-0.1.0-SNAPSHOT-standalone.jar --input %s --output %s' % (djvu_list_location,input_directory,output_directory)
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
        temp_list_writer.write(output_directory + '/' + path +'\n')
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
        temp_list_writer.write(output_directory + '/' + path +'\n')
    temp_list_writer.close()
    
    command = 'cat %s | java -jar ../phokas/phokas-1.0.0-SNAPSHOT-standalone.jar' % (temp_list_location)
    print(command)
    proc = subprocess.Popen(command, stdout=subprocess.PIPE, shell=True)
    for l in proc.stdout:
        print(l.decode().strip())
    os.remove(temp_list_location)

def build_index(djvu_file_paths, output_directory_location, primary_work_directory):
    temp_list_location = primary_work_directory + '/book_list.list'
    temp_list_writer = open(temp_list_location,'w')
    for path in djvu_file_paths:
        path = path.replace('_djvu.xml.bz2','.toktei.gz')
        temp_list_writer.write(output_directory_location + '/' + path +'\n')
    temp_list_writer.close()
    
    if not os.path.exists("raw-entity-docs"):
       os.mkdir("raw-entity-docs")
    if not os.path.exists("entity-docs"):
       os.mkdir("entity-docs")

    #build list of all tei books
    temp_list_tei_location = create_tei_file_list(output_directory_location, primary_work_directory)
    

    command = 'java -jar ../homer/target/homer-0.4-SNAPSHOT.jar build ../homer/scripts/pages.conf --server=false --indexPath=demo.pages --inputPath=%s' % (temp_list_tei_location)
    print(command)
    proc = subprocess.Popen(command, stdout=subprocess.PIPE, shell=True)
    for l in proc.stdout:
        print(l.decode().strip())

    command = 'java -Xmx9g -Xms9g -jar ../homer/target/homer-0.4-SNAPSHOT.jar build ../homer/scripts/books_ner.conf --server=false --indexPath=demo.books --inputPath=%s' % (temp_list_tei_location)
    print(command)
    proc = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
    #msg_writer = open('books_indexing.out','w')
    for l in proc.stdout:
        print(l.decode().strip())
    for l in proc.stderr:
        print(l.decode().strip())
        #msg_writer.write(l.decode())
    #msg_writer.close()

    command = 'java -cp ../homer/target/homer-0.4-SNAPSHOT.jar ciir.proteus.parse.NamedEntityDocumentGenerator raw-entity-docs'
    print(command)
    proc = subprocess.Popen(command, stdout=subprocess.PIPE, shell=True)
    for l in proc.stdout:
        print(l.decode().strip())

    command = 'java -jar ../homer/target/homer-0.4-SNAPSHOT.jar build --server=false --indexPath=person.index --inputPath=entity-docs --tokenizer/fields+"location" --tokenizer/fields+"alias"'
    print(command)
    proc = subprocess.Popen(command, stdout=subprocess.PIPE, shell=True)
    for l in proc.stdout:
        print(l.decode().strip())

    os.remove(temp_list_location)
    os.remove(temp_list_tei_location)

main()
