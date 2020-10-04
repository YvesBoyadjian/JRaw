/*
 * CBufferedReader.java
 *
 * Created on 20 janvier 2006, 13:01
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package portage_v3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author w22w087
 */
public class CBufferedReader extends BufferedReader {
    private boolean eos_flag = false;
    private boolean error_flag = false;
    
    /** Creates a new instance of CBufferedReader */
    public CBufferedReader( Reader in) {
        super(in);
    }
    
    
    public int read() throws IOException {
        int ret_code = super.read();
        if ( ret_code == -1)
            eos_flag = true;
        return ret_code;
    }
    
    public int read(char cbuf[], int off, int len) throws IOException {
        int ret_code = super.read( cbuf, off, len);
        if ( ret_code == -1)
            eos_flag = true;
        return ret_code;
    }

    public void reset() throws IOException {
        super.reset();
        eos_flag = false;
    }
    
    public void setError() {
        error_flag = true;
    }
    
    public boolean eos() {
        return eos_flag;
    }
    
    public boolean error() {
        return error_flag;
    }
}
