/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

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
