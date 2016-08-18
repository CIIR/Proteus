/**
 * Created by michaelz on 8/3/2016.
 */

// override alert so this script can run without supervision.
function alert(msg){
  console.log(msg);
}

QUnit.module( "Utilities" );
QUnit.test( "JQuery Escape", function( assert ) {
  var id = 'NothingSpecial';
  var newid = jqEsc(id)
  assert.equal( id, newid);

  id = 'i.have.dots';
  newid = jqEsc(id)
  assert.equal( "i\\.have\\.dots", newid);

  id = 'all.special:chars[]';
  newid = jqEsc(id)
  assert.equal( "all\\.special\\:chars\\[\\]", newid);
});

QUnit.test( "Parse Internet Archive ID", function( assert ) {
  var pageid = 'id_123';
  var obj = parsePageID(pageid);
  assert.equal(obj.id, "id");
  assert.equal(obj.page,"123");
  assert.equal(obj.page,123);
  assert.equal(obj.note,"");

  // make sure cache was updated and we use it
  assert.equal(parsedPageCache.size, 1);
  assert.equal(parsedPageCache.get(pageid), obj);
  var newobj = parsePageID(pageid);
  assert.equal(newobj.cached, true);

  // multiple underscores
  pageid = 'poems___00wott_191';
  obj = parsePageID(pageid);
  assert.equal(obj.id, "poems___00wott");
  assert.equal(obj.page,"191");
  assert.equal(obj.note,"");

  // no page number
  pageid = 'archiveid';
  obj = parsePageID(pageid);
  assert.equal(obj.id, "archiveid");
  assert.equal(obj.page,"");
  assert.equal(obj.note,"");

  // NaN page number
  pageid = 'archiveid_foo';
  obj = parsePageID(pageid);
  assert.equal(obj.id, pageid);
  assert.equal(obj.page, "");
  assert.equal(obj.note,"");

  pageid = 'archiveid_123_foo';
  obj = parsePageID(pageid);
  assert.equal(obj.id, pageid);
  assert.equal(obj.page, "");
  assert.equal(obj.note,"");

  pageid = 'archiveid_123xyz';
  obj = parsePageID(pageid);
  assert.equal(obj.id, pageid);
  assert.equal(obj.page, "");
  assert.equal(obj.note,"");

  // id is a number
  pageid = '123_456';
  obj = parsePageID(pageid);
  assert.equal(obj.id, "123");
  assert.equal(obj.page, "456");
  assert.equal(obj.note,"");

  pageid = '123';
  obj = parsePageID(pageid);
  assert.equal(obj.id, "123");
  assert.equal(obj.page, "");
  assert.equal(obj.note,"");

  // test note numbers
  pageid = 'archiveid_123_456';
  obj = parsePageID(pageid);
  assert.equal(obj.id, "archiveid");
  assert.equal(obj.page, "123");
  assert.equal(obj.note,"456");

  pageid = 'poems___00wott_191_23';
  obj = parsePageID(pageid);
  assert.equal(obj.id, "poems___00wott");
  assert.equal(obj.page,"191");
  assert.equal(obj.note,"23");

  pageid = '123_456_789';
  obj = parsePageID(pageid);
  assert.equal(obj.id, "123");
  assert.equal(obj.page,"456");
  assert.equal(obj.note,"789");

  pageid = '123_456_789_101112';
  obj = parsePageID(pageid);
  assert.equal(obj.id, "123_456");
  assert.equal(obj.page,"789");
  assert.equal(obj.note,"101112");

  pageid = '123_456_789_101112x';
  obj = parsePageID(pageid);
  assert.equal(obj.id, "123_456_789_101112x");
  assert.equal(obj.page,"");
  assert.equal(obj.note,"");

});

QUnit.test( "Capitalize Each Word", function( assert ) {
  var sentence = 'one two three';
  assert.equal("One Two Three", sentence.capitalizeEachWord());

  sentence = 'onE two tHree';
  assert.equal("OnE Two THree", sentence.capitalizeEachWord());

});

