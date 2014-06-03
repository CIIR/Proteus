/**
 * ui.js
 *
 * This file roughly contains all the interfacing with the index.html boilerplate, using jquery as needed.
 *
 */

var errorDiv = $("#error");
var resultsDiv = $("#results");
var viewResourceDiv = $("#view-resource");
var progressDiv = $("#progress");
var queryBox = $("#ui-search");
var loginInfo = $("#ui-login-info");
var moreButton = $("#ui-go-more");
var searchButtons = $("#search-buttons");


queryBox.keypress(function(e)
{
    if (e.keyCode == 13)
        handleEnter();
});

// UI object/namespace
var UI = {};

UI.generateButtons = function() {
    API.getKinds({}, function(data) {
        UI.defaultKind = data.defaultKind;

        var availableKinds = _(data.kinds);
        var buttonDescriptions = _(UI.buttons);

        _.forIn(data.kinds, function(spec, kind) {
            spec.kind = kind; // so the onClick knows what kind it was
            if (!spec.button) {
                UI.showError("You need to specify a \"button\" display attribute for kind \"" + kind + "\"");
            }
            var button = $('<input type="button" value="' + spec.button + '" />');
            button.click(function() {
                UI.onClickSearchButton(spec);
            });
            searchButtons.append(button)
        });
    });
};

UI.clear = function() {
    UI.clearResults();
    UI.clearError();
    viewResourceDiv.html('');
};

UI.clearResults = function() {
    viewResourceDiv.hide();
    resultsDiv.html('');
    resultsDiv.show();
    moreButton.hide();
};

UI.showProgress = function(str) {
    progressDiv.html(str);
};

UI.showError = function(str) {
    errorDiv.html(str);
    errorDiv.show();
};

UI.clearError = function() {
    errorDiv.html('');
    errorDiv.hide();
};

UI.getQuery = function() {
    return queryBox.val();
};

UI.setQuery = function(q) {
    queryBox.val(q);
};

/**
 * Render a single search result.
 * @see render.js
 */
UI.makeResult = function(queryTerms, result) {
    var renderer = getResultRenderer(result.viewKind);
    return '<div class="result">' + renderer(queryTerms, result) + '</div>';
};


// added labels to our button bar when they add a new one
function addLabelToButtons(newLabel) {
    // get the two parts of the label
    var tmp = newLabel.split(":");
    var type = tmp[0];
    var value = tmp[1];

    // handle the special case of when they don't enter a TYPE
    // IF there is no type, add it to the "special" group
    if (_.isUndefined(value)) {
        value = type;
        type = NO_TYPE_CONST();
    }

    var i = 0; // counter to get uniq elements
    // see if there is a matching button
    var found = false;
    $(".proteus-labels").each(function() {
        if (type === $(this).attr("name")) {
            // don't add the value if it already exists
            if ($('#multiselect-' + i + ' option[value="' + value + '"]').length == 0) {
                $("#multiselect-" + i).append('<option value="' + value + '">' + value + '</option>');
                $("#multiselect-" + i).multiselect('rebuild');
            }

            found = true;
        }

        i += 1;
    });
    if (!found) {
        // add a new one
        UI.createLabelMultiselect(type, value, i);
    }

}

// delete label from our button bar when they remove one
/* This needs more thought - can't just remove a type/value because
 * it may be attached to another document
 * 
 function deleteLabelFromButtons(newLabel) {
 // get the two parts of the label
 var tmp = newLabel.split(":");
 var type = tmp[0];
 var value = tmp[1];
 
 // handle the special case of when they don't enter a TYPE
 // IF there is no type, add it to the "special" group
 if (_.isUndefined(value)) {
 value = type;
 type = NO_TYPE_CONST();
 }
 
 var i = 0; // counter to get uniq elements
 // see if there is a matching button
 
 $(".proteus-labels").each(function() {
 if (type === $(this).attr("name")) {
 // ???? ONLY remove if it's not used ANYWHERE else
 $('#multiselect-' + i + ' option[value="' + value + '"]').remove();
 $("#multiselect-" + i).multiselect('rebuild');
 }
 i += 1;
 });
 
 }
 */

/**
 * Renders search results into UI after current results
 */


