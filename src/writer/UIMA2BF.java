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

public class UIMA2BF extends JCasAnnotator_ImplBase {
	// File to save output
	public static final String PARAM_EXPORT_FILE = "exportFile";
	@ConfigurationParameter(name = PARAM_EXPORT_FILE)
	protected String exportFile;

	// Threshhold for categorys
	public static final String CATEGORY_THRESHOLD = "categoryThreshold";
	@ConfigurationParameter(name = CATEGORY_THRESHOLD, defaultValue = "0.001")
	protected String categoryThreshold;

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
		
		// ArticleId gibt den Pfad zu der XMI Datei an. Meist aus der DoucmentBaseUri und der DocumentId herauszulesen.
		String articleId = DocumentMetaData.get(jCas).getDocumentBaseUri() +"" + DocumentMetaData.get(jCas).getDocumentId();
		// Volume gibt die Ausgabe einer Zeitung an. Kann in MetaDaten drinstehen oder auch über die Uri. Wenn nicht vorhanden wird void gewählt.
		String volume = "void";

		for(MetaDataStringField metaString : select(jCas, MetaDataStringField.class)) {
			if(metaString.getKey().equals("volume")){
				volume = metaString.getValue();
			}
		}
		
		// year gibt den Jahrgang der Erstellung des Textes an. Meist aus Metadaten, Id oder Uri auszulesen. Wenn nicht vorhanden, wird -1 gewählt.
		Integer year = Integer.parseInt(DocumentMetaData.get(jCas).getDocumentId().split("/")[0]);
		// month gibt den Monat der Erstellung des Textes an. Meist aus Metadaten, Id oder Uri auszulesen. Wenn nicht vorhanden, wird -1 gewählt.
		Integer month = -1;
		// day gibt den Tag der Erstellung des Textes an. Meist aus Metadaten, Id oder Uri auszulesen. Wenn nicht vorhanden, wird -1 gewählt.
		Integer day = -1;
		// time gibt die Uhrzeit der Erstellung des Textes an. Meist aus Metadaten, Id oder Uri auszulesen. Wenn nicht vorhanden, wird -1 gewählt.
		Integer time = -1;
		

		String t2wTopicString = "";
		String t2wRaumString = "";
		String t2wZeitString = "";
		String ddc2String = "";

		double threshold = Double.parseDouble(categoryThreshold);
		// Hier werden die einzelnen Kategorie Attribute gefüllt. 
		for (CategoryCoveredTagged tmpCatCov : select(jCas, CategoryCoveredTagged.class)) {
			try {
				if(tmpCatCov.getTags().equals("text2cwc_thema")){
					// Alles über dem Schwellenwert von 0.001 wird genommen.
					if(tmpCatCov.getScore()>threshold) {
						t2wTopicString += tmpCatCov.getValue().replace("__dewiki__", "") + "¶" + tmpCatCov.getScore() + "¤";
					}
				}
				if(tmpCatCov.getTags().equals("text2cwc_raum")){
					if(tmpCatCov.getScore()>threshold) {
						t2wRaumString += tmpCatCov.getValue().replace("__dewiki__", "") + "¶" + tmpCatCov.getScore() + "¤";
					}
				}
				if(tmpCatCov.getTags().equals("text2cwc_zeit")){
					if(tmpCatCov.getScore()>threshold) {
						t2wZeitString += tmpCatCov.getValue().replace("__dewiki__", "") + "¶" + tmpCatCov.getScore() + "¤";
					}
				}
				if(tmpCatCov.getTags().equals("ddc2")){
					if(tmpCatCov.getScore()>threshold) {
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
		
		tmpLine += articleId + "¤";
		tmpLine += "[Year¤"+ year + "¤]¤";
		tmpLine += "[Month¤"+month+"¤]¤";
		tmpLine += "[Day¤"+day+"¤]¤";
		tmpLine += "[Time¤"+time+"¤]¤";
		tmpLine += "[Volume¤" + volume + "¤]¤";
		tmpLine += "[t2wTopicClassification¤" + t2wTopicString + "]¤";
		tmpLine += "[t2wRaumClassification¤" + t2wRaumString + "]¤";
		tmpLine += "[t2wZeitClassification¤" + t2wZeitString + "]¤";
		tmpLine += "[ddc2Classification¤" + ddc2String + "]¤";
		
		try {
			newsWriter.write(tmpLine);
			newsWriter.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