QUnit.test( "Trim", function( assert ) {
  var sentence = ' start';
  assert.equal("start", sentence.trim());

  sentence = 'end ';
  assert.equal("end", sentence.trim());

  sentence = '     start'; // multiple spaces
  assert.equal("start", sentence.trim());

  sentence = 'end    ';
  assert.equal("end", sentence.trim());

  sentence = ' both ';
  assert.equal("both", sentence.trim());

  sentence = '   both   ';
  assert.equal("both", sentence.trim());

  sentence = '\tstartTab';
  assert.equal("startTab", sentence.trim());

  sentence = 'endTab\t';
  assert.equal("endTab", sentence.trim());

  sentence = ' \t  mixed  \t '; // mixed tabs and spaces
  assert.equal("mixed", sentence.trim());

});

QUnit.test( "Is Blank?", function( assert ) {
  var sentence = ' start';

  assert.notOk(isBlank(sentence));

  sentence = 'end    ';
  assert.equal(false, isBlank(sentence));

  sentence = '     ';
  assert.equal(true, isBlank(sentence));

  sentence = '\t';
  assert.equal(true, isBlank(sentence));

  sentence = '  \t   ';
  assert.equal(true, isBlank(sentence));

  var empty;
  assert.equal(true, isBlank(empty));

  assert.equal(true, isBlank(undefined));

});

QUnit.test( "Guess Kind", function( assert ) {

  var id = 123;
  assert.equal('article', guessKind(id));

  var id2 = '123';
  assert.equal('article', guessKind(id2));

  var id3 = "abc";
  assert.equal('ia-books', guessKind(id3));

  id3 = "abc_123";
  assert.equal('ia-pages', guessKind(id3));

  id3 = "abc_123_456";
  assert.equal('ia-notes', guessKind(id3));

  id3 = "abc_123_456_789";
  assert.equal(undefined, guessKind(id3));

});

var invalidID = 'dyescoinencXXyclop00dyejiala';
var validID = 'dyescoinencyclop00dyejiala';

QUnit.test( "getInternetArchiveJS - Invalid ID", function( assert ) {

  var done = assert.async();

  // First call with invalid ID so book reader will be undefined.
  getInternetArchiveJS(invalidID, function(){
    assert.equal(true, _.isUndefined(getBookReader()));
    done();
  });

});

QUnit.test( "getInternetArchiveJS - valid ID", function( assert ) {

  var done = assert.async();

  // valid archive id
  getInternetArchiveJS(validID, function(){
    var tmpBR = getBookReader();
    assert.equal(false, _.isUndefined(tmpBR));
    assert.equal(validID, tmpBR.getBookID());
    done();
  });

});

QUnit.test( "getInternetArchiveJS - book id mismatch", function( assert ) {

  var done = assert.async();

  // invalid archive id - book reader should be the prior one
  getInternetArchiveJS(invalidID, function(){
    var tmpBR = getBookReader();
    assert.equal(false, _.isUndefined(tmpBR));
    assert.equal(validID, tmpBR.getBookID());
    done();
  });

});


QUnit.test( "getInternetArchiveMetadata - Invalid ID", function( assert ) {

  var done = assert.async();

  // First call with invalid ID so book reader will be undefined.
  var data = {};
  assert.equal(true, _.isUndefined(data.metadata));
  getInternetArchiveMetadata(invalidID, data, function(){
    assert.equal(true, _.isUndefined(data.metadata));
    done();
  });

});

QUnit.test( "getInternetArchiveMetadata - valid ID", function( assert ) {

  var done = assert.async();

  // valid archive id
  var data = {};
  assert.equal(true, _.isUndefined(data.metadata));
  getInternetArchiveMetadata(validID, data, function(){
    assert.equal(false, _.isUndefined(data.metadata));
    assert.ok(_.size(data.metadata) > 0);
    assert.equal(true, _.isUndefined(data.metadata.cached));
    done();
  });

});

QUnit.test( "getInternetArchiveMetadata - cached", function( assert ) {

  var done = assert.async();

  // valid archive id - should use cached value
  var data = {};
  assert.equal(true, _.isUndefined(data.metadata));
  getInternetArchiveMetadata(validID, data, function(){
    assert.equal(false, _.isUndefined(data.metadata));
    assert.ok(_.size(data.metadata) > 0);
    assert.equal(false, _.isUndefined(data.metadata.cached));
    done();
  });

});
