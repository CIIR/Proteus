// This file handles the flow of a search request
// it interacts a lot with Model which is defined in main.js
//

var perStartRegEx = new RegExp("<PERSON>", 'g')
var locStartRegEx = new RegExp("<LOCATION>", 'g')
var orgStartRegEx = new RegExp("<ORGANIZATION>", 'g')
var endRegEx = new RegExp("<\/PERSON>|<\/LOCATION>|<\/ORGANIZATION>", 'g')

var page = {
    previous: -1,
    current: -1,
    next: -1,
    max: -1,
    skips: 0,
    MAX_SKIPS: 1000
}
var doSearchRequest = function(args) {

    disableAutoRetrieve(); // prevent double requests

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

    $("#more").html('<img src="/images/more-loader.gif"\>');

    Model.request = actualArgs;
    console.log(Model.request);

    UI.showProgress("Search Request sent to server!");
    API.action(actualArgs, onSearchSuccess, function(req, status, err) {
        UI.showError("ERROR: ``" + err + "``");
        // set up the auto retrieve again
        enableAutoRetrieve();
        throw err;
    });

    return actualArgs;
};

/**
 * This gets called with the response from JSONSearch
 */
var onSearchSuccess = function(data) {
    UI.clearError();

    var userID = getCookie("userid");
    $("#more").html(""); // clear progress animation
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
        if (!_.isUndefined(data.request.labelOwner) && data.request.labelOwner == userID)
            for (var val in data.request.labels) {
                tree.getNodeByKey(GLOBAL.users[userID] + TREE_KEY_SEP() + data.request.labels[val]).setSelected(true);
            }
    }
    // lowercase the query terms so when we hilight we match 
    // regardless of case

    var termLen = 0;

    if (!_.isUndefined(data.queryTerms))
        termLen = data.queryTerms.length;

    // clear out previous query terms
    Model.queryTerms = [];
    for (var i = 0; i < termLen; i++) {
        Model.queryTerms.push(data.queryTerms[i].toLowerCase());
    }
    UI.appendResults(Model.queryTerms, newResults);

    // if we searched by labels, we returned EVERYTHING so we
    // don't re-enable the auto-retrieve
    if (usingLabels === false)
      enableAutoRetrieve();
};

var doViewRequest = function(args) {
    UI.showProgress("View request sent to server!");
    API.action(args, onViewSuccess, function(req, status, err) {
        UI.showError("ERROR: ``" + err + "``");
        throw err;
    });
};

var viewPrevPageSuccess = function(args) {
    if (args.found == false){
        // get the prior page
        page.previous -= 1;
        doPrevPageRequest(args.request.page_id, page.previous);
        return;
    }
    if (!_.isUndefined(args.text)) {
        $("#prevPage").html(processTags(args.text) + "<br>" + $("#prevPage").html());
    }
    page.previous -= 1;
    setPageNavigation(args.request.page_id);

}

var viewNextPageSuccess = function(args) {

    // check if we're going beyond the end. We use the "number of images"
    // from the metadata, if that's not available, we'll stop after a set
    // number of skips
    if ((page.max != -1 && page.next > page.max) ||  page.skips > page.MAX_SKIPS){
        setPageNavigation(args.request.page_id);
        UI.showProgress("Reached the end of the book");
        return;
    }
    if (args.found == false){
        // count the number of pages we skipped
        page.skips += 1;
        // get the next page
        page.next += 1;
        doNextPageRequest(args.request.page_id, page.next);
        return;
    }
    if (!_.isUndefined(args.text)) {
        page.skips = 0; // reset
        $("#nextPage").html( $("#nextPage").html() + "<br>" + processTags(args.text));
    }
    page.next += 1;
    setPageNavigation(args.request.page_id);

}

