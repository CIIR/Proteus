// BSD License (http://lemurproject.org/galago-license)
package org.lemurproject.galago.core.tools;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.lemurproject.galago.core.index.corpus.SplitBTreeKeyWriter;
import org.lemurproject.galago.core.index.disk.PositionIndexWriter;
import org.lemurproject.galago.core.index.disk.PositionFieldIndexWriter;
import org.lemurproject.galago.core.index.corpus.CorpusReader;
import org.lemurproject.galago.core.parse.AdditionalTextCombiner;
import org.lemurproject.galago.core.parse.AnchorTextCreator;
import org.lemurproject.galago.core.index.corpus.CorpusFolderWriter;
import org.lemurproject.galago.core.index.disk.DiskNameReader;
import org.lemurproject.galago.core.index.merge.CorpusMerger;
import org.lemurproject.galago.core.parse.DocumentNumberer;
import org.lemurproject.galago.core.parse.DocumentSource;
import org.lemurproject.galago.core.parse.FieldLengthExtractor;
import org.lemurproject.galago.core.parse.LinkCombiner;
import org.lemurproject.galago.core.parse.LinkExtractor;
import org.lemurproject.galago.core.parse.NumberedDocumentDataExtractor;
import org.lemurproject.galago.core.parse.NumberedExtentExtractor;
import org.lemurproject.galago.core.parse.NumberedFieldExtractor;
import org.lemurproject.galago.core.parse.NumberedExtentPostingsExtractor;
import org.lemurproject.galago.core.parse.NumberedPostingsPositionExtractor;
import org.lemurproject.galago.core.parse.stem.KrovetzStemmer;
import org.lemurproject.galago.core.parse.stem.NullStemmer;
import org.lemurproject.galago.core.parse.stem.Porter2Stemmer;
import org.lemurproject.galago.core.tools.App.AppFunction;
import org.lemurproject.galago.core.types.AdditionalDocumentText;
import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.core.types.ExtractedLink;
import org.lemurproject.galago.core.types.FieldLengthData;
import org.lemurproject.galago.core.types.FieldNumberWordPosition;
import org.lemurproject.galago.core.types.KeyValuePair;
import org.lemurproject.galago.core.types.NumberWordPosition;
import org.lemurproject.galago.core.types.NumberedDocumentData;
import org.lemurproject.galago.core.types.NumberedExtent;
import org.lemurproject.galago.core.types.NumberedField;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.tupleflow.Order;
import org.lemurproject.galago.tupleflow.execution.ConnectionAssignmentType;
import org.lemurproject.galago.tupleflow.execution.InputStep;
import org.lemurproject.galago.tupleflow.execution.Job;
import org.lemurproject.galago.tupleflow.execution.MultiStep;
import org.lemurproject.galago.tupleflow.execution.OutputStep;
import org.lemurproject.galago.tupleflow.execution.Stage;
import org.lemurproject.galago.tupleflow.execution.Step;
import org.lemurproject.galago.tupleflow.types.SerializedParameters;

/**
 *
 * Builds an index using a faster method 
 * (it requires one less sort of the posting lists)
 *
 * @author sjh
 */
public class BuildIndex extends AppFunction {

