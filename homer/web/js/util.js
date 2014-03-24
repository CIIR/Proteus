/**
 * util.js
 *
 */

if(!String.prototype.trim) {
  String.prototype.trim=function(){return this.replace(/^\s+|\s+$/g, '');};
}

var isBlank = function(str) {
  return _.isEmpty(str.trim());
};

