var jsUser = "proteusJSTestUser";
API.register({user: jsUser});

test("hello test", function() {
  ok(1 == "1", "Javascript is weird!");
});
asyncTest("/search", function() {
  API.search({q: "romeo", kind: "books"}, function(data) {
    equal( 1, data.results.length, "found one book for romeo!" );
    start();
  });
});
asyncTest("/metadata", function() {
  API.search({q: "romeo", kind: "books"}, function(data) {
    equal( 1, data.results.length, "found one book for romeo!" );
    API.metadata({id: data.results[0].name}, function(data) {
      ok(!isBlank(data.title));
      start();
    });
  });
});

asyncTest("/api/tags", function() {
  API.login({user:jsUser}, function(data) {
    var token = data.token;
    API.createTags({user: jsUser, token: token, tags: {fake: ["res0", "res1"]}}, function(done) {
        API.getTags({user: jsUser, token: token, resource: ["res0", "res1"]}, function(data) {
          ok("fake", data.res0);
          ok("fake", data.res1);
          start();
        });
      });
  });
});

