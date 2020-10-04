/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package JRaw.WhiteBalance;

import JRaw.WhiteBalance.NullWhiteBalance;
import java.util.ArrayList;

/**
 *
 * @author YvesFabienne
 */
public class WhiteBalanceFactory {

    WhiteBalance nullWhiteBalance= new NullWhiteBalance();
    public ArrayList<WhiteBalance> wbv = new ArrayList<WhiteBalance>();

    public ArrayList<WhiteBalance> getVector() {
        return wbv;
    }

    public WhiteBalanceFactory() {
        /*
        wbv.add(new WhiteBalance("DayLight(Canon A720)",1.0f,0.51f,1.0f));
        wbv.add(new WhiteBalance("Tungstene(Canon A720)",1.0f,0.85f,3.0f));
         * 
         */
    }

    public WhiteBalance getNullWhiteBalance() {
        return nullWhiteBalance;
    }

    public void add( String wbs) {
        wbv.add(new WhiteBalance(wbs));
    }

    public void addWhiteBalance( String name, float red, float green, float blue) {
        int nb = wbv.size();
        for ( int i=0; i< nb; i++) {
            if ( wbv.get(i).getName().equals(name)) {
                wbv.get(i).red = red;
                wbv.get(i).green = green;
                wbv.get(i).blue = blue;
                return;
            }
        }
        wbv.add(new WhiteBalance( name, red, green, blue));
    }

    public WhiteBalance getWhiteBalance( float red, float green, float blue) {
        for ( WhiteBalance wb:wbv) {
            if ( wb.red == red && wb.green == green && wb.blue == blue) {
                return wb;
            }
        }
        return null;
    }
}
