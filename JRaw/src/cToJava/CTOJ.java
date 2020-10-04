/*
 * CTOJ.java
 *
 * Created on 18 octobre 2007, 20:09
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package cToJava;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author YvesFabienne
 */
public class CTOJ {
    public static final int SEEK_SET = 0;
    public static final int SEEK_CUR = 1;
    public static final int SEEK_END = 2;

    public static final long UINT_MAX  = ((long)Integer.MAX_VALUE)*2 +1;
    public static final int INT_MAX  = (Integer.MAX_VALUE);

    public static int EOF = -1;

    /** Creates a new instance of CTOJ */
    public CTOJ() {
    }
    
    public static void free( VoidPtr ptr) {
        ptr.bb = null;
    }

    public static VoidPtr malloc( int size) {
        byte[] data = new byte[size];
        ByteBuffer bb = ByteBuffer.wrap(data);
        
        VoidPtr ptr = new VoidPtr(bb);
        
        return ptr;
    }
    
    public static VoidPtr calloc( int nelem, int elsize) {
        byte[] data = new byte[nelem * elsize];
        ByteBuffer bb = ByteBuffer.wrap(data);
        
        VoidPtr ptr = new VoidPtr(bb);
        
        return ptr;
    }
    
    public static NewShortPtr callocShort( int nelem, int elsize) {
        /*
        try {
            short[] data = new short[nelem * elsize/2];
        } catch ( OutOfMemoryError e) {
            System.gc();
            System.runFinalization();
        }
         *
         */
        short[] data = new short[nelem * elsize/2];
        //ByteBuffer bb = ByteBuffer.wrap(data);
        
        NewShortPtr ptr = new NewShortPtr(data);
        
        return ptr;
    }
    
    public static int ftell( Object stream) { // Version limitee à 2 GigaOctets
        if ( stream  instanceof RandomAccessFile) {
            RandomAccessFile raf = (RandomAccessFile) stream;
            try {
                return (int) raf.getFilePointer();
            } catch (IOException e) {
                return -1;
            }
        }
        if ( stream  instanceof MappedByteBuffer) {
            MappedByteBuffer raf = (MappedByteBuffer) stream;
                return (int) raf.position();
        }
        return -1;
    }
    
    public static boolean feof( Object stream) {
        if ( stream instanceof RandomAccessFile) {
            RandomAccessFile raf = (RandomAccessFile) stream;
            try {
                long ret = raf.getFilePointer();
            } catch ( IOException e) {
                return true;
            }
            return false;
        }
        if ( stream instanceof MappedByteBuffer) {
            MappedByteBuffer raf = (MappedByteBuffer) stream;
            long ret = raf.remaining();
            if ( ret==0)
                return true;
            return false;
        }
        return false;
    }
    
    public static int fseek( Object stream, int offset, int origin) {
        if ( stream instanceof RandomAccessFile) {
            RandomAccessFile raf = (RandomAccessFile) stream;
            
            try {
            switch( origin ) {
                case SEEK_SET:
                    raf.seek(offset);
                    break;
                case SEEK_CUR:
                    raf.seek(raf.getFilePointer()+offset);
                    break;
                case SEEK_END:
                    raf.seek( raf.length() + offset);
                    break;
            }
            return 0;
            } catch ( IOException e) {
                return -1;
            }
        }
        if ( stream instanceof MappedByteBuffer) {
            MappedByteBuffer raf = (MappedByteBuffer) stream;

            try {
            switch( origin ) {
                case SEEK_SET:
                    raf.position(offset);
                    break;
                case SEEK_CUR:
                    raf.position(raf.position()+offset);
                    break;
                case SEEK_END:
                    raf.position( raf.capacity() + offset);
                    break;
            }
            return 0;
            } catch ( IllegalArgumentException e) {
                return -1;
            }
        }
        return -1;
    }
    
    public static int fread(byte[] ptr, int size, int count, Object stream) {
        if ( stream instanceof RandomAccessFile ) {
            RandomAccessFile raf = (RandomAccessFile) stream;
                try {
                    if ( raf.read(ptr) == -1)
                        return -1;

                } catch ( IOException  e){
                    return 0;
                }
            return count;
        }
        if ( stream instanceof MappedByteBuffer ) {
            MappedByteBuffer raf = (MappedByteBuffer) stream;
                try {
                    raf.get(ptr);
                        //return -1;

                } catch ( BufferUnderflowException  e){
                    return 0;
                }
            return count;
        }
        return 0;
    }

