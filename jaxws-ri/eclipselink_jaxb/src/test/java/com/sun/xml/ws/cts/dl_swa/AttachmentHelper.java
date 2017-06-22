/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.cts.dl_swa;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.awt.image.*;
import javax.xml.soap.AttachmentPart;
import javax.xml.transform.stream.StreamSource;
import javax.imageio.ImageWriter;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

public class AttachmentHelper {
    public Iterator getAttachments(Iterator iter) {
        while(iter.hasNext()) {
            Object obj = iter.next();
            if(!(obj instanceof AttachmentPart)) {                    
                return null;                    
            }
        }
        return iter;
    }
    
    public AttachmentPart getAttachment(java.net.URI ref, Iterator iter) {
        if(iter == null || ref == null) {
            System.err.println("getAttachment: null Iterator for AttachmentPart");
            return null;
        }
        while(iter.hasNext()) {
            AttachmentPart tempAttachment = (AttachmentPart)iter.next();            
            if(ref.isOpaque() && ref.getScheme().equals("cid")) {
                String refId = ref.getSchemeSpecificPart();
                String cId = tempAttachment.getContentId();
                if(cId.equals("<"+refId+">") || cId.equals(refId)) {
                    return tempAttachment;
                }                
            }
        }
        return null;
    }
    
    public boolean compareStreamSource(StreamSource src1, StreamSource src2, int length) throws Exception {
        if(src1 == null || src2 == null) {
            System.err.println("compareStreamSource: src1 or src2 null!");
            return false;
        }
        String in = getStringFromStreamSource(src1, length);
        String out = getStringFromStreamSource(src2, length);
        if(in == null) 
            System.err.println("compareStreamSource: src1 is null");
        if(out == null) 
            System.err.println("compareStreamSource: src2 is null");
        return in.equals(out);
    }
    
    private String getStringFromStreamSource(StreamSource src, int length) throws Exception {
        byte buf[]=null;
        if(src == null)
            return null;
        InputStream outStr = src.getInputStream();
        if(outStr != null) {                   
            int len = outStr.available(); if(outStr.markSupported())outStr.reset();
            buf = new byte[len];
            outStr.read(buf, 0, len);
            //System.out.println("From inputstream: "+new String(buf));
            return new String(buf);
        }else {
            char buf1[] = new char[length];
            Reader r = src.getReader();
            if(r == null)
                return null;
            r.reset();                                  
            r.read(buf1);
            //System.out.println("From Reader: "+new String(buf));
            return new String(buf1);
        }
    }
    
        public boolean compareImages(Image image1, Image image2, Rectangle rect, String attach) {
        if(image1 == null || image2 == null)
            return false;
        
        boolean matched = false;
        
        Iterator iter1 = handlePixels(image1, rect);
        Iterator iter2 = handlePixels(image2, rect);
        
        while(iter1.hasNext() && iter2.hasNext()) {
            Pixel pixel = (Pixel)iter1.next();
            if(pixel.equals((Pixel)iter2.next())) {
                matched = true;
            }else {
                matched = false;
            }
        }
        if(matched)
            return true;
        return false;        
    }

     public Iterator handlePixels(Image img, Rectangle rect) {
         int x = rect.x;
         int y = rect.y;
         int w = rect.width;
         int h = rect.height;
         
        int[] pixels = new int[w * h];
        PixelGrabber pg = new PixelGrabber(img, x, y, w, h, pixels, 0, w);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
            System.err.println("interrupted waiting for pixels!");
            return null;
        }
        if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
            System.err.println("image fetch aborted or errored");
            return null;
        }
        ArrayList tmpList = new ArrayList();
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
              tmpList.add(handleSinglePixel(x+i, y+j, pixels[j * w + i]));
            }
        }
        return tmpList.iterator();
     }

     private Pixel handleSinglePixel(int x, int y, int pixel) {
        int alpha = (pixel >> 24) & 0xff;
        int red   = (pixel >> 16) & 0xff;
        int green = (pixel >>  8) & 0xff;
        int blue  = (pixel      ) & 0xff;
        return new Pixel(alpha, red, green, blue);
     }

    private class Pixel {
        private int a;
        private int r;
        private int g;
        private int b;

        Pixel(int a, int r, int g, int b) {
            this.a = a;
            this.r = r;
            this.g = g;
            this.b = b;
        }

        protected boolean equals(Pixel p) {
            if(p.a == a && p.r == r && p.g == g && p.b == b)
                return true;
            return false;
        }
    }
    


    static public Image readImage(String filePath) throws IOException {
        return javax.imageio.ImageIO.read (new File(filePath));
    }
    
    static public byte[] getImageBytes(String filePath, String type) throws IOException {
        return getImageBytes(readImage(filePath), type);
    }
    
    public static byte[] getImageBytes(Image image, String type) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        BufferedImage bufImage = convertToBufferedImage(image);
        ImageWriter writer = null;
        Iterator i = ImageIO.getImageWritersByMIMEType(type);
        if (i.hasNext()) {
            writer = (ImageWriter)i.next();
        }
        if (writer != null) {
            ImageOutputStream stream = null;
            stream = ImageIO.createImageOutputStream(baos);
            writer.setOutput(stream);
            writer.write(bufImage);
            stream.close();
            return baos.toByteArray();
        }
        return null;
    }
    private static BufferedImage convertToBufferedImage (Image image) throws IOException {
        if (image instanceof BufferedImage) {
            return (BufferedImage)image;
            
        } else {
            MediaTracker tracker = new MediaTracker (null/*not sure how this is used*/);
            tracker.addImage (image, 0);
            try {
                tracker.waitForAll ();
            } catch (InterruptedException e) {
                throw new IOException (e.getMessage ());
            }
            BufferedImage bufImage = new BufferedImage (
                image.getWidth (null),
                image.getHeight (null),
                BufferedImage.TYPE_INT_RGB);
            
            Graphics g = bufImage.createGraphics ();
            g.drawImage (image, 0, 0, null);
            return bufImage;
        }
    }
}

