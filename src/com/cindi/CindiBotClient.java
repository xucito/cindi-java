package com.cindi;

import com.cindi.domain.Step;
import com.cindi.domain.WorkflowTemplate;
import com.cindi.requests.*;
import com.cindi.valueobjects.StepInput;
import com.cindi.valueobjects.WorkflowInput;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.PEMUtil;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import sun.misc.BASE64Decoder;
import sun.net.www.http.HttpClient;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;
import org.apache.http.client.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class CindiBotClient {

    public URI url;
    ObjectMapper mapper;
    public String privateKey;
    String publicKey;
    public String IdKey;
    public Integer Nonce = 0;
    PrivateKey privateKeyRaw;

    public CindiBotClient(String botName, String url)
    {
        try {
            this.url = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Security.addProvider(new BouncyCastleProvider());

        mapper = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair keyPair = generateRSAKeyPair();
            privateKeyRaw = keyPair.getPrivate();
            privateKey = GetPrivateString(keyPair.getPrivate());
            publicKey = GetPublicString(keyPair.getPublic());
            IdKey = RegisterBot(botName, publicKey).IdKey;
        } catch (NoSuchAlgorithmException | NoSuchProviderException | IOException | KeyManagementException e) {
            e.printStackTrace();
        }

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

    private static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
        generator.initialize(2048);

        KeyPair keyPair = generator.generateKeyPair();
        return keyPair;
    }


    public String AddStepLog(UUID stepId, String log) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return SendRequest(Request.Post(this.url + "/api/steps/" + stepId.toString() + "/logs")
                .bodyString(mapper.writeValueAsString(new Object() {
                    String Log = log;
                }), ContentType.APPLICATION_JSON)
                .connectTimeout(1000)
                .socketTimeout(1000), true).returnContent().asString();
    }

    public String CompleteStep(UpdateStepRequest request) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return SendRequest(Request.Put(this.url + "/api/steps/" + request.Id)
                .bodyString(mapper.writeValueAsString(request), ContentType.APPLICATION_JSON)
                .connectTimeout(1000)
                .socketTimeout(1000), true).returnContent().asString();
    }

    public Step GetNextStep(StepRequest request) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return mapper.readValue(SendRequest(Request.Post(this.url + "/api/steps/assignment-requests")
                .bodyString(mapper.writeValueAsString(request), ContentType.APPLICATION_JSON)
                .connectTimeout(1000)
                .socketTimeout(1000), true).returnContent().asString(), new TypeReference<CindiHttpResult<Step>>() {}).Result;
    }
    public String PostNewStep(StepInput stepInput) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return SendRequest(Request.Post(this.url + "/api/steps")
                .bodyString(mapper.writeValueAsString(stepInput), ContentType.APPLICATION_JSON)
                .connectTimeout(1000)
                .socketTimeout(1000), true).returnContent().asString();
    }
    public String PostNewWorkflow(WorkflowInput input) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return SendRequest(Request.Post(this.url + "/api/workflows")
                .bodyString(mapper.writeValueAsString(input), ContentType.APPLICATION_JSON)
                .connectTimeout(1000)
                .socketTimeout(1000), true).returnContent().asString();
    }
    public String PostNewWorkflowTemplate(WorkflowTemplate WorkflowTemplate) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return SendRequest(Request.Post(this.url + "/api/workflow-templates")
                .bodyString(mapper.writeValueAsString(WorkflowTemplate), ContentType.APPLICATION_JSON)
                .connectTimeout(1000)
                .socketTimeout(1000), true).returnContent().asString();
    }

    public String PostStepTemplate(NewStepTemplateRequest stepTemplate) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return SendRequest(Request.Post(this.url + "/api/step-templates")
                .bodyString(mapper.writeValueAsString(stepTemplate), ContentType.APPLICATION_JSON)
                .connectTimeout(1000)
                .socketTimeout(1000), true).returnContent().asString();

    }
    public Response SendRequest(Request r) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        return SendRequest(r, false);
    }


    public Response SendRequest(Request r, Boolean authorize) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {  }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {  }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).setSslcontext(sc).build();
            if(authorize)
            {
                r.addHeader("BotKey",IdKey);
                r.addHeader("Nonce", Nonce.toString());
                Nonce++;
            }
            return Executor.newInstance(httpClient).execute(r
                    .connectTimeout(30000)
                    .socketTimeout(30000));
        } catch (Exception e) {
            throw e;
        }
    }

    public NewBotKeyResult RegisterBot(String name, String rsaPublicKey) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        String result = SendRequest(Request.Post(this.url + "/api/bot-keys")
                .bodyString(mapper.writeValueAsString(new Object(){
                    public String BotKeyName = name;
                    public String PublicEncryptionKey = rsaPublicKey;
                }), ContentType.APPLICATION_JSON)
                .connectTimeout(1000)
                .socketTimeout(1000)).returnContent().asString();
        CindiHttpResult<NewBotKeyResult> parsed = mapper.readValue(result, new TypeReference<CindiHttpResult<NewBotKeyResult>>() {});
        return parsed.Result;
    }

    public static String decrypt(String data, PrivateKey privateKey) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        Cipher cipher1 = Cipher.getInstance("RSA");
        cipher1.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decStr = cipher1.doFinal(Base64.getDecoder().decode(data));
        return new String(decStr);
    }
}
