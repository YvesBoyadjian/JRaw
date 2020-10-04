/*
 * vsprintf.java
 *
 * Created on 14 décembre 2005, 12:13
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
//
// vsprintf.c
//
// Print formatting routines
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

/**
 *
 * @author w22w087
 */
public class Vsprintf {
    
    private static final int ZEROPAD =1,               // Pad with zero
    SIGN =   2,               // Unsigned/signed long
    PLUS =   4,               // Show plus
    SPACE =  8,               // Space if plus
    LEFT =   16,              // Left justified
    SPECIAL = 32,              // 0x
    LARGE =  64;              // Use 'ABCDEF' instead of 'abcdef'

    public static final int CVTBUFSIZE =       (309 + 43);

    
    /** Creates a new instance of vsprintf */
    public Vsprintf() {
    }
    
    private static boolean is_digit( char c) {
        return ((c) >= '0' && (c) <= '9');
    }

    private static final CharPtr digits = new CharPtr("0123456789abcdefghijklmnopqrstuvwxyz");
    private static final CharPtr upper_digits = new CharPtr("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");

    private static long strnlen( MutableCharPtr s, long count) {
        return strnlen( s.toCharPtr(), count);
    }
    
    private static long strnlen( CharPtr s, long count)
    {
      final MutableCharPtr sc = new MutableCharPtr();
      for (sc.assign( s); sc.etoile() != '\0' && count != 0; sc.plusPlus())
          count--;
      return sc.moins(s);
    }

    private static int skip_atoi( MutableCharPtr s)
    {
      int i = 0;
      while (is_digit(s.etoile())) {
          i = i*10 + s.etoile() - '0';
          s.plusPlus();
      }
      return i;
    }

