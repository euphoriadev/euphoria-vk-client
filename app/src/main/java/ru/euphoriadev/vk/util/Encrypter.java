package ru.euphoriadev.vk.util;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Igor on 08.11.15.
 */
public class Encrypter {
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    /**
     * MD5 (англ. Message Digest 5) — 128-битный алгоритм хеширования
     * Он построен таким образом, что расшифровать его практически невозможно
     * Если только использовать брутфорс
     *
     * @param s Строка, которую необходимо закодировать
     */
    public static String encodeMD5(String s) {
        MessageDigest md = null;
        byte[] thedigest;
        try {
            md = MessageDigest.getInstance("MD5");
            thedigest = md.digest(s.getBytes());

            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < thedigest.length; ++i) {
                buffer.append(Integer.toHexString((thedigest[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return buffer.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Вревращение строрки в HEX алфавит
     * Шестнадцатеричная система счисления (шестнадцатеричные числа) — позиционная система счисления по целочисленному основанию 16.
     * Алфавит этой системы счисления - (0, 1, 2, 3, 4, 5, 6, 7, 8, 9, A, B, C, D, E, F).
     * Иными словами, используются цифры от 0 до 9 и латинские буквы от A до F для обозначения цифр от 1010 до 1510.
     *
     * @param bytes
     * @return
     */
    public static String encodeHEX(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v>>>4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Обратный процесс, расшифровывание HEX строки в String
     *
     * @param hex
     * @return
     */
    public static String decodeHEX(String hex) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(hex.length() / 2);
            for (int i = 0; i < hex.length(); i += 2) {
                buffer.put((byte) Integer.parseInt(hex.substring(i, i + 2), 16));
            }
            buffer.rewind();
            Charset сharset = Charset.forName("UTF-8");                              // BBB
            CharBuffer cBuffer = сharset.decode(buffer);                              // BBB
            System.out.println(cBuffer.toString());

            return cBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * Зашифровывание строки, путем превращение каждого символа в байт
     * (01100101 01101100 01101100 01101111 00100000 01110111 01101111 01110010)
     *
     * @param s строка, которую необходимо закодировать
     * @return
     */
    public static String encodeBinary(String s) {
        StringBuilder binary = new StringBuilder();;
        try {
            byte[] bytes = s.getBytes("UTF-8");
            for (byte b : bytes) {
                int val = b;
                for (int i = 0; i < 8; i++) {
                    binary.append((val & 128) == 0 ? 0 : 1);
                    val <<= 1;
                }
                binary.append(' ');
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return binary.toString();
    }

    /**
     * Раскодирование байтов в строку
     *
     * @param binary последовательность байтов (01100101 01101100 01101100)
     * @return
     */
    public static String decodeBinary(String binary) {
        String binarys[] = binary.split(" ");
        StringBuilder builder = new StringBuilder();

        for (String b : binarys) {
            builder.append((char) Integer.parseInt(b, 2));
        }
        return builder.toString();
    }

    /**
     * Зашифрование текста алгоритмом Base64
     *
     * Base64 буквально означает — позиционная система счисления с основанием 64.
     * Здесь 64 — это число символов в алфавите кодирования,
     * из которого формируется конечный буквенно-цифровой текст на основе латинского алфавита.
     * Используют символы A-Z, a-z и 0-9, что составляет 62 знака
     *
     * @param text текст, который необходимо закодировать
     * @return
     */
    public static String encodeBase64(String text) {
        return encodeBase64(text.getBytes(Charset.forName("UTF-8")));
    }

    public static String encodeBase64(byte[] test) {
        return new String(Base64.encode(test, Base64.DEFAULT), Charset.forName("UTF-8"));
    }

    /**
     * Раскодирование строки из Base64
     *
     * @param text
     * @return
     */
    public static String decodeBase64(String text) {
       return new String(Base64.decode(text, Base64.DEFAULT));
    }

    public static String decodeBase64(byte[] text) {
        return new String(Base64.decode(text, Base64.DEFAULT));
    }

    /**
     * Triple DES (3DES англ. Data Encryption Standard) — симметричный блочный шифр,
     * созданный Уитфилдом Диффи, Мартином Хеллманом и Уолтом Тачманном в 1978 году на основе алгоритма DES,
     * с целью устранения главного недостатка последнего — малой длины ключа (56 бит),
     * который может быть взломан методом полного перебора ключа.
     *
     * @param message
     * @return
     */
    public static byte[] encodeDES3(String message) {
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            final byte[] digestOfPassword = md.digest("release".getBytes("UTF-8"));
            final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
            for (int j = 0, k = 16; j < 8;) {
                keyBytes[k++] = keyBytes[j++];
            }

            final SecretKey key = new SecretKeySpec(keyBytes, "DESede");
            final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
            final Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);

            final byte[] plainTextBytes = message.getBytes("utf-8");
            final byte[] cipherText = cipher.doFinal(plainTextBytes);

            // final String encodedCipherText = new sun.misc.BASE64Encoder()
            // .encode(cipherText);


            return Base64.encode(cipherText, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decodeDES3(byte[] message) {
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            final byte[] digestOfPassword = md.digest("release".getBytes("UTF-8"));
            final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
            for (int j = 0, k = 16; j < 8;) {
                keyBytes[k++] = keyBytes[j++];
            }

            final SecretKey key = new SecretKeySpec(keyBytes, "DESede");
            final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
            final Cipher decipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            decipher.init(Cipher.DECRYPT_MODE, key, iv);

            // final byte[] encData = new
            // sun.misc.BASE64Decoder().decodeBuffer(message);
            final byte[] plainText = decipher.doFinal(Base64.decode(message, Base64.NO_WRAP));

            return new String(plainText, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


    /**
     * Получение стандратного HashCode строки
     * @param text
     * @return
     */
    public static int encodeHashCode(String text) {
        return text.hashCode();
    }


}
