/*
 * TiffHdr.java
 *
 * Created on 18 octobre 2007, 18:21
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dcraw;

/**
 *
 * @author YvesFabienne
 */
public class TiffHdr {
  char order, magic;
  int ifd;
  char pad, ntag;
  TiffTag[] tag = new TiffTag[15];
  int nextifd;
  char pad2, nexif;
  TiffTag[] exif = new TiffTag[4];
  short[] bps = new short[4];
  int[] rat = new int[6];
  String make = new String(), model = new String(), soft = new String(), date = new String();
    
    /** Creates a new instance of TiffHdr */
    public TiffHdr() {
    }
    
}
