package net.mpolonioli.rdftojanusgraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class GetProperties {
	
	private static final String s = File.separator;
	
	public HashMap<String, String> getPropValues() throws IOException {
		
		InputStream inputStream = null;
		HashMap<String, String> result = new HashMap<>();

		try {
			Properties prop = new Properties();
			
			File propFile = new File("conf" + s + "config.properties");
			
			inputStream = new FileInputStream(propFile);
			prop.load(inputStream);

			// get the property value and print it out
			String ontologyXML = prop.getProperty("ontologyXML");
			String datasetRDF = prop.getProperty("datasetRDF");
			String outputDir = prop.getProperty("outputDir");
			String baseUri = prop.getProperty("baseUri");
			String rdfNotation = prop.getProperty("rdfNotation");
			String janusConf = prop.getProperty("janusConf");
			String importerBatchSize = prop.getProperty("importerBatchSize");
			String importerReportPeriod = prop.getProperty("importerReportPeriod");
			String importerThreadCount = prop.getProperty("importerThreadCount");
			
			System.out.println(
					"ontologyXML=" + ontologyXML + "\n" +
					"datasetRDF=" + datasetRDF + "\n" +
					"baseUri=" + baseUri + "\n" +
					"outputDir" + outputDir + "\n" +
					"rdfNotation=" + rdfNotation + "\n" +
					"janusConf=" + janusConf + "\n" +
					"importerBatchSize=" + importerBatchSize + "\n" +
					"importerReportPeriod=" + importerReportPeriod + "\n" +
					"importerThreadCount=" + importerThreadCount
					);

			// add the property value to the result
			result.put("ontologyXML", ontologyXML);
			result.put("datasetRDF", datasetRDF);
			result.put("outputDir", outputDir);
			result.put("baseUri", baseUri);
			result.put("rdfNotation", rdfNotation);
			result.put("janusConf", janusConf);
			result.put("importerBatchSize", importerBatchSize);
			result.put("importerReportPeriod", importerReportPeriod);
			result.put("importerThreadCount", importerThreadCount);
		}catch(Exception e) 
		{
			e.printStackTrace();
		}finally
		{
			inputStream.close();
		}

		return result;
	}

}
