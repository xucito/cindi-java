package com.cindi.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class  SecurityUtility {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    static SecureRandom rnd = new SecureRandom();

    static ObjectMapper mapper = new ObjectMapper();

    public static String GenerateRandomString(int len){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( ALPHA_NUMERIC_STRING.charAt( rnd.nextInt(ALPHA_NUMERIC_STRING.length()) ) );
        return sb.toString();
    }


    public static String GetPublicString(PublicKey key) throws IOException
    {
        PublicKey publicKey = key;
        StringWriter writer = new StringWriter();
        PemWriter pemWriter = new PemWriter(writer);
        pemWriter.writeObject(new PemObject("PUBLIC KEY", publicKey.getEncoded()));
        pemWriter.flush();
        pemWriter.close();
        return writer.toString();
    }

    public static String GetPrivateString(PrivateKey key) throws IOException
    {
        PrivateKey privateKey = key;
        StringWriter writer = new StringWriter();
        PemWriter pemWriter = new PemWriter(writer);
        pemWriter.writeObject(new PemObject("Private KEY", privateKey.getEncoded()));
        pemWriter.flush();
        pemWriter.close();
        return writer.toString();
    }

    public static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
        generator.initialize(2048);

        KeyPair keyPair = generator.generateKeyPair();
        return keyPair;
    }

    //https://gist.github.com/nasznjoka/ca215266baecb7d2c078c5cad6e17322
    public static String encryptAES(String keyString, String plaintext) throws Exception {
        byte[] keyValue = keyString.getBytes("UTF-8");
        Key key = new SecretKeySpec(keyValue, "AES");
        //serialize
        String serializedPlaintext = plaintext;
        byte[] plaintextBytes = serializedPlaintext.getBytes("UTF-8");

        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] iv = c.getIV();
        byte[] encVal = c.doFinal(plaintextBytes);
        String encryptedData = Base64.toBase64String(encVal);

        SecretKeySpec macKey = new SecretKeySpec(keyValue, "HmacSHA256");
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        hmacSha256.init(macKey);
        hmacSha256.update(Base64.encode(iv));
        byte[] calcMac = hmacSha256.doFinal(encryptedData.getBytes("UTF-8"));
        String mac = new String(Hex.encodeHex(calcMac));
        //Log.d("MAC",mac);

        AesEncryptionData aesData = new AesEncryptionData(
                Base64.toBase64String(iv),
                encryptedData,
                mac);

        String aesDataJson = mapper.writeValueAsString(aesData);

        return Base64.toBase64String(aesDataJson.getBytes("UTF-8"));
    }


    public static String decryptRSA(String data, Key key) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        Cipher cipher1 = Cipher.getInstance("RSA");
        cipher1.init(Cipher.DECRYPT_MODE, key);
        byte[] decStr = cipher1.doFinal(java.util.Base64.getDecoder().decode(data));
        return new String(decStr);
    }

    public static String encryptRSA(String data, Key key) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher encryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        encryptCipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] cipherText = encryptCipher.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        return Base64.toBase64String(cipherText);
    }

    public static String decryptAES(String keyString, String encodedData) throws Exception {
        byte[] keyValue = keyString.getBytes("UTF-8");
        byte[] base64DecodedString = Base64.decode(encodedData);
        String UtfDecodedString = new String(base64DecodedString, StandardCharsets.UTF_8);
        AesEncryptionData data = mapper.readValue(UtfDecodedString, AesEncryptionData.class);
        String ivValue = data.iv;
        String encryptedData = data.value;
        String macValue = data.mac;
        Key key = new SecretKeySpec(keyValue, "AES");
        byte[] iv = Base64.decode(ivValue.getBytes("UTF-8"));
        byte[] decodedValue = Base64.decode(encryptedData.getBytes("UTF-8"));

        SecretKeySpec macKey = new SecretKeySpec(keyValue, "HmacSHA256");
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        hmacSha256.init(macKey);
        hmacSha256.update(ivValue.getBytes("UTF-8"));
        byte[] calcMac = hmacSha256.doFinal(encryptedData.getBytes("UTF-8"));
        byte[] mac = Hex.decodeHex(macValue.toCharArray());
        if (!Arrays.equals(calcMac, mac))
            return "MAC mismatch";

        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding"); // or PKCS5Padding
        c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] decValue = c.doFinal(decodedValue);

        int firstQuoteIndex = 0;
        String test = new String(Arrays.copyOfRange(decValue, 0, decValue.length));
        while (firstQuoteIndex < decValue.length && decValue[firstQuoteIndex] != (byte) '"') firstQuoteIndex++;
        return new String(Arrays.copyOfRange(decValue, 0, decValue.length));
    }
}

