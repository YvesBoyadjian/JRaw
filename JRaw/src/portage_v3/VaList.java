/*
 * VaList.java
 *
 * Created on 14 décembre 2005, 15:09
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package portage_v3;

import java.util.ArrayList;

/**
 *
 * @author w22w087
 */
public class VaList {
    public static final int     INT = 0;
    public static final String  STRING = "";
    public static final double   DOUBLE = 0.0;
    public static final long    LONG = 0;
    public static final short   SHORT = 0;
    public static final MutableValue VOID = new MutableObject();
    
    ArrayList args = new ArrayList();
    int current_index = 0;
    
    /** Creates a new instance of VaList */
    public VaList() {
    }
    
    private VaList iaddArg( MutableValue mut) {
        args.add( mut);
        return this;
    }
    
    public VaList arg( MutableValue arg) {
        return iaddArg( arg);
    }
    
    public VaList arg( String ptr) {
        iaddArg( new CharPtr(ptr));
        return this;
    }
    
    public VaList arg( MutableCharPtr ptr) {
        iaddArg( ptr.toCharPtr());
        return this;
    }
    
    public VaList arg( long arg) {
        iaddArg( new MutableLong( arg));
        return this;
    }
    
    public VaList arg( int arg) {
        iaddArg( new MutableInteger(arg));
        return this;
    }
    
    public VaList arg( float arg) {
        iaddArg( new MutableDouble(arg));
        return this;
    }
    
    public VaList arg( double arg) {
        iaddArg( new MutableDouble(arg));
        return this;
    }
    
    public MutableValue nextArg() {
        final MutableValue mut = (MutableValue) args.get(current_index);
        current_index++;
        return mut;
    }
    
    static MutableValue va_arg ( VaList args, MutableValue type) {
        final MutableValue arg = args.nextArg();
        return arg;
    }
    
    static int va_arg ( VaList args, int type) {
        final MutableValue arg = args.nextArg();
        if ( arg instanceof MutableInteger) {
            final MutableInteger iarg = (MutableInteger) arg;
            return iarg.value;
        }
        if ( arg instanceof MutableLong) {
            final MutableLong iarg = (MutableLong) arg;
            return (int)iarg.value;
        }
        assert(false);
        return 0;
    }
    
    static long va_arg ( VaList args, long type) {
        final MutableValue arg = args.nextArg();
        assert( arg instanceof MutableLong);
        final MutableLong iarg = (MutableLong) arg;
        return iarg.value;
    }
    
    static double va_arg ( VaList args, double type) {
        final MutableValue arg = args.nextArg();
        if ( arg instanceof MutableDouble) {
            final MutableDouble iarg = (MutableDouble) arg;
            return iarg.value;
        }
        if ( arg instanceof MutableFloat) {
            final MutableFloat iarg = (MutableFloat) arg;
            return iarg.value;
        }
        assert( false);
        return 0.0;
    }
    
    static CharPtr va_arg ( VaList args, String type) {
        final MutableValue arg = args.nextArg();
        assert( arg instanceof CharPtr);
        return (CharPtr) arg;
    }
}
