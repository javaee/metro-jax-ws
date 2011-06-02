package com.sun.xml.ws.sdo.test.add;

import org.eclipse.persistence.sdo.SDODataObject;

public class AddNumbersResponseImpl extends SDODataObject implements AddNumbersResponse {

   public static final int START_PROPERTY_INDEX = 0;

   public static final int END_PROPERTY_INDEX = START_PROPERTY_INDEX + 0;

   public AddNumbersResponseImpl() {}

   public int getReturn() {
      return getInt(START_PROPERTY_INDEX + 0);
   }

   public void setReturn(int value) {
      set(START_PROPERTY_INDEX + 0 , value);
   }


}

