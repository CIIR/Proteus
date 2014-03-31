/**
 * ui.js
 *
 * This file roughly contains all the interfacing with the index.html boilerplate, using jquery as needed.
 *
 */

var clearUI = function() {
  $("#error").html('');
  $("#parsedQuery").html('');
  $("#request").html('');
  $("#results").html('');
  clearError();
};

var showProgress = function(str) {
  $("#progress").html(str);
};

var showError = function(str) {
  $("#error").html(str);
  $("#error").show();
};

var clearError = function() {
  $("#error").hide();
};

/**
 * Render a snippet.
 *
 * TODO: highlight the query terms!
 */
var makeSnippet = function(response, doc, numCols) {
  if(!response.request.snippets) {
    return '<tr><td class="snippet" colspan="'+numCols+'"></td></tr>';
  }
  var queryTerms = response.queryTerms;
  var words = doc.snippet.split(/\s/);
  var hiliSnippet = _(words).map(function(word) {
    if(_.contains(queryTerms, word)) {
      return '<span class="hili">'+word+'</span>';
    }
    return word;
  }).reduce(function (lhs, rhs) {
    return lhs + ' ' + rhs;
  }, ' ');

  return '<tr><td class="snippet" colspan="'+numCols+'">'+hiliSnippet+'</td></tr>';
};

/**
 * Render a single search result.
 *
 * TODO: move this to mustache.js or another templating engine.
 */
var makeResult = function(data, result) {
  var page = (data.request.kind === 'pages');
  var numCols = 2;
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
      '<img class="thumbnail" src="'+pageThumbnail(identifier, pageNum)+'" />' + 
      '</a>';
  }

  return '<div class="result">'+
    '<table>'+
    '<tr>'+
    '<td class="preview" rowspan="2">'+previewImage+'</td>' +
    '<td class="name">'+name+'</td>'+
    '<td class="score">'+result.score.toFixed(3)+'</td>'+
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

