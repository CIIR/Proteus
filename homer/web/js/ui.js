
var clearDivs = function() {
  $("#error").html('');
  $("#parsedQuery").html('');
  $("#request").html('');
  $("#results").html('');
};

var showProgress = function(str) {
  $("#progress").html(str);
};

var makeSnippet = function(response, doc, numCols) {
  if(response.request.snippets) {
    return '<tr><td class="snippet" colspan="'+numCols+'">'+doc.snippet+'</td></tr>';
  }
  return '';
};

var renderResults = function(data) {
  showProgress("Ajax response received!");
  var request = data.request;
  $("#request").html(JSON.stringify(request));
  $("#parsedQuery").html(data.parsedQuery);

  console.log(data);
  console.log(data.queryTerms);
  if(!_.isEmpty(data)) {
    console.log(_.first(data.results).meta);
  }

  var resultsDiv = $("#results");
  _(data.results).forEach(function (result) {
    var page = (request.kind === 'pages');
    var numCols = 3;
    var snippet = makeSnippet(data, result, numCols);
    var name = result.meta.title || result.name;
    var extURL = result.meta["identifier-access"];
    var previewImage = '';

    if(extURL) {
      name = '<a href="'+extURL +'">[ext]</a> ' + name;
    }
    if(page) {
      var identifier = result.name.split('_')[0];
      var pageNum = result.name.split('_')[1];
      name += ' pp. '+pageNum;
      previewImage = '<a href="'+pageImage(identifier, pageNum)+'">'+
        '<img src="'+pageThumbnail(identifier, pageNum)+'" />' + 
        '</a>';
    }

    resultsDiv.append('<div class="result">'+
                      '<table>'+
                      '<tr>'+
                      '<td>'+previewImage+'</td>' +
                      '<td><b>'+name+'</b></td>'+
                      '<td><i>'+result.score.toFixed(3)+'</i></td>'+
                      '</tr>'+
                      snippet+
                      '</table>'+
                      '</div>');
  });
};

var getQuery = function() { return $("#ui-search").val(); };
var setQuery = function(q) { $("#ui-search").val(q); };

var setReadyHandler = function(callback) {
  $(document).ready(callback);
};
var setPageHandler = function(callback) {
  $("#ui-go-pages").click(callback);
};
var setBookHandler = function(callback) {
  $("#ui-go-books").click(callback);
};

