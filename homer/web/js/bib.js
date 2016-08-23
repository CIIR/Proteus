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

  $("#bib-data").html('');
  $("#bib-data").append('TYPE' + FIELD_SEP + 'TITLE'+ FIELD_SEP + 'ARCHIVE_ID'+ FIELD_SEP)
  $("#bib-data").append('AUTHORS'+ FIELD_SEP + 'PUB_DATE'+ FIELD_SEP + 'PUBLISHER'+ FIELD_SEP)
  $("#bib-data").append('LANG'+ FIELD_SEP + 'OCLC'+ FIELD_SEP + 'HATHI_ID'+ FIELD_SEP)
  $("#bib-data").append('PAGE_NUM'+ FIELD_SEP + 'ARCHIVE_URL' + FIELD_SEP + 'MISC<br>')

  gBibArray.forEach(function (rec) {

    var id = parsePageID(rec.name);

    var type = 'BOOK';

    if (!_.isEmpty(id.note)) {
      type = 'NOTE';
    } else if (!_.isEmpty(id.page)) {
      type = 'PAGE';
    }
    // our id
    var data = ''; // rec.name + FIELD_SEP;

    data += type + FIELD_SEP;

    // title
    data += (rec.meta.title || rec.meta.TEI || rec.name) + FIELD_SEP;

    // archive id
    data += id.id + FIELD_SEP;

    // authors
    data += bibPrintUndefined(rec.meta.creator) + FIELD_SEP;

    // publication date
    data += bibPrintUndefined(rec.meta.date) + FIELD_SEP;

    data += bibPrintUndefined(rec.meta.publisher) + FIELD_SEP;

    data += bibPrintUndefined(rec.meta.language) + FIELD_SEP;

    data += bibPrintUndefined(rec.meta['oclc-id']) + FIELD_SEP;

    // Hathi Trust ID
    data += bibPrintUndefined(rec.hathiId) + FIELD_SEP;

    data += rec.realPage + FIELD_SEP;

    var url = archiveViewerURL(rec.name);
    data += '<a href="' + url + '">' + url + '</a>'  + FIELD_SEP;

    if (type == 'NOTE') {
      data += rec.snippet.replace(/<br>/g, " ");
    }

    $("#bib-data").append(data + '<br>')
  });
}

function bibPrintUndefined(field) {
  if (_.isUndefined(field)) {
    return '&nbsp;';
  }
  return field;
}

