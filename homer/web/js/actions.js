// This file handles the flow of a search request
// it interacts a lot with Model which is defined in main.js
//

// A queue to prevent the UI from locking up when doing things like loading
// all the notes for a book. Original code from:
// http://debuggable.com/posts/run-intense-js-without-freezing-the-browser:480f4dd6-f864-4f72-ae16-41cccbdd56cb
$.timeoutQueue = {
    _timer: null,
    _queue: [],
    add: function(fn, context, time) {
        var setTimer = function(time) {
            $.timeoutQueue._timer = setTimeout(function() {
                time = $.timeoutQueue.add();
                if ($.timeoutQueue._queue.length) {
                    setTimer(time);
                }
            }, time || 2);
        };

        if (fn) {
            $.timeoutQueue._queue.push([fn, context, time]);
            if ($.timeoutQueue._queue.length == 1) {
                setTimer(time);
            }
            return;
        }

        var next = $.timeoutQueue._queue.shift();
        if (!next) {
            return 0;
        }
        next[0].call(next[1] || window);
        return next[2];
    },
    clear: function() {
        clearTimeout($.queue._timer);
        $.timeoutQueue._queue = [];
    }
};

var pageHeight = 0;
var MAX_PAGE_HEIGHT = 2000;
var MIN_PAGE_HEIGHT = 500;

// this is the div we'll attach the annotation filter to.
var noteFilterDiv = $("#metadata");
// div for the annotation side bar
var noteSideBarDiv = $("#note-side-bar");


var page = {
    previous: -1,
    current: -1,
    next: -1,
    max: -1,
    skips: 0,
    MAX_SKIPS: 1000
};
var doSearchRequest = function(args) {

    UI.enableSearchButtons(false);

    clearQueryBuilder();

    if (_.isUndefined(Model[args.kind])) {
        Model[args.kind] = {};
    }

    // TODO - big hack, if we have a workingSetQuery we know we're searching for pages
    // belonging to a book so we want the results to go under that book
    if (_.isUndefined(args.workingSetQuery)) {
        resultsDiv = $("#results");
    } else {
        // workingSetQuery is the is the archiveid field followed by the book, we just want the book id
        resultsDiv = $("#page-results-" + args.workingSetQuery.split('archiveid:')[1]);
    }

    disableAutoRetrieve(); // prevent double requests

    var tmpSettings = getCookie("settings");
    // get the settings and use defaults if needed..
    var numEntities = 5; // default
    if (tmpSettings != "") { // first time we don't have cookies
        UI.settings = JSON.parse(tmpSettings);

        if (!_.isUndefined(UI.settings.num_entities)) {
            numEntities = UI.settings.num_entities;
        }
    }
    UI.checkSettings();

    var defaultArgs = {
        n: 10,
        skip: 0,
        snippets: true,
        metadata: true,
        top_k_entities: parseInt(numEntities)
    };

    var subcorpora = [];
    // TODO temp
    _.forEach(getSubcorporaElements(), function(rec) {
        console.log($(rec).attr("value") + ' is checked')
        subcorpora.push(parseInt($(rec).attr("value")));
    })

    // don't limit by subcorport IFF we're searching for pages within a book
    if (!_.isEmpty(subcorpora) && _.isUndefined(args.workingSetQuery)) {
        var subcorporaArgs = '{ "subcorpora":  ' + JSON.stringify(subcorpora) + '}';
        args = _.merge(args, JSON.parse(subcorporaArgs));
    }

    args.overlapOnly = false;
    if ($("#show-overlap").is(':checked')) {
        args.overlapOnly = true;
    }

    // end temp

    // we could have args passed in esp if they're reusing an URL
    if (!_.isUndefined(args.labels)) {
        // format them correctly
        var labelArgs = '{ "labels":  ' + JSON.stringify(args.labels.split(",")) + '}';
        args.labels = "";
        args = _.merge(args, JSON.parse(labelArgs));
    }

    // if we didn't ask for more
    if (!args.skip || args.skip === 0) {
        clearModelResults(Model[args.kind])
        clearSubcorpusFoundDocCount();
        UI.clearResults();
        // don't update the URL if we're getting pages for a book
        if (_.isUndefined(args.workingSetQuery)) {
            updateURL(args); // modify URL if possible
        }
    }

    var userName = getCookie("username");

    if (userName != "") {
        var userToken = getCookie("token");
        var userID = getCookie("userid");
        var corpus = getCookie("corpus");
        var corpusID = -1;
        if (corpus.length == 0) {
            alert("Please select a corpus!");
            return;
        } else {
            corpusID = getCorpusID(corpus);
        }
        console.log("corpus: " + corpus + " id: " + corpusID);
        var tagArgs = {
            tags: false,
            user: userName,
            userid: userID,
            token: userToken,
            corpus: parseInt(corpusID),
            corpusName: corpus
        };
        args = _.merge(args, tagArgs);
    }
    var actualArgs = _.merge(defaultArgs, args);

    if (args.kind.endsWith("corpus")) {
        actualArgs.action = "search-corpus";
        if (isLoggedIn() == false) {
            UI.showProgress("Please log in to search a corpus");
            return;
        }
    }

    // only allow blank queries if we're searching a corpus or by label(s)
    if ((!actualArgs.q || isBlank(actualArgs.q)) && (_.isEmpty(actualArgs.labels)) && (_.isUndefined(actualArgs.subcorpora) || actualArgs.subcorpora.length == 0)) {
        UI.showProgress("Query is blank!");
        UI.enableSearchButtons(true);
        return;
    }
    $("#more").html('<img src="/images/more-loader.gif"\>');

    Model[args.kind].request = actualArgs;
    console.log(Model[args.kind].request);

    UI.showProgress("Search Request sent to server!");
    API.action(actualArgs, onSearchSuccess, function(req, status, err) {
        UI.showError("ERROR: ``" + err + "``");
        UI.enableSearchButtons(true);

        // set up the auto retrieve again
        enableAutoRetrieve();
        throw err;
    });

    return actualArgs;
};

