package net.mpolonioli.rdfontologyparser.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SchemaStructures {
	
	public enum Cardinality {
		SINGLE, LIST
	}
	
	/*
	 * edges related fields
	 */
	private List<String> edgeNames = new ArrayList<>();
	private HashMap<String, String> edgeHasUri = new HashMap<>();
	
	/*
	 * properties related fields
	 */
	private List<String> propertyNames = new ArrayList<>();
	private HashMap<String, List<String>> vertexHasProperties = new HashMap<>();
	private HashMap<String, String> propertyHasUri = new HashMap<>();
	private HashMap<String, Cardinality> propertyHasCardinality = new HashMap<>();
	@SuppressWarnings("rawtypes")
	private HashMap<String, Class> propertyHasType = new HashMap<>();
	
	/*
	 * vertices related fields
	 */
	private List<String> vertexNames = new ArrayList<>();
	private List<String> classNames = new ArrayList<>();
	private HashMap<String, String> classHasUri = new HashMap<>();
	private HashMap<String, List<String>> subClassOf = new HashMap<>();
	private HashMap<String, List<String>> superClassOf = new HashMap<>();
	
	/*
	 * getter methods
	 */
	public List<String> getEdgeNames() {
		return edgeNames;
	}
	
	public List<String> getPropertyNames() {
		return propertyNames;
	}

	public List<String> getVertexNames() {
		return vertexNames;
	}
	
	public List<String> getClassNames() {
		return classNames;
	}

	public HashMap<String, String> getClassHasUri() {
		return classHasUri;
	}

	public HashMap<String, List<String>> getSubClassOf() {
		return subClassOf;
	}
	
	public HashMap<String, List<String>> getSuperClassOf() {
		return superClassOf;
	}

	public HashMap<String, List<String>> getVertexHasProperties() {
		return vertexHasProperties;
	}

	public HashMap<String, String> getPropertyHasUri() {
		return propertyHasUri;
	}
	
	public HashMap<String, Cardinality> getPropertyHasCardinality() {
		return propertyHasCardinality;
	}

	@SuppressWarnings("rawtypes")
	public HashMap<String, Class> getPropertyHasType() {
		return propertyHasType;
	}

	public HashMap<String, String> getEdgeHasUri() {
		return edgeHasUri;
	}
}
