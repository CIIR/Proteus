/**
 * main.js
 *
 * Application-specific logic, contains the server URL.
 *
 * For now, includes talking to the proteus/homer server.
 *
 */

var GLOBAL = {
    uniqTypes: [], allTags: [], users: {}, userComments: [], corpora: [],
    ratedDocuments: [
        {"doc" : "westindiesincana1916grea", "aveRating" : 3},
        {"doc" : "sirfrancisdrakeh04mayn_75", "aveRating" : 1.5},
        {"doc" : "westindiesincana1916grea_18", "aveRating" : 5},
        {"doc" : "obeahwitchcraft00bellgoog_197", "aveRating" : 4.5},
        {"doc" : "obeahwitchcraft00bellgoog", "aveRating" : 3}
    ]
};

// the JSON of the application state
var Model = {
    // retrieval result data
  //  request: {}, query: "", results: [], queryType: null, queryTerms: [], queryid: -1
};

var clearModelResults = function(m){
    m.results = [];
    m.query = "";
}

var gSearchedKind = 'ia-books'; // default
var urlParams = getURLParams();
if (!_.isUndefined(urlParams["kind"])) {
    gSearchedKind = urlParams["kind"];
}
//Model.clearResults = function() {
//    Model.results = [];
//    Model.query = "";
//};

var privateURLParams = _(["user", "token"]);

var updateURL = function(request) {

    if (showSideBarFlag){
        request = _.merge(request, {'showSideBar' : '1'});
    } else {
        request = _.merge(request, {'showSideBar' : '0'});
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

    if (params.action == "search" && (!isBlank(params.q) || !isBlank(params.labels))) {
        UI.setQuery(params.q);
        doActionRequest(params);
    } else if (params.action == "view") {
        doActionRequest(params);
    }

});

/**
 * This main "action-request" delegates to other things. Notice how retrieval requests disappear into actions.js early.
 */
var doActionRequest = function(args) {
    var action = args.action;
    if (action == "search") {
        return doSearchRequest(args);
    }
    if (action == "view") {
        var corpus = getCookie("corpus");
        var corpusID = getCorpusID(corpus);
        args = _.merge(args, {'corpusID' : corpusID});
        disableAutoRetrieve();
        return doViewRequest(args);
    }
    if (!action) {
        UI.showError("action not defined when calling doActionRequest in JS");
        return;
    }
    UI.showError("Unknown action `" + action + "'");
};

/* handlers for retrieval button types */
UI.onClickSearchButton = function(kind, text) {
//    var kind = buttonDesc.kind;

    // TODO ??? this needs to be set correctly if they REFRESH the page rather than hit the button
    gSearchedKind = kind;
    // make this selection the "current default"
    $("#search-buttons").unbind("click"); // prevent multiple clicks
    $("#search-buttons").click(function() {
        UI.onClickSearchButton(kind, text);
    });
    $("#search-button-text").html(text);

    // is this a new retrieval?
    var terms = _.escape(UI.getQuery().trim()).toLowerCase();

    var tmp = [];
    if (localStorage["pastSearches"] != null){
        tmp  = JSON.parse(localStorage["pastSearches"]);
    }
    // check if it exists
    var found = false;
    _.forEach(tmp, function(rec){
        if (rec.kind == kind && rec.terms == terms){
            found = true;
        }
    });

    if (found != true){
        tmp.push({ terms: terms, kind: kind});
        localStorage["pastSearches"] = JSON.stringify(tmp);
        if (terms.length == 0){
            terms = SEARCH_FOR_EVERYTHING;
        }
        $("#pastSearches").prepend( '<div class="query">&#8226;&nbsp;<a onclick="tmpSearch( $(this), \''+kind+'\')">' + terms +  '</a>'+ '&nbsp;(' + kind + ')<div class="xtmp"><img class="delimg" src=\'images/del.png\'/> </div>&nbsp;</div>' );
    }
    doActionRequest({kind: kind, q: UI.getQuery(), action: "search"});
};

