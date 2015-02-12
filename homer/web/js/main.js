/**
 * main.js
 *
 * Application-specific logic, contains the server URL.
 *
 * For now, includes talking to the proteus/homer server.
 *
 */

var GLOBAL = {
    uniqTypes: [],
    allTags: []
};

// the JSON of the application state
var Model = {
    // search result data
    request: {},
    query: "",
    results: [],
    queryType: null,
    // user login data
    user: null,
    userid: null,
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

    UI.dispalyUserName( );

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
            document.cookie = "userid=" + data.userid + ";";
            document.cookie = "token=" + data.token + ";";
            UI.dispalyUserName();
            // update the type tags
            getAllTagsByUser();
            Model.user = userName;
            Model.userid = data.userid;
            Model.token = data.token;
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
        Model.user = null;
        Model.token = null;
        Model.userid = null;
        // quick and dirty, trigger refresh to get rid of
        // all the label stuff.
        location.reload(true);
    }, function(req, status, err) {
        UI.showError("ERROR: ``" + err + "``");
        throw err;
    });



};

var addTag = function(tagText, resourceID, rating) {
    var userName = getCookie("username");
    var userToken = getCookie("token");
    var userID = getCookie("userid");

    var tmp = '{  "userid": ' + userID + ', "user": "' + userName + '", "token" :"' + userToken + '", "rating" : ' + rating + ',"tags": {"' + formatLabelForDatabase(tagText) + '": ["' + resourceID + '"]}}';
    console.log(tmp);
    var args = JSON.parse(tmp);
    API.createTags(args, null, function(req, status, err) {
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

// function to get all the tags for all users.
var getAllTagsByUser = function() {
    var userName = getCookie("username");
    var userID = getCookie("userid");
    var userToken = getCookie("token");
    var uniqType = [];

    var args = {resource: ["%"], user: userName, userid: userID, token: userToken};
    API.getAllTagsByUser(args, function(origresult) {

        var keys = Object.keys(origresult);

        GLOBAL.allTags = origresult[keys[0]];
        for (user in GLOBAL.allTags) {
            // not the most efficient code in the world
            tags = GLOBAL.allTags[user].toString().split(',');
            for (tag in tags) {
                uniqType.push(tags[tag].split(":")[0]);
            }
        }

        GLOBAL.uniqTypes = _.uniq(uniqType);
        var typeHTML = "";

        if (typeof GLOBAL.allTags[userID] !== 'undefined') {
            // get just our types
            var myTypes = [];
            tags = GLOBAL.allTags[userID].toString().split(',');
            for (tag in tags) {
                var kv = tags[tag].split(":");
                myTypes.push(kv[0]);
            }

            var type;
            var myUniq = _.uniq(myTypes);
            UI.createLabelMultiselect(myUniq);
            for (type in myUniq) {
                // get the values just for this type
                var myValues = [];
                var tags = GLOBAL.allTags[userID].toString().split(',');
                for (tag in tags) {
                    var kv = tags[tag].split(":");

                    if ((kv[0] === myUniq[type]) && (!_.isUndefined(kv[1]))) {
                        myValues.push(kv[1].split("@")[0]); // remove the rating
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
getAllTagsByUser();

var showSideBar = true;

$('#sidebar-button').click(function() {
    if (showSideBar == true) {
        $('#sidebar-button').html(">>");
        $("#results-left").hide();
        $("#results-right").removeClass("col-md-10");
        $("#results-right").addClass("col-md-12");
        showSideBar = false;
    } else {
        $('#sidebar-button').html("<<");
        $("#results-left").show();
        showSideBar = true;
        $("#results-right").removeClass("col-md-12");
        $("#results-right").addClass("col-md-10");
    }
}
);


						