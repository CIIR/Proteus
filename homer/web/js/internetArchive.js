/**
 * No other code should construct URLs or know about the internet archive in general,
 * so that we can generalize to the academic paper domain.
 *
 * URL format information for the Archive is at https://openlibrary.org/dev/docs/bookurls (as of March 2014)
 *
 */


var bookImage = function (archiveId) {
  return pageImage(archiveId, "0");
}

var bookThumbnail = function (archiveId) {
  return pageThumbnail(archiveId, "0");
}

var pageImage = function (archiveId, pageNum) {
  return "http://www.archive.org/download/" + encodeURIComponent(archiveId) + "/page/n" + pageNum + ".jpg";
};

var pageThumbnail = function (archiveId, pageNum) {
  return "http://www.archive.org/download/" + encodeURIComponent(archiveId) + "/page/n" + pageNum + "_thumb.jpg";
};

//console.log("Defining table, renderResult=" + renderResult);
var renderResult = function (queryTerms, result, resDiv, queryid) {

  var name = result.meta.title || result.meta.TEI || result.name;
  var identifier = result.name.split('_')[0];
  var snippet = result.snippet;
  var pageNum = result.name.split('_')[1];
  var iaURL = result.meta["identifier-access"];
  var nameLink = '';

  if (iaURL) {
    nameLink = Render.getDocumentURL(iaURL, name, queryTerms, result.rank);
  }
  var pgImage = iaURL;
  var kind = 'ia-books'; // default
  var thumbnail = '<img class="ia-thumbnail" src="' + pageThumbnail(identifier, pageNum) + '"/>';
  var previewImage = Render.getDocumentURL(pgImage, thumbnail, queryTerms, result.rank, true);

  if (!_.isUndefined(pageNum)) {
    kind = 'ia-pages';
    // if page result - make the link go to the page
    nameLink = Render.getDocumentURL('https://archive.org/stream/' + identifier + '#page/n' + pageNum + '/mode/2up', name, queryTerms, result.rank);

    // MCZ : removing page number for now as it does not match up with
    // the physical page number shown on the page
    //name += ' pp. ' + pageNum;
    pgImage = pageImage(identifier, pageNum);
    previewImage = Render.getPagePreviewURL(pgImage, thumbnail, queryTerms, result.rank);

    // check if we're a note. Can either check metadata "docType" = "note" or there are two underscores
    // TODO : this should be moved up
    if (!_.isUndefined(result.meta)) {
      if (result.meta.docType == "note") {
        return renderNoteResult(queryTerms, result, resDiv);
      }
    }
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
    _(result.entities).forEach(function (entKey) {
      _(entKey).forIn(function (value, key) {
        type = key + "-entities";
        tmphtml += '<div align="left"><' + type + '><b>' + key + ':</b> ';
        _(value).forIn(function (rec, key2) {
          // TODO: whould use default kind not hard code
          // add class="ui-widget-content mz-ner" to <ent> if we want drag-n-drop entities
          tmphtml += '<ent><a  href=\'' + buildSearchLink(key, rec.entity, currentKind) + '\'>' + rec.entity + '</a></ent> (' + rec.count + ')&nbsp;&#8226;&nbsp;';
        });
      });
      tmphtml += '</' + type + '></div>';
    });
  } // end if we have entities


  // var func = "recordSwipe('" + result.name + "', $('#" + result.name + "').data('metadata'),$('#" + result.name + "').data('kind'),"
  var html =
          '<table>' +
          '<tr>';
  //    }

  html += '<td class="preview" rowspan="3">' + previewImage + '</td>' +
  '<td class="name">' + nameLink + '&nbsp;(<a target="_blank" href="view.html?kind=' + kind + '&action=view&id=' + result.name + '&queryid=' + queryid + '">view OCR</a>)&nbsp;'

  // store the ratings with names but keep it hidden, we'll use this on hover to display the users and
  // their ratings on the left hand side of the screen.
  html += '<span id="' + result.name + '-user-ratings"></span><span  style="display:none" id="' + result.name + '-user-ratings-w-names"></span>';

  html += '<span class="highlight" id="' + result.name + '-dup-confidence"></span>';

  html += '</td></div></td>' +
  '<td class="score">&nbsp;&nbsp;&nbsp;rank: ' + result.rank + '</td>'; // + '&nbsp;&nbsp;&nbsp;score: ' + Math.exp(result.score) + '</td> ' +
  '</tr>';

  html += '<tr><td>' + result.meta.creator + '&nbsp;•&nbsp;published: ' + result.meta.date + '</td></tr>';

  if (snippet) {
    html += '<tr><td class="snippet" colspan="3"> ...';
    html += highlightText(queryTerms, snippet, '<span class="hili">', '</span>');
    html += '... </td></tr>';

    // only get uniq word if we haven't seen it before - this will be used later
    // to find duplicate documents
    if (_.isUndefined(uniqWords[result.rank - 1])) {
      var once = _.uniq(snippet.split(" "))
      uniqWords.push((once))
    }

  } // end if snippet

  //    html += '<tr><td>Search pages in this book...</td></tr>';

  html += '</table>';
  html += tmphtml;
  //    if (result.tags) {
  //        html += UI.renderTags(result);
  //    }

  // show notes
  var noteHTML = '';
  _.each(result.notes.rows, function (note) {
    noteHTML += '<div class="resource-notes" ><a target="_blank" href="../view.html?kind=ia-pages&action=view&id=' + note.uri + '&noteid=' + note.id + '">';
    noteHTML += note.user.split('@')[0] + ' : ' + note.text + ' : ';
    noteHTML += highlightText(queryTerms, note.quote, '<span class="hili">', '</span>');
    noteHTML += '</a></div>';
  })

  if (noteHTML.length > 0) {
    html += '<a href="#" onclick="UI.toggleNotes(\'tmpnotes-' + result.name + '\');"><span class="fa fa-pencil"></span>&nbsp;Show/Hide notes</a></div>';
    html += '<div id="tmpnotes-' + result.name + '"  style="display:none">' + noteHTML + '</div>';
  }

  // show queries
  var queries = [];
  _.each(result.queries.rows, function (query) {
    queries.push('<a target="_BLANK" href="index.html?action=search&kind=' + query.kind + '&q=' + encodeURI(query.query ) + '">' + query.query  + '</a>');
  })
  if (queries.length == 1) {
    html += '<div class="resource-query" >Found with query: ' + queries.toString() + '</div>';
  }
  if (queries.length > 1) {
    html += '<div class="resource-query" >Found with queries: ' + queries.join(', ').toString() + '</div>';
  }

  // show labels
  // TODO change notes to labels or subcorpus
  html += '<div id="notes-' + result.name + '" class="resource-labels" >' + displayLabels(result.name) + '</div>';


  resDiv.html(html);

  return resDiv;

};

