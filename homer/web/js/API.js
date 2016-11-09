
var API = {};


var queryWikipedia = function(terms){

    var safeTerms = encodeURI(terms);

  return  method("GET", "http://en.wikipedia.org/w/api.php?action=query&prop=revisions&callback=jsonp_handler&format=json&prop=info&inprop=url&titles=" + terms);

}

// -- factory for generating API calls based on type and url
var method = function(method, url, terms) {

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

       // console.log(ajaxOpts);
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
API.login = method("POST", "/api/login");
API.logout = method("POST", "/api/logout");
API.register = method("POST", "/api/register");
API.getUsers = method("GET", "/api/users");
API.newCorpus = method("POST", "/api/newcorpus");
API.updateUserSettings = method("POST", "/api/updatesettings");
API.getNotes = method("POST", "/store/annotations/search");
API.getNoteHistory = method("POST", "/api/notehistory");
API.getActivityLog = method("POST", "/api/activitylog");
API.getResourcesInCorpus = method("POST", "/api/resourcesincorpus");
API.updateSubcorpora = method("POST", "/api/updatesubcorpora");
API.voteForResource = method("POST", "/api/resourcevote");

