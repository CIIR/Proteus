/**
 * util.js
 *
 * For Javascript shims and other things that don't have a clear project-specific home.
 */

// keep track of how many documents we found for a subcorpus
var foundDocCount = new Map();

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

// the "exclude" option is so we don't highlight terms in the label buttons.
var singleTermHightlightOptions = {"exclude": [".resource-labels *"], "element": "span", "className": "hili"};
var nGramTermHightlightOptions = {"exclude": [".resource-labels *"], "element": "span", "className": "hili", "separateWordSearch": false};
var newHighlightText = function(selector, queryTerms) {
    if (_.isUndefined(queryTerms) || queryTerms.length == 0) {
        return;
    }

    var singleTerms = [];
    var nGramTerms = [];

    _.forEach(queryTerms, function(term) {
        if (term.includes(" ")) {
            nGramTerms.push(term);
        } else {
            singleTerms.push(term);
        }
    })

    if (singleTerms.length > 0) {
        $(selector).mark(singleTerms, singleTermHightlightOptions);
    }
    if (nGramTerms.length > 0) {
        $(selector).mark(nGramTerms, nGramTermHightlightOptions);
    }

};


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

function getNotesID(id) {
    return 'notes-' + id;
}

// temp func - using "resource rating" to track swipe left/right


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
        rating_html += key + ': <b>' + val + '</b><br>';
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
                var id = jqEsc('q-' + query_count + '-' + res);
                var el = $("#" + id);
                // see if we already have this resource listed
                if (!_.isEmpty(el)) {
                    var fontSize = parseInt(el.css('font-size'));
                    fontSize += 5;
                    // increase it
                    el.css('font-size', fontSize + "px")
                } else {
                    var title = res;
                    if (!_.isUndefined(data.metadata[res]) && (!_.isUndefined(data.metadata[res].title) || !_.isUndefined(data.metadata[res].TEI))) {
                        title = (data.metadata[res].title || data.metadata[res].TEI);
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

    if (isNaN(Number(resourceName))) {
        // assume Internet Archive resource
        var id = parsePageID(resourceName);

        var type = 'ia-books';

        if (!_.isEmpty(id.note)) {
            type = 'ia-notes';
        } else if (!_.isEmpty(id.page)) {
            type = 'ia-pages';
        }

        return type;

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

function addEntitySearchLinks() {

    // convert entities to search links

    $(".per-ent").bind("mouseup", function() {
        selectElementContents(this)
    })

    $(".org-ent").bind("mouseup", function() {
        selectElementContents(this)
    })

    $(".loc-ent").bind("mouseup", function() {
        selectElementContents(this)
    })
}

function updateRatings(args) {

    /*  // if the doc has ratings, add them to the local ratings store
     if (!_.isUndefined(args.ratings)) {
     ratingsJSON.document[args.request.id] = {};
     // Loop through ratings
     _.forEach(args.ratings, function(rating) {
     ratingsJSON.document[args.request.id][rating.user] = rating.rating;
     });
     }*/

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
function displayLabelsForComments(annotation) {

    var res = annotation.uri + '_' + annotation.id;
    // if this is a NEW annotation the vote isn't in votingJSON yet - because we don't know the
    // annotation ID when the annotation is created - so we'll update the votingJSON if needed.
    _.forEach(annotation.subcorpusLabels, function(obj) {

        if (_.isUndefined(votingJSON.document[res])) {
            votingJSON.document[res] = {};
        }
        if (_.isUndefined(votingJSON.document[res][annotation.user])) {
            votingJSON.document[res][annotation.user] = {};
        }
        if (obj.checked == true) {
            votingJSON.document[res][annotation.user][obj.subcorpusid] = 1;
        } else {
            delete votingJSON.document[res][annotation.user][obj.subcorpusid];
        }

    });


    // TODO ??? duplicate code - with displayLables() only diff is the onclick function

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
            html += '<button type="button" class="btn btn-default btn-sm label-button" onclick="labelClickComment(this, ' + r.id + ');"><span';

            if (!_.isUndefined(votingJSON.document[res]) && !_.isUndefined(votingJSON.document[res][getCookie("username").toLowerCase()]) && !_.isUndefined(votingJSON.document[res][getCookie("username").toLowerCase()][r.id])) {
                html += ' class="check-mark";'
            }
            html += '></span>' + r.name + '</button>';
        });

    }

    return html;

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
            var checkClass = '';
            var thumbClass = '';
            if (!_.isUndefined(votingJSON.document[res])) {

                var voteCount = 0;

                _.forEach(votingJSON.document[res], function(val, key) {
                    if (!_.isUndefined(votingJSON.document[res][key][r.id])) {
                        voteCount += 1;
                    }
                });

                if (!_.isUndefined(votingJSON.document[res][getCookie("username").toLowerCase()]) && !_.isUndefined(votingJSON.document[res][getCookie("username").toLowerCase()][r.id])) {
                    checkClass = 'check-mark';
                    voteCount -= 1;
                }
                // if anyone ELSE has "voted" for this resource, put a visual indication
                if (voteCount > 0) {
                    thumbClass = 'thumbs-up';
                }
            }

            html += ' class="' + thumbClass + ' ' + checkClass + '"></span>' + r.name + '</button>';
        });
    }

    return  html;

}


