/**
 * No other code should construct URLs or know about the internet archive in general,
 * so that we can generalize to the academic paper domain.
 *
 * URL format information for the Archive is at https://openlibrary.org/dev/docs/bookurls (as of March 2014)
 *
 */

var pageImage = function(pageid, meta) {
    if ( meta.page_image ) {
	return meta.page_image;
    } else if ( meta['identifier-access'] != undefined ) {
	var id = pageid.split("_");
	return "http://www.archive.org/download/" + encodeURIComponent(id[0]) + "/page/n" + id[1] + ".jpg";
    } else {
	return 'images/thumb.png';
    }
};

var pageThumbnail = function(pageid, meta) {
    if ( meta.page_thumb ) {
	return meta.page_thumb;
    } else if ( meta['identifier-access'] != undefined ) {
	var id = pageid.split("_");
	return "http://www.archive.org/download/" + encodeURIComponent(id[0]) + "/page/n" + id[1] + "_thumb.jpg";
    } else {
	return 'images/thumb.png';
    }
};

var archiveViewerURL = function(pageid, meta) {
    if ( meta.page_access ) {
	return meta.page_access;
    } else if ( meta['identifier-access'] != undefined ) {
	var id = pageid.split("_");
	return 'https://archive.org/stream/' + id[0] + '#page/n' + id[1] + '/mode/2up';
    } else {
	return '';
    }
};

var renderResult = function(queryTerms, result, resDiv, queryid) {

    var name = result.meta.title || result.meta.TEI || result.name;
    var identifier = result.name.split('_')[0];
    var docid = result.name;
    var snippet = result.snippet;
    var pageNum = result.name.split('_')[1];
    var iaURL = result.meta["identifier-access"] || result.meta.book_access;
    var nameLink = '';

    if (iaURL) {
        nameLink = Render.getDocumentURL(iaURL, name, queryTerms, result.rank, identifier);
    }
    var pgImage = iaURL;
    var kind = 'ia-books'; // default

    // if this is a book result - show the front page as the thumbnail but the links will
    // go to the max passage page.

    var thumbnail = '<img class="ia-thumbnail" src="' + pageThumbnail(result.name, result.meta) + '"/>';

    var previewImage = Render.getDocumentURL(pgImage, thumbnail, queryTerms, result.rank, identifier, true);

    if (!_.isUndefined(pageNum)) {
        kind = 'ia-pages';
        // if page result - make the link go to the page
        nameLink = Render.getDocumentURL(archiveViewerURL(result.name, result.meta), name, queryTerms, result.rank, identifier);

        // MCZ : removing page number for now as it does not match up with
        // the physical page number shown on the page
        //name += ' pp. ' + pageNum;
        pgImage = pageImage(result.name, result.meta);
        previewImage = Render.getPagePreviewURL(pgImage, thumbnail, queryTerms, result.rank);

        // check if we're a note. Can either check metadata "docType" = "note" or there are two underscores
        // TODO : this should be moved up
        if (!_.isUndefined(result.meta)) {
            if (result.meta.docType == "note") {
                return renderNoteResult(queryTerms, result, resDiv);
            }
        }
    }
    if (kind == 'ia-books' && !_.isUndefined(result.snippetPage) ){
        docid = identifier + "_" + result.snippetPage;
        nameLink = Render.getDocumentURL(archiveViewerURL(docid, result.meta), name, queryTerms, result.rank, identifier);
    }

    var tmphtml = '';

    if (!_.isUndefined(result.entities)) {

        // the "kind" variable right now represents the kind for this specific search result.
        // But for the link to search by entity we want the "kind" that we orginally
        // searched for, which is on the URL.
        // So if we searched "ia-all" (both books and pages) we want this to search "ia-all" too
        // not just books or pages.

        var urlParams = getURLParams();
        var currentKind = urlParams["kind"];
        if (_.isUndefined(currentKind)) {
            // we should be guarenteed to have a kind on the URL, but since
            // they can easily be changed, we'll take the kind from the search
            // result just in case.
            currentKind = kind;
        }

        var type;
        _(result.entities).forEach(function(entKey) {
            _(entKey).forIn(function(value, key) {
                type = key + "-entities";
                tmphtml += '<div align="left"><' + type + '><b>' + key + ':</b> ';
                _(value).forIn(function(rec, key2) {
                    // TODO: whould use default kind not hard code
                    // add class="ui-widget-content mz-ner" to <ent> if we want drag-n-drop entities
                    tmphtml += '<ent><a  href=\'' + buildSearchLink(key, rec.entity, currentKind) + '\'>' + rec.entity + '</a></ent> (' + rec.count + ')&nbsp;&#8226;&nbsp;';
                });
            });
            tmphtml += '</' + type + '></div>';
        });
    } // end if we have entities

    var html =  '<table class="result-table"><tr>';

    html += '<td class="preview" rowspan="3">' + previewImage + '</td>' +
            '<td class="name">' + nameLink + '&nbsp;(<a target="_blank" href="view.html?kind=ia-pages&action=view&id=' + docid + '&queryid=' + queryid + '">view OCR</a>)&nbsp;'

    // store the ratings with names but keep it hidden, we'll use this on hover to display the users and
    // their ratings on the left hand side of the screen.
    html += '<span id="' + result.name + '-user-ratings"></span><span  style="display:none" id="' + result.name + '-user-ratings-w-names"></span>';
    html += '<span class="highlight" id="' + result.name + '-dup-confidence"></span>';
    html += '</td></div></td><td class="score">&nbsp;&nbsp;&nbsp;rank: ' + result.rank + '</td></tr>';
    html += '<tr class="author"><td><span class="' + identifier + '-meta-author" >' + result.meta.creator ;
    html += '</span>&nbsp;•&nbsp;published: <span class="' + identifier + '-meta-published">' + result.meta.date + '</span></td></tr>';

    if (snippet) {
        html += '<tr><td class="snippet" colspan="3"> ...';
        html +=  snippet ;
        html += '... </td></tr>';

        // only get uniq word if we haven't seen it before - this will be used later
        // to find duplicate documents
        if (_.isUndefined(uniqWords[result.rank - 1])) {
            var once = _.uniq(snippet.split(" "))
            uniqWords.push((once))
        }

    } // end if snippet

    html += '</table>';
    html += tmphtml;

    // show notes
    var noteHTML = '';
    _.each(result.notes.rows, function(note) {
        noteHTML += '<div class="resource-notes" ><a target="_blank" href="../view.html?kind=ia-pages&action=view&id=' + note.uri + '&noteid=' + note.id + '"><b>';
        // remove any <br> tags, they make the note look odd in the results list
        noteHTML += note.user.split('@')[0] + ' : <i>' + note.text.replace(/<br>/g, " ") + '</i></b> : ';
        noteHTML += note.quote.replace(/<br>/g, " ") ;
        noteHTML += '</a></div>';
    })

    if (noteHTML.length > 0) {
        html += '<a href="#" onclick="UI.toggleNotes(\'' + result.name + '\');"><span id="notes-link-' + result.name + '"><span class="glyphicon glyphicon-collapse-down"></span>&nbsp;';
        if (UI.settings.show_notes == false){
            html += 'Show';
        } else {
            html += 'Hide';
        }
        html += ' notes&nbsp;</span><span class="fa fa-pencil"></span></a>';
        html += '<div id="notes-div-' + result.name + '"  ';
        if (UI.settings.show_notes == false){
            html += 'style="display:none"';
        }
        html += '>' + noteHTML + '</div>';
    }

    // show queries
    if (UI.settings.show_found_with_query){

        var queries = [];
        _.each(result.queries.rows, function(query) {
            queries.push('<a target="_BLANK" href="index.html?action=search&kind=' + query.kind + '&q=' + encodeURIComponent(query.query) + '">' + query.query + '</a>');
        })
        if (queries.length == 1) {
            html += '<div class="resource-query" >Found with query: ' + queries.toString() + '</div>';
        }
        if (queries.length > 1) {
            html += '<div class="resource-query" >Found with queries: ' + queries.join(', ').toString() + '</div>';
        }
    }

    // show labels
    // TODO change notes to labels or subcorpus
    html += '<div id="notes-' + result.name + '" class="resource-labels" >' + displayLabels(result.name) + '</div>';

    resDiv.html(html);



    return resDiv;

};

