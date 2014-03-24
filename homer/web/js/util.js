/**
 * util.js
 *
 * For Javascript shims and other things that don't have a clear project-specific home.
 */

if(!String.prototype.trim) {
  String.prototype.trim=function(){return this.replace(/^\s+|\s+$/g, '');};
}

var isBlank = function(str) {
  return _.isEmpty(str.trim());
};

var getURLParams = function () {
  var match,
  pl     = /\+/g,  // Regex for replacing addition symbol with a space
  search = /([^&=]+)=?([^&]*)/g,
  decode = function (s) { return decodeURIComponent(s.replace(pl, " ")); },
  query  = window.location.search.substring(1);

  var urlParams = {};
  while ((match = search.exec(query))) {
    urlParams[decode(match[1])] = decode(match[2]);
  }
  return urlParams;
};

