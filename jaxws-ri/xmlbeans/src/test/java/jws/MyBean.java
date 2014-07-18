package jws;

import com.bea.wli.sb.transports.ejb.test.xbean.CountriesDocument;
import com.bea.wli.sb.transports.ejb.test.xbean.CountryInfoType;

public class MyBean {
    int intCode;
    Integer integerCode;
    float[] floatArray;
    String[] stringArray;
    CountriesDocument contries;
    CountryInfoType country;
    public int getIntCode() {
        return intCode;
    }
    public void setIntCode(int intCode) {
        this.intCode = intCode;
    }
    public Integer getIntegerCode() {
        return integerCode;
    }
    public void setIntegerCode(Integer integerCode) {
        this.integerCode = integerCode;
    }
    public float[] getFloatArray() {
        return floatArray;
    }
    public void setFloatArray(float[] floatArray) {
        this.floatArray = floatArray;
    }
    public String[] getStringArray() {
        return stringArray;
    }
    public void setStringArray(String[] stringArray) {
        this.stringArray = stringArray;
    }
    public CountriesDocument getContries() {
        return contries;
    }
    public void setContries(CountriesDocument contries) {
        this.contries = contries;
    }
    public CountryInfoType getCountry() {
        return country;
    }
    public void setCountry(CountryInfoType country) {
        this.country = country;
    }

    MyBean children;
    
    public MyBean getChildren() {
        return children;
    }
    public void setChildren(MyBean children) {
        this.children = children;
    }
}
