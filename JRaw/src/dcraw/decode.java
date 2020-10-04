/*
 * decode.java
 *
 * Created on 12 novembre 2006, 19:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dcraw;

/**
 *
 * @author YvesFabienne
 */
public class decode {
  int branch0; // indice dans le tableau firstDecode
  int branch1;
  int leaf;
    
    /** Creates a new instance of decode */
    public decode() {
        branch0 = branch1 = -1;
        leaf = 0;
    }

    public int branch(int i) {
        switch(i) {
            case 0:
                return branch0;
            case 1:
                return branch1;
        }
        return -1;
    }
    
}
