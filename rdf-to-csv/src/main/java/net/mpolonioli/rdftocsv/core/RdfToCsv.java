package net.mpolonioli.rdftocsv.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleString;

public class RdfToCsv {
	
	public enum Cardinality {
		SINGLE, LIST
	}
	
	private final static String s = File.separator;
	
	private List<File> vertexFiles = new ArrayList<>();
	private List<File> edgeFiles = new ArrayList<>();
	
	private long tripleVertexCount = 0;
	private long triplePropertyCount = 0;
	private long tripleEdgeCount = 0;
	
	/*
	 * put in a list all files in the given directory at any level
	 */
	private static List<File> listAllFiles(File baseDir)
	{
		List<File> result = new ArrayList<>();
	    for (File entry : baseDir.listFiles()) {
	      if (entry.isDirectory())
	      {
	    	  result.addAll(listAllFiles(entry));
	      } else
	      {
	    	  result.add(entry);
	      }
	    }
	    return result;
	}
	
	/*
	 * concat all the files in the given list and create an output.rdf file in the given output directory
	 */
	private static File concatFiles(List<File> files, File outputDir) throws IOException
	{
		
		OutputStream out = null;
		File result = new File(outputDir.getAbsolutePath() + s + "dataset.rdf");
		try {
			out = new FileOutputStream(result);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		for (File file : files)
		{			
			Scanner scanner = new Scanner(file);
			
			while(scanner.hasNextLine())
			{
				String line = scanner.nextLine() + "\n";
				out.write(line.getBytes());
			}
			
			scanner.close();
		}

		out.close();
		return result;

	}
	
	/*
	 * concat all the files in the given base directory at any level and create an output.rdf file in the given output directory
	 */
	private static File concatFiles(File baseDir, File outputDir) throws IOException
	{
		return concatFiles(listAllFiles(baseDir), outputDir);
	}
	
	/*
	 * create all the CSV from the dataset contained in the given RDF file
	 */
	public void createCsvFiles(
			File datasetRDF,
			String baseURI,
			RDFNotation rdfNotation,
			File outputDirHDT,
			File outputDirVertexCsv,
			File outputDirEdgeCsv,
			List<String> classNames,  
			List<String> vertexNames, 
			List<String> propertyNames, 
			HashMap<String, String> classHasUri, 
			HashMap<String, List<String>> subClassOf, 
			HashMap<String, String> propertyHasUri, 
			HashMap<String, Cardinality> propertyHasCardinality, 
			HashMap<String, String> edgeHasUri, 
			HashMap<String, List<String>> vertexHasProperties) throws IOException, ParserException {
		
		
		HDT hdtFile = null;
		
		System.out.println("Converting RDF dataset in HDT...");
		if(datasetRDF.isDirectory())
		{
			datasetRDF = concatFiles(datasetRDF, outputDirHDT);
			hdtFile = HDTManager.generateHDT(datasetRDF.getAbsolutePath(), baseURI, rdfNotation, new HDTSpecification(), null);
			hdtFile.saveToHDT(outputDirHDT.getAbsolutePath() + s + "dataset.hdt", null);
			datasetRDF.delete();
		} else
		{
			hdtFile = HDTManager.generateHDT(datasetRDF.getAbsolutePath(), baseURI, rdfNotation, new HDTSpecification(), null);
			hdtFile.saveToHDT(outputDirHDT.getAbsolutePath() + s +"dataset.hdt", null);
		}
		createCsvFiles(hdtFile, outputDirVertexCsv, outputDirEdgeCsv, classNames, vertexNames, propertyNames, classHasUri, subClassOf, propertyHasUri, propertyHasCardinality, edgeHasUri, vertexHasProperties);
	}
	
	/*
	 * create all the CSV from the dataset contained in the given HDT file
	 */
	public void createCsvFiles (
			HDT hdtFile, 
			File outputDirVertexCsv,
			File outputDirEdgeCsv, 
			List<String> classNames,  
			List<String> vertexNames, 
			List<String> propertyNames, 
			HashMap<String, String> classHasUri, 
			HashMap<String, List<String>> subClassOf, 
			HashMap<String, String> propertyHasUri, 
			HashMap<String, Cardinality> propertyHasCardinality, 
			HashMap<String, String> edgeHasUri, 
			HashMap<String, List<String>> vertexHasProperties) {
		
		createCsvClasses(classNames, classHasUri, outputDirVertexCsv);
		createCsvVerticies(hdtFile, vertexNames, propertyNames, classHasUri, vertexHasProperties, propertyHasUri, propertyHasCardinality, outputDirVertexCsv, outputDirEdgeCsv);
		createCsvSubClassOf(classHasUri, subClassOf, outputDirEdgeCsv);
		createCsvEdges(hdtFile, edgeHasUri, outputDirEdgeCsv);
	
	}
	
	/*
	 * create a CSV file for each vertex
	 * create and fill also the edge file type.csv
	 */
	private void createCsvVerticies (
			HDT hdtFile,
			List<String> vertexNames,
			List<String> propertyNames,
			HashMap<String, String> classHasUri,
			HashMap<String, List<String>> vertexHasProperties,
			HashMap<String, String> propertyHasUri,
			HashMap<String, Cardinality> propertyHasCardinality,
			File outputVertexDir,
			File outputEdgeDir
			)
	{
		
		String filePath = outputEdgeDir.getAbsolutePath() + s + "type.csv";
		File typeFile = new File(filePath);
		try {
			if(!typeFile.exists())
			{
				typeFile.createNewFile();
				PrintWriter writer = new PrintWriter(new PrintWriter(typeFile), true);
				String firstLine = "uri|uri";
				writer.println(firstLine);
				writer.close();
			}else
			{
				typeFile.delete();
				typeFile.createNewFile();
				PrintWriter writer = new PrintWriter(new PrintWriter(typeFile), true);
				String firstLine = "uri|uri";
				writer.println(firstLine);
				writer.close();
			}
		}catch (IOException e) {
			System.out.println("error occured creating file " + typeFile.getName());
			e.printStackTrace();
		}
		edgeFiles.add(typeFile);
		
		for(String vertexName : vertexNames)
		{
			List<String> vertexProperties = vertexHasProperties.get(vertexName);
			createVertexCsv(hdtFile, vertexName, vertexProperties, classHasUri, propertyHasUri, propertyHasCardinality, outputVertexDir, outputEdgeDir);
		}
	}
	
	/*
	 * create a single CSV file for the given vertex
	 */
	private File createVertexCsv(
			HDT hdtFile,
			String vertexName,
			List<String> vertexProperties,
			HashMap<String, String> classHasUri,
			HashMap<String, String> propertyHasUri,
			HashMap<String, Cardinality> propertyHasCardinality,
			File outputVertexDir,
			File outputEdgeDir
			)
	{
		
		long count = 0;
		String className = vertexName + "Class";
		String classUri = classHasUri.get(className);
		
		// create the vertexName.csv file
		String filePath = outputVertexDir.getAbsolutePath() + s + vertexName + ".csv";
		File vertexFile = new File(filePath);
		if(vertexFile.exists())
		{
			vertexFile.delete();
		}
		
		System.out.println("Creating vertex file " + vertexFile.getName() + "...");
		PrintWriter writer = null;
		try {
			vertexFile.createNewFile();
			vertexFiles.add(vertexFile);
			writer = new PrintWriter(new PrintWriter(vertexFile), true);
		}catch (IOException e) {
			System.out.println("Error occured with file " + vertexFile.getName());
			e.printStackTrace();
		}
		
		// add the first line to the file
		String firstLine = "";
		for(String vertexProperty : vertexProperties)
		{
			firstLine += vertexProperty + "|";
		}
		firstLine += "uri";
		writer.println(firstLine);
		
		try {
			
			IteratorTripleString objClassIterator = hdtFile.search("", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", classUri);
			
			// iterate to all the vertex of the class className
			while(objClassIterator.hasNext())
			{
				
				TripleString tripleString = objClassIterator.next();
				count++;
				tripleVertexCount++;
				
				String objUri = tripleString.getSubject().toString();
				
				// add a line to the type.csv edge file
				addLineType(objUri, classUri, outputEdgeDir);
				
				String line = getVertexValuesLine(hdtFile, objUri, vertexProperties, propertyHasUri, propertyHasCardinality);
				
				// add a line with the property values to the csv file
				writer.println(line);
			}
		}catch(NotFoundException e)
		{
		}
		writer.close();
		System.out.println("Created, written " + count + " lines");
		return vertexFile;
	}

	/*
	 * return all the values of the properties of the given vertex by his uri
	 */
	private String getVertexValuesLine(
			HDT hdtFile,
			String objUri,
			List<String> vertexProperties,
			HashMap<String, String> propertyHasUri,
			HashMap<String, Cardinality> propertyHasCardinality
			) 
	{
		String line = "";
		for(String propertyName : vertexProperties)
		{
			String propertyUri = propertyHasUri.get(propertyName);

			try {
				IteratorTripleString propertyValuesIterator = hdtFile.search(objUri, propertyUri, "");

				if(propertyValuesIterator.estimatedNumResults() > 0)
				{
					if(propertyHasCardinality.get(propertyName).equals(Cardinality.SINGLE))
					{
						TripleString triple = propertyValuesIterator.next();
						triplePropertyCount++;
						
						String propertyStringValue = triple.getObject().toString().split("\"")[1];
						line += propertyStringValue + "|";
					}else
					{
						while(propertyValuesIterator.hasNext())
						{
							TripleString triple = propertyValuesIterator.next();
							triplePropertyCount++;
							
							String propertyStringValue = triple.getObject().toString().split("\"")[1];
							line += propertyStringValue + ";";
						}
						line = line.substring(0, line.length() - 1) + "|";
					}
				}else
				{
					line += "|";
				}
			}catch(NotFoundException e)
			{
				line += "|";
			}
		}
		// add a line with the property values to the CSV file
		line += objUri;
		return line;
	}
	
	/*
	 * create a CSV file for each classVertex
	 */
	private void createCsvClasses(
			List<String> classNames,
			HashMap<String, String> classHasUri,
			File outputDir
			)
	{
		for(String className : classNames)
		{
			createClassCsv(className, classHasUri, outputDir);
		}
	}
	
	/*
	 * create a single CSV file for the given vertexClass 
	 */
	private File createClassCsv(
			String className,
			HashMap<String, String> classHasUri,
			File outputDir
			)
	{
		long count = 0;
		String classUri = classHasUri.get(className);
		
		// create the vertexName.csv file
		String filePath = outputDir.getAbsolutePath() + s + className + ".csv";
		File classFile = new File(filePath);
		if(classFile.exists())
		{
			classFile.delete();
		}
		
		System.out.println("Creating vertex file " + classFile.getName() + "...");
		PrintWriter writer = null;
		try {
			classFile.createNewFile();
			vertexFiles.add(classFile);
			writer = new PrintWriter(new PrintWriter(classFile), true);
		}catch (IOException e) {
			System.out.println("Error occured with file " + classFile.getName());
			e.printStackTrace();
		}
		
		// add the first line to the file
		writer.println("uri");
		// add the second line with the class uri to the file
		writer.println(classUri);
	
		writer.close();
		System.out.println("Created, written " + count + " lines");
		return classFile;
	}
	
	
	/*
	 * create a CSV file for each edge label except "subClassOf.csv" and "type.csv"
	 */
	private void createCsvEdges(
			HDT hdtFile,
			HashMap<String, String> edgeHasUri,
			File outputDir
			)
	{
		
		Set<String> edgeNames = edgeHasUri.keySet();
		for(String edgeName : edgeNames)
		{
			if(!edgeName.equals("subClassOf") && !edgeName.equals("type"))
			{
				long count = 0;

				String edgeUri = edgeHasUri.get(edgeName);
				String filePath = outputDir.getAbsolutePath() + s + edgeName + ".csv";
				File edgeFile = new File(filePath);
				if(edgeFile.exists())
				{
					edgeFile.delete();
				}

				System.out.println("Creating edge file " + edgeFile.getName() + "...");
				PrintWriter writer = null;
				try {
					edgeFile.createNewFile();
					edgeFiles.add(edgeFile);
					writer = new PrintWriter(new PrintWriter(edgeFile), true);
				}catch (IOException e) {
					System.out.println("Error occured with file " + edgeFile.getName());
					e.printStackTrace();
				}

				// add the firstLine to the file
				String firstLine = "uri|uri";
				writer.println(firstLine);

				try 
				{
					IteratorTripleString edgesIterator = hdtFile.search("", edgeUri, "");

					if(edgesIterator.estimatedNumResults() > 0)
					{
						while(edgesIterator.hasNext())
						{
							TripleString edgeTriple = edgesIterator.next();
							count++;
							tripleEdgeCount++;

							String vertexUri1 = edgeTriple.getSubject().toString();
							String vertexUri2 = edgeTriple.getObject().toString();

							writer.println(vertexUri1 + "|" + vertexUri2);
						}
					}
				}catch(NotFoundException e)
				{
				}
				writer.close();
				System.out.println("Created, written " + count + " lines");
			}
		}
	}
	
	/*
	 * create the edge file subClassOf.csv
	 */
	private void createCsvSubClassOf(
			HashMap<String, String> classHasUri,
			HashMap<String, List<String>> subClassOf,
			File outputDir
			)
	{
		long count = 0;

		String filePath = outputDir.getAbsolutePath() + s + "subClassOf.csv";
		File edgeFile = new File(filePath);
		if(edgeFile.exists())
		{
			edgeFile.delete();
		}

		System.out.println("Creating edge file " + edgeFile.getName() + "...");
		PrintWriter writer = null;
		try {
			edgeFile.createNewFile();
			edgeFiles.add(edgeFile);
			writer = new PrintWriter(new PrintWriter(edgeFile), true);
		}catch (IOException e) {
			System.out.println("Error occured with file " + edgeFile.getName());
			e.printStackTrace();
		}

		// add the firstLine to the file
		String firstLine = "uri|uri";
		writer.println(firstLine);

		for( String className : subClassOf.keySet())
		{
			List<String> subClassNameList = subClassOf.get(className);
			
			for(String subClassName : subClassNameList)
			{
				String line = classHasUri.get(className) + "|" + classHasUri.get(subClassName);
				writer.println(line);
				count++;
			}
		}
		writer.close();
		System.out.println("Created, written " + count + " lines");
	}
	
	/*
	 * add a line to the file type.csv
	 */
	private void addLineType(
			String objectUri,
			String classUri,
			File outputDir
			)
	{
		String filePath = outputDir.getAbsolutePath() + s + "type.csv";
		File edgeFile = new File(filePath);
		PrintWriter writer = null;
		try {
			if(edgeFile.exists())
			{
				writer = new PrintWriter(new FileWriter(edgeFile, true), true);
			}else
			{
				edgeFile.createNewFile();
				writer = new PrintWriter(new PrintWriter(edgeFile), true);
				String firstLine = "uri|uri";
				writer.println(firstLine);
			}
		}catch (IOException e) {
			System.out.println("error occured with file " + edgeFile.getName());
			e.printStackTrace();
		}
		
		writer.println(objectUri + "|" + classUri);
		writer.close();
	}
	
	public List<File> getVertexFiles()
	{
		return vertexFiles;
	}

	public List<File> getEdgeFiles() {
		return edgeFiles;
	}

	public long getTripleVertexCount() {
		return tripleVertexCount;
	}

	public long getTripleEdgeCount() {
		return tripleEdgeCount;
	}

	public long getTriplePropertyCount() {
		return triplePropertyCount;
	}
}