function clearSubcorpusFoundDocCount() {
    foundDocCount = new Map();

    _.each($('.num-docs-retrieved-for-subcorpus'), function(el) {
        $(el).html(0);
    });
}

function displaySubcorporaFacets() {

    var html = '&nbsp;';
    var labels = localStorage["subcorpora"];

    // TODO ??? should be it's own function
    // save the searh radio button state
    //    var kind = $('input[name=search-kind]:checked').val();
    //    console.log('saved kinds: ' + kind);
    // save the check box state
    var array = [];
    var urlParams = getURLParams();
    if (!_.isUndefined(urlParams["subcorpora"])) {
        array = urlParams["subcorpora"].split(',');// $.map(getSubcorporaElements(), function(c){return c.value; })
    }
    if (!_.isUndefined(urlParams["overlapOnly"])) {
        $('#show-overlap').attr('checked', urlParams["overlapOnly"]);
    }

    // 1st check ensures we have an entry for subcorpora, 2nd check makes sure there is data,
    // 3rd check is just a safety - I manually cleared out the localstorage and the page would
    // say "Uncaught SyntaxError: Unexpected end of input" because it was trying to parse an
    // empty string.
    if (!_.isUndefined(labels) && labels != 'undefined' && labels.length != 0) {
        $("#clear-all-facets").removeClass("disabled");
        html = '';
        var recs = JSON.parse(labels);

        _.each(recs, function(r) {
            var checked = '';
            if ($.inArray(r.id.toString(), array) >= 0) {
                checked = 'checked';
            }
            subcorpusMap.set(r.id, r.name);
            // TODO ??? really should be doing the append() thing here rather than building an HTML string.
            html += '<input type="checkbox" onclick="UI.onClickSubcorpus();" name="facets" value="' + r.id + '" ' + checked + ' />&nbsp;' + r.name;
            html += ' (<span class="num-docs-retrieved-for-subcorpus" id="' + r.id + '-subcorpus-num-found">0</span>/' + r.count + ') ';
            html += '<a target="_BLANK" href="./bib.html?q=&bib=true&action=search&kind=all&subcorpora=' + r.id + '&name=' + encodeURIComponent(r.name) + '"';
            html += '<span class="fa fa-download"></span></a><br>';
            // TODO ?? don't pass things like bib on URL
        });
    }

    $("#facets").html(html);

    // say how many docs we've returned
    foundDocCount.forEach(function(v, k) {
        $('#' + k + '-subcorpus-num-found').html(v);
    }, foundDocCount);

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
        action: action,
        kind: kind
    };

    // TODO MCZ Horribly, HORRIBLY brain dead way of updating
    // subcorpus document counts. really needs to be revisited
    API.voteForResource(args, function() {
        console.log("voted!")
        var delta = 0;
        if (action == "add") {
            if (_.isUndefined(votingJSON.document[res])) {
                votingJSON.document[res] = {};
            }

            if (_.isUndefined(votingJSON.document[res][userName])) {
                votingJSON.document[res][userName] = {};
            }
            // ONLY increment the count IFF we're the ONLY "vote"
            delta = +1;
            _.forEach(votingJSON.document[res], function(user) {
                if (!_.isUndefined(user[subcorpus_id])) {
                    delta = 0;
                }
            });
            votingJSON.document[res][userName][subcorpus_id] = 1;

        } else {
            delete votingJSON.document[res][userName][subcorpus_id];
            delta = -1;
            _.forEach(votingJSON.document[res], function(user) {
                if (!_.isUndefined(user[subcorpus_id])) {
                    delta = 0;
                }
            });

        }
        setUserRatingsHTML(res);
        renderRatingsSidebar(res);

        // find the correct record
        var recs = JSON.parse(localStorage["subcorpora"]);
        _.forEach(recs, function(rec) {
            if (rec.id == subcorpus_id) {
                rec.count += delta;
                if (foundDocCount.has(subcorpus_id)) {
                    foundDocCount.set(subcorpus_id, foundDocCount.get(subcorpus_id) + delta);
                } else {
                    foundDocCount.set(subcorpus_id, delta);
                }
            }
        });
        localStorage["subcorpora"] = JSON.stringify(recs);

        displaySubcorporaFacets();
    }, function() {
        console.log("problem voting")
    });

}


function termClick(that) {

    // toggle check mark
    if ($($(that).find('span')[0]).hasClass("term-check-mark")) {
        $($(that).find('span')[0]).removeClass("term-check-mark");
    } else {
        $($(that).find('span')[0]).addClass("term-check-mark");
    }

    // re-build the query
    // get all the spans with the "term-check-mark" class
    var terms = $("span.term-check-mark")

    var query = '';

    _.forEach($(terms), function(t) {
        if ($(t).hasClass("entity")) {
            if ($(t).hasClass("person")) {
                query += 'person:';
            }
            if ($(t).hasClass("location")) {
                query += 'location:';
            }
            if ($(t).hasClass("organization")) {
                query += 'organization:';
            }
            query += '"' + $(t).text() + '" ';
        } else {
            if ($(t).hasClass("add-quote")) {
                query += '"' + $(t).text() + '" ';
            } else {
                query += $(t).text() + ' ';
            }
        }

    })

    $("#query-builder-query").html('<a target="_blank" href="index.html?action=search&kind=all&q=' + encodeURI(query) + '">' + query + '</a>');
}