    private static CharPtr number( MutableCharPtr str, long num, int base, int size, int precision, int type) {
        return number( str.toCharPtr(), num, base, size, precision, type);
    }
    private static CharPtr number( CharPtr str_immutable, long num, int base, int size, int precision, int type)
    {
        final MutableCharPtr str = new MutableCharPtr(str_immutable); // YB pour eviter de modifier le pointeur appelant.
        
      char c, sign;
      CharPtr tmp = new CharPtr(66);
      CharPtr dig = digits;
      int i;

      if ((type & LARGE)!= 0)
          dig = upper_digits;
      if ((type & LEFT) != 0) 
          type &= ~ZEROPAD;
      if (base < 2 || base > 36) 
          return null;

      c = (type & ZEROPAD) != 0 ? '0' : ' ';
      sign = 0;
      if ((type & SIGN)!=0)
      {
        if (num < 0)
        {
          sign = '-';
          num = -num;
          size--;
        }
        else if ((type & PLUS)!= 0)
        {
          sign = '+';
          size--;
        }
        else if ((type & SPACE)!=0)
        {
          sign = ' ';
          size--;
        }
      }

      if ((type & SPECIAL)!=0)
      {
        if (base == 16)
          size -= 2;
        else if (base == 8)
          size--;
      }

      i = 0;

      if (num == 0) {
        tmp.setAt( i, '0');
        i++;
      }
      else
      {
        while (num != 0)
        {
          tmp.setAt( i, dig.charAt( (int)(num % base)));
          num = ((long) num) / base;
          i++;
        }
      }

      if (i > precision) precision = i;
      size -= precision;
      if ((type & (ZEROPAD | LEFT)) == 0)
          while (size > 0) {
            size--;
              str.etoile( ' ');
              str.plusPlus();
          }
      if (sign != 0) {
          str.etoile( sign);
          str.plusPlus();
      }

      if ((type & SPECIAL)!=0)
      {
        if (base == 8) {
          str.etoile('0');
          str.plusPlus();
        }
        else if (base == 16)
        {
          str.etoile( '0');
          str.plusPlus();
          str.etoile( digits.charAt(33));
          str.plusPlus();
        }
      }

      if ((type & LEFT) == 0) 
          while (size > 0) {
          size--;
          str.etoile( c);
          str.plusPlus();
          }
      while (i < precision) {
          precision--;
          str.etoile( '0');
          str.plusPlus();
      }
      while (i-- > 0) {
          str.etoile(tmp.charAt(i));
          str.plusPlus();
      }
      while (size-- > 0) {
          str.etoile( ' ');
          str.plusPlus();
      }

      return str.toCharPtr();
    }
/*
    private static CharPtr eaddr(CharPtr str, char[] addr, int size, int precision, int type)
    {
      CharPtr tmp = new CharPtr(24);
      CharPtr dig = digits;
      int i, len;

      if ((type & LARGE)!=0)
          dig = upper_digits;
      len = 0;
      for (i = 0; i < 6; i++)
      {
        if (i != 0) {
            tmp.setAt( len, ':');
            len++;
        }
        tmp.setAt( len, dig.charAt(addr[i] >> 4));
        len++;
        tmp.setAt( len, dig.charAt(addr[i] & 0x0F));
        len++;
      }

      if ((type & LEFT) == 0) 
          while (len < size) {
          size--;
          str.etoile( ' ');
          str.plusPlus();
          }
      for (i = 0; i < len; ++i) {
          str.etoile( tmp.charAt(i));
          str.plusPlus();
      }
      while (len < size--) {
          str.etoile( ' ');
          str.plusPlus();
      }
      return str;
    }

    private static CharPtr iaddr( CharPtr str, char[] addr, int size, int precision, int type)
    {
      CharPtr tmp = new CharPtr(24);
      int i, n, len;

      len = 0;
      for (i = 0; i < 4; i++)
      {
        if (i != 0) {
            tmp.setAt( len, '.');
            len++;
        }
        n = addr[i];

        if (n == 0) {
          tmp.setAt( len, digits.charAt(0));
          len++;
        }
        else
        {
          if (n >= 100) 
          {
            tmp.setAt( len, digits.charAt(n / 100));
            len++;
            n = n % 100;
            tmp.setAt( len, digits.charAt(n / 10));
            len++;
            n = n % 10;
          }
          else if (n >= 10) 
          {
            tmp.setAt( len, digits.charAt(n / 10));
            len++;
            n = n % 10;
          }

          tmp.setAt( len, digits.charAt(n));
          len++;
        }
      }

      if ((type & LEFT) == 0) 
          while (len < size) {
            size--;
            str.etoile( ' ');
            str.plusPlus();
          }
      for (i = 0; i < len; ++i) {
          str.etoile( tmp.charAt(i));
          str.plusPlus();
      }
      while (len < size) {
          size--;
          str.etoile( ' ');
          str.plusPlus();
      }

      return str;
    }
*/
    private static double modf( double arg, MutableDouble iarg) {
        if ( arg == 0) {
            iarg.value = 0;
            return 0;
        }
        double sign = arg/Math.abs(arg);
        double abs = Math.abs(arg);
        iarg.value = Math.floor( abs) * sign;
        return (abs - iarg.value) * sign;
    }
    
