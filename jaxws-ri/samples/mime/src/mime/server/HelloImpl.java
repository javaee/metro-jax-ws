/**
 * $Id: HelloImpl.java,v 1.1 2005-09-01 00:06:38 vivekp Exp $
 */
package mime.server;

/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.activation.DataHandler;
import java.awt.*;
import java.io.ByteArrayInputStream;

@WebService (endpointInterface = "mime.server.Hello")
public class HelloImpl {

    /**
     *
     * @param data
     * @param body
     */
    public void echoData(String body, Holder<byte[]> data){
    }

    /**
     *
     * @param data
     * @param body
     */
    public void echoDataWithEnableMIMEContent(String body, Holder<Image> data){

    }

    /**
     *
     * @param data
     * @return
     *     returns javax.xml.transform.Source
     */
    public Source detail(DetailType data){
        return new StreamSource(new ByteArrayInputStream(sampleXML.getBytes()));
    }

    /**
     *
     * @param data
     * @return
     *     returns javax.activation.DataHandler
     */
    public DataHandler claimForm(DataHandler data){
        return data;
    }

    private final String sampleXML = "?xml version=\"1.0\" encoding=\"UTF-8\" ?> \n" +            
            "<NMEAstd>\n" +
            "<DevIdSentenceId>$GPRMC</DevIdSentenceId>\n" +
            "<Time>212949</Time>\n" +
            "<Navigation>A</Navigation>\n" +
            "<NorthOrSouth>4915.61N</NorthOrSouth>\n" +
            "<WestOrEast>12310.55W</WestOrEast>\n" +
            "<SpeedOnGround>000.0</SpeedOnGround>\n" +
            "<Course>360.0</Course>\n" +
            "<Date>030904</Date>\n" +
            "<MagneticVariation>020.3</MagneticVariation>\n" +
            "<MagneticPoleEastOrWest>E</MagneticPoleEastOrWest>\n" +
            "<ChecksumInHex>*6B</ChecksumInHex>\n" +
            "</NMEAstd>";

}
