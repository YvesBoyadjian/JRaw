package dcraw;

import cToJava.ShortPtr;
/*
 * jhead.java
 *
 * Created on 9 décembre 2006, 13:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author YvesFabienne
 */
public class jhead {
  int bits, high, wide, clrs, sraw, psv, restart, vpred[] = new int[4];
  int huff[] = new int[4];
  ShortPtr row;
    
    /** Creates a new instance of jhead */
    public jhead() {
        row = new ShortPtr();
    }
    
}