    private static CharPtr cvt(double arg, int ndigits, MutableInteger decpt, MutableInteger sign, CharPtr buf, int eflag)
    {
      int r2;
      final MutableDouble fi = new MutableDouble(), fj = new MutableDouble();
      final MutableCharPtr p = new MutableCharPtr();
      final MutableCharPtr p1 = new MutableCharPtr();

      if (ndigits < 0) ndigits = 0;
      if (ndigits >= CVTBUFSIZE - 1) 
          ndigits = CVTBUFSIZE - 2;
      r2 = 0;
      sign.value = 0;
      p.assign( buf);
      if (arg < 0)
      {
        sign.value = 1;
        arg = -arg;
      }
      arg = modf(arg, fi);
      p1.assign( buf.plus(CVTBUFSIZE));

      if (fi.value != 0) 
      {
        p1.assign( buf.plus(CVTBUFSIZE));
        while (fi.value != 0) 
        {
          fj.value = modf(fi.value / 10, fi);
          p1.moinsMoins();
          p1.etoile( (char)(((fj.value + .03) * 10) + '0'));
          r2++;
        }
        while (p1.toInt() < buf.plus(CVTBUFSIZE).toInt()) {
            p.etoile( p1.etoile());
            p.plusPlus();
            p1.plusPlus();
        }
      } 
      else if (arg > 0)
      {
        while ((fj.value = arg * 10) < 1) 
        {
          arg = fj.value;
          r2--;
        }
      }
      p1.assign( buf.plus(ndigits));
      if (eflag == 0) 
          p1.add(r2);
      decpt.value = r2;
      if (p1.toInt() < buf.toInt()) 
      {
        buf.setAt(0, '\0');
        return buf;
      }
      while (p.toInt() <= p1.toInt() && p.toInt() < buf.plus(CVTBUFSIZE).toInt())
      {
        arg *= 10;
        arg = modf(arg, fj);
        p.etoile( (char) (fj.value + '0'));
        p.plusPlus();
      }
      if (p1.toInt() >= buf.plus(CVTBUFSIZE).toInt())
      {
        buf.setAt(CVTBUFSIZE - 1, '\0');
        return buf;
      }
      p.assign( p1);
      p1.etoile((char)(p1.etoile() + 5));
      while ( p1.etoile() > '9') 
      {
        p1.etoile( '0');
        if (p1.toInt() > buf.toInt()) {
            p1.moinsMoins();
            p1.etoile( (char)(p1.etoile()+1));
        }
        else 
        {
          p1.etoile( '1');
          decpt.value++;
          if (eflag == 0) 
          {
            if (p.toInt() > buf.toInt())
                p.etoile( '0');
            p.plusPlus();
          }
        }
      }
      p.etoile( '\0');
      return buf;
    }


    private static CharPtr ecvtbuf(double arg, int ndigits, MutableInteger decpt, MutableInteger sign, CharPtr buf) {
        return cvt(arg, ndigits, decpt, sign, buf, 1);
    }
    private static CharPtr fcvtbuf(double arg, int ndigits, MutableInteger decpt, MutableInteger sign, CharPtr buf) {
       return cvt(arg, ndigits, decpt, sign, buf, 0);
   }

    private static void cfltcvt(double value, CharPtr buffer_immutable, char fmt, int precision)
    {
      final MutableCharPtr buffer = new MutableCharPtr( buffer_immutable);
        
      final MutableInteger decpt = new MutableInteger();
      final MutableInteger sign = new MutableInteger();
      int exp, pos;
      final MutableCharPtr digits = new MutableCharPtr();
      CharPtr cvtbuf = new CharPtr(CVTBUFSIZE);
      int capexp = 0;
      int magnitude;

      if (fmt == 'G' || fmt == 'E')
      {
        capexp = 1;
        fmt += 'a' - 'A';
      }

      if (fmt == 'g')
      {
        digits.assign( ecvtbuf(value, precision, decpt, sign, cvtbuf));
        magnitude = decpt.value - 1;
        if (magnitude < -4  ||  magnitude > precision - 1)
        {
          fmt = 'e';
          precision -= 1;
        }
        else
        {
          fmt = 'f';
          precision -= decpt.value;
        }
      }

      if (fmt == 'e')
      {
        digits.assign( ecvtbuf(value, precision + 1, decpt, sign, cvtbuf));

        if (sign.value != 0) {
            buffer.etoile( '-');
            buffer.plusPlus();
        }
        buffer.etoile( digits.etoile());
        buffer.plusPlus();
        if (precision > 0) {
            buffer.etoile( '.');
            buffer.plusPlus();
        }
        memcpy(buffer, digits.plus(1), precision);
        buffer.add(precision);
        buffer.etoile( capexp != 0 ? 'E' : 'e');
        buffer.plusPlus();

        if (decpt.value == 0)
        {
          if (value == 0.0)
            exp = 0;
          else
            exp = -1;
        }
        else
          exp = decpt.value - 1;

        if (exp < 0)
        {
          buffer.etoile( '-');
          buffer.plusPlus();
          exp = -exp;
        }
        else {
          buffer.etoile( '+');
          buffer.plusPlus();
        }
        buffer.setAt( 2, (char)((exp % 10) + '0'));
        exp = exp / 10;
        buffer.setAt( 1, (char)((exp % 10) + '0'));
        exp = exp / 10;
        buffer.setAt( 0, (char)((exp % 10) + '0'));
        buffer.add(3);
      }
      else if (fmt == 'f')
      {
        digits.assign( fcvtbuf(value, precision, decpt, sign, cvtbuf));
        if (sign.value != 0) {
            buffer.etoile( '-');
            buffer.plusPlus();
        }
        if (digits.etoile() != 0)
        {
          if (decpt.value <= 0)
          {
            buffer.etoile( '0');
            buffer.plusPlus();
            buffer.etoile('.');
            buffer.plusPlus();
            for (pos = 0; pos < -decpt.value; pos++) {
                buffer.etoile( '0');
                buffer.plusPlus();
            }
            while (digits.etoile() != 0) {
                buffer.etoile( digits.etoile());
                buffer.plusPlus();
                digits.plusPlus();
            }
          }
          else
          {
            pos = 0;
            while (digits.etoile() != 0)
            {
              if (pos == decpt.value) {
                  buffer.etoile('.');
                  buffer.plusPlus();
              }
              pos++;
              buffer.etoile( digits.etoile());
              buffer.plusPlus();
              digits.plusPlus();
            }
          }
        }
        else
        {
          buffer.etoile( '0');
          buffer.plusPlus();
          if (precision > 0)
          {
            buffer.etoile( '.');
            buffer.plusPlus();
            for (pos = 0; pos < precision; pos++) {
                buffer.etoile( '0');
                buffer.plusPlus();
            }
          }
        }
      }

      buffer.etoile( '\0');
    }

