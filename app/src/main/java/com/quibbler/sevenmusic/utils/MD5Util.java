package com.quibbler.sevenmusic.utils;

import android.text.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class MD5Util {

    private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    public static String encodeStr2MD5(String s) {
        if (TextUtils.isEmpty(s)) {
            return null;
        }

        try {
            // 使用MD5创建MessageDigest对象
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");

            digest.update(s.getBytes());

            byte messageDigest[] = digest.digest();

            return toHexString(messageDigest);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return s;
    }

    private static String toHexString(byte[] b) {

        StringBuilder sb = new StringBuilder(b.length * 2);

        for (int i = 0; i < b.length; i++) {

            sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }

        return sb.toString();
    }
}