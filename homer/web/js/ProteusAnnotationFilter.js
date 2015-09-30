var bind = function (fn, me) {
          return function () {
            return fn.apply(me, arguments);
          };
        },
        extend = function (child, parent) {
          for (var key in parent) {
            if (hasProp.call(parent, key)) child[key] = parent[key];
          }
          function ctor() {
            this.constructor = child;
          }

          ctor.prototype = parent.prototype;
          child.prototype = new ctor();
          child.__super__ = parent.prototype;
          return child;
        },
        hasProp = {}.hasOwnProperty;

// custom plug-in that extends the regular Filter plug-in
Annotator.Plugin.ProteusAnnotationFilter = (function (superClass) {
  extend(ProteusAnnotationFilter, Annotator.Plugin.Filter);

  ProteusAnnotationFilter.prototype.options = {

    searchArea: '#results-right',
    appendTo: 'body',
    filters: [],
    addAnnotationFilter: true,
    isFiltered: function (input, property) {
      var i, keyword, len, ref;
      if (!(input && property)) {
        return false;
      }
      ref = input.split(/\s+/);
      for (i = 0, len = ref.length; i < len; i++) {
        keyword = ref[i];
        if (property.indexOf(keyword) === -1) {
          return false;
        }
      }
      return true;
    }
  };

  function ProteusAnnotationFilter(element, options) {

    ProteusAnnotationFilter.__super__.constructor.call(this, element, options);

  }

  ProteusAnnotationFilter.prototype._setupListeners = function () {

    // subscribe to an event that each annotator will publish letting us
    // know we need to update the list of highlights. This is needed because
    // the stock events like annotationCreated are only subscribed to within the
    // individual annotator which we do per page.
    this.annotator.subscribe("noteUpdate", this.updateHighlights);
    return this;
  };


  ProteusAnnotationFilter.prototype.updateHighlights = function () {
    this.highlights = $(this.options.searchArea).find('.annotator-hl:visible');
    // original:    this.highlights = this.annotator.element.find('.annotator-hl:visible');
    return this.filtered = this.highlights.not(this.classes.hl.hide);
  };

  ProteusAnnotationFilter.prototype._scrollToHighlight = function (highlight) {
    highlight = $(highlight);
    this.highlights.removeClass(this.classes.hl.active);
    highlight.addClass(this.classes.hl.active);

    // if the highlights are in an inner html element we need to adjust for that as mentioned shown in
    // http://stackoverflow.com/questions/23777640/scrolltop-offset-not-animating-working-correctly-with-divs-inside-of-container
    var area = $(this.options.searchArea);
    area.animate({
      scrollTop: highlight.offset().top - 60 - (area.offset().top - area.scrollTop())
    }, 150);
  };

  return ProteusAnnotationFilter;

})(Annotator.Plugin);
