/*
 * Uc.java
 *
 * Created on 12 octobre 2005, 16:59
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package portage_v3;

import cToJava.BytePtr;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.nio.BufferUnderflowException;
import java.nio.MappedByteBuffer;
import javax.swing.JOptionPane;

/**
 *
 * @author w22w087
 */
public class Uc {
    public static final int PRINTF_BUFFER_LENGTH = 9999;
    public static int errno;
    public static final String[] sys_errlist = {
        "Error 0",
        "Not owner",
        "No such file or directory",
        "No such process",
        "Interrupted system call",
        "I/O error",
        "No such device or address",
        "Arg list too long",
        "Exec format error",
        "Bad file number",
        "No child processes",
        "Resource temporarily unavailable",
        "Not enough space",
        "Permission denied",
        "Bad address",
        "Block device required",
        "Device busy",
        "File exists",
        "Cross-device link",
        "No such device",
        "Not a directory",
        "Is a directory",
        "Invalid argument",
        "File table overflow",
        "Too many open files",
        "Inappropriate ioctl for device",
        "Text file busy",
        "File too large",
        "No space left on device",
        "Illegal seek",
        "Read-only file system",
        "Too many links",
        "Broken pipe",
        "Argument out of domain",
        "Result too large",
        "No message of desired type",
        "Identifier removed",
        "Channel number out of range",
        "Level 2 not synchronized",
        "Level 3 halted",
        "Level 3 reset",
        "Link number out of range",
        "Protocol driver not attached",
        "No CSI structure available",
        "Level 2 halted",
        "Deadlock situation detected/avoided",
        "No record locks available",
        "Operation canceled",
        "Operation not supported",
        "Disc quota exceeded",
        "Bad exchange descriptor",
        "Bad request descriptor",
        "Message tables full",
        "Anode table overflow",
        "Bad request code",
        "Invalid slot",
        "File locking deadlock",
        "Bad font file format",
        "Owner of the lock died",
        "Lock is not recoverable",
        "Not a stream device",
        "No data available",
        "Timer expired",
        "Out of stream resources",
        "Machine is not on the network",
        "Package not installed",
        "Object is remote",
        "Link has been severed",
        "Advertise error",
        "Srmount error",
        "Communication error on send",
        "Protocol error",  
        "Locked lock was unmapped ",
        "Error 73",
        "Multihop attempted",
        "Error 75",
        "Error 76",
        "Not a data message",
        "File name too long",
        "Value too large for defined data type",
        "Name not unique on network",
        "File descriptor in bad state",
        "Remote address changed",
        "Can not access a needed shared library",
        "Accessing a corrupted shared library",
        ".lib section in a.out corrupted",
        "Attempting to link in more shared libraries than system limit",
        "Can not exec a shared library directly",
        "Illegal byte sequence",
        "Operation not applicable",
        "Number of symbolic links encountered during path name traversal exceeds MAXSYMLINKS",
        "Error 91",
        "Error 92",
        "Directory not empty",
        "Too many users",
        "Socket operation on non-socket",
        "Destination address required",
        "Message too long",
        "Protocol wrong type for socket",
        "Option not supported by protocol",
        "Error 100",
        "Error 101",
        "Error 102",
        "Error 103",
        "Error 104",
        "Error 105",
        "Error 106",
        "Error 107",
        "Error 108",
        "Error 109",
        "Error 110",
        "Error 111",
        "Error 112",
        "Error 113",
        "Error 114",
        "Error 115",
        "Error 116",
        "Error 117",
        "Error 118",
        "Error 119",
        "Protocol not supported",
        "Socket type not supported",
        "Operation not supported on transport endpoint",
        "Protocol family not supported",
        "Address family not supported by protocol family",
        "Address already in use",
        "Cannot assign requested address",
        "Network is down",
        "Network is unreachable",
        "Network dropped connection because of reset",
        "Software caused connection abort",
        "Connection reset by peer",
        "No buffer space available",
        "Transport endpoint is already connected",
        "Transport endpoint is not connected",
        "Structure needs cleaning",
        "Error 136",
        "Not a name file",
        "Not available",
        "Is a name file",
        "Remote I/O error",
        "Reserved for future use",
        "Error 142",
        "Cannot send after socket shutdown",
        "Too many references: cannot splice",
        "Connection timed out",
        "Connection refused",
        "Host is down",
        "No route to host",
        "Operation already in progress",
        "Operation now in progress",
        "Stale NFS file handle"
    };
    public static final int sys_nerr = 152;
    static {
        assert( sys_errlist.length == (sys_nerr));
    }
    public static final int EOF = -1;
    public static final int L_tmpnam = 25;
    
