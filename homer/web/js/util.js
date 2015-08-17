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

var removeURLParam = function(param){

    var p =  getURLParams();

    if (!_.isUndefined(p[param])){
        delete p[param]
        pushURLParams(p);
    }

}

// from: http://www.w3schools.com/js/js_cookies.asp
var getCookie = function(cname) {
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

var getUser = function(){
    return getCookie("username")
}
var highlightText = function(queryTerms, text, beforeTag, afterTag) {

    // there are situations where the "text" is actually an image such
    // as a thumbnail so skip those.
    if (text.toString().substring(0, 4) == "<img")
        return text;

    // remove any punctuation. For papers the authors are listed
    // spearated by semicolons.
    // Note we use toString(), because if a string like "2014" is passed
    // in, JS will think it's a numeric datatype.
    text = text.toString().replace(/[\.,-\/#!$%\^&\*;:{}=\-_`~()]/g, "");
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

function TREE_KEY_SEP(){ return "\t";}

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
    if (_.isUndefined(_rating[0])){
      _rating = rating; // they didn't pass in colon sep values
    }
    if (origLabel.substring(0, 2) === NO_TYPE_CONST() + ":") {
        var label = origLabel.split(":");
        return  label[1] + " (" + _rating + ")";
    }

    return  origLabel + " (" + _rating + ")";

}
function isLoggedIn() {
    return (getCookie("username") !== "") ;
}

function enableAutoRetrieve() {

  $('#results-right').bind('scroll', function() {
    if($(this).scrollTop() + $(this).innerHeight() >= this.scrollHeight) {
      var prev = Model.request;
      prev.skip = Model.results.length;
      doSearchRequest(prev);
    }
  });
}

function disableAutoRetrieve(){
  $('#results-right').unbind('scroll');
}

// mimic Google's URL redirect
rwt = function (a, rank) {
    try {
        var origURL = escape(a.href);
        var token = "&token=" + getCookie("token");
        a.href = "/url?url=" + origURL + token + "&rank=" + rank;
        a.onmousedown = ""
    } catch (o) {
    }
    return 1
};


function getCorpusID(corpusName){
    // TODO: has to be a better way to do this - just brute force it for now...
    var corpora = JSON.parse(localStorage["corpora"]);
    var corpID = -1;
    _.forEach(corpora, function(c){
        if (c.name == corpusName)
            corpID = parseInt(c.id);
    });
    return corpID;
}

jsonp_handler = function(data)
{
    console.log(data);
    var html = $("#important-entities").html();
    var wikipedia = '';


    _.forEach(data.query.pages, function(pg){
        if (!_.isUndefined(pg.pageid)){
            wikipedia += ' <a target="_blank" href="https://en.wikipedia.org/wiki/' + pg.title + '">' + pg.title + '</a>';
        }

    });

    if (wikipedia.length > 0)
        $("#important-entities").html(html + " (" + wikipedia +  ")");

}

function initImportantEntities() {
    $("#important-entities").droppable({
        drop: function (event, ui) {
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

            var query =  encodeURI(ui.draggable[0].textContent) + "|" + encodeURI(ui.draggable[0].textContent.capitalizeEachWord());
            var script = document.createElement('script');
            script.src = 'http://en.wikipedia.org/w/api.php?action=query&callback=jsonp_handler&format=json&titles=' + query;

           // script.src = 'http://en.wikipedia.org/w/api.php?action=query&callback=jsonp_handler&prop=images&imlimit=20&format=json&titles=' + encodeURI(ui.draggable[0].textContent);
            //      script.src = 'http://en.wikipedia.org/w/api.php?action=query&callback=jsonp_handler&list=retrieval&limit=20&format=json&srsearch=' + encodeURI(ui.draggable[0].textContent);

            document.head.appendChild(script);


        },
        over: function () {
            $(this).css("background-color", "lightgrey");
        },
        out: function () {
            $(this).css("background-color", "");
        }

    });
}

String.prototype.capitalizeEachWord = function() {
    return this.replace(/(?:^|\s)\S/g, function(a) { return a.toUpperCase(); });
};

function getNotesID(pageid, pagenum) {
    if (!_.isUndefined(pagenum) && parseInt(pagenum) >= 0)
        return 'notes-' + pageid + '_' + pagenum;
    else
        return 'notes-' + pageid;
}

