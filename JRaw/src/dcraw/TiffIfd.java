/*
 * TiffIfd.java
 *
 * Created on 12 novembre 2006, 19:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dcraw;

/**
 *
 * @author YvesFabienne
 */
public class TiffIfd {
  int width, height, bps, comp, phint, offset, flip, samples, bytes;
    
    /** Creates a new instance of TiffIfd */
    public TiffIfd() {
        width = 0;
        height = 0;
        bps = 0;
        comp = 0;
        phint = 0;
        offset= 0;
        flip = 0;
        samples = 0;
        bytes = 0;
    }
    
}