    private static final int MAXPATH = 256;
    private static final CharPtr tmpnambuf = new CharPtr(MAXPATH);
    private static long unique = 0;
    private static char PS1 = '\\';    // Primary path separator
    private static char PS2 = '/';     // Alternate path separator

    
    /**
     * Creates a new instance of Uc 
     */
    public Uc() {
    }

    public static CharPtr memset( CharPtr s, char c, int n) {
        while( n-- != 0)
            s.setAt(n, c);
        return s;
    }
    
    public static void memset( double[] s, double value, int n) {
        while( n-- != 0)
            s[n] = value;
    }
    
    public static void memset( int[] s, int value, int n) {
        while( n-- != 0)
            s[n] = value;
    }
    
    public static void memcpy( double[] target, double[] source, int n) {
        while( n-- != 0)
            target[n] = source[n];
    }
    
    public static void memcpy( float[] target, float[] source, int n) {
        while( n-- != 0)
            target[n] = source[n];
    }
    
    public static void memcpy( float[][] target, float[][] source, long n) {
            for ( int i=0; i< source.length; i++)
                for ( int j=0; j< source[i].length; j++)
                {
                    target[i][j] = source[i][j];
                    n--;
                    if ( n==0)
                        return;
                }
    }
    
    public static void memmove( CharPtr destination, CharPtr source, int num) {
        CharPtr buffer = new CharPtr(num);
        for ( int i=0; i<num;i++)
            buffer.setAt(i,source.charAt(i));
        for ( int i=0; i<num; i++)
            destination.setAt(i, buffer.charAt(i));
    }
    
    public static int memcmp( MutableCharPtr c, MutableCharPtr needle, int needlelen) {
        for ( int i=0; i<needlelen; i++) {
            if ( c.charAt(i) < needle.charAt(i))
                return -1;
            else if ( c.charAt(i) > needle.charAt(i))
                return 1;
        }
        return 0;
    }
    
    public static long sizeof( Object[] tab) {
        return tab.length;
    }
    
    public static long sizeof( float[] tab) {
        return tab.length;
    }
    
    public static long sizeof( double[] tab) {
        return tab.length;
    }
    
    public static File tmpnam( CharPtr string) {
        unique = 0;
        String path = null;
        CharPtr tempfn;
        long rc;

        if (string != null)
            tempfn = string;
        else
            tempfn = tmpnambuf;

        path = getenv("tmp");
        if ( path == null) 
            path = "/var/tmp";

        rc = gentmpfn( new CharPtr(path), "s", 0, tempfn);
        if (rc < 0) 
            return null;
        unique = rc;

        return new File(tempfn.toString());
    }
    
    static long gentmpfn( CharPtr path, String prefix, long unique, CharPtr tempfn)
    {
        final String format = "%s%4.4lx.tmp";
        int len;

        len = strlen(path);
        if (len > 0 && (path.charAt(len - 1) == PS1 || path.charAt(len - 1) == PS2)) 
            len--;

        if (unique == 0) 
            unique = System.currentTimeMillis();//clock();

        sprintf(tempfn, format, Uc.arg( prefix).arg( unique));
        File tmp = new File( path.toString(), tempfn.toString());
        tempfn.set(tmp.toString());
        while (access(new File(tempfn.toString()), 0) == 0) {
            unique++;
            sprintf(tempfn, format, Uc.arg( prefix).arg( unique));
            tmp = new File( path.toString(), tempfn.toString());
            tempfn.set(tmp.toString());
        }
        if (errno != SysErrNo.ENOENT) 
            return -1;

        return unique;
    }

    static public int access( File name, int mode) {
        boolean exists = name.exists();
        if ( exists) {
            return 0;
        }
        else {
            errno = SysErrNo.ENOENT;
            return -1;
        }
    }

    public static int chmod( File path, int mode) {
        int ret_code = chmod_( path.toString(), mode);
        if ( ret_code == 0)
            return 0;
        
        errno = ret_code;
        return -1;
    }
    
    /** Retourne zéro si OK, errno sinon.
     */
    private static native int chmod_( String path, int mode);

    public static int unlink( File path) {
        if ( path.delete())
            return 0;
        return -1;
    }
    
    public static int rename( File oldName, File newName) {
        if ( oldName.renameTo(newName))
            return 0;
        return -1;
    }
    
    public static int atoi( CharPtr str) {
        return Integer.parseInt(str.toString().trim());
    }
    
    public static int atoi( String str) {
        return Integer.parseInt(str.trim());
    }
    
    public static long atol( CharPtr str) {
        return Long.parseLong(str.toString().trim());
    }
    
    public static double atof( CharPtr str) {
        return Double.parseDouble(str.toString().trim());
    }
    
    public static double atof( String str) {
        return Double.parseDouble(str.trim());
    }
    
    public static int strlen( String str) {
            return str.length();        
    }

