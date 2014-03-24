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
    kind: "pages"
  };

  var actualArgs = _.merge(defaultArgs, args);

  if(!args.q || isBlank(args.q)) { 
    showProgress("Query is blank!");
    return;
  }

  var ajaxOpts = {
    type: "POST",
    url: "/api/search",
    data: actualArgs,
    crossDomain: true
  };
  
  clearUI();
  showProgress("Search Request sent to server!");
  $.ajax(ajaxOpts)
    .done(renderResults)
    .error(function(req, status, err) {
      showError("ERROR: "+status);
      throw err;
    });

  return actualArgs;
};

/* handlers for search button types */
setPageHandler(function() { makeRequest({kind:"pages", q:getQuery()}); });
setBookHandler(function() { makeRequest({kind:"books", q:getQuery()}); });


