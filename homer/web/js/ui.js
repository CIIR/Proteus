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

// UI object/namespace
var UI = {};
UI.generateButtons = function() {


    API.getKinds({}, function(data) {
        if (data.title)
            $("#proteus-title").html(data.title);
        UI.defaultKind = data.defaultKind;
        var availableKinds = _(data.kinds);
        var buttonDescriptions = _(UI.buttons);
        // for all kinds the server gives back, make a button
        _.forIn(data.kinds, function(spec, kind) {
            spec.kind = kind; // so the onClick knows what kind it was
            if (!spec.button) {
                UI.showError("You need to specify a \"button\" display attribute for kind \"" + kind + "\"");
                return;
            }
            var button = $('<input type="button" value="' + spec.button + '" />');
            // see if we're the default button
            if (kind === UI.defaultKind) {
                button.addClass("default-search-button")
            }
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
 * I'm adding comments now since i regret it
 * when I dont
 * deleted UI.makeResult since its useless.
 
 */

// added labels to our button bar when they add a new one

function addLabelToButtons(newLabel) {
    //alert("addLabelToButtons");
    // get the two parts of the label, remove the rating
    var tmp = newLabel.split(":");
    var type = tmp[0];
    var value = tmp[1];
    // handle the special case of when they don't enter a TYPE
    // IF there is no type, add it to the "special" group

    if (_.isUndefined(value)) {
        value = type;
        type = NO_TYPE_CONST();
    }

    var tree = $("#tree").fancytree("getTree");

    newLabel = type + ":" + value;
    // make sure we don't already have this
    var check = tree.getNodeByKey(newLabel);
    if (check !== null)
        return;

    // new labels:

    // search for the parent to attach it to
    var node = tree.getNodeByKey(type);
    if (node !== null) {

        node.addChildren({
            title: value, key: newLabel
        });
        tree.render();
    } else {
        // get the root node
        var root = tree.getFirstChild();

        // special logic for the first label
        if (_.isUndefined(root)) {
            getAllTagsByUser();
            return;
        }
        //add the parent & child
        var newNode = root.addChildren({
            title: type, key: type, folder: true
        });
        newNode.addChildren({
            title: value, key: newLabel
        });
        newNode.setExpanded(true);
        tree.render();
    }
}

/**
 * Renders search results into UI after current results
 */


UI.appendResults = function(queryTerms, results, usingLabels) {

    UI.showProgress("Ajax response received!");
    _(results).forEach(function(result) {
        console.debug("result name: " + result.name);
        var renderer = getResultRenderer(result.viewKind); //added this line and 5 below make adding/subt elements in future easier
        var resDiv = $('<div>');
        resDiv.attr('class', 'result');
        resDiv.attr('id', result.name);
        resultsDiv.append(renderer(queryTerms, result, resDiv)); //* 6/26/2014

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
                var that = this;
                if (!ui.duringInitialization) {
                    // only ask "are you sure" if this is a NEW tag TYPE
                    tmp = ui.tagLabel.split(":");
                    var res = true;
                    if (tmp.length === 2 && $.inArray(tmp[0], GLOBAL.uniqTypes) === -1) {
                        res = confirm("Are you sure you want to create the label type \"" + tmp[0] + "\"?");
                        if (res == false)
                            return false;
                        // add the new type to our list
                        GLOBAL.uniqTypes.push(tmp[0]);
                    }

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
};
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
UI.dispalyUserName = function() {
    // if it's an email address, just display the first part
    var user = getCookie("username").split("@")[0];

    if (user) {
        $("#ui-login-form").hide();
        $("#user-info").html("<span id='login-form-text'> Welcome " + user + "</span> <input id='ui-go-logout' type='button' value='LogOut' />").show();

        $("#ui-go-logout").click(function() {
            $("#user-info").hide();
            logOut();
            $("#ui-login-form").show();
        });
    }
};

UI.renderTags = function(result) {

    var my_html = '';
    var ro_html = ''; // read only tags

    var userid = getCookie("userid");
    for (var user in result.tags) {

        tags = result.tags[user].toString().split(',');
        for (tag in tags) {
            if (user !== userid) {
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

UI.toggleMyTags = function() {
    // if no one is logged in, we don't show anything
    if (getCookie("username") === "") {
        return;
    }
    var ele = $("#my-tags-container");
    if (ele.is(":visible")) {
        $("#toggle-my-tags-img").attr("src", "/images/down_arrows.png");
        ele.hide();
    } else {
        $("#toggle-my-tags-img").attr("src", "/images/up_arrows.png");
        ele.show();
    }
    $("#toggle-my-tags-img").show();
};
// param is array of uniq types
UI.createLabelMultiselect = function(myUniqTypes) {

    // alert("createLabelMultiselect");
    var userName = getCookie("username");
    var userID = getCookie("userid");
    if (userName === "") {
        return;
    }
    $("#empty-tree").html("");
    var node0 = $("#tree").fancytree("getRootNode");
    //   var all_key = ALL_NODE_KEY();
    var rootNode = node0.addChildren({
        title: "All", //     key: all_key,
        folder: true
    });

    for (type in myUniqTypes) {

        var childNode = rootNode.addChildren({
            title: myUniqTypes[type], key: myUniqTypes[type], folder: true
        });

        // get the values just for this type
        var myValues = [];
        var tags = GLOBAL.allTags[userID].toString().split(',');
        for (tag in tags) {
            var kv = tags[tag].split(":");
            // remove any rating info
            if ((kv[0] === myUniqTypes[type]) && (!_.isUndefined(kv[1]))) {
                // remove the rating, an add only if not already there 
                var val = kv[1].split("@")[0];
                if (myValues.indexOf(val) == -1)
                    myValues.push(val);
            }
        }

        // var tags = valueList.split(',');
        for (tag in myValues) {
            childNode.addChildren({
                title: myValues[tag], key: myUniqTypes[type] + ":" + myValues[tag]
            });
        }
    }
    $("#tree").fancytree("getRootNode").visit(function(node) {
        node.setExpanded(true);
    });

};


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
