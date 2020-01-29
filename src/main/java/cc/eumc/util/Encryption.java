package cc.eumc.util;

import cc.eumc.UniBanPlugin;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.apache.commons.lang.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


public class Encryption {
    public static String encrypt(String text, @Nullable Key aesKey) {
        if (aesKey == null) {
            return text;
        }
        try
        {
            // Create key and cipher
            Cipher cipher = Cipher.getInstance("AES");
            // encrypt the text
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encrypted = Base64.getEncoder().encode(cipher.doFinal(text.getBytes()));
            return new String(encrypted);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String text, @Nullable Key aesKey) {
        if (aesKey == null) {
            return text;
        }
        try
        {
            // Create key and cipher
            Cipher cipher = Cipher.getInstance("AES");
            // decrypt the text
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(text.getBytes())));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static @Nullable Key getKeyFromString(String password) {
        if (password.length() == 0) return null;
        else if (password.length() < 16) {
            password = StringUtils.rightPad(password, 16, "0");
        }
        else if (password.length() > 16 && password.length() < 24) {
            password = StringUtils.rightPad(password, 24, "0");
        }
        else if (password.length() < 32) {
            password = StringUtils.rightPad(password, 32, "0");
        }
        else if (password.length() > 32) {
            password = password.substring(0, 31);
        }
        return new SecretKeySpec(password.getBytes(), "AES");
    }

    public static String getMd5(String input)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] messageDigest = md.digest(input.getBytes());

            BigInteger no = new BigInteger(1, messageDigest);

            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}