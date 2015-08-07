/*
 Annotator view panel Plugin v1.0 (https://https://github.com/albertjuhe/annotator_view/)
 Copyright (C) 2014 Albert Juhé Brugué
 License: https://github.com/albertjuhe/annotator_view/License.rst

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

/**
 * Heavily modified by michaelz on 7/2015.
 */

var __bind = function (fn, me) {
          return function () {
            return fn.apply(me, arguments);
          };
        },
        __hasProp = {}.hasOwnProperty,
        __extends = function (child, parent) {
          for (var key in parent) {
            if (__hasProp.call(parent, key)) child[key] = parent[key];
          }
          function ctor() {
            this.constructor = child;
          }

          ctor.prototype = parent.prototype;
          child.prototype = new ctor();
          child.__super__ = parent.prototype;
          return child;
        };

//constants
var IMAGE_DELETE = '../images/icono_eliminar.png',
        IMAGE_EDIT = '../images/edit-icon.png';

Annotator.Plugin.AnnotatorViewer = (function (_super) {
  __extends(AnnotatorViewer, _super);

  AnnotatorViewer.prototype.events = {

    'noteViewerCreateEventName': 'onAnnotationCreated',
    'noteViewerUpdateEventName': 'onAnnotationUpdated',
    'noteViewerDeleteteEventName': 'onAnnotationDeleted'

  };

  AnnotatorViewer.prototype.field = null;

  AnnotatorViewer.prototype.input = null;

  AnnotatorViewer.prototype.options = {
    AnnotatorViewer: {}
  };


  function AnnotatorViewer(element, options) {

    this.onCancelPanel = __bind(this.onCancelPanel, this);
    this.onSavePanel = __bind(this.onSavePanel, this);
    this.onAnnotationUpdated = __bind(this.onAnnotationUpdated, this);

    AnnotatorViewer.__super__.constructor.apply(this, arguments);

    that = this
    // only load the panel once
    if (($("#annotations-panel").length == 0)) {
      $("#results-right").append(this.createAnnotationPanel());
      $(".container-anotacions").toggle();
      $("#annotations-panel").click(function (event) {
        $(".container-anotacions").toggle("slide");
      });
      //Annotation scroll
      $('#anotacions-uoc-panel').slimscroll({height: '97%'});
    }

  };

  AnnotatorViewer.prototype.pluginInit = function () {
    if (!Annotator.supported()) {
      return;
    }

    var that = this
    this.annotator.subscribe("myannotationsLoaded", function (annotation) {
      if (_.isUndefined(annotation))
        return;

      var annotation;

      _.forEach(annotation.notes, function (a) {
        that.createReferenceAnnotation(a);
      })
      $('#count-anotations').text($(".container-anotacions").find('.annotator-marginviewer-element').length);
    })

  };

  AnnotatorViewer.prototype.onDeleteClick = function (event) {
    event.stopPropagation();
    if (confirm("Do you want to delete this annotation?")) {
      this.click;
      return this.onButtonClick(event, 'delete');
    }
    return false;
  };

  AnnotatorViewer.prototype.onEditClick = function (event) {
    event.stopPropagation();
    return this.onButtonClick(event, 'edit');
  };

  AnnotatorViewer.prototype.onButtonClick = function (event, type) {
    var item;
    //item contains all the annotation information, this information is stored in an attribute called data-annotation.
    item = $(event.target).parents('.annotator-marginviewer-element');
    if (type == 'delete') {
      // MCZ - we need to use the annotator specific for that note
      var current_annotation = item.data('annotation');
      var parts = current_annotation.uri.split("_")
      var el = "#" + getNotesID(parts[0], parts[1]);
      $(el).trigger("myannotationDeleted", current_annotation)
      this.onAnnotationDeleted(current_annotation)
      return;// this.annotator.deleteAnnotation(item.data('annotation'));
    }
    if (type == 'edit') { //We want to transform de div to a textarea
      //Find the text field
      var annotator_textArea = item.find('div.anotador_text');
      this.textareaEditor(annotator_textArea, item.data('annotation'));

    }
  };

  //Textarea editor controller
  AnnotatorViewer.prototype.textareaEditor = function (annotator_textArea, item) {
    //First we have to get the text, if no, we will have an empty text area after replace the div
    if ($('li#annotation-' + item.id).find('textarea.panelTextArea').length == 0) {
      var content = item.text;
      var editableTextArea = $("<textarea id='textarea-" + item.id + "'' class='panelTextArea'>" + content + "</textarea>");
      var annotationCSSReference = 'li#annotation-' + item.id + ' > div.annotator-marginviewer-text';

      annotator_textArea.replaceWith(editableTextArea);
      editableTextArea.css('height', editableTextArea[0].scrollHeight + 'px');
      editableTextArea.blur(); //Textarea blur
      if (typeof(this.annotator.plugins.RichEditor) != 'undefined') {
        this.tinymceActivation(annotationCSSReference + ' > textarea#textarea-' + item.id);
      }
      $('<div class="annotator-textarea-controls annotator-editor"></div>').insertAfter(editableTextArea);
      var control_buttons = $(annotationCSSReference + '> .annotator-textarea-controls');
      $('<a href="#save" class="annotator-panel-save">Save</a>').appendTo(control_buttons).bind("click", {annotation: item}, this.onSavePanel);
      $('<a href="#cancel" class="annotator-panel-cancel">Cancel</a>').appendTo(control_buttons).bind("click", {annotation: item}, this.onCancelPanel);
    }
  };

  AnnotatorViewer.prototype.tinymceActivation = function (selector) {
    tinymce.init({
      selector: selector,
      plugins: "media image insertdatetime link paste",
      menubar: false,
      statusbar: false,
      toolbar_items_size: 'small',
      extended_valid_elements: "",
      toolbar: "undo redo bold italic alignleft aligncenter alignright alignjustify | link image media"
    });
  }

  //Event triggered when save the content of the annotation
  AnnotatorViewer.prototype.onSavePanel = function (event) {

    var current_annotation = event.data.annotation;
    var textarea = $('li#annotation-' + current_annotation.id).find("#textarea-" + current_annotation.id);
    if (typeof(this.annotator.plugins.RichEditor) != 'undefined') {
      current_annotation.text = tinymce.activeEditor.getContent();
      tinymce.remove("#textarea-" + current_annotation.id);
      tinymce.activeEditor.setContent(current_annotation.text);
    } else {
      current_annotation.text = textarea.val();
    }
    var anotation_reference = "annotation-" + current_annotation.id;

    $('#' + anotation_reference).data('annotation', current_annotation);

    // MCZ - we need to use the annotator specific for that note
    var parts = current_annotation.uri.split("_")
    var el = "#" + getNotesID(parts[0], parts[1]);
    $(el).trigger("myannotationUpdated", current_annotation)

  };

  //Event triggered when save the content of the annotation
  AnnotatorViewer.prototype.onCancelPanel = function (event) {
    var current_annotation = event.data.annotation;
    var styleHeight = 'style="height:12px"';
    if (current_annotation.text.length > 0) styleHeight = '';

    if (typeof(this.annotator.plugins.RichEditor) != 'undefined') {
      tinymce.remove("#textarea-" + current_annotation.id);

      var textAnnotation = '<div class="anotador_text" ' + styleHeight + '>' + current_annotation.text + '</div>';
      //       var anotacio_capa =  '<div class="annotator-marginviewer-text"><div class="'+current_annotation.category+' anotator_color_box"></div>'+ textAnnotation  + '</div>';
      var anotacio_capa = '<div class="annotator-marginviewer-text">' + textAnnotation + '</div>';
      var textAreaEditor = $('li#annotation-' + current_annotation.id + ' > .annotator-marginviewer-text');

      textAreaEditor.replaceWith(anotacio_capa);
    } else {
      var textarea = $('li#annotation-' + current_annotation.id).find('textarea.panelTextArea');
      this.normalEditor(current_annotation, textarea);
    }

  };

  //Annotator in a non editable state
  AnnotatorViewer.prototype.normalEditor = function (annotation, editableTextArea) {
    var buttons = $('li#annotation-' + annotation.id).find('div.annotator-textarea-controls');
    var textAnnotation = this.removeTags('iframe', annotation.text);
    editableTextArea.replaceWith('<div class="anotador_text">' + textAnnotation + '</div>');
    buttons.remove();
  };


  AnnotatorViewer.prototype.onAnnotationCreated = function (annotation) {

    this.createReferenceAnnotation(annotation);

  };

  AnnotatorViewer.prototype.onAnnotationUpdated = function (annotation) {

    $("#annotation-" + annotation.id).html(this.mascaraAnnotation(annotation));
  };

  AnnotatorViewer.prototype.onAnnotationsLoaded = function (annotations) {

    var annotation;
     if (annotations.length > 0) {
      for (i = 0, len = annotations.length; i < len; i++) {
        annotation = annotations[i];
        this.createReferenceAnnotation(annotation);
      }

    }
    $('#count-anotations').text($(".container-anotacions").find('.annotator-marginviewer-element').length);

  };

  AnnotatorViewer.prototype.onAnnotationDeleted = function (annotation) {

    $("li").remove("#annotation-" + annotation.id);
    $('#count-anotations').text($(".container-anotacions").find('.annotator-marginviewer-element').length);

  };

  AnnotatorViewer.prototype.mascaraAnnotation = function (annotation) {

    if (!annotation.data_creacio) annotation.data_creacio = $.now();


    var shared_annotation = "";
    var class_label = "label";
    var delete_icon = '';

    // only allow edit/delete if it's our note
    if (getUser() == annotation.user) {
      delete_icon = "<img src=\"" + IMAGE_DELETE
      + "\" class=\"annotator-viewer-delete\" title=\"Delete\" style=\" float:right;margin-top:3px;;margin-left:5px\"/><img src=\""
      + IMAGE_EDIT + "\"   class=\"annotator-viewer-edit\" title=\"Edit\" style=\"float:right;margin-top:3px\"/>";
    }
    //    if (annotation.estat == 1 || annotation.permissions.read.length === 0) {
    //      shared_annotation = "<img src=\"" + SHARED_ICON + "\" title=\"Share\" style=\"margin-left:5px\"/>"
    //    }

    if (annotation.propietary == 0) {
      class_label = "label-compartit";
      delete_icon = "";
    }

    //If you have instal.led a plug-in for categorize anotations, panel viewer can get this information with the category atribute
    if (annotation.category != null) {
      anotation_color = annotation.category;
    } else {
      anotation_color = "hightlight";
    }
    var textAnnotation = annotation.text;
    var annotation_layer = '<div class="annotator-marginviewer-text">';
     //    annotation_layer += '<div class="anotador_text">'+  textAnnotation  + '</div></div><div class="annotator-marginviewer-date">'+ $.format.date(annotation.data_creacio, "dd/MM/yyyy HH:mm:ss") + '</div><div class="annotator-marginviewer-quote">'+ annotation.quote + '</div><div class="annotator-marginviewer-footer"><span class="'+class_label+'">' + annotation.user + '</span>'+shared_annotation+delete_icon+'</div>';
    annotation_layer += '<div class="anotador_text">' + textAnnotation + '</div></div><div class="annotator-marginviewer-quote">'
    + annotation.quote + '</div><div class="annotator-marginviewer-footer"><span class="' + class_label + '">'
    + annotation.user.split("@")[0] + '</span>' + shared_annotation + delete_icon + '</div>';


    return annotation_layer;
  };

  AnnotatorViewer.prototype.createAnnotationPanel = function (annotation) {
    var checboxes = '';//<label class="checkbox-inline"><input type="checkbox" id="type_own" rel="me"/>My annotations</label><label class="checkbox-inline">  <input type="checkbox" id="type_share" rel="shared"/>Shared</label>';

    var annotation_layer = '<div  class="annotations-list-uoc" style="background-color:#ddd;"><div id="annotations-panel">'
            + '<span class="rotate" title="View annotations" style="padding:5px;background-color:#ddd;position: absolute; '
            + 'top:10em;left: -50px; width: 155px; height: 110px;cursor:pointer">View annotations<span class="label-counter" '
            + 'style="padding:0.2em 0.3em;float:right" id="count-anotations">0</span></span></div><div id="anotacions-uoc-panel" style="height:80%">'
            + '<ul class="container-anotacions"><li class="filter-panel">' + checboxes + '</li></ul></div></div>';

    return annotation_layer;
  };

  AnnotatorViewer.prototype.createReferenceAnnotation = function (annotation) {

    var anotation_reference = null;
    var data_owner = "me";
    var data_type = "";
    var myAnnotation = false;


    anotation_reference = "annotation-" + annotation.id;
    //We need to add this id to the text annotation
    $element = $('span.annotator-hl:not([data-annotation-id])');
    if ($element) {
      $element.attr('data-annotation-id', annotation.id);
    }

    if (annotation.estat == 1 || annotation.permissions.read.length === 0) {
      data_type = "shared";

    }
    if (annotation.propietary == 0) {
      data_owner = "";
    } else {
      myAnnotation = true;
    }

    var annotation_layer = '<li class="annotator-marginviewer-element ' + data_type + ' ' + data_owner + '" id="' + anotation_reference + '">' + this.mascaraAnnotation(annotation) + '</li>';
    //   var malert = i18n_dict.anotacio_lost

    anotacioObject = $(annotation_layer).appendTo('.container-anotacions').click(function (event) {

      if (event.target.className == "annotator-viewer-edit") {
        that.onEditClick(event);
        return;
      }
      if (event.target.className == "annotator-viewer-delete") {
        that.onDeleteClick(event);
        return;
      }
      var viewPanelHeight = jQuery(window).height();
      var annotation_reference = annotation.id;

      $element = jQuery("span[data-annotation-id=" + annotation.id + "]");

      //      if (!$element.length) {
      //        $element = jQuery("#" + annotation.order);
      //        annotation_reference = annotation.order; //If exists a sorted annotations we put it in the right order, using order attribute
      //      }

      if ($element.length) {
        elOffset = $element.offset();
        $("#annotation-" + annotation.id).children(".annotator-marginviewer-quote").toggle();

        var area = $('#results-right');
        area.animate({
          scrollTop: $("span[data-annotation-id=" + annotation.id + "]").offset().top - 60 - (area.offset().top - area.scrollTop())
        }, 150);
      }
    })
    .mouseover(function () {
      $element = jQuery("span[data-annotation-id=" + annotation.id + "]");
      if ($element.length) {
        $element.css({
          "border-color": "#000000",
          "border-width": "1px",
          "border-style": "solid"
        });
      }
    })
    .mouseout(function () {
      $element = jQuery("span[data-annotation-id=" + annotation.id + "]");
      if ($element.length) {
        $element.css({
          "border-width": "0px"
        });
      }
    });

    //Adding annotation to data element for delete and link
    $('#' + anotation_reference).data('annotation', annotation);
    $(anotacioObject).fadeIn('fast');

    $('#count-anotations').text($(".container-anotacions").find('.annotator-marginviewer-element').length);
  };
  //Strip content tags
  AnnotatorViewer.prototype.removeTags = function (striptags, html) {
    striptags = (((striptags || '') + '').toLowerCase().match(/<[a-z][a-z0-9]*>/g) || []).join('');
    var tags = /<\/?([a-z][a-z0-9]*)\b[^>]*>/gi, commentsAndPhpTags = /<!--[\s\S]*?-->|<\?(?:php)?[\s\S]*?\?>/gi;

    return html.replace(commentsAndPhpTags, '').replace(tags, function ($0, $1) {
      return html.indexOf('<' + $1.toLowerCase() + '>') > -1 ? $0 : '';
    });
  };


  return AnnotatorViewer;

})(Annotator.Plugin);
