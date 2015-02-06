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
        urlParams += "&labelOwner=" + getCookie("userid");
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

            labels.push(node.key);
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
// users are allowed to enter labels w/o at TYPE, this is troublesome later when
// we want to allow them to select type/values to filter/search by/etc. So we'll store
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
function formatLabelForDispaly(origLabel) {

    if (origLabel.substring(0, 2) === NO_TYPE_CONST() + ":") {
        var label = origLabel.substr(2).split("@");
        return  label[0] + " (" + label[1] + ")";
    }
    var label = origLabel.split("@");
    return  label[0] + " (" + label[1] + ")";

}
function isLoggedIn() {
    return Model.user != null;
}