    private static void forcdecpt( CharPtr buffer_immutable)
    {
        final MutableCharPtr buffer = new MutableCharPtr( buffer_immutable);
      while (buffer.etoile() != 0)
      {
        if (buffer.etoile() == '.') return;
        if (buffer.etoile() == 'e' || buffer.etoile() == 'E') break;
        buffer.plusPlus();
      }

      if (buffer.etoile() != 0)
      {
        int n = Uc.strlen(buffer);
        while (n > 0) 
        {
          buffer.setAt( n + 1, buffer.charAt(n));
          n--;
        }

        buffer.etoile( '.');
      }
      else
      {
        buffer.etoile('.');
        buffer.plusPlus();
        buffer.etoile('\0');
      }
    }

    private static void cropzeros( CharPtr buffer_immutable)
    {
        final MutableCharPtr buffer = new MutableCharPtr(buffer_immutable);
      final MutableCharPtr stop = new MutableCharPtr();

      while (buffer.etoile()!= 0 && buffer.etoile() != '.') 
          buffer.plusPlus();
      if (buffer.etoile() != 0)
      {
          buffer.plusPlus();
        while (buffer.etoile() != 0 && buffer.etoile() != 'e' && buffer.etoile() != 'E') 
            buffer.plusPlus();
        stop.assign( buffer);
        buffer.moinsMoins();
        while ( buffer.etoile() == '0') 
            buffer.moinsMoins();
        if (buffer.etoile() == '.') 
            buffer.moinsMoins();
        buffer.plusPlus();
        while (buffer.etoile(stop.etoile()) != 0){
            stop.plusPlus();
            buffer.plusPlus();
        }
      }
      else
          buffer.plusPlus();
    }

