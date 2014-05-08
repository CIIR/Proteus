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
 * This main "action-request" delegates to other things. Notice how search requests disappear into search.js early.
 */
var doActionRequest = function(args) {
  var action = args.action;
  if(action == "search") {
    doSearchRequest(args);
    return;
  }
  if(!action) {
    UI.showError("action not defined when calling doActionRequest in JS");
    return;
  }

  console.log(request);
  var onSuccess = function(data) {
    UI.clearError();
    var action = data.request.action;
    console.log(data);

    if(action === "view") {
      console.log(data);
      UI.showError("TODO: handle 'view' action.")
    } else {
      console.log(data);
      UI.showError("Error: UI doesn't know how to handle '"+action+"' action.")
    }
  };

  UI.showProgress("Request sent to server!");
  API.action(args, onSuccess, function(req, status, err) {
    UI.showError("ERROR: ``" + err + "``");
    throw err;
  });

  return args;
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

