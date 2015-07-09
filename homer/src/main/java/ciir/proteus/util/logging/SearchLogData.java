package ciir.proteus.util.logging;

/**
 * Created by michaelz on 7/8/2015.
 */
public class SearchLogData extends LogData {

  private String enteredQuery; // query as typed in by the user
  private String expandedQuery; // query expanded to Galago query language
  private String corpus; // name of the corpus being searched
  private String kind; // what "kind" we're searching
  private String labels; // any labels that were being searched
  private Integer numResults; // how many results were requested
  private Integer startAt; // what result to start at

  public SearchLogData(String id, String user) {
    super(id, user);
  }

  @Override
  String getAction() {
    return "SEARCH";
  }

  @Override
  public String toTSV() {

      return getCommon() + "\t"
            + enteredQuery + "\t"
            + expandedQuery + "\t"
            + labels + "\t"
            + corpus + "\t"
            + kind + "\t"
            + numResults + "\t"
            + startAt;
  }

  @Override
  public String toHTML() {
    return null;
  }

  public void setEnteredQuery(String enteredQuery) {
    this.enteredQuery = enteredQuery;
  }

  public void setExpandedQuery(String expandedQuery) {
    this.expandedQuery = expandedQuery;
  }

  public void setCorpus(String corpus) {
    this.corpus = corpus;
  }

  public void setKind(String kind) {
    this.kind = kind;
  }

  public void setLabels(String labels) {
    this.labels = labels;
  }

  public void setNumResults(Integer numResults) {
    this.numResults = numResults;
  }

  public void setStartAt(Integer startAt) {
    this.startAt = startAt;
  }
}
