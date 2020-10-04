/*
 * BytePtr.java
 *
 * Created on 14 novembre 2006, 21:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package portage_v3;

/**
 *
 * @author YvesFabienne
 */
public class OldBytePtr extends BaseBytePtr implements MutableValue  {
    
    /** Creates a new instance of BytePtr */
    public OldBytePtr( int size) {
        super(size);
    }
    protected OldBytePtr( BaseBytePtr other, int offset) {
        super( other, offset);
    }
    protected OldBytePtr( BaseBytePtr other) {
        super( other);
    }
//    public BytePtr( String arg) {
//        super( arg);
//    }
    
    
}
