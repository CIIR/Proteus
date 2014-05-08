// This file handles the flow of a search request
// it interacts a lot with Model which is defined in main.js
//

var doSearchRequest = function(args) {
  var defaultArgs = {
    n: 10,
    skip: 0,
    snippets: true,
    metadata: true
  };

  // if we didn't ask for more
  if (!args.skip || args.skip === 0) {
    Model.clearResults();
    UI.clearResults();
    updateURL(args); // modify URL if possible
  }

  var userName = getCookie("username");

  if (userName != "") {
    var userToken = getCookie("token");
    var tagArgs = {
      tags: true,
      user: userName,
      token: userToken
    };
    args = _.merge(args, tagArgs);
  }

  var actualArgs = _.merge(defaultArgs, args);

  if (!actualArgs.q || isBlank(actualArgs.q)) {
    UI.showProgress("Query is blank!");
    return;
  }

  Model.request = actualArgs;
  console.log(request);

  UI.showProgress("Search Request sent to server!");
  API.action(actualArgs, onSearchSuccess, function(req, status, err) {
    UI.showError("ERROR: ``" + err + "``");
    throw err;
  });

  return actualArgs;
};

/**
 * This gets called with the response from JSONSearch
 */
var onSearchSuccess = function(data) {
  UI.clearError();

  Model.query = data.request.q;
  var rank = Model.results.length + 1;
  var newResults = _(data.results).map(function(result) {
    result.kind = data.request.kind;
    result.rank = rank++;

    return result;
  }).value();

  Model.results = _(Model.results).concat(data.results).value();
  UI.appendResults(data.queryTerms, newResults);
};

var doViewRequest = function(args) {
  UI.showProgress("View request sent to server!");
  API.action(args, onViewSuccess, function(req, status, err) {
    UI.showError("ERROR: ``" + err + "``");
    throw err;
  });
};

/** this gets called with the response from ViewResource */
var onViewSuccess = function(args) {
  UI.clearError();
  moreButton.hide();
  resultsDiv.hide();
  var html = '<div>' + _.escape(args.text) + '</div>';
  viewResourceDiv.html(html);
  viewResourceDiv.show();
};