  public Stage getParsePostingsStage(Parameters buildParameters) throws ClassNotFoundException {
    Stage stage = new Stage("parsePostings");

    // connections
    stage.addInput("splits", new DocumentSplit.FileIdOrder());
    stage.addOutput("fieldLengthData", new FieldLengthData.FieldDocumentOrder());
    stage.addOutput("numberedDocumentDataNumbers", new NumberedDocumentData.NumberOrder());
    stage.addOutput("numberedDocumentDataNames", new NumberedDocumentData.IdentifierOrder());

    if (buildParameters.getBoolean("links")) {
      stage.addInput("anchorText", new AdditionalDocumentText.IdentifierOrder());
    }
    if (buildParameters.getBoolean("corpus")) {
      stage.addOutput("corpusKeys", new KeyValuePair.KeyOrder());
    }
    if (buildParameters.getBoolean("nonStemmedPostings")) {
      stage.addOutput("numberedPostings", new NumberWordPosition.WordDocumentPositionOrder());
    }
    if (buildParameters.getBoolean("stemmedPostings")) {
      for (String stemmer : (List<String>) buildParameters.getList("stemmer")) {
        stage.addOutput("numberedStemmedPostings-" + stemmer, new NumberWordPosition.WordDocumentPositionOrder());
      }
    }
    if (!buildParameters.getMap("tokenizer").getList("fields").isEmpty()) {
      stage.addOutput("numberedExtents", new NumberedExtent.ExtentNameNumberBeginOrder());
    }
    if (!buildParameters.getMap("tokenizer").getMap("formats").isEmpty()) {
      stage.addOutput("numberedFields", new NumberedField.FieldNameNumberOrder());
    }

    if (buildParameters.getBoolean("fieldIndex")) {
      if (buildParameters.getMap("fieldIndexParameters").getBoolean("nonStemmedPostings")) {
        stage.addOutput("numberedExtentPostings", new FieldNumberWordPosition.FieldWordDocumentPositionOrder());
      }
      if (buildParameters.getMap("fieldIndexParameters").getBoolean("stemmedPostings")) {
        for (String stemmer : (List<String>) buildParameters.getMap("fieldIndexParameters").getList("stemmer")) {
          stage.addOutput("numberedExtentPostings-" + stemmer, new FieldNumberWordPosition.FieldWordDocumentPositionOrder());
        }
      }
    }

    // Steps
    stage.add(new InputStep("splits"));
    stage.add(BuildStageTemplates.getParserStep(buildParameters));
    stage.add(BuildStageTemplates.getTokenizerStep(buildParameters));
    stage.add(BuildStageTemplates.getNumberingStep(buildParameters));
    if (buildParameters.getBoolean("links")) {
      Parameters p = new Parameters();
      p.set("textSource", "anchorText");
      stage.add(new Step(AdditionalTextCombiner.class, p));
    }

    MultiStep processingFork = new MultiStep();

    // these forks are always executed
    ArrayList<Step> lengthData =
            BuildStageTemplates.getExtractionSteps("fieldLengthData", FieldLengthExtractor.class,
            new FieldLengthData.FieldDocumentOrder());
    processingFork.groups.add(lengthData);

    ArrayList<Step> documentData =
            BuildStageTemplates.getExtractionSteps("numberedDocumentDataNumbers", NumberedDocumentDataExtractor.class,
            new NumberedDocumentData.NumberOrder());
    processingFork.groups.add(documentData);


    ArrayList<Step> documentDataReverse =
            BuildStageTemplates.getExtractionSteps("numberedDocumentDataNames", NumberedDocumentDataExtractor.class,
            new NumberedDocumentData.IdentifierOrder());
    processingFork.groups.add(documentDataReverse);

    // now optional forks
    if (buildParameters.getBoolean("corpus")) {
      Parameters corpusParameters = buildParameters.getMap("corpusParameters").clone();

      ArrayList<Step> corpusSteps = new ArrayList();
      corpusSteps.add(new Step(CorpusFolderWriter.class, corpusParameters.clone()));
      corpusSteps.add(Utility.getSorter(new KeyValuePair.KeyOrder()));
      corpusSteps.add(new OutputStep("corpusKeys"));
      processingFork.groups.add(corpusSteps);
    }


    if (buildParameters.getBoolean("nonStemmedPostings")) {
      ArrayList<Step> text =
              BuildStageTemplates.getExtractionSteps("numberedPostings", NumberedPostingsPositionExtractor.class,
              new NumberWordPosition.WordDocumentPositionOrder());
      processingFork.groups.add(text);
    }
    if (!buildParameters.getMap("tokenizer").getList("fields").isEmpty()) {
      ArrayList<Step> extents =
              BuildStageTemplates.getExtractionSteps("numberedExtents", NumberedExtentExtractor.class,
              new NumberedExtent.ExtentNameNumberBeginOrder());
      processingFork.groups.add(extents);
    }
    if (!buildParameters.getMap("tokenizer").getMap("formats").isEmpty()) {
      ArrayList<Step> fields =
              BuildStageTemplates.getExtractionSteps("numberedFields", NumberedFieldExtractor.class,
              buildParameters, new NumberedField.FieldNameNumberOrder());
      processingFork.groups.add(fields);
    }

    if (buildParameters.getBoolean("stemmedPostings")) {
      for (String stemmer : (List<String>) buildParameters.getList("stemmer")) {
        // produce a stemmed postings fork.
        ArrayList<Step> stemmingSteps = new ArrayList<Step>();
        stemmingSteps.add(BuildStageTemplates.getStemmerStep(new Parameters(),
                Class.forName(buildParameters.getMap("stemmerClass").getString(stemmer))));
        stemmingSteps.add(new Step(NumberedPostingsPositionExtractor.class));
        stemmingSteps.add(Utility.getSorter(new NumberWordPosition.WordDocumentPositionOrder()));
        stemmingSteps.add(new OutputStep("numberedStemmedPostings-" + stemmer));
        processingFork.groups.add(stemmingSteps);
      }
    }

    if (buildParameters.getBoolean("fieldIndex")) {
      if (buildParameters.getMap("fieldIndexParameters").getBoolean("nonStemmedPostings")) {
        ArrayList<Step> extentPostings =
                BuildStageTemplates.getExtractionSteps("numberedExtentPostings", NumberedExtentPostingsExtractor.class,
                new FieldNumberWordPosition.FieldWordDocumentPositionOrder());
        processingFork.groups.add(extentPostings);
      }

      if (buildParameters.getMap("fieldIndexParameters").getBoolean("stemmedPostings")) {
        for (String stemmer : (List<String>) buildParameters.getMap("fieldIndexParameters").getList("stemmer")) {
          ArrayList<Step> stemmingSteps = new ArrayList<Step>();
          stemmingSteps.add(BuildStageTemplates.getStemmerStep(new Parameters(),
                  Class.forName(buildParameters.getMap("stemmerClass").getString(stemmer))));
          stemmingSteps.add(new Step(NumberedExtentPostingsExtractor.class));
          stemmingSteps.add(Utility.getSorter(new FieldNumberWordPosition.FieldWordDocumentPositionOrder()));
          stemmingSteps.add(new OutputStep("numberedExtentPostings-" + stemmer));
          processingFork.groups.add(stemmingSteps);
        }
      }
    }

    stage.add(processingFork);

    return stage;
  }