UI.populateRecentSearches = function(){

    // TODO should be it's own function
    var corpus = getCookie("corpus");
    if (corpus == ""){
        $("#pastSearches").html("");
        $('#note-list').html("");
        return;
    }

    var corpusID = getCorpusID(corpus);
    var args = {corpus: corpusID};

    API.getNoteHistory(args,
            function(results) {

                var html = '';
                for (i in results.rows){
                    rec = results.rows[i];
                    var identifier = rec.uri.split('_')[0];
                    var pageNum = rec.uri.split('_')[1];

                    // strip the seconds/milliseconds from the date
                    var dt = rec.dttm.substring(0, rec.dttm.lastIndexOf(":"));
                    var name = rec.user.toString();
                        name = name.split("@")[0];

                    html += '&#8226;&nbsp;' + dt + ' ' + name + ': <i>' + rec.text
                        + '</i> view: <a target="_blank" href="view.html?kind=ia-pages&action=view&id=' + identifier + '&pgno=' + pageNum + '&noteid=' + rec.id + '">Page, </a>'
                        + '<a target="_blank" href="view.html?kind=ia-books&action=view&id=' + identifier + '_' + pageNum +'&noteid=' + rec.id + '">Book</a><br>';

                }
                $('#note-list').html(html);
            },
            function() {alert("error getting notes!")});


    if (localStorage["pastSearches"] == null){
        return;
    }
    $("#pastSearches").html("");
    _.forEach( JSON.parse(localStorage["pastSearches"]), function(rec){
        $("#pastSearches").prepend( '<div class="query">&#8226;&nbsp;<a onclick="tmpSearch( $(this), \''+rec.kind+'\')">' + rec.terms +  '</a>'+ '&nbsp;(' + rec.kind + ')<div class="xtmp"><img class="delimg" src=\'images/del.png\'/> </div>&nbsp;</div>' );
    });
}

function tmpSearch(that, kind){
    var query = that.text();
    if (query == SEARCH_FOR_EVERYTHING){
        query = "";
    }
    console.log("Query: " + query);
    $("#ui-search").val(query);
    // TODO: can have > 1 retrieval button so we need to know which one to trigger
    doActionRequest({kind: kind, q: query, action: "search"});
}

//function tmpEntSearch(entType, that, kind){
//    var query =  entType  + ':"' + that.text() + '"';
//    console.log("Query: " + query);
//    $("#ui-search").val(query);
//    UI.onClickSearchButton(kind);
//}

function buildSearchLink(entType, q,  kind){
    var query =  entType  + ':"' + q + '"';
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
            var settings = JSON.parse(data.settings);

            localStorage["corpora"] = JSON.stringify(data.corpora);
            localStorage["subcorpora"] = JSON.stringify(data.subcorpora);
            document.cookie = "broadcast=;";
            if (!_.isUndefined(data.broadcast)){
                // TODO: duplicate info in the cookie and local storage
                document.cookie = "broadcast=" + JSON.stringify(data.broadcast) + ";";
                // set all the messages we can receive and if we want to see them.
                localStorage["messages"] = JSON.stringify(data.broadcast.messages);
                if (_.isUndefined(settings.broadcast)){
                    settings.broadcast = {};
                }
                // if there are any new broadcast messages, default them to "Yes, I want to see them"
                // until the user explicitly disables them on the setting page.
                for (msg in data.broadcast.messages){

                    if (!settings.broadcast.hasOwnProperty(data.broadcast.messages[msg])){
                        settings.broadcast[data.broadcast.messages[msg]] = "Y";
                    }
                }
            }
            document.cookie = "settings=" + JSON.stringify(settings) + ";";
            // TODO ??? should we save these if they changed?

            UI.updateCorpusListButton();
            UI.dispalyUserName();
            // update the type tags
            getAllTagsByUser();
            location.reload(true);
            showSideBar();
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

var createNewCorpus = function(corpusName){

    if (!isLoggedIn()) {
        return;
    }

    var userName = getCookie("username");

    var args = {user: userName, corpus: corpusName};

    API.newCorpus(args, function(data) {

                UI.appendToCorpusList(corpusName);
                // add this to the localStorage
                var tmp = JSON.parse(localStorage["corpora"]);
                tmp.push({ name: corpusName, id: data.id });
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
}

// get all tags grouped by user on start up
getAllUsers();
