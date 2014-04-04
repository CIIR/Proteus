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

  setUserName(getCookie("username"));

  if (!isBlank(params.q)) {
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

  if (!args.q || isBlank(args.q)) {
    showProgress("Query is blank!");
    return;
  }

  clearUI();
  showProgress("Search Request sent to server!");
  API.search(actualArgs, renderResults, function(req, status, err) {
    showError("ERROR: ``" + err + "``");
    throw err;
  });

  return actualArgs;
};

/* handlers for search button types */
setPageHandler(function() {
  search({kind: "pages", q: getQuery()});
});
setBookHandler(function() {
  search({kind: "books", q: getQuery()});
});

function logIn(userName)
{

  if (!userName)
    return;

  var args = {user: userName};

  // MZ: first we'll try to register them then log them in. It's OK if they're
  // already registered. Eventually we'll want this to be a 2 step process
  // but for now we just want something running. FOR NOW, we'll assume an error
  // means they're already registred (duplicate key error).
  var loginFunc = function() {
    API.login(args, function(data) {
      document.cookie = "username=" + userName + ";";
      document.cookie = "token=" + data.token + ";";
    }, function(req, status, err) {
      showError("ERROR: ``" + err + "``");
      throw err;
    })
  };

  API.register(args, loginFunc, loginFunc);

}

function logOut()
{
  var userName = getCookie("username");
  var userToken = getCookie("token");

  var args = {user: userName, token: userToken};
  API.logout(args, function() {
    document.cookie = "username=;";
    document.cookie = "token=;";
  }, function(req, status, err) {
    showError("ERROR: ``" + err + "``");
    throw err;
  });

  setUserName("");

}
