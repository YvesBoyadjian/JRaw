/*
 * Input.java
 *
 * Created on 16 décembre 2005, 09:19
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

//
// input.c
//
// C formatted input, used by scanf, etc.
//
// Copyright (C) 2002 Michael Ringgaard. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 
// 1. Redistributions of source code must retain the above copyright 
//    notice, this list of conditions and the following disclaimer.  
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.  
// 3. Neither the name of the project nor the names of its contributors
//    may be used to endorse or promote products derived from this software
//    without specific prior written permission. 
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
// OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
// OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF 
// SUCH DAMAGE.
// 


package portage_v3;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.PushbackReader;

/** Portage en Java de la fonction sscanf() par Yves Boyadjian, le 12/2005.
 *
 * @author w22w087
 */
public class Input {
      PushbackReader stream;
      final MutableCharPtr format = new MutableCharPtr();
      VaList arglist;
      final CharPtr table = new CharPtr(RANGESETSIZE);           // Which chars allowed for %[], %s
      final CharPtr fltbuf = new CharPtr( Vsprintf.CVTBUFSIZE + 1);        // ASCII buffer for floats
      long number;               // Temp hold-value
      long num64;             // Temp for 64-bit integers
      Object pointer;        // Points to user data receptacle
      final MutableCharPtr start = new MutableCharPtr(); // Indicate non-empty string

      final MutableCharPtr scanptr = new MutableCharPtr();             // For building "table" data
      int ch;

      final MutableInteger charcount = new MutableInteger();                      // Total number of chars read
      int comchr;                         // Holds designator type
      int count;                          // Return value. # of assignments

      int started;                        // Indicate good number
      int width;                          // Width of field
      int widthset;                       // User has specified width

      char done_flag;                     // General purpose loop monitor
      char longone;                       // 0 = short, 1 = long, 2 = long double
      int integer64;                      // 1 for 64-bit integer, 0 otherwise
      char reject;                        // %[^ABC] instead of %[ABC]
      char negative;                      // Flag for '-' detected
      char suppress;                      // Don't assign anything
      char match;                         // Flag: !0 if any fields matched
      VaList arglistsave;                // Save arglist value

      char rngch;                // Used while scanning range
      char last;                 // Also for %[a-z]
      char prevchar;             // For %[a-z]
    
    /** Creates a new instance of Input */
    public Input() {
    }
    
    int GETCH() throws InputException {
        charcount.value++;
        return inc(stream);
    }
    
    void UNGETCH( int ch) throws InputException {
        charcount.value--;
        uninc(ch, stream);
    }
    
    static final int EOF = -1;
    static final int RANGESETSIZE = 32;             // # of bytes needed to hold 256 bits
    static final char DECIMALPOINT = '.';
    static final char LEFT_BRACKET = ('[' | ('a' - 'A')); // 'lowercase' version of [

    static final CharPtr sbrackset = new CharPtr(" \t-\r]"); // Use range-style list
    static final CharPtr cbrackset = new CharPtr("]");


    static int hextodec(int ch)
    {
      return Character.isDigit((char)ch) ? ch : (ch & ~('a' - 'A')) - 'A' + 10 + '0';
    }

    static int inc(PushbackReader stream) throws InputException
    {
        try {
      return stream.read();
        } catch( IOException e) {
            throw( new InputException(-1));
        }
    }

    static void uninc(int ch, PushbackReader stream) throws InputException
    {
      if (ch != EOF) {
          try {
          stream.unread(ch);
          } catch( IOException e) {
              throw (new InputException(-1));
          }
      }
    }

    static int skipws( MutableInteger counter, PushbackReader stream) throws InputException
    {
      int ch;

      while (true)
      {
        ch = inc(stream);
        counter.value++;
        if (!Character.isWhitespace((char)ch)) 
            return ch;
      }
    }

    int input(PushbackReader streamArg, CharPtr format_immutable, VaList arglistArg) throws InputException
    {
        stream = streamArg;
        format.assign( format_immutable);
        arglist = arglistArg;
      // count = # fields assigned
      // charcount = # chars read
      // match = flag indicating if any fields were matched

      // Note that we need both count and match.  For example, a field
      // may match a format but have assignments suppressed.  In this case,
      // match will get set, but 'count' will still equal 0.  We need to
      // distinguish 'match vs no-match' when terminating due to EOF.

      count = charcount.value = match = 0;

      while ( format.etoile() != 0) 
      {
        if ( Character.isWhitespace(format.etoile()))
        {
          UNGETCH(skipws( charcount, stream)); // Put first non-space char back

          format.plusPlus();
          while ( Character.isWhitespace(format.etoile())) {
              format.plusPlus();
          }
          // Careful: isspace macro may evaluate argument more than once!
        }

        if (format.etoile() == '%') 
        {
            formatEtoileEgalePourcent();
        }
        else  
        {
          // *format != '%'
          if ((int) format.etoile() != (int) (ch = GETCH()))
          {
              format.plusPlus();
            UNGETCH(ch);
            return error_return();
          }
          else
              format.plusPlus();
        }

        if ((ch == EOF) && ((format.etoile() != '%') || ( format.plus(1).etoile() != 'n')))  
            break;
      }

        return error_return();
    }
    
