/*******************************************************************************
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc, Eolos IT Corp and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 *******************************************************************************/
package org.restcomm.sbc;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.restcomm.sbc.notification.SuspectActivity;
import org.restcomm.sbc.notification.SuspectActivityCache;
import org.restcomm.sbc.notification.SuspectActivityElectable;

import static org.junit.Assert.*; 
 
/**
 * @author Crunchify.com
 */
 
public class SuspectActivityCacheTest {
	private static transient Logger LOG = Logger.getLogger(SuspectActivityCacheTest.class);
	
    public static void main(String[] args) throws InterruptedException {
 
        SuspectActivityCacheTest suspectActivityCache = new SuspectActivityCacheTest();
 
        LOG.info("\n\n==========Test1: suspectActivityTestAddRemoveObjects ==========");
        suspectActivityCache.suspectActivityShouldAddRemoveObjects();
        LOG.info("\n\n==========Test2: suspectActivityTestExpiredCacheObjects ==========");
        suspectActivityCache.suspectActivityShouldHaveExpiredCacheObjects();
        LOG.info("\n\n==========Test3: suspectActivityTestObjectsCleanupTime ==========");
        suspectActivityCache.suspectActivityShouldCleanupObjects();
    }
    @Test
    public void suspectActivityShouldAddRemoveObjects() {
 
        // Test with timeToLiveInSeconds = 200 seconds
        // timerIntervalInSeconds = 500 seconds
        // maxItems = 6
        SuspectActivityCache<String, SuspectActivityElectable> cache = new SuspectActivityCache<String, SuspectActivityElectable>(200, 500, 6);
        
        SuspectActivity sar1 = new SuspectActivity("201.216.233.51");
        SuspectActivity sar2 = new SuspectActivity("201.216.233.52");
        SuspectActivity sar3 = new SuspectActivity("201.216.233.53");
        SuspectActivity sar4 = new SuspectActivity("201.216.233.54");
        SuspectActivity sar5 = new SuspectActivity("201.216.233.55");
        SuspectActivity sar6 = new SuspectActivity("201.216.233.56");
        
        for (int i = 0; i < 15; i++) {
        	cache.update(sar1.getHost(), sar1);
        }
         
        cache.update(sar2.getHost(), sar2);
        cache.update(sar3.getHost(), sar3);
        cache.update(sar4.getHost(), sar4);
        cache.update(sar5.getHost(), sar5);
        cache.update(sar6.getHost(), sar6);
        
 
        LOG.info("6 Cache Object Added.. cache.size(): " + cache.size());
        
        for(String host:cache.getThreatCandidates()) {
        	LOG.info(host+"->"+cache.get(host).getUnauthorizedAccessCount());   	
        }
       
        cache.remove("IBM");
        LOG.info("One object removed.. cache.size(): " + cache.size());
 
        cache.update("Twitter", new SuspectActivity("Twitter"));
        cache.update("SAP", new SuspectActivity("SAP"));
        LOG.info("Two objects Added but reached maxItems.. cache.size(): " + cache.size());
 
    }
    @Test
    public void suspectActivityShouldHaveExpiredCacheObjects() throws InterruptedException {
 
        // Test with timeToLiveInSeconds = 1 second
        // timerIntervalInSeconds = 1 second
        // maxItems = 10
        SuspectActivityCache<String, SuspectActivityElectable> cache = new SuspectActivityCache<String, SuspectActivityElectable>(1, 1, 10);
 
        cache.update("eBay", new SuspectActivity("eBay"));
        cache.update("eBay", new SuspectActivity("eBay"));
        // Adding 3 seconds sleep.. Both above objects will be removed from
        // Cache because of timeToLiveInSeconds value
        Thread.sleep(3000);
 
        LOG.info("Two objects are added but reached timeToLive. cache.size(): " + cache.size());
 
    }
    @Test
    public void suspectActivityShouldCleanupObjects() throws InterruptedException {
        int size = 500000;
 
        // Test with timeToLiveInSeconds = 100 seconds
        // timerIntervalInSeconds = 100 seconds
        // maxItems = 500000
 
        SuspectActivityCache<String, SuspectActivityElectable> cache = new SuspectActivityCache<String, SuspectActivityElectable>(100, 100, 500000);
 
        for (int i = 0; i < size; i++) {
            String value = Integer.toString(i);
            cache.update(value, new SuspectActivity(value));
        }
 
        Thread.sleep(200);
 
        long start = System.currentTimeMillis();
        cache.cleanup();
        double finish = (double) (System.currentTimeMillis() - start) / 1000.0;
 
        LOG.info("Cleanup times for " + size + " objects are " + finish + " s");
 
    }
}