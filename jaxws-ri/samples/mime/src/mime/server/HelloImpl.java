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

    public Source detail(DetailType data){
        return new StreamSource(new ByteArrayInputStream(sampleXML.getBytes()));
    }

    /**
     *
     * @param data
     * @return
     *     returns ClaimFormTypeResponse
     */
    public ClaimFormTypeResponse claimForm(ClaimFormTypeRequest data){
        ClaimFormTypeResponse resp = new ClaimFormTypeResponse();
        resp.setResponse(data.getRequest());
        return resp;
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
