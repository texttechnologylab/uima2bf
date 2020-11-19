package writer;

import static org.apache.uima.fit.util.JCasUtil.select;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.hucompute.textimager.uima.type.category.CategoryCoveredTagged;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.MetaDataStringField;

public class Zeit2BF extends JCasAnnotator_ImplBase {
	// File to save output
	public static final String PARAM_EXPORT_FILE = "exportFile";
	@ConfigurationParameter(name = PARAM_EXPORT_FILE)
	protected String exportFile;


	int docCount = 0;
	BufferedWriter newsWriter;
	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);
		try {
			newsWriter = new BufferedWriter(new FileWriter(exportFile));
			newsWriter.write("directed");
			newsWriter.newLine();
			newsWriter.write("SimilarityGraph");
			newsWriter.newLine();
			newsWriter.write("Vertex Attributes:");
			newsWriter.write("[Year¤Integer];");
			newsWriter.write("[Month¤Integer];");
			newsWriter.write("[Day¤Integer];");
			newsWriter.write("[Time¤String];");
			newsWriter.write("[Volume¤String]");
			newsWriter.write("[t2wTopicClassification¤IntegerDistribution];");
			newsWriter.write("[t2wRaumClassification¤IntegerDistribution];");
			newsWriter.write("[t2wZeitClassification¤IntegerDistribution];");
			newsWriter.write("[ddc2Classification¤IntegerDistribution];");
			newsWriter.newLine();
			newsWriter.write("Edge Attributes:");
			newsWriter.newLine();
			newsWriter.write("ProbabilityMassOfGraph: 0");
			newsWriter.newLine();
			newsWriter.write("Vertices:");
			newsWriter.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
		try {
			newsWriter.flush();
			newsWriter.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		String tmpLine = "";

		String articleId = DocumentMetaData.get(jCas).getDocumentBaseUri().replace("file:", "").replace("raw", "xmiNeu") + "" + DocumentMetaData.get(jCas).getDocumentId().replace("tei","tei.xmi");
		String articleDate = "void";
		Integer year = Integer.parseInt(DocumentMetaData.get(jCas).getDocumentId().split("/")[0]);

		for(MetaDataStringField metaString : select(jCas, MetaDataStringField.class)) {
			if(metaString.getKey().equals("volume")){
				if(metaString.getValue().split("/").length==3) {
					String day = metaString.getValue().split("/")[2];
					String month = metaString.getValue().split("/")[1];
					String tmpYear = metaString.getValue().split("/")[0];
					if(day.length()==1)
						day = "0"+day;
					if(month.length()==1)
						month = "0"+month;
					if(tmpYear.length()==1)
						tmpYear = "0"+tmpYear;
					if(tmpYear.length()>3) {
						articleDate = day + "/" + month + "/" + tmpYear;					
					}
					else {
						articleDate = tmpYear + "/" + month + "/" + day;										
					}
				}
			}
		}
		
		tmpLine += articleId + "¤";
		tmpLine += "[Year¤"+ year + "¤]¤";
		tmpLine += "[Month¤-1¤]¤";
		tmpLine += "[Day¤-1¤]¤";
		tmpLine += "[Time¤void¤]¤";
		tmpLine += "[Volume¤" + articleDate + "¤]¤";

		String t2wTopicString = "";
		String t2wRaumString = "";
		String t2wZeitString = "";
		String ddc2String = "";

		for (CategoryCoveredTagged tmpCatCov : select(jCas, CategoryCoveredTagged.class)) {
			try {
				if(tmpCatCov.getTags().equals("text2cwc_thema")){
					if(tmpCatCov.getScore()>0.001) {
						t2wTopicString += tmpCatCov.getValue().replace("__dewiki__", "") + "¶" + tmpCatCov.getScore() + "¤";
					}
				}
				if(tmpCatCov.getTags().equals("text2cwc_raum")){
					if(tmpCatCov.getScore()>0.001) {
						t2wRaumString += tmpCatCov.getValue().replace("__dewiki__", "") + "¶" + tmpCatCov.getScore() + "¤";
					}
				}
				if(tmpCatCov.getTags().equals("text2cwc_zeit")){
					if(tmpCatCov.getScore()>0.001) {
						t2wZeitString += tmpCatCov.getValue().replace("__dewiki__", "") + "¶" + tmpCatCov.getScore() + "¤";
					}
				}
				if(tmpCatCov.getTags().equals("ddc2")){
					if(tmpCatCov.getScore()>0.001) {
						ddc2String += tmpCatCov.getValue().replace("__label_ddc__", "") + "¶" + tmpCatCov.getScore() + "¤";
					}
				}
			}
			catch(Exception e) {
			}
		}

		if(t2wTopicString.equals(""))
			t2wTopicString = "8132441¶0¤";
		if(t2wRaumString.equals(""))
			t2wRaumString = "1335049¶0¤";
		if(t2wZeitString.equals(""))
			t2wZeitString = "288291¶0¤";
		if(ddc2String.equals(""))
			ddc2String = "000¶0¤";

		tmpLine +=  "[t2wTopicClassification¤" + t2wTopicString + "]¤";
		tmpLine +=  "[t2wRaumClassification¤" + t2wRaumString + "]¤";
		tmpLine +=  "[t2wZeitClassification¤" + t2wZeitString + "]¤";
		tmpLine +=  "[ddc2Classification¤" + ddc2String + "]¤";
		try {
			newsWriter.write(tmpLine);
			newsWriter.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
