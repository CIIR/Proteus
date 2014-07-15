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
    var words = text.split(/\s/);
    return _(words).map(function(word) {
        if (_.contains(queryTerms, word)) {
            return beforeTag + word + afterTag;
        }
        return word;
    }).join(' ');
};

function getSelectedLabels() {

    //  console.log("LABELS: " + $('#multiselect-all').val());
    return($('#multiselect-all').val());

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
function formatLabelForDatabase(origLabel) {

    if (origLabel.indexOf(":") === -1) {
        return NO_TYPE_CONST() + ":" + origLabel;
    }

    return origLabel;
}
// when displaying tags, don't show the "*:"
function formatLabelForDispaly(origLabel) {

    if (origLabel.substring(0, 2) === NO_TYPE_CONST() + ":") {
        return  origLabel.substr(2);
    }
    return origLabel;
}
