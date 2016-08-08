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