/*
 * LoadedImageI.java
 *
 * Created on 11 décembre 2007, 22:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package JRaw;

import java.awt.image.BufferedImage;

/**
 *
 * @author YvesFabienne
 */
public interface LoadedImageI {
    
    public BufferedImage getLoadedImage();

    public MainApplication getParentApplication();
 
    public int getLoadedIndex();
    
    public void triggerScaledImage();
}
