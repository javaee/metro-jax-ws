package com.sun.xml.ws.db.sdo;

import javax.xml.transform.Source;

import org.w3c.dom.Element;

//TODO 2 xmlCatalog/entityResolver for SchemaInfo
/**
 * A SchemaInfo object contains the information of a XML Schema document.
 * 
 * @author SHIH-CHANG.CHEN@ORACLE.COM
 */
public class SchemaInfo {

	protected String systemID;
	protected String targetNamespace;
	protected boolean imported;
	protected boolean included;
	protected String schemaLocation;
	protected Source schemaSource;
	protected Element schemaElement;
	
  public SchemaInfo(String systemID, String targetNamespace, Element schema) {
    this.systemID = systemID;
    this.targetNamespace = targetNamespace;
    this.schemaElement = schema;
    
  }
	public SchemaInfo(String systemID, String targetNamespace, Source schema) {
		this.systemID = systemID;
		this.targetNamespace = targetNamespace;
		this.schemaSource = schema;
	}

  /**
   * Gets the systemID of this SchemaInfo
   *
   * @return the systemID
   */
  public String getSystemID() {
    return systemID;
  }

  /**
   * Sets the systemID of this SchemaInfo
   *
   * @param systemID the systemID to set
   */
  public void setSystemID(String systemID) {
    this.systemID = systemID;
  }

  /**
   * Gets the targetNamespace of this SchemaInfo
   *
   * @return the targetNamespace
   */
  public String getTargetNamespace() {
    return targetNamespace;
  }

  /**
   * Sets the targetNamespace of this SchemaInfo
   *
   * @param targetNamespace the targetNamespace to set
   */
  public void setTargetNamespace(String targetNamespace) {
    this.targetNamespace = targetNamespace;
  }

  /**
   * Gets the imported of this SchemaInfo
   *
   * @return the imported
   */
  public boolean isImported() {
    return imported;
  }

  /**
   * Sets the imported of this SchemaInfo
   *
   * @param imported the imported to set
   */
  public void setImported(boolean imported) {
    this.imported = imported;
  }

  /**
   * Gets the included of this SchemaInfo
   *
   * @return the included
   */
  public boolean isIncluded() {
    return included;
  }

  /**
   * Sets the included of this SchemaInfo
   *
   * @param included the included to set
   */
  public void setIncluded(boolean included) {
    this.included = included;
  }

  /**
   * Gets the schemaLocation of this SchemaInfo
   *
   * @return the schemaLocation
   */
  public String getSchemaLocation() {
    return schemaLocation;
  }

  /**
   * Sets the schemaLocation of this SchemaInfo
   *
   * @param schemaLocation the schemaLocation to set
   */
  public void setSchemaLocation(String schemaLocation) {
    this.schemaLocation = schemaLocation;
  }

  /**
   * Gets the schemaSource of this SchemaInfo
   *
   * @return the schemaSource
   */
  public Source getSchemaSource() {
    return schemaSource;
  }

  /**
   * Sets the schemaSource of this SchemaInfo
   *
   * @param schemaSource the schemaSource to set
   */
  public void setSchemaSource(Source schemaSource) {
    this.schemaSource = schemaSource;
  }

  public Element getSchemaElement() {
    return schemaElement;
  }
  
  public void setSchemaElement(Element schemaElement) {
    this.schemaElement = schemaElement;
  }
  
}
