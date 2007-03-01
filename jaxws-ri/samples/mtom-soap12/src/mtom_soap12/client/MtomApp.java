package mtom_soap12.client;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.transform.stream.StreamSource;
import javax.activation.DataHandler;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.awt.*;

public class MtomApp {
    
    public static void main (String[] args){
        try {
            Hello port = new HelloService().getHelloPort (new MTOMFeature());
            if(port == null){
                System.out.println ("TEST FAILURE: Couldnt get port!");
                System.exit (-1);
            }

            //test mtom
            testMtom (port);

            //test echo
            testEcho(port);

        } catch (Exception ex) {
            System.out.println ("SOAP 1.2 MtomApp FAILED!");
            ex.printStackTrace ();
        }
    }

    /**
     * Demonstrates xmime:expectedContentTypes annotation
     */
    public static void testMtom (Hello port) throws Exception{
        String name="Duke";
        Holder<byte[]> photo = new Holder<byte[]>(name.getBytes ());
        Holder<Image> image = new Holder<Image>(getImage ("java.jpg"));
        port.detail (photo, image);
        if(new String (photo.value).equals (name) && (image.value != null))
            System.out.println ("SOAP 1.2 testMtom() PASSED!");
        else
            System.out.println ("SOAP 1.2 testMtom() FAILED!");
    }

    /**
     * Demonstrates a basic xs:base64Binary optimization
     */
    public static void testEcho(Hello port) throws Exception {
        byte[] bytes = AttachmentHelper.getImageBytes(getImage("java.jpg"), "image/jpeg");
        Holder<byte[]> image = new Holder<byte[]>(bytes);
        port.echoData(image);
        if (image.value != null)
            System.out.println("SOAP 1.1 testEcho() PASSED!");
        else
            System.out.println("SOAP 1.1 testEcho() FAILED!");
    }


    private static Image getImage (String imageName) throws Exception {
        String location = getDataDir () + imageName;
        return javax.imageio.ImageIO.read (new File (location));
    }
    
    private static String getDataDir () {
        String userDir = System.getProperty ("user.dir");
        String sepChar = System.getProperty ("file.separator");
        return userDir+sepChar+ "common_resources/";
    }
    
    private static StreamSource getFileAsStreamSource (String fileName)
    throws Exception {
        InputStream is = null;
        String location = getDataDir () + fileName;
        File f = new File (location);
        FileInputStream fis = new FileInputStream (f);
        return new StreamSource (fis);
    }
}
