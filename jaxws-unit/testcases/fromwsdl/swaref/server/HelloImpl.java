package fromwsdl.swaref.server;

import javax.jws.WebService;

@WebService(endpointInterface = "fromwsdl.swaref.server.Hello")
public class HelloImpl {


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
