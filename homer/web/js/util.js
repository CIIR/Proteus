/**
 * util.js
 *
 * For Javascript shims and other things that don't have a clear project-specific home.
 */

if (!String.prototype.trim) {
    String.prototype.trim = function() {
        return this.replace(/^\s+|\s+$/g, '');
    };
}

var isBlank = function(str) {
    return _.isUndefined(str) || _.isEmpty(str.trim());
};

var pushURLParams = function(params) {
    var urlParams = "?" + _(params).map(function(vals, key) {
        //console.log(key + ":" + vals);
        // some values - like labels - can have multiple comma 
        // separated value. If these are passed like "labels=a,b,c"
        // they get interpreted as one value. So if there are multiples
        // we'll pass multiple key/value pairs like: labels=a&labels=b&labels=c
        return  _.map(vals.toString().split(","), function(val) {
            //console.log("          " + key + ":" + val);
            return encodeURIComponent(key) + "=" + encodeURIComponent(val);
        }).join('&');
    }).join('&');

    // if there are labels AND we don't have a "labelOwner" param, add the user that owns them
    if (!_.isUndefined(params.labels) && urlParams.indexOf("labelOwner") == -1) {
        // MCZ 3/2015 - quick hack so we can retrieval by ANYONE'S labels
        urlParams += "&labelOwner=-1"; //  + getCookie("userid");
    }
    History.pushState(null, null, urlParams);

};

var getURLParams = function() {
    var match,
            pl = /\+/g, // Regex for replacing addition symbol with a space
            search = /([^&=]+)=?([^&]*)/g,
            decode = function(s) {
                return decodeURIComponent(s.replace(pl, " "));
            },
            query = window.location.search.substring(1);

    var urlParams = {};
    while ((match = search.exec(query))) {
        var key = decode(match[1]);
        var value = decode(match[2]);
        if (value === "null") {
            value = null;
        } else if (value === "true") {
            value = true;
        } else if (value === "false") {
            value = false;
        }
        // it's possible there are multiple values for things such as labels
        if (_.isUndefined(urlParams[key])) {
            urlParams[key] = value;
        } else {
            // urlParams[key] += "&" + key + "=" + value;
            urlParams[key] += "," + value;
        }
    }
    return urlParams;
};

var removeURLParam = function(param) {

    var p = getURLParams();

    if (!_.isUndefined(p[param])) {
        delete p[param]
        pushURLParams(p);
    }

}

// from: http://www.w3schools.com/js/js_cookies.asp
var getCookie = function(cname) {
    // 10/2015 MCZ moving towards a tagging model so if they ask for
    // the corpus, always return "default"
    if (cname == "corpus") {
        return "default";
    }
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++)
    {
        var c = ca[i].trim();
        if (c.indexOf(name) === 0)
            return c.substring(name.length, c.length);
    }
    return "";
};

