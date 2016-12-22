/**
 * Created by michaelz on 7/13/2015.
 */

// functions to take the JSON for an action and convert it to HTML

function convertJSONtoHTML(jsonStr) {

  // the logs start with a timestamp, remove it
  jsonStr = jsonStr.split("\t")[1];

  // console.log( (jsonStr))

  var data = JSON.parse(jsonStr);
  //console.log("action: " + data.action)
  var func = JSONtoHTMLFunctions[data.action];

  if (_.isUndefined(func)) {
    console.log("Unknown function for " + data.action);
  } else {
    return func(data);
  }

}

var JSONtoHTMLFunctions = {};

var logStart = function (data) {
  return '<div class="activity-row activity-' + data.action.toLowerCase() + ' user-' + data.user + '">' + data.timestamp + ' ' + data.user + ' ';
};

JSONtoHTMLFunctions["LOGIN"] = function (data) {
  return logStart(data)  + ' logged in.</div>';
};

JSONtoHTMLFunctions["LOGOUT"] = function (data) {
  return logStart(data) + ' logged out.</div>'
};

JSONtoHTMLFunctions["SEARCH"] = function (data) {
  return logStart(data)  + ' searched: ' + data.enteredQuery + '</div>';
  // TODO add labels
};

JSONtoHTMLFunctions["RESULTS"] = function (data) {
  return logStart(data)  + ' search results: ' + data.docIDs + '</div>'
};

var noteHTML = function(data, action){
  var id = parsePageID(data.resource);
  // if there's only one part of the resource, assume it's a paper - because we currently only have one annotation
  // object per paper while books have one per page.
  var html = logStart(data)  + ' ' + action + ' a note to document: ' + data.resource + ' note: "' + data.data.text + '"';
  if (id.page.length == 0){
    return html
            + ' view: <a target="_blank" href="../view.html?kind=article&action=view&id=' +  data.resource + '&noteid=' + data.notePK + '">Article</a></div>'
  } else {
    return html
            + ' view: <a target="_blank" href="../view.html?kind=ia-pages&action=view&id=' +  data.resource + '&noteid=' + data.notePK + '">Page</a></div>'
  }

};
JSONtoHTMLFunctions["ADD-NOTE"] = function (data) {
  return noteHTML(data, "added");
};

JSONtoHTMLFunctions["UPD-NOTE"] = function (data) {
  return noteHTML(data, "updated");
};

JSONtoHTMLFunctions["DEL-NOTE"] = function (data) {
  return logStart(data)  + ' deleted a note from document: ' + data.resource + '</div>'
};

JSONtoHTMLFunctions["ADD-TAG"] = function (data) {
  return logStart(data) + ' added a tag to document: ' + data.resource + ' tag: ' + data.tag + '</div>'
};

JSONtoHTMLFunctions["DEL-TAG"] = function (data) {
  return logStart(data)  + ' deleted a tag from document: ' + data.resource + ' tag: ' + data.tag + '</div>'
};

JSONtoHTMLFunctions["VIEW-RES"] = function (data) {
  return logStart(data)  + ' viewed the document: ' + data.docIDs + '</div>'
};
JSONtoHTMLFunctions["REGISTER"] = function (data) {
  return ''
};
JSONtoHTMLFunctions["CREATE-CORPUS"] = function (data) {
  return ''
};
JSONtoHTMLFunctions["CLICK"] = function (data) {
  return ''
};







