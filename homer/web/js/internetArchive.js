/**
 * This file should hopefully encapsulate the different data source backends.
 *
 * No other code should construct URLs or know about the internet archive in general, 
 * so that we can generalize to the academic paper domain.
 *
 * URL format information for the Archive is at htjjtps://openlibrary.org/dev/docs/bookurls (as of March 2014)
 *
 */

var pageImage = function(archiveId, pageNum) {
  return "http://www.archive.org/download/" + archiveId + "/page/n" + pageNum + ".jpg";
};

var pageThumbnail = function(archiveId, pageNum) {
  return "http://www.archive.org/download/" + archiveId + "/page/n" + pageNum + "_thumb.jpg";
};