function labelClickComment(that, subcorpus_id) {

    $(that).data('subcorpusid', subcorpus_id);

    // toggle check mark
    if ($(that).find('span').hasClass("check-mark")) {
        $(that).find('span').removeClass("check-mark");
        $(that).data('checked', false);
    } else {
        $(that).find('span').addClass("check-mark");
        $(that).data('checked', true);
    }

}

function getSubcorporaElements() {
    return $('#facets input[type="checkbox"]:checked');
}

// when selecting IDs/classes in JQuery, need to escape certain characters.
function jqEsc(myid) {
    return  myid.replace(/(:|\.|\[|\])/g, "\\$1");
}

function clearOCRSearchResults() {
    $("#book-search-results").html('');
    $("#book-search-results").addClass("center-align");
    $(".book-text").unmark();
    $(".ocr-page-thumbnail").removeClass("ocr-page-result");
}


// We append he page number to the archive ID separated by an underscore.
// While rare, there are some archive IDs that require a bit more than
// a simple split('_') such as: poems___00wott_191.
// We also can have note IDs so an ID could be: poems___00wott_191_56.
// Since we do this a lot, we'll cache the archive IDs we've already done.

var parsedPageCache = new Map();

function parsePageID(pageid) {

    if (parsedPageCache.has(pageid)) {
        var cachedObj = parsedPageCache.get(pageid);
        // this is just here for the unit test to make sure we're using the cache
        cachedObj.cached = true;
        return cachedObj;
    }

    var parts = pageid.split('_');
    var len = parts.length;
    var isNumber = _.map(parts, function(p) {
        return p.length > 0 && !isNaN(p);
    });

    var page = '';
    var note = '';

    // see if we have 2 numbers
    if (len > 2 && isNumber[len - 1] && isNumber[len - 2]) {
        note = parts[len - 1];
        page = parts[len - 2];
        parts.splice(len - 2, 2); // remove note/page entries
    } else if (len > 1 && isNumber[len - 1]) {
        page = parts[len - 1];
        parts.splice(len - 1, 1); // remove page entry
    }

    var obj = {};
    obj.id = parts.join('_');
    obj.page = page;
    obj.note = note;
    parsedPageCache.set(pageid, obj);
    return obj;

}


// This function is used if metadata is not returned with the results.
// Since we could have multiple pages for the same book returned, we
// don't want to get metadata we already know, so we'll keep track of
// books we've already seen.

var bookMetadataCache = new Map();

function getInternetArchiveMetadata(bookid, obj, callback) {

    if (bookMetadataCache.has(bookid)) {
        console.log("I've seen this book before!!!!!");
        obj.metadata = bookMetadataCache.get(bookid);
        // this is just here for the unit test to make sure we're using the cache
        obj.metadata.cached = true;
        callback();
        return;
    }

    $.getJSON('http://archive.org/metadata/' + bookid + '/metadata')
            .done(function(json) {
                obj.metadata = json.result;
                bookMetadataCache.set(bookid, json.result);
                callback();
            })
            .fail(function(jqxhr, textStatus, error) {
                var err = textStatus + ", " + error;
                alert("Something went wrong getting the metadata from archive.org: " + err);
                console.log("Request Failed: " + err);
                callback();
            });
}


var archiveViewerURL = function(pageid) {
    var id = parsePageID(pageid);
    if (isBlank(id.page)) {
        return 'https://archive.org/stream/' + encodeURIComponent(id.id);
    } else {
        return 'https://archive.org/stream/' + encodeURIComponent(id.id) + '#page/n' + id.page + '/mode/1up';
    }
};


Object.defineProperty(Array.prototype, 'naturalSortByField', {
    enumerable: false,
    value: function(p) {
        return this.slice(0).sort(function(a, b) {
            // MCZ:
            // Code for naturalCompare() from: http://stackoverflow.com/questions/15478954/sort-array-elements-string-with-numbers-natural-sort.
            // Modified to accept a parameter as seen in: http://stackoverflow.com/questions/1129216/sort-array-of-objects-by-string-property-value-in-javascript
            // Also added toString() so we can sort on numeric fields.
            var ax = [], bx = [];

            a[p].toString().replace(/(\d+)|(\D+)/g, function(_, $1, $2) {
                ax.push([$1 || Infinity, $2 || ""])
            });
            b[p].toString().replace(/(\d+)|(\D+)/g, function(_, $1, $2) {
                bx.push([$1 || Infinity, $2 || ""])
            });

            while (ax.length && bx.length) {
                var an = ax.shift();
                var bn = bx.shift();
                var nn = (an[0] - bn[0]) || an[1].localeCompare(bn[1]);
                if (nn)
                    return nn;
            }
            return ax.length - bx.length;
        }
        );
    }
});

