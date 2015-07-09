// This file handles the flow of a search request
// it interacts a lot with Model which is defined in main.js
//

var perStartRegEx = new RegExp("<PERSON>", 'g')
var locStartRegEx = new RegExp("<LOCATION>", 'g')
var orgStartRegEx = new RegExp("<ORGANIZATION>", 'g')
var endRegEx = new RegExp("<\/PERSON>|<\/LOCATION>|<\/ORGANIZATION>", 'g')

var pageHeight = 0;
var MAX_PAGE_HEIGHT = 2000;
var MIN_PAGE_HEIGHT = 500;


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

    var tmpSettings = getCookie("settings");
    var settings;
    var numEntities = 5; // default
    if (tmpSettings != "") { // first time we don't have cookies
        settings = JSON.parse(tmpSettings);

        if (!_.isUndefined(settings.num_entities)) {
            numEntities = settings.num_entities;
        }
    }
    var defaultArgs = {
        n: 10,
        skip: 0,
        snippets: true,
        metadata: true,
        top_k_entities: parseInt(numEntities)
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
        var corpus = getCookie("corpus");
        var corpusID = -1
        if (corpus.length == 0){
            alert("Please select a corpus!");
            return;
        } else {
            corpusID = getCorpusID(corpus);
        }
        console.log("corpus: " + corpus + " id: " + corpusID)
        var tagArgs = {
            tags: true,
            user: userName,
            userid: userID,
            token: userToken,
            corpus: parseInt(corpusID),
            corpusName: corpus
        };
        args = _.merge(args, tagArgs);
    }
    var actualArgs = _.merge(defaultArgs, args);

    if (args.kind.endsWith("corpus")){
        actualArgs.action = "search-corpus";
    }

    // only allow blank queries if we're searching a corpus or by label(s)
    if ((!actualArgs.q || isBlank(actualArgs.q)) && (_.isEmpty(actualArgs.labels)) && ( actualArgs.action != "search-corpus")) {
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
    $("#per-cloud").hide();
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

        ratingsJSON.document[result.name] = [];
        // Loop through ratings
        _.forEach(result.ratings, function(rating){
            ratingsJSON.document[result.name].push({"user": rating.user, "rating": rating.rating + 2}); // +2 hack to keep it consistent with other ratings
        })

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

    // if we searched by labels or corpus, we returned EVERYTHING so we
    // don't re-enable the auto-retrieve
    if (usingLabels === false && Model.request.action != "search-corpus")
        enableAutoRetrieve();

    setUpMouseEvents(); // TODO : only want to do this once

};

function getNotesID(pageid, pagenum){
    if (parseInt(pagenum) >= 0)
        return 'notes-' + pageid + '_' +  pagenum;
    else
        return 'notes-' + pageid;
}
var getPageHTML = function(text, pageID, pageNum){
    var pgImage = pageImage(pageID, pageNum);
    var pgTxt=  processTags(text);
    return '<div class="book-page row clearfix ">' +
            '<div id="' + getNotesID(pageID, pageNum) + '" class="book-text col-md-5 column left-align">' + pgTxt + '</div>'+
            '<div  class="page-image col-md-5 column left-align"><br>' + '<a class="fancybox" href="' + pgImage + '" ><img src="' + pgImage + '"></a></div>' +
            '</div>';

}
var doViewRequest = function(args) {
    UI.showProgress("View request sent to server!");

        API.action(args,
            function(args){
                if (args.request.kind == 'ia-pages') {
                    onViewPageSuccess(args);
                } else {
                    onViewBookSuccess(args);
                }
            },
            function(req, status, err) {
                UI.showError("ERROR: ``" + err + "``");
                throw err;
        });

};
// TODO : should be in arcive js file
var viewPrevPageSuccess = function(args) {
    if (args.found == false){
        // get the prior page
        page.previous -= 1;
        doPrevPageRequest(args.request.page_id, page.previous);
        return;
    }
    if (!_.isUndefined(args.text)) {
    // TODO <div> logic can go in getPageHTML
  //      var html = '<div class="zitate" id="page-' + args.request.page_id + '-' +  args.request.page_num + '">' + getPageHTML(args.text, args.request.page_id, args.request.page_num) + '</div>';
        var html = getPageHTML(args.text, args.request.page_id, args.request.page_num);

        // find the first instance of the "book-page" class and append to that:
        $(".book-page:first").before(html)
        if ($(".book-page:first").height() >  pageHeight && $(".book-page:first").height() < MAX_PAGE_HEIGHT){
            pageHeight = $(".book-page:first").height();
        }
        $(".page-image").height( pageHeight);

    }
    page.previous -= 1;
    setPageNavigation(args.request.page_id);
    initAnnotationLogic(args.request.page_id, args.request.page_num);

}


