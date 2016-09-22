/**
 * Created by michaelz on 8/22/2016.
 *
 * Functionality to export a corpus' bibliographic information.
 *
 */

// since there are async calls to get data, we have no assurance on
// the order the information will be printed. So we'll save
// the results ordered in an array so we can print them in
// order when we are done.
var gBibArray = new Array();

// when debugging, can use a visual character to see field separator
var FIELD_SEP = '\t'; //'<span style="color:blue;font-size:150%;">•</span>';

// keep track of how many records we've processed so we know when we're done.
var gBibDoneCount = 0;

function bibGetBookReader(next) {

  var id = parsePageID(this.name);
  // TODO really do need a cache for this
  var that = this;
  getInternetArchiveJS(id.id, function () {
    var page = '';
    var myBr = getBookReader(); // ???? should pass in book id?
    if (!_.isUndefined(myBr) && id.id == myBr.getBookID()) {

      if (_.isEmpty(id.page)) {
        // it's a book, so page number is irrelevant
        that.realPage = 'n/a';
      } else {
        that.realPage = myBr.getPageNumber(id.page);
        // the IA puts "null" for a page it's not sure of
        if (that.realPage == null) {
          that.realPage = '?';
        }
      }
    }
    next();
  });

}

function bibGetMetadata(next) {

  var id = parsePageID(this.name);
  // we'll always get the metadata from archive.org because it may have been updated
  // since the book was indexed and because for books with multiple authors (e.g. cu31924020438929)
  // we currently (August 2016) only store one author in the metadata.

  var args = {};
  var that = this;
  getInternetArchiveMetadata(id.id, args, function () {
    that.meta = args.metadata;
    next();
  });

}

function bibGetHathiTrustId(next) {

  if (_.isUndefined(this.meta['oclc-id'])) {
    next();
    return;
  }
  var that = this;
  $.getJSON("https://catalog.hathitrust.org/api/volumes/brief/oclc/" + this.meta['oclc-id'] + ".json")
          .done(function (rec) {

            var hathiId = '';
            if (rec.items.length > 0 && !_.isUndefined(rec.items[0].fromRecord)) {
              that.hathiId = rec.items[0].fromRecord;
            }
            next();
          })

}

function bibPrint() {

  gBibDoneCount += 1;
  if (gBibDoneCount < gBibArray.length) {
    return;
  }

  var DELMITER = ':' + FIELD_SEP;
  var NEWLINE = '&#13;&#10;';

  $("#bib-tabs").css("visibility", "visible");
  $("#bib-data").html('');
  var header = 'TYPE' + FIELD_SEP + 'TITLE' + FIELD_SEP + 'ARCHIVE_ID' + FIELD_SEP;
  header += 'AUTHORS' + FIELD_SEP + 'PUB_DATE' + FIELD_SEP + 'PUBLISHER' + FIELD_SEP;
  header += 'LANG' + FIELD_SEP + 'OCLC' + FIELD_SEP + 'HATHI_ID' + FIELD_SEP;
  header += 'PAGE_NUM' + FIELD_SEP + 'ARCHIVE_URL' + FIELD_SEP + 'MISC';

  var bibTSV = header + NEWLINE;
  var bibText = '';
  var tsvText = header + '\n';
  gBibArray.forEach(function (rec) {

    var id = parsePageID(rec.name);

    var type = 'BOOK';

    if (!_.isEmpty(id.note)) {
      type = 'NOTE';
    } else if (!_.isEmpty(id.page)) {
      type = 'PAGE';
    }

    bibText += 'Type' + DELMITER + type + NEWLINE;
    bibText += 'Title'  + DELMITER + (rec.meta.title || rec.meta.TEI || rec.name) + NEWLINE;
    bibText +=  'Archive ID'  + DELMITER + id.id + NEWLINE;
    bibText +=  'Author(s)'  + DELMITER +bibPrintUndefined(rec.meta.creator) + NEWLINE;
    bibText +=  'Pub Date'  + DELMITER + bibPrintUndefined(rec.meta.date) + NEWLINE;
    bibText +=  'Publisher'  + DELMITER +bibPrintUndefined(rec.meta.publisher) + NEWLINE;
    bibText +=  'Language'  + DELMITER +bibPrintUndefined(rec.meta.language) + NEWLINE;
    bibText +=  'OCLC ID'  + DELMITER +bibPrintUndefined(rec.meta['oclc-id']) + NEWLINE;
    bibText +=  'Hathi Trust ID'  + DELMITER +bibPrintUndefined(rec.hathiId) + NEWLINE;
    bibText +=  'Page #'  + DELMITER +rec.realPage + NEWLINE;

    bibTSV += type + FIELD_SEP;

    // title
    bibTSV += (rec.meta.title || rec.meta.TEI || rec.name) + FIELD_SEP;

    // archive id
    bibTSV += id.id + FIELD_SEP;

    // authors
    bibTSV += bibPrintUndefined(rec.meta.creator) + FIELD_SEP;

    // publication date
    bibTSV += bibPrintUndefined(rec.meta.date) + FIELD_SEP;

    bibTSV += bibPrintUndefined(rec.meta.publisher) + FIELD_SEP;

    bibTSV += bibPrintUndefined(rec.meta.language) + FIELD_SEP;

    bibTSV += bibPrintUndefined(rec.meta['oclc-id']) + FIELD_SEP;

    // Hathi Trust ID
    bibTSV += bibPrintUndefined(rec.hathiId) + FIELD_SEP;

    bibTSV += rec.realPage + FIELD_SEP;

    var url = archiveViewerURL(rec.name);

    bibTSV += url + FIELD_SEP;

    bibText +=  'Link'  + DELMITER + url + NEWLINE;

    if (type == 'NOTE') {
      // strip out any html tags
      var note = rec.snippet.replace(/<(?:.|\n)*?>/gm, " ");
      bibTSV += note ;
      bibText += 'Note'  + DELMITER + note + NEWLINE;
    }

    bibTSV += NEWLINE;
    bibText += NEWLINE;
  });

  // the "pre" is needed for newlines to be displayed when the text area is read only
  $("#bib-tsv").append('<textarea  readonly="yes" style="white-space: pre;" class="bib-textarea">' + bibTSV + '</textarea>')
  tsvText += bibTSV + '\n';

  $("#bib-text").append('<textarea  readonly="yes" style="white-space: pre;" class="bib-textarea">' + bibText + '</textarea>')

  // if the browser supports it, download the TSV file.
  // Code from: http://stackoverflow.com/questions/3665115/create-a-file-in-memory-for-user-to-download-not-through-server
  // This is not an ideal solution, there really is none client side. Ideally we could send the text to the server,
  // write it out to a file then serve that file. Right now, I don't see the ROI for that.
  try {
    tsvText = tsvText.replace(/&nbsp;/g, '');
    var tsvfile = window.document.createElement('a');
    tsvfile.href = window.URL.createObjectURL(new Blob([tsvText], {type: 'text/tab-separated-values'}));

    var p = getURLParams();
    tsvfile.download = p['name'] + '.tsv';

    // Append anchor to body.
    document.body.appendChild(tsvfile)
    tsvfile.click();

    // Remove anchor from body.
    document.body.removeChild(tsvfile)

  } catch (e) {
    console.log('Error creating TSV file: ' + e.toString());
  }
}


function bibPrintUndefined(field) {
  if (_.isUndefined(field)) {
    return '&nbsp;';
  }
  return field;
}

