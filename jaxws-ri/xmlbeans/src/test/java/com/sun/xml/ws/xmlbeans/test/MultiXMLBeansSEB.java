package com.sun.xml.ws.xmlbeans.test;

import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.oracle.wli.sb.test.xbeanA0.CountriesDocument;
import com.oracle.wli.sb.test.xbeanA1.Code;
import com.oracle.wli.sb.test.xbeanA1.CountryInfoType;
import com.oracle.wli.sb.test.xbeanB0.BooksDocument;
import com.oracle.wli.sb.test.xbeanB1.BookInfoType;
import com.oracle.wli.sb.test.xbeanB1.ISBN10;

@WebService(targetNamespace="http://www.openuri.org/MultiXMLBeansSEI")
@SOAPBinding(style=SOAPBinding.Style.DOCUMENT, use=SOAPBinding.Use.LITERAL, parameterStyle=SOAPBinding.ParameterStyle.WRAPPED)
public class MultiXMLBeansSEB implements MultiXMLBeansSEI {

    @Override
    public CountriesDocument addCountry(CountriesDocument countries,
            CountryInfoType info) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CountryInfoType getCountryInfo(String code, String name)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getCountryName(CountriesDocument countries, Code code)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BooksDocument addBook(BooksDocument countries, BookInfoType info)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @WebResult(name="book-info")
    public BookInfoType getBookInfoType(ISBN10 code, String name)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getBookName(BooksDocument countries, ISBN10 code)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @WebResult(name="book-info")
    public BookInfoType[] getBooks(CountryInfoType info) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
