/*
 * JPEGMetaData.java
 *
 * Created on 14 novembre 2007, 21:45
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package JRaw;

import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import javax.imageio.metadata.IIOMetadata;
import java.awt.Graphics2D;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.IIOImage;
import java.io.File;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JPEGMetaData {
public JPEGMetaData() {
long timer = System.currentTimeMillis();
String sourceFile = "/home/markus/tmp/100casio/cimg2361.jpg";
String targetFile = "/home/markus/tmp/100casio/cimg2361.bak.jpg";
//String targetFile = "/home/markus/tmp/100casio/cimg2361.bak.png";
try {

File inFile = new File(sourceFile);
ImageInputStream stream = ImageIO.createImageInputStream(inFile);
Iterator iter = ImageIO.getImageReaders(stream);
File outFile = new File(targetFile);
ImageOutputStream ios = ImageIO.createImageOutputStream(outFile);


ImageReader reader = (ImageReader)iter.next();
reader.setInput(stream);
System.out.println(reader.getNumImages(false));
IIOMetadata iIOMetadata = reader.getImageMetadata(0);
BufferedImage image = reader.read(0);


Iterator writers = ImageIO.getImageWritersByFormatName("jpg");
//Iterator writers = ImageIO.getImageWritersByFormatName("png");
ImageWriter writer = (ImageWriter) writers.next();

int tw = image.getWidth()/5;
int th = image.getHeight()/5;
BufferedImage thumb = new BufferedImage(tw, th, BufferedImage.TYPE_INT_RGB);
Graphics2D g2D = thumb.createGraphics();
g2D.drawImage(image, 0, 0, tw, th, null);
List thumbnails = new ArrayList(1);
thumbnails.add(thumb);
//IIOImage holds image, thumbnails, metadata
//IIOImage iioim = new IIOImage(image, thumbnails, iIOMetadata);
IIOImage iioim = new IIOImage(image, null, iIOMetadata);
writer.setOutput(ios);
writer.write(iioim);
}
catch (Exception e) {
e.printStackTrace();
}
System.out.println("Time to read and write File: " +
( System.currentTimeMillis() -timer) +
" ms.");
}

    static void writeJPEG(BufferedImage image, File jpegFile) throws IOException {
        
        Iterator writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = (ImageWriter) writers.next();

        //try {
        File inFile = new File("D:\\Nikon\\NEF\\2006-11-04\\100_0867.jpg");
        ImageInputStream stream = ImageIO.createImageInputStream(inFile);
        Iterator iter = ImageIO.getImageReaders(stream);
        ImageReader reader = (ImageReader)iter.next();
        reader.setInput(stream);
        IIOMetadata iIOMetadata = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image),null);//reader.getImageMetadata(0);
        IIOMetadata iIOMetadata2 = reader.getImageMetadata(0);
         if (iIOMetadata.isReadOnly())
             return;
        String[] strs = iIOMetadata2.getMetadataFormatNames();
        
        String str = iIOMetadata2.getNativeMetadataFormatName();
        
        Node node =  iIOMetadata2.getAsTree("javax_imageio_1.0");
        IIOMetadataNode node2 = (IIOMetadataNode)iIOMetadata2.getAsTree("javax_imageio_jpeg_image_1.0");
        IIOMetadataNode node3 = (IIOMetadataNode)iIOMetadata.getAsTree("javax_imageio_jpeg_image_1.0");
        
        byte[] tab = null;
        
        NodeList nl1 = node2.getChildNodes();
        for ( int i=0; i< nl1.getLength(); i++ ) {
            Node n = nl1.item(i);
            if ( n.getLocalName().equals("markerSequence")) {
                IIOMetadataNode n4 = (IIOMetadataNode)n;
                IIOMetadataNode nn = (IIOMetadataNode)n4.getFirstChild();
                
                tab  = (byte[])nn.getUserObject();
                
            StringBuffer buf = new StringBuffer();
            int len = tab.length;
            for ( int ii=0; ii< len; ii++) {
                //System.out.print();
                System.out.printf( "%d %d %c\n",ii,(int)tab[ii], (char)tab[ii]);
                buf.append((char)tab[ii]);
                if ( (char)(tab[ii]) == '5')
                    tab[ii] = '6';
            }
            }
        }
        
        NodeList nl = node3.getChildNodes();
        for ( int i=0; i< nl.getLength(); i++ ) {
            Node n = nl.item(i);
            if ( n.getLocalName().equals("markerSequence")) {
                IIOMetadataNode n4 = (IIOMetadataNode)n;
                IIOMetadataNode nn = new IIOMetadataNode("unknown");
                nn.setAttribute("MarkerTag","225");                
             nn.setUserObject(tab);
             
                n4.appendChild(nn);
            }
        }
        
        //iIOMetadata.setFromTree("javax_imageio_1.0", iIOMetadata2.getAsTree("javax_imageio_1.0"));
        
        displayMetadata( node2);
        
        iIOMetadata.setFromTree("javax_imageio_jpeg_image_1.0", node3);
        
        IIOImage iioim = new IIOImage(image, null, iIOMetadata);
        
        ImageOutputStream ios = ImageIO.createImageOutputStream(jpegFile);
        
        writer.setOutput(ios);
        writer.write(iioim);
        writer.dispose();
        ios.close();
//        } catch ( IOException e) {
//            JOptionPane.showMessageDialog(null,"ooups!");
//        }
    }

public static void displayMetadata(IIOMetadataNode root) {
	displayMetadata(root, 0);
}

static void indent(int level) {
	for (int i = 0; i < level; i++) {
		System.out.print("  ");
	}
} 

static void displayMetadata( IIOMetadataNode node, int level) {
    
        Object obj =  node.getUserObject();
        
        if ( obj instanceof byte[]) {
          if ( node.getAttribute("MarkerTag").equals("225")) {
            //System.out.print(buf.toString());
            node.setUserObject(null);
          }
          byte[] exif = (byte[]) obj;
          Metadata metadata = new Metadata();
          new ExifReader().extract(exif,metadata);

          ExifIFD0Directory edir = (ExifIFD0Directory) metadata.getDirectory(ExifIFD0Directory.class);
            Date date = edir.getDate(ExifIFD0Directory.TAG_DATETIME);
            Date date2 = edir.getDate(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED);
            Date date3 = edir.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        }
            
        String str = node.getTextContent();
	indent(level); // emit open tag
	System.out.print("<" + node.getNodeName());
	NamedNodeMap map = node.getAttributes();        
	if (map != null) { // print attribute values
		int length = map.getLength();
		for (int i = 0; i < length; i++) {
			Node attr = map.item(i);
			System.out.print(" " + attr.getNodeName() +
			                 "=\"" + attr.getNodeValue() + "\"");
		}
	}

	IIOMetadataNode child = (IIOMetadataNode) node.getFirstChild();
	if (child != null) {
		System.out.println(">"); // close current tag
		while (child != null) { // emit child tags recursively
			displayMetadata(child, level + 1);
			child = (IIOMetadataNode)child.getNextSibling();
		}
		indent(level); // emit close tag
		System.out.println("</" + node.getNodeName() + ">");
	} else {
		System.out.println("/>");
	}
}

public static void main(String[] args) {
new JPEGMetaData();
}
}