    public static int strlen( MutableCharPtr str) {
        return str.toString().length();
    }
    
    public static int strlen( CharPtr str) {
        return str.toString().length();
    }
    
    public static CharPtr strcpy( CharPtr s1, CharPtr s2) {
        return strcpy( s1, s2.toString());
    }
    
    public static CharPtr strcpy( CharPtr s1, MutableCharPtr s2) {
        return strcpy( s1, s2.toString());
    }
    
    public static CharPtr strcpy( CharPtr s1, String s2) {
        String ss2 = (String) s2;
        int i;
        for ( i=0; i<ss2.length() && i< s1.capacity(); i++)
            s1.setAt(i, ss2.charAt(i));
        if ( i < s1.capacity())
            s1.setAt(i, '\0');
        return s1;
    }

    /**
     *  strcpy(), strncpy()
     The strcpy() function copies string s2 to s1, including  the
     terminating  null character, stopping after the null charac-
     ter has been copied. The strncpy() function copies exactly n
     bytes,  truncating  s2  or  adding  null characters to s1 if
     necessary. The result will not  be  null-terminated  if  the
     length of s2 is n or more. Each function returns s1.
     */
    
    public static CharPtr strncpy( CharPtr s1, CharPtr s2, int n) {
        return strncpy( s1, s2.toString(), n);
    }
    public static CharPtr strncpy( CharPtr s1, String s2, int n) {
        String ss2 = (String) s2;
        int i;
        for ( i=0; i<ss2.length() && i< s1.capacity() && i<n; i++)
            s1.setAt( i, ss2.charAt(i));
        while ( i<n && i< s1.capacity()) {
            s1.setAt( i, '\0');
            i++;
        }
        return s1;
    }
    
    public static int strcasecmp(CharPtr s1, String s2) {
        return s1.toString().compareToIgnoreCase(s2);
    }
    
    public static int strcmp( String s1, String s2) {
        return s1.compareTo(s2);        
    }
    public static int strcmp( String s1, CharPtr s2) {
        return strcmp( s1, s2.toString());
    }
    public static int strcmp( CharPtr s1, String s2) {
        return strcmp( s1.toString(), s2);
    }
    public static int strcmp( CharPtr s1, CharPtr s2) {
        return strcmp( s1.toString(), s2.toString());
    }
    
    public static int strncmp( String s1, String s2, int n) {
        return strncmp( new MutableCharPtr(s1), s2, n);
    }
    public static int strncmp( String s1, CharPtr s2, int n) {
        return strncmp(new CharPtr(s1), s2, n);
    }
    public static int strncmp( CharPtr s1, String s2, int n) {
        return strncmp( s1, new CharPtr(s2), n);
    }
    public static int strncmp( MutableCharPtr s1Arg, CharPtr s2Arg, int n) {
        return strncmp ( s1Arg.toCharPtr(), s2Arg, n);
    }
    
    public static int strncmp( MutableCharPtr s1Arg, String s2Arg, int n) {
        final MutableCharPtr s1 = new MutableCharPtr( s1Arg);
        final MutableCharPtr s2 = new MutableCharPtr( s2Arg);
      if (n == 0) 
          return 0;

      --n;
      while (n != 0 && s1.etoile() != 0 && s1.etoile() == s2.etoile())
      {
          --n;
        s1.plusPlus();
        s2.plusPlus();
      }

      return s1.etoile() - s2.etoile();
    }
    
    public static int strncmp( CharPtr s1Arg, CharPtr s2Arg, int n) {
        final MutableCharPtr s1 = new MutableCharPtr( s1Arg);
        final MutableCharPtr s2 = new MutableCharPtr( s2Arg);
      if (n == 0) 
          return 0;

      --n;
      while (n != 0 && s1.etoile() != 0 && s1.etoile() == s2.etoile())
      {
          --n;
        s1.plusPlus();
        s2.plusPlus();
      }

      return s1.etoile() - s2.etoile();
    }
    
    /** Yves BOYADJIAN 30/12/2005
     */
    public static int strcspn( CharPtr s1, String s2) {
        int i = 0;
        char car;
        
        while ( ( car = s1.charAt(i)) != 0 && s2.indexOf(car) == -1) {
            i++;
        }
        return i;
    }
    
    // Interdiction de passer une String, car c'est une 
    // adresse sur la chaine passée en argument qui est retournée.
    public static CharPtr strchr( CharPtr s, char c) {
        int index = s.toString().indexOf(c);
        if ( index == -1)
            return null;
        return s.plus(index);
    }
    
    public static CharPtr strstr( CharPtr str1, String str2) {
        int index = str1.toString().indexOf(str2);
        if ( index == -1)
            return null;
        return str1.plus(index);
    }
    
    public static CharPtr strcat( CharPtr s1, CharPtr s2) {
        return strcat( s1, s2.toString());
    }
    