/**
 * This gets called with the response from JSONSearch
 */
var savedData = [];
var uniqWords = [];

function getTermHTMl(term, count, classes) {
    return '<button class="button term" type="button"  onclick="termClick(this);"><span  class="term ' + classes + '" style="font-size: ' + calcPixelSize(count) + 'px !important;">' + term + '</span><span class="term-freq"> (' + count + ') </span></button> ';

}
function calcPixelSize(count) {
    return Math.max(12, Math.min(50, Math.round(Math.sqrt(count))));
}

var onSearchSuccess = function(data) {

    UI.enableSearchButtons(true);

    // clear data on each new search IFF we're not
    // searching pages within a book
    if (data.request.skip == 0 && _.isUndefined(data.request.workingSetQuery) == true) {
        uniqWords = [];
        savedData = [];
    }

    UI.clearError();

    var userID = getCookie("userid");
    $("#more").html(""); // clear progress animation

    $("#show-corpus-terms").hide();

    // show query builder if they searched the corpus OR have a sub-corpus selected
    if (getSubcorporaElements().length > 0) {

        if (UI.settings.show_unigrams) {
            $("#high-tf").parent().show();
            $("#high-snippettf").parent().show();
        } else {
            $("#high-tf").parent().hide();
            $("#high-snippettf").parent().hide();
        }
        if (UI.settings.use_query_builder) {
            $("#query-builder-link").show();
        }
        _.forEach(data.totalTF, function(t) {
            $("#high-tf").append(getTermHTMl(t.term, t.count, ''));
        })

        _.forEach(data.snippetTF, function(t) {
            $("#high-snippettf").append(getTermHTMl(t.term, t.count, ''));
        })

        _.forEach(data.bigrams, function(t) {
            $("#high-bigrams").append(getTermHTMl(t.ngram, t.count, 'add-quote'));
        })

        _.forEach(data.trigrams, function(t) {
            $("#high-trigrams").append(getTermHTMl(t.ngram, t.count, 'add-quote'));
        })

        _.forEach(data.perEntities, function(t) {
            $("#high-entities-per").append(getTermHTMl(t.entity, t.count, 'entity person'));
        })

        _.forEach(data.locEntities, function(t) {
            $("#high-entities-loc").append(getTermHTMl(t.entity, t.count, 'entity location'));
        })

        _.forEach(data.orgEntities, function(t) {
            $("#high-entities-org").append(getTermHTMl(t.entity, t.count, 'entity organization'));
        })

    } // end if show query builder

    // mark up results with rank and kind
    Model[data.request.kind].query = data.request.q;
    Model[data.request.kind].queryType = data.queryType;
    Model[data.request.kind].queryid = data.queryid;

    var rank = Model[data.request.kind].results.length + 1;
    var newResults = _(data.results).map(function(result) {
        result.viewKind = data.request.viewKind || data.request.kind;
        result.kind = data.request.kind;
        result.rank = rank++;

        // TODO ??? duplicate code - we do this in a couple places - should probably just call updateRatings() (with a better name)
        votingJSON.document[result.name] = {};
        // Loop through ratings

        // HUGE UGLY HACK - we only want to count one vote per label so we'll use a
        // set to make sure we only get one per label per document
        var docSubcorpus = new Set();

        _.forEach(result.labels, function(rec) {
            if (_.isUndefined(votingJSON.document[rec.name])) {
                votingJSON.document[rec.name] = {};
            }
            if (_.isUndefined(votingJSON.document[rec.name][rec.user])) {
                votingJSON.document[rec.name][rec.user] = {};
            }
            votingJSON.document[rec.name][rec.user][rec.subcorpusid] = 1;

            // the labels are found using "LIKE <resource-name>%" so we are getting pages & notes
            // for a book. Make sure we're dealing with the current document.
            if (rec.name == result.name) {
                docSubcorpus.add(rec.subcorpusid);
            }
        });

        // loop through the set and add ONE for each label found for the current doc
        docSubcorpus.forEach(function(value) {
            // keep track of how many documents per subcorpus are returned
            if (foundDocCount.has(value)) {
                foundDocCount.set(value, foundDocCount.get(value) + 1);
            } else {
                foundDocCount.set(value, 1);
            }
        });

        // see if we're getting pages for a book
        // TODO ??? kind specific logic
        if (!_.isUndefined(data.request.workingSetQuery)) {
            // extract the book id
            var bookid = result.name.split('_')[0];
            var html = '<a href="#" onclick="UI.hidePages(\'' + bookid + '\');">';
            html += '<span class="glyphicon glyphicon-collapse-up"></span>&nbsp;Hide pages (' + data.results.length + ')</a>'
            $("#search-pages-link-" + bookid).html(html)
            $("#search-pages-link-" + bookid).data("num_results", data.results.length);
        }

        return result;
    }).value();


    if (!_.isUndefined(data.subcorpora)) {
        localStorage["subcorpora"] = JSON.stringify(data.subcorpora);
        displaySubcorporaFacets();
    }


    // update the model
    Model[data.request.kind].results = _(Model[data.request.kind].results).concat(data.results).value();

    // don't show results if empty
    if (_.isEmpty(data.results)) {
        if (data.request.skip > 0) {
            UI.showProgress("No more results for '" + data.request.q + "'");
        } else {
            UI.showProgress("No results found for '" + data.request.q + "'");
        }
        return;
    }

    var usingLabels = false;

    // lowercase the query terms so when we hilight we match
    // regardless of case

    var termLen = 0;

    if (!_.isUndefined(data.queryTerms))
        termLen = data.queryTerms.length;

    // clear out previous query terms
    Model[data.request.kind].queryTerms = [];
    for (var i = 0; i < termLen; i++) {
        Model[data.request.kind].queryTerms.push(data.queryTerms[i].toLowerCase());
    }
    UI.appendResults(Model[data.request.kind].queryTerms, newResults, data.queryid);

    if (_.isUndefined(data.request.workingSetQuery)) {
        // TODO : we only really need the results array - not the whole structure
        savedData = _.merge(savedData, data, function(a, b) {
            return _.isArray(a) ? a.concat(b) : undefined;
        });

        renderDups(savedData);
    }

    if (data.request.n > data.results.length) {
        UI.showProgress("No more results for '" + data.request.q + "'");
    }
    // if we searched by labels or (sub)corpus, we returned EVERYTHING so we
    // don't re-enable the auto-retrieve
    if (usingLabels === false && Model[data.request.kind].request.action != "search-corpus" && (_.isUndefined(data.request.subcorpora) || data.request.subcorpora.length == 0)) {
        // ??? if we're expanding pages for a book, this happens TOO SOON, the pages haven't rendered
        // yet so the scroll bar size is wrong
        enableAutoRetrieve();
    } else {
        UI.showProgress("No more results.");
    }

    setUpMouseEvents(); // TODO : only want to do this once

};

