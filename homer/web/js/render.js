/**
 * render.js, for rendering results and other things into html
 */
var Render = {};

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
  html += '<td class="name">' + Render.makeViewLink(result.name, result.kind, result.name) + '</a></td>';
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
  html += '</tr></table>';

  if (result.tags) {
    html += UI.renderTags(result);
  } // end if display tags

  return html;
};

Render.makeViewLink = function(id, kind, label) {
  return '<a href="/?action=view&id='+id+'&kind='+kind+'">'+label+'</a>';
}