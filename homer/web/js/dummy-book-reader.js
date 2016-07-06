/**
 * Created by michaelz on 7/6/2016.
 */

// Dummy BookReader class for IA script. I stripped out
// all of the code since I'm only interested in the page numbers
// original source: http://github.com/openlibrary/bookreader/
// Note, the script will create a  variable "br" which we use to access the page numbers.
// We create the "br" variable here to avoid errors such as
// "Uncaught ReferenceError: br is not defined" if the IA script fails for some reason.
var br;
function BookReader() {};
BookReader.prototype.init = function() { return true;}
function getBookReader() { return br;};