    public static CharPtr strcat( CharPtr dst, String srcArg) {
        final MutableCharPtr src = new MutableCharPtr( srcArg);
        final MutableCharPtr cp = new MutableCharPtr(dst);
        while ( cp.etoile() != 0) 
            cp.plusPlus();
        while ( cp.etoile( src.etoile()) != 0) {
            cp.plusPlus();
            src.plusPlus();
        }
        return dst;
    }
    
    public static CharPtr strdup( CharPtr src) {
        return new CharPtr(src.toString());
    }
    public static CharPtr strdup( String src) {
        return new CharPtr(src);
    }
    
    private static final MutableCharPtr strtok_ptr = new MutableCharPtr();
    
    public static CharPtr strtok( CharPtr s1, String s2) {
        return strtok_r( s1, new CharPtr(s2), strtok_ptr);
    }
    
    // Copyright (C) 2002 Michael Ringgaard. All rights reserved.

    public static CharPtr strtok_r( CharPtr stringArg, CharPtr control, MutableCharPtr lasts) {
      final MutableCharPtr string = new MutableCharPtr(stringArg);
      
      final MutableCharPtr str = new MutableCharPtr();
      final MutableCharPtr ctrl = new MutableCharPtr(control);

      char[] map = new char[32];
      int count;

      // Clear control map
      for (count = 0; count < 32; count++) map[count] = 0;

      // Set bits in delimiter table
      do {
          map[ ctrl.etoile() >> 3] |= (1 << ( ctrl.etoile() & (char)7)); 
          ctrl.plusPlus();
      } while (ctrl.charAt(-1) != 0);

      // Initialize str. If string is NULL, set str to the saved
      // pointer (i.e., continue breaking tokens out of the string
      // from the last strtok call)
      if ( !string.isNull())
        str.assign( string);
      else
        str.assign(lasts);

      // Find beginning of token (skip over leading delimiters). Note that
      // there is no token iff this loop sets str to point to the terminal
      // null (*str == '\0')

      while ((map[ str.etoile() >> 3] & (1 << ( str.etoile() & (char)7))) != 0 && str.etoile() != 0) 
          str.plusPlus();

      string.assign( str);

      // Find the end of the token. If it is not the end of the string,
      // put a null there
      for ( ; str.etoile() != 0; str.plusPlus())
      {
        if ( (map[ str.etoile() >> 3] & (1 << (str.etoile() & (char)7))) !=0 )
        {
          str.etoile('\0');
          str.plusPlus();
          break;
        }
      }

      // Update nexttoken
      lasts.assign( str);

      // Determine if a token has been found.
      if (string.equals(str))
        return null;
      else 
        return string.toCharPtr();
}
    
    public static String getenv(String name) {
        return System.getProperty(name);
    }
    
    public static VaList arg(MutableValue arg) {
        VaList list = new VaList();
        return list.arg(arg);       
    }
    
    public static VaList arg(String arg) {
        VaList list = new VaList();
        return list.arg(arg);       
    }
    
    public static VaList arg(CharPtr arg) {
        VaList list = new VaList();
        return list.arg(arg);       
    }
    
    public static VaList arg(long arg) {
        VaList list = new VaList();
        return list.arg(arg);       
    }
    
    public static VaList arg(int arg) {
        VaList list = new VaList();
        return list.arg(arg);       
    }
    
    public static VaList arg(float arg) {
        VaList list = new VaList();
        return list.arg(arg);       
    }
    
    public static VaList arg(double arg) {
        VaList list = new VaList();
        return list.arg(arg);       
    }
    
    public static CharPtr gcvt( double value, int ndigit, CharPtr buf) {
        CharPtr format = new CharPtr(10);
        sprintf( format, "%%.%dg", ndigit);
        sprintf(buf, format.toString(),new VaList().arg(value));
        return buf;
    }
    
    public static boolean isdigit(int c) {
        int diff = c -'0';
        if ( diff >=0 && diff <= 9)
            return true;
        return false;
    }
    
    //____________________________________________________________________printf
    public static int printf( String format) {
        System.out.print(format);
        return format.length();
    }
    
    public static int printf( CharPtr format) {
        String str = format.toString();
        System.out.print( str);
        return str.length();
    }
    
    public static int printf( String format, VaList args) {
        CharPtr s = new CharPtr(PRINTF_BUFFER_LENGTH);
        Vsprintf.sprintf( s, format, args);
        return printf( s);
    }
    
    public static int printf( String format, int arg1) {
        CharPtr s = new CharPtr(PRINTF_BUFFER_LENGTH);
        Vsprintf.sprintf( s, format, new VaList().arg(arg1));
        return printf( s);        
    }
    
