/**
 * ui.js
 *
 * This file roughly contains all the interfacing with the index.html boilerplate, using jquery as needed.
 *
 */

var clearDivs = function() {
  $("#error").html('');
  $("#parsedQuery").html('');
  $("#request").html('');
  $("#results").html('');
};

var showProgress = function(str) {
  $("#progress").html(str);
};

/**
 * Render a snippet.
 *
 * TODO: highlight the query terms!
 */
var makeSnippet = function(response, doc, numCols) {
  var queryTerms = response.queryTerms;
  if(response.request.snippets) {
    return '<tr><td class="snippet" colspan="'+numCols+'">'+doc.snippet+'</td></tr>';
  }
  return '';
};

/**
 * Render a single search result.
 *
 * TODO: move this to mustache.js or another templating engine.
 */
var makeResult = function(data, result) {
  var page = (data.request.kind === 'pages');
  var numCols = 3;
  var snippet = makeSnippet(data, result, numCols);
  var name = result.meta.title || result.name;
  var extURL = result.meta["identifier-access"];
  var previewImage = '';

  if(extURL) {
    name = '<a href="'+extURL +'">'+name+'</a>';
  }
  if(page) {
    var identifier = result.name.split('_')[0];
    var pageNum = result.name.split('_')[1];
    name += ' pp. '+pageNum;
    previewImage = '<a href="'+pageImage(identifier, pageNum)+'">'+
      '<img src="'+pageThumbnail(identifier, pageNum)+'" />' + 
      '</a>';
  }

  return '<div class="result">'+
    '<table>'+
    '<tr>'+
    '<td>'+previewImage+'</td>' +
    '<td><b>'+name+'</b></td>'+
    '<td><i>'+result.score.toFixed(3)+'</i></td>'+
    '</tr>'+
    snippet+
    '</table>'+
    '</div>';
};

/**
 * Async callback that receives /search JSON data and renders it.
 */
var renderResults = function(data) {
  showProgress("Ajax response received!");
  $("#request").html(JSON.stringify(data.request));
  $("#parsedQuery").html(data.parsedQuery);

  var resultsDiv = $("#results");
  _(data.results).forEach(function (result) {
    resultsDiv.append(makeResult(data, result));
  });
};

var getQuery = function() { return $("#ui-search").val(); };
var setQuery = function(q) { $("#ui-search").val(q); };

/**
 * A set of functions for reacting to events in other, more general code.
 */
var setReadyHandler = function(callback) {
  $(document).ready(callback);
};
var setPageHandler = function(callback) {
  $("#ui-go-pages").click(callback);
};
var setBookHandler = function(callback) {
  $("#ui-go-books").click(callback);
};