var renderNoteResult = function (queryTerms, result, resDiv) {

  // TODO duplicate code with renderResult()

  var name = "Note: ";
  var snippet = result.text;
  var idParts = result.name.split('_');

  var html =
          '<table>' +
          '<tr>';

  html += '<td class="name">' + name;
  // store the ratings with names but keep it hidden, we'll use this on hover to display the users and
  // their ratings on the left hand side of the screen.
  html += '<span id="' + result.name + '-user-ratings"></span><span  style="display:none" id="' + result.name + '-user-ratings-w-names"></span>';

  html += '</td></div></td>' +
  '<td class="score">&nbsp;&nbsp;&nbsp;rank: ' + result.rank + '</td>' +
  '</tr>';

  if (snippet) {
    html += '<tr><td class="snippet" colspan="3">';
    html += '<div  ><a target="_blank" href="../view.html?kind=ia-pages&action=view&id=' + idParts[0] + '_' + idParts[1] + '&noteid=' + idParts[2] + '">';
    html += highlightText(queryTerms, snippet, '<span class="hili">', '</span>', false); // last param says not to strip out punctuation
    html += '</a></td></tr>';
  }
  if (_.isUndefined(uniqWords[result.rank - 1])) {
    uniqWords.push(""); // put an empty entry, we don't want to compare notes to check for duplicates.
  }
  html += '</table>';
  html += '<div id="notes-' + result.name + '" class="resource-labels" >' + displayLabels(result.name) + '</div>';

  resDiv.html(html);

  return resDiv;

};