    public static int printf( String format, long arg1) {
        CharPtr s = new CharPtr(PRINTF_BUFFER_LENGTH);
        Vsprintf.sprintf( s, format, new VaList().arg(arg1));
        return printf( s);        
    }
    
    public static int printf( String format, float arg1) {
        CharPtr s = new CharPtr(PRINTF_BUFFER_LENGTH);
        Vsprintf.sprintf( s, format, new VaList().arg(arg1));
        return printf( s);        
    }
    
    public static int printf( String format, String arg1) {
        CharPtr s = new CharPtr(PRINTF_BUFFER_LENGTH);
        Vsprintf.sprintf( s, format, new VaList().arg(arg1));
        return printf( s);        
    }
    
    public static int printf( String format, CharPtr arg1) {
        CharPtr s = new CharPtr(PRINTF_BUFFER_LENGTH);
        Vsprintf.sprintf( s, format, new VaList().arg(arg1));
        return printf( s);        
    }
    
    public static int printf( String format, File arg1) {
        return printf( format, arg1.toString());
    }
    
    public static int printf( String format, float arg1, float arg2) {
        CharPtr s = new CharPtr(PRINTF_BUFFER_LENGTH);
        Vsprintf.sprintf( s, format, new VaList().arg(arg1).arg(arg2));
        return printf( s);        
    }
    
    public static int printf( String format, int arg1, float arg2) {
        CharPtr s = new CharPtr(PRINTF_BUFFER_LENGTH);
        Vsprintf.sprintf( s, format, new VaList().arg(arg1).arg(arg2));
        return printf( s);        
    }
        
    public static int printf( String format, int arg1, int arg2) {
        CharPtr s = new CharPtr(PRINTF_BUFFER_LENGTH);
        Vsprintf.sprintf( s, format, new VaList().arg(arg1).arg(arg2));
        return printf( s);        
    }
    
    //___________________________________________________________________sprintf
    public static int sprintf( CharPtr s, CharPtr format) {
        return sprintf( s, format.toString());
    }
    
    public static int sprintf( CharPtr s, String format) {
        return Vsprintf.sprintf(s, format, new VaList());
    }
    
    public static int sprintf( CharPtr s, String format, VaList arg) {
        return Vsprintf.sprintf(s, format, arg);
    }
    
    public static int sprintf( CharPtr s, String format, int arg1) {
        return Vsprintf.sprintf(s, format, new VaList().arg(arg1));
    }
    
    public static int sprintf( CharPtr s, String format, long arg1) {
        return Vsprintf.sprintf(s, format, new VaList().arg(arg1));
    }
    
    public static int sprintf( CharPtr s, String format, float arg1) {
        return Vsprintf.sprintf( s, format, new VaList().arg(arg1));
    }
    
    public static int sprintf( CharPtr s, String format, CharPtr arg1) {        
        return Vsprintf.sprintf( s, format, new VaList().arg(arg1));
    }
    
    public static int sprintf( CharPtr s, String format, String arg1) {
        return Vsprintf.sprintf( s, format, new VaList().arg(arg1));
    }
    
    public static int sprintf( CharPtr s, String format, int arg1, int arg2) {
        return Vsprintf.sprintf(s, format, new VaList().arg(arg1).arg(arg2));
    }
    

    public static int sprintf ( CharPtr s, String format, String arg1, String arg2) {
        return Vsprintf.sprintf( s, format, new VaList().arg(arg1).arg(arg2));
    }
    
    public static int sprintf ( CharPtr s, String format, String arg1, CharPtr arg2) {
        return Vsprintf.sprintf( s, format, new VaList().arg(arg1).arg(arg2));
    }
    
    public static int sprintf ( CharPtr s, String format, CharPtr arg1, String arg2) {
        return Vsprintf.sprintf( s, format, new VaList().arg(arg1).arg(arg2));
    }
    
    public static int sprintf ( CharPtr s, String format, CharPtr arg1, CharPtr arg2) {
        return Vsprintf.sprintf( s, format, new VaList().arg(arg1).arg(arg2));
    }
    
    //___________________________________________________________________fprintf
    
    public static int fprintf( Writer fic_message, CharPtr format) {
        return fprintf( fic_message, format.toString());
    }
    
    public static int fprintf( Writer fic_message, String format) {
        CharPtr buffer = new CharPtr(PRINTF_BUFFER_LENGTH);
        int ret_val = sprintf( buffer, format);
        try {
            fic_message.write(buffer.toString());
            return ret_val;
        } catch( IOException e) {
            return -1;
        }
    }
    
    public static int fprintf( Writer fic_message, String format, VaList arg) {
        CharPtr buffer = new CharPtr(PRINTF_BUFFER_LENGTH);
        int ret_val = sprintf( buffer, format, arg);
        try {
            fic_message.write(buffer.toString());
            return ret_val;
        } catch( IOException e) {
            return -1;
        }
    }
    