  public Stage getParseLinksStage(Parameters buildParameters) {
    Stage stage = new Stage("parseLinks");

    // Connections
    stage.addInput("splits", new DocumentSplit.FileIdOrder());
    stage.addOutput("links", new ExtractedLink.DestUrlOrder());
    stage.addOutput("documentUrls", new NumberedDocumentData.UrlOrder());

    // Steps
    stage.add(new InputStep("splits"));
    stage.add(BuildStageTemplates.getParserStep(buildParameters));
    stage.add(BuildStageTemplates.getTokenizerStep(buildParameters));
    stage.add(new Step(DocumentNumberer.class));

    MultiStep multi = new MultiStep();
    ArrayList<Step> links =
            BuildStageTemplates.getExtractionSteps("links", LinkExtractor.class, new ExtractedLink.DestUrlOrder());
    ArrayList<Step> data =
            BuildStageTemplates.getExtractionSteps("documentUrls", NumberedDocumentDataExtractor.class,
            new NumberedDocumentData.UrlOrder());

    multi.groups.add(links);
    multi.groups.add(data);
    stage.add(multi);

    return stage;
  }

  public Stage getLinkCombineStage() {
    Stage stage = new Stage("linkCombine");

    // Connections
    stage.addInput("documentUrls", new NumberedDocumentData.UrlOrder());
    stage.addInput("links", new ExtractedLink.DestUrlOrder());
    stage.addOutput("anchorText", new AdditionalDocumentText.IdentifierOrder());

    // Steps
    Parameters p = new Parameters();
    p.set("documentDatas", "documentUrls");
    p.set("extractedLinks", "links");
    stage.add(new Step(LinkCombiner.class, p));
    stage.add(new Step(AnchorTextCreator.class));
    stage.add(Utility.getSorter(new AdditionalDocumentText.IdentifierOrder()));
    stage.add(new OutputStep("anchorText"));

    return stage;
  }

  public Stage getWritePostingsStage(Parameters buildParameters, String stageName,
          String inputName, Order inputOrder, String indexName,
          Class indexWriter, String stemmerName) {

    Stage stage = new Stage(stageName);

    stage.addInput(inputName, inputOrder);
    stage.addInput("docCounts", new SerializedParameters.ParametersOrder());

    Parameters p = new Parameters();
    p.set("filename", buildParameters.getString("indexPath") + File.separator + indexName);
    p.set("pipename", "docCounts");
    if (stemmerName != null) {
      p.set("stemmer", buildParameters.getMap("stemmerClass").getString(stemmerName));
    }
    p.set("skipping", buildParameters.getBoolean("skipping"));
    p.set("skipDistance", buildParameters.getLong("skipDistance"));

    stage.add(new InputStep(inputName));
    stage.add(new Step(indexWriter, p));
    return stage;
  }

  public Stage getParallelIndexKeyWriterStage(String name, String input, Parameters indexParameters) {
    Stage stage = new Stage(name);

    stage.addInput(input, new KeyValuePair.KeyOrder());
    stage.add(new InputStep(input));
    stage.add(new Step(SplitBTreeKeyWriter.class, indexParameters));

    return stage;
  }

