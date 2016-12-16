/**
 * ui.js
 *
 * This file roughly contains all the interfacing with the index.html boilerplate, using jquery as needed.
 *
 */

var errorDiv = $("#error");
var resultsDiv = $("#results");
var metadataDiv = $("#book-metadata");
var viewResourceDiv = $("#view-resource");
var progressDiv = $("#progress");
var queryBox = $("#ui-search");

var votingJSON = {"document": {}};

// UI object/namespace
var UI = {};
UI.generateButtons = function() {

    var urlParams = getURLParams();
    var currentKind = urlParams["kind"];

    API.getKinds({}, function(data) {
        if (data.title)
            $("#proteus-title").html(data.title);
        UI.defaultKind = data.defaultKind;
        $("#search-buttons").click(function() {
            UI.onClickSearchButton(data.defaultKind, "Search");

        });

        var availableKinds = _(data.kinds);
        var buttonDescriptions = _(UI.buttons);
        // for all kinds the server gives back, make a button
        _.forIn(data.kinds, function(spec, kind) {
            spec.kind = kind; // so the onClick knows what kind it was
            if (!spec.button) {
                // UI.showError("You need to specify a \"button\" display attribute for kind \"" + kind + "\"");
                // the "view" action requires that we have a kind/index specified, but we may not
                // want retrieval buttons for them so rather than display a warning just write it to the console.
                console.log("You didn't specify a \"button\" display attribute for kind \"" + kind + "\"");
                return;
            }

            $("#search-rb").append('<label class="radio-inline"><input type="radio" name="search-kind" value="' + spec.kind + '">' + spec.button + '</label>');

            // see if we're the default button - unless there's already a "kind" on the URL. If they're
            // clicking on an entity it'll re-trigger this logic and we want to keep the current "kind" we
            // do not want to rest the search button to be the default
            if ((!_.isUndefined(currentKind) && currentKind == kind) || (_.isUndefined(currentKind) && kind === UI.defaultKind)) {
                $('#search-rb input[value=' + kind + ']').attr('checked', true);
                $("#search-rb").click(function() {
                    UI.onClickSearchButton(spec.kind, spec.button);
                });
            }

        });
    });

};

UI.clear = function() {
    UI.clearResults();
    UI.clearError();
    viewResourceDiv.html('');
};
UI.clearResults = function() {
    resultsDiv.html('');
    resultsDiv.show();

    // clear previous results
    var cardCount = $("[id^=entity-card-]").length;
    for (i = cardCount -1; i >= 0; i--){
        $('.my-slider').slick('slickRemove', i);
    }
    $("#external-results").hide();
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
    // show the "x" if there is something in the search input (helpful if they refresh)
    if (q != '') {
        $('.form-control-clear').toggleClass('hidden', false);
    }
};
/**
 * Render a single retrieval result.
 * @see render.js
 * I'm adding comments now since i regret it
 * when I dont
 * deleted UI.makeResult since its useless.
 
 */