    public static int fprintf( Writer fic_message, String format, File arg1) {
        return fprintf( fic_message, format, arg1.toString());
    }
    
    public static int fprintf( Writer fic_message, String format, String arg1) {
        CharPtr buffer = new CharPtr(PRINTF_BUFFER_LENGTH);
        int ret_val = sprintf( buffer, format, arg1);
        try {
            fic_message.write(buffer.toString());
            return ret_val;
        } catch( IOException e) {
            return -1;
        }
    }
    
    public static int fprintf( Writer fic_message, String format, int arg1) {
        CharPtr buffer = new CharPtr(PRINTF_BUFFER_LENGTH);
        int ret_val = sprintf( buffer, format, arg1);
        try {
            fic_message.write(buffer.toString());
            return ret_val;
        } catch( IOException e) {
            return -1;
        }
    }
    
    public static int fprintf( Writer fic_message, String format, long arg1) {
        CharPtr buffer = new CharPtr(PRINTF_BUFFER_LENGTH);
        int ret_val = sprintf( buffer, format, arg1);
        try {
            fic_message.write(buffer.toString());
            return ret_val;
        } catch( IOException e) {
            return -1;
        }
    }
    
    public static int fprintf( Writer fic_message, String format, float arg1) {
        CharPtr buffer = new CharPtr(PRINTF_BUFFER_LENGTH);
        int ret_val = sprintf( buffer, format, arg1);
        try {
            fic_message.write(buffer.toString());
            return ret_val;
        } catch( IOException e) {
            return -1;
        }
    }
    
    public static int fprintf( Writer fic_message, String format, CharPtr arg1) {
        return fprintf( fic_message, format, arg1.toString());
    }
    
    public static int fprintf( Writer fic_message, String format, CharPtr arg1, CharPtr arg2) {
        CharPtr buffer = new CharPtr(PRINTF_BUFFER_LENGTH);
        int ret_val = sprintf( buffer, format, arg1, arg2);
        try {
            fic_message.write(buffer.toString());
            return ret_val;
        } catch( IOException e) {
            return -1;
        }
    }
    
    public static int fprintf( Writer fic_message, String format, CharPtr arg1, CharPtr arg2, CharPtr arg3) {
        CharPtr buffer = new CharPtr(PRINTF_BUFFER_LENGTH);
        int ret_val = sprintf( buffer, format, Uc.arg( arg1).arg( arg2).arg( arg3));
        try {
            fic_message.write(buffer.toString());
            return ret_val;
        } catch( IOException e) {
            return -1;
        }
    }
    
    public static int fprintf( Writer fic_message, String format, float arg1, float arg2, float arg3, float arg4) {
        CharPtr buffer = new CharPtr(PRINTF_BUFFER_LENGTH);
        int ret_val = sprintf( buffer, format, Uc.arg( arg1).arg( arg2).arg( arg3).arg( arg4));
        try {
            fic_message.write(buffer.toString());
            return ret_val;
        } catch( IOException e) {
            return -1;
        }
    }
    
    public static int fprintf( Writer fic_message, String format, int arg1, float arg2, float arg3, float arg4) {
        CharPtr buffer = new CharPtr(PRINTF_BUFFER_LENGTH);
        int ret_val = sprintf( buffer, format, Uc.arg( arg1).arg( arg2).arg( arg3).arg( arg4));
        try {
            fic_message.write(buffer.toString());
            return ret_val;
        } catch( IOException e) {
            return -1;
        }
    }
    
    public static int fprintf( Writer fic_message, String format, String arg1, String arg2) {
        CharPtr buffer = new CharPtr(PRINTF_BUFFER_LENGTH);
        int ret_val = sprintf( buffer, format, arg1, arg2);
        try {
            fic_message.write(buffer.toString());
            return ret_val;
        } catch( IOException e) {
            return -1;
        }
    }
    
    public static int fprintf( Writer fic_message, String format, String arg1, int arg2) {
        CharPtr buffer = new CharPtr(PRINTF_BUFFER_LENGTH);
        int ret_val = sprintf( buffer, format, Uc.arg( arg1).arg( arg2));
        try {
            fic_message.write(buffer.toString());
            return ret_val;
        } catch( IOException e) {
            return -1;
        }
    }
    
    public static int fprintf( PrintStream fic_message, String format, File arg1) {
        return fprintf( new OutputStreamWriter(fic_message), format, arg1.toString());
    }
    
    public static int fprintf( PrintStream fic_message, String format, CharPtr arg1) {
        return fprintf( new OutputStreamWriter(fic_message), format, arg1.toString());
    }
    
    //____________________________________________________________________sscanf
    
