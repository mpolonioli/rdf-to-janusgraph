package net.mpolonioli.rdfontologyparser.examples;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import net.mpolonioli.rdfontologyparser.core.OntologyParser;
import net.mpolonioli.rdfontologyparser.core.SchemaStructures;

public class ExampleApp {

	public static void main(String[] args) {
		String s = File.separator;
		File ontologyFile = new File("example-resources" + s + "example-ontology.rdf");
		OntologyParser parser = new OntologyParser(ontologyFile);
		try {
			SchemaStructures schema = parser.parse();
			System.out.println("classHasUri = " + schema.getClassHasUri());
			System.out.println("classNames = " + schema.getClassNames());
			System.out.println("edgeHasUri = " + schema.getEdgeHasUri());
			System.out.println("edgeNames = " + schema.getEdgeNames());
			System.out.println("propertyHasCardinality = " + schema.getPropertyHasCardinality());
			System.out.println("propertyHasType = " + schema.getPropertyHasType());
			System.out.println("propertyHasUri = " + schema.getPropertyHasUri());
			System.out.println("propertyNames = " + schema.getPropertyNames());
			System.out.println("subClassOf = " + schema.getSubClassOf());
			System.out.println("superClassOf = " + schema.getSuperClassOf());
			System.out.println("vertexHasProperties = " + schema.getVertexHasProperties());
			System.out.println("vertexNames = " + schema.getVertexNames());
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}
}
