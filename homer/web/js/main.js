/**
 * main.js
 *
 * Application-specific logic, contains the server URL.
 *
 * For now, includes talking to the proteus/homer server.
 *
 */

// the JSON of the application state
var Model = {
  // search result data
  request: {},
  query: "",
  results: [],
  // user login data
  user: null,
  token: null
};

Model.clearResults = function() {
  Model.results = [];
  Model.query = "";
};

/**
 * document.ready handler onload
 *
 * reads the ?q=foo parameters and sends of a JSON API request
 */
UI.setReadyHandler(function() {
  var params = getURLParams();
  console.log(params);

  UI.setUserName(getCookie("username"));

  if (!isBlank(params.q)) {
    UI.setQuery(params.q);
    search(params);
  }
});

/**
 * This search functions to get initial and more results from the server through the API.search call. It hands the results it receives on success to UI.appendResults
 * @see UI.appendResults
 * @param args
 * @returns {Object|*}
 */
var search = function(args) {
  var defaultArgs = {
    n: 10,
    skip: 0,
    snippets: true,
    metadata: true
  };

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
  API.search(actualArgs, onSuccess, function(req, status, err) {
    UI.showError("ERROR: ``" + err + "``");
    throw err;
  });

  return actualArgs;
};

/* handlers for search button types */
UI.onClickSearchButton = function(buttonDesc) {
  var kind = buttonDesc.kind;
  search({kind: kind, q: UI.getQuery()});
};

/* pull the previous request out of the "Model" and send it to the server, but request the next 10 */
UI.setMoreHandler(function() {
  var prev = Model.request;
  prev.skip = Model.results.length;
  prev.n = 10;
  search(prev);
});

var logIn = function(userName) {
  if (!userName)
    return;

  var args = {user: userName};

  // MZ: first we'll try to register them then log them in. It's OK if they're
  // already registered. Eventually we'll want this to be a 2 step process
  // but for now we just want something running. FOR NOW, we'll assume an error
  // means they're already registered (duplicate key error).
  var loginFunc = function() {
    API.login(args, function(data) {
      document.cookie = "username=" + userName + ";";
      document.cookie = "token=" + data.token + ";";
      Model.user = userName;
      Model.token = data.token;
    }, function(req, status, err) {
      UI.showError("ERROR: ``" + err + "``");
      throw err;
    })
  };

  API.register(args, loginFunc, loginFunc);
};

var logOut = function() {
  var userName = getCookie("username");
  var userToken = getCookie("token");

  var args = {user: userName, token: userToken};
  API.logout(args, function() {
    document.cookie = "username=;";
    document.cookie = "token=;";
    Model.user = null;
    Model.token = null;
  }, function(req, status, err) {
    UI.showError("ERROR: ``" + err + "``");
    throw err;
  });

  UI.setUserName("");

};