var doActionSearchPages = function (args) {
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

// page specific function. If we used the doSearchRequest() we'd mess up the "get more results"
// button (or auto retrieve)
// ???? we would need another onSearchSuccess too
// TODO - although this resuls in TONS of dupllicate code
//var doSearchPageRequest = function (args) {
//
//    // TODO - big hack, if we have a workingSetQuery we know we're searching for pages
//    // belonging to a book so we want the results to go under that book
//
//        resultsDiv = $("#page-results-" + args.workingSetQuery.split('archiveid:')[1]);
//
//
//    //disableAutoRetrieve(); // prevent double requests
//
//    var tmpSettings = getCookie("settings");
//    var settings;
//    var numEntities = 5; // default
//    if (tmpSettings != "") { // first time we don't have cookies
//        settings = JSON.parse(tmpSettings);
//
//        if (!_.isUndefined(settings.num_entities)) {
//            numEntities = settings.num_entities;
//        }
//    }
//    var defaultArgs = {
//        n: 10,
//        skip: 0,
//        snippets: true,
//        metadata: true,
//        top_k_entities: parseInt(numEntities)
//    };
//
//    // we could have args passed in esp if they're reusing an URL
//    if (_.isUndefined(args.labels)) {
//        var labelList = getSelectedLabels();
//
//        if (!_.isEmpty(labelList)) {
//            var labelArgs = '{ "labels":  ' + JSON.stringify(labelList) + '}';
//            args = _.merge(args, JSON.parse(labelArgs));
//        }
//    } else {
//        // format them correctly
//        var labelArgs = '{ "labels":  ' + JSON.stringify(args.labels.split(",")) + '}';
//        args.labels = "";
//        args = _.merge(args, JSON.parse(labelArgs));
//    }
//
//    // if we didn't ask for more
//    if (!args.skip || args.skip === 0) {
//  //      Model.clearResults();
//        UI.clearResults();
//        updateURL(args); // modify URL if possible
//    }
//
//    var userName = getCookie("username");
//
//    if (userName != "") {
//        var userToken = getCookie("token");
//        var userID = getCookie("userid");
//        var corpus = getCookie("corpus");
//        var corpusID = -1;
//        if (corpus.length == 0) {
//            alert("Please select a corpus!");
//            return;
//        } else {
//            corpusID = getCorpusID(corpus);
//        }
//        console.log("corpus: " + corpus + " id: " + corpusID);
//        var tagArgs = {
//            tags: true,
//            user: userName,
//            userid: userID,
//            token: userToken,
//            corpus: parseInt(corpusID),
//            corpusName: corpus
//        };
//        args = _.merge(args, tagArgs);
//    }
//    var actualArgs = _.merge(defaultArgs, args);
//
////    if (args.kind.endsWith("corpus")) {
////        actualArgs.action = "search-corpus";
////    }
//
//    // only allow blank queries if we're searching a corpus or by label(s)
//    if ((!actualArgs.q || isBlank(actualArgs.q)) && (_.isEmpty(actualArgs.labels)) && ( actualArgs.action != "search-corpus")) {
//        UI.showProgress("Query is blank!");
//        return;
//    }
//    //$("#more").html('<img src="/images/more-loader.gif"\>');
//
////    Model.request = actualArgs;
////    console.log(Model.request);
//
//    UI.showProgress("Search Request sent to server!");
//    API.action(actualArgs, onSearchSuccess, function (req, status, err) {
//        UI.showError("ERROR: ``" + err + "``");
//        // set up the auto retrieve again
//     //   enableAutoRetrieve();
//        throw err;
//    });
//
//    return actualArgs;
//};


resultRenderers["ia-books"] = renderResult;
resultRenderers["ia-pages"] = renderResult;
resultRenderers["ia-all"] = renderResult;

resultRenderers["ia-corpus"] = renderResult;


