/**
 * Created by michaelz on 8/3/2016.
 */

// override alert so this script can run without supervision.
function alert(msg) {
  console.log(msg);
}

QUnit.module("Utilities");

QUnit.test("JQuery Escape", function (assert) {
  var id = 'NothingSpecial';
  var newid = jqEsc(id);
  assert.equal(newid, id);

  id = 'i.have.dots';
  newid = jqEsc(id);
  assert.equal(newid, "i\\.have\\.dots");

  id = 'all.special:chars[]';
  newid = jqEsc(id);
  assert.equal(newid, "all\\.special\\:chars\\[\\]");
});

QUnit.test("Parse Internet Archive ID", function (assert) {
  var pageid = 'id_123';
  var obj = parsePageID(pageid);
  assert.equal(obj.id, "id");
  assert.equal(obj.page, "123");
  assert.equal(obj.page, 123);
  assert.equal(obj.note, "");

  // make sure cache was updated and we use it
  assert.equal(parsedPageCache.size, 1);
  assert.equal(parsedPageCache.get(pageid), obj);
  var newobj = parsePageID(pageid);
  assert.equal(newobj.cached, true);

  // multiple underscores
  pageid = 'poems___00wott_191';
  obj = parsePageID(pageid);
  assert.equal(obj.id, "poems___00wott");
  assert.equal(obj.page, "191");
  assert.equal(obj.note, "");

  // no page number
  pageid = 'archiveid';
  obj = parsePageID(pageid);
  assert.equal(obj.id, "archiveid");
  assert.equal(obj.page, "");
  assert.equal(obj.note, "");

  // NaN page number
  pageid = 'archiveid_foo';
  obj = parsePageID(pageid);
  assert.equal(obj.id, pageid);
  assert.equal(obj.page, "");
  assert.equal(obj.note, "");

  pageid = 'archiveid_123_foo';
  obj = parsePageID(pageid);
  assert.equal(obj.id, pageid);
  assert.equal(obj.page, "");
  assert.equal(obj.note, "");

  pageid = 'archiveid_123xyz';
  obj = parsePageID(pageid);
  assert.equal(obj.id, pageid);
  assert.equal(obj.page, "");
  assert.equal(obj.note, "");

  // id is a number
  pageid = '123_456';
  obj = parsePageID(pageid);
  assert.equal(obj.id, "123");
  assert.equal(obj.page, "456");
  assert.equal(obj.note, "");

  pageid = '123';
  obj = parsePageID(pageid);
  assert.equal(obj.id, "123");
  assert.equal(obj.page, "");
  assert.equal(obj.note, "");

  // test note numbers
  pageid = 'archiveid_123_456';
  obj = parsePageID(pageid);
  assert.equal(obj.id, "archiveid");
  assert.equal(obj.page, "123");
  assert.equal(obj.note, "456");

  pageid = 'poems___00wott_191_23';
  obj = parsePageID(pageid);
  assert.equal(obj.id, "poems___00wott");
  assert.equal(obj.page, "191");
  assert.equal(obj.note, "23");

  pageid = '123_456_789';
  obj = parsePageID(pageid);
  assert.equal(obj.id, "123");
  assert.equal(obj.page, "456");
  assert.equal(obj.note, "789");

  pageid = '123_456_789_101112';
  obj = parsePageID(pageid);
  assert.equal(obj.id, "123_456");
  assert.equal(obj.page, "789");
  assert.equal(obj.note, "101112");

  pageid = '123_456_789_101112x';
  obj = parsePageID(pageid);
  assert.equal(obj.id, "123_456_789_101112x");
  assert.equal(obj.page, "");
  assert.equal(obj.note, "");

  pageid = 'Bowdoin_Orient_v.130_no.1-22_1998-1999_83';
  obj = parsePageID(pageid);
  assert.equal(obj.id, "Bowdoin_Orient_v.130_no.1-22_1998-1999");
  assert.equal(obj.page, "83");
  assert.equal(obj.note, "");

});

QUnit.test("Capitalize Each Word", function (assert) {
  var sentence = 'one two three';
  assert.equal(sentence.capitalizeEachWord(), "One Two Three");

  sentence = 'onE two tHree';
  assert.equal(sentence.capitalizeEachWord(), "OnE Two THree");

});

QUnit.test("Trim", function (assert) {
  var sentence = ' start';
  assert.equal(sentence.trim(), "start");

  sentence = 'end ';
  assert.equal(sentence.trim(), "end");

  sentence = '     start'; // multiple spaces
  assert.equal(sentence.trim(), "start");

  sentence = 'end    ';
  assert.equal(sentence.trim(), "end");

  sentence = ' both ';
  assert.equal(sentence.trim(), "both");

  sentence = '   both   ';
  assert.equal(sentence.trim(), "both");

  sentence = '\tstartTab';
  assert.equal(sentence.trim(), "startTab");

  sentence = 'endTab\t';
  assert.equal(sentence.trim(), "endTab");

  sentence = ' \t  mixed  \t '; // mixed tabs and spaces
  assert.equal(sentence.trim(), "mixed");

});

