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

  if (params.action=="search" && !isBlank(params.q)) {
    UI.setQuery(params.q);
    doActionRequest(params);
  } else if(params.action == "view") {
    doActionRequest(params);
  }
});

/**
 * This search functions to get initial and more results from the server through the API.search call. It hands the results it receives on success to UI.appendResults
 * @see UI.appendResults
 * @param args
 * @returns {Object|*}
 */
var doActionRequest = function(args) {
  var action = args.action;
  if(!action) {
    UI.showError("action not defined when calling doActionRequest in JS");
    return;
  }
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
    }
    args = _.merge(args, tagArgs);
  }

  var actualArgs = _.merge(defaultArgs, args);

  if(action == "search") {
    if (!actualArgs.q || isBlank(actualArgs.q)) {
      UI.showProgress("Query is blank!");
      return;
    }
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
    var action = data.request.action;
    console.log(data);

    if(action === "search") {
      Model.query = data.request.q;
      var rank = Model.results.length + 1;
      var newResults = _(data.results).map(function(result) {
        result.kind = data.request.kind;
        result.rank = rank++;

        return result;
      }).value();

      Model.results = _(Model.results).concat(data.results).value();
      UI.appendResults(data.queryTerms, newResults);
    } else if(action === "view") {
      console.log(data);
      UI.showError("TODO: handle 'view' action.")
    } else {
      console.log(data);
      UI.showError("Error: UI doesn't know how to handle '"+action+"' action.")
    }
  };

  UI.showProgress("Search Request sent to server!");
  API.action(actualArgs, onSuccess, function(req, status, err) {
    UI.showError("ERROR: ``" + err + "``");
    throw err;
  });

  return actualArgs;
};

/* handlers for search button types */
UI.onClickSearchButton = function(buttonDesc) {
  var kind = buttonDesc.kind;
  doActionRequest({kind: kind, q: UI.getQuery(), action:"search"});
};

/* pull the previous request out of the "Model" and send it to the server, but request the next 10 */
UI.setMoreHandler(function() {
  var prev = Model.request;
  prev.skip = Model.results.length;
  prev.n = 10;
  doActionRequest(prev);
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

var addTag = function(tagText, resourceID) {
  var userName = getCookie("username");
  var userToken = getCookie("token");

  var tmp = '{ "user": "' + userName + '", "token" :"' + userToken + '", "tags": {"' + tagText + '": ["' + resourceID + '"]}}';
  var args = JSON.parse(tmp);
  API.createTags(args, null, function(req, status, err) {
    UI.showError("ERROR: ``" + err + "``");
    throw err;
  });

};


var deleteTag = function(tagText, resourceID) {
  var userName = getCookie("username");
  var userToken = getCookie("token");

  var tmp = '{ "user": "' + userName + '", "token" :"' + userToken + '", "tags": {"' + tagText + '": ["' + resourceID + '"]}}';
  var args = JSON.parse(tmp);
  API.deleteTags(args, null, function(req, status, err) {
    UI.showError("ERROR: ``" + err + "``");
    throw err;
  });

};

