/**
 * main.js
 *
 * Application-specific logic, contains the server URL.
 *
 * For now, includes talking to the proteus/homer server.
 *
 */
var showSideBarFlag;
var GLOBAL = {
    uniqTypes: [], allTags: [], users: {}, userComments: [], corpora: []
};

// the JSON of the application state
var Model = {
    // retrieval result data
    //  request: {}, query: "", results: [], queryType: null, queryTerms: [], queryid: -1
};

var clearModelResults = function(m) {
    m.results = [];
    m.query = "";
};

var gSearchedKind = 'ia-books'; // default
var urlParams = getURLParams();
if (!_.isUndefined(urlParams["kind"])) {
    gSearchedKind = urlParams["kind"];
}

var privateURLParams = _(["user", "token"]);

var updateURL = function(request) {

    // TODO - showSideBar is defined lots of places and is of limited use except on the main index page.
    if (!_.isUndefined(showSideBarFlag)) {

        if (showSideBarFlag) {
            request = _.merge(request, {'showSideBar': '1'});
        } else {
            request = _.merge(request, {'showSideBar': '0'});
        }

    }

    pushURLParams(_.omit(request, privateURLParams));
};

var SEARCH_FOR_EVERYTHING = "*all*";

/**
 * document.ready handler onload
 *
 * reads the ?q=foo parameters and sends of a JSON API request
 */
UI.setReadyHandler(function() {
    var params = _.omit(getURLParams(), privateURLParams);
    var userToken = getCookie("token");
    setCorpus(getCookie("corpus"));
    params = _.merge(params, {token: userToken});
    console.log(params);

    UI.dispalyUserName();

    doActionRequest(params);

});

/**
 * This main "action-request" delegates to other things. Notice how retrieval requests disappear into actions.js early.
 */
var doActionRequest = function(args) {

    var action = args.action;
    if (action == "bib") {
        return doBibliographyRequest(args);
    }
    if (action == "search") {
        UI.setQuery(args.q);
        return doSearchRequest(args);
    }
    if (action == "view") {
        var corpus = getCookie("corpus");
        var corpusID = getCorpusID(corpus);
        var userName = getCookie("username");
        args = _.merge(args, {'corpusID': corpusID, 'user': userName});
        //disableAutoRetrieve();
        return doViewRequest(args);
    }
    // else, nothing special to do
};

/* handlers for retrieval button types */
UI.onClickSearchButton = function() {

    // disable the buttons so they can't quickly switch between them which mixes result sets
    UI.enableSearchButtons(false);

    // get the kind from the radio buttons
    kind = $('input[name=search-kind]:checked').val();

    $("#search-button-text").html("Search " + $('input[name=search-kind]:checked').parent().text());
    // TODO ??? this needs to be set correctly if they REFRESH the page rather than hit the button
    gSearchedKind = kind;
    // make this selection the "current default"
//    $("#search-buttons").unbind("click"); // prevent multiple clicks
//    $("#search-buttons").click(function() {
//        UI.onClickSearchButton(kind, text);
//    });
//    $("#search-button-text").html(text);


    doActionRequest({kind: kind, q: UI.getQuery(), action: "search"});
};

/*UI.onClickVizButton = function() {

    console.log("clicked visualization  button!");
    doActionRequest({kind: "all", q: "", action: "viz"});
};*/

function buildSearchLink(entType, q, kind) {
    var query = entType + ':"' + q + '"';
    return "index.html?action=search&kind=" + kind + "&q=" + query;
}

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
            document.cookie = "fields=" + data.fields + ";";
            var settings = JSON.parse(data.settings);

            localStorage["corpora"] = JSON.stringify(data.corpora);
            localStorage["subcorpora"] = JSON.stringify(data.subcorpora);
            document.cookie = "broadcast=;";
            if (!_.isUndefined(data.broadcast)) {
                // TODO: duplicate info in the cookie and local storage
                document.cookie = "broadcast=" + JSON.stringify(data.broadcast) + ";";
                // set all the messages we can receive and if we want to see them.
                localStorage["messages"] = JSON.stringify(data.broadcast.messages);
                if (_.isUndefined(settings.broadcast)) {
                    settings.broadcast = {};
                }
                // if there are any new broadcast messages, default them to "Yes, I want to see them"
                // until the user explicitly disables them on the setting page.
                for (msg in data.broadcast.messages) {

                    if (!settings.broadcast.hasOwnProperty(data.broadcast.messages[msg])) {
                        settings.broadcast[data.broadcast.messages[msg]] = "Y";
                    }
                }
            }
            document.cookie = "settings=" + JSON.stringify(settings) + ";";
            // TODO ??? should we save these if they changed?

            UI.updateCorpusListButton();
            UI.dispalyUserName();
            showSideBar();
            removeURLParam("user");
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
        document.cookie = "corpus=;";
        document.cookie = "fields=;";
        // quick and dirty, trigger refresh to get rid of
        // all the label stuff.
        location.reload(true);

    }, function(req, status, err) {
        UI.showError("ERROR: ``" + err + "``");
        throw err;
    });


};

var getAllUsers = function() {

    if (!isLoggedIn()) {
        return;
    }

    API.getUsers(null, function(data) {

        $.each(data.users, function(key, value) {
            GLOBAL.users[key] = value;
        });

    }, function(req, status, err) {
        UI.showError("ERROR: ``" + err + "``");
        throw err;
    });
};


var createNewCorpus = function(corpusName) {

    if (!isLoggedIn()) {
        return;
    }

    var userName = getCookie("username");

    var args = {user: userName, corpus: corpusName};

    API.newCorpus(args, function(data) {

        UI.appendToCorpusList(corpusName);
        // add this to the localStorage
        var tmp = JSON.parse(localStorage["corpora"]);
        tmp.push({name: corpusName, id: data.id});
        localStorage["corpora"] = JSON.stringify(tmp);
        // need to re-bind click event...
        bindCorpusMenuClick();

        // set the corpus selection to the newly created one...
        setCorpus(corpusName);
    },
            function(req, status, err) {
                console.log("ERROR: ``" + err + "``");
                UI.showError("ERROR: ``" + err + "``");
                throw err;
            });
};

// get all tags grouped by user on start up
getAllUsers();
// todo ? ??  should move this to documentReady?