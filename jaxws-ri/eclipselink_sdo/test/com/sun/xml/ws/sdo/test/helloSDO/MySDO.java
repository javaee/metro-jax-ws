package com.sun.xml.ws.sdo.test.helloSDO;

import commonj.sdo.DataObject;

public interface MySDO extends DataObject {

   public java.lang.String getStringPart();

   public void setStringPart(java.lang.String value);

   public int getIntPart();

   public void setIntPart(int value);

}
