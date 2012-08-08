// - SearchRequest
// - SearchResponse
// - LookupRequest
// - LookupResponse
// - TransformRequest
// - TransformResponse
namespace java ciir.proteus

// The Core Proteus Types
enum ProteusType {
  COLLECTION = 0,
  PAGE = 1,
  PICTURE = 2,
  VIDEO = 3,
  AUDIO = 4,
  PERSON = 5,
  LOCATION = 6,
  ORGANIZATION = 7,
  MISCELLANEOUS = 8,
  TOPIC = 9
}

// Specify containment
// A mapping of objects to what objects may contain them. A reverse mapping
// of 'contains', defined below.
const map<string, list<string> > isContainedBy = { 
  "page" : [ "collection" ],
  "picture" : [ "page", "collection" ],
  "video" : [ "page", "collection" ],
  "audio" : [ "page", "collection" ],
  "person" : ["page", "picture", "video", "audio", "collection"],
  "location" : [ "page", "picture", "video", "audio", "collection" ],
  "organization" : [ "page", "picture", "video", "audio", "collection" ],
  "miscellaneous" : [ "page", "picture", "video", "audio", "collection" ]
}

// What a given type can have contents of type
// For example, the first mapping states that a collection can contain
// pages. The next mapping says a page may contain picutres, videos,
// and so on.
//
// Note that we define "contains" w.r.t. the modality in question,
// for example an audio object may contain persons, locations, but 
// in the sense of them being mentioned, whereas a page would contain
// the text of those entities.
const map<string, list<string>> contains = {
  "collection" : [ "page", "picture", "video", "audio",
  	     "person", "location", "organization", "miscellaneous"],
  "page" : [ "picture", "video", "audio",
  	     "person", "location", "organization", "miscellaneous"],
  "picture" : [ "person", "location", "organization", "miscellaneous" ],
  "video" : [ "person", "location", "organization", "miscellaneous" ],
  "audio" : [ "person", "location", "organization", "miscellaneous" ]
}

// List atomic structs first

// Information needed to access a specific resource item
// This object represents a "unique identifier" in the
// Proteus space.
struct AccessIdentifier {
  // Identifier for the accessing a result, used for lookup of an object.
  1: string identifier,
  2: ProteusType type,
  3: string resource_id,
  4: optional string error,
}

// Parameters used in searching and transformations
struct RequestParameters {
  1: i32 num_results_requested = 10,
  2: i32 start_at = 0,
  3: string language = "en",
}

// **** Query Type Structs ****
// Defines a search query to run
struct SearchRequest {
  1: string raw_query,
  2: list<ProteusType> types,
  3: optional RequestParameters parameters,
}

struct WeightedDate {
  1: i64 date,
  2: double weight = 1.0,
}

// Defines a region in text
struct TextRegion {
  // Starting character index, inclusive.
  1: i32 start,

  // Ending index, exclusive.
  2: i32 stop,
}

// A chunk of text with optional highlighting of regions
struct ResultSummary {
  1: string text,
  2: list<TextRegion> highlights,
}

struct WeightedTerm {
  1: string term,
  2: double weight = 1.0,
}

// A list of string (term) : weight (frequency) pairs. Used for language models, and other things.
struct TermList {
  1: list<WeightedTerm> terms,
}

// This IS a posting list, but as map entries.
struct PrefixedTermMap {  
  1: map<string, TermList> term_lists
}

// Organization: An organization mentioned on a page
struct Organization {
  1: optional string full_name,
  2: list<string> alternate_names,
  3: optional string wiki_link,
}

// Dates where this object was mentioned (e.g. a person)
// or dates contained by this object (e.g. collection)
// A list of long values (dates) : weight (frequency) pairs. Used for date mentions, and other things.
struct LongValueList {
  1: list<WeightedDate> dates,
}

// Representation of an abstract collection of ideas.
// Or something.
struct Topic {
 1: optional TermList words,
 2: optional PrefixedTermMap pages
}

// Collection: Book, Newspaper, Website, etc.
// (highest level container of data)
struct Collection {
  1: optional i64 publication_date,
  2: optional string publisher,
  3: optional string edition,
  4: optional i32 num_pages,
  5: list<string> creators,
  6: optional string full_text
}

// Page: Page in a book, page of a newspaper, web page on a site, etc.
struct Page {
  1: optional string full_text,
  2: list<string> creators,
  3: optional i32 page_number,
}

// Coordinates of something on a page
struct Coordinates {
  1: i32 left,
  2: i32 right,
  3: i32 top,
  4: i32 bottom,
}

// Audio: Audio contained on a web page, or other page instance
// (Zero or more per page)
struct Audio {
  1: optional string caption,
  2: optional Coordinates coordinates,
  3: optional i32 length,
  4: list<string> creators,
}