  public static Parameters checkBuildIndexParameters(Parameters globalParameters) throws Exception {
    ArrayList<String> errorLog = new ArrayList();

    // inputPath may be a string, or a list of strings -- required
    try {
      List<String> inputPath = (List<String>) globalParameters.getAsList("inputPath");
      ArrayList<String> absolutePaths = new ArrayList();
      for (String path : inputPath) {
        absolutePaths.add((new File(path)).getAbsolutePath());
      }
      globalParameters.remove("inputPath");
      globalParameters.set("inputPath", absolutePaths);
    } catch (Exception e) {
      errorLog.add("Parameter 'inputPath' is required. It should be a string or list of strings.");
    }


    // indexPath may be a string
    try {
      String indexPath = globalParameters.getString("indexPath");
      globalParameters.set("indexPath", (new File(indexPath).getAbsolutePath()));
    } catch (Exception e) {
      errorLog.add("Parameter 'indexPath' is required. It should be a string.");
    }


    // skipping turns on skip lists for all index parts [optional]
    // [default = true]
    if (globalParameters.containsKey("skipping")) {
      try {
        boolean skipping = globalParameters.getBoolean("skipping");
      } catch (Exception e) {
        errorLog.add("Parameter 'skipping' should be a boolean. Defaults to true.");
      }
    } else {
      globalParameters.set("skipping", true);
    }

    // skipDistance turns on skip lists for all index parts [optional]
    // [default = 500]
    if (globalParameters.containsKey("skipDistance")) {
      try {
        long skipDist = globalParameters.getLong("skipDistance");
      } catch (Exception e) {
        errorLog.add("Parameter 'skipDistance' should be a long value. Defaults to 500.");
      }
    } else {
      globalParameters.set("skipDistance", 500);
    }


    // corpus may be a boolean [optional parameter]
    // defaults to true
    if (globalParameters.containsKey("corpus")) {
      try {
        boolean corpus = globalParameters.getBoolean("corpus");
      } catch (Exception e) {
        errorLog.add("Parameter 'corpus' should be a boolean. Defaults to true.");
      }
    } else {
      globalParameters.set("corpus", true);
    }

    // corpusParameters must be a map [optional parameter]
    //  + only used if corpus parameter is true
    // defaults to create a compressed 'corpus' as a corpusFolder part
    if (globalParameters.containsKey("corpusParameters")) {
      try {
        Parameters cp = globalParameters.getMap("corpusParameters");
      } catch (Exception e) {
        errorLog.add("Parameter 'corpusParameters' should be a map object. "
                + "Default creates a compressed corpus folder in a folder called 'corpus'. "
                + "This parameter is only active when the 'corpus' parameter is true.");
      }
    }
    // ensure there are some specific parameters
    if (globalParameters.isBoolean("corpus") && globalParameters.getBoolean("corpus")) {
      Parameters corpusParameters = new Parameters();
      corpusParameters.set("readerClass", CorpusReader.class.getName());
      corpusParameters.set("writerClass", CorpusFolderWriter.class.getName());
      corpusParameters.set("mergerClass", CorpusMerger.class.getName());
      // we need a small block size because the stored values are small
      corpusParameters.set("blockSize", globalParameters.get("corpusBlockSize", 512));
      corpusParameters.set("filename", globalParameters.getString("indexPath") + File.separator + "corpus");

      // copy over the other parameters
      if (globalParameters.isMap("corpusParameters")) {
        corpusParameters.copyFrom(globalParameters.getMap("corpusParameters"));
      }

      // insert back into the globalParams
      globalParameters.set("corpusParameters", corpusParameters);
    }

    // nonStemmedPostings must be a boolean [optional parameter]
    // defaults to true
    if (globalParameters.containsKey("nonStemmedPostings")) {
      try {
        boolean nsp = globalParameters.getBoolean("nonStemmedPostings");
      } catch (Exception e) {
        errorLog.add("Parameter 'nonStemmedPostings' should be a boolean. Defaults to true.");
      }
    } else {
      globalParameters.set("nonStemmedPostings", true);
    }


    // stemmedPostings may be a boolean [optional parameter]
    // defaults to true
    if (globalParameters.containsKey("stemmedPostings")) {
      try {
        boolean nsp = globalParameters.getBoolean("stemmedPostings");
      } catch (Exception e) {
        errorLog.add("Parameter 'stemmedPostings' should be a boolean. Defaults to true.");
      }
    } else {
      globalParameters.set("stemmedPostings", true);
    }


    // stemmer must be a list of stemmers [optional parameter]
    //   possible values { null | porter | krovetz | <class> }
    if (globalParameters.containsKey("stemmer")) {
      // check that it's a list of strings:
      try {
        List<String> stemmers = globalParameters.getList("stemmer");
      } catch (Exception e) {
        errorLog.add("Parameter 'stemmer' should be a list of strings indicating stemmer names. \n"
                + "This parameter is only active when the 'stemmedPostings' parameter is true. \n"
                + "Stemmer names should be associated with classes in 'stemmerClass' parameter.\n"
                + "Entries may be automatically generated from the 'stemmerClass' parameter. \n"
                + "Default to : \n"
                + "{\"stemmer\": [\"porter\"],\n"
                + " \"stemmerClass\" : {\"porter\" : \"org.lemurproject.galago.core.parse.stem.Porter2Stemmer\"}}\n");

        // now ensure that this error is not propegated.
        globalParameters.remove("stemmer");
        globalParameters.set("stemmer", new ArrayList());
      }
    } else {
      if (globalParameters.getBoolean("stemmedPostings")) {
        ArrayList<String> stemmers = new ArrayList();
        // try to find the stemmerClass parameters
        if (globalParameters.containsKey("stemmerClass")
                && globalParameters.isMap("stemmerClass")) {
          Parameters stemmerClass = globalParameters.getMap("stemmerClass");
          for (String key : stemmerClass.getKeys()) {
            stemmers.add(key);
          }
        } else {
          // defaults to use porter stemmer
          stemmers.add("porter");
        }
        globalParameters.set("stemmer", stemmers);
      }
    }


    // stemmerClass must be a map from stemmer name to stemmer class path
    // [optional parameter]
    // must match the full list of stemmers in parameter 'stemmer'
    try {
      if (!globalParameters.containsKey("stemmerClass")) {
        globalParameters.set("stemmerClass", new Parameters());
      }
      Parameters stemmerClasses = globalParameters.getMap("stemmerClass");
      if (globalParameters.getBoolean("stemmedPostings")
              && globalParameters.isList("stemmer")) {
        // this is safe - thanks to previous checks
        List<String> stemmer = (List<String>) globalParameters.getList("stemmer");
        for (String stemmerKey : stemmer) {
          if (stemmerClasses.containsKey(stemmerKey)) {
            // check that the class can be assigned
            Class c = Class.forName(stemmerClasses.getString(stemmerKey));
          } else {
            // we are missing a class
            if (stemmerKey.equals("null")) {
              stemmerClasses.set(stemmerKey, NullStemmer.class.getName());
            } else if (stemmerKey.equals("porter")) {
              stemmerClasses.set(stemmerKey, Porter2Stemmer.class.getName());
            } else if (stemmerKey.equals("krovetz")) {
              stemmerClasses.set(stemmerKey, KrovetzStemmer.class.getName());
            } else {
              errorLog.add("Unknown stemmer name: " + stemmerKey + " -- please provide a class in stemmerClass parameter.");
            }
          }
        }
      }
    } catch (Exception e) {
      errorLog.add("Parameter 'stemmerClass' should be a mapping of strings to strings.\n"
              + "This parameter is only active when the 'stemmedPostings' parameter is true.\n"
              + "This parameter should contain one entry for each element in the list 'stemmer'.\n"
              + "Some entries may be automatically generated from the 'stemmer' parameter.\n"
              + "e.g: {\"stemmer\": [\"porter\"],\n"
              + "      \"stemmerClass\" : {\"porter\" : \"org.lemurproject.galago.core.parse.stem.Porter2Stemmer\"}}\n");
    }


    // tokenizer/fields must be a list of strings [optional parameter]
    // defaults
    if (globalParameters.containsKey("tokenizer")) {
      try {
        Parameters t = globalParameters.getMap("tokenizer");
      } catch (Exception e) {
        errorLog.add("Parameter 'tokenizer' must be a map.\n");
        globalParameters.remove("tokenizer");
        globalParameters.set("tokenizer", new Parameters());
      }
    } else {
      globalParameters.set("tokenizer", new Parameters());
    }

    HashSet<String> fieldNames = new HashSet();
    Parameters tokenizerParams = globalParameters.getMap("tokenizer");
    if (tokenizerParams.containsKey("fields")) {
      try {
        List<String> fields = tokenizerParams.getAsList("fields");
        fieldNames.addAll(fields);
      } catch (Exception e) {
        errorLog.add("Parameter 'tokenizer/fields' should be a list of strings.\n"
                + "default is an empty list");
        // for safety ensure that fields exists
        tokenizerParams.remove("fields");
        tokenizerParams.set("fields", new ArrayList<String>());
      }
    } else {
      // default : set an empty string list
      tokenizerParams.set("fields", new ArrayList<String>());
    }

    // tokenizer/format must be a mapping from fields to types [optional parameter]
    //  each type needs to be indexable {string,int,long,float,double,date}
    if (tokenizerParams.containsKey("formats")) {
      try {
        HashSet<String> possibleFormats = new HashSet();
        possibleFormats.add("string");
        possibleFormats.add("int");
        possibleFormats.add("long");
        possibleFormats.add("float");
        possibleFormats.add("double");
        possibleFormats.add("date");
        Parameters formats = tokenizerParams.getMap("formats");
        for (String field : formats.getKeys()) {
          if (!fieldNames.contains(field)) {
            errorLog.add("Found a format for an unknown field: " + field);
          } else if (!possibleFormats.contains(formats.getString(field))) {
            errorLog.add("Unknown format: " + formats.getString(field) + " for field :" + field);
          }
        }
      } catch (Exception e) {
        errorLog.add("Parameter 'tokenizer/formats' should be a map of fieldnames to field formats.\n"
                + "default is to omit parameter.");
      }
    } else {
      tokenizerParams.set("formats", new Parameters());
    }

    // fieldIndex must be a boolean [optional]
    // defaults to true
    if (globalParameters.containsKey("fieldIndex")) {
      try {
        boolean fi = globalParameters.getBoolean("fieldIndex");
      } catch (Exception e) {
        errorLog.add("Parameter 'fieldIndex' must be a boolean value. Default reflects the presence of the parameter 'tokenizer/field'");
      }
    } else {
      if (globalParameters.getMap("tokenizer").getList("fields").isEmpty()) {
        globalParameters.set("fieldIndex", false);
      } else {
        globalParameters.set("fieldIndex", true);
      }
    }

    // fieldIndexParameters must be a map [optional]
    //  required when fieldIndex is true
    // defaults to produce both non-stemmed and stemmed postings using the above set of stemmers
    //
    // fieldIndexParameters/nonStemmedPostings must be a boolean
    // fieldIndexParameters/stemmedPostings must be a boolean
    // fieldIndexParameters/stemmer must be a list of stemmers (that must occur in stemmer list above)
    if (!globalParameters.containsKey("fieldIndexParameters")) {
      globalParameters.set("fieldIndexParameters", new Parameters());
    }
    try {
      Parameters fieldIndexParameters = globalParameters.getMap("fieldIndexParameters");
      // check the map
      if (!fieldIndexParameters.containsKey("nonStemmedPostings")) {
        fieldIndexParameters.set("nonStemmedPostings", globalParameters.getBoolean("nonStemmedPostings"));
      }
      if (!fieldIndexParameters.containsKey("stemmedPostings")) {
        fieldIndexParameters.set("stemmedPostings", globalParameters.getBoolean("stemmedPostings"));
      }

      // assert that either stemmedpostings or nonstemmedpostings will be created.
      if (!fieldIndexParameters.getBoolean("nonStemmedPostings")
              && !fieldIndexParameters.getBoolean("stemmedPostings")) {
        errorLog.add("FieldIndexParameters: either nonstemmedpostings or stemmedpostings must be created.");
      }

      // if nonstemmedpostings is required - make sure we have a full nonstemmedpostings index
      if (!globalParameters.getBoolean("nonStemmedPostings")
              && fieldIndexParameters.getBoolean("nonStemmedPostings")) {
        errorLog.add("FieldIndexParameters: can not make non stemmed field postings without a global non stemmed postings option");
      }

      // if stemmedpostings is required - make sure we have a full stemmedpostings index
      if (!globalParameters.getBoolean("stemmedPostings")
              && fieldIndexParameters.getBoolean("stemmedPostings")) {
        errorLog.add("FieldIndexParameters: can not make stemmed field postings without a global stemmed postings option");
      }

      // if we are stemming - check the stemmers match the global stemmers
      if (fieldIndexParameters.containsKey("stemmer")) {
        HashSet<String> stemmerList = new HashSet(globalParameters.getList("stemmer"));
        for (String stemmer : (List<String>) fieldIndexParameters.getList("stemmer")) {
          if (!stemmerList.contains(stemmer)) {
            errorLog.add("FieldIndexParameters: stemmers must be in the global 'stemmer' list.");
          }
        }
      } else {
        // by default use the global stemmer list (empty if not used)
        fieldIndexParameters.set("stemmer", globalParameters.getList("stemmer"));
      }
    } catch (Exception e) {
      errorLog.add("Parameter 'fieldIndexParameters' should be a map.\n"
              + "Should include boolean values for 'nonStemmedPostings', and 'stemmedPostings'\n"
              + "Should also include a list of stemmer names. Each stemmer should be in the global parameter stemmer.\n"
              + "Options selected here must conform with global options.\n");
    }


    // links must be a boolean [optional parameter]
    // defaults to false
    if (globalParameters.containsKey("links")) {
      try {
        globalParameters.getBoolean("links");
      } catch (Exception e) {
        errorLog.add("Parameter 'links' must be a boolean value. Defaults to false.\n");
      }
    } else {
      globalParameters.set("links", false);
    }

    // if links is true - check that the tag 'a' is extracted
    if (globalParameters.getBoolean("links")) {
      if (!fieldNames.contains("a")) {
        // ensure the tag 'a' is extracted
        globalParameters.getMap("tokenizer").getList("fields").add("a");
      }
    }


    if (!globalParameters.getBoolean("nonStemmedPostings")
            && !globalParameters.getBoolean("stemmedPostings")) {
      errorLog.add("Either parameter 'nonStemmedPostings' or 'stemmedPostings' must be true.");
    }

    if (errorLog.isEmpty()) {
      return globalParameters;
    } else {
      for (String err : errorLog) {
        System.err.println(err);
      }
      return null;
    }
  }

