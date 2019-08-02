package edu.harvard.hms.dbmi.avillach.irct;

import org.junit.BeforeClass;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class BaseIT {
	protected static String IRCT_ENDPOINT_URL;
	
	@BeforeClass
	public static void beforeClass() {
		// nbenik - use JDNI contexts
		try {
			Context ctx = new InitialContext();
			IRCT_ENDPOINT_URL = (String) ctx.lookup("java:global/irct_endpoint_url");
			ctx.close();
		} catch (
				NamingException e) {
			throw new RuntimeException("could not find setting in JDNI");
		}
	}
}