UI.renderSingleResult = function(result, queryTerms, prependTo, queryid) {

    var name = result.name;
    var id = jqEsc(result.name);

    //console.debug("result name: " + result.name);
    var renderer = getResultRenderer(result.viewKind); //added this line and 5 below make adding/subt elements in future easier
    var resDiv = $('<div>');
    resDiv.attr('class', 'result');
    resDiv.attr('id', name);


    // put it at the end unless we pass in where we want it to go
    if (_.isUndefined(prependTo) || prependTo == false) {
        resultsDiv.append(renderer(queryTerms, result, resDiv, queryid)); //* 6/26/2014
    } else {
        $(prependTo).after(renderer(queryTerms, result, resDiv, queryid));
    }

    // TODO this is IA (book) specific
    // if we don't have metadata, get it from the internet archive
    if (_.isUndefined(result.meta) || _.isEmpty(result.meta)){
        var args = {};
        // note we do NOT use the escaped version of the document name. Doc's with
        // dots in them would look like "a\.b\.c" and would fail on the API call.
        // But we DO need to escape it so JQuery works.
        var identifier = parsePageID(result.name).id;
        getInternetArchiveMetadata(identifier, args, function( ){
            let id = jqEsc(identifier);
            // populate any missing data
            $("." + id + "-meta-name").html(args.metadata.title || args.metadata.TEI || result.name);
            // using toString() because there can be more than one author.
            if (_.isUndefined(args.metadata.creator)){
                $("." + id + "-meta-author").html('unknown');
            } else {
                $("." + id + "-meta-author").html(args.metadata.creator.toString());
            }
            if (!_.isUndefined(args.metadata.date)) {
                $("." + id + "-meta-published").html(args.metadata.date)
            }
        });
    }

    if (isLoggedIn()) {
        setUserRatingsHTML(name);
    }

    $("#" + id).data("metadata", result.meta);
    $("#" + id).data("kind", result.viewKind);
    $("#" + id).data("new-labels", result.newLabels);

    // TODO ?? book specific - should be in internetArchive.js
    var docType = guessKind(name);
    if (docType == 'ia-books') {
        resDiv.addClass('book-result');
    }
    if (docType == 'ia-pages') {
        resDiv.addClass('page-result');
    }
    if (docType == 'ia-notes') {
        resDiv.addClass('note-result');
    }

    resDiv.addClass('result-' + result.rank);
    html = '';
    // if they search a subcorpus for just books with a blank query, "search pages" doesn't make sense
    if (result.viewKind == 'ia-books' && queryBox.val().length > 0) {
        html += '<div  id="search-pages-link-' + name + '" class="search-pages-link" >';
        html += '<a href="#" onclick="UI.getPages(\'' + name + '\');"><span class="glyphicon glyphicon-collapse-down"></span>&nbsp;Show matching pages in this book...</a></div>';
        html += '<div id="page-results-' + name + '"></div>';
    }
    html += '<div ';
    if (UI.settings.show_dups == false) {
        html += 'style="display: none;" ';
    }
    html += 'class="result-dups-' + result.rank + '"></div>';

    $('#notes-' + id).after(html);

};