    private static CharPtr flt( MutableCharPtr str, double num, int size, int precision, char fmt, int flags) {
        return flt( str.toCharPtr(), num, size, precision, fmt, flags);
    }
    private static CharPtr flt( CharPtr str_immutable, double num, int size, int precision, char fmt, int flags)
    {
        final MutableCharPtr str = new MutableCharPtr( str_immutable);
      CharPtr tmp = new CharPtr(80);
      char c, sign;
      int n, i;
      
      if ( Double.isNaN(num)) { // Y. BOYADJIAN: Cas non prévu dans SANOS
          str.set("NaN");
          str.add(3);
          return str.toCharPtr();
      }

      // Left align means no zero padding
      if ((flags & LEFT) != 0) 
          flags &= ~ZEROPAD;

      // Determine padding and sign char
      c = (flags & ZEROPAD) != 0 ? '0' : ' ';
      sign = 0;
      if ((flags & SIGN)!= 0)
      {
        if (num < 0.0)
        {
          sign = '-';
          num = -num;
          size--;
        }
        else if ((flags & PLUS) != 0)
        {
          sign = '+';
          size--;
        }
        else if ((flags & SPACE) != 0)
        {
          sign = ' ';
          size--;
        }
      }

      // Compute the precision value
      if (precision < 0)
        precision = 6; // Default precision: 6
      else if (precision == 0 && fmt == 'g')
        precision = 1; // ANSI specified

      // Convert floating point number to text
      cfltcvt(num, tmp, fmt, precision);

      // '#' and precision == 0 means force a decimal point
      if ((flags & SPECIAL)!= 0 && precision == 0) 
          forcdecpt(tmp);

      // 'g' format means crop zero unless '#' given
      if (fmt == 'g' && (flags & SPECIAL) == 0) cropzeros(tmp);

      n = Uc.strlen(tmp);

      // Output number with alignment and padding
      size -= n;
      if ((flags & (ZEROPAD | LEFT)) == 0) 
          while (size > 0) {
            size--;
              str.etoile( ' ');
              str.plusPlus();
          }
      if (sign != 0){
          str.etoile( sign);
          str.plusPlus();
      }
      if ((flags & LEFT) == 0) 
          while (size > 0) {
            size--;
              str.etoile( c);
              str.plusPlus();
          }
      for (i = 0; i < n; i++) {
          str.etoile( tmp.charAt(i));
          str.plusPlus();
      }
      while (size > 0) {
          size--;
          str.etoile( ' ');
          str.plusPlus();
      }

      return str.toCharPtr();
    }