    void formatEtoileEgalePourcent() throws InputException {
        
          number = 0;
          prevchar = 0;
          width = widthset = started = 0;
          done_flag = suppress = negative = reject = 0;
          longone = 1;
          integer64 = 0;

          while (done_flag == 0) 
          {
              format.plusPlus();
            comchr = format.etoile();
            if (Character.isDigit((char)comchr)) 
            {
              ++widthset;
              width = width * 10 + (comchr - '0');
            } 
            else
            {
              switch (comchr) 
              {
                case 'F':
                case 'N':
                  // Near and far pointer modifiers ignored
                  break;

                case 'h':
                  --longone;
                  break;

                case 'I':
                  if ((format.plus(1).etoile() == '6') && ( format.plus(2).etoile() == '4'))
                  {
                    format.add( 2);
                    ++integer64;
                    num64 = 0;
                    break;
                  }
//                  goto default_label;
                  ++done_flag;
                  break;

                case 'L':
                case 'l':
                  ++longone;
                  break;

                case '*':
                  ++suppress;
                  break;

                default:
    default_label:
                  ++done_flag;
                  break;
              }
            }
          }

          if (suppress == 0) 
          {
            arglistsave = arglist;
            pointer = VaList.va_arg(arglist, VaList.VOID);
            if ( pointer instanceof CharPtr)
                pointer = new MutableCharPtr( (CharPtr)pointer);
          }

          done_flag = 0;

          // Switch to lowercase to allow %E,%G, and to keep the switch table small
          comchr = format.etoile() | ('a' - 'A');

          if (comchr != 'n')
          {
            if (comchr != 'c' && comchr != LEFT_BRACKET)
              ch = skipws( charcount, stream);
            else
              ch = GETCH();
          }
          
          if ( (widthset==0) || width !=0 ) 
          {
              switchComchr();

            // Matched a format field - set flag
            match++;        

          }
          else 
          {  
            // Zero-width field in format string
            UNGETCH(ch);
            error_return();
          }

          // Skip to next char
          format.plusPlus();
    }
    