    public static int sscanf( CharPtr s, String format, VaList arg) {
        return Input.sscanf( s, new CharPtr(format), arg);
    }
    
    public static int sscanf( CharPtr s, String format, MutableValue arg1) {
        return Input.sscanf( s, new CharPtr(format), new VaList().arg(arg1));
    }
    
    public static int sscanf( String s, String format, MutableValue arg1) {
        return Input.sscanf(new CharPtr(s), new CharPtr(format), new VaList().arg(arg1));
    }
    
    public static int sscanf( CharPtr s, String format, CharPtr arg1) {
        return Input.sscanf( s, new CharPtr(format), new VaList().arg(arg1));
    }

    public static int sscanf( String s, String format, CharPtr arg1) {
        return Input.sscanf(new CharPtr(s), new CharPtr(format), new VaList().arg(arg1));
    }

    public static int sscanf( CharPtr s, String format, CharPtr arg1, CharPtr arg2) {
        return Input.sscanf( s, new CharPtr(format), new VaList().arg(arg1).arg(arg2));
    }

    public static int sscanf( CharPtr s, String format, MutableValue arg1, MutableValue arg2, MutableValue arg3) {
        return Input.sscanf( s, new CharPtr(format), new VaList().arg(arg1).arg(arg2).arg(arg3));
    }
    
    public static int sscanf( BytePtr s, String format, MutableValue arg1, MutableValue arg2, MutableValue arg3) {
        CharPtr cs = new CharPtr(s.toString());
        
        return Input.sscanf( cs, new CharPtr(format), new VaList().arg(arg1).arg(arg2).arg(arg3));
    }
    
    public static int sscanf( CharPtr s, String format, MutableValue arg1, MutableValue arg2, MutableValue arg3, MutableValue arg4) {
        return Input.sscanf( s, new CharPtr(format), new VaList().arg(arg1).arg(arg2).arg(arg3).arg(arg4));
    }
    
    public static void exit( int status) {
        System.exit(status);
    }
    
    public static Process popen( String command, String mode) {
        try {
            String[] orders = new String[3];
            orders[0] = "sh";
            orders[1] = "-c";
            orders[2] = command;
            Process proc = Runtime.getRuntime().exec(orders);
            try {
            int ret_code = proc.waitFor();
            if ( ret_code != 0) {
            JOptionPane.showMessageDialog(null, 
                    "Erreur d'execution de la commande popen: \""+ command+"\", code d'erreur: "+ ret_code,
                    "Erreur CALAO", 
                    JOptionPane.ERROR_MESSAGE);                
            }
            return proc;
            } catch ( InterruptedException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                    "Execution de la commande popen avortée: \""+ command+"\"",
                    "Erreur CALAO", 
                    JOptionPane.ERROR_MESSAGE);                
            }
        } catch ( IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                    "Erreur d'execution de la commande popen: \""+ command+"\"",
                    "Erreur CALAO", 
                    JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }
    
    public static Process popen( CharPtr command, String mode ) {
        return popen( command.toString(), mode);
    }
    
