/**
 * No other code should construct URLs or know about the internet archive in general,
 * so that we can generalize to the academic paper domain.
 *
 * URL format information for the Archive is at https://openlibrary.org/dev/docs/bookurls (as of March 2014)
 *
 */

var pageImage = function(pageid) {
    var id = pageid.split("_");
    return "http://www.archive.org/download/" + encodeURIComponent(id[0]) + "/page/n" + id[1] + ".jpg";
};

var pageThumbnail = function(pageid) {
    var id = pageid.split("_");
    return "http://www.archive.org/download/" + encodeURIComponent(id[0]) + "/page/n" + id[1] + "_thumb.jpg";
};

var archiveViewerURL = function(pageid) {
    var id = pageid.split("_");
    return 'https://archive.org/stream/' + id[0] + '#page/n' + id[1] + '/mode/2up';
};

var renderResult = function(queryTerms, result, resDiv, queryid) {

    var name = result.meta.title || result.meta.TEI || result.name;
    var identifier = result.name.split('_')[0];
    var docid = result.name;
    var snippet = result.snippet;
    var pageNum = result.name.split('_')[1];
    var iaURL = result.meta["identifier-access"];
    var nameLink = '';

    if (iaURL) {
        nameLink = Render.getDocumentURL(iaURL, name, queryTerms, result.rank);
    }
    var pgImage = iaURL;
    var kind = 'ia-books'; // default

    // if this is a book result - show the front page as the thumbnail but the links will
    // go to the max passage page.

    var thumbnail = '<img class="ia-thumbnail" src="' + pageThumbnail(result.name) + '"/>';

    var previewImage = Render.getDocumentURL(pgImage, thumbnail, queryTerms, result.rank, true);

    if (!_.isUndefined(pageNum)) {
        kind = 'ia-pages';
        // if page result - make the link go to the page
        nameLink = Render.getDocumentURL(archiveViewerURL(result.name), name, queryTerms, result.rank);

        // MCZ : removing page number for now as it does not match up with
        // the physical page number shown on the page
        //name += ' pp. ' + pageNum;
        pgImage = pageImage(result.name);
        previewImage = Render.getPagePreviewURL(pgImage, thumbnail, queryTerms, result.rank);

        // check if we're a note. Can either check metadata "docType" = "note" or there are two underscores
        // TODO : this should be moved up
        if (!_.isUndefined(result.meta)) {
            if (result.meta.docType == "note") {
                return renderNoteResult(queryTerms, result, resDiv);
            }
        }
    }
    if (kind == 'ia-books' && !_.isUndefined(result.snippetPage) ){
        docid = identifier + "_" + result.snippetPage;
        nameLink = Render.getDocumentURL(archiveViewerURL(docid), name, queryTerms, result.rank);
    }

    var tmphtml = '';

    if (!_.isUndefined(result.entities)) {

        // the "kind" variable right now represents the kind for this specific search result.
        // But for the link to search by entity we want the "kind" that we orginally
        // searched for, which is on the URL.
        // So if we searched "ia-all" (both books and pages) we want this to search "ia-all" too
        // not just books or pages.

        var urlParams = getURLParams();
        var currentKind = urlParams["kind"];
        if (_.isUndefined(currentKind)) {
            // we should be guarenteed to have a kind on the URL, but since
            // they can easily be changed, we'll take the kind from the search
            // result just in case.
            currentKind = kind;
        }

        var type;
        _(result.entities).forEach(function(entKey) {
            _(entKey).forIn(function(value, key) {
                type = key + "-entities";
                tmphtml += '<div align="left"><' + type + '><b>' + key + ':</b> ';
                _(value).forIn(function(rec, key2) {
                    // TODO: whould use default kind not hard code
                    // add class="ui-widget-content mz-ner" to <ent> if we want drag-n-drop entities
                    tmphtml += '<ent><a  href=\'' + buildSearchLink(key, rec.entity, currentKind) + '\'>' + rec.entity + '</a></ent> (' + rec.count + ')&nbsp;&#8226;&nbsp;';
                });
            });
            tmphtml += '</' + type + '></div>';
        });
    } // end if we have entities

    var html =  '<table><tr>';

    html += '<td class="preview" rowspan="3">' + previewImage + '</td>' +
            '<td class="name">' + nameLink + '&nbsp;(<a target="_blank" href="view.html?kind=ia-pages&action=view&id=' + docid + '&queryid=' + queryid + '">view OCR</a>)&nbsp;'

    // store the ratings with names but keep it hidden, we'll use this on hover to display the users and
    // their ratings on the left hand side of the screen.
    html += '<span id="' + result.name + '-user-ratings"></span><span  style="display:none" id="' + result.name + '-user-ratings-w-names"></span>';
    html += '<span class="highlight" id="' + result.name + '-dup-confidence"></span>';
    html += '</td></div></td><td class="score">&nbsp;&nbsp;&nbsp;rank: ' + result.rank + '</td></tr>';
    html += '<tr  class="author" ><td>' + result.meta.creator + '&nbsp;•&nbsp;published: ' + result.meta.date + '</td></tr>';

    if (snippet) {
        html += '<tr><td class="snippet" colspan="3"> ...';
        html +=  snippet ;
        html += '... </td></tr>';

        // only get uniq word if we haven't seen it before - this will be used later
        // to find duplicate documents
        if (_.isUndefined(uniqWords[result.rank - 1])) {
            var once = _.uniq(snippet.split(" "))
            uniqWords.push((once))
        }

    } // end if snippet

    html += '</table>';
    html += tmphtml;

    // show notes
    var noteHTML = '';
    _.each(result.notes.rows, function(note) {
        noteHTML += '<div class="resource-notes" ><a target="_blank" href="../view.html?kind=ia-pages&action=view&id=' + note.uri + '&noteid=' + note.id + '"><b>';
        // remove any <br> tags, they make the note look odd in the results list
        noteHTML += note.user.split('@')[0] + ' : <i>' + note.text.replace(/<br>/g, " ") + '</i></b> : ';
        noteHTML += note.quote.replace(/<br>/g, " ") ;
        noteHTML += '</a></div>';
    })

    if (noteHTML.length > 0) {
        html += '<a href="#" onclick="UI.toggleNotes(\'' + result.name + '\');"><span id="notes-link-' + result.name + '"><span class="glyphicon glyphicon-collapse-down"></span>&nbsp;';
        if (UI.settings.show_notes == false){
            html += 'Show';
        } else {
            html += 'Hide';
        }
        html += ' notes&nbsp;</span><span class="fa fa-pencil"></span></a>';
        html += '<div id="notes-div-' + result.name + '"  ';
        if (UI.settings.show_notes == false){
            html += 'style="display:none"';
        }
        html += '>' + noteHTML + '</div>';
    }

    // show queries
    if (UI.settings.show_found_with_query){

        var queries = [];
        _.each(result.queries.rows, function(query) {
            queries.push('<a target="_BLANK" href="index.html?action=search&kind=' + query.kind + '&q=' + encodeURIComponent(query.query) + '">' + query.query + '</a>');
        })
        if (queries.length == 1) {
            html += '<div class="resource-query" >Found with query: ' + queries.toString() + '</div>';
        }
        if (queries.length > 1) {
            html += '<div class="resource-query" >Found with queries: ' + queries.join(', ').toString() + '</div>';
        }
    }

    // show labels
    // TODO change notes to labels or subcorpus
    html += '<div id="notes-' + result.name + '" class="resource-labels" >' + displayLabels(result.name) + '</div>';

    resDiv.html(html);

    return resDiv;

};

