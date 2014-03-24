/**
 * main.js
 *
 */

var server = "http://localhost:1234/";

var clearDivs = function() {
  $("#error").html('');
  $("#parsedQuery").html('');
  $("#request").html('');
  $("#results").html('');
};

var showProgress = function(str) {
  $("#progress").html(str);
};

var renderResults = function(data) {
  showProgress("Ajax response received!");
  var request = data.request;
  $("#request").html(JSON.stringify(request));
  $("#parsedQuery").html(data.parsedQuery);

  console.log(data.queryTerms);

  var resultsDiv = $("#results");
  _(data.results).forEach(function (result) {
    var snippet = "";
    if(request.snippets) {
      snippet = '<tr><td colspan="2">'+result.snippet+'</td></tr>';
    }
    resultsDiv.append('<div class="result">'+
                      '<table>'+
                      snippet+
                      '<tr>'+
                      '<td><b>'+result.name+'</b></td>'+
                      '<td><i>'+result.score.toFixed(3)+'</i></td>'+
                      '</tr>'+
                      '</table>'+
                      '</div>');
  });
};

var makeRequest = function(args) {
  var defaultArgs = {
    n: 10,
    snippets: false,
    kind: "books",
  };
  var actualArgs = _.merge(defaultArgs, args);

  if(!args.q || isBlank(args.q)) { 
    showProgress("Query is blank!");
    return;
  }

  var ajaxOpts = {
    type: "POST",
    url: server+"search",
    data: actualArgs,
    crossDomain: true,
  };
  
  clearDivs();
  showProgress("Search Request sent to server!");
  $.ajax(ajaxOpts)
    .done(renderResults)
    .error(function(req, status, err) {
      $("#error").html("ERROR: "+status);
    });

  return actualArgs;
};

var formQuery = function() { return $("#ui-search").val(); };

var goPages = function() {
  makeRequest({kind:"pages", snippets:true, q:formQuery()});
};

var goBooks = function() {
  makeRequest({kind:"books", q:formQuery()});
};

$("#ui-go-pages").click(goPages);
$("#ui-go-books").click(goBooks);


