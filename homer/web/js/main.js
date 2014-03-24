/**
 * main.js
 *
 */

var server = "http://localhost:1234/";

setReadyHandler(function() {
  var params = getURLParams();
  console.log(params);
  if(!isBlank(params.q)) {
    setQuery(params.q);
    makeRequest(params);
  }
});

var makeRequest = function(args) {
  var defaultArgs = {
    n: 10,
    snippets: true,
    metadata: true,
    kind: "pages",
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

var goPages = function() {
  makeRequest({kind:"pages", q:getQuery()});
};
setPageHandler(goPages);

var goBooks = function() {
  makeRequest({kind:"books", snippets:false, q:getQuery()});
};
setBookHandler(goBooks);


