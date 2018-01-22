package net.mpolonioli.rdfontologyparser.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.mpolonioli.rdfontologyparser.core.SchemaStructures.Cardinality;

public class OntologyParser {
	
	private SchemaStructures schema = new SchemaStructures();
	
	private File ontologyFile;
	
	public OntologyParser(File ontologyFile)
	{
		this.ontologyFile = ontologyFile;
	}
	
	public SchemaStructures parse() throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(ontologyFile);
		
		doc.getDocumentElement().normalize();
		
		parseClass(doc);
		parseDataTypeProperty(doc);
		parseAnnotationProperties(doc);
		parseObjectProperty(doc);
		
		return schema;
	}
	
	/*
	 * parse all classes as vertices
	 * for each class associate the respective URI;
	 * for each class associate the subclass if exists.
	 * 
	 * fill the fields: vertexNames, classHasUri, subClassOf, superClassOf, vertexHasProperties.
	 */
	private void parseClass(Document doc) throws ParserConfigurationException, SAXException, IOException
	{
		NodeList nClass = doc.getElementsByTagName("owl:Class");
		
		for (int temp = 0; temp < nClass.getLength(); temp++) {

			Node nNode = nClass.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;

				String classUri = eElement.getAttribute("rdf:about");
				if(!classUri.equals(""))
				{
					String[] classUriSplit = classUri.split("#");
					String vertexName = classUriSplit[classUriSplit.length - 1];
					String className = vertexName + "Class";

					if(!schema.getClassNames().contains(className) && !schema.getVertexNames().contains(vertexName))
					{
						schema.getVertexNames().add(vertexName);
						schema.getClassNames().add(className);
						schema.getClassHasUri().put(className, classUri);
					}
					Element subClass = (Element) eElement.getElementsByTagName("rdfs:subClassOf").item(0);
					if(subClass != null)
					{		
						if(subClass.hasChildNodes())
						{
							Element eClass = (Element) subClass.getElementsByTagName("owl:Class").item(0);
							Element eUnionOf = (Element) eClass.getElementsByTagName("owl:unionOf").item(0);
							NodeList eSubClassList = eUnionOf.getElementsByTagName("rdf:Description");
							
							for(int i = 0; i < eSubClassList.getLength(); i++)
							{
								Element eSubClass = (Element) eSubClassList.item(i);
								String superClassUri = eSubClass.getAttribute("rdf:about");
								addSubClassOf(superClassUri, className);
							}
						}else
						{
							String superClassUri = subClass.getAttribute("rdf:resource");	
							addSubClassOf(superClassUri, className);
						}
					}
				}
			}
		}
		
		for(String subClass : schema.getSubClassOf().keySet())
		{
			List<String> superClasses = schema.getSubClassOf().get(subClass);
			for(String superClass : superClasses)
			{
				if(schema.getSuperClassOf().containsKey(superClass))
				{
					if(!schema.getSuperClassOf().get(superClass).contains(subClass))
					{
						schema.getSuperClassOf().get(superClass).add(subClass);	
					}
				}else
				{
					List<String> subClassList = new ArrayList<>();
					subClassList.add(subClass);
					schema.getSuperClassOf().put(superClass, subClassList);
				}
			}
		}
	}
	
	private void addSubClassOf(String classUri, String subClassName) {
		String[] classUriSplit = classUri.split("#");
		String vertexName = classUriSplit[classUriSplit.length - 1];
		String className = vertexName + "Class";
		
		if(!schema.getVertexNames().contains(vertexName))
		{
			schema.getVertexNames().add(vertexName);
		}
		
		if(!schema.getClassNames().contains(className))
		{
			schema.getClassNames().add(className);
			schema.getClassHasUri().put(className, classUri);
		}
		
		if(schema.getSubClassOf().containsKey(subClassName))
		{
			if(!schema.getSubClassOf().get(subClassName).contains(className))
			{
				schema.getSubClassOf().get(subClassName).add(className);	
			}
		}else
		{
			List<String> superClassList = new ArrayList<>();
			superClassList.add(className);
			schema.getSubClassOf().put(subClassName, superClassList);
		}
	}
	
	/*
	 * parse all dataTypeProperty as properties.
	 * 
	 * fill the fields: 
	 * 		propertyNames, vertexHasProperties, propertyHasUri, propertyHasCardinality, propertyHasType.
	 */
	private void parseDataTypeProperty(Document doc)
	{
		
		for(String vName : schema.getVertexNames())
		{
			schema.getVertexHasProperties().put(vName, new ArrayList<String>());
		}
		
		NodeList nDatatypeProperty = doc.getElementsByTagName("owl:DatatypeProperty");
		
		for (int temp = 0; temp < nDatatypeProperty.getLength(); temp++) {

			Node nNode = nDatatypeProperty.item(temp);
			
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;
				
				String propertyUri = eElement.getAttribute("rdf:about");
				String[] propSplit = propertyUri.split("#");
				String propName = propSplit[propSplit.length - 1];
				
				if(!schema.getPropertyHasUri().containsKey(propName))
				{
					schema.getPropertyHasUri().put(propName, propertyUri);
				}
				
				Element eRange = (Element) eElement.getElementsByTagName("rdfs:range").item(0);
				Element eType = (Element) eElement.getElementsByTagName("rdf:type").item(0);
				Element eDomain = (Element) eElement.getElementsByTagName("rdfs:domain").item(0);
				
				String range = eRange.getAttribute("rdf:resource");
				String[] rangeSplit = range.split("#");
				String rangeName = rangeSplit[rangeSplit.length - 1];

				String typeName;
				try
				{
					String type = eType.getAttribute("rdf:resource");
					String[] typeSplit = type.split("#");
					typeName = typeSplit[typeSplit.length - 1];
				}catch(NullPointerException e)
				{
					typeName = "";
				}
				
				if(eDomain.hasChildNodes())
				{
					Element eClass = (Element) eDomain.getElementsByTagName("owl:Class").item(0);
					Element eUnionOf = (Element) eClass.getElementsByTagName("owl:unionOf").item(0);
					NodeList eVertexList = eUnionOf.getElementsByTagName("rdf:Description");
					
					for(int i = 0; i < eVertexList.getLength(); i++)
					{
						Element eVertex = (Element) eVertexList.item(i);
						
						String classUri = eVertex.getAttribute("rdf:about");
						String[] domainSplit = classUri.split("#");
						String vertexName = domainSplit[domainSplit.length - 1];
						
						addPropertyToSubclasses(vertexName, propName);
					}
				}else
				{
					String classUri = eDomain.getAttribute("rdf:resource");
					String[] domainSplit = classUri.split("#");
					String vertexName = domainSplit[domainSplit.length - 1];
					
					addPropertyToSubclasses(vertexName, propName);
				}
				
				// filling structures
				
				if(!schema.getPropertyNames().contains(propName))
				{
					schema.getPropertyNames().add(propName);
				}

				if(!schema.getPropertyHasCardinality().containsKey(propName))
				{
					if(typeName.equals("FunctionalProperty"))
					{
						schema.getPropertyHasCardinality().put(propName, Cardinality.SINGLE);
					}else
					{
						schema.getPropertyHasCardinality().put(propName, Cardinality.LIST);
					}
				}

				// find and add the propertyType to the HashMap propertyHasType
				switch(rangeName)
				{
				case "string": 
				{
					schema.getPropertyHasType().put(propName, String.class);
					break;
				}
				case "boolean":
				{
					schema.getPropertyHasType().put(propName, Boolean.class);
					break;
				}
				case "long":
				{
					schema.getPropertyHasType().put(propName, Long.class);
					break;
				}
				case "int":
				{
					schema.getPropertyHasType().put(propName, Integer.class);
					break;
				}
				case "dateTime":
				{
					schema.getPropertyHasType().put(propName, Date.class);
					break;
				}
				default :
				{
					break;
				}
				}
			}
		}
		
		schema.getPropertyNames().add("uri");
		schema.getPropertyHasCardinality().put("uri", Cardinality.SINGLE);
		schema.getPropertyHasType().put("uri", String.class);
	}
	
	/*
	 * parse all annotationProperty as properties.
	 * 
	 * fill the fields: 
	 * 		propertyNames, vertexHasProperties, propertyHasUri, propertyHasCardinality, propertyHasType.
	 */
	private void parseAnnotationProperties(Document doc)
	{
		NodeList nDatatypeProperty = doc.getElementsByTagName("owl:AnnotationProperty");

		for (int temp = 0; temp < nDatatypeProperty.getLength(); temp++) {

			Node nNode = nDatatypeProperty.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;

				String propertyUri = eElement.getAttribute("rdf:about");
				String[] propSplit = propertyUri.split("#");
				String propName = propSplit[propSplit.length - 1];

				// add propertyName to the poropertyNames list
				if(!schema.getPropertyNames().contains(propName))
				{
					schema.getPropertyNames().add(propName);
				}
				
				// add the propertyName and URI
				if(!schema.getPropertyHasUri().containsKey(propName))
				{
					schema.getPropertyHasUri().put(propName, propertyUri);
				}
				
				// add the property to all vertices
				for(String vertexName : schema.getVertexHasProperties().keySet())
				{
					if(!schema.getVertexHasProperties().get(vertexName).contains(propName))
					{
						schema.getVertexHasProperties().get(vertexName).add(propName);
					}
				}
				
				// declare the property cardinality as LIST
				if(!schema.getPropertyHasCardinality().containsKey(propName))
				{
					schema.getPropertyHasCardinality().put(propName, Cardinality.LIST);
				}
				
				Element eRange = (Element) eElement.getElementsByTagName("rdfs:range").item(0);

				String range = eRange.getAttribute("rdf:resource");
				String[] rangeSplit = range.split("#");
				String rangeName = rangeSplit[rangeSplit.length - 1];

				// find and add the propertyType to the HashMap propertyHasType
				switch(rangeName)
				{
				case "string": 
				{
					schema.getPropertyHasType().put(propName, String.class);
					break;
				}
				case "boolean":
				{
					schema.getPropertyHasType().put(propName, Boolean.class);
					break;
				}
				case "long":
				{
					schema.getPropertyHasType().put(propName, Long.class);
					break;
				}
				case "int":
				{
					schema.getPropertyHasType().put(propName, Integer.class);
					break;
				}
				case "dateTime":
				{
					schema.getPropertyHasType().put(propName, Date.class);
					break;
				}
				default :
				{
					break;
				}
				}
			}
		}
	}
	
	/*
	 * add the property to the vertex passed and also to the related subVertex
	 */
	private void addPropertyToSubclasses(String vertexName, String propertyName)
	{
		if(schema.getVertexHasProperties().containsKey(vertexName))
		{
			if(!schema.getVertexHasProperties().get(vertexName).contains(propertyName))
			{
				schema.getVertexHasProperties().get(vertexName).add(propertyName);
			}
		}else
		{
			List<String> propNameList = new ArrayList<>();
			propNameList.add(propertyName);
			schema.getVertexHasProperties().put(vertexName, propNameList);
		}
		
		String className = vertexName + "Class";
		if(schema.getSuperClassOf().containsKey(className))
		{
			List<String> subClassNames = schema.getSuperClassOf().get(className);
			for(String subClassName : subClassNames)
			{
				addPropertyToSubclasses(subClassName.substring(0, subClassName.length() - 5), propertyName);
			}
		}
	}
	
	/*
	 * parse all ObjectProperty as edges.
	 * 
	 * fill the fields: edgeNames, edgeHasUri
	 */
	private void parseObjectProperty(Document doc)
	{
		NodeList nObjectPropertyList = doc.getElementsByTagName("owl:ObjectProperty");
		
		for (int temp = 0; temp < nObjectPropertyList.getLength(); temp++) {

			Node nNode = nObjectPropertyList.item(temp);
			
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;
				
				String edgeUri = eElement.getAttribute("rdf:about");
				String edgeName = edgeUri.split("#")[1];
				
				if(!schema.getEdgeNames().contains(edgeUri))
				{
					schema.getEdgeNames().add(edgeUri);
				}
				
				if(!schema.getEdgeHasUri().containsKey(edgeName))
				{
					schema.getEdgeHasUri().put(edgeName, edgeUri);
				}
			}
		}
		String subClassOfEdgeName = "subClassOf";
		String subClassOfEdgeUri = "http://www.w3.org/2000/01/rdf-schema#subClassOf";
		String typeEdgeName = "type";
		String typeEdgeUri = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
		if(!schema.getEdgeNames().contains(subClassOfEdgeName))
		{
			schema.getEdgeNames().add(subClassOfEdgeUri);
			schema.getEdgeHasUri().put(subClassOfEdgeName, subClassOfEdgeUri);
		}
		if(!schema.getEdgeNames().contains(typeEdgeUri))
		{
			schema.getEdgeNames().add(typeEdgeUri);
			schema.getEdgeHasUri().put(typeEdgeName, typeEdgeUri);
		}
	}
	
	/*
	 * this method must be called after the parse() method,
	 * otherwise all the structures will be empty
	 */
	public SchemaStructures getSchemaStructures()
	{
		return schema;
	}
}
