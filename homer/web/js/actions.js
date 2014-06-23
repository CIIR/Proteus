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

    // if we didn't ask for more
    if (!args.skip || args.skip === 0) {
        Model.clearResults();
        UI.clearResults();
        updateURL(args); // modify URL if possible
    }

    var userName = getCookie("username");

    if (userName != "") {
        var userToken = getCookie("token");
        var tagArgs = {
            tags: true,
            user: userName,
            token: userToken
        };
        args = _.merge(args, tagArgs);
    }
    var actualArgs = _.merge(defaultArgs, args);

    var labelList = getSelectedLabels();

    if (!_.isEmpty(labelList)) {
        var labelArgs = '{ "labels":  ' + JSON.stringify(labelList) + '}';
        actualArgs = _.merge(actualArgs, JSON.parse(labelArgs));
    }

    if ((!actualArgs.q || isBlank(actualArgs.q)) && (_.isEmpty(labelList))) {
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
    if (!_.isUndefined(data.request.labels)) {
        usingLabels = true;
    }
    UI.appendResults(data.queryTerms, newResults, usingLabels);
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