    public static int fgetc( Reader stream)
    {
      int ch;
      int rc;

      try {
        ch = stream.read();
      if (ch == -1) 
      {
        return EOF;
      }

      return ch;
      } catch( IOException e) {
            ((CBufferedReader)stream).setError();
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                    "Erreur d'execution de la fonction fgetc: ",
                    "Erreur CALSIMEC", 
                    JOptionPane.ERROR_MESSAGE);
            return EOF;
      }
    }

    public static CharPtr fgets(CharPtr str, int num, Object stream) {
        if( stream instanceof RandomAccessFile) {
            RandomAccessFile raf = (RandomAccessFile) stream;
            try {
            byte[] b = new byte[num];            
            int n = raf.read(b);
            for ( int i=0; i<n ;i++)
                str.plus(i).etoile((char)b[i]);
            
            return str;
            } catch (IOException e) {
                return null;
            }
        }
        if( stream instanceof MappedByteBuffer) {
            MappedByteBuffer raf = (MappedByteBuffer) stream;
            try {
            byte[] b = new byte[num];
            raf.get(b);
            int n = num;
            for ( int i=0; i<n ;i++)
                str.plus(i).etoile((char)b[i]);

            return str;
            } catch (BufferUnderflowException e) {
                return null;
            }
        }
        return null;
    }
    public static CharPtr fgets( CharPtr s, int n, Process stream) {
        return fgets( s, n, new InputStreamReader(stream.getInputStream()));
    }
        
    public static CharPtr fgets( CharPtr s, int n, Reader stream) {
        try {
            final MutableCharPtr ptr = new MutableCharPtr(s);
            int ch;

            if ( n<=0)
                return null;

            n--;
            while( n != 0) {
                if ( ( ch = stream.read()) == -1) {
                    if ( ptr.equals(s))
                        return null;
                    break;
                }
                if (ptr.etoile((char)ch) == '\n') {
                    ptr.plusPlus();
                    break;
                }
                ptr.plusPlus();
                n--;
            }
            ptr.etoile('\0');
            return s;
        } catch( IOException e) {
            ((CBufferedReader)stream).setError();
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                    "Erreur d'execution de la commande fgets.",
                    "Erreur CALAO", 
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    public static int fread( CharPtr ptr, int size, int count, Object stream) {
        MutableCharPtr ptr2 = new MutableCharPtr();
        ptr2.assign(ptr);
        if ( stream instanceof RandomAccessFile ) {
            RandomAccessFile raf = (RandomAccessFile) stream;
            byte[] buffer = new byte[size];
            int i;
            for ( i=0; i< count; i++) {
                try {
                    if ( raf.read(buffer) == -1)
                        break;
                    for ( int j=0; j<size; j++) {
                        ptr2.etoile( (char)buffer[j]);
                        ptr2.plusPlus();
                    }
                    
                } catch ( IOException  e){
                    break;
                }
            }
            return i;
        }
        return 0;
    }
    
    
    public static int fputs( CharPtr s, Writer stream) {
        try {
            stream.write(s.toString());
            return s.toString().length();
        } catch ( IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                    "Erreur d'execution de la commande fgets.",
                    "Erreur CALAO", 
                    JOptionPane.ERROR_MESSAGE);
            return EOF;            
        }
    }
    
    public static int fflush( Writer stream) {
        try {
            stream.flush();
            return 0;
        } catch( IOException e) {
            return EOF;
        }
    }
    
    public static Object fopen( File filename, String mode) {
        if ( mode.equals("r") || mode.equals("rb")) {
            try {
                return new CBufferedReader( new FileReader(filename));
            } catch( FileNotFoundException e) {
                errno = SysErrNo.ENOENT;
                return null;
            }
        }
        if ( mode.equals("w")) {
            try {
                return new FileWriter( filename);
            } catch( IOException e) {
                errno = SysErrNo.EACCES;
                return null;
            }
        }
        if ( mode.equals("a")) {
            try {
                return new FileWriter( filename, true);
            } catch( IOException e) {
                errno = SysErrNo.EACCES;
                return null;
            }
        }
        errno = SysErrNo.EINVAL;
        assert( false);
        return null;
    }
    
    public static int fclose( Reader fr) {
        try {
            fr.close();
            return 0;
        } catch ( IOException e) {
            e.printStackTrace();
            return EOF;
        }
    }
    
    public static int fclose( Writer fw) {
        try {
            fw.flush();
            fw.close();
            return 0;
        } catch ( IOException e) {
            e.printStackTrace();
            return EOF;
        }
    }
    
    public static boolean feof( Reader stream) {
        return ((CBufferedReader)stream).eos();
    }
    
    public static boolean ferror( Reader stream) {
        return ((CBufferedReader)stream).error();
    }
    
    public static int pclose( Process stream) {
        return stream.exitValue();
    }

    public static int system( CharPtr string) {
        return system( string.toString());
    }
    
    public static int system( String string) {
        try {
            try {
            String[] orders = new String[3];
            orders[0] = "sh";
            orders[1] = "-c";
            orders[2] = string;
                Process process = Runtime.getRuntime().exec( orders);
                int ret_code = process.waitFor();
                InputStreamReader cout_reader = new InputStreamReader(process.getInputStream());
                InputStreamReader cerr_reader = new InputStreamReader(process.getErrorStream());
                int car;
                while( (car = cout_reader.read()) != -1)
                    System.out.print((char)car);
                while( (car = cerr_reader.read()) != -1)
                    System.err.print((char)car);
                return ret_code;
            } catch( InterruptedException e) {
                return -1;
            }
        } catch ( IOException e) {
            return -1;
        }
    }
    
    public static DIR opendir( File dirname) {
        if ( !dirname.exists()) {
            return null;
        }
        if ( !dirname.isDirectory()) {
            return null;
        }
        return new DIR( dirname);
    }
    
    public static Dirent readdir( DIR dirp) {
        return dirp.readdir();
    }
    
    public static int closedir( DIR dirp) {
        dirp.files = null;
        return 0;
    }
        
    private static void stringToChar( String in, CharPtr out) {
        for ( int i=0; i<in.length();i++)
            out.setAt( i, in.charAt(i));
        out.setAt(in.length(), '\0');
    }
    
    private static String firstDigits( String s) {
        String s_out = "";
        int index = 0;
        while ( s.charAt(index) >= '0' && s.charAt(index) <= '9') {
            s_out =s_out + s.charAt(index);
            index++;
        }
        return s_out;
    }

}
