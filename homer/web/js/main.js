/**
 * main.js
 *
 * Application-specific logic, contains the server URL.
 *
 * For now, includes talking to the proteus/homer server.
 *
 */

var GLOBAL = {
    uniqTypes: [], allTags: [], users: {}, userComments: []
};

// the JSON of the application state
var Model = {
    // search result data
    request: {}, query: "", results: [], queryType: null, queryTerms: []
};

Model.clearResults = function() {
    Model.results = [];
    Model.query = "";
};

var privateURLParams = _(["user", "token"]);

var updateURL = function(request) {

    if (showSideBarFlag){
        request = _.merge(request, {'showSideBar' : '1'});
    } else {
        request = _.merge(request, {'showSideBar' : '0'});
    }

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

    UI.dispalyUserName();

    if (params.action == "search" && (!isBlank(params.q) || !isBlank(params.labels))) {
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
        disableAutoRetrieve();
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
            document.cookie = "userid=" + data.userid + ";";
            document.cookie = "token=" + data.token + ";";
            UI.dispalyUserName();
            // update the type tags
            getAllTagsByUser();
            location.reload(true);
        }, function(req, status, err) {
            UI.showError("ERROR: ``" + err + "``");
            throw err;
        })
    };

    API.register(args, loginFunc, loginFunc);

};

var logOut = function() {

    var userName = getCookie("username");
    var userID = getCookie("userid");
    var userToken = getCookie("token");

    var args = {user: userName, token: userToken, userid: userID};
    API.logout(args, function() {
        document.cookie = "username=;";
        document.cookie = "token=;";
        document.cookie = "userid=;";
        // update the type tags
        getAllTagsByUser();
        // quick and dirty, trigger refresh to get rid of
        // all the label stuff.
        location.reload(true);
    }, function(req, status, err) {
        UI.showError("ERROR: ``" + err + "``");
        throw err;
    });


};

var addTag = function(tagText, resourceID, rating, comment) {
    var userName = getCookie("username");
    var userToken = getCookie("token");
    var userID = getCookie("userid");

    comment = comment.replace(/\n/g, "\\n");
    var tmp = '{  "userid": ' + userID + ', "user": "' + userName + '", "token" :"' + userToken + '", "rating" : '
            + rating + ', "comment" : "' + comment + '" ,"tags": {"' + formatLabelForDatabase(tagText) + '": ["' + resourceID + '"]}}';
    console.log(tmp);
    var args = JSON.parse(tmp);
    API.createTags(args, null, function(req, status, err) {
        UI.showError("ERROR: ``" + err + "``");
        throw err;
    });

};


var updateTag = function(tagText, resourceID, rating, comment) {
    var userName = getCookie("username");
    var userToken = getCookie("token");
    var userID = getCookie("userid");

    comment = comment.replace(/\n/g, "\\n");
    var tmp = '{  "userid": ' + userID + ', "user": "' + userName + '", "token" :"' + userToken + '", "rating" : '
            + rating + ', "comment" : "' + comment + '" ,"tags": {"' + formatLabelForDatabase(tagText) + '": ["' + resourceID + '"]}}';
    console.log(tmp);
    var args = JSON.parse(tmp);
    API.updateTags(args, null, function(req, status, err) {
        UI.showError("ERROR: ``" + err + "``");
        throw err;
    });

};

var deleteTag = function(tagText, resourceID) {
    var userName = getCookie("username");
    var userID = getCookie("userid");
    var userToken = getCookie("token");

    var tmp = '{ "userid": ' + userID + ', "user": "' + userName + '", "token" :"' + userToken + '", "tags": {"' + formatLabelForDatabase(tagText) + '": ["' + resourceID + '"]}}';
    console.log(tmp);
    var args = JSON.parse(tmp);
    API.deleteTags(args, null, function(req, status, err) {
        UI.showError("ERROR: ``" + err + "``");
        throw err;
    });

};

var getAllUsers = function() {

    if (!isLoggedIn()) {
        return;
    }
    var userName = getCookie("username");
    var userID = getCookie("userid");
    var userToken = getCookie("token");

    var tmp = '{ "userid": ' + userID + ', "user": "' + userName + '", "token" :"' + userToken + '", "labels":  ["*.newTag"]}';
    console.log(tmp);
    var args = JSON.parse(tmp);
    API.getResourcesForLabels(args, function(data) {
        var x = 2332;
    }, function(req, status, err) {
        UI.showError("ERROR: ``" + err + "``");
        throw err;
    });

    API.getUsers(null, function(data) {

        $.each(data.users, function(key, value) {
            GLOBAL.users[key] = value;
        });

        // we have the users/IDs, now get the tags
        getAllTagsByUser();

    }, function(req, status, err) {
        UI.showError("ERROR: ``" + err + "``");
        throw err;
    });
};

// function to get all the tags for all users.
var getAllTagsByUser = function() {

    if (!isLoggedIn()) {
        return;
    }

    var userName = getCookie("username");
    var userID = getCookie("userid");
    var userToken = getCookie("token");
    var uniqType = [];

    var args = {resource: ["%"], user: userName, userid: userID, token: userToken};
    API.getAllTagsByUser(args, function(origresult) {

        var keys = Object.keys(origresult);

        GLOBAL.allTags = origresult[keys[0]];

        console.log("tags: " + origresult);

        // not the most efficient code in the world
        for (user in GLOBAL.allTags) {
            // labels are the keys in a map, the values are the rating and comment

            tags = GLOBAL.allTags[user];
            for (tag in tags) {
                uniqType.push(tag.split(":")[0]);
            }
            UI.createLabelMultiselect(user);
        }

        GLOBAL.uniqTypes = _.uniq(uniqType);
        var typeHTML = "";

        if (typeof GLOBAL.allTags[userID] !== 'undefined') {
            // get just our types
            var myTypes = [];
            tags = GLOBAL.allTags[userID];
            for (tag in tags) {
                var kv = tag.split(":");
                myTypes.push(kv[0]);
            }

            var type;
            var myUniq = _.uniq(myTypes);
            //       UI.createLabelMultiselect(myUniq);
            for (type in myUniq) {
                // get the values just for this type
                var myValues = [];
                var tags = GLOBAL.allTags[userID];
                for (tag in tags) {
                    var kv = tag.split(":");

                    if ((kv[0] === myUniq[type]) && (!_.isUndefined(kv[1]))) {
                        myValues.push(kv[1]);
                    }
                }
            }
        }


    }, function(req, status, err) {
        UI.showError("ERROR: ``" + err + "``");
        throw err;
    });
};


// get all tags grouped by user on start up
getAllUsers();

function hideSideBar(){
  $('#sidebar-button').html("&gt;&gt;");
  $("#results-left").hide();
  $("#results-right").removeClass("col-md-10");
  $("#results-right").addClass("col-md-12");
  showSideBarFlag = false;
  p = getURLParams();
  p = _.merge(p, {'showSideBar' : '0'});
  pushURLParams(p);
}
function showSideBar(){
  $('#sidebar-button').html("&lt;&lt;");
  $("#results-left").show();
  showSideBarFlag = true;
  $("#results-right").removeClass("col-md-12");
  $("#results-right").addClass("col-md-10");

  p = getURLParams();
  p = _.merge(p, {'showSideBar' : '1'});
  pushURLParams(p);
}
$('#sidebar-button').click(function() {
    if (showSideBarFlag == true) {
      hideSideBar();
     } else {
      showSideBar();
    }
});


						