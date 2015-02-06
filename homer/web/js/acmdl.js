/**
 * Render an article from the acm digital library
 */
resultRenderers["article"] = function(queryTerms, result, resDiv) {

    console.log(Model.query);
    var title = result.meta["title"];
    var snippet = result.snippet;

    var artid = result.meta["artid"];
    var author = result.meta["author"];
    var institution = result.meta["institution"];
    var proc = result.meta["proc"];
    var procid = result.meta["procid"];
    var citation = result.meta["citation"];
    var pubyear = result.meta["pubyear"];

    var url_art = "http://dl.acm.org/";
    var url_proc = "http://dl.acm.org/";

    if (artid != null) {
        url_art = url_art + "citation.cfm?id=" + encodeURIComponent(artid);
    }

    if (procid != null) {
        url_proc = url_proc + "citation.cfm?id=" + encodeURIComponent(procid);
    }

    var html = '<div class="result"><table>';

    html += '<tr>' +
            '<td class="title">' + Render.getDocumentURL(url_art, title, queryTerms, result.rank) + '</td>' +
            '<td class="citation">Citation: ' + highlightText(queryTerms, citation, '<span class="hili">', '</span>') + '</td>' +
            '<td class="score">' + result.score.toFixed(3) + ' r' + result.rank + '</td>' +
            '</tr>';

    if (proc != null) {
        html += '<tr>' +
                '<td class="proc">Published in: <a href="' + url_proc + '">';
        html += highlightText(queryTerms, proc, '<span class="hili">', '</span>', 'proc');
        html += '</a>, ';
        html += highlightText(queryTerms, pubyear, '<span class="hili">', '</span>', 'pubyear');
        html += '</td></tr>';
    }

    if (author != null) {
        html += '<tr><td class="author">Written by: ';
        html += highlightText(queryTerms, author, '<span class="hili">', '</span>', 'author');
        html += '</td></tr>';
    }

    if (institution != null) {
        html += '<tr><td class="insti">From: ';
        html += highlightText(queryTerms, institution, '<span class="hili">', '</span>', 'institution');
        html += '</td></tr>';
    }

    if (snippet != null) {
        html += '<tr><td class="snippet" colspan="2"> ...';
        html += highlightText(queryTerms, snippet, '<span class="hili">', '</span>');
        html += '... </td></tr>';
    }

    html = html + '</table></div>';
    if (result.tags) {
        html += UI.renderTags(result);
    } // end if display tags
    resDiv.html(html);
    return resDiv;
    //return html;
};


