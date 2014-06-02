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
    var urlParams = "?" + _(params).map(function(val, key) {
        return encodeURIComponent(key) + "=" + encodeURIComponent(val);
    }).join('&');

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
        urlParams[key] = value;
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
    var selectedLabels = [];
    var i = 0; // counter to get uniq elements
    // get all the elements with the class "proteus-labels"
    $(".proteus-labels").each(function() {
        var type = $(this).attr("name");

        // have to go through by index. Ideally we'd the TYPE as an identifier,
        // but they can have spaces
        var selected = $($("#multiselect-" + i), 'option:selected');
        for (var value in selected.val()) {
            //console.log(selected.val()[value]);
            selectedLabels.push(type + ":" + selected.val()[value]);
        }
        i += 1;
    });
    //console.log(JSON.stringify(selectedLabels));

    getResourcesForLabels(selectedLabels);

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