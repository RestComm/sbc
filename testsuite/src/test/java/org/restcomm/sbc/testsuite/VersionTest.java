package org.restcomm.sbc.testsuite;


import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 * @author <a href="mailto:jean.deruelle@telestax.com">Jean Deruelle</a>
 */

public class VersionTest {
    private final static Logger LOG = Logger.getLogger(VersionTest.class.getName());
    private static final String version = org.restcomm.sbc.Version.getVersion();

    @Test
    public void testVersion() {
        LOG.info(version);
    }
}