    void switchComchr() throws InputException {
        switch(comchr) 
        {
          case 'c':
            if (widthset == 0) 
            {
              ++widthset;
              ++width;
            }
            scanptr.assign( cbrackset);
            --reject;
            scanit2();
            break;

          case 's':
            scanptr.assign(sbrackset);
            --reject;
            scanit2();
            break;

          case LEFT_BRACKET:
              format.plusPlus();
            scanptr.assign(format);

            if (scanptr.etoile() == '^') 
            {
              scanptr.plusPlus();
              --reject;
            }
            scanit2();
            break;

          case 'i':
            comchr = 'd';

          case 'x':
            if (ch == '-') 
            {
              ++negative;
              x_incwidth();
            } 
            else if (ch == '+') 
            {
                x_incwidth();
            }

            if (ch == '0') 
            {
              if ((ch = GETCH()) == 'x' || ch == 'X') 
              {
                ch = GETCH();
                comchr = 'x';
              } 
              else 
              {
                ++started;
                if (comchr != 'x') 
                  comchr = 'o';
                else 
                {
                  // Scanning a hex number that starts
                  // with a 0. Push back the character
                  // currently in ch and restore the 0
                  UNGETCH(ch);
                  ch = '0';
                }
              }
            }
            getnum();
            break;

          case 'p':
            // Force %hp to be treated as %p
            longone = 1;

          case 'o':
          case 'u':
          case 'd':
            if (ch == '-') 
            {
              ++negative;
              d_incwidth();
            } 
            else if (ch == '+') 
            {
                d_incwidth();
            }
            getnum();
            break;

          case 'n':      
            // Char count, don't inc return value
            number = charcount.value;
            if (suppress == 0) 
                assign_num(); // Found in number code above
            break;

          case 'e':
          case 'f':
          case 'g': 
            // Scan a float
            scanptr.assign( fltbuf);

            if (ch == '-') 
            {
              scanptr.etoile( '-');
              scanptr.plusPlus();
              f_incwidth();
            } 
            else if (ch == '+') 
            {
                f_incwidth();
            }

            if (widthset==0 || width > Vsprintf.CVTBUFSIZE) 
                width = Vsprintf.CVTBUFSIZE;

            // Now get integral part
            while (Character.isDigit((char)ch) && width != 0) 
            {
                width--;
              ++started;
              scanptr.etoile( (char)ch);
              scanptr.plusPlus();
              ch = GETCH();
            }
            if (Character.isDigit((char)ch))
                width--;

            // Now check for decimal
            if (ch == DECIMALPOINT)
                if (width != 0) 
                {
                    width--;
                  ch = GETCH();
                  scanptr.etoile( DECIMALPOINT);
                  scanptr.plusPlus();

                  while ( Character.isDigit((char)ch) && width != 0) 
                  {
                      width--;
                    ++started;
                    scanptr.etoile( (char) ch);
                    scanptr.plusPlus();
                    ch = GETCH();
                  }
                  if ( Character.isDigit((char)ch))
                      width--;
                }
                else
                    width--;

            // Now check for exponent
            if (started!=0 && (ch == 'e' || ch == 'E'))
                if ( width != 0)
                {
                    width--;
                      scanptr.etoile('e');
                      scanptr.plusPlus();

                      if ((ch = GETCH()) == '-') 
                      {
                        scanptr.etoile( '-');
                        scanptr.plusPlus();
                        f_incwidth2();
                      } 
                      else if (ch == '+') 
                      {
                        f_incwidth2();
                      }

                      while (Character.isDigit((char)ch) && width != 0) 
                      {
                          width--;
                        ++started;
                        scanptr.etoile( (char)ch);
                        scanptr.plusPlus();
                        ch = GETCH();
                      }
                      if ( Character.isDigit((char)ch))
                          width--;
                } // endif
                else
                    width--;

            UNGETCH(ch);

            if (started != 0)
            {
              if (suppress == 0) 
              {
                double d;

                ++count;
                scanptr.etoile( '\0');

                d = Double.parseDouble(fltbuf.toString());

                if (longone != 0) {
                    if ( pointer instanceof MutableDouble)
                        ((MutableDouble)pointer).value = d;
                    if ( pointer instanceof MutableFloat)
                        ((MutableFloat)pointer).value = (float)d;
                }
                else
                  ((MutableFloat)pointer).value = (float) d;
              }
            }
            else
              error_return();

            break;

          default:
            // Either found '%' or something else
            if ((int) format.etoile() != (int) ch) 
            {
              UNGETCH(ch);
              error_return();
            }
            else
              match--; // % found, compensate for inc below

            if (suppress == 0) 
                arglist = arglistsave;
        }
    }
    
    void getnum() throws InputException {
        if (integer64 != 0) 
        {
          while (done_flag == 0) 
          {
            if (comchr == 'x')
            {
                // Cette fonctionnalité n'est pas utilisée dans CALAO
                throw( new InputException(-1));
                /*
              if (isxdigit(ch)) 
              {
                num64 <<= 4;
                ch = hextodec(ch);
              }
              else
                ++done_flag;
                 */
            }
            else if (Character.isDigit((char)ch))
            {
              if (comchr == 'o')
              {
                if (ch < '8')
                  num64 <<= 3;
                else 
                  ++done_flag;
              }
              else 
              {
                // comchr == 'd'
                num64 = num64 * 10;
              }
            }
            else
              ++done_flag;

            if (done_flag == 0) 
            {
              ++started;
              num64 += ch - '0';

              width--;
              if (widthset!= 0 && width == 0)
                ++done_flag;
              else
                ch = GETCH();
            } 
            else
              UNGETCH(ch);
          }

          if (negative != 0) 
              num64 = -num64;
        }
        else 
        {
          while (done_flag == 0) 
          {
            if (comchr == 'x' || comchr == 'p')
            {
                // Fonctionnalité non utilisée dans CALAO
                throw( new InputException(-1));
                /*
              if (isxdigit(ch)) 
              {
                number = (number << 4);
                ch = hextodec(ch);
              }
              else
                ++done_flag;
                 */
            }
            else if (Character.isDigit((char)ch))
            {
              if (comchr == 'o')
              {
                if (ch < '8')
                  number = (number << 3);
                else 
                  ++done_flag;
              }
              else 
              {
                // comchr == 'd'
                number = number * 10;
              }
            }
            else
              ++done_flag;

            if (done_flag == 0) 
            {
              ++started;
              number += ch - '0';

              width--;
              if (widthset != 0 && width == 0)
                ++done_flag;
              else
                ch = GETCH();
            } 
            else
              UNGETCH(ch);
          }

          if (negative != 0) 
              number = -number;
        }

        if (comchr == 'F') started = 0; // Expected ':' in long pointer

        if (started != 0)
        {
          if (suppress == 0) 
          {
            ++count;
            assign_num();
          }
        }
        else
          error_return();        
    }
    
