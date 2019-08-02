package edu.harvard.dbmi.avillach;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import edu.harvard.dbmi.avillach.data.entity.Resource;
import edu.harvard.hms.dbmi.avillach.GnomeI2B2CountResourceRS;
import edu.harvard.hms.dbmi.avillach.IRCTResourceRS;

import static edu.harvard.dbmi.avillach.util.HttpClientUtil.composeURL;

@Singleton
@Startup
public class ResourceTestInitializer
{
    public static String PICSURE_URL;
    public static String IRCT_URL;
    public static String AGGREGATE_RS_URL;
    public static String IRCT_RS_URL;
    public static String GNOME_I2B2_RS_URL;

    @PersistenceContext(unitName = "picsure")
    private EntityManager em;

    @PostConstruct
    public void init() {
        // get all the variable values needed for testing
        try {
            InitialContext ctx = new InitialContext();
            PICSURE_URL = (String) ctx.lookup("java:global/picsure_url");
            IRCT_URL = (String) ctx.lookup("java:global/");
            IRCT_RS_URL = (String) ctx.lookup("java:global/");
            AGGREGATE_RS_URL = (String) ctx.lookup("java:global/aggregate_rs_url");
            GNOME_I2B2_RS_URL = (String) ctx.lookup("java:global/");
            ctx.close();
        } catch (NamingException e) {
            throw new RuntimeException("Could not find JNDI name(s)!");
        }
        insertTestResources();
    }

    public void insertTestResources() {
        System.out.println("ResourceTestInitializer, target picsure url is!!!: " + PICSURE_URL);

		Resource fooResource = new Resource()
//				.setTargetURL(IRCT_URL)
//                .setTargetURL("http://localhost:8080/pic-sure-api-wildfly-2.0.0-SNAPSHOT/pic-sure/v1.4")
                .setResourceRSPath(IRCT_RS_URL)
				.setDescription("HMS DBMI NHANES PIC-SURE 1.4  Supply token with key '" + IRCTResourceRS.IRCT_BEARER_TOKEN_KEY + "'")
				.setName("nhanes.hms.harvard.edu")
                .setToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmb29AYmFyLmNvbSIsImlzcyI6ImJhciIsImV4cCI6ODY1NTI4Mzk4NTQzLCJpYXQiOjE1Mjg0ODQ5NDMsImp0aSI6IkZvbyIsImVtYWlsIjoiZm9vQGJhci5jb20ifQ.KE2NIfCzQnd_vhykhb0sHdPHEwvy2Wphc4UVsKAVTgM");
		em.persist(fooResource);

        Resource ga4ghResource = new Resource()
				.setTargetURL("http://54.174.229.198:8080/ga4gh/dos/v1/")
                .setResourceRSPath("http://54.174.229.198:8080/ga4gh/dos/v1/")
                .setDescription("GA4GH DOS Resource Server at DBMI/AvillachLab")
                .setName("ga4gh-dos-server");
        em.persist(ga4ghResource);

        Resource aggregateResource = new Resource()
//                .setTargetURL("http://localhost:8080/pic-sure-api-wildfly-2.0.0-SNAPSHOT/pic-sure/group")
                .setTargetURL(PICSURE_URL)
                .setResourceRSPath(AGGREGATE_RS_URL)
                .setToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmb29AYmFyLmNvbSIsImlzcyI6ImJhciIsImV4cCI6ODY1NTI4Mzk4NTQzLCJpYXQiOjE1Mjg0ODQ5NDMsImp0aSI6IkZvbyIsImVtYWlsIjoiZm9vQGJhci5jb20ifQ.KE2NIfCzQnd_vhykhb0sHdPHEwvy2Wphc4UVsKAVTgM")
                .setDescription("Aggregate Resource RS")
                .setName("Aggregate Resource RS");
        em.persist(aggregateResource);

        Resource hsapiResource = new Resource()
                .setTargetURL("https://beta.commonsshare.org/hsapi/")
                .setResourceRSPath(composeURL(PICSURE_URL,"hsapi"))
                .setToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmb29AYmFyLmNvbSIsImlzcyI6ImJhciIsImV4cCI6ODY1NTI4Mzk4NTQzLCJpYXQiOjE1Mjg0ODQ5NDMsImp0aSI6IkZvbyIsImVtYWlsIjoiZm9vQGJhci5jb20ifQ.KE2NIfCzQnd_vhykhb0sHdPHEwvy2Wphc4UVsKAVTgM")
                .setDescription("HSAPI Resource RS")
                .setName("HSAPI Resource RS");
        em.persist(hsapiResource);

        Resource gnomeI2B2Resource = new Resource()
                .setTargetURL(PICSURE_URL)
                .setResourceRSPath(GNOME_I2B2_RS_URL)
                .setToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmb29AYmFyLmNvbSIsImlzcyI6ImJhciIsImV4cCI6ODY1NTI4Mzk4NTQzLCJpYXQiOjE1Mjg0ODQ5NDMsImp0aSI6IkZvbyIsImVtYWlsIjoiZm9vQGJhci5jb20ifQ.KE2NIfCzQnd_vhykhb0sHdPHEwvy2Wphc4UVsKAVTgM")
                .setDescription("Gnome I2B2 Count Resource RS Supply tokens with keys " + GnomeI2B2CountResourceRS.I2B2_BEARER_TOKEN_KEY + " and " + GnomeI2B2CountResourceRS.GNOME_BEARER_TOKEN_KEY)
                .setName("Gnome I2B2 Count Resource RS");
        em.persist(gnomeI2B2Resource);
    }

}