function renderDups(data) {

    if (!_.isUndefined(data.request.workingSetQuery)) {
        // if we're searching for pages in a book, don't look for duplicates - there can't be any unless
        // something was scanned twice.
        return;
    }

    // create a matrix that tells us how confident we are that two docs are duplicates
    var count = uniqWords.length // data.results.length;

    var conf = Array.apply(null, Array(count)).map(Number.prototype.valueOf, 0);
    // ???? temp matrix for visualizing scores/duplicates
    var docScores = Array.apply(null, Array(count)).map(Number.prototype.valueOf, 0);

    for (i = 0; i < count; i += 1) {
        conf[i] = Array.apply(null, Array(count)).map(Number.prototype.valueOf, 0);
        docScores[i] = Array.apply(null, Array(count)).map(Number.prototype.valueOf, 0);
        for (j = 0; j < count; j += 1) {

            if (i == j) {
                continue; // don't compare a doc with itself
            }

            // skip any empty entries - these are things like notes
            if (uniqWords[i].length == 0 || uniqWords[j].length == 0) {
                continue;
            }
            var diff = _.xor(uniqWords[i], uniqWords[j]);
            var confidence = (100 - (diff.length * 2));

            conf[i][j] = Math.max(conf[i][j], confidence);

        }
    }

    var ignoreCol = new Set();

    var minConfidence = $("#dup-slider").slider("value");

    // using a greedy algorithm, ANY doc that has a non-zero confidence is
    // considered a duplicate of the document at that rank (row number).
    // The slider can be used to remove low confidence values
    var parentSet = new Set();

    for (row = 0; row < count; row += 1) {
        var doneRow = false;
        while (doneRow == false) {
            // find the max confidence for the row
            var m = _.max(conf[row]);
            if (m <= minConfidence) {
                doneRow = true;

                // if there are no negative values, this row has no dups
                if (_.min(conf[row]) >= 0) {
                    docScores[row][row] = data.results[row].score;
                }
                continue;
            }

            var col = conf[row].indexOf(m)
            // check if this is a column we want to ignore
            if (ignoreCol.has(col)) {
                conf[row][col] = 0;
                continue;
            }
            docScores[row][col] = data.results[col].score;
            docScores[row][row] = data.results[row].score; // score for the highest ranked doc with duplicates

            conf[row][col] = -1 * conf[row][col];
            conf[col][row] = -1 * conf[col][row];

            // we don't need to check this column again
            ignoreCol.add(col);

            moveDocument(data, row, col, m);
            parentSet.add(row);
        }
    }

    UI.showHideDups();

    // go through all the docs that have duplicates and add the "show/hide dup" link
    parentSet.forEach(function(rank) {
        rank += 1;
        UI.setDupLinkHTML(rank);
    });

}