    public static int fread( VoidPtr ptr, int size, int count, Object stream) {
        BytePtr ptr2 = new BytePtr();
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
                        ptr2.etoile( buffer[j]);
                        ptr2.plusPlus();
                    }
                    
                } catch ( IOException  e){
                    break;
                }
            }
            return i;
        }
        if ( stream instanceof MappedByteBuffer ) {
            MappedByteBuffer raf = (MappedByteBuffer) stream;
            byte[] buffer = new byte[size];
            int i;
            for ( i=0; i< count; i++) {
                try {
                    raf.get(buffer);
                } catch ( BufferUnderflowException  e){
                    break;
                }
                    for ( int j=0; j<size; j++) {
                        ptr2.etoile( buffer[j]);
                        ptr2.plusPlus();
                    }

            }
            return i;
        }
        return 0;
    }
    
    public static int fwrite( VoidPtr ptr, int size, int count, Object stream) {
        
        BytePtr ptr2 = new BytePtr();
        ptr2.assign(ptr);
        if ( stream instanceof RandomAccessFile ) {
            RandomAccessFile raf = (RandomAccessFile) stream;
            byte[] buffer = new byte[size];
            int i;
            for ( i=0; i< count; i++) {
                try {
                    raf.write(buffer);
                    for ( int j=0; j<size; j++) {
                        ptr2.etoile( buffer[j]);
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
    
    public static int getc( Object stream) {
        return fgetc( stream);
    }
    
    public static /*RandomAccessFile*/Object fopen( String filename, String mode) {
        RandomAccessFile raf = null;
        try {
            if ( mode.endsWith("b") )
                mode = mode.substring(0,mode.length()-1);
        raf = new RandomAccessFile(filename, mode);
        //return raf;
        } catch( IOException e ) {
            return null;
        }
        
        FileChannel canal= raf.getChannel();
        MappedByteBuffer tampon = null;
        try {
            tampon = canal.map(FileChannel.MapMode.READ_ONLY, 0, canal.size());
        } catch ( IOException e) {
            System.out.println("Echec de Creation de MappedByteBuffer()");
            return raf;
        }
        try {
            raf.close();
        } catch (IOException ex) {
            return null;
        }
        return tampon;
    }
    
    public static int fclose( Object stream) {
        if ( stream instanceof RandomAccessFile) {
            RandomAccessFile raf = (RandomAccessFile) stream;
            try {
                raf.close();
                return 0;
            } catch ( IOException e) {
                return EOF;
            }
        }
        if ( stream instanceof MappedByteBuffer) {
            MappedByteBuffer mbb = (MappedByteBuffer)stream;
            mbb.clear();
        }
        return EOF;
    }
    
    public static int fgetc( Object stream) {
        if ( stream instanceof RandomAccessFile) {
            RandomAccessFile raf = (RandomAccessFile) stream;
            try {
            return raf.read();
            } catch ( IOException e ) {
                return EOF;
            }
        }
        if ( stream instanceof MappedByteBuffer) {
            MappedByteBuffer raf = (MappedByteBuffer) stream;
            try {
            return CTOJ.toUnsigned(raf.get());
            } catch ( BufferUnderflowException e ) {
                return EOF;
            }
        }
        return EOF;
    }
    
    public static void swab(VoidPtr src, VoidPtr dest, int nbytes) {
        BytePtr src2 = new BytePtr();
        src2.assign(src);
        BytePtr dest2 = new BytePtr();
        dest2.assign(dest);
        for ( int i=0; i<nbytes; i+=2) {
            byte b1 = src2.at(i);
            byte b2 = src2.at(i+1);
            dest2.at(i, b2);
            dest2.at(i+1, b1);
        }
    }
    
   public static BytePtr memmem ( BytePtr haystack, int haystacklen,
	      String needle1, int needlelen) {
       BytePtr needle = new BytePtr(needle1);
    BytePtr c = new BytePtr();
    for (c.assign(haystack); c.lessOrEqualThan( haystack.plus( haystacklen - needlelen)); c.plusPlus())
        if ( memcmp (c, needle, needlelen) == 0)
            return c;
    return new BytePtr();
    }

   public static int memcmp( VoidPtr dst, String src, int n) {
       return memcmp(dst, new BytePtr(src), n);
   }
   
   public static int memcmp( VoidPtr dst, VoidPtr src, int n) {
       
       BytePtr dst1 = new BytePtr();
       BytePtr src1 = new BytePtr();
       dst1.assign(dst);
       src1.assign(src);
       
       if ( n==0)
           return 0;
       
       while ( --n != 0 && dst1.at(0) == src1.at(0)) {
           dst1.plusPlus();
           src1.plusPlus();
       }
       
       return dst1.at(0) - src1.at(0);
   }
   
   public static byte[] memcpy( byte[] destination, String source, int num) {
       for ( int i=0; i<num; i++) {
           destination[i] = (byte)source.charAt(i);
       }
       return destination;
   }
   
   public static VoidPtr memcpy( VoidPtr destination, VoidPtr source, int num) {
       BytePtr dest = new BytePtr();
       BytePtr src = new BytePtr();
       
       dest.assign(destination);
       src.assign(source);
       
       for ( int i=0; i<num; i++ ) {
           dest.at(i, src.at(i));
       }
       
       return destination;
   }
   
    public static int strncmp(BytePtr str1, String str2, int num) {
        for (int i=0; i<num; i++) {
            if ( str1.at(i) == str2.charAt(i) )
                continue;
            if ( str1.at(i) > str2.charAt(i) )
                return 1;
            if ( str1.at(i) < str2.charAt(i) )
                return -1;
        }
        return 0;
    }
    public static int strcmp(BytePtr s1Arg, String s2Arg) {
        BytePtr s1 = new BytePtr();
        s1.assign(s1Arg);
        BytePtr s2 = new BytePtr(s2Arg);
        
        int ret = 0;
        while(  (ret = s1.at(0) - s2.at(0))==0 && s2.at(0) != 0  ) {
            s1.plusPlus();
            s2.plusPlus();
        }
        if ( ret <0)
            ret = -1;
        else if (ret > 0)
            ret = 1;
        
        return ret;
    }
    
    public static BytePtr strcpy(BytePtr destination, String source) {
        byte[] src = source.getBytes();
        int n = source.length();
        for ( int i=0; i<n;i++)
            destination.at(i, src[i]);
        return destination;
    }
    
    public static BytePtr strchr( BytePtr str, char character) {
        BytePtr str2 = new BytePtr();
        str2.assign(str);
        int n = str.length();
        
        for ( int i=0; i<n; i++) {
            if ( str2.at(0) == character)
                return str2;
            str2.plusPlus();
        }
        
        return new BytePtr();
    }

    public static BytePtr fgets(BytePtr string, int n, Object stream) {
        if ( stream instanceof RandomAccessFile) {
            RandomAccessFile raf = (RandomAccessFile) stream;
            
            BytePtr ptr = new BytePtr();
            ptr.assign(string);
            int ch;
            
            if ( n <=0 )
                return new BytePtr();
            
            while ( --n != 0) {
                if ( ( ch = getc(stream)) == EOF) {
                    if ( ptr == string) 
                        return new BytePtr();
                    break;
                }
                if ( ptr.etoile((byte)ch) == '\n') {
                    ptr.plusPlus();
                    break;
                }
                ptr.plusPlus();
            }
            ptr.etoile((byte)0);
            return string;
        }
        if ( stream instanceof MappedByteBuffer) {
            MappedByteBuffer raf = (MappedByteBuffer) stream;

            BytePtr ptr = new BytePtr();
            ptr.assign(string);
            int ch;

            if ( n <=0 )
                return new BytePtr();

            while ( --n != 0) {
                if ( ( ch = getc(stream)) == EOF) {
                    if ( ptr == string)
                        return new BytePtr();
                    break;
                }
                if ( ptr.etoile((byte)ch) == '\n') {
                    ptr.plusPlus();
                    break;
                }
                ptr.plusPlus();
            }
            ptr.etoile((byte)0);
            return string;
        }
        return new BytePtr();
    }
    
    public static int sizeof( int[] tab) {
        return 4 * tab.length;
    }
    
    public static int sizeof( VoidPtr ptr) {
        return ptr.sizeof();
    }
    
    public static int htonl(int x) {
        return x;
    }
    
    public static short toUnsigned( byte val) {
        if ( val  < 0) {
            return (short)(val + 256);
        }
        return val;
    }
    public static int toUnsigned( short val) {
        if ( val  < 0) {
            return (int)(val + 256l*256l);
        }
        return val;
    }
    public static long toUnsigned( int val) {
        if ( val  < 0) {
            return ((long)val + 256l*256l*256l*256l);
        }
        return val;
    }
    
    public static long cutToUnsigned( long val) {
        return ( val & ( (1l << 32) - 1));
    }
}
