/*
 * jQuery UI Tag-it!
 *
 * @version v2.0 (06/2011)
 *
 * Copyright 2011, Levy Carneiro Jr.
 * Released under the MIT license.
 * http://aehlke.github.com/tag-it/LICENSE
 *
 * Homepage:
 *   http://aehlke.github.com/tag-it/
 *
 * Authors:
 *   Levy Carneiro Jr.
 *   Martin Rehfeld
 *   Tobias Schmidt
 *   Skylar Challand
 *   Alex Ehlke
 *
 * Maintainer:
 *   Alex Ehlke - Twitter: @aehlke
 *
 * Dependencies:
 *   jQuery v1.4+
 *   jQuery UI v1.8+
 */
(function($) {

    $.widget('ui.tagit', {
        options: {
            allowDuplicates: false,
            caseSensitive: false,
            fieldName: 'tags',
            placeholderText: null, // Sets `placeholder` attr on input field.
            readOnly: false, // Disables editing.
            removeConfirmation: false, // Require confirmation to remove tags.
            tagLimit: null, // Max number of tags allowed (null for unlimited).

            // Used for autocomplete, unless you override `autocomplete.source`.
            availableTags: [],
            // Use to override or add any options to the autocomplete widget.
            //
            // By default, autocomplete.source will map to availableTags,
            // unless overridden.
            autocomplete: {},
            // Shows autocomplete before the user even types anything.
            showAutocompleteOnFocus: false,
            // When enabled, quotes are unneccesary for inputting multi-word tags.
            allowSpaces: true,
            // The below options are for using a single field instead of several
            // for our form values.
            //
            // When enabled, will use a single hidden field for the form,
            // rather than one per tag. It will delimit tags in the field
            // with singleFieldDelimiter.
            //
            // The easiest way to use singleField is to just instantiate tag-it
            // on an INPUT element, in which case singleField is automatically
            // set to true, and singleFieldNode is set to that element. This
            // way, you don't need to fiddle with these options.
            singleField: false,
            // This is just used when preloading data from the field, and for
            // populating the field with delimited tags as the user adds them.
            singleFieldDelimiter: ',',
            // Set this to an input DOM node to use an existing form field.
            // Any text in it will be erased on init. But it will be
            // populated with the text of tags as they are created,
            // delimited by singleFieldDelimiter.
            //
            // If this is not set, we create an input node for it,
            // with the name given in settings.fieldName.
            singleFieldNode: null,
            // Whether to animate tag removals or not.
            animate: true,
            // Optionally set a tabindex attribute on the input that gets
            // created for tag-it.
            tabIndex: null,
            // Event callbacks.
            beforeTagAdded: null,
            afterTagAdded: null,
            beforeTagRemoved: null,
            afterTagRemoved: null,
            onTagClicked:  null,
            onTagLimitExceeded: null,
            // DEPRECATED:
            //
            // /!\ These event callbacks are deprecated and WILL BE REMOVED at some
            // point in the future. They're here for backwards-compatibility.
            // Use the above before/after event callbacks instead.
            onTagAdded: null,
            onTagRemoved: null,
            // `autocomplete.source` is the replacement for tagSource.
            tagSource: null
                    // Do not use the above deprecated options.
        },
        _create: function() {
            // for handling static scoping inside callbacks
            var that = this;
            // There are 2 kinds of DOM nodes this widget can be instantiated on:
            //     1. UL, OL, or some element containing either of these.
            //     2. INPUT, in which case 'singleField' is overridden to true,
            //        a UL is created and the INPUT is hidden.
            if (this.element.is('input')) {
                this.tagList = $('<ul></ul>').insertAfter(this.element);
                this.options.singleField = true;
                this.options.singleFieldNode = this.element;
                this.element.addClass('tagit-hidden-field');
            } else {
                this.tagList = this.element.find('ul, ol').andSelf().last();
            }

            this.tagInput = $('<input type="text" />').addClass('ui-widget-content');
            if (this.options.readOnly)
                this.tagInput.attr('disabled', 'disabled');
            if (this.options.tabIndex) {
                this.tagInput.attr('tabindex', this.options.tabIndex);
            }

            if (this.options.placeholderText) {
                this.tagInput.attr('placeholder', this.options.placeholderText);
            }

            if (!this.options.autocomplete.source) {
                this.options.autocomplete.source = function(search, showChoices) {
                    var filter = search.term.toLowerCase();
                    var choices = $.grep(this.options.availableTags, function(element) {
                        // Only match autocomplete options that begin with the search term.
                        // (Case insensitive.)
                        return (element.toLowerCase().indexOf(filter) === 0);
                    });
                    if (!this.options.allowDuplicates) {
                        choices = this._subtractArray(choices, this.assignedTags());
                    }
                    showChoices(choices);
                };
            }

            if (this.options.showAutocompleteOnFocus) {
                this.tagInput.focus(function(event, ui) {
                    that._showAutocomplete();
                });
                if (typeof this.options.autocomplete.minLength === 'undefined') {
                    this.options.autocomplete.minLength = 0;
                }
            }

            // Bind autocomplete.source callback functions to this context.
            if ($.isFunction(this.options.autocomplete.source)) {
                this.options.autocomplete.source = $.proxy(this.options.autocomplete.source, this);
            }

            // DEPRECATED.
            if ($.isFunction(this.options.tagSource)) {
                this.options.tagSource = $.proxy(this.options.tagSource, this);
            }

            this.tagList
                    .addClass('tagit')
                    .addClass('ui-widget ui-widget-content ui-corner-all')
                    // Create the input field.
                    .append($('<li class="tagit-new"></li>').append(this.tagInput))
                    .click(function(e) {
                        var target = $(e.target);
                        if (target.hasClass('tagit-label')) {
                            var tag = target.closest('.tagit-choice');
                            if (!tag.hasClass('removed')) {
                                that._trigger('onTagClicked', e, {tag: tag, tagLabel: that.tagLabel(tag)});
                            }
                        } else {
                            // Sets the focus() to the input field, if the user
                            // clicks anywhere inside the UL. This is needed
                            // because the input field needs to be of a small size.
                            that.tagInput.focus();
                        }
                    });
            // Single field support.
            var addedExistingFromSingleFieldNode = false;
            if (this.options.singleField) {
                if (this.options.singleFieldNode) {
                    // Add existing tags from the input field.
                    var node = $(this.options.singleFieldNode);
                    var tags = node.val().split(this.options.singleFieldDelimiter);
                    node.val('');
                    $.each(tags, function(index, tag) {
                        that.createTag(tag, null, true);
                        addedExistingFromSingleFieldNode = true;
                    });
                } else {
                    // Create our single field input after our list.
                    this.options.singleFieldNode = $('<input type="hidden" style="display:none;" value="" name="' + this.options.fieldName + '" />');
                    this.tagList.after(this.options.singleFieldNode);
                }
            }

            // Add existing tags from the list, if any.
            if (!addedExistingFromSingleFieldNode) {
                this.tagList.children('li').each(function() {
                    if (!$(this).hasClass('tagit-new')) {
                        that.createTag($(this).text(), $(this).attr('class'), true);
                        $(this).remove();
                    }
                });
            }

            // Events.
            this.tagInput
                    .keydown(function(event) {
                        // Backspace is not detected within a keypress, so it must use keydown.
                        if (event.which == $.ui.keyCode.BACKSPACE && that.tagInput.val() === '') {
                            var tag = that._lastTag();
                            // MCZ 2/2015 - don't allow delete if it's read only
                            if (tag.hasClass('tagit-choice-read-only'))
                                return;

                            if (!that.options.removeConfirmation || tag.hasClass('remove')) {
                                // When backspace is pressed, the last tag is deleted.
                                that.removeTag(tag);
                            } else if (that.options.removeConfirmation) {
                                tag.addClass('remove ui-state-highlight');
                            }
                        } else if (that.options.removeConfirmation) {
                            that._lastTag().removeClass('remove ui-state-highlight');
                        }

                        // Comma/Space/Enter are all valid delimiters for new tags,
                        // except when there is an open quote or if setting allowSpaces = true.
                        // Tab will also create a tag, unless the tag input is empty,
                        // in which case it isn't caught.
                        if (
                                (event.which === $.ui.keyCode.COMMA && event.shiftKey === false) ||
                                event.which === $.ui.keyCode.ENTER ||
                                (
                                        event.which == $.ui.keyCode.TAB &&
                                        that.tagInput.val() !== ''
                                        ) ||
                                (
                                        event.which == $.ui.keyCode.SPACE &&
                                        that.options.allowSpaces !== true &&
                                        (
                                                $.trim(that.tagInput.val()).replace(/^s*/, '').charAt(0) != '"' ||
                                                (
                                                        $.trim(that.tagInput.val()).charAt(0) == '"' &&
                                                        $.trim(that.tagInput.val()).charAt($.trim(that.tagInput.val()).length - 1) == '"' &&
                                                        $.trim(that.tagInput.val()).length - 1 !== 0
                                                        )
                                                )
                                        )
                                ) {
                            // Enter submits the form if there's no text in the input.
                            if (!(event.which === $.ui.keyCode.ENTER && that.tagInput.val() === '')) {
                                event.preventDefault();
                            }

                            // Autocomplete will create its own tag from a selection and close automatically.
                            if (!(that.options.autocomplete.autoFocus && that.tagInput.data('autocomplete-open'))) {
                                that.tagInput.autocomplete('close');
                                that.createTag(that._cleanedInput());
                            }
                        }
                    }).blur(function(e) {
                // Create a tag when the element loses focus.
                // If autocomplete is enabled and suggestion was clicked, don't add it.
                // MCZ - removing - this is showing duplicate tags on the page
                // when things like "confirm" are asked.
                /*
                 if (!that.tagInput.data('autocomplete-open')) {
                 that.createTag(that._cleanedInput());
                 }
                 */
            });
            // Autocomplete.
            if (this.options.availableTags || this.options.tagSource || this.options.autocomplete.source) {
                var autocompleteOptions = {
                    select: function(event, ui) {

                        // MZ: don't create tag when they select something from auto complete
                        //that.createTag(ui.item.value);
                        ui.item.value = ui.item.value + ":";
                        that.tagInput.value = ui.item.value;
                        that.tagInput.text(that.tagInput.value);
                        // Preventing the tag input to be updated with the chosen value.
                        return true; // MZ  - show what we selected in the input field
                        //return false;
                    }
                };
                $.extend(autocompleteOptions, this.options.autocomplete);
                // tagSource is deprecated, but takes precedence here since autocomplete.source is set by default,
                // while tagSource is left null by default.
                autocompleteOptions.source = this.options.tagSource || autocompleteOptions.source;
                this.tagInput.autocomplete(autocompleteOptions).bind('autocompleteopen.tagit', function(event, ui) {
                    that.tagInput.data('autocomplete-open', true);
                }).bind('autocompleteclose.tagit', function(event, ui) {
                    that.tagInput.data('autocomplete-open', false)
                });
                this.tagInput.autocomplete('widget').addClass('tagit-autocomplete');
            }
        },
        destroy: function() {
            $.Widget.prototype.destroy.call(this);
            this.element.unbind('.tagit');
            this.tagList.unbind('.tagit');
            this.tagInput.removeData('autocomplete-open');
            this.tagList.removeClass([
                'tagit',
                'ui-widget',
                'ui-widget-content',
                'ui-corner-all',
                'tagit-hidden-field'
            ].join(' '));
            if (this.element.is('input')) {
                this.element.removeClass('tagit-hidden-field');
                this.tagList.remove();
            } else {
                this.element.children('li').each(function() {
                    if ($(this).hasClass('tagit-new')) {
                        $(this).remove();
                    } else {
                        $(this).removeClass([
                            'tagit-choice',
                            'ui-widget-content',
                            'ui-state-default',
                            'ui-state-highlight',
                            'ui-corner-all',
                            'remove',
                            'tagit-choice-editable',
                            'tagit-choice-read-only'
                        ].join(' '));
                        $(this).text($(this).children('.tagit-label').text());
                    }
                });
                if (this.singleFieldNode) {
                    this.singleFieldNode.remove();
                }
            }

            return this;
        },
        _cleanedInput: function() {
            // Returns the contents of the tag input, cleaned and ready to be passed to createTag
            return $.trim(this.tagInput.val().replace(/^"(.*)"$/, '$1'));
        },
        _lastTag: function() {
            return this.tagList.find('.tagit-choice:last:not(.removed)');
        },
        _tags: function() {
            return this.tagList.find('.tagit-choice:not(.removed)');
        },
        assignedTags: function() {
            // Returns an array of tag string values
            var that = this;
            var tags = [];
            if (this.options.singleField) {
                tags = $(this.options.singleFieldNode).val().split(this.options.singleFieldDelimiter);
                if (tags[0] === '') {
                    tags = [];
                }
            } else {
                this._tags().each(function() {
                    tags.push(that.tagLabel(this));
                });
            }
            return tags;
        },
        _updateSingleTagsField: function(tags) {
            // Takes a list of tag string values, updates this.options.singleFieldNode.val to the tags delimited by this.options.singleFieldDelimiter
            $(this.options.singleFieldNode).val(tags.join(this.options.singleFieldDelimiter)).trigger('change');
        },
        _subtractArray: function(a1, a2) {
            var result = [];
            for (var i = 0; i < a1.length; i++) {
                if ($.inArray(a1[i], a2) == -1) {
                    result.push(a1[i]);
                }
            }
            return result;
        },
        tagLabel: function(tag) {
            // Returns the tag's string label.
            if (this.options.singleField) {
                return $(tag).find('.tagit-label:first').text();
            } else {
                return $(tag).find('input:first').val();
            }
        },
        _showAutocomplete: function() {
            this.tagInput.autocomplete('search', '');
        },
        _findTagByLabel: function(name) {
            var that = this;
            var tag = null;
            this._tags().each(function(i) {
                if (that._formatStr(name) == that._formatStr(that.tagLabel(this))) {
                    tag = $(this);
                    return false;
                }
            });
            return tag;
        },
        _isNew: function(name) {
            return !this._findTagByLabel(name);
        },
        _formatStr: function(str) {
            if (this.options.caseSensitive) {
                return str;
            }
            return $.trim(str.toLowerCase());
        },
        _effectExists: function(name) {
            return Boolean($.effects && ($.effects[name] || ($.effects.effect && $.effects.effect[name])));
        },
        createTag: function(value, additionalClass, duringInitialization) {
            var that = this;
            value = $.trim(value);
            if (this.options.preprocessTag) {
                value = this.options.preprocessTag(value);
            }

            if (value === '') {
                return false;
            }

            if (!this.options.allowDuplicates && !this._isNew(value)) {
                var existingTag = this._findTagByLabel(value);
                if (this._trigger('onTagExists', null, {
                    existingTag: existingTag,
                    duringInitialization: duringInitialization
                }) !== false) {
                    if (this._effectExists('highlight')) {
                        existingTag.effect('highlight');
                    }
                }
                return false;
            }

            if (this.options.tagLimit && this._tags().length >= this.options.tagLimit) {
                this._trigger('onTagLimitExceeded', null, {duringInitialization: duringInitialization});
                return false;
            }

            var label = $(this.options.onTagClicked ? '<a class="tagit-label"></a>' : '<span class="tagit-label"></span>').text(value);
            // Create tag.
            var tag = $('<li></li>')
                    .click(function() {
                      var id = $(this).parent().attr("id").replace("tags_", "");// remove "tag_" prefix
                      var rank = $(this).parent().attr("rank") - 1;

                      var html = "";
                      $(".label-details-wrapper").html(html);

                      // get the type:value for the tag clicked
                      var label = formatLabelForDatabase($(this).children("input")[0].defaultValue);
                      // find the element for that label
                      // tags are stored as:
                      //  Model.results[rank].tags[userid].map<label, rating:comments>
                      // it's possible they're clicking on a label they don't have a rank for
                      // OR that they don't have any labels at all.
                      var myRating = Model.results[rank].tags[getCookie("userid")];
                      if (_.isUndefined(myRating)){
                        myRating = "-1:";
                     } else {
                        myRating = Model.results[rank].tags[getCookie("userid")][label];
                        if (_.isUndefined(myRating)){
                          myRating = "-1:";
                        }
                      }
                      userData = myRating.split(":");
                      html = that._getRatingHTML(userData[0], userData[1], label);

                      $("#" + id).after(html);


                      $("#detail-button").click(function() {
                        var $el = $(this);
                        if ($el.text() == "Show Details"){
                          $el.text("Hide Details");
                          // GLOBAL.users[userID]
                          var ratings = "";
                          for (userid in Model.results[rank].tags){
                            // don't show our rating
                            if (userid == getCookie("userid")){
                              continue;
                            }
                            rating = Model.results[rank].tags[userid][label];
                            if (!_.isUndefined(rating)){
                              ud = rating.split(":");
                              ratings += "<b>" + GLOBAL.users[userid] +  "</b>: rating: <b>" + ud[0] + "</b> notes: <b>" + ud[1] + "</b><br>";
                            }

                          }
                          if (ratings === ""){
                            ratings = "No one else has rated this label.";
                          }
                          $("#rating-details").html(ratings);
                        } else {
                          $el.text("Show Details");
                          $("#rating-details").html("");
                        }

                      });

                      $("#rating-cancel").click(function() {
                        $(".label-details-wrapper").html(""); // close the pop up
                      });
                      $("#rating-submit").click(function() {
                        var rating = -1;

                        if ($("input:radio[name='rating-value']").is(":checked") === true ){
                          rating = $("input:radio[name=rating-value]:checked").val();
                        }

                        comment = $("#notes-field").val();
                        if (rating === -1){
                          alert("Please provide a rating");
                          return;
                        }
                        // ??? UGLY - the use of myRating
                        // if we're just rating someone eles's tag, add it
                        if (myRating === "-1:"){
                          addTag(label, id, rating, comment);
                        } else {
                          updateTag(label, id, rating, comment);
                        }
                        $(".label-details-wrapper").html(""); // close the pop up
                        // update the rating in memory

                        // NOTE: this may be the first tag for this document so we need to make sure to add any elements we need
                        if (_.isUndefined(Model.results[rank].tags[getCookie("userid")])){
                          var text = '{"' +  formatLabelForDatabase(label) + '" : "' + rating + ':' + comment + '"}';
                          Model.results[rank].tags[getCookie("userid")] =  JSON.parse(text);
                        } else {
                          Model.results[rank].tags[getCookie("userid")][formatLabelForDatabase(label)] = rating + ":" + comment;
                        }

                        // put a temp place holder
                        $("#" + id).before('<div id="updateMe"></div>' );
                        // remove the original
                        $("#" + id).remove();
                        // last param tells it to prepend to the results list
                        UI.renderSingleResult(Model.results[rank], Model.queryTerms, "#updateMe");
                       $("#updateMe").remove();
                      });

                    })
                    .addClass('tagit-choice ui-widget-content ui-state-default ui-corner-all')
                    .addClass(additionalClass)
                    .append(label);
            // MCZ: 2/19/2015 - allowing read only tags to be mixed in with regular
            // tags. The code inserting it adds the tagit-choice-read-only class so we
            // can check it here.
            if (this.options.readOnly || tag.hasClass('tagit-choice-read-only')) {
                tag.addClass('tagit-choice-read-only');
            } else {

                tag.addClass('tagit-choice-editable');
                // Button for removing the tag.
                var removeTagIcon = $('<span></span>')
                        .addClass('ui-icon ui-icon-close');
                var removeTag = $('<a><span class="text-icon">\xd7</span></a>') // \xd7 is an X
                        .addClass('tagit-close')
                        .append(removeTagIcon)
                        .click(function(e) {
                            // Removes a tag when the little 'x' is clicked.
                            that.removeTag(tag);
                        });
                tag.append(removeTag);
            }

            // Unless options.singleField is set, each tag has a hidden input field inline.
            if (!this.options.singleField) {
                var escapedValue = label.html().split(" (")[0]; // MCZ: store JUST the label value, not any rating
                tag.append('<input type="hidden" value="' + escapedValue + '" name="' + this.options.fieldName + '" class="tagit-hidden-field" />');
            }

            if (this._trigger('beforeTagAdded', null, {
                tag: tag,
                tagLabel: this.tagLabel(tag),
                duringInitialization: duringInitialization
            }) === false) {
                // MCZ - remove what they were typing. Otherwise it just sits there
                this.tagInput.val('');
                return;
            }

            if (this.options.singleField) {
                var tags = this.assignedTags();
                tags.push(value);
                this._updateSingleTagsField(tags);
            }

            // DEPRECATED.
            this._trigger('onTagAdded', null, tag);
            this.tagInput.val('');
            // Insert tag.
            this.tagInput.parent().before(tag);
            this._trigger('afterTagAdded', null, {
                tag: tag,
                tagLabel: this.tagLabel(tag),
                duringInitialization: duringInitialization
            });
            // MCZ: added logic for rating
            if (!duringInitialization) {
                // have them rate the label for the resource

                // get the parent so we know where to display the dialog
                var foundTag = that._findTagByLabel(value);
                var parentID = $(foundTag).parent().attr("id");
                this._getRating(value, parentID);

            }

            if (this.options.showAutocompleteOnFocus && !duringInitialization) {
                setTimeout(function() {
                    that._showAutocomplete();
                }, 0);
            }
        },
        // MCZ: added code
        // MCZ - added 3rd param so we skip deleting from the db
        removeTag: function(tag, animate, delFromDB) {
            animate = typeof animate === 'undefined' ? this.options.animate : animate;
            delFromDB = typeof delFromDB === 'undefined' ? true : delFromDB;

            tag = $(tag);
            // DEPRECATED.
            this._trigger('onTagRemoved', null, tag);
            if (this._trigger('beforeTagRemoved', null, {tag: tag, tagLabel: this.tagLabel(tag)}) === false) {
                return;
            }

            if (this.options.singleField) {
                var tags = this.assignedTags();
                var removedTagLabel = this.tagLabel(tag);
                tags = $.grep(tags, function(el) {
                    return el != removedTagLabel;
                });
                this._updateSingleTagsField(tags);
            }

            if (animate) {
                tag.addClass('removed'); // Excludes this tag from _tags.
                var hide_args = this._effectExists('blind') ? ['blind', {direction: 'horizontal'}, 'fast'] : ['fast'];
                var thisTag = this;
                hide_args.push(function() {
                    tag.remove();
                    if (delFromDB)
                        thisTag._trigger('afterTagRemoved', null, {tag: tag, tagLabel: thisTag.tagLabel(tag)});
                });
                tag.fadeOut('fast').hide.apply(tag, hide_args).dequeue();
            } else {
                tag.remove();
                if (delFromDB)
                    this._trigger('afterTagRemoved', null, {tag: tag, tagLabel: this.tagLabel(tag)});
            }


        },
        removeTagByLabel: function(tagLabel, animate) {
            var toRemove = this._findTagByLabel(tagLabel);
            if (!toRemove) {
                throw "No such tag exists with the name '" + tagLabel + "'";
            }
            this.removeTag(toRemove, animate);
        },
        removeAll: function() {
            // Removes all tags.
            var that = this;
            this._tags().each(function(index, tag) {
                that.removeTag(tag, false);
            });
        },
        _getRatingHTML: function(rating, comment, label){

          var checked = ["","","",""];
          var cancelButton  =  '<button id="rating-cancel" >Cancel</button><button id="detail-button">Show Details</button>';
          if (_.isUndefined(rating) ){
            rating = 1; // default
            // if new label, don't allow them to cancel - they have to rate it.
            cancelButton = "";
          }

          if (_.isUndefined(comment) ){
            comment = "";
          }

          if (rating > 0){
            // provide a cancel button IF this is an update
            checked[rating - 1] = "checked";
            }

          // remove any leading "*:"
          label = label.replace("*:", "");

             var html ='<div class="label-details-wrapper">'
            + '<div class="label-details">'
            + '<div id="rating-label"><b>' + label + '</b></div>'
            + '<div> '
            + 'Rating:&nbsp; <input type="radio" name="rating-value"  value="1" ' + checked[0] + '> 1 (Fair) &nbsp;'
            + '<input type="radio" name="rating-value" value="2" ' + checked[1] + '> 2 (Good) &nbsp;'
            + '<input type="radio" name="rating-value" value="3" ' + checked[2] + '> 3 (Excellent) &nbsp;'
            + '<input type="radio" name="rating-value" value="4" ' + checked[3] + '> 4 (Perfect) &nbsp;'
            + '</div> '
            + '<div > '
            + '<span class="notes-label">Notes:&nbsp;</span><span></span> <textarea id="notes-field" name="label-notes">' + comment + '</textarea></span>'
            + '</div> '
            + '<div> '
            + '<button id="rating-submit" >Submit</button>'
            + cancelButton
            + '</div>'
            + '<div id="rating-details"></div>'
            + '</div>';
            return html;
      },
        _getRating: function(label, parentID){

          var id = parentID;
          var resource = parentID.substring(5); // ignore the "tags_" prefix
          var rank = $("#" + id).attr("rank") - 1;
          var html = "";
          $(".label-details-wrapper").html(html);

          html = this._getRatingHTML(undefined, undefined, label);
          $("#" + id).after(html);

          $("#overlay").show();
          $("#notes-field").focus();
          var docHeight = $(document).height();

          $("#rating-submit").click(function() {

            $("#overlay").hide();
            rating = $("input[name=rating-value]:checked").val();
            comment = $("#notes-field").val();

            // update the label with the rating we just gave it
            $("#"+id).children(".tagit-choice").last().children("span.tagit-label").text(label + " (" + rating + ")");

            addTag(label, resource, rating, comment);
            addLabelToButtons(label);
            // add it to our in memory representation
            // NOTE: this may be the first tag for this document so we need to make sure to add any elements we need
            var fullLabel = formatLabelForDatabase(label);

            if (_.isUndefined(Model.results[rank].tags[getCookie("userid")])){
              var text = '{"' +  formatLabelForDatabase(label) + '" : "' + rating + ':' + comment + '"}';
              Model.results[rank].tags[getCookie("userid")] =  JSON.parse(text);
            } else {
              Model.results[rank].tags[getCookie("userid")][formatLabelForDatabase(label)] = rating + ":" + comment;
            }

            $(".label-details-wrapper").html(""); // close the pop up
          });


        }
    });
})(jQuery);

