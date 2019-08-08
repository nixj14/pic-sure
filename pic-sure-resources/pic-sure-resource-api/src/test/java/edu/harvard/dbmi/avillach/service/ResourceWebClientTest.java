package edu.harvard.dbmi.avillach.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import edu.harvard.dbmi.avillach.domain.*;
import edu.harvard.dbmi.avillach.util.PicSureStatus;
import edu.harvard.dbmi.avillach.util.exception.ApplicationException;
import edu.harvard.dbmi.avillach.util.exception.NotAuthorizedException;
import edu.harvard.dbmi.avillach.util.exception.ProtocolException;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.internal.RuntimeDelegateImpl;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ResourceWebClientTest {

    private final static ObjectMapper json = new ObjectMapper();
    private final static String token = "a.valid-Token";

    private final ResourceWebClient cut = new ResourceWebClient();
    private static String MOCKITO_BASE_URL;
    private static int MOCKITO_PORT;
    public  static WireMockClassRule wireMockRule;


    @BeforeClass
    public static void beforeClass() {
        //Need to be able to throw exceptions without container so we can verify correct errors are being thrown
        RuntimeDelegate runtimeDelegate = new RuntimeDelegateImpl();
        RuntimeDelegate.setInstance(runtimeDelegate);

        // nbenik - use JDNI contexts but fallback to POM systemPropertyVariables
        try {
            Context ctx = new InitialContext();
            MOCKITO_BASE_URL = (String) ctx.lookup("java:global/mockito_base_url");
            try {
                URL temp_url = new URL(MOCKITO_BASE_URL);
                MOCKITO_PORT = temp_url.getDefaultPort();
            } catch (MalformedURLException e2) {
                throw new RuntimeException("Mockito_base_url is malformed in JDNI setting!");
            }
            ctx.close();
        } catch (NamingException e1) {
            MOCKITO_BASE_URL = (String) System.getProperty("mockito_base_url");
            try {
                URL temp_url = new URL(MOCKITO_BASE_URL);
                MOCKITO_PORT = temp_url.getDefaultPort();
            } catch (MalformedURLException e2) {
                throw new RuntimeException("Mockito_base_url is malformed in JDNI setting!");
            }
        }
        throw new RuntimeException("We executed constructor");
    }

    @Test
    public void testInfo() throws JsonProcessingException{
        String resourceInfo = json.writeValueAsString(new ResourceInfo());

        wireMockRule.stubFor(any(urlEqualTo("/info"))
                .willReturn(aResponse()
                .withStatus(200)
              .withBody(resourceInfo)));

        //Any targetURL that matches /info will trigger wiremock
        String targetURL = "/info";
        //Should throw an error if any parameters are missing
        try {
            cut.info(MOCKITO_BASE_URL, null);
            fail();
        } catch (ProtocolException e) {
            assertEquals(ProtocolException.MISSING_DATA, e.getContent());
        }
        QueryRequest queryRequest = new QueryRequest();
        Map<String, String> credentials = new HashMap<>();
        credentials.put(ResourceWebClient.BEARER_TOKEN_KEY, token);
        queryRequest.setResourceCredentials(credentials);
//        queryRequest.setTargetURL(targetURL);
        //Obviously should fail without the rsURL
        try {
            cut.info(null, queryRequest);
            fail();
        } catch (ApplicationException e) {
            assertEquals(ApplicationException.MISSING_RESOURCE_PATH, e.getContent());
        }

        //Should fail without a targetURL

//        queryRequest.setTargetURL(null);
//        try {
//            cut.info(MOCKITO_BASE_URL, queryRequest);
//            fail();
//        } catch (ApplicationException e) {
//            assertEquals(ApplicationException.MISSING_TARGET_URL, e.getContent());
//        }

        //Assuming everything goes right
//        queryRequest.setTargetURL(targetURL);
        ResourceInfo result = cut.info(MOCKITO_BASE_URL, queryRequest);
        assertNotNull("Result should not be null", result);

        //What if the resource has a problem?
        wireMockRule.stubFor(any(urlEqualTo("/info"))
                .willReturn(aResponse()
                        .withStatus(500)));

        try {
            cut.info(MOCKITO_BASE_URL, queryRequest);
            fail();
        } catch (Exception e) {
            assertTrue( e.getMessage().contains("500 Server Error"));
        }

        //What if resource returns the wrong type of object for some reason?
        String incorrectResponse = json.writeValueAsString(new SearchResults());
        wireMockRule.stubFor(any(urlEqualTo("/info"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(incorrectResponse)));

        try {
            cut.info(MOCKITO_BASE_URL, queryRequest);
            fail();
        } catch (Exception e) {
            assertTrue( e.getMessage().contains("Incorrect object type returned"));
        }
    }

    @Test
    public void testSearch() throws JsonProcessingException{
        String searchResults = json.writeValueAsString(new SearchResults());

        wireMockRule.stubFor(any(urlEqualTo("/search"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(searchResults)));

        //Should throw an error if any parameters are missing
        try {
            cut.search(MOCKITO_BASE_URL, null);
            fail();
        } catch (ProtocolException e) {
            assertEquals(ProtocolException.MISSING_DATA, e.getContent());
        }

        QueryRequest request = new QueryRequest();
        try {
            cut.search(MOCKITO_BASE_URL, request);
            fail();
        } catch (ProtocolException e) {
            assertEquals(ProtocolException.MISSING_DATA, e.getContent());
        }

        request.setQuery("query");

//        try {
//            cut.search(MOCKITO_BASE_URL, request);
//            fail();
//        } catch (ApplicationException e) {
//            assertEquals(ApplicationException.MISSING_TARGET_URL, e.getContent());
//        }

        String targetURL = "/search";
//        request.setTargetURL(targetURL);

        try {
            cut.search(null, request);
            fail();
        } catch (ApplicationException e) {
            assertEquals(ApplicationException.MISSING_RESOURCE_PATH, e.getContent());
        }

//        //Should fail if no credentials given
//        request.setQuery("query");
//        request.setTargetURL(targetURL);
//        try {
//            cut.search(MOCKITO_BASE_URL, request);
//            fail();
//        } catch (Exception e) {
//            assertEquals("HTTP 401 Unauthorized", e.getMessage());
//        }

        //With credentials but not search term
       /* Map<String, String> credentials = new HashMap<>();
        credentials.put(ResourceWebClient.BEARER_TOKEN_KEY, token);
        request.setQuery(null);
        request.setResourceCredentials(credentials);
        try {
            cut.search(MOCKITO_BASE_URL, request);
            fail();
        } catch (ProtocolException e) {
            assertEquals(ProtocolException.MISSING_DATA, e.getContent());
        }*/

        //Should fail with no targetURL
        Map<String, String> credentials = new HashMap<>();
        credentials.put(ResourceWebClient.BEARER_TOKEN_KEY, token);
        request.setResourceCredentials(credentials);
//        request.setTargetURL(null);
        request.setQuery("%blood%");
//        try {
//            cut.search(MOCKITO_BASE_URL, request);
//            fail();
//        } catch (ApplicationException e) {
//            assertEquals(ApplicationException.MISSING_TARGET_URL, e.getContent());
//        }

//        request.setTargetURL(targetURL);
        SearchResults result = cut.search(MOCKITO_BASE_URL, request);
        assertNotNull("Result should not be null", result);

        //What if the resource has a problem?
        wireMockRule.stubFor(any(urlEqualTo("/search"))
                .willReturn(aResponse()
                        .withStatus(500)));

        try {
            cut.search(MOCKITO_BASE_URL, request);
            fail();
        } catch (Exception e) {
            assertTrue( e.getMessage().contains("500 Server Error"));
        }

        //What if resource returns the wrong type of object for some reason?
        ResourceInfo incorrectResponse = new ResourceInfo();
        incorrectResponse.setName("resource name");
        incorrectResponse.setId(new UUID(1L, 1L));
        wireMockRule.stubFor(any(urlEqualTo("/search"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(json.writeValueAsString(incorrectResponse))));

        try {
            cut.search(MOCKITO_BASE_URL, request);
            fail();
        } catch (Exception e) {
            assertTrue( e.getMessage().contains("Incorrect object type returned"));
        }
    }

    @Test
    public void testQuery() throws JsonProcessingException{
        String queryResults = json.writeValueAsString(new QueryStatus());

        wireMockRule.stubFor(any(urlEqualTo("/query"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(queryResults)));

        //Should fail if any parameters are missing
        try {
            cut.query(MOCKITO_BASE_URL, null);
            fail();
        } catch (ProtocolException e) {
            assertEquals(ProtocolException.MISSING_DATA, e.getContent());
        }
        QueryRequest request = new QueryRequest();
//        request.setTargetURL("/query");

        try {
            cut.query(null, request);
            fail();
        } catch (ApplicationException e) {
            assertEquals(ApplicationException.MISSING_RESOURCE_PATH, e.getContent());
        }

        //Should fail if no credentials given
//        try {
//            cut.query(MOCKITO_BASE_URL, request);
//            fail();
//        } catch (Exception e) {
//            assertEquals("HTTP 401 Unauthorized", e.getMessage());
//        }

        Map<String, String> credentials = new HashMap<>();
        request.setResourceCredentials(credentials);
//        request.setTargetURL(null);
        //Should fail without a targetURL
//        try {
//            cut.query(MOCKITO_BASE_URL, request);
//            fail();
//        } catch (ApplicationException e) {
//            assertEquals(ApplicationException.MISSING_TARGET_URL, e.getContent());
//        }

//        request.setTargetURL("/query");

        //Everything goes correctly
        QueryStatus result = cut.query(MOCKITO_BASE_URL, request);
        assertNotNull("Result should not be null", result);

        //What if the resource has a problem?
        wireMockRule.stubFor(any(urlEqualTo("/query"))
                .willReturn(aResponse()
                        .withStatus(500)));

        try {
            cut.query(MOCKITO_BASE_URL, request);
            fail();
        } catch (Exception e) {
            assertTrue( e.getMessage().contains("500 Server Error"));
        }

        //What if resource returns the wrong type of object for some reason?
        ResourceInfo incorrectResponse = new ResourceInfo();
        incorrectResponse.setName("resource name");
        incorrectResponse.setId(new UUID(1L, 1L));
        wireMockRule.stubFor(any(urlEqualTo("/query"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(json.writeValueAsString(incorrectResponse))));

        try {
            cut.query(MOCKITO_BASE_URL, request);
            fail();
        } catch (Exception e) {
            assertTrue( e.getMessage().contains("Incorrect object type returned"));
        }
    }

    @Test
    public void testQueryResult() throws JsonProcessingException{
        String testId = "230048";
        String mockResult = "Any old response will work";



        wireMockRule.stubFor(any(urlMatching("/query/.*/result"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(mockResult)));

        //Should fail if missing any parameters
//        try {
//            cut.queryResult(MOCKITO_BASE_URL, testId, null);
//            fail();
//        } catch (ProtocolException e) {
//            assertEquals(ProtocolException.MISSING_DATA, e.getContent());
//        }
        QueryRequest queryRequest = new QueryRequest();
        Map<String, String> credentials = new HashMap<>();
        credentials.put(ResourceWebClient.BEARER_TOKEN_KEY, token);
        queryRequest.setResourceCredentials(credentials);
//        String targetURL = "/query/13452134/result";
////        queryRequest.setTargetURL(targetURL);
//
//        try {
//            cut.queryResult(MOCKITO_BASE_URL, null, queryRequest);
//            fail();
//        } catch (ProtocolException e) {
//            assertEquals(ProtocolException.MISSING_QUERY_ID, e.getContent());
//        }
//        try {
//            cut.queryResult(null, testId, queryRequest);
//            fail();
//        } catch (ApplicationException e) {
//            assertEquals(ApplicationException.MISSING_RESOURCE_PATH, e.getContent());
//        }

////        queryRequest.setTargetURL(null);
//        //Should fail without a targetURL
//        try {
//            cut.queryResult(MOCKITO_BASE_URL, testId, queryRequest);
//            fail();
//        } catch (ApplicationException e) {
//            assertEquals(ApplicationException.MISSING_TARGET_URL, e.getContent());
//        }
//
////        queryRequest.setTargetURL(targetURL);


        //Everything should work here
        Response result = cut.queryResult(MOCKITO_BASE_URL, testId, queryRequest);
        assertNotNull("Result should not be null", result);
        try {
            String resultContent = IOUtils.toString((InputStream) result.getEntity(), "UTF-8");
            assertEquals("Result should match " + mockResult, mockResult, resultContent);
        } catch (IOException e ){
            fail("Result content was unreadable");
        }

        //What if the resource has a problem?
        wireMockRule.stubFor(any(urlMatching("/query/.*/result"))
                .willReturn(aResponse()
                        .withStatus(500)));

        try {
            cut.queryResult(MOCKITO_BASE_URL, testId, queryRequest);
            fail();
        } catch (Exception e) {
            assertTrue( e.getMessage().contains("500 Server Error"));
        }
    }

    @Test
    public void testQueryStatus() throws JsonProcessingException{
        String testId = "230048";
        QueryStatus testResult = new QueryStatus();
        testResult.setStatus(PicSureStatus.PENDING);
        testResult.setResourceStatus("RUNNING");
        String queryStatus = json.writeValueAsString(testResult);

        wireMockRule.stubFor(any(urlMatching("/query/.*/status"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(queryStatus)));

        //Fails with any missing parameters
        try {
            cut.queryStatus(MOCKITO_BASE_URL, testId, null);
            fail();
        } catch (ProtocolException e) {
            assertEquals(ProtocolException.MISSING_DATA, e.getContent());
        }
        QueryRequest queryRequest = new QueryRequest();
        Map<String, String> credentials = new HashMap<>();
        credentials.put(ResourceWebClient.BEARER_TOKEN_KEY, token);
        queryRequest.setResourceCredentials(credentials);
//        String targetURL = "/query/13452134/result";
//        queryRequest.setTargetURL(targetURL);

        try {
            cut.queryStatus(MOCKITO_BASE_URL,null, queryRequest);
            fail();
        } catch (ProtocolException e) {
            assertEquals(ProtocolException.MISSING_QUERY_ID, e.getContent());
        }
        try {
            cut.queryStatus(null, testId, queryRequest);
            fail();
        } catch (ApplicationException e) {
            assertEquals(ApplicationException.MISSING_RESOURCE_PATH, e.getContent());
        }

//        queryRequest.setTargetURL(null);

        //Should fail without a targetURL
//        try {
//            cut.queryStatus(MOCKITO_BASE_URL, testId, queryRequest);
//            fail();
//        } catch (ApplicationException e) {
//            assertEquals(ApplicationException.MISSING_TARGET_URL, e.getContent());
//        }



//        queryRequest.setTargetURL(targetURL);

        //Everything should work here
        QueryStatus result = cut.queryStatus(MOCKITO_BASE_URL, testId, queryRequest);
        assertNotNull("Result should not be null", result);
        //Make sure all necessary fields are present
        assertNotNull("Duration should not be null",result.getDuration());
        assertNotNull("Expiration should not be null",result.getExpiration());
        assertNotNull("ResourceStatus should not be null",result.getResourceStatus());
        assertNotNull("Status should not be null",result.getStatus());

        //What if the resource has a problem?
        wireMockRule.stubFor(any(urlMatching("/query/.*/status"))
                .willReturn(aResponse()
                        .withStatus(500)));

        try {
            cut.queryStatus(MOCKITO_BASE_URL, testId, queryRequest);
            fail();
        } catch (Exception e) {
            assertTrue( e.getMessage().contains("500 Server Error"));
        }

        //What if resource returns the wrong type of object for some reason?
        ResourceInfo incorrect = new ResourceInfo();
        incorrect.setName("resource name");
        incorrect.setId(new UUID(1L, 1L));
        wireMockRule.stubFor(any(urlMatching("/query/.*/status"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(json.writeValueAsString(incorrect))));

        try {
            cut.queryStatus(MOCKITO_BASE_URL, testId, queryRequest);
            fail();
        } catch (Exception e) {
            assertTrue( e.getMessage().contains("Incorrect object type returned"));
        }
    }
}
