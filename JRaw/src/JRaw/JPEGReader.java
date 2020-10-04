/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package JRaw;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.Node;

/**
 *
 * @author YvesFabienne
 */
public class JPEGReader {

    /**
     * 
     */
    public JPEGReader() {
        
    }
    
    static void readJPEG(File jpegFile) throws IOException {
        
        Iterator readers = ImageIO.getImageReadersByFormatName("jpg");
        ImageReader reader = (ImageReader) readers.next();
        
        ImageInputStream iis = ImageIO.createImageInputStream(jpegFile);
        
        reader.setInput(iis);
        IIOImage image = reader.readAll( 0, null);
        reader.dispose();
        iis.close();
        IIOMetadata meta = image.getMetadata();
        
        String[] formats = meta.getMetadataFormatNames();
        
        for ( int i=0; i< formats.length; i++) {
            Node node = meta.getAsTree(formats[i]);
        
            IIOMetadataNode root = (IIOMetadataNode) node;
        
            JPEGMetaData.displayMetadata(root);
        }
    }
}
