// BSD License (http://lemurproject.org/galago-license)
package org.lemurproject.galago.core.tools;

import java.io.*;
import java.util.List;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.tupleflow.Order;
import org.lemurproject.galago.core.tools.App.AppFunction;
import org.lemurproject.galago.tupleflow.execution.ConnectionAssignmentType;
import org.lemurproject.galago.tupleflow.execution.InputStep;
import org.lemurproject.galago.tupleflow.execution.Job;
import org.lemurproject.galago.tupleflow.execution.MultiStep;
import org.lemurproject.galago.tupleflow.execution.OutputStep;
import org.lemurproject.galago.tupleflow.execution.Stage;
import org.lemurproject.galago.tupleflow.execution.Step;
import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.core.index.corpus.DocumentToKeyValuePair;
import org.lemurproject.galago.core.index.corpus.DocumentAggregator;
import org.lemurproject.galago.core.types.KeyValuePair;
import org.lemurproject.galago.core.parse.*;

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
	stage.add(new Step(DocumentToKeyValuePair.class, p));
	stage.add(Utility.getSorter(new KeyValuePair.KeyOrder()));
	stage.add(new OutputStep(outStream));
	return stage;
    }

    public Stage getWriteStage(String name,
			       String inStream,
			       Parameters p) throws IOException {
	Stage stage = new Stage(name);
	stage.addInput(inStream, new KeyValuePair.KeyOrder());
	stage.add(new InputStep(inStream));
	Parameters copy = p.clone();
	copy.set("filename", new File(p.getString("filename"), "corpus").getCanonicalPath());
	stage.add(new Step(DocumentAggregator.class, copy));
	return stage;
    }

    public Job getJob(Parameters jobParameters) throws Exception {
	Job job = new Job();	
	List<String> inputPaths = jobParameters.getAsList("inputPath");
	Parameters splitParameters = jobParameters.get("parser", new Parameters()).clone();
	job.add(BuildStageTemplates.getSplitStage(inputPaths,
						  DocumentSource.class,
						  new DocumentSplit.FileIdOrder(),
						  splitParameters));
	job.add(getParseStage("parse",
			      "splits",
			      "documents",
			      jobParameters));
	Parameters writeParameters = new Parameters();
	writeParameters.set("filename", jobParameters.getString("corpusPath"));
	job.add(getWriteStage("write", "documents", writeParameters));

	job.connect("inputSplit", "parse", ConnectionAssignmentType.Each);
	job.connect("parse", "write", ConnectionAssignmentType.Combined);
	return job;
    }


  @Override
  public void run(Parameters p, PrintStream output) throws Exception {
      if (!p.isString("corpuspath") && !p.isList("inputPath")) {
	  output.println(getHelpString());
	  return;
      }
      
      Job job;
      BuildEntityCorpus dIndex = new BuildEntityCorpus();
      job = dIndex.getJob(p);
      
      if (job != null) {
	  App.runTupleFlowJob(job, p, output);
      }
  }

  @Override
  public String getHelpString() {
    return "galago build-word-dates [flags] --indexPath=<dir> (--inputPath+<input>)+\n\n"
            + " Creates an corpus of pseudo-docs based on diffuse entity occurrences.\n\n"
            + App.getTupleFlowParameterString();
  }
}