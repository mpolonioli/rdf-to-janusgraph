package net.mpolonioli.rdftojanusgraph;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.xml.sax.SAXException;

import net.mpolonioli.janusgraphimporter.core.JanusGraphImporter;
import net.mpolonioli.rdfontologyparser.core.OntologyParser;
import net.mpolonioli.rdfontologyparser.core.SchemaStructures;
import net.mpolonioli.rdftocsv.core.RdfToCsv;

public class RdfToJanusGraph 
{
		
		private static final String s = File.separator;
		
		private static long startOntologyParsing;
		private static long endOntologyParsing;
		private static long startCreatingCsv;
		private static long endCreatingCsv;
		private static long startLoadingData;
		private static long endLoadingData;

		@SuppressWarnings("rawtypes")
		public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, ParserException {
						
			/*
			 * 	Get the required parameters from configuration file
			 */
			GetProperties propertiesGetter = new GetProperties();
			HashMap<String, String> propHashMap = null;
			propHashMap = propertiesGetter.getPropValues();
			
			File ontologyXML = new File(propHashMap.get("ontologyXML"));
			File datasetRDF = new File(propHashMap.get("datasetRDF"));
			File outputDir = new File(propHashMap.get("outputDir"));
			String baseUri = propHashMap.get("baseUri");
			RDFNotation rdfNotation = RDFNotation.parse(propHashMap.get("rdfNotation"));
			String janusConf = propHashMap.get("janusConf");
			int importerBatchSize = Integer.parseInt(propHashMap.get("importerBatchSize"));
			int importerReportPeriod = Integer.parseInt(propHashMap.get("importerReportPeriod"));
			int importerThreadCount = Integer.parseInt(propHashMap.get("importerThreadCount"));
			
			
			/*
			 * parse the ontology and print the schema structures in a file
			 */
			startOntologyParsing = System.currentTimeMillis();
			System.out.println("Parsing ontology...");
			OntologyParser ontologyParser = new OntologyParser(ontologyXML);
			SchemaStructures schemaStructures = ontologyParser.parse();

			List<String> classNames = schemaStructures.getClassNames();
			List<String> vertexNames = schemaStructures.getVertexNames();
			List<String> propertyNames = schemaStructures.getPropertyNames();
			List<String> edgeNames = schemaStructures.getEdgeNames();
			
			HashMap<String, String> classHasUri = schemaStructures.getClassHasUri();
			HashMap<String, String> propertyHasUri = schemaStructures.getPropertyHasUri();
			HashMap<String, String> edgeHasUri = schemaStructures.getEdgeHasUri();

			HashMap<String, List<String>> vertexHasProperties = schemaStructures.getVertexHasProperties();
			HashMap<String, SchemaStructures.Cardinality> propertyHasCardinality = schemaStructures.getPropertyHasCardinality();
			HashMap<String, Class> propertyHasType = schemaStructures.getPropertyHasType();

			HashMap<String, List<String>> subClassOf = schemaStructures.getSubClassOf();
			HashMap<String, List<String>> superClassOf = schemaStructures.getSuperClassOf();

			File schemaStructuresFile = new File(outputDir.getAbsolutePath() + s + "schema-structures.txt");
			if(schemaStructuresFile.exists())
			{
				schemaStructuresFile.delete();
			}
			schemaStructuresFile.createNewFile();
			PrintWriter writer = new PrintWriter(new PrintWriter(schemaStructuresFile), true);
			writer.println("classNames:" + classNames.toString());
			writer.println("vertexNames:" + vertexNames.toString());
			writer.println("propertyNames:" + propertyNames.toString());
			writer.println("edgeNames:" + edgeNames.toString());
			writer.println("classHasUri:" + classHasUri.toString());
			writer.println("propertyHasUri:" + propertyHasUri.toString());
			writer.println("edgeHasUri:" + edgeHasUri.toString());
			writer.println("vertexHasProperties:" + vertexHasProperties.toString());
			writer.println("propertyHasCardinality:" + propertyHasCardinality.toString());
			writer.println("propertyHasType:" + propertyHasType.toString());
			writer.println("subClassOf:" + subClassOf.toString());
			writer.println("superClassOf:" + superClassOf.toString());
			writer.close();
			System.out.println("Finished");
			endOntologyParsing = System.currentTimeMillis();
	
			
			/*
			 * instance the RdfToCsv and create the CSV files
			 */
			startCreatingCsv = System.currentTimeMillis();
			File outputCsv = new File(outputDir.getAbsolutePath() + s + "data-csv");
			if(!outputCsv.exists())
			{
				outputCsv.mkdirs();
			}

			File outputDirVertexCsv = new File(outputCsv.getAbsolutePath() + s + "vertices");
			if(!outputDirVertexCsv.exists())
			{
				outputDirVertexCsv.mkdirs();
			}

			File outputDirEdgeCsv = new File(outputCsv.getAbsolutePath() + s + "edges");
			if(!outputDirEdgeCsv.exists())
			{
				outputDirEdgeCsv.mkdirs();
			}
			RdfToCsv rdfToCsv = new RdfToCsv();
			
			// this call will create the file "outputDir/dataset.hdt"
			rdfToCsv.createCsvFiles(
					datasetRDF, 
					baseUri, 
					rdfNotation, 
					outputDir, 
					outputDirVertexCsv, 
					outputDirEdgeCsv, 
					classNames, 
					vertexNames, 
					propertyNames, 
					classHasUri, 
					subClassOf, 
					propertyHasUri, 
					convertCardRdfToCsv(propertyHasCardinality), 
					edgeHasUri, 
					vertexHasProperties
					);
			
			List<File> vertexFiles = rdfToCsv.getVertexFiles();
			List<File> edgeFiles = rdfToCsv.getEdgeFiles();
			endCreatingCsv = System.currentTimeMillis();

			/*
			 * instance the JanusGraph importer and load the data from the CSV files
			 */
			List<String> vertexLabels = new ArrayList<>();
			vertexLabels.addAll(classNames);
			vertexLabels.addAll(vertexNames);
			List<String> propertiesWithIndex = new ArrayList<>(Arrays.asList("uri"));
			
			JanusGraphImporter janusImporter = new JanusGraphImporter(janusConf);

			System.out.println("Clearing the existing graph...");
			janusImporter.clearGraph();
			System.out.println("Finished");
			System.out.println("Defining schema...");
			janusImporter.defineSchema(vertexLabels, edgeNames, propertyNames, convertCardJanus(propertyHasCardinality), propertyHasType, propertiesWithIndex);
			System.out.println("Finished");
			janusImporter.openConnection();
			startLoadingData = System.currentTimeMillis();
			for(File file : vertexFiles)
			{
				System.out.println("Loading vertex file "  + file.getName() + "...");
				try {
					janusImporter.loadVertices(file, true, importerBatchSize, importerReportPeriod, importerThreadCount, propertyHasType, convertCardJanus(propertyHasCardinality));
				} catch (IOException | ParseException | InterruptedException e) {
					System.out.println("error occured while loading vertex file " + file.getName());
					e.printStackTrace();
					janusImporter.closeConnection();
				}
				System.out.println("Finished");
			}

			for(File file : edgeFiles)
			{
				System.out.println("Loading edge file "  + file.getName() + "...");
				try {
					janusImporter.loadEdges(file, edgeHasUri, false, true, importerBatchSize, importerReportPeriod, importerThreadCount, propertyHasType);
				} catch (IOException | ParseException | InterruptedException e) {
					System.out.println("error occured while loading edge file " + file.getName());
					e.printStackTrace();
					janusImporter.closeConnection();
				}
				System.out.println("Finished");
			}
			janusImporter.closeConnection();
			endLoadingData = System.currentTimeMillis();

			
			/*
			 * print times
			 */
			System.out.println(String.format(
					"Time Elapsed for parsing ontology: %03dh.%02dm.%02ds",
					((((endOntologyParsing - startOntologyParsing) / 1000) / 60) / 60), ((((endOntologyParsing - startOntologyParsing) / 1000) / 60) % 60), (((endOntologyParsing - startOntologyParsing) / 1000) % 60)));
			System.out.println(String.format(
					"Time Elapsed for creating csv files: %03dh.%02dm.%02ds",
					((((endCreatingCsv - startCreatingCsv) / 1000) / 60) / 60), ((((endCreatingCsv - startCreatingCsv) / 1000) / 60) % 60), (((endCreatingCsv - startCreatingCsv) / 1000) % 60)));
			System.out.println(String.format(
					"Time Elapsed for loading data: %03dh.%02dm.%02ds",
					((((endLoadingData - startLoadingData) / 1000) / 60) / 60), ((((endLoadingData - startLoadingData) / 1000) / 60) % 60), (((endLoadingData - startLoadingData) / 1000) % 60)));
 
			/*
			 * calculate the coverage
			 */
			try {
				HDT hdtFile = HDTManager.loadHDT(outputDir + s + "dataset.hdt", null);
				
				long tripleCount = hdtFile.search("", "", "").estimatedNumResults();
				long tripleEncountered = 
						rdfToCsv.getTripleVertexCount() +
						rdfToCsv.getTriplePropertyCount() +
						rdfToCsv.getTripleEdgeCount();
				double coverage = (((double) tripleEncountered) / ((double) tripleCount));
				System.out.println("TripleCount: " + tripleCount);
				System.out.println("TripleEncountered: " + tripleEncountered);
				System.out.println("coverage: " + coverage * 100 + "%");

			} catch (NotFoundException | IOException e) {
				System.out.println("Error calculating coverage");
				e.printStackTrace();
			}
		}

