//  Copyright (C) 2005 - 2010  Markus Fischer, Pascal Steiner
//
//  This program is free software; you can redistribute it and/or
//  modify it under the terms of the GNU General Public License
//  as published by the Free Software Foundation; version 2 of the License.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
//
//  Contact: info@doctor-doc.com

package util;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.StringTokenizer;


public class Decoder {

    /**
     * Decodes a URL encoded string. Also decodes html entities.
     * NB: java.net.URLDecoder can only handle (encoded) ASCII strings.
     * @param input the <code>String</code> to decode
     * @return the newly decoded <code>String</code>
     */
    public String urlDecode(final String input, final String charset) throws UnsupportedEncodingException {
        final StringBuffer buffer = new StringBuffer();
        final int max = input.length();
        for (int i = 0; i < max; i++) {
            final char c = input.charAt(i);
            switch (c) {
            case '+':
                buffer.append(' ');
                break;
            case '%':
                try {
                    buffer.append((char) Integer.parseInt(input.substring(i + 1, i + 3), 16));
                    i += 2;
                } catch (final Throwable t) {
                    buffer.append(c);
                }
                break;
            default:
                buffer.append(c);
                break;
            }
        }
        // Undo conversion to external encoding
        String result = buffer.toString();
        if (charset != null) {
            result = new String(result.getBytes("ISO-8859-1"), charset);
        }

        // handle html entities in urls (allows 16 bit input from 8 bit forms)
        return htmlEntityDecode(result);

    }

    /**
     * Decodes html entities. (Nur Zeichen der Art &#33;)
     * @param input the <code>String</code> to decode
     * @return the newly decoded <code>String</code>
     */
    public String htmlEntityDecode(final String input) {

        int i = 0, j = 0, pos = 0;
        final StringBuffer sb = new StringBuffer();
        while ((i = input.indexOf("&#", pos)) != -1 && (j = input.indexOf(';', i)) != -1) {
            int n = -1;
            for (i += 2; i < j; ++i) {
                final char c = input.charAt(i);
                if ('0' <= c && c <= '9') {
                    n = (n == -1 ? 0 : n * 10) + c - '0';
                } else {
                    break;
                }
            }
            if (i != j)  { n = -1; } // malformed entity - abort
            if (n != -1) {
                sb.append((char) n);
                i = j + 1;      // skip ';'
            } else {
                for (int k = pos; k < i; ++k) { sb.append(input.charAt(k)); }
            }
            pos = i;
        }
        if (sb.length() == 0) {
            return input;
        } else {
            sb.append(input.substring(pos, input.length()));
        }
        return sb.toString();

    }


    public static void decodeQueryString(final String qs, final String charset, final Map<String, String> m)
    throws UnsupportedEncodingException {

        if (qs == null) {
            return;
        }

        final StringTokenizer st = new StringTokenizer(qs, "&");
        final Decoder decode = new Decoder();
        while (st.hasMoreTokens()) {
            final String param = st.nextToken();
            final StringTokenizer pst = new StringTokenizer(param, "=");
            final String name = decode.urlDecode(pst.nextToken(), charset);
            String val = "";
            if (pst.hasMoreTokens()) {
                val = decode.urlDecode(pst.nextToken(), charset);
            }
            m.put(name, val);
        }
    }

    public static final String utf8Convert(final String utf8String)
    throws java.io.UnsupportedEncodingException {
        final byte[] bytes = new byte[utf8String.length()];
        final int max = utf8String.length();
        for (int i = 0; i < max; i++) {
            bytes[i] = (byte) utf8String.charAt(i);
        }
        return new String(bytes, "UTF-8");
    }

}