// Picture: Picture on a page, on a webpage, etc.
// (There can be zero or more of these on a page)
struct Picture {
  1: optional string caption,
  2: optional Coordinates coordinates,
  3: list<string> creators,
}

struct PictureList {
  1: list<Picture> pictures
}

// Video: Video contained on a web page, or other page instance
// (Zero or more per page)
struct Video {
  1: optional string caption,
  2: optional Coordinates coordinates,
  3: optional i32 length_in_seconds,
  4: list<string> creators,
}

// Person: A person entity mentioned on a page
// (in text, picture, video, or audio)
struct Person {
  1: optional string full_name,
  2: list<string> alternate_names,
  3: optional string wiki_link,
  4: optional i64 birth_date,
  5: optional i64 death_date,
}

// Location: A location entity mentioned on a page
struct Location {
  1: optional string full_name,
  2: list<string> alternate_names,
  3: optional string wiki_link,
  4: optional double longitude,
  5: optional double latitude,
}


struct WikipediaEntity {
 1: string wikipedia_id,
 2: string display_name,
 3: string external_link,
 4: list<string> image_links,
 5: optional string rdf_triples
 6: optional i32 num_mentions
}

struct KeywordsInContext {
 1: AccessIdentifier id,
 2: string text_content,
 3: string external_id
}

// **** Proteus Typed Data Structures ****
// We create a single ProteusObject struct, that is
// then specialized via composition with lower level
// optional structs
struct ProteusObject {
  1: AccessIdentifier id,
  2: optional string title,
  3: optional string description,
  4: optional string img_url,
  5: optional string thumb_url,
  6: optional string external_url,
  7: optional LongValueList date_freq,
  8: optional TermList language_model,
  9: string language = "en",

  // Each of the composed struct types
  // Only one of these should be filled in for a given instance.
  10: optional Collection collection,
  11: optional Page page,
  12: optional Picture picture,
  13: optional Video video,
  14: optional Audio audio,
  15: optional Person person,
  16: optional Location location,
  17: optional Organization organization,
  18: optional Topic topic,
  19: optional WikipediaEntity external_entity
    
  // Leave room for Jeff to expand
  50: optional list<KeywordsInContext> contexts
}

// Defines a single result item from a search query
struct SearchResult {
  1: AccessIdentifier id,
  2: double score;
  3: optional string title,
  4: optional ResultSummary summary,
  5: optional string img_url,
  6: optional string thumb_url,
  // URL for visiting the original data source for this item
  7: optional string external_url,
  8: optional ProteusObject full_object
}

// **** Response Type Structs ****
// The response struct to a search query
struct SearchResponse {
  1: list<SearchResult> results,
  2: optional string error,
}

enum TransformType {
  TO_CONTAINER = 0,
  TO_CONTENTS = 1,
  OVERLAP = 2,
  OBJECT_OF = 3,
  SUBJECT_OF = 4,
  HAS_AS_OBJECT = 5,
  HAS_AS_SUBJECT = 6,
  GEOGRAPHIC_PROX = 7,
}

struct TransformRequest {
  1: TransformType transform_type,
  2: AccessIdentifier reference_id,
  3: optional ProteusType target_type,
  4: optional RequestParameters params,
  // This is only used for *_OF and *_AS_* transforms
  5: optional string term,
  // Only used for the geographic proximity transform
  6: optional i32 radius_in_miles = 10,
}

struct TransformResponse {
  1: list<ProteusObject> objects,
}

struct RelatedRequest {
  1: list<SearchResult> beliefs,
  2: list<ProteusType> target_types,
}

// Something to look up one or more objects
struct LookupRequest {
  1: list<AccessIdentifier> ids,
}

struct LookupResponse {
  1: list<ProteusObject> objects,
}

struct CollectionInfo {
 1: ProteusType type,
 2: i64 num_docs,
 3: i64 vocab_size,
 4: i64 num_tokens,
 5: list<string> fields,
} 

struct LinkInfo {
 1: ProteusType src,
 2: ProteusType target,
 3: i64 num_links,
}

struct StatusResponse {
  1: string siteId,
  2: list<CollectionInfo> collectionData,
  3: list<LinkInfo> linkData,
}

service ProteusProvider {
  SearchResponse search(1:SearchRequest srequest),
  LookupResponse lookup(1:LookupRequest lrequest),
  TransformResponse transform(1:TransformRequest trequest),
  SearchResponse related(1:RelatedRequest rrequest),
  StatusResponse status(),
}

struct Target {
  1: string id,
  2: string type,
  3: optional list<i32> positions,
}

struct IndexLink {
  1: string srcid,
  2: string srctype,
  3: list<Target> target,
}

