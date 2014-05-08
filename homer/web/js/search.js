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

  // we inherit args from the URL - which could contain
  // the user name, so we want to strip out any data we're
  // filling in here.
  delete args.tags;
  delete args.user;
  delete args.token;

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

  // if we didn't ask for more
  if (actualArgs.skip === 0) {
    Model.clearResults();
    UI.clearResults();
    pushURLParams(args); // modify URL if possible
  }

  Model.request = actualArgs;
  console.log(request);

  var onSuccess = function(data) {
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

  UI.showProgress("Search Request sent to server!");
  API.action(actualArgs, onSuccess, function(req, status, err) {
    UI.showError("ERROR: ``" + err + "``");
    throw err;
  });

  return actualArgs;
};