UI.appendResults = function(queryTerms, results, queryid) {

    _(results).forEach(function(result) {
        UI.renderSingleResult(result, queryTerms, false, queryid);
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

UI.showHideMetadata = function() {
    if (metadataDiv.is(':visible') == true) {
        metadataDiv.hide();
        $(".facets-hr").hide();
    } else {
        metadataDiv.show();
        $(".facets-hr").show();
    }

};

UI.getPages = function(bookid) {

    var terms = (UI.getQuery().trim()).toLowerCase();

    // passing in an archiveid will limit the search to that book
    $("#search-pages-link-" + bookid).html($("#search-pages-link-" + bookid).html() + '&nbsp;&nbsp;<img src="/images/more-loader.gif"\>');
    doActionSearchPages({kind: 'ia-pages', q: terms, action: "search", n: 1000, archiveid: bookid, workingSetQuery: true});

};

UI.hidePages = function(bookid) {

    $("#page-results-" + bookid).hide("slow");

    var html = '<a href="#" onclick="UI.showPages(\'' + bookid + '\');"><span class="glyphicon glyphicon-collapse-down"></span>';
    html += '&nbsp;Show pages (' + $("#search-pages-link-" + bookid).data('num_results') + ')</a>';

    $("#search-pages-link-" + bookid).html(html)
};

UI.showPages = function(bookid) {

    $("#page-results-" + bookid).show("slow");

    // TODO  - duplicate code with getPages

    var html = '<a href="#" onclick="UI.hidePages(\'' + bookid + '\');"><span class="glyphicon glyphicon-collapse-up"></span>';
    html += '&nbsp;Hide pages (' + $("#search-pages-link-" + bookid).data('num_results') + ')</a>';

    $("#search-pages-link-" + bookid).html(html);

};

var init = true;


function renderRatingsSidebar(id) {

    // we already have this info in the document list - although it is hidden -
    // so grab it and display it here
    var rating_html = $('#' + id + '-user-ratings-w-names').html();
    if (_.isUndefined(rating_html) || rating_html.length == 0) {
        rating_html = '<span>No labels yet for this document</span>';
    }

    $("#ratings").html(rating_html);

}
function clearRatingsSidebar() {

    $("#ratings").html('');

}
function setUpMouseEvents() {

    $(".result").mouseenter(function(event) {

        renderRatingsSidebar(this.id);
        $(this).css("background", "lightyellow");

    });

    $(".result").mouseleave(function(event) {

        $(this).css("background", "");
        clearRatingsSidebar()
    });

}

function bindCorpusMenuClick() {

    $("ul#corpus-list.dropdown-menu li a").off("click");
    $("ul#corpus-list.dropdown-menu li a").on("click", function() {

        console.log("clicked: " + $(this).text());
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


function hideSideBar() {
    $("#search-results-left").hide();
    $('#sidebar-button').attr("src", "images/sidebar_expand.png");

    $("#search-results-right").removeClass("col-md-10");
    $("#search-results-right").addClass("col-md-12");

    showSideBarFlag = false;
    p = getURLParams();
    p = _.merge(p, {'showSideBar': '0'});
    pushURLParams(p);

}
function showSideBar() {
    $("#search-results-left").show();
    $('#sidebar-button').attr("src", "images/sidebar_shrink.png");

    $("#search-results-right").removeClass("col-md-12");
    $("#search-results-right").addClass("col-md-10");

    showSideBarFlag = true;
    p = getURLParams();
    p = _.merge(p, {'showSideBar': '1'});
    pushURLParams(p);
}


function hideOCRSideBar() {
     $("#ocr-results-left").hide();
    $("#ocr-results-right").removeClass("col-md-9");
    $("#ocr-results-right").addClass("col-md-11");

    $('#ocr-sidebar-button').attr("src", "images/sidebar_expand.png");

    showSideBarFlag = false;

    // when the OCR text is longer than the page image, showing/hiding
    // the side bar can result in the book "jumping" to a different
    // page. So we'll make sure we go back to the page we were viewing.
    scrollToPage($(".ocr-current-page").attr("id").split("thumbnail-image-")[1]);

}
function showOCRSideBar() {
    $("#ocr-results-left").show();

    $("#ocr-results-right").removeClass("col-md-11");
    $("#ocr-results-right").addClass("col-md-9");

    $('#ocr-sidebar-button').attr("src", "images/sidebar_shrink.png");

    showSideBarFlag = true;

    scrollToPage($(".ocr-current-page").attr("id").split("thumbnail-image-")[1]);

}

$('#sidebar-button').click(function() {
    if (showSideBarFlag == true) {
        hideSideBar();
    } else {
        showSideBar();
    }
});


$('#ocr-sidebar-button').click(function() {
    if (showSideBarFlag == true) {
        hideOCRSideBar();
    } else {
        showOCRSideBar();
    }
});



UI.appendToCorpusList = function(corpusName) {
    $("#corpus-list-divider").before('<li><a href="#">' + corpusName + '</a></li>');
};

UI.updateCorpusListButton = function() {

    if (localStorage["corpora"] == null)
        return;

    var corpora = JSON.parse(localStorage["corpora"]);
    _.forEach(corpora, function(c) {
        UI.appendToCorpusList(c.name);
    });

};

function handleNERHilightClick(that, type) {

    var el = $('.' + type + '-class');

    if (that.checked == true) {
        el.removeClass(type + "-off");
        el.addClass(type + "-on")
    } else {
        el.addClass(type + "-off");
        el.removeClass(type + "-on")
    }

}

UI.toggleNotes = function(noteDivID) {


    $('#notes-div-' + noteDivID).toggle();
    var html = '';

    if ($('#notes-div-' + noteDivID).is(":visible") == true) {
        html = '<span class="glyphicon glyphicon-collapse-up"></span>&nbsp;Hide notes&nbsp;</span>';
    } else {
        html = '<span class="glyphicon glyphicon-collapse-down"></span>&nbsp;Show notes&nbsp;</span>';
    }

    $("#notes-link-" + noteDivID).html(html);

};

UI.toggleDups = function(clazz, rank) {
    $('.' + clazz).toggle();
    UI.setDupLinkHTML(rank);
};

UI.setDupLinkHTML = function(rank) {

    var upOrDown = 'down';
    var showOrHide = 'Show';

    if ($(".result-dups-" + rank).is(":visible")) {
        upOrDown = 'up';
        showOrHide = 'Hide';
    }

    var html = '<a href="#" onclick="UI.toggleDups(\'result-dups-' + rank + '\',' + rank + ');">';
    html += '<span class="glyphicon glyphicon-collapse-' + upOrDown + '"></span>';
    html += '&nbsp;' + showOrHide + ' duplicates&nbsp;(' + $(".result-dups-" + rank + " .dup-result").length + ')&nbsp;<span class="fa fa-files-o"></span></a>';
    $("#dup-parent-" + rank).html(html);

};

UI.showHideDups = function() {
    var el = $('#hide-dups input[type="checkbox"]:checked');
    if (el.length == 0) {
        $("#results .dup-result").show("slow");
    } else {
        $("#results .dup-result").hide("slow");
    }
};

UI.onClickSubcorpus = function() {

    UI.onClickSearchButton();
};

UI.enableSearchButtons = function(state) {
    $('input[name=search-kind]').attr('disabled', !state);
    $('#facets input[type=checkbox]').attr('disabled', !state);
    $('#clear-all-facets').attr('disabled', !state);
    $('#show-overlap').attr('disabled', !state);
    // only enable the overlap button if at least on subcorpus is selected
    if (getSubcorporaElements().length == 0) {
        $('#show-overlap').attr('checked', false);
        $('#show-overlap').attr('disabled', true);
    }
};

/*
 
 UI.showOverlap = function( ){
 if ($("#show-overlap").is(':checked')){
 $(".result").hide();
 
 $('.result').each(function () {
 var ar = this.id;
 
 if (Object.keys($('#' + ar).data("new-labels")).length > 1){
 $('#' + ar).show();
 }
 });
 } else {
 $(".result").show();
 }
 };
 */

UI.checkSettings = function() {
    // makse sure we have defaults
    if (_.isUndefined(UI.settings.show_dups)) {
        UI.settings.show_dups = false;
    }
    if (_.isUndefined(UI.settings.show_notes)) {
        UI.settings.show_notes = false;
    }
    if (_.isUndefined(UI.settings.show_found_with_query)) {
        UI.settings.show_found_with_query = false;
    }
    if (_.isUndefined(UI.settings.show_unigrams)) {
        UI.settings.show_unigrams = true;
    }
    if (_.isUndefined(UI.settings.use_query_builder)) {
        UI.settings.use_query_builder = false;
    }

    document.cookie = "settings=" + JSON.stringify(UI.settings) + ";";
};


function clearQueryBuilder() {
    $("#query-builder-link").hide();
    $("#show-corpus-terms").hide();
    $(".query-term-buttons").html(''); // clear any prior terms
    $("#high-tf").html('');
    $("#high-snippettf").html('');
    $("#high-bigrams").html('');
    $("#high-trigrams").html('');
    $("#high-entity-fields").html('');
    $("#query-builder-query").html('');
}

UI.searchWithinBook = function(bookid) {

    var terms = (UI.getQuery().trim()).toLowerCase();
    // pass in archiveid to restrict the search to just this book.
    doSearchWithinBookRequest({kind: 'ia-pages', q: terms, action: "search", n: 1000, archiveid: bookid, workingSetQuery: true});

};