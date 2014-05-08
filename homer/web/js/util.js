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
  var urlParams = "?"+_.pairs(params).filter(function(kv) {
    var key = kv[0];
    return !(key == "user" || key == "token");
  }).map(function(kv) {
    return _(kv).map(encodeURIComponent).join('=');
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
  return _(words).map(function (word) {
    if (_.contains(queryTerms, word)) {
      return beforeTag + word + afterTag;
    }
    return word;
  }).join(' ');
};
