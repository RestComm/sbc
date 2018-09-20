package org.restcomm.sbc.managers;

import org.infinispan.manager.DefaultCacheManager;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    12 oct. 2016 22:55:44
 * @class   CacheManager.java
 *
 */
public class CacheManager {
	
	private static DefaultCacheManager cacheManager;
	
	private CacheManager() {
    	cacheManager = new DefaultCacheManager();
    	cacheManager.start();
	}
	
	public static DefaultCacheManager getCacheManager() {
		if(cacheManager==null) {
			new CacheManager();
		}
		return cacheManager;
	}

}