    void assign_num() {
        if (integer64 != 0)
          ((MutableLong)pointer).value = num64;
        else if (longone == 2) // Y. BOYADJIAN pour portage JAVA
          ((MutableLong)pointer).value = number;
        else
          ((MutableInteger)pointer).value = (int)number;        
    }
    
    void scanit2() throws InputException {
        Uc.memset(table, '\0', RANGESETSIZE);

        if (LEFT_BRACKET == comchr)
        {
          if ( scanptr.etoile() == ']') 
          {
            prevchar = ']';
            scanptr.plusPlus();
            table.setAt(']' >> 3, (char)(1 << (']' & (char)7)));
          }
        }

        while ( scanptr.etoile() != ']') 
        {
          rngch = scanptr.etoile();
          scanptr.plusPlus();

          if (rngch != '-'|| prevchar==0 || scanptr.etoile() == ']') {
              prevchar = rngch;
            table.setAt(prevchar >> 3, (char)(table.charAt(prevchar >> 3) | (1 << (rngch & (char)7))));
          }
          else 
          {  
            // Handle a-z type set
            rngch = scanptr.etoile(); // Get end of range
            scanptr.plusPlus();

            if (prevchar < rngch)
            {
              // %[a-z]
              last = rngch;
            }
            else 
            {
              // %[z-a]
              last = prevchar;
              prevchar = rngch;
            }

            for (rngch = prevchar; rngch <= last; ++rngch) {
                table.setAt(rngch >> 3, (char)(table.charAt(rngch >> 3) | (1 << (rngch & 7))));
            }
            prevchar = 0;
          }
        }


        if ( scanptr.etoile() == '\0') 
            error_return();      // Truncated format string

        // Scanset completed. Now read string
        if (LEFT_BRACKET == comchr) 
            format.assign( scanptr);
        start.assign( (BaseCharPtr)pointer);

        // Execute the format directive. That is, scan input
        // characters until the directive is fulfilled, eof
        // is reached, or a non-matching character is
        // encountered.
        // 
        // It is important not to get the next character
        // unless that character needs to be tested! Other-
        // wise, reads from line-buffered devices (e.g.,
        // scanf()) would require an extra, spurious, newline
        // if the first newline completes the current format
        // directive.

        UNGETCH(ch);

        while ( widthset == 0 || width != 0) 
        {
            width--;
          ch = GETCH();
          // Y. BOYADJIAN : Test si on arrive en fin de chaine car plante sinon.
          if ( ch != -1 && (((table.charAt(ch >> 3) ^ reject) & (1 << (ch & (char)7))) != 0)) 
          {
            if (suppress == 0) 
            {
                ((MutableCharPtr)pointer).etoile((char)ch);
              ((MutableCharPtr)pointer).add(1);
            }
            else 
            {
              // Just indicate a match
              ((MutableCharPtr)start).add(1);
            }
          }
          else  
          {
            UNGETCH(ch);
            break;
          }
        }
        if ( widthset == 0)
            width--;

        // Make sure something has been matched and, if
        // assignment is not suppressed, null-terminate
        // output string if comchr != c

        if ( ! start.equals(pointer) )
        {
          if (suppress == 0) 
          {
            ++count;
            if (comchr != 'c') 
            {
              // Null-terminate strings
              ((MutableCharPtr)pointer).etoile('\0');
            }
          }
        }
        else
          error_return();        
    }
    
    void x_incwidth() throws InputException {
        width--;
      if ( width == 0 && widthset != 0)
        ++done_flag;
      else
        ch = GETCH();        
    }
    
    void d_incwidth() throws InputException {
        width--;
      if ( width == 0 && widthset != 0)
        ++done_flag;
      else
        ch = GETCH();        
    }
    
    void f_incwidth() throws InputException {
      --width;
      ch = GETCH();        
    }
    
    void f_incwidth2 () throws InputException {
        if (width == 0) {
            width--;
          ++width;
        }
        else {
            width--;
          ch = GETCH();        
        }
    }
    
    int error_return() throws InputException {
        int error_code;
        
      if (ch == EOF)
        // If any fields were matched or assigned, return count
        error_code = (count != 0 || match != 0) ? count : EOF;
      else
        error_code = count;
        
        throw( new InputException(error_code));
    }
    
    public static int sscanf( CharPtr buffer, CharPtr fmt, VaList args)
    {
      int rc;
//      va_list args;
      PushbackReader str = new PushbackReader( new CharArrayReader(buffer.buffer, buffer.index, Uc.strlen(buffer)), Uc.strlen(buffer)+1);

//      va_start(args, fmt);

//      str.flag = _IORD | _IOSTR | _IOOWNBUF;
//      str.ptr = str.base = (char *) buffer;
//      str.cnt = Uc.strlen(buffer);
            
      try {
        rc = new Input().input( str, fmt, args);
      } catch( InputException e) {
          return e.error_code;
      }
      return rc;
    }
}
