
var pageImage = function(pageid, meta) {
    if ( meta.page_image ) {
	return meta.page_image;
    } else if ( meta['identifier-access'] != undefined ) {
	var id = parsePageID(pageid);
	return "http://www.archive.org/download/" + encodeURIComponent(id.id) + "/page/n" + id.page + ".jpg";
    } else {
	return 'images/thumb.png';
    }
};

var pageThumbnail = function(pageid, meta) {
    if ( meta.page_thumb ) {
	return meta.page_thumb;
    } else if ( meta['identifier-access'] != undefined ) {
	var id = parsePageID(pageid);
	return "http://www.archive.org/download/" + encodeURIComponent(id.id) + "/page/n" + id.page + "_thumb.jpg";
    } else {
	return 'images/thumb.png';
    }
};

var renderResult = function(queryTerms, result, resDiv, queryid) {

  // check if we're a note. Can either check metadata "docType" = "note"
  if (!_.isUndefined(result.meta) && result.meta.docType == "note") {
    return renderNoteResult(queryTerms, result, resDiv);
  }

  var name = result.meta.title || result.meta.TEI || result.name;
  var tmpid = parsePageID(result.name);
  var identifier = tmpid.id;
  var pageNum = tmpid.page;
  var docid = result.name;
  var snippet = result.snippet;
  var kind = 'ia-books'; // default

    var nameURL = result.meta.page_access;
    if ( result.meta['identifier-access'] != undefined ) {
	nameURL = archiveViewerURL(docid);
    }

  if (!_.isUndefined(pageNum) && pageNum.length > 0) {
    kind = 'ia-pages';
  }
  if (kind == 'ia-books' && !_.isUndefined(result.snippetPage)) {
    docid = identifier + "_" + result.snippetPage;
  }
  var thumbnail = '<img class="ia-thumbnail" src="' + pageThumbnail(docid, result.meta) + '"/>';
  var previewImage = '<a class="fancybox" href="' + pageImage(docid, result.meta) + '" >' +  thumbnail + '</a>';
  var nameLink = '';
    if ( nameURL  ) {
	nameLink += '<a href="' + nameURL + '" onmousedown="return rwt(this,' +  result.rank;
	nameLink += ')" target="_blank"><span class="' + identifier + '-meta-name">';
    }
    nameLink += name;
    if ( nameURL ) {
	nameLink += '</span>&nbsp;<span class="fa fa-external-link"></span></a>';
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
      // we should be guaranteed to have a kind on the URL, but since
      // they can easily be changed, we'll take the kind from the search
      // result just in case.
      currentKind = kind;
    }

    var type;
    _.forEach(result.entities, function(entKey) {
      _.forIn(entKey, function(value, key) {
        type = key + "-entities";
        tmphtml += '<div align="left"><' + type + '><b>' + key + ':</b> ';
        _.forEach(value, function(obj) {
          // TODO:  should use default kind not hard code
          // add class="ui-widget-content mz-ner" to <ent> if we want drag-n-drop entities
          tmphtml += '<ent><a href=\'' + buildSearchLink(key, obj.term, currentKind) + '\'>' + obj.term + '</a></ent> (' + obj.count + ')&nbsp;&#8226;&nbsp;';
        });
      });
      tmphtml += '</' + type + '></div>';
    });
  } // end if we have entities

  var html = '<table class="result-table"><tr>';

  html += '<td class="result-img-preview" rowspan="3">' + previewImage + '</td>' +
  '<td class="name">' + nameLink + '&nbsp;(<a target="_blank" href="view.html?kind=ia-pages&action=view&id=' + docid + '&queryid=' + queryid + '">view OCR</a>)&nbsp;';

  // store the ratings with names but keep it hidden, we'll use this on hover to display the users and
  // their ratings on the left hand side of the screen.
  html += '<span id="' + result.name + '-user-ratings"></span><span  style="display:none" id="' + result.name + '-user-ratings-w-names"></span>';
  html += '<span class="dup-highlight" id="' + result.name + '-dup-confidence"></span>';
  html += '</td></div></td><td class="score">&nbsp;&nbsp;&nbsp;rank: ' + result.rank +'</td></tr>';
  html += '<tr class="author"><td><span class="' + identifier + '-meta-author" >' + result.meta.creator;
  html += '</span>&nbsp;•&nbsp;published: <span class="' + identifier + '-meta-published">' + result.meta.date + '</span></td></tr>';

  if (snippet) {
    snippet = snippet.replace(/<br>/g, " ");
    html += '<tr><td class="snippet" colspan="3"> ...' + snippet + '... </td></tr>';

    // only get unique words if we haven't seen it before - this will be used later
    // to find duplicate documents
    if (_.isUndefined(uniqWords[result.rank - 1])) {
      var once = _.uniq(snippet.split(" "));
      uniqWords.push(once);
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
    noteHTML += note.quote.replace(/<br>/g, " ");
    noteHTML += '</a></div>';
  });

  if (noteHTML.length > 0) {
    html += '<a href="#" onclick="UI.toggleNotes(\'' + result.name + '\');"><span id="notes-link-' + result.name + '"><span class="glyphicon glyphicon-collapse-down"></span>&nbsp;';
    if (UI.settings.show_notes == false) {
      html += 'Show';
    } else {
      html += 'Hide';
    }
    html += ' notes&nbsp;</span><span class="fa fa-pencil"></span></a>';
    html += '<div id="notes-div-' + result.name + '"  ';
    if (UI.settings.show_notes == false) {
      html += 'style="display:none"';
    }
    html += '>' + noteHTML + '</div>';
  }

  // show queries
  if (UI.settings.show_found_with_query && !_.isUndefined(result.queries)) {

    var queries = [];
    _.each(result.queries.rows, function(query) {
      queries.push('<a target="_blank" href="index.html?action=search&kind=' + query.kind + '&q=' + encodeURIComponent(query.query) + '">' + query.query + '</a>');
    });
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

  var html = '<table class="note-table"><tr><td class="name">' + name;

  // store the ratings with names but keep it hidden, we'll use this on hover to display the users and
  // their ratings on the left hand side of the screen.
  html += '<span id="' + result.name + '-user-ratings"></span><span  style="display:none" id="' + result.name + '-user-ratings-w-names"></span>';

  html += '</td></div></td>';
  if (snippet) {
    html += '<td class="snippet">';
    html += '<div  ><a target="_blank" href="../view.html?kind=ia-pages&action=view&id=' + idParts[0] + '_' + idParts[1] + '&noteid=' + idParts[2] + '">';
    html += snippet; // last param says not to strip out punctuation
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

resultRenderers["ia-books"] = renderResult;
resultRenderers["ia-pages"] = renderResult;
resultRenderers["ia-all"] = renderResult;
// TODO : MCZ temp for now, focusing on just books/pages
resultRenderers["all"] = renderResult;
