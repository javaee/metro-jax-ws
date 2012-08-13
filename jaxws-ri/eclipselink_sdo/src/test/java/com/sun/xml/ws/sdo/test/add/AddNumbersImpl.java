package com.sun.xml.ws.sdo.test.add;

import org.eclipse.persistence.sdo.SDODataObject;

public class AddNumbersImpl extends SDODataObject implements AddNumbers {

   public static final int START_PROPERTY_INDEX = 0;

   public static final int END_PROPERTY_INDEX = START_PROPERTY_INDEX + 1;

   public AddNumbersImpl() {}

   public int getArg0() {
      return getInt(START_PROPERTY_INDEX + 0);
   }

   public void setArg0(int value) {
      set(START_PROPERTY_INDEX + 0 , value);
   }

   public int getArg1() {
      return getInt(START_PROPERTY_INDEX + 1);
   }

   public void setArg1(int value) {
      set(START_PROPERTY_INDEX + 1 , value);
   }


}

