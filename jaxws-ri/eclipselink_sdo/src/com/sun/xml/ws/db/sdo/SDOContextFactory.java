package com.sun.xml.ws.db.sdo;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.sun.xml.ws.spi.db.BindingContext;
import com.sun.xml.ws.spi.db.BindingContextFactory;
import com.sun.xml.ws.spi.db.BindingInfo;

public class SDOContextFactory extends BindingContextFactory {

    static public final String ECLIPSELINK_SDO = "eclipselink.sdo";
    static public final String TOPLINK_SDO = "toplink.sdo";

    @Override
    protected BindingContext newContext(JAXBContext context) {
        return null;
    }

    @Override
    protected BindingContext newContext(BindingInfo bi) {
        return new SDOContextWrapper(bi.properties());
    }

    @Override
    protected boolean isFor(String str) {
        return (str.equals(TOPLINK_SDO) ||
                str.equals(ECLIPSELINK_SDO)||
                str.equals(this.getClass().getName())||
                str.equals("org.eclipse.persistence.sdo"));
    }

    @Override
    protected BindingContext getContext(Marshaller m) {
        // TODO Auto-generated method stub
        return null;
    }

}
