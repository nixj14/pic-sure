package edu.harvard.dbmi.avillach;

import org.glassfish.jersey.internal.RuntimeDelegateImpl;
import org.junit.BeforeClass;
import org.junit.Ignore;

import javax.ws.rs.ext.RuntimeDelegate;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Ignore
public class BaseServiceTest {

    protected static String PICSURE_ENDPOINT_URL;

    @BeforeClass
    public static void beforeClass() {
        InputStream testConfiguration = BaseServiceTest.class.getClassLoader().getResourceAsStream("testing.properties");
        Properties testProperties  = new Properties();
        try {
            testProperties.load(testConfiguration);
            PICSURE_ENDPOINT_URL = (String) testProperties.getProperty("picsure_url");
        } catch (IOException e) {
            throw new RuntimeException("Error loading configuration values from 'testing.properties'");
        }

        //Need to be able to throw exceptions without container so we can verify correct errors are being thrown
        RuntimeDelegate runtimeDelegate = new RuntimeDelegateImpl();
        RuntimeDelegate.setInstance(runtimeDelegate);
}

}
