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
    super(id, user, "RATE-RES");
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
  public String toTSV() {

    return getCommonTSV() + "\t"
            + resource + "\t"
            + corpus + "\t"
            + corpusName + "\t"
            + rating;
  }



}
