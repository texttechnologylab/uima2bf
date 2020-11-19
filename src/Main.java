import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.io.xmi.XmiReader;

import writer.Guardian2BF;
import writer.SZ2BF;
import writer.Zeit2BF;

public class Main {

	public static void main(String[] args) throws UIMAException {
		
		CollectionReader reader = CollectionReaderFactory.createReader(
				XmiReader.class 
				,XmiReader.PARAM_SOURCE_LOCATION,"/resources/corpora/Zeit/xmiNeu",		// Zeit Corpus
//				,XmiReader.PARAM_SOURCE_LOCATION,"/resources/corpora/SueddeutscheZeitung/1992-2014/TEIBasic_xmi",
//				,XmiReader.PARAM_SOURCE_LOCATION,"/resources/corpora/Guardian/xmi",
				XmiReader.PARAM_PATTERNS,"**/*.xmi*",
				XmiReader.PARAM_LANGUAGE,"de"
				);
				
		AggregateBuilder builder = new AggregateBuilder();
		
		builder.add(org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription(Zeit2BF.class,
                Zeit2BF.PARAM_EXPORT_FILE, "output/Zeit.bf")
		); 
		
//		builder.add(org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription(SZ2BF.class,
//				SZ2BF.PARAM_EXPORT_FILE, "output/SZ.bf")
//		); 
		
//		builder.add(org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription(Guardian2BF.class,
//				SZ2BF.PARAM_EXPORT_FILE, "output/Guardian.bf")
//		); 		
		
		try {
			SimplePipeline.runPipeline(reader,builder.createAggregate());
		} catch (ResourceInitializationException e) {
			e.printStackTrace();
		} catch (UIMAException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 


	}

}
