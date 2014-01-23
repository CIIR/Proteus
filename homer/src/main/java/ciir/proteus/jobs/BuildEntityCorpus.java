 // BSD License (http://lemurproject.org/galago-license)
package ciir.proteus.jobs;

import ciir.proteus.index.DocumentToNamedKeyValue;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import org.lemurproject.galago.core.index.corpus.CorpusFolderWriter;
import org.lemurproject.galago.core.index.corpus.CorpusReader;
import org.lemurproject.galago.core.index.corpus.DocumentToKeyValuePair;
import org.lemurproject.galago.core.index.corpus.SplitBTreeKeyWriter;
import org.lemurproject.galago.core.index.merge.CorpusMerger;
import org.lemurproject.galago.core.parse.DocumentSource;
import org.lemurproject.galago.core.tools.AppFunction;
import org.lemurproject.galago.core.tools.apps.BuildStageTemplates;
import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.core.types.KeyValuePair;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.tupleflow.execution.ConnectionAssignmentType;
import org.lemurproject.galago.tupleflow.execution.InputStep;
import org.lemurproject.galago.tupleflow.execution.Job;
import org.lemurproject.galago.tupleflow.execution.OutputStep;
import org.lemurproject.galago.tupleflow.execution.Stage;
import org.lemurproject.galago.tupleflow.execution.Step;

public class BuildEntityCorpus extends AppFunction {

  public Stage getParseStage(String name,
          String inStream,
          String outStream,
          Parameters p) {
    Stage stage = new Stage(name);
    stage.addInput(inStream, new DocumentSplit.FileIdOrder());
    stage.addOutput(outStream, new KeyValuePair.KeyOrder());
    stage.add(new InputStep(inStream));
    stage.add(BuildStageTemplates.getParserStep(p));
    stage.add(new Step(DocumentToNamedKeyValue.class, p));
    stage.add(Utility.getSorter(new KeyValuePair.KeyOrder()));
    stage.add(new OutputStep(outStream));
    return stage;
  }

  public Stage getWriteStage(String name,
          String inStream,
          String outStream,
          Parameters p) throws IOException {
    Stage stage = new Stage(name);
    stage.addInput(inStream, new KeyValuePair.KeyOrder());
    stage.addOutput(outStream, new KeyValuePair.KeyOrder());
    stage.add(new InputStep(inStream));
    
    Parameters cparms = makeCorpusParameters(p);
    
    stage.add(new Step(DocumentAggregator.class, p));
    stage.add(new Step(CorpusFolderWriter.class, cparms));
    stage.add(Utility.getSorter(new KeyValuePair.KeyOrder()));
    stage.add(new OutputStep(outStream));
    return stage;
  }
  
  public Stage getParallelIndexKeyWriterStage(String name, String input, Parameters jobParms) {
    Stage stage = new Stage(name);

    Parameters cparms = makeCorpusParameters(jobParms);
    
    stage.addInput(input, new KeyValuePair.KeyOrder());
    stage.add(new InputStep(input));
    stage.add(new Step(SplitBTreeKeyWriter.class, cparms));

    return stage;
  }
  
  public Parameters makeCorpusParameters(Parameters jobParameters) {
    Parameters cps = new Parameters();
    cps.set("readerClass", CorpusReader.class.getName());
    cps.set("writerClass", CorpusFolderWriter.class.getName());
    cps.set("mergerClass", CorpusMerger.class.getName());
    // we need a small block size because the stored values are small
    cps.set("blockSize", jobParameters.get("corpusBlockSize", 512));
    cps.set("filename", jobParameters.getString("indexPath") + File.separator + "corpus");
    return cps;
  }
  
  public Job getJob(Parameters jobParameters) throws Exception {
    Job job = new Job();
    List<String> inputPaths = jobParameters.getAsList("inputPath");
    Parameters splitParameters = jobParameters.isMap("parser") ? jobParameters.getMap("parser") : new Parameters();
    job.add(BuildStageTemplates.getSplitStage(inputPaths,
            DocumentSource.class,
            new DocumentSplit.FileIdOrder(),
            splitParameters));
    job.add(getParseStage("parse",
            "splits",
            "documents",
            jobParameters));
    
    job.add(getWriteStage("write", "documents", "corpusKeys", jobParameters.clone()));
    job.add(getParallelIndexKeyWriterStage("finishCorpus", "corpusKeys", jobParameters.clone()));
        
    job.connect("inputSplit", "parse", ConnectionAssignmentType.Each);
    job.connect("parse", "write", ConnectionAssignmentType.Combined);
    job.connect("write", "finishCorpus", ConnectionAssignmentType.Combined);
    return job;
  }

  @Override
  public void run(Parameters p, PrintStream output) throws Exception {
    if (!p.isString("indexPath") && !p.isList("inputPath")) {
      output.println(getHelpString());
      return;
    }

    Job job;
    BuildEntityCorpus dIndex = new BuildEntityCorpus();
    job = dIndex.getJob(p);

    if (job != null) {
      runTupleFlowJob(job, p, output);
    }
  }

  @Override
  public String getName() {
    return "build-entity-corpus";
  }

  @Override
  public String getHelpString() {
    return "galago build-entity-corpus [parameters]\n\n"
            + getTupleFlowParameterString();
  }
}