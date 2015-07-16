/**
 * Created by michaelz on 7/13/2015.
 */

// functions to take the JSON for an action and convert it to HTML

function convertJSONtoHTML(jsonStr) {

  // the logs start with a timestamp, remove it
  jsonStr = jsonStr.split("\t")[1]

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
  return '<div class="activity-row activity-' + data.action.toLowerCase() + '">' + data.timestamp + ' ';
}

JSONtoHTMLFunctions["LOGIN"] = function (data) {
  return logStart(data) + data.user + ' logged in.</div>';
}

JSONtoHTMLFunctions["LOGOUT"] = function (data) {
  return logStart(data) + data.user + ' logged out.</div>'
}

JSONtoHTMLFunctions["SEARCH"] = function (data) {
  return logStart(data) + data.user + ' searched: ' + data.enteredQuery + '</div>'
  // TODO add labels
}

JSONtoHTMLFunctions["RESULTS"] = function (data) {
  return logStart(data) + data.user + ' search results: ' + data.docIDs + '</div>'
}

var noteHTML = function(data, action){
  var id = data.resource.split("_")
  return logStart(data) + data.user + ' ' + action + ' a note to document: ' + data.resource + ' note: ' + data.data.text
          + ' view: <a target="_blank" href="../index.html?kind=ia-pages&action=view&id=' +  data.resource + '&noteid=' + data.notePK + '">Page, </a>'
          + '<a target="_blank"  href="../index.html?kind=ia-books&action=view&id=' + id[0] + '&pgno=' + id[1] + '&noteid=' + data.notePK + '">Book</a></div>'

}
JSONtoHTMLFunctions["ADD-NOTE"] = function (data) {
  return noteHTML(data, "added");
}

JSONtoHTMLFunctions["UPD-NOTE"] = function (data) {
  return noteHTML(data, "updated");
}

JSONtoHTMLFunctions["DEL-NOTE"] = function (data) {
  return logStart(data) + data.user + ' deleted a note from document: ' + data.resource + '</div>'
}

JSONtoHTMLFunctions["ADD-TAG"] = function (data) {
  return logStart(data) + data.user + ' added a tag to document: ' + data.resource + ' tag: ' + data.tag + '</div>'
}

JSONtoHTMLFunctions["UPD-TAG"] = function (data) {
  return logStart(data) + data.user + ' updated a tag to document: ' + data.resource + ' tag: ' + data.tag + '</div>'
}

JSONtoHTMLFunctions["DEL-TAG"] = function (data) {
  return logStart(data) + data.user + ' deleted a tag from document: ' + data.resource + '</div>'
}

JSONtoHTMLFunctions["RATE-RES"] = function (data) {
  return logStart(data) + data.user + ' rated the document: ' + data.resource + ' a ' + data.rating + '</div>'
}

JSONtoHTMLFunctions["VIEW-RES"] = function (data) {
  return ''
}
JSONtoHTMLFunctions["REGISTER"] = function (data) {
  return ''
}
JSONtoHTMLFunctions["CREATE-CORPUS"] = function (data) {
  return ''
}
JSONtoHTMLFunctions["CLICK"] = function (data) {
  return ''
}







