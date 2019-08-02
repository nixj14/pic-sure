package edu.harvard.dbmi.avillach;

import org.glassfish.jersey.internal.RuntimeDelegateImpl;
import org.junit.BeforeClass;

import org.junit.Ignore;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.ext.RuntimeDelegate;

@Ignore
public class BaseServiceTest {

    protected static String PICSURE_ENDPOINT_URL;

    @BeforeClass
    public static void beforeClass() {
        // nbenik - use JDNI contexts
        try {
            Context ctx = new InitialContext();
            PICSURE_ENDPOINT_URL = (String) ctx.lookup("global/picsure_url");
            ctx.close();
        } catch (NamingException e) {
            throw new RuntimeException("Could not find setting(s) in JDNI!");
        }

        //Need to be able to throw exceptions without container so we can verify correct errors are being thrown
        RuntimeDelegate runtimeDelegate = new RuntimeDelegateImpl();
        RuntimeDelegate.setInstance(runtimeDelegate);
}

}