function moveDocument(data, parentIdx, dupIdx, confidence) {

    var name = jqEsc(data.results[dupIdx].name);

    // dim out the dup
    $("#" + name).addClass("dup-result")
    $("#" + name + '-dup-confidence').html("    Duplicate confidence: " + confidence + "%")

    parentIdx += 1;
    if ($("#dup-parent-" + parentIdx).length == 0) {
        $('<div id="dup-parent-' + parentIdx + '"></div>').insertBefore($(".result-dups-" + parentIdx))
    }
    var obj = $("#" + name);
    obj.appendTo((".result-dups-" + parentIdx));
}
function printMatrix(conf, count) {

    var tmp = '';
    var header = '';
    for (i = 1; i < count + 1; i += 1) {
        header += (i + '\t');
    }
    console.log('\t' + header);
    for (i = 0; i < count; i += 1) {
        console.log(tmp);
        tmp = 'rank ' + (i + 1) + '\t';
        for (j = 0; j < count; j += 1) {
            tmp += '\t' + conf[i][j];
        }
    }
    console.log(tmp);
}

var renderBookPageHTML = function(text, pageID, pageNum, el) {
    var pgImage = pageImage(pageID, pageNum);
    var pgTxt = text;
    var id = getBookID(pageID, pageNum);

    var labelHTML = '<div id="notes-' + id + '" class="resource-labels">' + displayLabels(id) + '</div>';

    el.html('<div class="book-page row clearfix ">' +
            '<div class="col-md-2 column left-align" ><span id="' + id + '-user-ratings-w-names"></span></div>' +
            '<div id="' + getNotesID(pageID, pageNum) + '" class="book-text col-md-4 column left-align">' + pgTxt + labelHTML + '</div>' +
            '<div  class="page-image col-md-4 column left-align"><br>' + '<a class="fancybox" href="' + pgImage + '" ><img src="' + pgImage + '"></a></div>' +
            '</div>');

    setUserRatingsHTML(id);
    setVoteHTML(id);
    processTags();
};