var doPrevPageRequest = function(pageID) {
    console.log("id: " + pageID + " current page: " + page.previous);
    if (page.previous < 0){
        UI.showProgress("Found start of the book");
        setPageNavigation(pageID);
        return;
    }
    UI.showProgress("View request sent to server!");
    // NOTE: some pages may not exist because the original page could have been blank.
    var id = pageID + '_' + page.previous;
    API.action({kind: "ia-pages", id: id , action: "view", page_id: pageID, page_num: page.previous}, viewPrevPageSuccess, function(req, status, err) {
        UI.showError("ERROR: ``" + err + "``");
        throw err;
    });
    UI.showProgress("");

};


var doNextPageRequest = function(pageID) {
    console.log("id: " + pageID + " current page: " + page.next);
    UI.showProgress("View request sent to server!");
    // NOTE: some pages may not exist because the original page could have been blank.
    var id = pageID + '_' + page.next;
    API.action({kind: "ia-pages", id: id , action: "view", page_id: pageID, page_num:  page.next}, viewNextPageSuccess, function(req, status, err) {
        UI.showError("ERROR: ``" + err + "``");
        throw err;
    });

    UI.showProgress("");
};

/** this gets called with the response from ViewResource */
var onViewSuccess = function(args) {

    var pageID = "";
    UI.clearError();
    resultsDiv.hide();
    metadataDiv.hide();
    if (args.request.kind == 'ia-pages'){
        pos = args.request.id.lastIndexOf('_');
        page.current = parseInt(args.request.id.substr(pos+1));
        page.previous = page.current-1;
        page.next = page.current+1;
        if (!_.isUndefined(args.metadata) && !_.isUndefined(args.metadata.imagecount)) {
            page.max = args.metadata.imagecount;
        }
        pageID = args.request.id.slice(0, pos);

    }
    var metaHtml = '';
    metaHtml += ' <table>'
    _(args.metadata).forIn(function(val, key) {
        metaHtml += '<tr>';
        metaHtml += '<td>' + key + '</td>';
        metaHtml += '<td>' + val + '</td>'
        metaHtml += '</tr>';
    })
    metaHtml += '</table></div>'
    metadataDiv.html(metaHtml);
    var html = '';
    html += '<a class="show-hide-metadata" onclick="UI.showHideMetadata();">Show Metadata</a>'

    // change any entity tags into HTML tags
    args.text = processTags(args.text);
   // console.log("*************************\n" + args.text);
  //  html += '<div>' + _.escape(args.text) + '</div>';
    html += '<div>[<span class="per">PERSON</span>]&nbsp;[<span class="loc">LOCATION</span>]&nbsp;[<span class="org">ORGANIZATION</span>]</div>';
    if (args.request.kind == 'ia-pages'){
        html += '<div class="pageNavigation"></div>';
        html += '<div id="prevPage"></div>';
    }

    html += '<div>' + (args.text) + '</div>';
    if (args.request.kind == 'ia-pages'){
        html += '<div id="nextPage"></div>';
        html += '<div class="pageNavigation"></div>';
    }

    viewResourceDiv.html(html);
    setPageNavigation(pageID);
    viewResourceDiv.show();
    UI.showProgress("");

};

var setPageNavigation = function(pageID) {

    var prevHTML = '<a onclick="doPrevPageRequest(\'' + pageID + '\',' + (page.previous) + ');">&Lt; Previous</a>&nbsp;';
    var nextHTML = '<a onclick="doNextPageRequest(\'' + pageID + '\',' + (page.next) + ');">Next &Gt;</a>';

    if ((page.max != -1 && page.next > page.max) || page.skips > page.MAX_SKIPS){
        nextHTML = '';
    }

    if (page.previous < 0){
        prevHTML = '';
    }

    $(".pageNavigation").html(prevHTML + "<br>" + nextHTML);

}
 var processTags = function(text){
     text = text.replace(perStartRegEx, "<span class=\"per\">");
     text = text.replace(locStartRegEx, "<span class=\"loc\">");
     text= text.replace(orgStartRegEx, "<span class=\"org\">");
     text = text.replace(endRegEx, "</span>");
     return text;
 }