var getUser = function() {
    return getCookie("username")
}
var highlightText = function(queryTerms, text, beforeTag, afterTag, stripPunctuation) {

    if (_.isUndefined(stripPunctuation)) {
        stripPunctuation = true;
    }
    // there are situations where the "text" is actually an image such
    // as a thumbnail so skip those.
    if (text.toString().substring(0, 4) == "<img")
        return text;

    // remove any punctuation. For papers the authors are listed
    // spearated by semicolons.
    // Note we use toString(), because if a string like "2014" is passed
    // in, JS will think it's a numeric datatype.
    if (stripPunctuation) {
        text = text.toString().replace(/[\.,-\/#!$%\^&\*;:{}=\-_`~()]/g, "");
    }

    var words = text.split(/\s/);

    return _(words).map(function(word) {
        if (_.contains(queryTerms, word.toLowerCase())) {
            return beforeTag + word + afterTag;
        }
        return word;
    }).join(' ');
};

function getSelectedLabels() {

    var labels = [];
    var tree = $("#tree").fancytree("getTree");
    var selNodes = tree.getSelectedNodes();

    selNodes.forEach(function(node) {

        if (!node.hasChildren()) {// ONLY count leaf nodes
            // strip out the user's email part of the key
            key = node.key.split(TREE_KEY_SEP())[1];
            labels.push(key);
        }
    });

    console.log("LABELS: " + labels);

    return labels;

}

// users have the ability to just type a value w/o a type,
// in order to let them choose these, we need to "group" them
// somehow, that's done with this "special" type.
function NO_TYPE_CONST() {
    return "*";
}

function TREE_KEY_SEP() {
    return "\t";
}

// users are allowed to enter labels w/o at TYPE, this is troublesome later when
// we want to allow them to select type/values to filter/retrieval by/etc. So we'll store
// them with a special "*" type, and as long as we don't display that, all the existing
// code works nicely.
// Also remove the rating
function formatLabelForDatabase(origLabel) {

    var labelWithoutRating = origLabel;

    // if we have a rating, remove it
    var idx = origLabel.lastIndexOf(" (");
    if (idx > 0) {
        labelWithoutRating = origLabel.substring(0, idx).trim();
    }
    if (labelWithoutRating.indexOf(":") === -1) {
        return NO_TYPE_CONST() + ":" + labelWithoutRating;
    }

    return labelWithoutRating;
}
// when displaying tags, don't show the "*:" and display the
// rating in a user friendly way
function formatLabelForDispaly(origLabel, rating) {
    var _rating = rating.toString().split(":");
    if (_.isUndefined(_rating[0])) {
        _rating = rating; // they didn't pass in colon sep values
    }
    if (origLabel.substring(0, 2) === NO_TYPE_CONST() + ":") {
        var label = origLabel.split(":");
        return  label[1] + " (" + _rating + ")";
    }

    return  origLabel + " (" + _rating + ")";

}
function isLoggedIn() {
    return (getCookie("username") !== "");
}


function enableAutoRetrieve() {
// TODO this needs to be for the "searched" kind - can't default to books
    $('#results-right').bind('scroll', function() {

        if ($(this).scrollTop() + $(this).innerHeight() >= this.scrollHeight) {

            var prev = Model[gSearchedKind].request;
            prev.skip = Model[gSearchedKind].results.length;
            doSearchRequest(prev);

        }
    });
}

function disableAutoRetrieve() {
    $('#results-right').unbind('scroll');
}

// mimic Google's URL redirect
rwt = function(a, rank) {
    try {
        var origURL = escape(a.href);
        var token = "&token=" + getCookie("token");
        a.href = "/url?url=" + origURL + token + "&rank=" + rank;
        a.onmousedown = ""
    } catch (o) {
    }
    return 1
};


function getCorpusID(corpusName) {
    // 10/2015 MCZ moving towards a tagging model so this will always
    // be "1" - the "umbrella" corpus
    return 1;
//    // TODO: has to be a better way to do this - just brute force it for now...
//    var corpora = JSON.parse(localStorage["corpora"]);
//    var corpID = -1;
//    _.forEach(corpora, function(c){
//        if (c.name == corpusName)
//            corpID = parseInt(c.id);
//    });
//    return corpID;
}

jsonp_handler = function(data)
{
    console.log(data);
    var html = $("#important-entities").html();
    var wikipedia = '';


    _.forEach(data.query.pages, function(pg) {
        if (!_.isUndefined(pg.pageid)) {
            wikipedia += ' <a target="_blank" href="https://en.wikipedia.org/wiki/' + pg.title + '">' + pg.title + '</a>';
        }

    });

    if (wikipedia.length > 0)
        $("#important-entities").html(html + " (" + wikipedia + ")");

}

function initImportantEntities() {
    $("#important-entities").droppable({
        drop: function(event, ui) {
            $(this).css("background-color", "");
            var html = $("#important-entities").html();
            console.log(ui)
            //    $("#important-entities").html(html + "<br>" + ui.draggable[0].parentElement.nodeName + ": " + ui.draggable[0].textContent);
            $("#important-entities").html(html + "<br>" + ui.draggable[0].parentElement.nodeName + ": " + ui.draggable[0].outerHTML);
            // remove any classes
            $("#important-entities .mz-ner").removeClass();
//
//            API.callWikipedia(ui.draggable[0].textContent, function(data){
//                console.log(JSON.stringify(data));
//            }, function(){
//                console.log("Failure");
//            })


            // the text we're taking the NER from is all lower case. The Wikipedia API will only capitalize the first
            // word so we'll pass the original version and one with the first letter capitalized.

            var query = encodeURI(ui.draggable[0].textContent) + "|" + encodeURI(ui.draggable[0].textContent.capitalizeEachWord());
            var script = document.createElement('script');
            script.src = 'http://en.wikipedia.org/w/api.php?action=query&callback=jsonp_handler&format=json&titles=' + query;

            // script.src = 'http://en.wikipedia.org/w/api.php?action=query&callback=jsonp_handler&prop=images&imlimit=20&format=json&titles=' + encodeURI(ui.draggable[0].textContent);
            //      script.src = 'http://en.wikipedia.org/w/api.php?action=query&callback=jsonp_handler&list=retrieval&limit=20&format=json&srsearch=' + encodeURI(ui.draggable[0].textContent);

            document.head.appendChild(script);


        },
        over: function() {
            $(this).css("background-color", "lightgrey");
        },
        out: function() {
            $(this).css("background-color", "");
        }

    });
}

String.prototype.capitalizeEachWord = function() {
    return this.replace(/(?:^|\s)\S/g, function(a) {
        return a.toUpperCase();
    });
};

function getNotesID(pageid, pagenum) {
    return 'notes-' + getBookID(pageid, pagenum);
}
function getBookID(pageid, pagenum) {
    if (!_.isUndefined(pagenum) && parseInt(pagenum) >= 0)
        return pageid + '_' + pagenum;
    else
        return pageid;
}
// temp func - using "resource rating" to track swipe left/right

var recordSwipe = function(res, kind, swipeVal) {

    // send this rating to the DB

    var userName = getCookie("username");
    var userID = parseInt(getCookie("userid"));
    var userToken = getCookie("token");
    var corpus = getCookie("corpus");
    var corpID = getCorpusID(corpus);
    var queryID = 0;

    if (!_.isUndefined(Model[kind])) {
        queryID = Model[kind].queryid;
    }

    var args = {
        userid: userID,
        user: userName,
        token: userToken,
        resource: res,
        corpus: corpID,
        corpusName: corpus,
        rating: parseInt(swipeVal),
        kind: kind,
        queryid: queryID
    };

    // update the local rating
    if (_.isUndefined(ratingsJSON.document[res])) {
        ratingsJSON.document[res] = {};
    }
    ratingsJSON.document[res][userName] = swipeVal;

    API.rateResource(args, function() {
        setUserRatingsHTML(res);
        setVoteHTML(res);
        renderRatingsSidebar(res);
    }, function(req, status, err) {
        UI.showError("ERROR: ``" + err + "``");
        throw err;
    });

}

var setUserRatingsHTML = function(res) {

    if (_.isUndefined(votingJSON.document[res]) || _.isEmpty(votingJSON.document[res])) {
        return '';
    }

    // TODO - fix this - completely brain dead way of building a map from subcorpus ID to
    // subcorpus name  - should be a gobal built once
    var id2name = {};

    var recs = JSON.parse(localStorage["subcorpora"]);
    _.each(recs, function(r) {
        id2name[r.id] = r.name;
    });


//    var rating_wo_names_html =   '<span>';

    // use a map to group who "voted" for a subcorpus
    var votes = {};

    _.forEach(votingJSON.document[res], function(val, key) {

        var user = key.split("@")[0];

        _.forEach(val, function(v, k) {
            if (_.isUndefined(votes[id2name[k]])) {
                votes[id2name[k]] = user;
            } else {
                votes[id2name[k]] += ', ' + user;
            }
        })

    })
    var rating_html = '<span>';
    _.forEach(votes, function(val, key) {
        rating_html += key + ': ' + val + '<br>';
    })

    rating_html += '</span>';

    //  rating_wo_names_html += '</span>';

    //   $('#' + res + '-user-ratings').html(rating_wo_names_html);
    $('#' + res + '-user-ratings-w-names').html(rating_html);

    //    if (_.isUndefined( ratingsJSON.document[res]) || _.isEmpty(ratingsJSON.document[res])) {
//        return '';
//    }
//
//    var rating_html =   '<span>';
//    var rating_wo_names_html =   '<span>';
//
//    _.forEach(ratingsJSON.document[res], function (val, key) {
//
//        // ignore any zero ratings
//        if (val  != 0){
//            var user  = key.split("@")[0] ;
//
//            if (val == 1){
//                rating_html += user + ' <span class="glyphicon glyphicon-ok"></span><br>'
//                rating_wo_names_html +=   ' <span class="glyphicon glyphicon-ok"></span> '
//            } else {
//                rating_html += user + ' <span class="glyphicon glyphicon-remove"></span><br>'
//                rating_wo_names_html +=  ' <span class="glyphicon glyphicon-remove"></span> '
//            }
//        }
//    })
//
//    rating_html += '</span>';
//    rating_wo_names_html += '</span>';
//
//    $('#' + res + '-user-ratings').html(rating_wo_names_html);
//    $('#' + res + '-user-ratings-w-names').html(rating_html);

}

var setVoteHTML = function(res) {

//    var myRating = 0;
//
//    if (!_.isUndefined( ratingsJSON.document[res])) {
//        myRating = ratingsJSON.document[res][getCookie("username").toLowerCase()];
//    }
//
//    // see if we've rated this
//    if (_.isUndefined(myRating)){
//        myRating = 0;
//    }
//
//    var vote_html =   '<div id="' + res + '-voting">';
//
//    var func = "recordSwipe('" + res + "', $('#" + res + "').data('kind'),"
//    vote_html += '<span id="' + res + '-accept-button" onclick="' + func + ' 1);" class="glyphicon glyphicon-ok-circle accept ';
//    if (myRating < 0){
//        vote_html += ' grey ';
//    }
//
//    vote_html += '"></span><span  id="' + res + '-reject-button" onclick="' + func + ' -1);" class="glyphicon glyphicon-remove-circle reject ';
//    if (myRating > 0){
//        vote_html += ' grey ';
//    }
//    vote_html += '"></span> </div> '   ;
//
//    $('#' + res + '-voting-buttons').html(vote_html);
//
//
// //   var loc = jQuery.unique($(".loc"));
//    var locs =  $(".loc") ;
//
//    var entHTML = '';
//    var known = {};
//    locs.each(function(loc){
////    _.forEach(jQuery.unique($(".loc a")), function(loc){
//
//        console.log((loc) )
//        entHTML += locs[loc].innerText
//        entHTML +=  '<br>';
////        $('#' + res + '-voting-buttons').after(loc);
////        $('#' + res + '-voting-buttons').after('<br>');
//
//    })
//
////    $('#corpus-docs').after('');
////    $('#corpus-docs').after(entHTML);

}

var getResourcesForCorpus = function(that) {

    if (!isLoggedIn()) {
        return;
    }

    var groupByQuery = true;

    if (!_.isUndefined(that) && that.checked == false) {
        groupByQuery = false;
    }

    var userName = getCookie("username");
    var userID = parseInt(getCookie("userid"));
    var userToken = getCookie("token");
    var corpus = getCookie("corpus");
    var corpID = getCorpusID(corpus);

    // don't pass "numResults" so we get all resources
    var args = {
        userid: userID,
        user: userName,
        token: userToken,
        corpus: corpID,
        corpusName: corpus
    };


    // var args = JSON.parse(tmp);
    API.getResourcesInCorpus(args, function(data) {

        $("#corpus-docs").html('');

        // there are (currently) two ways a doc can become part of a corpus:
        // (1) right swipe on search results
        // (2) adding a note in the OCR view
        // if we display by "query group" we can miss some docs because they
        // could be in page view, click "next page" then add a note. That doc
        // is now part of the corpus but was not added via a query.
        // The "metadata" passed back, includes ALL documents. They "queries" data
        // only contains docs that were swiped to become part of the corpus.

        if (!groupByQuery) {
            $("#corpus-docs").append('<ul>Resources</ul>');
        }
        // to get a unique ID we'll just use a counter. We can't use the actual
        // query text because there could be spaces.
        var query_count = 0;
        for (i in data.queries) {
            if (groupByQuery) {
                query_count += 1;
                $("#corpus-docs").append('<ul>Query: ' + data.queries[i].query + '</ul>');
            }
            for (j in data.queries[i].resources) {
                var res = data.queries[i].resources[j];
                var id = 'q-' + query_count + '-' + res;
                var el = $("#" + id);
                // see if we already have this resource listed
                if (!_.isEmpty(el)) {
                    var fontSize = parseInt(el.css('font-size'));
                    fontSize += 5;
                    // increase it
                    el.css('font-size', fontSize + "px")
                } else {
                    var title = res;
                    if (!_.isUndefined(data.metadata[res]) && !_.isUndefined(data.metadata[res].title)) {
                        title = data.metadata[res].title;
                    }
                    $("#corpus-docs ul:last").append('<li><a id="' + id + '" href="view.html?kind=' + guessKind(res) + '&id=' + res + '&action=view">' + title + '</a></li>');
                }
            }
        }

        /*
         // version w/o query headers
         var query_count = 0;
         $("#corpus-docs").append('<ul>Resources</ul>');
         for (i in data.queries) {
         
         
         for (j in data.queries[i].resources){
         var res = data.queries[i].resources[j];
         var id = 'q-' + query_count + '-' + res;
         var el = $("#" + id);
         // see if we already have this resource listed
         if (!_.isEmpty(el)){
         var fontSize = parseInt(el.css('font-size'));
         fontSize += 5;
         // increase it
         el.css('font-size', fontSize + "px")
         } else {
         var title = res;
         if (!_.isUndefined(data.metadata[res].title)){
         title = data.metadata[res].title;
         // adding a page number seems more confusing than helpful as they
         // don't correlate with what the actual page number is.
         //                        if (!_.isUndefined(data.metadata[res].pageNumber)){
         //                            title += ' (p ' + data.metadata[res].pageNumber + ')';
         //                        }
         }
         $("#corpus-docs ul").append('<li><a id="' + id + '" href="view.html?kind=' + guessKind(res) + '&id=' + res + '&action=view">' + title + '</a></li>');
         }
         }
         }
         */

//        $("#corpus-docs").html(html);

    }, function(req, status, err) {
        UI.showError("ERROR: ``" + err + "``");
        throw err;
    });




};

var guessKind = function(resourceName) {

    // I'm not happy with this at all, but for now it'll work.

    if (isNaN(Number(resourceName))) {
        // assume Internet Archive resource
        var parts = resourceName.split("_");
        if (parts.length == 1) {
            return 'ia-books';
        }
        if (parts.length == 2) {
            return 'ia-pages';
        }
        if (parts.length == 3) {
            return 'ia-notes';
        }

    } else {
        return "article"; // ACM paper
    }

};

// http://stackoverflow.com/questions/6139107/programatically-select-text-in-a-contenteditable-html-element
function selectElementContents(el) {
    var range = document.createRange();
    range.selectNodeContents(el);
    var sel = window.getSelection();
    sel.removeAllRanges();
    sel.addRange(range);
}


var mzSelect = function(that, entType) {

    console.log(that);

//    var event = new MouseEvent('mousedown', {
//        'view': window,
//        'bubbles': true,
//        'cancelable': true
//    });
    //document.getElementById("David-link").fireEvent("ondblclick")
//    $("#David-link").focus();
//    $("#David-link").select();


    //  var el = document.getElementById("David-link");
//    var el = document.getElementById("David-link");
    selectElementContents((that));
    // el.dispatchEvent(event);


}
function addEntitySearchLinks() {

    // convert entities to search links

//    $(".per-ent").each(function () {
////        var val = $(this).html();
////        $(this).html('<a target="#"  >' + val + '</a>');
////        $("#David-link").mouseup(function(){
////            mzSelect()
////        })
//     //  $(this).html('<a target="#" id="' + val + '-link" onclick="mzSelect(this);">' + val + '</a>');
//        //  $(this).html('<a target="_BLANK" href=\'' + buildSearchLink('person', val, 'ia-books') + '\'>' + val + '</a>');
//    })
    $(".per-ent").bind("mouseup", function() {
        mzSelect(this, 'PER')
    })
//    $(".org-ent").each(function () {
////        var val = $(this).html();
////        $(this).html('<a target="#"  >' + val + '</a>');
//    //    $(this).html('<a target="_BLANK" href=\'' + buildSearchLink('organization', val, 'ia-books') + '\'>' + val + '</a>');
//    })
    $(".org-ent").bind("mouseup", function() {
        mzSelect(this, 'ORG')
    })
//    $(".loc-ent").each(function () {
////        var val = $(this).html();
////        $(this).html('<a target="#"  >' + val + '</a>');
//    //    $(this).html('<a target="_BLANK" href=\'' + buildSearchLink('location', val, 'ia-books') + '\'>' + val + '</a>');
//    })
    $(".loc-ent").bind("mouseup", function() {
        mzSelect(this, 'LOC')
    })
}

function updateRatings(args) {

    // if the doc has ratings, add them to the local ratings store
    if (!_.isUndefined(args.ratings)) {
        ratingsJSON.document[args.request.id] = {};
        // Loop through ratings
        _.forEach(args.ratings, function(rating) {
            ratingsJSON.document[args.request.id][rating.user] = rating.rating;
        });
    }

    // TODO ???? should be its own function but putting here for now so the
    // curent program "flow" will work
    if (!_.isUndefined(args.labels)) {
        // Loop through ratings
        _.forEach(args.labels, function(rec) {
            if (_.isUndefined(votingJSON.document[rec.name])) {
                votingJSON.document[rec.name] = {};
            }
            if (_.isUndefined(votingJSON.document[rec.name][rec.user])) {
                votingJSON.document[rec.name][rec.user] = {};
            }
            votingJSON.document[rec.name][rec.user][rec.subcorpusid] = 1;
        });
    }
}

function displayLabels(res) {

    // TODO ??? duplicate code

    var html = '&nbsp;';
    var labels = localStorage["subcorpora"];
    // 1st check ensures we have an entry for subcorpora, 2nd check makes sure there is data,
    // 3rd check is just a safety - I manually cleared out the localstorage and the page would
    // say "Uncaught SyntaxError: Unexpected end of input" because it was trying to parse an
    // empty string.
    if (!_.isUndefined(labels) && labels != 'undefined' && labels.length != 0) {
        html = '';
        var recs = JSON.parse(labels);

        _.each(recs, function(r) {
            html += '<button type="button" class="btn btn-default btn-sm label-button" onclick="labelClick(this, ' + r.id + ', \'' + res + '\', $(\'#' + res + '\').data(\'kind\'));"><span';

            if (!_.isUndefined(votingJSON.document[res]) && !_.isUndefined(votingJSON.document[res][getCookie("username").toLowerCase()]) && !_.isUndefined(votingJSON.document[res][getCookie("username").toLowerCase()][r.id])) {
                html += ' class="check-mark";'
            }
            html += '></span>' + r.name + '</button>';
        });
    }

//    $(".label_button").bind("click", function(){
//        console.log( this);
//    })
    return html;

}

function displayFacets() {

    // TODO ??? duplicate code

    var html = '&nbsp;';
    var labels = localStorage["subcorpora"];
    // 1st check ensures we have an entry for subcorpora, 2nd check makes sure there is data,
    // 3rd check is just a safety - I manually cleared out the localstorage and the page would
    // say "Uncaught SyntaxError: Unexpected end of input" because it was trying to parse an
    // empty string.
    if (!_.isUndefined(labels) && labels != 'undefined' && labels.length != 0) {
        $("#clear-all-facets").removeClass("disabled");
        html = '';
        var recs = JSON.parse(labels);

        _.each(recs, function(r) {

            html += '<input type="checkbox" name="facets" value="' + r.id + '" />&nbsp;' + r.name + '<br>';

        });
    }

    return html;

}

// if we added a subcorpus, add it to the facet list
function updateFacets() {

    // TODO ??? duplicate code

    if (_.isUndefined(localStorage["subcorpora"])){
        return;
    }
    var html = '&nbsp;';
    var labels = JSON.parse(localStorage["subcorpora"]);

    _.each(labels, function(rec) {
        // see if it exists, if not add it
        if (_.isEmpty($('#facets input[value=' + rec.id + ']'))) {
            $("#facets").append('<input type="checkbox" name="facets" value="' + rec.id + '" />&nbsp;' + rec.name + '<br>');
        }
    })

}
function labelClick(that, subcorpus_id, res, kind) {
//    console.log($(that).text());
//    console.log(res);
    var action = 'add';
    // toggle check mark
    if ($(that).find('span').hasClass("check-mark")) {
        action = 'remove';
        $(that).find('span').removeClass("check-mark");
    } else {
        $(that).find('span').addClass("check-mark");
    }

    var userName = getCookie("username");
    var userID = parseInt(getCookie("userid"));
    var userToken = getCookie("token");
    var corpus = getCookie("corpus");
    var corpID = getCorpusID(corpus);
    var queryID = 0;

    if (!_.isUndefined(Model[kind])) {
        queryID = Model[kind].queryid;
    }

    var args = {
        userid: userID,
        user: userName,
        token: userToken,
        resource: res,
        corpusid: corpID,
        subcorpusid: subcorpus_id,
        queryid: queryID,
        action: action
    };

    API.voteForResource(args, function() {
        console.log("voted!")
        if (action == "add") {
            if (_.isUndefined(votingJSON.document[res][userName])) {
                votingJSON.document[res][userName] = {};
            }
            votingJSON.document[res][userName][subcorpus_id] = 1;
        } else {
            delete votingJSON.document[res][userName][subcorpus_id];
        }
        setUserRatingsHTML(res);
        renderRatingsSidebar(res);
    }, function() {
        console.log("problem voting")
    });

}