var doViewRequest = function(args) {
    UI.showProgress("View request sent to server!");

    API.action(args,
            function(args) {
                updateRatings(args);
                if (args.request.kind == 'article') {
                    onViewArticleSuccess(args);
                } else if (args.request.kind == 'ia-pages') {
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

    updateRatings(args);

    if (args.found == false) {
        // get the prior page
        page.previous -= 1;
        doPrevPageRequest(args.request.page_id, page.previous);
        return;
    }
    if (!_.isUndefined(args.text)) {

        // find the first instance of the "book-page" class and append to that:
        var elid = "page-" + args.request.page_id + "_" + args.request.page_num;
        $(".book-page:first").before('<div id="' + elid + '"></div>');

        renderBookPageHTML(args.text, args.request.page_id, args.request.page_num, $("#" + elid));

        // ??? whole issue with this is that we don't know where to render the page in the
        // DOM, why not put in a placeholder THEN renderBookPageHTML can  do the substitution?????


        if ($(".book-page:first").height() > pageHeight && $(".book-page:first").height() < MAX_PAGE_HEIGHT) {
            pageHeight = $(".book-page:first").height();
        }
        $(".page-image").height(pageHeight);

    }
    page.previous -= 1;
    setPageNavigation(args.request.page_id);

    var corpus = getCookie("corpus");
    if (!isLoggedIn() || corpus == "")
        return;

    initBookAnnotationLogic(args.request.page_id, args.request.page_num, true);
    addEntitySearchLinks();
};

// We have an annotator for each page, and the filter (ProteusAnnotationFilter)
// and the note side bar annotators apply to the whole book, we need to forward
// events that happen on the page to the filter and sidebar and anything that
// happens in the sidebar has to be forwarded to the page annotator.

Annotator.Plugin.NoteEvent = function(element, reorderAll) {
    var plugin = {};

    plugin.pluginInit = function() {

        var that = this;
        this.annotator
                .subscribe("afterAnnotationCreated", function(annotation) {
                    noteSideBarDiv.trigger("noteViewerCreateEventName", annotation);
                })
                .subscribe("annotationCreated", function(annotation) {
                    noteFilterDiv.trigger("noteUpdate");
                })
                .subscribe("annotationUpdated", function(annotation) {
                    noteSideBarDiv.trigger("noteViewerUpdateEventName", annotation);
                })
                .subscribe("annotationsLoaded", function(annotation) {
                    if (!_.isUndefined(annotation) && annotation.length > 0) {
                        noteFilterDiv.trigger("noteUpdate");
                        // on the *initial* load, we can't rely on the notes coming in order. This means
                        // that the notes in the side bar may be out of order. To fix this, as we load each
                        // page, we'll reorder *just* that page's notes which are the last ones in the
                        // side bar. This is a slight optimization over reordering *all* notes on each
                        // "annotationsLoaded" message. If a book had 10 pages with 5 notes on each page,
                        // we'll only have to reorder 50 notes as opposed to 275:
                        // 5 + 10 + 15 + 20 + 25 + 30 + 35 + 40 + 45 + 50 = 275
                        // In the case that we want to reorder all, we set the reordeAll flag to true. We
                        // would want to do this in cases such as if you're in the page OCR view and you
                        // hit the "previous page" button. That would load the notes at the bottom of the list
                        // so we have to reorder the entire note list to get them in the correct order.
                        if (_.isUndefined(reorderAll) || reorderAll == false) {
                            noteSideBarDiv.trigger("myannotationsLoaded", annotation[0].uri);
                        } else {
                            noteSideBarDiv.trigger("myannotationsLoaded", "*");
                        }
                    }
                })
                .subscribe("annotationDeleted", function(annotation) {
                    noteFilterDiv.trigger("noteUpdate");
                    noteSideBarDiv.trigger("noteViewerDeleteteEventName", annotation);
                })
                // these events are triggered from the note side bar and
                // are forwarded to the page.
                .subscribe("myannotationUpdated", function(annotation) {
                    that.annotator.updateAnnotation(annotation);
                })
                .subscribe("myannotationDeleted", function(annotation) {
                    that.annotator.deleteAnnotation(annotation);
                });
    };
    return plugin;

};

var firstTime = true;

var initBookAnnotationLogic = function(pageID, pageNum, reorder) {

    var el = '#' + getNotesID(pageID, pageNum);
    initAnnotationLogic(el, reorder);

};

var initAnnotationLogic = function(element, reorder) {

    var corpus = getCookie("corpus");
    var userName = getCookie("username");
    var userToken = getCookie("token");
    var userID = getCookie("userid");
    var corpusID = getCorpusID(corpus);

    var ann = $(element).annotator();
    var resource = element.substring(7).toString(); // strip off "[#|.]notes-"

    // set up a plugin that will notify the filter when a note
    // was created or deleted.
    if (_.isUndefined(reorder) || reorder == false)
        ann.annotator('addPlugin', 'NoteEvent', false);
    else
        ann.annotator('addPlugin', 'NoteEvent', true);

    ann.annotator('addPlugin', 'Store', {
        annotationData: {
            uri: resource,
            userid: parseInt(userID),
            user: userName,
            token: userToken,
            corpus: parseInt(corpusID),
            corpusName: corpus
        },
        loadFromSearch: {
            'uri': resource,
            corpus: parseInt(corpusID)

        },
        urls: {
            // These are the default URLs.
            create: '/annotations/ins',
            update: '/annotations/upd/:id',
            destroy: '/annotations/del/:id',
            search: '/annotations/search'
        }
    });

    ann.annotator('addPlugin', 'Permissions', {
        user: {
            id: parseInt(userID),
            name: userName
        },
        showViewPermissionsCheckbox: false,
        showEditPermissionsCheckbox: false,
        userId: function(user) {
            if (user && user.id) {
                return parseInt(user.id);
            }
            return user;
        },
        userString: function(user) {
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

    if (firstTime) {
        firstTime = false;
        noteSideBarDiv.annotator().annotator('addPlugin', 'AnnotatorViewer');

        // the stock Filter only searches within the HTML element
        // it's attached to. Since we split books into pages and each
        // page has its own annotator that won't work for us. So I
        // extended the stock Filter and have it search for all
        // annotations within the "searchArea" we pass in. Note that
        // it's attached to an element that is NOT the searchArea, if was
        // it would render the entire area read-only.
        noteFilterDiv.annotator({
            readOnly: true
        });
        noteFilterDiv.annotator('addPlugin', 'ProteusAnnotationFilter');
    }

    $(".annotator-hl-test").draggable({
        appendTo: "body",
        helper: 'clone',
        scroll: 'true',
        refreshPositions: true
    });

};

var viewNextPageSuccess = function(args) {

    updateRatings(args);

    // check if we're going beyond the end. We use the "number of images"
    // from the metadata, if that's not available, we'll stop after a set
    // number of skips
    if ((page.max != -1 && page.next > page.max) || page.skips > page.MAX_SKIPS) {
        setPageNavigation(args.request.page_id);
        UI.showProgress("Reached the end of the book");
        return;
    }
    if (args.found == false) {
        // count the number of pages we skipped
        page.skips += 1;
        // get the next page
        page.next += 1;
        doNextPageRequest(args.request.page_id, page.next);
        return;
    }
    if (!_.isUndefined(args.text)) {
        page.skips = 0; // reset

        var elid = "page-" + args.request.page_id + "_" + args.request.page_num;

        // find the last instance of the "book-page" class and append to that:
        $(".book-page:last").after('<div id="' + elid + '"></div>');
        var html = renderBookPageHTML(args.text, args.request.page_id, args.request.page_num, $("#" + elid));

        if ($(".book-page:last").height() > pageHeight && $(".book-page:last").height() < MAX_PAGE_HEIGHT) {
            pageHeight = $(".book-page:last").height();
        }
        $(".page-image").height(pageHeight);
    }
    page.next += 1;
    setPageNavigation(args.request.page_id);

    var corpus = getCookie("corpus");
    if (!isLoggedIn() || corpus == "")
        return;

    initBookAnnotationLogic(args.request.page_id, args.request.page_num);
    addEntitySearchLinks();
};

var doPrevPageRequest = function(pageID) {
    console.log("id: " + pageID + " current page: " + page.previous);
    if (page.previous < 0) {
        UI.showProgress("Found start of the book");
        setPageNavigation(pageID);
        return;
    }
    UI.showProgress("View request sent to server!");
    // NOTE: some pages may not exist because the original page could have been blank.
    var id = pageID + '_' + page.previous;
    var userToken = getCookie("token");
    var corpus = getCookie("corpus");
    var corpusID = getCorpusID(corpus);
    API.action({
        kind: "ia-pages",
        id: id,
        action: "view",
        token: userToken,
        page_id: pageID,
        page_num: page.previous,
        corpusID: corpusID
    }, viewPrevPageSuccess, function(req, status, err) {
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
    var corpus = getCookie("corpus");
    var corpusID = getCorpusID(corpus);
    API.action({
        kind: "ia-pages",
        id: id,
        action: "view",
        token: userToken,
        page_id: pageID,
        page_num: page.next,
        corpusID: corpusID
    }, viewNextPageSuccess, function(req, status, err) {
        UI.showError("ERROR: ``" + err + "``");
        throw err;
    });

    UI.showProgress("");
};

/** this gets called with the response from ViewResource */
var onViewPageSuccess = function(args) {

    var identifier = args.request.id.split('_')[0];
    var pageNum = args.request.id.split('_')[1];

    var pageID = "";
    UI.clearError();
    metadataDiv.hide();

    pos = args.request.id.lastIndexOf('_');
    page.current = parseInt(args.request.id.substr(pos + 1));
    page.previous = page.current - 1;
    page.next = page.current + 1;
    if (!_.isUndefined(args.metadata) && !_.isUndefined(args.metadata.imagecount)) {
        page.max = args.metadata.imagecount;
    }
    pageID = args.request.id.slice(0, pos);

    var metaHtml = '';
    metaHtml += ' <table>';
    _(args.metadata).forIn(function(val, key) {
        metaHtml += '<tr>';
        metaHtml += '<td>' + key + '</td>';
        metaHtml += '<td>' + val + '</td>';
        metaHtml += '</tr>';
    });
    metaHtml += '</table></div>';
    metadataDiv.html(metaHtml);
    var html = '';
    html += '<a class="show-hide-metadata" onclick="UI.showHideMetadata();">Show Metadata</a>'
    //
    //    html += '<div>[<span class="per">PERSON</span>]&nbsp;[<span class="loc">LOCATION</span>]&nbsp;[<span class="org">ORGANIZATION</span>]</div>';
    //    if (args.request.kind == 'ia-pages'){
    //        html += '<div class="pageNavigation"></div>';
    //        html += '<div id="prevPage"></div>';
    //    }

    var elid = "page-" + args.request.id;
    $("#book-pages").html('<div id="' + elid + '"></div>');

    html += renderBookPageHTML(args.text, identifier, pageNum, $("#" + elid));

    // base the page size on the first page
    pageHeight = Math.max(MIN_PAGE_HEIGHT, $(".book-text").height());
    $(".page-image").height(pageHeight);

    //  viewResourceDiv.html(html);
    setPageNavigation(pageID);

    viewResourceDiv.show();
    var corpus = getCookie("corpus");
    if (!isLoggedIn() || corpus == "") {
        return;
    }

    // if we have a noteid, scroll to that note
    var el = "#" + getNotesID(identifier, pageNum);
    var urlParams = getURLParams();
    if (!_.isUndefined(urlParams["noteid"])) {
        // scroll to the note once all notes for that page are loaded.
        $(el).bind("annotationsLoaded", function() {

            var note = '.annotator-hl[data-annotation-id="' + urlParams["noteid"] + '"]';

            if ($(note).length) {

                $('#results-right').animate({
                    scrollTop: $(note).offset().top - 80
                }, 2000);

            } else {
                alert("Couldn't find that note, perhaps it was deleted?")
            }

            // remove the notid from the URL so we don't re-trigger
            removeURLParam("noteid");

            // unbind once we're done
            $(el).unbind("annotationsLoaded");
        });
    }

    initBookAnnotationLogic(identifier, pageNum);

    addEntitySearchLinks();

    UI.showProgress("");

};

var onViewBookSuccess = function(args) {

    UI.clearError();
    metadataDiv.hide();

    var metaHtml = '';
    metaHtml += ' <table>';
    _(args.metadata).forIn(function(val, key) {
        metaHtml += '<tr>';
        metaHtml += '<td>' + key + '</td>';
        metaHtml += '<td>' + val + '</td>';
        metaHtml += '</tr>';
    });
    metaHtml += '</table></div>';
    metadataDiv.html(metaHtml);

    var html = '<a href="#" class="show-hide-metadata" onclick="UI.showHideMetadata();">Show Metadata</a>';

    html += args.text;

    viewResourceDiv.html(html);
    viewResourceDiv.show();
    processTags();

    var corpus = getCookie("corpus");
    if (!isLoggedIn() || corpus == "")
        return;

    // go through the book and insert an ID attribute so we can share the notes across
    // both books and pages.
    var pageBreaks = $(".page-break");
    $(".page-break").addClass("book-page row clearfix ");

    var id = args.request.id;

    // if we have a "noteid" parameter, load that note first and
    // scroll to it. All the other notes will load in the background
    // via timeouts so the UI doesn't lock up when loading large books.
    var urlParams = getURLParams();
    var pgNum = urlParams["pgno"];
    if (_.isUndefined(pgNum)) {
        pgNum = -1;
    }

    // add the annotation widget to the page with the note
    updateNoteDiv(id, pgNum);
    initBookAnnotationLogic(id, pgNum);
    el = "#" + getNotesID(id, pgNum);

    if (!_.isUndefined(urlParams["noteid"])) {
        // scroll to the note once all notes for that page are loaded.
        $(el).bind("annotationsLoaded", function() {

            var note = '.annotator-hl[data-annotation-id="' + urlParams["noteid"] + '"]';

            if ($(note).length) {

                $('#results-right').animate({
                    scrollTop: $(note).offset().top - 80
                }, 2000);

            } else {
                alert("Couldn't find that note, perhaps it was deleted?")
            }

            // remove the notid from the URL so we don't re-trigger
            removeURLParam("noteid");

            // unbind once we're done
            $(el).unbind("annotationsLoaded");
        });

    } // end if we have a noteid

    // create a place were we can display the status
    $(".navbar").append('<div id="loading-msg"></div>');

    // now get any notes for all other pages
    _.forEach(pageBreaks, function(pb) {

        var currentPg = $(pb).attr("page");

        // don't re-do the page we may have done above
        if (currentPg == pgNum) {
            return;
        }

        updateNoteDiv(id, currentPg);

        $.timeoutQueue.add(function() {
            $("#loading-msg").html("loading notes for page " + currentPg);
            initBookAnnotationLogic(id, currentPg)
        }, this);

    });

    // lastly... hide the progress message and hook up the Filter plugin
    $.timeoutQueue.add(function() {
        $("#loading-msg").hide();

        // the stock Filter only searches within the HTML element
        // it's attached to. Since we split books into pages and each
        // page has its own annotator that won't work for us. So I
        // extended the stock Filter and have it search for all
        // annotations within the "searchArea" we pass in. Note that
        // it's attached to an element that is NOT the searchArea, if was
        // it would render the entire area read-only.
        noteFilterDiv.annotator({
            readOnly: true
        });
        noteFilterDiv.annotator('addPlugin', 'ProteusAnnotationFilter');
        processTags()
    }, this);

    UI.showProgress("");
    addEntitySearchLinks();
};

var updateNoteDiv = function(bookid, pgnum) {

    var pgImage = pageImage(bookid, pgnum);
    var noteid = getNotesID(bookid, pgnum);

    var pb = '.page-break[page=' + pgnum + ']';

    var id = getBookID(bookid, pgnum);
    var labelHTML = '<div id="notes-' + id + '" class="resource-labels">' + displayLabels(id) + '</div>';

    var pghtml = '<div id="' + noteid + '" class="book-text col-md-5 column left-align">' + $(pb).html() + labelHTML + '</div>' +
            '<div id="' + noteid + '-page-image" class="page-image col-md-5 column left-align"><br><a href="#" onclick="getPageImage(\'' +
            noteid + '-page-image\',\'' + pgImage + '\');" >View the actual page</a></div>';
    $(pb).html(pghtml);
};

var getPageImage = function(id, imgURL) {

    $('#' + id).html('<br><a class="fancybox" href="' + imgURL + '" ><img src="' + imgURL + '"></a>')

};

var setPageNavigation = function(bookID) {

    var prevHTML = '<button style="width: 100%;"  onclick="doPrevPageRequest(\'' + bookID + '\',' + (page.previous) + ');" >&#9650;&nbsp;Previous Page&nbsp;&#9650;</button>';
    var nextHTML = '<button style="width: 100%;" onclick="doNextPageRequest(\'' + bookID + '\',' + (page.next) + ');">&#9660;&nbsp;Next Page&nbsp;&#9660;</button>';

    if ((page.max != -1 && page.next > page.max) || page.skips > page.MAX_SKIPS) {
        nextHTML = '';
    }

    if (page.previous < 0) {
        prevHTML = '';
    }

    $("#view-nav-top").html(prevHTML);
    $("#view-nav-bottom").html(nextHTML)

};

var processTags = function() {

    if (document.getElementById("cb-per").checked) {
        $("person").removeClass("person-off")
    } else {
        $("person").addClass("person-off")
    }
    if (document.getElementById("cb-loc").checked) {
        $("location").removeClass("location-off")
    } else {
        $("location").addClass("location-off")
    }
    if (document.getElementById("cb-org").checked) {
        $("organization").removeClass("organization-off")
    } else {
        $("organization").addClass("organization-off")
    }

};

function submitCorpusDialog() {
    // make sure we have a name
    var corpusName = $("#corpus-name").val().trim();
    if (corpusName.trim() == "") {

        $("#corpusError").addClass("in");
        $("#corpusError").html("Corpus name cannot be blank.");
        setTimeout(function() {
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
