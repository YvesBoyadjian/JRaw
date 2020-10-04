/*
 * IdentifyTable.java
 *
 * Created on 7 octobre 2007, 21:38
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dcraw;

import java.util.Vector;

/**
 *
 * @author YvesFabienne
 */
public class IdentifyTable {
    Vector values = new Vector();
    
    /** Creates a new instance of IdentifyTable */
    public IdentifyTable() {
    }
    void addElt(int fsize, String make, String model, int withjpeg) {
        values.add(new Identify(fsize,make,model,withjpeg != 0));
    }
    public Identify at( int i){
        return (Identify)values.get(i);
    }
    
    public int size() {
        return values.size();
    }
}
