package edu.harvard.dbmi.avillach;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.ApplicationException;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.net.ProxySelector;

@Singleton
@Startup
@ApplicationScoped
public class PicSureWarInit {

    Logger logger = LoggerFactory.getLogger(PicSureWarInit.class);

    // decide which authentication method is going to be used
    public static final String VERIFY_METHOD_LOCAL="local";
    public static final String VERIFY_METHOD_TOKEN_INTRO="tokenIntrospection";
    public static String ROLES_CLAIM;
    public static String USERID_CLAIM;
    public static String CLIENT_SECRET;
    public static String VERIFY_USER_METHOD;
    public static String TOKEN_INTROSPECTION_URL;
    public static String TOKEN_INTROSPECTION_TOKEN;

    //to be able to pre modified
    public static final ObjectMapper objectMapper = new ObjectMapper();

    // check the example from Apache HttpClient official website:
    // http://hc.apache.org/httpcomponents-client-4.5.x/httpclient/examples/org/apache/http/examples/client/ClientMultiThreadedExecution.java
    public static final PoolingHttpClientConnectionManager HTTP_CLIENT_CONNECTION_MANAGER;

    // If want to use self sign certificate for https,
    // please follow the official httpclient example link:
    // https://hc.apache.org/httpcomponents-client-ga/httpclient/examples/org/apache/http/examples/client/ClientCustomSSL.java
    public static final CloseableHttpClient CLOSEABLE_HTTP_CLIENT;
    static {
        HTTP_CLIENT_CONNECTION_MANAGER = new PoolingHttpClientConnectionManager();
        HTTP_CLIENT_CONNECTION_MANAGER.setMaxTotal(100);
        CLOSEABLE_HTTP_CLIENT = HttpClients
                .custom()
                .setConnectionManager(HTTP_CLIENT_CONNECTION_MANAGER)
                .setRoutePlanner(
                        new SystemDefaultRoutePlanner(ProxySelector
                                .getDefault()))
                .build();
    }

    @PostConstruct
    public void init() {
        loadTokenIntrospection();
        initializeRolesClaim();
// TODO: fix these
        try{
            Context ctx = new InitialContext();
            CLIENT_SECRET = (String) ctx.lookup("java:global/picsure_client_secret");
            USERID_CLAIM  = (String) ctx.lookup("java:global/picsure_user_id_claim");
            ctx.close();
        } catch (NamingException e) {
            if (VERIFY_USER_METHOD != VERIFY_METHOD_LOCAL) {
                throw new RuntimeException();
            } else {
                ROLES_CLAIM = "foobar-23452345";
            }
        }

    }

    private void initializeRolesClaim(){
        try{
            logger.info("Initializing roles claim.");
            Context ctx = new InitialContext();
            ROLES_CLAIM = (String) ctx.lookup("global/roles_claim");
            ctx.close();
            logger.info("Finished initializing roles claim.");
        } catch (NamingException e) {
            ROLES_CLAIM = "privileges";
        }
    }

    private void loadTokenIntrospection(){
        logger.info("start loading token introspection...");
        try {
            Context ctx = new InitialContext();
            VERIFY_USER_METHOD = (String) ctx.lookup("global/picsure_verify_user_method");
            TOKEN_INTROSPECTION_URL = (String) ctx.lookup("global/picsure_token_introspection_url");
            TOKEN_INTROSPECTION_TOKEN = (String) ctx.lookup("global/picsure_token_introspection_token");
            ctx.close();
        } catch (NamingException e) {
            VERIFY_USER_METHOD = VERIFY_METHOD_LOCAL;
        }

        logger.info("verify_user_method setup as: " + VERIFY_USER_METHOD);
    }

    public String getToken_introspection_url() {
        return TOKEN_INTROSPECTION_URL;
    }

    public void setToken_introspection_url(String token_introspection_url) {
        this.TOKEN_INTROSPECTION_URL = token_introspection_url;
    }

    public String getToken_introspection_token() {
        return TOKEN_INTROSPECTION_TOKEN;
    }

    public void setToken_introspection_token(String token_introspection_token) {
        this.TOKEN_INTROSPECTION_TOKEN = token_introspection_token;
    }

    public String getVerify_user_method() {
        return VERIFY_USER_METHOD;
    }

    public void setVerify_user_method(String verify_user_method) {
        this.VERIFY_USER_METHOD = verify_user_method;
    }
}