QUnit.test("Is Blank?", function (assert) {
  var sentence = ' start';

  assert.notOk(isBlank(sentence));

  sentence = 'end    ';
  assert.equal(isBlank(sentence), false);

  sentence = '     ';
  assert.equal(isBlank(sentence), true);

  sentence = '\t';
  assert.equal(isBlank(sentence), true);

  sentence = '  \t   ';
  assert.equal(isBlank(sentence), true);

  var empty;
  assert.equal(isBlank(empty), true);

  assert.equal(isBlank(undefined), true);

});

QUnit.test("Guess Kind", function (assert) {

  var id = 123;
  assert.equal(guessKind(id), 'article');

  var id2 = '123';
  assert.equal(guessKind(id2), 'article');

  var id3 = "abc";
  assert.equal(guessKind(id3), 'ia-books');

  id3 = "abc_123";
  assert.equal(guessKind(id3), 'ia-pages');

  id3 = "abc_123_456";
  assert.equal(guessKind(id3), 'ia-notes');

  id3 = "poems___00wott";
  assert.equal(guessKind(id3), 'ia-books');

  id3 = "poems___00wott_191";
  assert.equal(guessKind(id3), 'ia-pages');

  id3 = "poems___00wott_191_1";
  assert.equal(guessKind(id3), 'ia-notes');

  id3 = "abc_123_456_789";
  assert.equal(guessKind(id3), 'ia-notes');

  id3 = "Quincy_Sun_1982_July-Dec";
  assert.equal(guessKind(id3), 'ia-books');

  id3 = "Quincy_Sun_1982_July-Dec_234";
  assert.equal(guessKind(id3), 'ia-pages');

  id3 = "Quincy_Sun_1982_July-Dec_234_99";
  assert.equal(guessKind(id3), 'ia-notes');

});

var invalidID = 'dyescoinencXXyclop00dyejiala';
var validID = 'dyescoinencyclop00dyejiala';

QUnit.test("getInternetArchiveJS - Invalid ID", function (assert) {

  var done = assert.async();

  // First call with invalid ID so book reader will be undefined.
  getInternetArchiveJS(invalidID, function () {
    assert.equal(_.isUndefined(getBookReader()), true);
    done();
  });

});

QUnit.test("getInternetArchiveJS - valid ID", function (assert) {

  var done = assert.async();

  // valid archive id
  getInternetArchiveJS(validID, function () {
    var tmpBR = getBookReader();
    assert.equal(_.isUndefined(tmpBR), false);
    assert.equal(tmpBR.getBookID(), validID);
    done();
  });

});

QUnit.test("getInternetArchiveJS - book id mismatch", function (assert) {

  var done = assert.async();

  // invalid archive id - book reader should be the prior one
  getInternetArchiveJS(invalidID, function () {
    var tmpBR = getBookReader();
    assert.equal(_.isUndefined(tmpBR), false);
    assert.equal(tmpBR.getBookID(), validID);
    done();
  });

});


QUnit.test("getInternetArchiveMetadata - Invalid ID", function (assert) {

  var done = assert.async();

  // First call with invalid ID so book reader will be undefined.
  var data = {};
  assert.equal(_.isUndefined(data.metadata), true);
  getInternetArchiveMetadata(invalidID, data, function () {
    assert.equal(_.isUndefined(data.metadata), true);
    done();
  });

});

QUnit.test("getInternetArchiveMetadata - valid ID", function (assert) {

  var done = assert.async();

  // valid archive id
  var data = {};
  assert.equal(_.isUndefined(data.metadata), true);
  getInternetArchiveMetadata(validID, data, function () {
    assert.equal(_.isUndefined(data.metadata), false);
    assert.ok(_.size(data.metadata) > 0);
    assert.equal(_.isUndefined(data.metadata.cached), true);
    done();
  });

});

QUnit.test("getInternetArchiveMetadata - cached", function (assert) {

  var done = assert.async();

  // valid archive id - should use cached value
  var data = {};
  assert.equal(_.isUndefined(data.metadata), true);
  getInternetArchiveMetadata(validID, data, function () {
    assert.equal(_.isUndefined(data.metadata), false);
    assert.ok(_.size(data.metadata) > 0);
    assert.equal(_.isUndefined(data.metadata.cached), false);
    done();
  });
});


QUnit.test("naturalSortByField", function (assert) {

  var records = [
    {key: 'z', value: 1},
    {key: 'abc', value: 2},
    {key: 'a', value: 11},
    {key: 'c_123', value: 10},
    {key: 'c', value: 0}
  ];

  var sorted = records.naturalSortByField("key");

  assert.equal(records[2], sorted[0]);
  assert.equal(records[1], sorted[1]);
  assert.equal(records[4], sorted[2]);
  assert.equal(records[3], sorted[3]);
  assert.equal(records[0], sorted[4]);

  sorted = records.naturalSortByField("value");

  assert.equal(records[4], sorted[0]);
  assert.equal(records[0], sorted[1]);
  assert.equal(records[1], sorted[2]);
  assert.equal(records[3], sorted[3]);
  assert.equal(records[2], sorted[4]);

});


