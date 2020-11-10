package com.cindi;

import com.cindi.domain.Step;
import com.cindi.domain.WorkflowTemplate;
import com.cindi.requests.*;
import com.cindi.utilities.SecurityUtility;
import com.cindi.valueobjects.NextStep;
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
import org.bouncycastle.jcajce.provider.symmetric.AES;
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
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;
import org.apache.http.client.*;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
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
    Executor ex;

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
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).setSslcontext(sc).build();
            ex = Executor.newInstance(httpClient);

            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(4096);
            KeyPair keyPair = SecurityUtility.generateRSAKeyPair();
            privateKeyRaw = keyPair.getPrivate();
            privateKey = SecurityUtility.GetPrivateString(keyPair.getPrivate());
            publicKey = SecurityUtility.GetPublicString(keyPair.getPublic());
            IdKey = RegisterBot(botName, publicKey).IdKey;
        } catch (NoSuchAlgorithmException | NoSuchProviderException | IOException | KeyManagementException e) {
            e.printStackTrace();
        }

    }



    public String AddStepLog(UUID stepId, String log) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return SendRequest(Request.Post(this.url + "/api/steps/" + stepId.toString() + "/logs")
                .bodyString(mapper.writeValueAsString(new Object() {
                    String Log = log;
                }), ContentType.APPLICATION_JSON)
                .connectTimeout(60000)
                .socketTimeout(60000), true).returnContent().asString();
    }

    public String CompleteStep(UpdateStepRequest request) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return SendRequest(Request.Put(this.url + "/api/steps/" + request.Id)
                .bodyString(mapper.writeValueAsString(request), ContentType.APPLICATION_JSON)
                .connectTimeout(60000)
                .socketTimeout(60000), true).returnContent().asString();
    }

    public NextStep GetNextStep(StepRequest request) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        CindiHttpResult<Step> value = mapper.readValue(SendRequest(Request.Post(this.url + "/api/steps/assignment-requests")
                .bodyString(mapper.writeValueAsString(request), ContentType.APPLICATION_JSON)
                .connectTimeout(60000)
                .socketTimeout(60000), true).returnContent().asString(), new TypeReference<CindiHttpResult<Step>>() {});
        NextStep result =  new NextStep();
        result.Step = value.Result;
        result.EncryptionKey = value.EncryptionKey;
        return result;
    }

    public String PostNewStep(StepInput stepInput) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return SendRequest(Request.Post(this.url + "/api/steps")
                .bodyString(mapper.writeValueAsString(stepInput), ContentType.APPLICATION_JSON)
                .connectTimeout(60000)
                .socketTimeout(60000), true).returnContent().asString();
    }
    public String PostNewWorkflow(WorkflowInput input) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return SendRequest(Request.Post(this.url + "/api/workflows")
                .bodyString(mapper.writeValueAsString(input), ContentType.APPLICATION_JSON)
                .connectTimeout(60000)
                .socketTimeout(60000), true).returnContent().asString();
    }
    public String PostNewWorkflowTemplate(WorkflowTemplate WorkflowTemplate) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return SendRequest(Request.Post(this.url + "/api/workflow-templates")
                .bodyString(mapper.writeValueAsString(WorkflowTemplate), ContentType.APPLICATION_JSON)
                .connectTimeout(60000)
                .socketTimeout(60000), true).returnContent().asString();
    }

    public String PostStepTemplate(NewStepTemplateRequest stepTemplate) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return SendRequest(Request.Post(this.url + "/api/step-templates")
                .bodyString(mapper.writeValueAsString(stepTemplate), ContentType.APPLICATION_JSON)
                .connectTimeout(60000)
                .socketTimeout(60000), true).returnContent().asString();

    }
    public Response SendRequest(Request r) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        return SendRequest(r, false);
    }


    public Response SendRequest(Request r, Boolean authorize) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        try {
            if(authorize)
            {
                r.addHeader("BotKey",IdKey);
                r.addHeader("Nonce", Nonce.toString());
                Nonce++;
            }
            return ex.execute(r
                    .connectTimeout(60000)
                    .socketTimeout(60000));
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
                .connectTimeout(60000)
                .socketTimeout(60000)).returnContent().asString();
        CindiHttpResult<NewBotKeyResult> parsed = mapper.readValue(result, new TypeReference<CindiHttpResult<NewBotKeyResult>>() {});
        return parsed.Result;
    }

}