UI.appendResults = function(queryTerms, results) {

    UI.showProgress("Ajax response received!");

    _(results).forEach(function(result) {
        console.debug("result name: " + result.name);
        resultsDiv.append(UI.makeResult(queryTerms, result));
        var tagName = "#tags_" + result.name;
        $(tagName).tagit({
            availableTags: GLOBAL.uniqTypes,
            autocomplete: {delay: 0, minLength: 0},
            allowSpaces: true,
            placeholderText: "Add a Label",
            afterTagRemoved: function(event, ui) {

                deleteTag(ui.tagLabel, result.name);
                // update the buttons
                //deleteLabelFromButtons(ui.tagLabel);

                return true;
            },
            beforeTagAdded: function(event, ui) {
                if (!ui.duringInitialization) {
                    // only ask "are you sure" if this is a NEW tag TYPE
                    tmp = ui.tagLabel.split(":");
                    var res = true;
                    if (tmp.length === 2 && $.inArray(tmp[0], GLOBAL.uniqTypes) === -1) {
                        res = confirm("Are you sure you want to create the label type \"" + tmp[0] + "\"?");
                        // add the new type to our list
                        GLOBAL.uniqTypes.push(tmp[0]);
                    }
                    if (res == true) {
                        addTag(ui.tagLabel, result.name);
                        // update the buttons
                        addLabelToButtons(ui.tagLabel);
                    }
                    return res;
                } else {
                    return true;
                }
            }
        });
        $(".read-only-tags").tagit({
            readOnly: true
        });
        moreButton.show();
    });

}
;
/**
 * A set of functions for reacting to events in other, more general code.
 */
UI.setReadyHandler = function(callback) {
    $(document).ready(function() {
        UI.generateButtons();
        callback();
    });
};
UI.setMoreHandler = function(callback) {
    moreButton.click(callback);
};
UI.setUserName = function(user) {
    if (!user) {
        UI.clearUserName();
    } else {
        loginInfo.html("<span id='login-form-text'> Welcome " + user + "</span> <input id='ui-go-logout' type='button' value='LogOut' />");
        $("#ui-go-logout").click(function() {
            logOut();
        });
    }
};
UI.clearUserName = function() {
    loginInfo.html(" <input id='ui-username' type='text' /> " +
            "<input id='ui-go-login' type='button' value='Login' />");
    // have to bind click event here because that ID doesn't exist until we do this.
    $("#ui-go-login").click(function() {
        var username = $("#ui-username").val();
        logIn(username);
        UI.setUserName(username);
    });
};
UI.renderTags = function(result) {

    var my_html = '';
    var ro_html = ''; // read only tags

    var username = getCookie("username");

    for (var user in result.tags) {

        tags = result.tags[user].toString().split(',');
        for (tag in tags) {
            if (user !== username) {
                ro_html += '  <li> ' + formatLabelForDispaly(tags[tag]) + ' </li> ';
            } else {
                my_html += '  <li> ' + formatLabelForDispaly(tags[tag]) + ' </li> ';
            }
        }
    }
    // only show the tag box if there is something to show
    if (ro_html !== '') {
        ro_html = '<ul class="read-only-tags">' + ro_html + '</ul>';
    }

    return '<div><ul id="tags_' + result.name + '">' + my_html + '</ul>' + ro_html + '</div>';
};

// used when we don't want to dispaly any of the "my tags" features
UI.hideMyTagsFunctionality = function() {
    $("#my-tags").html("");
    $("#toggle-my-tags-img").hide();
    $("#my-tags-container").hide();
};

UI.toggleMyTags = function() {
    // if no one is loged in, we don't show anything
    if (getCookie("username") === "") {
        UI.hideMyTagsFunctionality();
        return;
    }
    var ele = $("#my-tags-container");
    if (ele.is(":visible")) {
        $("#toggle-my-tags-img").attr("src", "/images/down_arrows.png");
        ele.hide();
    }
    else {
        $("#toggle-my-tags-img").attr("src", "/images/up_arrows.png");
        ele.show();
    }
    $("#toggle-my-tags-img").show();
};

UI.createLabelMultiselect = function(type, valueList, index) {
    // note we're using the index value as part of the ID because the
    // actual values could have spaces.
    var html = '<select id="multiselect-' + index + '" class="proteus-labels" multiple="multiple" name="' + type + '">';

    var tags = valueList.split(',');
    for (tag in tags) {
        html += '<option value="' + tags[tag] + '">' + tags[tag] + '</option>';
    }

    html += ' </select>';

    $("#my-tags").append(html);

    $('#multiselect-' + index).multiselect({
        includeSelectAllOption: true,
        nonSelectedText: type,
        nSelectedText: type,
        buttonText: function(options, select) {
            return  this.nonSelectedText + ' <b class="caret"></b>';
        },
        buttonLabel: function(options, select) {
            return  this.nonSelectedText + ' <b class="caret"></b>';
        }
    });
};

UI.addLabelActionButtons = function() {
    $("#search-labels-button").html("");
    $("#search-labels-button").append('<input id="ui-label-search" type="button" onclick="getResourcesForLabels();" value="Search Labels">');
    $("#search-labels-button").append('<input id="ui-label-clear" type="button" onclick="UI.clearSelectedLabels();" value="Clear Selections">');
}

UI.clearSelectedLabels = function() {
    var i = 0; // counter to get uniq elements

    $(".proteus-labels").each(function() {

        $('option', $('#multiselect-' + i)).each(function(element) {
            $('#multiselect-' + i).multiselect('deselect', $(this).val());
        });
        $("#multiselect-" + i).multiselect('refresh'); // this clears the "select all" option
        i += 1;
    });
};