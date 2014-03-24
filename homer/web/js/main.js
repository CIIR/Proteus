/**
 * main.js
 *
 * Application-specific logic, contains the server URL.
 *
 * For now, includes talking to the proteus/homer server.
 *
 * TODO: use require.js and add a build system.
 * 
 */

var server = "http://localhost:1234/";

/**
 * document.ready handler onload
 *
 * reads the ?q=foo parameters and sends of a JSON API request
 */
setReadyHandler(function() {
  var params = getURLParams();
  console.log(params);
  if(!isBlank(params.q)) {
    setQuery(params.q);
    makeRequest(params);
  }
});

/**
 * Main async server communication call, only does /search for now
 */
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

/* handlers for search button types */
setPageHandler(function() { makeRequest({kind:"pages", q:getQuery()}); });
setBookHandler(function() { makeRequest({kind:"books", snippets:false, q:getQuery()}); });