    private static int vsprintf( CharPtr buf, CharPtr fmt_immutable, VaList args)
    {
        final MutableCharPtr fmt = new MutableCharPtr(fmt_immutable);
      int len;
      long num;
      int i, base;
      final MutableCharPtr str = new MutableCharPtr();
      final MutableCharPtr s = new MutableCharPtr();

      int flags;            // Flags to number()

      int field_width;      // Width of output field
      int precision;        // Min. # of digits for integers; max number of chars for from string
      int qualifier;        // 'h', 'l', or 'L' for integer fields

      for (str.assign( buf); fmt.etoile() != 0; fmt.plusPlus())
      {
        if (fmt.etoile() != '%')
        {
          str.etoile( fmt.etoile());
          str.plusPlus();
          continue;
        }

        // Process flags
        flags = 0;
//    repeat:
        boolean repeat_flag = false;
        do {
            repeat_flag = false;
            fmt.plusPlus(); // This also skips first '%'
            switch ( fmt.etoile())
            {
              case '-': 
                  flags |= LEFT; 
                  repeat_flag = true;
                  break;
              case '+': flags |= PLUS;
                  repeat_flag = true;
                  break;
              case ' ': flags |= SPACE;
                  repeat_flag = true;
                  break;
              case '#': flags |= SPECIAL;
                  repeat_flag = true;
                  break;
              case '0': flags |= ZEROPAD;
                  repeat_flag = true;
                  break;
            }
        } while( repeat_flag);

        // Get field width
        field_width = -1;
        if (is_digit(fmt.etoile()))
          field_width = skip_atoi(fmt);
        else if ( fmt.etoile() == '*')
        {
          fmt.plusPlus();
          field_width = VaList.va_arg(args, VaList.INT);
          if (field_width < 0)
          {
            field_width = -field_width;
            flags |= LEFT;
          }
        }

        // Get the precision
        precision = -1;
        if ( fmt.etoile() == '.')
        {
          fmt.plusPlus();
          if (is_digit( fmt.etoile()))
            precision = skip_atoi( fmt);
          else if ( fmt.etoile() == '*')
          {
            fmt.plusPlus();
            precision = VaList.va_arg(args , VaList.INT);
          }
          if (precision < 0) 
              precision = 0;
        }

        // Get the conversion qualifier
        qualifier = -1;
        if (fmt.etoile() == 'h' || fmt.etoile() == 'l' || fmt.etoile() == 'L')
        {
          qualifier = fmt.etoile();
          fmt.plusPlus();
        }

        // Default base
        base = 10;

        switch ( fmt.etoile())
        {
          case 'c':
            if ((flags & LEFT) == 0) 
                while (--field_width > 0) {
                str.etoile( ' ');
                str.plusPlus();
            }
            str.etoile( (char) VaList.va_arg(args, VaList.INT));
            str.plusPlus();
            field_width--;
            while (field_width > 0) {
                str.etoile( ' ');
                str.plusPlus();
                field_width--;
            }
            continue;

          case 's':
            s.assign( VaList.va_arg(args, VaList.STRING));
            if (s.isNull()) 
                s.assign( new CharPtr("<null>"));
            len = (int)strnlen(s, precision);
            if ((flags & LEFT) == 0) 
                while (len < field_width) {
                    field_width--;
                    str.etoile( ' ');
                    str.plusPlus();
                }
            for (i = 0; i < len; ++i) {
                str.etoile( s.etoile());
                str.plusPlus();
                s.plusPlus();
            }
            while (len < field_width) {
                field_width--;
                str.etoile( ' ');
                str.plusPlus();
            }
            continue;
/* Les pointeurs n'existent pas en Java
          case 'p':
            if (field_width == -1)
            {
              field_width = 2 * sizeof(void *);
              flags |= ZEROPAD;
            }
            str = number(str, (unsigned long) VaList.va_arg(args, void *), 16, field_width, precision, flags);
            continue;
*/
/* Fonction non nécessaire pour ce portage
          case 'n':
            if (qualifier == 'l')
            {
              long *ip = VaList.va_arg(args, long *);
              *ip = (str - buf);
            }
            else
            {
              int *ip = VaList.va_arg(args, int *);
              *ip = (str - buf);
            }
            continue;
*/
          case 'A':
            flags |= LARGE;
// Fonction non nécessaire pour ce portage
//          case 'a':
//            if (qualifier == 'l')
//              str = eaddr(str, VaList.va_arg(args /*, unsigned char **/), field_width, precision, flags);
//            else
//              str = iaddr(str, VaList.va_arg(args/*, unsigned char **/), field_width, precision, flags);
//            continue;
//
          // Integer number formats - set up the flags and "break"
          case 'o':
            base = 8;
            break;

          case 'X':
            flags |= LARGE;

          case 'x':
            base = 16;
            break;

          case 'd':
          case 'i':
            flags |= SIGN;

          case 'u':
            break;

          case 'E':
          case 'G':
          case 'e':
          case 'f':
          case 'g':
            str.assign( flt(str, VaList.va_arg(args, VaList.DOUBLE), field_width, precision, fmt.etoile(), flags | SIGN));
            continue;

          default:
            if ( fmt.etoile() != '%'){
                str.etoile( '%');
                str.plusPlus();
            }
            if (fmt.etoile() != 0) {
              str.etoile( fmt.etoile());
              str.plusPlus();
            }
            else
              fmt.moinsMoins();
            continue;
        }

        if (qualifier == 'l')
          num = VaList.va_arg(args, VaList.LONG);
        else if (qualifier == 'h')
        {
          if ((flags & SIGN) !=0)
            num = VaList.va_arg(args, VaList.SHORT);
          else
            num = VaList.va_arg(args, VaList.SHORT);
        }
        else if ((flags & SIGN)!=0)
          num = VaList.va_arg(args, VaList.INT);
        else
          num = VaList.va_arg(args, VaList.INT);

        str.assign( number(str, num, base, field_width, precision, flags));
      }

      str.etoile( '\0');
      return str.moins( buf);
    }

    public static int sprintf( CharPtr buf, String fmt, VaList args)
    {
      int n;

//      va_start(args, fmt);
      n = vsprintf(buf, new CharPtr(fmt), args);
//      va_end(args);

      return n;
    }
    
    private static CharPtr memcpy( MutableCharPtr dst, CharPtr src, int n) {
        return memcpy( dst.toCharPtr(), src, n);
    }
    private static CharPtr memcpy( CharPtr dst, CharPtr src, int n) {
        CharPtr ret = dst.copy();
        
        while( n != 0) {
            n--;
            dst.etoile(src.etoile());
            dst = dst.plus(1);
            src = src.plus(1);
        }
        return ret;
    }
    
}
