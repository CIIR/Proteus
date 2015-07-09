package ciir.proteus.util.logging;

/**
 * Created by michaelz on 7/8/2015.
 */
public class RateResourceLogData extends LogData {

  private String resource;
  private Integer corpus;

  private Integer rating;
  private String corpusName;

  public RateResourceLogData(String id, String user) {
    super(id, user);
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public void setCorpus(Integer corpus) {
    this.corpus = corpus;
  }

  public void setCorpusName(String corpusName) {
    this.corpusName = corpusName;
  }

  public void setRating(Integer rating) {
    this.rating = rating;
  }

  @Override
  String getAction() {
    return "ADD-TAG";
  }

  @Override
  public String toTSV() {

      return getCommon() + "\t"
            + resource + "\t"
              + corpus + "\t"
              + corpusName + "\t"
            + rating;
  }

  @Override
  public String toHTML() {
    return null;
  }


}
