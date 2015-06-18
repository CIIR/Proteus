
var API = {};


var queryWikipedia = function(terms){

    var safeTerms = encodeURI(terms);

  return  method("GET", "http://en.wikipedia.org/w/api.php?action=query&prop=revisions&callback=jsonp_handler&format=json&prop=info&inprop=url&titles=" + terms);

}

// -- factory for generating API calls based on type and url
var method = function(method, url, terms) {

    if (!_.isUndefined(terms)){
        url = "http://en.wikipedia.org/w/api.php?action=query&prop=revisions&format=json&prop=info&inprop=url&titles=" + terms;
        url = 'http://en.wikipedia.org/w/api.php?action=query&callback=jsonp_handler&titles=San_Francisco&prop=images&imlimit=20&format=json';
    }


    var ajaxOpts = {
        type: method,
        url: url,
        dataType: "json",
        contentType: "application/json",
        processData: false
    };
    var dataFn = JSON.stringify;
    if (method == "GET") {
        dataFn = _.identity;
        ajaxOpts = {
            type: method,
            url: url
        };
    }

    // -- this is the real signature of the API calls
    return function(options, doneCallback, errorCallback) {

        ajaxOpts.data = dataFn(options);

        console.log(ajaxOpts);
        $.ajax(ajaxOpts).done(function(data) {
            if (!doneCallback) {
                return;
            }
            data.request = options;
            doneCallback(data);
        }).error(errorCallback);
    };
};

API.action = method("POST", "/api/action");
API.getKinds = method("GET", "/api/kinds");
API.getTags = method("GET", "/api/tags");
API.createTags = method("POST", "/api/tags/create");
API.updateTags = method("POST", "/api/tags/update");
API.deleteTags = method("POST", "/api/tags/delete");
API.login = method("POST", "/api/login");
API.logout = method("POST", "/api/logout");
API.register = method("POST", "/api/register");
API.getAllTagsByUser = method("POST", "/api/alltags");
API.getResourcesForLabels = method("POST", "/api/resourcesforlabels");
API.getUsers = method("GET", "/api/users");
API.newCorpus = method("POST", "/api/newcorpus");
API.updateUserSettings = method("POST", "/api/updatesettings");
API.rateResource = method("POST", "/api/rateresource");
API.callWikipedia = method("GET", "http://en.wikipedia.org/w/api.php?action=query&prop=revisions&format=json&prop=info&inprop=url&titles=" );