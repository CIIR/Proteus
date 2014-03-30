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
    search(params);
  }
});

var search = function(args) {
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

  clearUI();
  showProgress("Search Request sent to server!");
  API.search(actualArgs, renderResults, function(req, status, err) {
    showError("ERROR: ``"+err+"``");
    throw err;
  });

  return actualArgs;
};

/* handlers for search button types */
setPageHandler(function() { search({kind:"pages", q:getQuery()}); });
setBookHandler(function() { search({kind:"books", q:getQuery()}); });