var renderNoteResult = function(queryTerms, result, resDiv) {

    // TODO duplicate code with renderResult()

    var name = "Note: ";
    // remove any <br> tags, they make the note look odd in the results list
    var snippet = result.text.replace(/<br>/g, " ");

    var idParts = result.name.split('_');

    var html =
            '<table class="note-table">' +
            '<tr>';

    html += '<td class="name">' + name;
    // store the ratings with names but keep it hidden, we'll use this on hover to display the users and
    // their ratings on the left hand side of the screen.
    html += '<span id="' + result.name + '-user-ratings"></span><span  style="display:none" id="' + result.name + '-user-ratings-w-names"></span>';

    html += '</td></div></td>';
    if (snippet) {
        html += '<td class="snippet">';
        html += '<div  ><a target="_blank" href="../view.html?kind=ia-pages&action=view&id=' + idParts[0] + '_' + idParts[1] + '&noteid=' + idParts[2] + '">';
        html +=  snippet ; // last param says not to strip out punctuation
        html += '</a></td>';
    }
    html += '<td class="score">&nbsp;&nbsp;&nbsp;rank: ' + result.rank + '</td>' + '</tr>';

    if (_.isUndefined(uniqWords[result.rank - 1])) {
        uniqWords.push(""); // put an empty entry, we don't want to compare notes to check for duplicates.
    }
    html += '</table>';
    html += '<div id="notes-' + result.name + '" class="resource-labels" >' + displayLabels(result.name) + '</div>';

    resDiv.html(html);

    return resDiv;

};


