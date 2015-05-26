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
    var kind = 'ia-books'; // default
    var thumbnail = '<img class="ia-thumbnail" src="' + pageThumbnail(identifier, pageNum) + '"/>';
    var previewImage = Render.getDocumentURL(pgImage, thumbnail, queryTerms, result.rank);

    if (!_.isUndefined(pageNum)) {
        kind = 'ia-pages';

        // if page result - make the link go to the page
        name = Render.getDocumentURL('https://archive.org/stream/' + identifier + '#page/n' + pageNum + '/mode/2up', result.meta.title || result.name, queryTerms, result.rank);

        // MCZ : removing page number for now as it does not match up with 
        // the physical page number shown on the page
        //name += ' pp. ' + pageNum;
        pgImage = pageImage(identifier, pageNum);
        previewImage = Render.getPagePreviewURL(pgImage, thumbnail, queryTerms, result.rank);
    }

  var tmphtml = '';

  if (!_.isUndefined(result.entities)){

    _(result.entities).forEach( function(entKey) {
      _( entKey ).forIn( function(value, key) {
        tmphtml += '<div align="left"><b>' + key + ':</b> ';
        _( value ).forIn( function(rec, key2) {
          // TODO: whould use default kind not hard code
          tmphtml +=  '<a onclick="tmpEntSearch(\'' + key + '\', $(this), \'ia-all\')">' + rec.entity +  '</a> (' + rec.count + ')&nbsp;&#8226;&nbsp;';
        });
      });
      tmphtml += '</div>';
    });
  } // end if we have entities

    var html =
            '<table>' +
            '<tr>' +
            '<td class="preview" rowspan="2">' + previewImage + '</td>' +
            '<td class="name">' + name + '&nbsp;(<a target="_blank" href="?kind=' + kind +'&action=view&id=' + result.name + '">view OCR</a>)</td>' +
            '<td class="slider-rating"><div id="rating-' + result.name + '" class="rainbow-slider"></div></td>'+
            '<td class="score">&nbsp;&nbsp;rank: ' + result.rank + '</td>' +
            '</tr>' ;

    if (snippet) {
        html += '<tr><td class="snippet" colspan="3"> ...';
        html += highlightText(queryTerms, snippet, '<span class="hili">', '</span>');
        html += '... </td></tr>';
    }

  html += '</table>';
  html +=  tmphtml  ;
    if (result.tags) {
        html += UI.renderTags(result);
    }
    resDiv.html(html);

    return resDiv;

};

resultRenderers["ia-books"] = renderResult;
resultRenderers["ia-pages"] = renderResult;
resultRenderers["ia-all"] = renderResult;


