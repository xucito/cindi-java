package test.java.cindi.java;

import com.cindi.utilities.SecurityUtility;
import org.junit.Assert;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SecurityTest {
    @Test
    public void testSomeLibraryMethod() throws Exception {
        String testString = "THIS IS A TEST STRING";
        String key = "65AA8CD46274E4BC1B9958FE47FA3E50";
        String encryptedString = SecurityUtility.encryptAES(key, testString);
        cindi.java.Library classUnderTest = new cindi.java.Library();
        assertTrue("someLibraryMethod should return 'true'", classUnderTest.someLibraryMethod());
        assertEquals(testString, SecurityUtility.decryptAES(key,encryptedString), testString );
    }

    @Test
    public void decryptString() throws Exception {
        assertEquals("THIS IS A TEST STRING", SecurityUtility.decryptAES("65AA8CD46274E4BC1B9958FE47FA3E50","eyJpdiI6IjQzK0xnZGFZMUhLME5uR29obnIybkE9PSIsInZhbHVlIjoiRW9qaVpDV0M1MTJpSmlxWFF5ZU1rZWhLRUwzemFxQ0I3VjI5ZGNwZzdGOD0iLCJtYWMiOiJkMzlhZjgxOTdiMjdkMWYyZTYyNzQwOTM3Y2NmNTFlNTJiYTk1NWZmNmU1MDFkMmY3NGFmNGQ3NGU3MjZmNDFiIn0="));
    }

    @Test
    public void EncryptRSA() throws NoSuchProviderException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, BadPaddingException {
        KeyPair keyPair = SecurityUtility.generateRSAKeyPair();
        String testString = "THIS IS A TEST STRING";
        String encrypted = SecurityUtility.encryptRSA(testString, keyPair.getPrivate());
        Assert.assertNotEquals(testString, encrypted);
        Assert.assertEquals(testString, SecurityUtility.decryptRSA(encrypted, keyPair.getPublic()));
    }
}