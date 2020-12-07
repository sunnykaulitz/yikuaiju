package com.example.yikuaiju.util;



import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Algorithm {

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};


    public static String useSHA1(String str) {
        if (str == null) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            messageDigest.update(str.getBytes("utf-8"));
            String result = getFormattedText(messageDigest.digest());
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getFormattedText(byte[] bytes) {
        int len = bytes.length;
        StringBuilder buf = new StringBuilder(len * 2);
        // 把密文转换成十六进制的字符串形式
        for (int j = 0; j < len; j++) {
            buf.append(HEX_DIGITS[(bytes[j] >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[bytes[j] & 0x0f]);
        }
        return buf.toString();
    }

    /**
     * MD5加密
     * @param text
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static String MD5(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return MD5.MD5Encode(text);
    }

}