var renderNoteResult = function(queryTerms, result, resDiv) {

    // TODO duplicate code with renderResult()

    var name = "Note: ";
    // remove any <br> tags, they make the note look odd in the results list
    var snippet = result.text.replace(/<br>/g, " ");

    var idParts = result.name.split('_');

    var html =
            '<table class="note-table">' +
            '<tr>';

    html += '<td class="name">' + name;
    // store the ratings with names but keep it hidden, we'll use this on hover to display the users and
    // their ratings on the left hand side of the screen.
    html += '<span id="' + result.name + '-user-ratings"></span><span  style="display:none" id="' + result.name + '-user-ratings-w-names"></span>';

    html += '</td></div></td>';
    if (snippet) {
        html += '<td class="snippet">';
        html += '<div  ><a target="_blank" href="../view.html?kind=ia-pages&action=view&id=' + idParts[0] + '_' + idParts[1] + '&noteid=' + idParts[2] + '">';
        html +=  snippet ; // last param says not to strip out punctuation
        html += '</a></td>';
    }
    html += '<td class="score">&nbsp;&nbsp;&nbsp;rank: ' + result.rank + '</td>' + '</tr>';

    if (_.isUndefined(uniqWords[result.rank - 1])) {
        uniqWords.push(""); // put an empty entry, we don't want to compare notes to check for duplicates.
    }
    html += '</table>';
    html += '<div id="notes-' + result.name + '" class="resource-labels" >' + displayLabels(result.name) + '</div>';

    resDiv.html(html);

    return resDiv;

};


var doActionSearchPages = function(args) {
    var action = args.action;
    if (action == "search") {
        return doSearchRequest(args);
    }

    if (!action) {
        UI.showError("action not defined when calling doActionRequest in JS");
        return;
    }
    UI.showError("Unknown action `" + action + "'");
};

// This function is used if metadata is not returned with the results.
// Since we could have muliiple pages for the same book returned, we
// don't want to get metadata we already know, so we'll keep track of
// books we've already seen.

var bookMetadata = new Map();

function getInternetArchiveMetadata(bookid, args, callback){

    if (bookMetadata.has(bookid)){
        console.log("I've seen this book before!!!!!");
        args.metadata = bookMetadata.get(bookid);
        callback();
        return;
    }

    $.getJSON('http://archive.org/metadata/' + bookid + '/metadata')
            .done(function (json) {
                args.metadata = json.result;
                bookMetadata.set(bookid, args.metadata);
                callback();
            })
            .fail(function (jqxhr, textStatus, error ) {
                var err = textStatus + ", " + error;
                alert("Something went wrong getting the metadata from archive.org: " + err);
                console.log( "Request Failed: " + err );
            });

}

resultRenderers["ia-books"] = renderResult;
resultRenderers["ia-pages"] = renderResult;
resultRenderers["ia-all"] = renderResult;
// TODO : MCZ temp for now, focusing on just books/pages
resultRenderers["all"] = renderResult;

resultRenderers["ia-corpus"] = renderResult;
