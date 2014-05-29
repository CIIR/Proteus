/**
 * No other code should construct URLs or know about the internet archive in general,
 * so that we can generalize to the academic paper domain.
 *
 * URL format information for the Archive is at https://openlibrary.org/dev/docs/bookurls (as of March 2014)
 *
 */

var bookImage = function(archiveId){
	return pageImage(archiveId,"0");
}

var bookThumbnail = function(archiveId){
	return pageThumbnail(archiveId, "0");
}

var pageImage = function(archiveId, pageNum) {
	return "http://www.archive.org/download/" + archiveId + "/page/n" + pageNum + ".jpg";
};

var pageThumbnail = function(archiveId, pageNum) {
	return "http://www.archive.org/download/" + archiveId + "/page/n" + pageNum + "_thumb.jpg";
};



console.log("Defining table, renderResult="+renderResult);
var renderResult = function(queryTerms, result) {

	var name = result.meta.title || result.name;
	var identifier = result.name.split('_')[0];
	var snippet = result.snippet;
	var pageNum= result.name.split('_')[1];
	var iaURL = result.meta["identifier-access"];


	console.log(result);

	if (iaURL) {


		name = '<a href="'+iaURL+'">' + name + '</a>';
	}
	name += ' pp. ' + pageNum;

	var previewImage = '<a href="'+ pageImage(identifier, pageNum) +'">' +
	    '<img class="thumbnail" src="' + pageThumbnail(identifier, pageNum) + '"/>' +
	     '</a>';
	var html =
		'<div class="result">' +
		'<table>' +
		'<tr>' +
		'<td class="preview" rowspan="2">' + previewImage + '</td>' +
		'<td class="name">' + name + '</td>' +
		'<td class="score">' + result.score.toFixed(3) + ' r'+ result.rank + '</td>' +
		'</tr>';
	if(snippet) {
		html += '<tr><td class="snippet" colspan="2"> ...';
		html += highlightText(queryTerms, snippet, '<span class="hili">','</span>');
		html += '... </td></tr>';
	}
	html += '</table>';

	return html;


};

resultRenderers["ia-books"] = renderResult;
resultRenderers["ia-pages"] = renderResult;


