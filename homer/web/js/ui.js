/**
 * ui.js
 *
 * This file roughly contains all the interfacing with the index.html boilerplate, using jquery as needed.
 *
 */

var errorDiv = $("#error");
var resultsDiv = $("#results");
var progressDiv = $("#progress");
var queryBox = $("#ui-search");
var loginInfo = $("#ui-login-info");
var moreButton = $("#ui-go-more");
var searchButtons = $("#search-buttons");

// UI object/namespace
var UI = {};

// a list of defined buttons
UI.buttons = [
  {display: "Pages", kind: "ia-pages" },
  {display: "Books", kind: "ia-books" },
  {display: "Metadata", kind: "ia-meta" }
];

UI.generateButtons = function() {
  _(UI.buttons).forEach(function(buttonDesc) {
    var button = $('<input type="button" value="'+buttonDesc.display+'" />');
    button.click(function() { UI.onClickSearchButton(buttonDesc); });
    searchButtons.append(button)
  });
};

UI.clear = function() {
  UI.clearResults();
  clearError();
};

UI.clearResults = function() {
  resultsDiv.html('');
  moreButton.hide();
};

UI.showProgress = function(str) {
  progressDiv.html(str);
};

UI.showError = function(str) {
  errorDiv.html(str);
  errorDiv.show();
};

UI.clearError = function() {
  errorDiv.html('');
  errorDiv.hide();
};

UI.getQuery = function() {
  return queryBox.val();
};

UI.setQuery = function(q) {
  queryBox.val(q);
};

/**
 * Render a single search result.
 * @see render.js
 */
UI.makeResult = function(queryTerms, result) {
  var renderer = getResultRenderer(result.kind);
  return '<div class="result">'+renderer(queryTerms, result)+'</div>';
};

/**
 * Renders search results into UI after current results
 */
UI.appendResults = function(queryTerms, results) {
  UI.showProgress("Ajax response received!");

  _(results).forEach(function(result) {
    resultsDiv.append(UI.makeResult(queryTerms, result));
  });
  moreButton.show();
};

/**
 * A set of functions for reacting to events in other, more general code.
 */
UI.setReadyHandler = function(callback) {
  $(document).ready(function() {
    UI.generateButtons();
    callback();
  });
};
UI.setMoreHandler = function(callback) {
  moreButton.click(callback);
};

UI.setUserName = function(user) {
  if (!user) {
    UI.clearUserName();
  } else {
    loginInfo.html("Welcome " + user + " <input id='ui-go-logout' type='button' value='LogOut' />");
    $("#ui-go-logout").click(function() {
      logOut();
    });
  }
};

UI.clearUserName = function() {
  loginInfo.html(" <input id='ui-username' type='text' /> " +
          "<input id='ui-go-login' type='button' value='Login' />");
  // have to bind click event here because that ID doesn't exist until we do this.
  $("#ui-go-login").click(function() {
    var username = $("#ui-username").val();
    logIn(username);
    UI.setUserName(username);
  });
};

