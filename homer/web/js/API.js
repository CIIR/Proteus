
var API = {};

// -- factory for generating API calls based on type and url
var method = function(method, url) {
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
API.deleteTags = method("POST", "/api/tags/delete");
API.login = method("POST", "/api/login");
API.logout = method("POST", "/api/logout");
API.register = method("POST", "/api/register");
API.getAllTagsByUser = method("POST", "/api/alltags");
API.getResourcesForLabels = method("POST", "/api/resourcesforlabels");
