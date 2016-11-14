/**
 * Render an article from the acm digital library
 */
resultRenderers["article"] = function(queryTerms, result, resDiv) {

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

    var html = '<table>';

    html += '<tr>' +
            '<td class="title">' + Render.getDocumentURL(url_art, title, queryTerms, result.rank) + '&nbsp;(<a target="_blank" href="view.html?kind=article&action=view&id=' + result.name + '">view OCR</a>)&nbsp;';
    for (r in result.ratings){
        if (result.ratings[r].rating == 1){
            html += '<span class="glyphicon glyphicon-ok"></span>'
        } else {
            html += '<span class="glyphicon glyphicon-remove"></span>'
        }
    }
            '</td><td class="citation">Citation: ' + highlightText(queryTerms, citation, '<span class="hili">', '</span>') + '</td>' +
            '<td class="score">' + result.score.toFixed(3) + ' r' + result.rank + '</td>' +
            '</tr>';

    if (proc != null) {
        html += '<tr>' +
                '<td class="proc">Published in: ' + Render.getDocumentURL(url_proc, proc, result.rank) ;
        html += '&nbsp;' + highlightText(queryTerms, pubyear, '<span class="hili">', '</span>', 'pubyear');
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

    html = html + '</table>';

    resDiv.html(html);
    return resDiv;
    //return html;
};

var onViewArticleSuccess = function (args) {

    // add a class that will show newlines, etc.
    $("#view-resource").addClass("acm-text");

    UI.clearError();
    metadataDiv.hide();

    var metaHtml = '';
    metaHtml += ' <table>';
    _(args.metadata).forIn(function (val, key) {
        metaHtml += '<tr>';
        metaHtml += '<td>' + key + '</td>';
        metaHtml += '<td>' + val + '</td>';
        metaHtml += '</tr>';
    });
    metaHtml += '</table></div>';
    metadataDiv.html(metaHtml);
    var id = args.request.id;
    // create a div around the text that we'll attach the annotator to.
    var html =  '<div id="notes-' + id + '"><a href="#" class="show-hide-metadata" onclick="UI.showHideMetadata();">Show Metadata</a>';

    html +=  args.text + '</div>';

    viewResourceDiv.html(html);
    viewResourceDiv.show();

    var corpus = getCookie("corpus");
    if (!isLoggedIn() || corpus == "")
        return;

    var corpus = getCookie("corpus");
    var userName = getCookie("username");
    var userToken = getCookie("token");
    var userID = getCookie("userid");
    var corpusID = getCorpusID(corpus);
    var cookieData = {
        "userID": userID,
        "userName": userName,
        "userToken": userToken,
        "corpus": corpus,
        "corpusID": corpusID
    }

    // add a doc specific class to attach notes to.
 //   $("#view-resource").addClass( "notes-" + id);

    // TODO : lots of duplicate code with onViewBookSuccess

    // if we have a "noteid" parameter, load that note first and
    // scroll to it. All the other notes will load in the background
    // via timeouts so the UI doesn't lock up when loading large books.
    var urlParams = getURLParams();
    var pgNum = urlParams["pgno"];
    if (_.isUndefined(pgNum)) {
        pgNum = -1;
    }
    el = "#notes-" + id;

    if (!_.isUndefined(urlParams["noteid"])) {
        // scroll to the note once all notes for that page are loaded.
        $(el).bind("annotationsLoaded", function () {

            var note = '.annotator-hl[data-annotation-id="' + urlParams["noteid"] + '"]';

            if ($(note).length){

                $('#results-right').animate({
                    scrollTop: $(note).offset().top - 80
                }, 2000);

            } else {
                alert("Couldn't find that note, perhaps it was deleted?")
            }

            // remove the notid from the URL so we don't re-trigger
            removeURLParam("noteid");

            // unbind once we're done
            $(el).unbind("annotationsLoaded");
        });

    } // end if we have a noteid

    // add the annotation widget
    initAnnotationLogic(el, cookieData);

    UI.showProgress("");

};
