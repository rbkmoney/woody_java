package com.rbkmoney.woody.thrift.impl.http.util;

import java.nio.ByteBuffer;

/**
 * Created by tolkonepiu on 14.06.16.
 */
public class Base62Utils {

    private static final char[] CODES =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private static final char CODE_FLAG = '9';

    private static void append(int b, StringBuilder out) {
        if (b < 61) {
            out.append(CODES[b]);
        } else {
            out.append(CODE_FLAG);
            out.append(CODES[b - 61]);
        }
    }

    public static String base62Encode(long value) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(value);
        return base62Encode(buffer.array());
    }

    public static String base62Encode(byte[] in) {

        StringBuilder out = new StringBuilder();

        int b;

        for (int i = 0; i < in.length; i += 3) {
            b = (in[i] & 0xFC) >> 2;
            append(b, out);

            b = (in[i] & 0x03) << 4;
            if (i + 1 < in.length) {
                b |= (in[i + 1] & 0xF0) >> 4;
                append(b, out);

                b = (in[i + 1] & 0x0F) << 2;
                if (i + 2 < in.length) {
                    b |= (in[i + 2] & 0xC0) >> 6;
                    append(b, out);

                    b = in[i + 2] & 0x3F;
                    append(b, out);
                } else {
                    append(b, out);
                }
            } else {
                append(b, out);
            }
        }
        return out.toString();
    }

}
