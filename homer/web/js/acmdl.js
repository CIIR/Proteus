/**
 * Render an article from the acm digital library
 */
resultRenderers["article"] = function(queryTerms, result) {
  var title = result.meta["title"];
  var snippet = result.snippet;

  var artid = result.meta["artid"];
  var author = result.meta["author"];
  var institution = result.meta["institution"];
  var proc = result.meta["proc"];
  var procid = result.meta["procid"];
  var citation = result.meta["citation"];
  var pubyear = result.meta["pubyear"];

  var url_art = "http://dl.acm.org/";
  var url_proc = "http://dl.acm.org/";

  if( artid!=null ) {
    url_art = url_art + "citation.cfm?id=" + artid;
  }

  if( procid!=null ) {
    url_proc = url_proc + "citation.cfm?id=" + procid;
  }

  var html = '<div class="result"><table>';

  html += '<tr>' +
      '<td class="title"><a href="'+url_art+'">' + title + '</a></td>' +
      '<td class="citation">Citation: ' + citation + '</td>' +
      '<td class="score">' + result.score.toFixed(3) + ' r'+ result.rank + '</td>' +
      '</tr>';

  if( proc!=null ) {
    html +='<tr>' +
        '<td class="proc">Published in <a href="'+url_proc+'">' + proc + '</a>, ' + pubyear + '</td>' +
        '</tr>';
  }

  if( author!=null ) {
    html +='<tr>' +
        '<td class="author">Written by ' + author + '</td>' +
        '</tr>';
  }

  if( author!=null ) {
    html +='<tr>' +
        '<td class="insti">From ' + institution + '</td>' +
        '</tr>';
  }

  if( snippet!=null ) {
    html += '<tr><td class="snippet" colspan="2"> ...';
    html += highlightText(queryTerms, snippet, '<span class="hili">','</span>');
    html += '... </td></tr>';
  }

  html = html + '</table></div>';

  return html;
};