		private static HashMap<String, RdfToCsv.Cardinality> convertCardRdfToCsv(
				HashMap<String, SchemaStructures.Cardinality> propertyHasCardinality) {

			HashMap<String, RdfToCsv.Cardinality> result = new HashMap<>();
			
			for(String key : propertyHasCardinality.keySet()) {
				
				switch(propertyHasCardinality.get(key)) {
				case LIST:
				{
					result.put(key, RdfToCsv.Cardinality.LIST);
					break;
				}
				case SINGLE:
				{
					result.put(key, RdfToCsv.Cardinality.SINGLE);
					break;
				}
				}
				
			}

			return result;
		}
		
		private static HashMap<String, org.janusgraph.core.Cardinality> convertCardJanus(
				HashMap<String, SchemaStructures.Cardinality> propertyHasCardinality) {

			HashMap<String, org.janusgraph.core.Cardinality> result = new HashMap<>();
			
			for(String key : propertyHasCardinality.keySet()) {
				
				switch(propertyHasCardinality.get(key)) {
				case LIST:
				{
					result.put(key, org.janusgraph.core.Cardinality.LIST);
					break;
				}
				case SINGLE:
				{
					result.put(key, org.janusgraph.core.Cardinality.SINGLE);
					break;
				}
				}
				
			}

			return result;
		}
}
