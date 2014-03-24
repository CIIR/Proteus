/**
 * This file should hopefully encapsulate the different data source backends.
 *
 * No other code should construct URLs or know about the internet archive in general, 
 * so that we can generalize to the academic paper domain.
 *
 */

var pageImage = function(archiveId, pageNum) {
	return "http://www.archive.org/download/" + archiveId + "/page/n"	+ pageNum + ".jpg";
};

var pageThumbnail = function(archiveId, pageNum) {
	return "http://www.archive.org/download/" + archiveId + "/page/n"	+ pageNum + "_thumb.jpg";
};