  public Job getIndexJob(Parameters buildParameters) throws Exception {

    buildParameters = checkBuildIndexParameters(buildParameters);
    if (buildParameters == null) {
      return null;
    }

    Job job = new Job();

    String indexPath = new File(buildParameters.getString("indexPath")).getAbsolutePath(); // fail if no path.
    // ensure the index folder exists
    File buildManifest = new File(indexPath, "buildManifest.json");
    Utility.makeParentDirectories(buildManifest);
    Utility.copyStringToFile(buildParameters.toPrettyString(), buildManifest);

    List<String> inputPaths = buildParameters.getAsList("inputPath");

    // common steps + connections

    Parameters splitParameters = buildParameters.get("parser", new Parameters()).clone();
    splitParameters.set("corpusPieces", buildParameters.get("distrib", 10));
    job.add(BuildStageTemplates.getSplitStage(inputPaths, DocumentSource.class, new DocumentSplit.FileIdOrder(), splitParameters));

    job.add(getParsePostingsStage(buildParameters));
    job.add(BuildStageTemplates.getDocumentCounter("countDocuments", "numberedDocumentDataNumbers", "docCounts"));
    job.add(BuildStageTemplates.getWriteNamesStage("writeNames", new File(indexPath, "names"), "numberedDocumentDataNumbers"));
    job.add(BuildStageTemplates.getWriteNamesRevStage("writeNamesRev", new File(indexPath, "names.reverse"), "numberedDocumentDataNames"));
    job.add(BuildStageTemplates.getWriteLengthsStage("writeLengths", new File(indexPath, "lengths"), "fieldLengthData"));

    job.connect("inputSplit", "parsePostings", ConnectionAssignmentType.Each);
    job.connect("parsePostings", "countDocuments", ConnectionAssignmentType.Each);
    job.connect("parsePostings", "writeLengths", ConnectionAssignmentType.Combined);
    job.connect("parsePostings", "writeNames", ConnectionAssignmentType.Combined);
    job.connect("parsePostings", "writeNamesRev", ConnectionAssignmentType.Combined);

    // if extracting links
    if (buildParameters.getBoolean("links")) {
      job.add(getParseLinksStage(buildParameters));
      job.add(getLinkCombineStage());

      job.connect("inputSplit", "parseLinks", ConnectionAssignmentType.Each);
      job.connect("parseLinks", "linkCombine", ConnectionAssignmentType.Combined);
      job.connect("linkCombine", "parsePostings", ConnectionAssignmentType.Combined);
    }

    // corpus key data
    if (buildParameters.getBoolean("corpus")) {
      job.add(getParallelIndexKeyWriterStage("writeCorpusKeys", "corpusKeys", buildParameters.getMap("corpusParameters")));
      job.connect("parsePostings", "writeCorpusKeys", ConnectionAssignmentType.Combined);
    }

    // nonstemmedpostings
    if (buildParameters.getBoolean("nonStemmedPostings")) {
      job.add(getWritePostingsStage(buildParameters, "writePostings", "numberedPostings",
              new NumberWordPosition.WordDocumentPositionOrder(), "postings",
              PositionIndexWriter.class, null));

      job.connect("countDocuments", "writePostings", ConnectionAssignmentType.Combined);
      job.connect("parsePostings", "writePostings", ConnectionAssignmentType.Combined);
    }

    // stemmedpostings
    if (buildParameters.getBoolean("stemmedPostings")) {
      for (String stemmer : (List<String>) buildParameters.getList("stemmer")) {
        job.add(getWritePostingsStage(buildParameters, "writePostings-" + stemmer,
                "numberedStemmedPostings-" + stemmer,
                new NumberWordPosition.WordDocumentPositionOrder(),
                "postings." + stemmer, PositionIndexWriter.class, stemmer));
        job.connect("parsePostings", "writePostings-" + stemmer, ConnectionAssignmentType.Combined);
        job.connect("countDocuments", "writePostings-" + stemmer, ConnectionAssignmentType.Combined);
      }
    }

    // if we have at least one field - write extents
    if (!buildParameters.getMap("tokenizer").getList("fields").isEmpty()) {
      job.add(BuildStageTemplates.getWriteExtentsStage("writeExtents", new File(indexPath, "extents"), "numberedExtents"));

      job.connect("parsePostings", "writeExtents", ConnectionAssignmentType.Combined);
    }

    // if we have at least one field format - write fields
    if (!buildParameters.getMap("tokenizer").getMap("formats").isEmpty()) {
      Parameters p = new Parameters();
      p.set("tokenizer", buildParameters.getMap("tokenizer"));
      job.add(BuildStageTemplates.getWriteFieldsStage("writeFields", new File(indexPath, "fields"), "numberedFields", p));

      job.connect("parsePostings", "writeFields", ConnectionAssignmentType.Combined);
    }

    // field indexes - one for each stemmer
    if (buildParameters.getBoolean("fieldIndex")) {
      if (buildParameters.getMap("fieldIndexParameters").getBoolean("nonStemmedPostings")) {
        // create one writer for each stemmer + one for nonstemmed
        job.add(getWritePostingsStage(buildParameters, "writeExtentPostings", "numberedExtentPostings",
                new FieldNumberWordPosition.FieldWordDocumentPositionOrder(), "field.", PositionFieldIndexWriter.class, null));

        job.connect("parsePostings", "writeExtentPostings", ConnectionAssignmentType.Combined);
        job.connect("countDocuments", "writeExtentPostings", ConnectionAssignmentType.Combined);
      }
      if (buildParameters.getMap("fieldIndexParameters").getBoolean("stemmedPostings")) {
        for (String stemmer : (List<String>) buildParameters.getMap("fieldIndexParameters").getList("stemmer")) {
          job.add(getWritePostingsStage(buildParameters, "writeExtentPostings-" + stemmer, "numberedExtentPostings-" + stemmer,
                  new FieldNumberWordPosition.FieldWordDocumentPositionOrder(), "field." + stemmer + ".", PositionFieldIndexWriter.class, stemmer));

          job.connect("parsePostings", "writeExtentPostings-" + stemmer, ConnectionAssignmentType.Combined);
          job.connect("countDocuments", "writeExtentPostings-" + stemmer, ConnectionAssignmentType.Combined);
        }
      }
    }

    return job;
  }

