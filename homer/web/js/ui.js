/**
 * ui.js
 *
 * This file roughly contains all the interfacing with the index.html boilerplate, using jquery as needed.
 *
 */

var errorDiv = $("#error");
var resultsDiv = $("#results");
var metadataDiv = $("#metadata");
var viewResourceDiv = $("#view-resource");
var progressDiv = $("#progress");
var queryBox = $("#ui-search");
var loginInfo = $("#ui-login-info");
var searchButtons = $("#search-buttons");
var relevanceLabels = ["terrible", "not relevant", "neutral", "slightly relevant", "highly relevant"];
var relevanceLabelColorClasses = ["rel-label-terrible", "rel-label-not-relevant", "rel-label-neutral", "rel-label-slightly-relevant", "rel-label-highly-relevant"];

var ratingsJSON = {"document" : []};

// UI object/namespace
var UI = {};
UI.generateButtons = function() {

    API.getKinds({}, function(data) {
        if (data.title)
            $("#proteus-title").html(data.title);
        UI.defaultKind = data.defaultKind;
        var availableKinds = _(data.kinds);
        var buttonDescriptions = _(UI.buttons);
        // for all kinds the server gives back, make a button
        _.forIn(data.kinds, function(spec, kind) {
            spec.kind = kind; // so the onClick knows what kind it was
            if (!spec.button) {
                // UI.showError("You need to specify a \"button\" display attribute for kind \"" + kind + "\"");
                // the "view" action requires that we have a kind/index specified, but we may not
                // want search buttons for them so rather than display a warning just write it to the console.
                console.log("You didn't specify a \"button\" display attribute for kind \"" + kind + "\"");
                return;
            }

            $("#search-button-choices").append('<li><a href="#" onclick="UI.onClickSearchButton(\'' + spec.kind + '\');">' + spec.button + '</a></li>');

            // see if we're the default button
            if (kind === UI.defaultKind) {
              $("#search-buttons").click(function() {
                UI.onClickSearchButton(spec.kind);
              });
            }

        });
        // add an option to search rated documents
        $("#search-button-choices").append('<li class="divider"></li><li><a href="#" onclick="UI.onClickSearchButton(\'rated-only\');">Rated Documents</a></li>');

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
 * I'm adding comments now since i regret it
 * when I dont
 * deleted UI.makeResult since its useless.
 
 */

// added labels to our button bar when they add a new one

function addLabelToButtons(newLabel) {

    // get the two parts of the label, remove the rating
    var tmp = newLabel.split(":");
    var type = tmp[0];
    var value = tmp[1];
    // handle the special case of when they don't enter a TYPE
    // IF there is no type, add it to the "special" group

    if (_.isUndefined(value)) {
        value = type;
        type = NO_TYPE_CONST();
    }
    var userID = getCookie("userid");
    var tree = $("#tree").fancytree("getTree");
    var rootNode = $("#tree").fancytree("getRootNode");

    newLabel = GLOBAL.users[userID] + TREE_KEY_SEP() + type + ":" + value;
    // make sure we don't already have this
    var check = tree.getNodeByKey(newLabel);
    if (check !== null)
        return;

    // see if the current user is in the tree

    var userNode = tree.getNodeByKey(GLOBAL.users[userID]);
    if (userNode == null){
        rootNode.addChildren({
            key: GLOBAL.users[userID],
            title: GLOBAL.users[userID],
            folder: true
        });
        userNode = tree.getNodeByKey(GLOBAL.users[userID]);

    }

     // new labels:

    // search for the parent to attach it to
    var node = tree.getNodeByKey(GLOBAL.users[userID] + TREE_KEY_SEP() + type);

    if (node !== null) {

        node.addChildren({
            title: value, key: newLabel
        });
        tree.render();
    } else {
        // get the root node
        var root = userNode.getFirstChild();

        // special logic for the first label
        if (_.isUndefined(root)) {
            getAllTagsByUser();
            return;
        }
        //add the parent & child
        var newNode = userNode.addChildren({
            title: type, key: GLOBAL.users[userID] + TREE_KEY_SEP() + type, folder: true
        });
        newNode.addChildren({
            title: value, key: newLabel
        });
        userNode.setExpanded(true);
        newNode.setExpanded(true);
        tree.render();
    }
}

function getAve(ave, id){
    var pct = (ave/4)*100;

   var aveRating = '<span class="ui-state-hover ui-slider-handle ui-slider-ave ui-slider-horizontal proteus-rating " style="left: ' +  pct.toFixed(0) + '%; "><span class=" ui-slider-tip">' + (ave-2).toFixed(2) + '</span></span>';
   $("#rating-" + id + " span:first").before(aveRating);

}

function setSliderValue(name, init) {

    var tot = 0;
    var cnt = 0;

    ratingsJSON.document[name].forEach(function (rec) {
        tot += rec.rating;
        cnt += 1;
    })

    if (cnt == 0){
        return;
    }
    var ave = tot / cnt;

    // remove old (if it's there - we use the "proteus-rating" class so we're sure)
    $("#rating-" + name + "  .proteus-rating ").remove();

    getAve(ave, name);

    return ave-2;

};

/**
 * Renders search results into UI after current results
 */

UI.renderSingleResult = function(result, queryTerms,  prependTo) {
    //console.debug("result name: " + result.name);
    var renderer = getResultRenderer(result.viewKind); //added this line and 5 below make adding/subt elements in future easier
    var resDiv = $('<div>');
    resDiv.attr('class', 'result');
    resDiv.attr('id', result.name);

    // put it at the end unless we pass in where we want it to go
    if (_.isUndefined(prependTo)) {
        resultsDiv.append(renderer(queryTerms, result, resDiv)); //* 6/26/2014
    } else {
        $(prependTo).after(renderer(queryTerms, result, resDiv));
    }

     $("#rating-" + result.name)
            .slider({
                 max: 2,
                min: -2
            })
            .slider("pips", {
                rest: "label",
                labels: relevanceLabels
            })
            .slider("float").slider({

        change: function( event, ui ) {

            // send this rating to the DB

            // TODO: we do this a lot - should have it's own function
            var userName = getCookie("username");
            var userID = parseInt(getCookie("userid"));
            var userToken = getCookie("token");
            var corpus = getCookie("corpus");
            var corpID = getCorpusID(corpus);

            var args = { userid:  userID, user: userName, token : userToken, resource: result.name, corpus: corpID, rating: ui.value };

            API.rateResource(args, function(){

                if (_.isUndefined(ratingsJSON.document[result.name])) {
                    ratingsJSON.document[result.name] = [];
                    // add our rating
                    ratingsJSON.document[result.name].push({"user": userName, "rating": ui.value + 2}); // +2 hack to keep it consistent with other ratings
                }
                    // see if we have a value
                    var idx =  _.findIndex(ratingsJSON.document[result.name], function(rec) {
                        return rec.user == userName;
                    });

                  // update our rating - or remove it if it's zero
                    if (idx < 0){
                        ratingsJSON.document[result.name].push({ "user" : userName, "rating" : ui.value + 2});
                    }else{
                        ratingsJSON.document[result.name][idx].rating = ui.value + 2;
                    }

                setSliderValue(result.name, false);
                renderRatingsSidebar(result.name);
            }, function(req, status, err) {
                UI.showError("ERROR: ``" + err + "``");
                throw err;
            });

        }

    }) ;

    var myVal = setSliderValue(result.name, true);
    $("#rating-" + result.name).slider("values", myVal);



    var tagName = "#tags_" + result.name;
    $(tagName).tagit({
        availableTags: GLOBAL.uniqTypes,
        autocomplete: {delay: 0, minLength: 0},
        allowSpaces: true,
        placeholderText: "Add a Label",
        afterTagRemoved: function(event, ui) {

            deleteTag(ui.tagLabel, result.name);
            // update the buttons
            //deleteLabelFromButtons(ui.tagLabel);

            return true;
        },
        beforeTagAdded: function(event, ui) {
            var that = this;
            if (!ui.duringInitialization) {
                // only ask "are you sure" if this is a NEW tag TYPE
                tmp = ui.tagLabel.split(":");
                var res = true;
                if (tmp.length === 2 && $.inArray(tmp[0], GLOBAL.uniqTypes) === -1) {
                    res = confirm("Are you sure you want to create the label type \"" + tmp[0] + "\"?");
                    if (res == false)
                        return false;
                    // add the new type to our list
                    GLOBAL.uniqTypes.push(tmp[0]);
                }

            } else {
                return true;
            }
        }
    });
    $(".read-only-tags").tagit({
        readOnly: true
    });

};

UI.appendResults = function(queryTerms, results) {

    UI.showProgress("Ajax response received!");
    _(results).forEach(function(result) {
        UI.renderSingleResult(result, queryTerms);
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

UI.dispalyUserName = function() {
    // if it's an email address, just display the first part
    var user = getCookie("username").split("@")[0];

    if (user) {
        $("#ui-login-form").hide();
        $("#user-info").html("<span class='login-form-text'> Welcome " + user + "</span> <input class='btn btn-sm'  id='ui-go-logout' type='button' value='LogOut' />").show();

        $("#ui-go-logout").click(function() {
            $("#user-info").hide();
            logOut();
            $("#ui-login-form").show();
        });
    }
};

UI.renderTags = function(result) {
    // don't show tags if they're not logged in
    if (!isLoggedIn()) {
        return "<div></div>";
    }
    var my_html = '';
    var ro_html = ''; // read only tags

    var labelRatings = {};
    var labelScore = {};
    // TODO has to be a more efficient way of doing this than looping through everything twice
    // get the rating each user gave to a label
    for (var user in result.tags) {

        tags = result.tags[user];
        for (tag in tags) {

            if (tag in labelRatings) {
                labelRatings[tag] += " " + tags[tag].split(":")[0];
            } else {
                labelRatings[tag] = tags[tag].split(":")[0];
            }
        }
    }
    // keep track of labels that more than one person has rated - they'll be read-only
    var roLabels = new Set();

    // now calc the score
    for (label in labelRatings) {
        var scores = labelRatings[label].split(" ");
        var sum = _.reduce(scores, function(a, b) {
            return parseInt(a) + parseInt(b);
        });
        labelScore[label] = sum / scores.length;
        if (scores.length > 1)
            roLabels.add(label);
    }

    // there now can be duplicates because others can rate labels they didn't
    // create, so they'll be returned per user. So we'll keep a set of the
    // labels we've already displayed so we can quickly check.
    var doneLabels = new Set();

    // the only labels we allow you to delete have to be created by you and no one else
    // has rated them.
    var userid = getCookie("userid");

    for (var user in result.tags) {

        tags = result.tags[user];
        for (tag in tags) {
            if (doneLabels.has(tag)) {
                continue;
            }
            doneLabels.add(tag);
            // if we have a decmial, only show 2 place
            var score = labelScore[tag];
            if (score.toString().indexOf(".") != -1) {
                score = score.toFixed(2);
            }
            if (user !== userid || roLabels.has(tag)) {
                ro_html += '  <li class="tagit-choice-read-only"> ' + formatLabelForDispaly(tag, score) + ' </li> ';
            } else {
                my_html += '  <li> ' + formatLabelForDispaly(tag, score) + ' </li> ';
            }
        }
    }

    return '<div><ul rank="' + result.rank + '" id="tags_' + result.name + '">' + ro_html + my_html + '</ul>' + '</div>';

};

UI.toggleMyTags = function() {
    // if no one is logged in, we don't show anything
    if (getCookie("username") === "") {
        return;
    }
    var ele = $("#my-tags-container");
    if (ele.is(":visible")) {
        $("#toggle-my-tags-img").attr("src", "/images/down_arrows.png");
        ele.hide();
    } else {
        $("#toggle-my-tags-img").attr("src", "/images/up_arrows.png");
        ele.show();
    }
    $("#toggle-my-tags-img").show();
};
// param is array of uniq types
UI.createLabelMultiselect = function(userID) {

    $("#empty-tree").html("");

    var node0 = $("#tree").fancytree("getRootNode");

    var rootNode = node0.addChildren({
        key: GLOBAL.users[userID],
        title: GLOBAL.users[userID],
        folder: true
    });

    var tags = GLOBAL.allTags[userID];
    var lastType = null;
    for (tag in tags) {
        //console.log("Tag: " + tags[tag]);
        var kv = tag.split(":");
        var key = GLOBAL.users[userID] + TREE_KEY_SEP() + kv[0];
        var val = kv[1];
        var childNode;
        if (lastType === null || key != lastType) {
            childNode = rootNode.addChildren({
                title: kv[0], key: key, folder: true
            });
            lastType = key;
        }
        childNode.addChildren({
            title: val, key: key + ":" + val
        });
    }

    $("#tree").fancytree("getRootNode").visit(function(node) {
        node.setExpanded(true);
    });

};
UI.clearSelectedLabels = function() {
    var i = 0; // counter to get uniq elements

    $(".proteus-labels").each(function() {

        $('option', $('#multiselect-' + i)).each(function(element) {
            $('#multiselect-' + i).multiselect('deselect', $(this).val());
        });
        $("#multiselect-" + i).multiselect('refresh'); // this clears the "select all" option
        i += 1;
    });
};

UI.showHideMetadata = function(){
    if (metadataDiv.is(':visible') == true){
        metadataDiv.hide();
        $(".show-hide-metadata").html("Show metadata");
    } else {
        metadataDiv.show();
        $(".show-hide-metadata").html("Hide metadata");
    }

}

var init = true;

function renderRatingsSidebar(id){
    var html = "<div>";
    var aveRating = 0;
    var cnt = 0;

    if (!_.isUndefined( ratingsJSON.document[id])) {

        _.forEach(ratingsJSON.document[id], function (rec) {

            // ignore any zero ratings
            if (rec.rating -2 != 0){
                cnt += 1;
                html += '<span class="rating-user">' + rec.user.split("@")[0] + ':&nbsp;</span><span class="rating-value rel-label ' + relevanceLabelColorClasses[rec.rating] + '">' + relevanceLabels[rec.rating] + ' (' + (rec.rating - 2) + ')</span><br>';
                aveRating += rec.rating - 2;
            }
        })
    }

    if (cnt == 0){
        html = '<span >No ratings yet for this document</span><br>' + html;
    } else {
        html = '<span class="rating-user">Ave Rating:&nbsp;</span><span class="rating-value">' + (aveRating/cnt).toFixed(2) + '</span><br>' + html;
    }

    $("#ratings").html(html + "</div>" );

}
function setUpMouseEvents(){
 
        $(".result").mouseenter(function(  event) {

            renderRatingsSidebar(this.id);
            $(this).css("background", "lightyellow");

        });

        $(".result").mouseleave(function(  event) {

        $(this).css("background", "");

        });

}

UI.populateRatedDocuments = function(){

    _.forEach(GLOBAL.ratedDocuments, function(rec){
        $("#ratedDocuments").prepend( '<div class="query">&#8226;&nbsp;<a  onclick="">'
            + rec.doc
            + '</a><div>'
            + rec.aveRating
            + '</div>' );

    });

}


function bindCorpusMenuClick() {

    $("ul#corpus-list.dropdown-menu li a").off("click");
    $("ul#corpus-list.dropdown-menu li a").on("click", function () {

        console.log("clicked: " + $(this).text())
        if ($(this).text() == "New...") {
            // clear out any old value
            $("#corpus-name").val("");
            $('#newCorpusDialog .alert-danger').removeClass("in");
            $('#newCorpusDialog .alert-danger').addClass("out");
            $("#newCorpusDialog").modal();

            return;
        }

        setCorpus($(this).text());
        UI.clearResults();
    });
}
function setCorpus(corpus) {
    $("#active-corpus").find('.selection').text(corpus);
    $("#active-corpus").find('.selection').val(corpus);
    document.cookie = "corpus=" + corpus + ";";
}


function hideSideBar(){
    $('#sidebar-button').attr("src", "images/sidebar_expand.png");
    $("#results-left").hide();
    $("#results-right").removeClass("col-md-10");
    $("#results-right").addClass("col-md-12");
    showSideBarFlag = false;
    p = getURLParams();
    p = _.merge(p, {'showSideBar' : '0'});
    pushURLParams(p);
}
function showSideBar(){
    $('#sidebar-button').attr("src", "images/sidebar_shrink.png");
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


UI.appendToCorpusList = function(corpusName){
    $("#corpus-list-divider").before('<li><a href="#">' + corpusName + '</a></li>');
}

UI.updateCorpusListButton = function(){

    if (localStorage["corpora"] == null)
        return;

    var corpora = JSON.parse(localStorage["corpora"]);
    _.forEach(corpora, function(c){
        UI.appendToCorpusList(c.name);
    });

}
