/*
 * GraphicTools.java
 *
 * Created on 30 mars 2006, 22:09
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package JRaw;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

/**
 *
 * @author FabienneYves
 */
public class GraphicTools {
    
    /** Creates a new instance of GraphicTools */
    public GraphicTools() {
    }
    
    public static BufferedImage rotateImage( BufferedImage inputImage, double angleDegree) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        BufferedImage outputImage = new BufferedImage(
                width,
                height, 
                inputImage.getType());
        
        Graphics2D gr = outputImage.createGraphics();
//        gr.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//        gr.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        gr.setColor( Color.WHITE);
        gr.fillRect(0,0, width, height);
        double theta = - angleDegree * Math.PI / 180.0;
        gr.rotate(theta);
        gr.drawImage( inputImage, 0, 0, width, height, null);
        return outputImage;
    }
    
    public static BufferedImage shrinkImage( BufferedImage inputImage, int shrinkFactor) {
        int width = inputImage.getWidth()/shrinkFactor;
        int height = inputImage.getHeight()/shrinkFactor;
        BufferedImage outputImage = new BufferedImage(
                width,
                height, 
                inputImage.getType());
        
        Graphics2D gr = outputImage.createGraphics();
        gr.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        //gr.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        //gr.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gr.drawImage( inputImage, 0, 0, width, height, null);
        return outputImage;
    }
    
    public static Rectangle drawImage( Graphics2D gr, BufferedImage viewingImage, int new_width, int new_height) {
            
        int image_width = viewingImage.getWidth(null);
        int image_height = viewingImage.getHeight(null);
        
        Rectangle rect = computeImageBounds( image_width, image_height, new_width, new_height);

        //gr.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        //gr.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        //gr.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gr.drawImage( viewingImage,rect.x,rect.y,rect.width,rect.height,null);
        return rect;
    }
    
    public static Rectangle drawImage2( Graphics2D gr, BufferedImage viewingImage, int new_width, int new_height) {
            
        int image_width = viewingImage.getWidth(null);
        int image_height = viewingImage.getHeight(null);
        
        Rectangle rect = computeImageBounds( image_width, image_height, new_width, new_height);

        Image scaled = viewingImage.getScaledInstance( rect.width, rect.height, Image.SCALE_AREA_AVERAGING);
            
        //gr.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        gr.drawImage(scaled, rect.x, rect.y, null);
//        gr.drawImage( viewingImage,rect.x,rect.y,rect.width,rect.height,null);
        return rect;
    }
    
    public static Rectangle computeImageBounds(int image_width, int image_height, int new_width, int new_height) {
        
        double viewport_ratio = (double)new_width/(double)new_height;
        
        double image_ratio = (double)image_width/(double)image_height;
        
        int x,y,width, height;
        
        if ( image_ratio > viewport_ratio) { // image plus large que le viewport
            double scale = (double)new_width/(double)image_width;
        
            width = new_width;
            height = (int)Math.round( image_height * scale);
            x = 0;
            y = (new_height - height)/2;
        }
        else {
            double scale = (double)new_height/(double)image_height;
            
            width = (int) Math.round(image_width * scale);
            height = new_height;
            x = (new_width - width)/2;
            y = 0;
        }
        
        Rectangle rect = new Rectangle(x,y,width,height);
        
        return rect;
    }
    
  /**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     *
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in downscaling cases, where
     *    {@code targetWidth} or {@code targetHeight} is
     *    smaller than the original dimensions, and generally only when
     *    the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    public static BufferedImage getScaledInstance(BufferedImage img,
                                           int targetWidth,
                                           int targetHeight,
                                           Object hint,
                                           boolean higherQuality)
    {
        int type = (img.getTransparency() == Transparency.OPAQUE) ?
            BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage)img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }
        
        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }

    public static BufferedImage shrinkImage2(BufferedImage image ) {
            int width = image.getWidth()/2;
            int height = image.getHeight()/2;
            int imageType = BufferedImage.TYPE_INT_RGB;

            BufferedImage halfImage = new BufferedImage( width, height, imageType);

            for ( int i=0; i < width; i++)
                for ( int j=0; j < height; j++) {
                    Color c1 = new Color( image.getRGB(2*i, 2*j));
                    Color c2 = new Color( image.getRGB(2*i+1, 2*j));
                    Color c3 = new Color( image.getRGB(2*i, 2*j+1));
                    Color c4 = new Color( image.getRGB(2*i+1, 2*j+1));

                    int bleu = ( c1.getBlue() + c2.getBlue() + c3.getBlue()+ c4.getBlue())/4;
                    int vert = ( c1.getGreen() + c2.getGreen() + c3.getGreen() + c4.getGreen())/4;
                    int rouge = ( c1.getRed() + c2.getRed() + c3.getRed() + c4.getRed())/4;

                    int new_rgb = bleu  + (vert << 8) + (rouge << 16);

                    halfImage.setRGB(i, j, new_rgb);
                }
           return halfImage;
    }
}
