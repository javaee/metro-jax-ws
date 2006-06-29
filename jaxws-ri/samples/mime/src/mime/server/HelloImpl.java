/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

/**
 * $Id: HelloImpl.java,v 1.4 2006-06-29 22:30:04 ofung Exp $
 */
package mime.server;

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
