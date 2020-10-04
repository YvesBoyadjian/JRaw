/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package JRaw;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
public class PNGWriter {

    static long writePNG(BufferedImage image, File pngFile, float quality) throws IOException {

        if ( quality == MainApplication.PNG_X8_QUALITY) {
            image = GraphicTools.shrinkImage2( GraphicTools.shrinkImage2( GraphicTools.shrinkImage2(image)));
        }
        if ( quality == MainApplication.PNG_X4_QUALITY) {
            image = GraphicTools.shrinkImage2( GraphicTools.shrinkImage2(image));
        }
        if ( quality == MainApplication.PNG_X2_QUALITY) {
            image = GraphicTools.shrinkImage2(image);
        }
        if ( pngFile.isFile()) {
            pngFile.delete();
        }
        String[] form = ImageIO.getWriterFormatNames();

        Iterator writers = ImageIO.getImageWritersByFormatName("png");
        ImageWriter writer = (ImageWriter) writers.next();

        IIOImage iioim = new IIOImage(image, null, null);

        ImageOutputStream ios = ImageIO.createImageOutputStream(pngFile);

        ImageWriteParam param = writer.getDefaultWriteParam();

        //param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        //param.setCompressionQuality(quality);

        writer.setOutput(ios);
        writer.write(null, iioim, param);
        writer.dispose();
        long size = ios.getStreamPosition();
        ios.close();
        return size;
    }
}
