/**
 * main.js
 *
 * Application-specific logic, contains the server URL.
 *
 * For now, includes talking to the proteus/homer server.
 *
 */
var gUniqType = [];
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

var privateURLParams = _(["user", "token"]);

var updateURL = function(request) {
  pushURLParams(_.omit(request, privateURLParams));
};

/**
 * document.ready handler onload
 *
 * reads the ?q=foo parameters and sends of a JSON API request
 */
UI.setReadyHandler(function() {
  var params = _.omit(getURLParams(), privateURLParams);
  console.log(params);

  UI.setUserName(getCookie("username"));

  if (params.action == "search" && !isBlank(params.q)) {
    UI.setQuery(params.q);
    doActionRequest(params);
  } else if (params.action == "view") {
    doActionRequest(params);
  }
});

/**
 * This main "action-request" delegates to other things. Notice how search requests disappear into actions.js early.
 */
var doActionRequest = function(args) {
  var action = args.action;
  if (action == "search") {
    return doSearchRequest(args);
  }
  if (action == "view") {
    return doViewRequest(args);
  }
  if (!action) {
    UI.showError("action not defined when calling doActionRequest in JS");
    return;
  }
  UI.showError("Unknown action `" + action + "'");
};

/* handlers for search button types */
UI.onClickSearchButton = function(buttonDesc) {
  var kind = buttonDesc.kind;
  doActionRequest({kind: kind, q: UI.getQuery(), action: "search"});
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
      //  getAllTagsByUser();
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


// function to get all the tags for all users.
var getAllTagsByUser = function() {
  var userName = getCookie("username");
  var userToken = getCookie("token");
  var uniqType = [];
// really want tags per user by resource OR "project"
  var args = {resource: ["%"], user: userName, token: userToken};
  API.getAllTagsByUser(args, function(origresult) {
    // $("#my-tags").html("poop");
    //  DUPLIATE CODES
    var keys = Object.keys(origresult);
// UGLY - assuming only one resuorce
    // for (var key in result) {
    //alert(JSON.stringify(result[keys[0]]));
    result = origresult[keys[0]]
    // }
    // alert(JSON.stringify(result ));
    html = "";
    if (userName !== "") {

      html += '<span><b>My Tags:</b>&nbsp;';

      if (typeof result[userName] !== 'undefined') {
//        tags = result[userName].toString().split(',');
//        for (tag in tags) {
//          html += tags[tag] + ', ';
//        }
        html += result[userName].toString();
      }
      html += '</span>'
      $("#my-tags").html(html);
    } // end if someone is logged in
    html = "";
    for (user in result) {
      //??? not the most effiecent code in the world
      tags = result[user].toString().split(',');
      for (tag in tags) {
        //alert(tag);
        uniqType.push(tags[tag].split(":")[0]);
      }

      if (user != userName)
        html += "<b>" + user + ":</b>&nbsp;" + result[user].toString() + "&nbsp;";

    }
    $("#other-tags").html(html);

    gUniqType = _.uniq(uniqType);
    // alert(newUniqType.toString());


  }, function(req, status, err) {
    UI.showError("ERROR: ``" + err + "``");
    throw err;
  });
};


var userTagsJSON = "";

var setAllTagsByUser = function(setGlobalFunc) {
  var userName = getCookie("username");
  var userToken = getCookie("token");

// really want tags per user by resource OR "project"
  var args = {resource: ["%"], user: userName, token: userToken};

  API.getAllTagsByUser(args, function(origresult) {

    // there is only one key cuz we only passed in one resource
    var keys = Object.keys(origresult);
    setGlobalFunc(origresult[keys[0]]);

  }, function(req, status, err) {
    UI.showError("ERROR: ``" + err + "``");
    throw err;
  });

};


// ???? tmp global list of all users/tags



setAllTagsByUser(function(result) {
  userTagsJSON = result;
  $("#tmp-tags").html(JSON.stringify(userTagsJSON));

});


