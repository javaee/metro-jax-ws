/**
 * $Id: HelloImpl.java,v 1.2 2005-09-10 19:49:12 kohsuke Exp $
 */
package mime.server;

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
