/**
 * No other code should construct URLs or know about the internet archive in general,
 * so that we can generalize to the academic paper domain.
 *
 * URL format information for the Archive is at https://openlibrary.org/dev/docs/bookurls (as of March 2014)
 *
 */

var bookImage = function(archiveId) {
    return pageImage(archiveId, "0");
}

var bookThumbnail = function(archiveId) {
    return pageThumbnail(archiveId, "0");
}

var pageImage = function(archiveId, pageNum) {
    return "http://www.archive.org/download/" + encodeURIComponent(archiveId) + "/page/n" + pageNum + ".jpg";
};

var pageThumbnail = function(archiveId, pageNum) {
    return "http://www.archive.org/download/" + encodeURIComponent(archiveId) + "/page/n" + pageNum + "_thumb.jpg";
};



//console.log("Defining table, renderResult=" + renderResult);
var renderResult = function(queryTerms, result, resDiv) {

    var name = result.meta.title || result.name;
    var identifier = result.name.split('_')[0];
    var snippet = result.snippet;
    var pageNum = result.name.split('_')[1];
    var iaURL = result.meta["identifier-access"];

    if (iaURL) {
        name = Render.getDocumentURL(iaURL, name, queryTerms, result.rank);
    }
    var pgImage = iaURL;
    if (!_.isUndefined(pageNum)) {
        // if page result - make the link go to the page
        name = Render.getDocumentURL('https://archive.org/stream/' + identifier + '#page/n' + pageNum + '/mode/2up', result.meta.title || result.name, queryTerms, result.rank);

        // MCZ : removing page number for now as it does not match up with 
        // the physical page number shown on the page
        //name += ' pp. ' + pageNum;
        pgImage = pageImage(identifier, pageNum);
    }
    var thumbnail = '<img class="thumbnail" src="' + pageThumbnail(identifier, pageNum) + '"/>';
    var previewImage = Render.getDocumentURL(pgImage, thumbnail, queryTerms, result.rank);

    var html =
            '<div class="result">' +
            '<table>' +
            '<tr>' +
            '<td class="preview" rowspan="2">' + previewImage + '</td>' +
            '<td class="name">' + name + '</td>' +
            '<td class="score">&nbsp;&nbsp;' + result.score.toFixed(3) + ' r' + result.rank + '</td>' +
            '</tr>';
    if (snippet) {
        html += '<tr><td class="snippet" colspan="2"> ...';
        html += highlightText(queryTerms, snippet, '<span class="hili">', '</span>');
        html += '... </td></tr>';
    }
    html += '</table>';
    if (result.tags) {
        html += UI.renderTags(result);
    }
    resDiv.html(html);
    return resDiv;

};

resultRenderers["ia-books"] = renderResult;
resultRenderers["ia-pages"] = renderResult;
resultRenderers["ia-all"] = renderResult;


