/**
 * ui.js
 *
 * This file roughly contains all the interfacing with the index.html boilerplate, using jquery as needed.
 *
 */

var errorDiv = $("#error");
var resultsDiv = $("#results");
var viewResourceDiv = $("#view-resource");
var progressDiv = $("#progress");
var queryBox = $("#ui-search");
var loginInfo = $("#ui-login-info");
var moreButton = $("#ui-go-more");
var searchButtons = $("#search-buttons");


queryBox.keypress(function(e)
{
if (e.keyCode == 13)
handleEnter();
});

// UI object/namespace
var UI = {};

UI.generateButtons = function() {
  API.getKinds({}, function(data) {
    var availableKinds = _(data.kinds);
    var buttonDescriptions = _(UI.buttons);

    _.forIn(data.kinds, function(spec, kind) {
      spec.kind = kind; // so the onClick knows what kind it was
      if (!spec.button) {
        UI.showError("You need to specify a \"button\" display attribute for kind \"" + kind + "\"");
      }
      var button = $('<input type="button" value="' + spec.button + '" />');
      button.click(function() {
        UI.onClickSearchButton(spec);
      });
      searchButtons.append(button)
    });
  });
};

UI.clear = function() {
  UI.clearResults();
  UI.clearError();
  viewResourceDiv.html('');
};

UI.clearResults = function() {
  viewResourceDiv.hide();
  resultsDiv.html('');
  resultsDiv.show();
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
  var renderer = getResultRenderer(result.viewKind);
  return '<div class="result">' + renderer(queryTerms, result) + '</div>';
};

/**
 * Renders search results into UI after current results
 */


UI.appendResults = function(queryTerms, results) {
  UI.showProgress("Ajax response received!");

  _(results).forEach(function(result) {
    console.debug("result name: " + result.name);
    resultsDiv.append(UI.makeResult(queryTerms, result));


    var tagName = "#tags_" + result.name;

    $(tagName).tagit({
      availableTags: GLOBAL.uniqTypes,
      autocomplete: {delay: 0, minLength: 0},
      allowSpaces: true,
      placeholderText: "Add a Label",
      // uniqueID: result.name

      afterTagRemoved: function(event, ui) {

        deleteTag(ui.tagLabel, result.name);
        return true;

      },
      beforeTagAdded: function(event, ui) {
        if (!ui.duringInitialization) {
          // only ask "are you sure" if this is a NEW tag TYPE
          tmp = ui.tagLabel.split(":");

          var res = true;
          if (tmp.length === 2 && $.inArray(tmp[0], GLOBAL.uniqTypes) === -1) {
            res = confirm("Are you sure you want to create the label type \"" + tmp[0] + "\"?");
            // add the new type to our list
            GLOBAL.uniqTypes.push(tmp[0]);
            UI.appendMyTag(tmp[0]);

          }
          if (res == true) {
            addTag(ui.tagLabel, result.name);
          }
          return res;

        } else {
          return true;
        }
      }
    });
    $(".read-only-tags").tagit({
      readOnly: true
    });

    moreButton.show();

  });
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

UI.renderTags = function(result) {

  var html = '<div>';
  // we ALWAYS want a div if you're logged in so you can add tags
  var username = getCookie("username");
  if (username !== "") {

    html += '<ul id="tags_' + result.name + '">  ';
    if (typeof result.tags[username] !== 'undefined') {
      //console.log(result.tags[username].toString());
      tags = result.tags[username].toString().split(',');
      //console.log(tags);
      for (var tag in tags) {
        html += '  <li> ' + tags[tag] + ' </li> ';
      }
    }

    html += '</ul>';

  } // end if someone is logged in

  var tmp_html = "";
  var read_only_tags = false;
  for (var user in result.tags) {

    tags = result.tags[user].toString().split(',');
    // skip current user
    if (user !== username) {
      for (tag in tags) {
        read_only_tags = true;
        tmp_html += '  <li> ' + tags[tag] + ' </li> ';
      }
    }
  }
  // only show the tag box if there is something to show
  if (read_only_tags === true) {
    html += '<ul class="read-only-tags">' + tmp_html + '</ul>'
  }
  html += '</div>';
  return html;
};

UI.appendMyTag = function(name) {

  $("#my-tags").append('<button class="ui-widget-content ui-state-default type-button">' + name + '</button>');

  if ($("#toggle-my-tags-img").is(":visible") === false) {
    $("#toggle-my-tags-img").attr("src", "/images/up_arrows.png");
    $("#toggle-my-tags-img").show();
    $("#my-tags").show();

  }
};
UI.clearAllMyTags = function( ) {


  $("#my-tags").html("");

};

// used when we don't want to dispaly any of the "my tags" features
UI.hideMyTagsFunctionality = function() {
  UI.clearAllMyTags();
  $("#toggle-my-tags-img").hide();
  $("#my-tags").hide();

};

UI.toggleMyTags = function() {
  var ele = $("#my-tags");
  if (ele.is(":visible")) {
    $("#toggle-my-tags-img").attr("src", "/images/down_arrows.png");
    ele.hide();
  }
  else {
    $("#toggle-my-tags-img").attr("src", "/images/up_arrows.png");
    ele.show();
  }
};
