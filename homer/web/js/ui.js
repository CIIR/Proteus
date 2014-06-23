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

    newLabel = type + ":" + value;

    // see if that value is in the list
    if ($('#multiselect-all optgroup[label="' + type + '"]').length == 0) {
        $("#multiselect-all").append('<optgroup  label="' + type + '"><option value="' + newLabel + '">' + value + '</option></optgroup >');
        $("#multiselect-all").multiselect('rebuild');
    } else {
        // see if the value exists
        if ($('#multiselect-all optgroup[label="' + type + '"] option[value="' + newLabel + '"]').length == 0) {
            $('#multiselect-all optgroup[label="' + type + '"]').append('<option value="' + newLabel + '">' + value + '</option>');
            $("#multiselect-all").multiselect('rebuild');
        }
    }

}

/**
 * Renders search results into UI after current results
 */


UI.appendResults = function(queryTerms, results, usingLabels) {

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

        // don't show the more button if we searched within labels - cuz
        // we return ALL results (for now)

        if (usingLabels) {
            moreButton.hide();
        } else {
            moreButton.show();
        }

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
    $("#all-my-tags").html("");
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
// param is array of uniq types
UI.createLabelMultiselect = function(myUniqTypes) {

    var userName = getCookie("username");
    if (userName === "") {
        $("#all-my-tags").html("");
        return;
    }

    var html = '<select id="multiselect-all" class="proteus-labels" multiple="multiple" >';
    for (type in myUniqTypes) {

        html += '<optgroup label="' + myUniqTypes[type] + '">'
        // get the values just for this type
        var myValues = [];
        var tags = GLOBAL.allTags[userName].toString().split(',');
        for (tag in tags) {
            var kv = tags[tag].split(":");
            if ((kv[0] === myUniqTypes[type]) && (!_.isUndefined(kv[1]))) {
                myValues.push(kv[1]);
            }
        }

        // var tags = valueList.split(',');
        for (tag in myValues) {
            // note we inlclude the "type" part so we can get the values easily later
            html += '<option value="' + myUniqTypes[type] + ":" + myValues[tag] + '">' + myValues[tag] + '</option>';
        }
        html += '</optgroup>';
    }
    html += ' </select>';
    $("#all-my-tags").append(html);
    $('#multiselect-all').multiselect(
            {
                includeSelectAllOption: true,
            }
    );
}
;


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