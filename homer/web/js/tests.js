/**
 * Created by michaelz on 8/3/2016.
 */

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
  assert.equal("id", obj.id);
  assert.equal("123", obj.page);
  assert.equal(123, obj.page);

  // make sure cache was updated and we use it
  assert.equal(1, parsedPageCache.size);
  assert.equal(obj, parsedPageCache.get(pageid));
  var newobj = parsePageID(pageid);
  assert.equal(true, newobj.cached);

  // multiple underscores
  pageid = 'poems___00wott_191';
  obj = parsePageID(pageid);
  assert.equal("poems___00wott", obj.id);
  assert.equal("191", obj.page);

  // no page number
  pageid = 'archiveid';
  obj = parsePageID(pageid);
  assert.equal("archiveid", obj.id);
  assert.equal("", obj.page);

  // NaN page number
  pageid = 'archiveid_foo';
  obj = parsePageID(pageid);
  assert.equal("archiveid", obj.id);
  assert.equal("", obj.page);

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
