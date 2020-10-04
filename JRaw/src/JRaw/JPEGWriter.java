/*
 * JPEGWriter.java
 *
 * Created on 25 novembre 2007, 20:51
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package JRaw;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
 *
 * @author YvesFabienne
 */
public class JPEGWriter {
    
    /** Creates a new instance of JPEGWriter */
    public JPEGWriter() {
    }

    static void writeJPEG(BufferedImage image, File jpegFile, float quality) throws IOException {
        if ( quality <= 1.0) {
            writeJPEGInternal( image, jpegFile, quality);
        }
        else { // Constant Size in Ko
            BufferedImage halfImage = GraphicTools.shrinkImage2(image);
            long size = 0;
            float real_quality = 0.9f;
            do {
                size = writeJPEGInternal( halfImage, jpegFile, real_quality);
                real_quality -= 0.02f;
            } while ( size/1024 > quality && real_quality > 0.0f);
        }
    }
    
    private static long writeJPEGInternal(BufferedImage image, File jpegFile, float quality) throws IOException {
        /*
        if ( jpegFile.isFile()) {
            jpegFile.delete();
        }
        */
        String[] form = ImageIO.getWriterFormatNames();
        
        Iterator writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = (ImageWriter) writers.next();

        IIOImage iioim = new IIOImage(image, null, null);
        
        OutputStream os = new FileOutputStream(jpegFile);
        os.close();
        os = new FileOutputStream(jpegFile);
        ImageOutputStream ios = ImageIO.createImageOutputStream(os);
        
        ImageWriteParam param = writer.getDefaultWriteParam();
        
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);
        
        writer.setOutput(ios);
        writer.write(null, iioim, param);
        writer.dispose();
        long size = ios.getStreamPosition();
        ios.close();
        os.close();
        return size;
    }


}
