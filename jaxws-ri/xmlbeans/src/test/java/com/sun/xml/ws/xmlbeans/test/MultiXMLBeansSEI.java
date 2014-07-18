package com.sun.xml.ws.xmlbeans.test;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.oracle.wli.sb.test.xbeanA0.CountriesDocument;
import com.oracle.wli.sb.test.xbeanA1.CountryInfoType;
import com.oracle.wli.sb.test.xbeanA1.Code;
import com.oracle.wli.sb.test.xbeanB0.BooksDocument;
import com.oracle.wli.sb.test.xbeanB1.BookInfoType;
import com.oracle.wli.sb.test.xbeanB1.ISBN10;

@WebService(targetNamespace="http://www.openuri.org/MultiXMLBeansSEI")
@SOAPBinding(style=SOAPBinding.Style.DOCUMENT, use=SOAPBinding.Use.LITERAL, parameterStyle=SOAPBinding.ParameterStyle.WRAPPED)
public interface MultiXMLBeansSEI {

    public CountriesDocument addCountry(CountriesDocument countries, CountryInfoType info) throws Exception;

    @WebResult(name="info")
    public CountryInfoType getCountryInfo(final java.lang.String code, String name) throws Exception;

    @WebResult(name="name")
    public String getCountryName(CountriesDocument countries, Code code) throws Exception;

    public BooksDocument addBook(BooksDocument countries, BookInfoType info) throws Exception;

    @WebResult(name="book-info")
    public BookInfoType getBookInfoType(ISBN10 code, String name) throws Exception;

    @WebResult(name="name")
    public String getBookName(BooksDocument countries, ISBN10 code) throws Exception;

    @WebResult(name="book-info")
    public BookInfoType[] getBooks(CountryInfoType info) throws Exception;
}
