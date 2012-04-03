import scala.io.Source

val suffix = ".trecweb"

val threshold = 250000
var counter = 0
var file_counter = 0

println("Splitting file: " + args(0) + " into multiple files of the form: " + args(1) + "123" + suffix)

val base_name = args(1) // the prefix for files being created
var last_subj = ""
var current_file = new java.io.PrintWriter(base_name + file_counter + suffix)
val infile = args(0) // input file to split up

for (line <- Source.fromFile(infile).getLines) {
    val subj = line.split(" ")(0)
    if (counter >= threshold && subj != last_subj) {
       counter = 0
       // switch files
       current_file.close
       file_counter += 1
       current_file = new java.io.PrintWriter(base_name + file_counter + suffix)
    }

    // Write to the file    
    current_file.println(line)
    last_subj = subj
    counter += 1
}

current_file.close