var initAnnotationLogic = function(pageID, pageNum){

    var corpus = getCookie("corpus");
    if (!isLoggedIn() || corpus == "")
        return;

    var userName = getCookie("username");
    var userToken = getCookie("token");
    var userID = getCookie("userid");

    var corpusID = getCorpusID(corpus);
    // resource has to match the Internet Archive format so we are consistent across the system
    var resource = pageID;
    if (parseInt(pageNum) >= 0){

        resource +=  '_' +  pageNum;
    }

    var el = '#' + getNotesID(pageID, pageNum);

    // ??? only do this IFF we have a noteid?
    // ??? this is nice for a book, but what about if it's for a page?
    $(el).bind("annotationsLoaded", function(){
        var urlParams = getURLParams();
        // check if we are passed a note id
        if (!_.isUndefined(urlParams["noteid"])){

            var el =  '.annotator-hl[data-annotation-id="' + urlParams["noteid"] + '"]'

            $('#results-right').animate({
                scrollTop: $(el).offset().top - 60
            }, 2000);

            // remove the notid from the URL so we don't re-trigger
           removeURLParam("noteid");

            // unbind once we're done
            $(el).unbind("annotationsLoaded");
        }
    });

    $(el).annotator().data('annotator');

        $(el).annotator('addPlugin', 'Store', {

           annotationData: {
                uri : resource,
                userid: parseInt(userID),
                user:  userName,
                token: userToken,
                corpus: parseInt(corpusID),
                corpusName: corpus
            },
            loadFromSearch: {
                 'uri': resource,
                corpus:  parseInt(corpusID)

            },
            urls: {
                // These are the default URLs.
                create:  '/annotations/ins',
                update:  '/annotations/upd/:id',
                destroy: '/annotations/del/:id',
                search:  '/annotations/search'
            }
        });

    $(el).annotator('addPlugin', 'Permissions', {
        user: {
            id: parseInt(userID),
            name:  userName
        },
        showViewPermissionsCheckbox: false,
        showEditPermissionsCheckbox: false,
        userId: function (user) {
            if (user && user.id) {
                return parseInt(user.id);
            }
            return user;
        },
        userString: function (user) {
            if (user && user.name) {
                return user.name;
            }
            return user;
        },
        userAuthorize: function(action, annotation, user) {
            // MCZ: for some reason you can delete a post even if you're not
            // allowed to edit it... doesn't make much sense to me. Making it
            // (for now) that only creator can edit/delete.
            return this.userId(user) === this.userId(annotation.userid);
            }
    });

    // this can only be done AFTER all the notes are highlighted
//    var urlParams = getURLParams();
//    // check if we are passed a note id
//    if (!_.isUndefined(urlParams["noteid"])){
//
//        var el =  '.annotator-hl[data-annotation-id="' + urlParams["noteid"] + '"]'
//        $('#results-right').animate({
//            scrollTop: $(el).offset().top
//        }, 2000);
//    }


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

        var html = getPageHTML(args.text, args.request.page_id, args.request.page_num);

        // find the last instance of the "book-page" class and append to that:
        $(".book-page:last").after(html)
        if ($(".book-page:last").height() >  pageHeight && $(".book-page:last").height() < MAX_PAGE_HEIGHT ){
            pageHeight = $(".book-page:last").height();
        }
        $(".page-image").height( pageHeight);
   }
    page.next += 1;
    setPageNavigation(args.request.page_id);

    initAnnotationLogic(args.request.page_id, args.request.page_num);

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
    var userToken = getCookie("token");

    API.action({kind: "ia-pages", id: id , action: "view", token: userToken, page_id: pageID, page_num: page.previous}, viewPrevPageSuccess, function(req, status, err) {
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
    var userToken = getCookie("token");
    API.action({kind: "ia-pages", id: id , action: "view", token: userToken, page_id: pageID, page_num:  page.next}, viewNextPageSuccess, function(req, status, err) {
        UI.showError("ERROR: ``" + err + "``");
        throw err;
    });

    UI.showProgress("");
};

/** this gets called with the response from ViewResource */
var onViewPageSuccess = function(args) {

    var pageID = "";
    UI.clearError();
    resultsDiv.hide();
    metadataDiv.hide();

    pos = args.request.id.lastIndexOf('_');
    page.current = parseInt(args.request.id.substr(pos+1));
    page.previous = page.current-1;
    page.next = page.current+1;
    if (!_.isUndefined(args.metadata) && !_.isUndefined(args.metadata.imagecount)) {
        page.max = args.metadata.imagecount;
    }
    pageID = args.request.id.slice(0, pos);


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
    //    html += '<a class="show-hide-metadata" onclick="UI.showHideMetadata();">Show Metadata</a>'
    //
    //    html += '<div>[<span class="per">PERSON</span>]&nbsp;[<span class="loc">LOCATION</span>]&nbsp;[<span class="org">ORGANIZATION</span>]</div>';
    //    if (args.request.kind == 'ia-pages'){
    //        html += '<div class="pageNavigation"></div>';
    //        html += '<div id="prevPage"></div>';
    //    }

    var identifier = args.request.id.split('_')[0];
    var pageNum = args.request.id.split('_')[1];

    var html = getPageHTML(args.text, identifier, pageNum);

    $("#book-pages").html(html);
    // base the page size on the first page
    pageHeight = Math.max(MIN_PAGE_HEIGHT, $(".book-text").height());
    $(".page-image").height( pageHeight);

    //  viewResourceDiv.html(html);
    setPageNavigation(pageID);
    viewResourceDiv.show();
    initAnnotationLogic(identifier, pageNum);
    UI.showProgress("");

};

var onViewBookSuccess = function(args) {

    UI.clearError();
    resultsDiv.hide();
    metadataDiv.hide();

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

    var pgTxt= processTags(args.text);

    var html =  '<a href="#" class="show-hide-metadata" onclick="UI.showHideMetadata();">Show Metadata</a>';
    html +=  pgTxt ;

    viewResourceDiv.html(html);

    // go through the book and insert an ID attribute so we can share the notes across
    // both books and pages.
    var pageBreaks = $(".page-break");
    var id = args.request.id;

    _.forEach(pageBreaks, function(pb){

        var pgImage = pageImage(id, $(pb).attr("page"));
        var el =  getNotesID(id, $(pb).attr("page"));
        $(pb).addClass("book-page row clearfix ");

        var pghtml =  '<div id="' + el + '" class="book-text col-md-5 column left-align">' + $(pb).html() + '</div>'+
                '<div id="' + el + '-page-image" class="page-image col-md-5 column left-align"><br><a href="#" onclick="getPageImage(\'' + el + '-page-image\',\'' +  pgImage + '\');" >View the actual page</a></div>';
        $(pb).html(pghtml);
        initAnnotationLogic(id, $(pb).attr("page"));
    });

    viewResourceDiv.show();
    UI.showProgress("");

};

var getPageImage = function(id, imgURL){

    $('#' + id).html('<br><a class="fancybox" href="' + imgURL + '" ><img src="' + imgURL + '"></a>')

}

var setPageNavigation = function(pageID) {

    var prevHTML = '<button style="width: 100%;"  onclick="doPrevPageRequest(\'' + pageID + '\',' + (page.previous) + ');" >&#9650;&nbsp;Previous Page&nbsp;&#9650;</button>';
    var nextHTML = '<button style="width: 100%;" onclick="doNextPageRequest(\'' + pageID + '\',' + (page.next) + ');">&#9660;&nbsp;Next Page&nbsp;&#9660;</button>';

    if ((page.max != -1 && page.next > page.max) || page.skips > page.MAX_SKIPS){
        nextHTML = '';
    }

    if (page.previous < 0){
        prevHTML = '';
    }

    $("#view-nav-top").html(prevHTML)
    $("#view-nav-bottom").html(nextHTML)

}

 var processTags = function(text){
     if (document.getElementById("cb-per").checked)
         text = text.replace(perStartRegEx, "<span class=\"per\">");
     else
         text = text.replace(perStartRegEx, "<span class=\"per-off\">");

     if (document.getElementById("cb-loc").checked)
         text = text.replace(locStartRegEx, "<span class=\"loc\">");
     else
         text = text.replace(locStartRegEx, "<span class=\"loc-off\">");

     if (document.getElementById("cb-org").checked)
         text= text.replace(orgStartRegEx, "<span class=\"org\">");
     else
         text= text.replace(orgStartRegEx, "<span class=\"org-off\">");

     text = text.replace(endRegEx, "</span>");

     return text;
 }

function submitCorpusDialog() {
    // make sure we have a name
    var corpusName = $("#corpus-name").val().trim();
    if (corpusName.trim() == "") {

        $("#corpusError").addClass("in")
        $("#corpusError").html("Corpus name cannot be blank.")
        setTimeout(function(){
            $('#corpusError').removeClass("in");
            $('#corpusError').addClass("out");

        }, 10000);

        return;
    }
    // TODO: make sure it's unique - don't need to look at the list, these should
    // all be held in a JSON object
    //                if ($('#corpus-list li:contains("' + corpusName + '")').length) {
    //                    alert("non-unique");
    //                    return;
    //                }

    createNewCorpus(corpusName);

    $('#newCorpusDialog').modal('hide');
}
