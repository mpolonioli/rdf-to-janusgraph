package net.mpolonioli.rdftocsv.examples;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;

import net.mpolonioli.rdftocsv.core.RdfToCsv;
import net.mpolonioli.rdftocsv.core.RdfToCsv.Cardinality;

public class ExampleDatasetHDT {

	@SuppressWarnings("serial")
	public static void main(String[] args) throws IOException {

		RdfToCsv rdfToCsv = new RdfToCsv();
		
		String s = File.separator;

		HDT hdtFile = HDTManager.loadHDT("example-resources" + s + "example-dataset-hdt" + s + "input-hdt" + s + "dataset.hdt", null);
		File outputDirEdgeCsv = new File("example-resources" + s + "example-dataset-hdt" + s + "output-csv" + s + "edges");
		File outputDirVertexCsv = new File("example-resources" + s + "example-dataset-hdt" + s + "output-csv" + s + "vertices"); 
		List<String> classNames = new ArrayList<>(Arrays.asList("ThingClass", "PersonClass", "EmployeeClass", "ManagerClass"));
		List<String> vertexNames = new ArrayList<>(Arrays.asList("Thing", "Person", "Employee", "Manager"));
		List<String> propertyNames = new ArrayList<>(Arrays.asList("id", "name", "surname", "email", "birthdate", "isAdmin", "branchNumber", "description", "insertDate"));
		HashMap<String, String> classHasUri = new HashMap<String, String>() {
			{
				put("EmployeeClass", "http://www.example.com/ontology#Employee");
				put("ManagerClass", "http://www.example.com/ontology#Manager");
				put("ThingClass", "http://www.example.com/ontology#Thing");
				put("PersonClass", "http://www.example.com/ontology#Person");
			}
		};
		HashMap<String, List<String>> subClassOf = new HashMap<String, List<String>>() {
			{
				put("EmployeeClass", Arrays.asList("PersonClass"));
				put("ManagerClass", Arrays.asList("EmployeeClass"));
				put("PersonClass", Arrays.asList("ThingClass"));

			}
		};
		HashMap<String, String> propertyHasUri = new HashMap<String, String>() {
			{
				put("birthdate", "http://www.example.com/ontology#birthdate"); 
				put("branchNumber", "http://www.example.com/ontology#branchNumber");
				put("surname", "http://www.example.com/ontology#surname");
				put("name", "http://www.example.com/ontology#name");
				put("insertDate", "http://www.example.com/ontology#insertDate");
				put("description", "http://www.example.com/ontology#description");
				put("id", "http://www.example.com/ontology#id");
				put("isAdmin", "http://www.example.com/ontology#isAdmin");
				put("email", "http://www.example.com/ontology#email");
			};
		};
		HashMap<String, Cardinality> propertyHasCardinality = new HashMap<String, Cardinality>() {
			{
				put("birthdate", Cardinality.SINGLE); 
				put("branchNumber", Cardinality.SINGLE);
				put("surname", Cardinality.SINGLE);
				put("name", Cardinality.SINGLE);
				put("insertDate", Cardinality.SINGLE);
				put("description", Cardinality.SINGLE);
				put("id", Cardinality.SINGLE);
				put("isAdmin", Cardinality.SINGLE);
				put("email", Cardinality.LIST);
			};
		};
		HashMap<String, String> edgeHasUri = new HashMap<String, String>() {
			{
				put("type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
				put("subClassOf", "http://www.w3.org/2000/01/rdf-schema#subClassOf");
				put("knows", "http://www.example.com/ontology#knows");
			};
		};
		HashMap<String, List<String>> vertexHasProperties = new HashMap<String, List<String>>() {
			{
				put("Employee", Arrays.asList("id", "name", "surname", "email", "birthdate", "isAdmin", "description", "insertDate"));
				put("Manager",  Arrays.asList("id", "name", "surname", "email", "birthdate", "isAdmin", "branchNumber", "description", "insertDate"));
				put("Person", Arrays.asList("id", "name", "surname", "email", "birthdate", "description", "insertDate"));
				put("Thing", Arrays.asList("description", "insertDate"));
			}
		};

		rdfToCsv.createCsvFiles(hdtFile, outputDirVertexCsv, outputDirEdgeCsv, classNames, vertexNames, propertyNames, classHasUri, subClassOf, propertyHasUri, propertyHasCardinality, edgeHasUri, vertexHasProperties);

	}

}
