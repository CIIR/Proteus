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
    console.log("Falling back to default for " + kind);
    return resultRenderers["default"];
};
/** This is the default, plain result renderer */
resultRenderers["default"] = function(queryTerms, result, resDiv) {
    var html = '<table><tr>';
    html += '<td class="rank">' + result.rank + '</td>';
    html += '<td class="name">' + Render.makeViewLink(result.name, result.kind, result.name, result.rank ) + '</a></td>';
    html += '<td class="score">' + result.score + '</td>';
    html += '</tr>';
    if (result.snippet) {
        html += '<tr>';
        html += '<td>&nbsp;</td>';
        html += '<td class="snippet" colspan=2> ...' +
         result.snippet +
        '... </td>';
        html += '</tr>';
    }

    if (result.meta) {
        _.forIn(result.meta, function(value, key) {
            html += '<tr>';
            html += '<td class="metakey">' + key + '</td>';
            html += '<td class="metavalue">' + value + '</td>';
            html += '</tr>';
        });
    }
    html += '</table>';

    resDiv.html(html);
    return resDiv;
};

Render.makeViewLink = function(id, kind, label, rank) {
    return '<a href="/?action=view' +
            '&id=' + encodeURIComponent(id) +
            '&kind=' + encodeURIComponent(kind) + '" onmousedown="return rwt(this,' + rank + ')" target="_blank">' +  label + '</a>';
};

Render.getPagePreviewURL = function(url, title, queryTerms, rank) {
    return '<a class="fancybox" href="' + url + '" >' +  title + '</a>';
}
// todo ??? THIS IS getting called many times and inserting two "meta-name" classes which screws up when no metadta
Render.getDocumentURL = function(url, title, rank, id ) {

    var html =  '<a href="' + url + '" onmousedown="return rwt(this,' + rank + ')" target="_blank"><span class="' + id + '-meta-name">' + title + '</span>' ;
    html +=  '&nbsp;<span class="fa fa-external-link"></span></span></a>';
    return html;

}

Render.getDocumentURL_tmp = function(url, title, rank) {

    var html =  '<a href="' + url + '" onmousedown="return rwt(this,' + rank + ')" target="_blank"><span >' + title + '</span>' ;
    html +=  '&nbsp;<span class="fa fa-external-link overimage"></span></span></a>';
    return html;

}