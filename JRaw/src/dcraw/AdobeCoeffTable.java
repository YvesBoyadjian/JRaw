/*
 * AdobeCoeffTable.java
 *
 * Created on 7 octobre 2007, 20:47
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
public class AdobeCoeffTable {
    Vector values = new Vector<AdobeCoeff>();
    
    /** Creates a new instance of AdobeCoeffTable */
    public AdobeCoeffTable() {
    }
    
    int size() {
        return values.size();
    }
    AdobeCoeff get(int index) {
        return (AdobeCoeff) values.get(index);
    }
    
    void addElt(String prefix, int black, int maximum,
            int trans1,
            int trans2,
            int trans3,
            int trans4,
            int trans5,
            int trans6,
            int trans7,
            int trans8,
            int trans9) {
        short [] trans = new short[9];
        trans[0] = (short)trans1;
        trans[1] = (short)trans2;
        trans[2] = (short)trans3;
        trans[3] = (short)trans4;
        trans[4] = (short)trans5;
        trans[5] = (short)trans6;
        trans[6] = (short)trans7;
        trans[7] = (short)trans8;
        trans[8] = (short)trans9;
        values.add(new AdobeCoeff(prefix, (short)black, (short)maximum, trans));
    }
    void addElt(String prefix, int black,
            int trans1,
            int trans2,
            int trans3,
            int trans4,
            int trans5,
            int trans6,
            int trans7,
            int trans8,
            int trans9,
            int trans10,
            int trans11,
            int trans12) {
        short [] trans = new short[12];
        trans[0] = (short)trans1;
        trans[1] = (short)trans2;
        trans[2] = (short)trans3;
        trans[3] = (short)trans4;
        trans[4] = (short)trans5;
        trans[5] = (short)trans6;
        trans[6] = (short)trans7;
        trans[7] = (short)trans8;
        trans[8] = (short)trans9;
        trans[9] = (short)trans10;
        trans[10] = (short)trans11;
        trans[11] = (short)trans12;
        values.add(new AdobeCoeff(prefix, (short)black, (short)0, trans));
        
    }
}
