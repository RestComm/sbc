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
 */

package org.restcomm.sbc.managers;

import org.restcomm.sbc.bo.Cache;
import org.restcomm.sbc.bo.Call;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    21/6/2016 8:52:36
 * @class   CallCacheManager.java
 *
 */
public class CallCacheManager extends Cache<Integer, Call> {
	
	private static CallCacheManager cacheManager;
	
	private CallCacheManager() {
		super();
	}
	
	public static CallCacheManager getCallCacheManager() {
		if(cacheManager==null) {
			cacheManager=new CallCacheManager();
		}
		return cacheManager;
	}
	
	
	public Call newCall(int uid, String fromIP, String fromUser, String toUser) {
		Call call=new Call(uid, fromIP, fromUser, toUser);		
		put(uid, call);
		return call;
			
	}
	
	public Call getCall(int uid) {
		return get(uid);
	}

}
