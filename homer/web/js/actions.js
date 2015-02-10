// This file handles the flow of a search request
// it interacts a lot with Model which is defined in main.js
//

var doSearchRequest = function(args) {
    var defaultArgs = {
        n: 10,
        skip: 0,
        snippets: true,
        metadata: true
    };

    // we could have args passed in esp if they're reusing an URL
    if (_.isUndefined(args.labels)) {
        var labelList = getSelectedLabels();

        if (!_.isEmpty(labelList)) {
            var labelArgs = '{ "labels":  ' + JSON.stringify(labelList) + '}';
            args = _.merge(args, JSON.parse(labelArgs));
        }
    } else {
        // format them correctly
        var labelArgs = '{ "labels":  ' + JSON.stringify(args.labels.split(",")) + '}';
        args.labels = "";
        args = _.merge(args, JSON.parse(labelArgs));
    }

    // if we didn't ask for more
    if (!args.skip || args.skip === 0) {
        Model.clearResults();
        UI.clearResults();
        updateURL(args); // modify URL if possible
    }

    var userName = getCookie("username");

    if (userName != "") {
        var userToken = getCookie("token");
        var userID = getCookie("userid");
        var tagArgs = {
            tags: true,
            user: userName,
            userid: userID,
            token: userToken
        };
        args = _.merge(args, tagArgs);
    }
    var actualArgs = _.merge(defaultArgs, args);

    if ((!actualArgs.q || isBlank(actualArgs.q)) && (_.isEmpty(actualArgs.labels))) {
        UI.showProgress("Query is blank!");
        return;
    }

    Model.request = actualArgs;
    console.log(Model.request);

    UI.showProgress("Search Request sent to server!");
    API.action(actualArgs, onSearchSuccess, function(req, status, err) {
        UI.showError("ERROR: ``" + err + "``");
        throw err;
    });

    return actualArgs;
};

/**
 * This gets called with the response from JSONSearch
 */
var onSearchSuccess = function(data) {
    UI.clearError();

    console.log(data);

    // mark up results with rank and kind
    Model.query = data.request.q;
    Model.queryType = data.queryType;

    var rank = Model.results.length + 1;
    var newResults = _(data.results).map(function(result) {
        result.viewKind = data.request.viewKind || data.request.kind;
        result.kind = data.request.kind;
        result.rank = rank++;

        return result;
    }).value();

    // update the model
    Model.results = _(Model.results).concat(data.results).value();

    // don't show results if empty
    if (_.isEmpty(data.results)) {
        UI.showProgress("No results found for '" + data.request.q + "'");
        return;
    }
    var usingLabels = false;
    var tree = $("#tree").fancytree("getTree");
    if (!_.isUndefined(data.request.labels)) {
        usingLabels = true;

        // if the labels are on the URL AND they're ours, select them
        if (!_.isUndefined(data.request.labelOwner) && data.request.labelOwner == getCookie("userid"))
            for (var val in data.request.labels) {
                tree.getNodeByKey(data.request.labels[val]).setSelected(true);
            }
    }
    // lowercase the query terms so when we hilight we match 
    // regardless of case

    var lowerTerms = [];

    var termLen = 0;

    if (!_.isUndefined(data.queryTerms))
        termLen = data.queryTerms.length;

    for (var i = 0; i < termLen; i++) {
        lowerTerms.push(data.queryTerms[i].toLowerCase());
    }
    UI.appendResults(lowerTerms, newResults, usingLabels);
};

var doViewRequest = function(args) {
    UI.showProgress("View request sent to server!");
    API.action(args, onViewSuccess, function(req, status, err) {
        UI.showError("ERROR: ``" + err + "``");
        throw err;
    });
};

/** this gets called with the response from ViewResource */
var onViewSuccess = function(args) {
    UI.clearError();
    moreButton.hide();
    resultsDiv.hide();
    var html = '';
    html += '<table>'
    _(args.metadata).forIn(function(val, key) {
        html += '<tr>';
        html += '<td>' + key + '</td>';
        html += '<td>' + val + '</td>'
        html += '</tr>';
    })
    html += '</table>'
    console.log(args.metadata);
    html += '<div>' + _.escape(args.text) + '</div>';
    viewResourceDiv.html(html);
    viewResourceDiv.show();
};