  @Override
  public String getHelpString() {
    return "galago build [flags] --indexPath=<index> (--inputPath+<input>)+\n\n"
            + "  Builds a Galago StructuredIndex with TupleFlow, using one thread\n"
            + "  for each CPU core on your computer.  While some debugging output\n"
            + "  will be displayed on the screen, most of the status information will\n"
            + "  appear on a web page.  A URL should appear in the command output\n"
            + "  that will direct you to the status page.\n\n"
            + "<input>:  Can be either a file or directory, and as many can be\n"
            + "          specified as you like.  Galago can read html, xml, txt, \n"
            + "          arc (Heritrix), warc, trectext, trecweb and corpus files.\n"
            + "          Files may be gzip compressed (.gz|.bz).\n"
            + "<index>:  The directory path of the index to produce.\n\n"
            + "Algorithm Flags:\n"
            + "  --links={true|false}:    Selects whether to collect anchor text\n"
            + "                           [default=false]\n"
            + "  --nonStemmedPosting={true|false}: Selects whether to build non-stemmed inverted indexes\n"
            + "                           [default=[true]]\n"
            + "  --stemmedPosting={true|false}: Selects whether to build stemmed inverted indexes\n"
            + "                           [default=[true]]\n"
            + "  --stemmer+porter|krovetz: Selects which stemmers to use.\n"
            + "                           [default=[porter]]\n"
            + "  --corpus={true|false}:   Selects to output a corpus folder.\n"
            + "                           [default=true]\n\n"
            + "  --tokenizer/fields+{field-name}:   \n"
            + "                           Selects field parts to index.\n"
            + "                           [omitted]\n\n"
            + App.getTupleFlowParameterString();
    //TODO: need to design parameters for field indexes + stemming for field indexes
  }

  @Override
  public void run(Parameters p, PrintStream output) throws Exception {
    // build-fast index input
    if (!p.isString("indexPath") && !p.isList("inputPath")) {
      output.println(getHelpString());
      return;
    }

    Job job;
    BuildIndex build = new BuildIndex();
    job = build.getIndexJob(p);

    if (job != null) {
      App.runTupleFlowJob(job, p, output);
    }

    output.println("Done Indexing.");
    
    // sanity check - get the number of documents out of ./names
    DiskNameReader names = new DiskNameReader(p.getString("indexPath") + File.separator + "names");
    Parameters namesParams = names.getManifest();
    output.println("Documents Indexed: " + namesParams.getLong("keyCount") + ".");
  }
}
