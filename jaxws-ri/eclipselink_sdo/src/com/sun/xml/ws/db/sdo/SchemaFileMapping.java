package com.sun.xml.ws.db.sdo;

/* $Header: webservices/src/orajaxrpc/oracle/j2ee/ws/common/wsdl/SchemaFileMapping.java /main/1 2010/04/14 10:30:39 bnaugle Exp $ */

/* Copyright (c) 2006, 2010, Oracle and/or its affiliates. 
All rights reserved. */
/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    bnaugle    Mar 29, 2010 - Creation
 */


public class SchemaFileMapping {
    private String advertisedName;
    private String path;

    public SchemaFileMapping(String advertisedName, String path) {
        this.advertisedName = advertisedName;
        this.path = path;
    }

    public String getAdvertisedName() {
        return advertisedName;
    }

    public String getPath() {
        return path;
    }
}
