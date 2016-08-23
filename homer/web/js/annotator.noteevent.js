/**
 * Created by michaelz on 8/22/2016.
 */


  // MCZ: We have an annotator for each page, and the filter (ProteusAnnotationFilter)
  // and the note side bar annotators apply to the whole book, we need to forward
  // events that happen on the page to the filter and sidebar and anything that
  // happens in the sidebar has to be forwarded to the page annotator.

Annotator.Plugin.NoteEvent = function (element, reorderAll) {
  var plugin = {};

  plugin.pluginInit = function () {

    var that = this;
    this.annotator
            .subscribe("afterAnnotationCreated", function (annotation) {
              noteSideBarDiv.trigger("noteViewerCreateEventName", annotation);
            })
            .subscribe("annotationCreated", function (annotation) {
              noteFilterDiv.trigger("noteUpdate");
            })
            .subscribe("annotationUpdated", function (annotation) {
              noteSideBarDiv.trigger("noteViewerUpdateEventName", annotation);
            })
            .subscribe("annotationsLoaded", function (annotation) {
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
            .subscribe("annotationDeleted", function (annotation) {
              noteFilterDiv.trigger("noteUpdate");
              noteSideBarDiv.trigger("noteViewerDeleteteEventName", annotation);
            })
      // these events are triggered from the note side bar and
      // are forwarded to the page.
            .subscribe("myannotationUpdated", function (annotation) {
              that.annotator.updateAnnotation(annotation);
            })
            .subscribe("myannotationDeleted", function (annotation) {
              that.annotator.deleteAnnotation(annotation);
            });
  };
  return plugin;

};

