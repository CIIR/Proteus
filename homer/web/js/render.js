/**
 * render.js, for rendering results and other things into html
 */

/**
 * This is a table of functions that creates html for a result, keyed on the "kind" of the result.
 * See, for example, resultRenderers["ia-pages"] in internetArchive.js
 */
var resultRenderers = {};
var getResultRenderer = function(kind) {
  if (resultRenderers[kind]) {
    return resultRenderers[kind];
  }
  return resultRenderers["default"];
};
/** This is the default, plain result renderer */
resultRenderers["default"] = function(queryTerms, result) {
  var html = '<table><tr>';
  html += '<td class="rank">' + result.rank + '</td>';
  html += '<td class="name">' + result.name + '</td>';
  html += '<td class="score">' + result.score + '</td>';
  if (result.snippet) {
    html += '</tr><tr>';
    html += '<td>&nbsp;</td>';
    html += '<td class="snippet" colspan=2> ...' +
            highlightText(queryTerms, result.snippet, '<span class="hili">', '</span>') +
            '... </td>';
  }

  if (result.meta) {
//html += '</tr><tr>'; // open new row
//html += '<td class="meta" colspan="2">TODO: default meta</td>'; // single column
  }
  html += '</tr></table><div>';

  if (result.tags) {
    // we ALWAYS want a div if you're logged in so you can add tags
    var username = getCookie("username");
    if (username !== "") {

      html += '<ul class="tags_' + result.name + '">  ';
      if (typeof result.tags[username] !== 'undefined') {
        tags = result.tags[username].toString().split(',');
        for (tag in tags) {
          html += '  <li> ' + tags[tag] + ' </li> ';
        }
      }
      html += '</ul>'
    } // end if someone is logged in

    var tmp_html = "";
    var read_only_tags = false;
    for (user in result.tags) {

      tags = result.tags[user].toString().split(',');
      // skip current user
      if (user !== username) {
        for (tag in tags) {
          read_only_tags = true;
          tmp_html += '  <li> ' + tags[tag] + ' </li> ';
        }
      }
    }
    // only show the tag box if there is something to show
    if (read_only_tags === true) {
      html += '<ul class="read-only-tags">' + tmp_html + '</ul>'
    }

  } // end if display tags

  html += '</div>';
  return html;
};