var doActionSearchPages = function(args) {
    var action = args.action;
    if (action == "search") {
        return doSearchRequest(args);
    }

    if (!action) {
        UI.showError("action not defined when calling doActionRequest in JS");
        return;
    }
    UI.showError("Unknown action `" + action + "'");
};

resultRenderers["ia-books"] = renderResult;
resultRenderers["ia-pages"] = renderResult;
resultRenderers["ia-all"] = renderResult;
// TODO : MCZ temp for now, focusing on just books/pages
resultRenderers["all"] = renderResult;

resultRenderers["ia-corpus"] = renderResult;

// ===============================================================================================
/*
function BookReader() {

}

// Error reporting - this helps us fix errors quickly
function logError(description,page,line) {
    if (typeof(archive_analytics) != 'undefined') {
        var values = {
            'bookreader': 'error',
            'description': description,
            'page': page,
            'line': line,
            'itemid': 'cu31924020438929',
            'subPrefix': 'cu31924020438929',
            'server': 'ia600207.us.archive.org',
            'bookPath': '\x2F27\x2Fitems\x2Fcu31924020438929\x2Fcu31924020438929'
        };

        // if no referrer set '-' as referrer
        if (document.referrer == '') {
            values['referrer'] = '-';
        } else {
            values['referrer'] = document.referrer;
        }

        if (typeof(br) != 'undefined') {
            values['itemid'] = br.bookId;
            values['subPrefix'] = br.subPrefix;
            values['server'] = br.server;
            values['bookPath'] = br.bookPath;
        }

        archive_analytics.send_ping(values);
    }

    return false; // allow browser error handling so user sees there was a problem
}
window.onerror=logError;

br = new BookReader();

br.titleLeaf = 8;

br.getPageWidth = function(index) {
    return this.pageW[index];
}

br.getPageHeight = function(index) {
    return this.pageH[index];
}

// Returns true if page image is available rotated
br.canRotatePage = function(index) {
    return 'jp2' == this.imageFormat; // Assume single format for now
}

// reduce defaults to 1 (no reduction)
// rotate defaults to 0 (no rotation)
br.getPageURI = function(index, reduce, rotate) {
    var _reduce;
    var _rotate;

    if ('undefined' == typeof(reduce)) {
        _reduce = 1;
    } else {
        _reduce = reduce;
    }
    if ('undefined' == typeof(rotate)) {
        _rotate = 0;
    } else {
        _rotate = rotate;
    }

    var file = this._getPageFile(index);

    // $$$ add more image stack formats here
    return '//'+this.server+'/BookReader/BookReaderImages.php?zip='+this.zip+'&file='+file+'&scale='+_reduce+'&rotate='+_rotate;
}

// Get a rectangular region out of a page
br.getRegionURI = function(index, reduce, rotate, sourceX, sourceY, sourceWidth, sourceHeight) {

    // Map function arguments to the url keys
    var urlKeys = ['n', 'r', 'rot', 'x', 'y', 'w', 'h'];
    var page = '';
    for (var i = 0; i < arguments.length; i++) {
        if ('undefined' != typeof(arguments[i])) {
            if (i > 0 ) {
                page += '_';
            }
            page += urlKeys[i] + arguments[i];
        }
    }

    var itemPath = this.bookPath.replace(new RegExp('/'+this.subPrefix+'$'), ''); // remove trailing subPrefix

    return '//'+this.server+'/BookReader/BookReaderImages.php?id=' + this.bookId + '&itemPath=' + itemPath + '&server=' + this.server + '&subPrefix=' + this.subPrefix + '&page=' +page + '.jpg';
}

br._getPageFile = function(index) {
    var leafStr = '0000';
    var imgStr = this.leafMap[index].toString();
    var re = new RegExp("0{"+imgStr.length+"}$");

    var insideZipPrefix = this.subPrefix.match('[^/]+$');
    var file = insideZipPrefix + '_' + this.imageFormat + '/' + insideZipPrefix + '_' + leafStr.replace(re, imgStr) + '.' + this.imageFormat;

    return file;
}

br.getPageSide = function(index) {
    //assume the book starts with a cover (right-hand leaf)
    //we should really get handside from scandata.xml


    // $$$ we should get this from scandata instead of assuming the accessible
    //     leafs are contiguous
    if ('rl' != this.pageProgression) {
        // If pageProgression is not set RTL we assume it is LTR
        if (0 == (index & 0x1)) {
            // Even-numbered page
            return 'R';
        } else {
            // Odd-numbered page
            return 'L';
        }
    } else {
        // RTL
        if (0 == (index & 0x1)) {
            return 'L';
        } else {
            return 'R';
        }
    }
}

br.getPageNum = function(index) {
    var pageNum = this.pageNums[index];
    if (pageNum) {
        return pageNum;
    } else {
        return 'n' + index;
    }
}

// Single images in the Internet Archive scandata.xml metadata are (somewhat incorrectly)
// given a "leaf" number.  Some of these images from the scanning process should not
// be displayed in the BookReader (for example colour calibration cards).  Since some
// of the scanned images will not be displayed in the BookReader (those marked with
// addToAccessFormats false in the scandata.xml) leaf numbers and BookReader page
// indexes are generally not the same.  This function returns the BookReader page
// index given a scanned leaf number.
//
// This function is used, for example, to map between search results (that use the
// leaf numbers) and the displayed pages in the BookReader.
br.leafNumToIndex = function(leafNum) {
    for (var index = 0; index < this.leafMap.length; index++) {
        if (this.leafMap[index] == leafNum) {
            return index;
        }
    }

    return null;
}

// This function returns the left and right indices for the user-visible
// spread that contains the given index.  The return values may be
// null if there is no facing page or the index is invalid.
br.getSpreadIndices = function(pindex) {
    // $$$ we could make a separate function for the RTL case and
    //      only bind it if necessary instead of always checking
    // $$$ we currently assume there are no gaps

    var spreadIndices = [null, null];
    if ('rl' == this.pageProgression) {
        // Right to Left
        if (this.getPageSide(pindex) == 'R') {
            spreadIndices[1] = pindex;
            spreadIndices[0] = pindex + 1;
        } else {
            // Given index was LHS
            spreadIndices[0] = pindex;
            spreadIndices[1] = pindex - 1;
        }
    } else {
        // Left to right
        if (this.getPageSide(pindex) == 'L') {
            spreadIndices[0] = pindex;
            spreadIndices[1] = pindex + 1;
        } else {
            // Given index was RHS
            spreadIndices[1] = pindex;
            spreadIndices[0] = pindex - 1;
        }
    }

    //console.log("   index %d mapped to spread %d,%d", pindex, spreadIndices[0], spreadIndices[1]);

    return spreadIndices;
}

// Remove the page number assertions for all but the highest index page with
// a given assertion.  Ensures there is only a single page "{pagenum}"
// e.g. the last page asserted as page 5 retains that assertion.
br.uniquifyPageNums = function() {
    if (br.pageNums.length == 0)return;
    var seen = {};

    for (var i = br.pageNums.length - 1; i--; i >= 0) {
        var pageNum = br.pageNums[i];
        if ( !seen[pageNum] ) {
            seen[pageNum] = true;
        } else {
            br.pageNums[i] = null;
        }
    }

}

br.cleanupMetadata = function() {
    br.uniquifyPageNums();
}

// getEmbedURL
//________
// Returns a URL for an embedded version of the current book
br.getEmbedURL = function(viewParams) {
    // We could generate a URL hash fragment here but for now we just leave at defaults
    var url = 'https://' + window.location.host + '/stream/'+this.bookId;
    if (this.subPrefix != this.bookId) { // Only include if needed
        url += '/' + this.subPrefix;
    }
    url += '?ui=embed';
    if (typeof(viewParams) != 'undefined') {
        url += '#' + this.fragmentFromParams(viewParams);
    }
    return url;
}

// getEmbedCode
//________
// Returns the embed code HTML fragment suitable for copy and paste
br.getEmbedCode = function(frameWidth, frameHeight, viewParams) {
    return "<iframe src='" + this.getEmbedURL(viewParams) + "' width='" + frameWidth + "' height='" + frameHeight + "' frameborder='0' ></iframe>";
}


// getOpenLibraryRecord
br.getOpenLibraryRecord = function(callback) {
    // Try looking up by ocaid first, then by source_record

    var self = this; // closure

    var jsonURL = self.olHost + '/query.json?type=/type/edition&*=&ocaid=' + self.bookId;
    $.ajax({
        url: jsonURL,
        success: function(data) {
            if (data && data.length > 0) {
                callback(self, data[0]);
            } else {
                // try sourceid
                jsonURL = self.olHost + '/query.json?type=/type/edition&*=&source_records=ia:' + self.bookId;
                $.ajax({
                    url: jsonURL,
                    success: function(data) {
                        if (data && data.length > 0) {
                            callback(self, data[0]);
                        }
                    },
                    dataType: 'jsonp'
                });
            }
        },
        dataType: 'jsonp'
    });
}

br.buildInfoDiv = function(jInfoDiv) {
    // $$$ it might make more sense to have a URL on openlibrary.org that returns this info

    var escapedTitle = BookReader.util.escapeHTML(this.bookTitle);
    var domainRe = /(\w+\.(com|org))/;
    var domainMatch = domainRe.exec(this.bookUrl);
    var domain = this.bookUrl;
    if (domainMatch) {
        domain = domainMatch[1];
    }

    // $$$ cover looks weird before it loads
    jInfoDiv.find('.BRfloatCover').append([
                '<div style="height: 140px; min-width: 80px; padding: 0; margin: 0;"><a href="', this.bookUrl, '"><img src="//archive.org/download/', this.bookId, '/page/cover_t.jpg" alt="' + escapedTitle + '" height="140px" /></a></div>'].join('')
    );

    var download_links = [];
    if (!this.olAuth) {
        download_links = [
            '<h3>Other Formats</h3>',
            '<ul class="links">',
            '<li><a href="//archive.org/download/', this.bookId, '/', this.subPrefix, '.pdf">PDF</a><span>|</span></li>',
            '<li><a href="//archive.org/download/', this.bookId, '/', this.subPrefix, '_djvu.txt">Plain Text</a><span>|</span></li>',
            '<li><a href="//archive.org/download/', this.bookId, '/', this.subPrefix, '_daisy.zip">DAISY</a><span>|</span></li>',
            '<li><a href="//archive.org/download/', this.bookId, '/', this.subPrefix, '.epub">ePub</a><span>|</span></li>',
            '<li><a href="https://www.amazon.com/gp/digital/fiona/web-to-kindle?clientid=IA&itemid=', this.bookId, '&docid=', this.subPrefix, '">Send to Kindle</a></li>',
            '</ul>'
        ];
    }

    download_links.push('<p class="moreInfo"><span></span>More information on <a href="'+ this.bookUrl + '">' + domain + '</a>  </p>');

    jInfoDiv.find('.BRfloatMeta').append(download_links.join('\n'));

    jInfoDiv.find('.BRfloatFoot').append([
        '<span>|</span>',
        '<a href="https://openlibrary.org/contact" class="problem">Report a problem</a>',
    ].join('\n'));

    if (domain == 'archive.org') {
        jInfoDiv.find('.BRfloatMeta p.moreInfo span').css(
                {'background': 'url(https://archive.org/favicon.ico) no-repeat', 'width': 22, 'height': 18 }
        );
    }

    jInfoDiv.find('.BRfloatTitle a').attr({'href': this.bookUrl, 'alt': this.bookTitle}).text(this.bookTitle);
    var bookPath = (window.location + '').replace('#','%23');
    jInfoDiv.find('a.problem').attr('href','https://openlibrary.org/contact?path=' + bookPath);

}

br.pageW =  [
    1305,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1261,1305            ];

br.pageH =  [
    2001,1934,1916,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,1983,2001            ];
br.leafMap = [
    0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115            ];

br.pageNums = [
    null,null,3,null,5,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null            ];


br.numLeafs = br.pageW.length;


br.bookId   = 'cu31924020438929';
br.zip      = '\x2F27\x2Fitems\x2Fcu31924020438929\x2Fcu31924020438929_jp2.zip';
br.subPrefix = 'cu31924020438929';
br.server   = 'ia600207.us.archive.org';
br.bookTitle= 'Studies\x20in\x20Jamaica\x20history';
br.bookPath = '\x2F27\x2Fitems\x2Fcu31924020438929\x2Fcu31924020438929';
br.bookUrl  = 'https://archive.org/details/cu31924020438929';
br.imageFormat = 'jp2';
br.archiveFormat = 'zip';


br.pageProgression = 'lr';
br.olHost = 'https://openlibrary.org';
br.olAuthUrl = null;
br.olAuth = false;

// Check for config object
// $$$ change this to use the newer params object
if (typeof(brConfig) != 'undefined') {
    if (typeof(brConfig["ui"]) != 'undefined') {
        br.ui = brConfig["ui"];
    }

    if (brConfig['mode'] == 1) {
        br.mode = 1;
        if (typeof(brConfig['reduce'] != 'undefined')) {
            br.reduce = brConfig['reduce'];
        }
    } else if (brConfig['mode'] == 2) {
        br.mode = 2;
    }

    if (typeof(brConfig["isAdmin"]) != 'undefined') {
        br.isAdmin = brConfig["isAdmin"];
    } else {
        br.isAdmin = false;
    }

    if (typeof(brConfig["theme"]) != 'undefined') {
        br.theme = brConfig["theme"];
    }
} // brConfig



function OLAuth() {
    this.olConnect = false;
    this.loanUUID = false;
    this.permsToken = false;

    var cookieRe = /;\s*!/;
    var cookies = document.cookie.split(cookieRe);
    var length = cookies.length;
    var i;
    for (i=0; i<length; i++) {
        if (0 == cookies[i].indexOf('br-loan-' + br.bookId)) {
            this.loanUUID = cookies[i].split('=')[1];
        }
        if (0 == cookies[i].indexOf('loan-' + br.bookId)) {
            this.permsToken = cookies[i].split('=')[1];
        }

        // Set olHost to use if passed in
        if (0 == cookies[i].indexOf('ol-host')) {
            br.olHost = 'https://' + unescape(cookies[i].split('=')[1]);
        }

        if (0 == cookies[i].indexOf('ol-auth-url')) {
            br.olAuthUrl = unescape(cookies[i].split('=')[1]);
        }
    }

    if (br.olAuthUrl == null) {
        br.olAuthUrl = 'https://archive.org/bookreader/BookReaderAuthProxy.php?id=XXX';
    }

    this.authUrl = br.olAuthUrl.replace("XXX", br.bookId);
    return this;
}

function add_query_param(url, name, value) {
    // Use & if the url already has some query parameters.
    // Use ? otherwise.
    var prefix = (url.indexOf("?") >= 0) ? "&" : "?";
    return url + prefix + name + "=" + value;
}

OLAuth.prototype.init = function() {
    var htmlStr =  'Checking loan status';

    this.showPopup("#F0EEE2", "#000", htmlStr, 'Please wait as we check the status of this book...');
    this.callAuthUrl();
}

OLAuth.prototype.callAuthUrl = function() {
    var authUrl = this.authUrl;

    // be sure to add random param to authUrl to avoid stale cache
    authUrl = add_query_param(authUrl, 'rand', Math.random());

    if (false !== this.loanUUID) {
        authUrl = add_query_param(authUrl, 'loan', this.loanUUID);
    }
    if (false !== this.permsToken) {
        authUrl = add_query_param(authUrl, 'token', this.permsToken);
    }
    $.ajax({url:authUrl, dataType:'jsonp', jsonpCallback:'olAuth.initCallback'});
}

OLAuth.prototype.showPopup = function(bgColor, textColor, msg, resolution) {
    this.popup = document.createElement("div");
    $(this.popup).css({
        position: 'absolute',
        top:      '50px',
        left:     ($('#BookReader').attr('clientWidth')-400)/2 + 'px',
        width:    '400px',
        padding:  "15px",
        border:   "3px double #999999",
        zIndex:   3,
        textAlign: 'center',
        backgroundColor: bgColor,
        color: textColor
    }).appendTo('#BookReader');

    this.setPopupMsg(msg, resolution);

}

OLAuth.prototype.setPopupMsg = function(msg, resolution) {
    this.popup.innerHTML = ['<p><strong>', msg, '</strong></p><p>', resolution, '</p>'].join('\n');
}

OLAuth.prototype.showError = function(msg, resolution) {
    $(this.popup).css({
        backgroundColor: "#fff",
        color: "#000"
    });

    this.setPopupMsg(msg, resolution);
}

OLAuth.prototype.initCallback = function(obj) {
    if (false == obj.success) {
        if (br.isAdmin) {
            ret = confirm("We couldn't authenticate your loan with Open Library, but since you are an administrator or uploader of this book, you can access this book for QA purposes. Would you like to QA this book?");
            if (!ret) {
                this.showError(obj.msg, obj.resolution)
            } else {
                br.init();
            }
        } else {
            this.showError(obj.msg, obj.resolution)
        }
    } else {
        //user is authenticated
        this.setCookie(obj.token);
        this.olConnect = true;
        this.startPolling();
        br.init();
    }
}

OLAuth.prototype.callback = function(obj) {
    if (false == obj.success) {
        this.showPopup("#F0EEE2", "#000", obj.msg, obj.resolution);
        clearInterval(this.poller);
        this.ttsPoller = null;
    } else {
        this.olConnect = true;
        this.setCookie(obj.token);
    }
}

OLAuth.prototype.setCookie = function(value) {
    var date = new Date();
    date.setTime(date.getTime()+(10*60*1000));  //10 min expiry
    var expiry = date.toGMTString();
    var cookie = 'loan-'+br.bookId+'='+value;
    cookie    += '; expires='+expiry;
    cookie    += '; path=/; domain=.archive.org;';
    document.cookie = cookie;
    this.permsToken = value;

    //refresh the br-loan uuid cookie with current expiry, if needed
    if (false !== this.loanUUID) {
        cookie = 'br-loan-'+br.bookId+'='+this.loanUUID;
        cookie    += '; expires='+expiry;
        cookie    += '; path=/; domain=.archive.org;';
        document.cookie = cookie;
    }
}

OLAuth.prototype.deleteCookies = function() {
    var date = new Date();
    date.setTime(date.getTime()-(24*60*60*1000));  //one day ago
    var expiry = date.toGMTString();
    var cookie = 'loan-'+br.bookId+'=""';
    cookie    += '; expires='+expiry;
    cookie    += '; path=/; domain=.archive.org;';
    document.cookie = cookie;

    cookie = 'br-loan-'+br.bookId+'=""';
    cookie    += '; expires='+expiry;
    cookie    += '; path=/; domain=.archive.org;';
    document.cookie = cookie;
}

OLAuth.prototype.startPolling = function () {
    var self = this;
    this.poller=setInterval(function(){
        if (!self.olConnect) {
            self.showPopup("#F0EEE2", "#000", 'Connection error', 'The BookReader cannot reach Open Library. This might mean that you are offline or that Open Library is down. Please check your Internet connection and refresh this page or try again later.');
            clearInterval(self.poller);
            self.ttsPoller = null;
        } else {
            self.olConnect = false;
            self.callAuthUrl();
        }
    },300000);   //five minute interval
}

br.cleanupMetadata();
if (br.olAuth) {
    var olAuth = new OLAuth();
    olAuth.init();
} else {
    br.init();
}

*/
