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

import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.restcomm.sbc.router.RoutingPolicy;
import org.restcomm.sbc.router.impl.FailoverRoutingPolicy;
import org.restcomm.sbc.router.impl.HARoutingPolicy;
import org.restcomm.sbc.router.impl.StaticRoutingPolicy;
import org.restcomm.sbc.router.impl.UnavailableRoutingPolicyException;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    17 mar. 2017 7:02:18
 * @class   RoutingPolicyFactory.java
 *
 */
public class RoutingPolicyFactory {
	
	public static String STATIC 	="static";
	public static String FAILOVER 	="fail-over";
	public static String HA		 	="load-balance";
	
	private static transient Logger LOG = Logger.getLogger(RoutingPolicyFactory.class);
	private HashMap<String, RoutingPolicy> policies=new HashMap<String, RoutingPolicy>();
	
	private static RoutingPolicyFactory routingPolicyFactory;
	
	private RoutingPolicyFactory() {
		
		registerRouter(STATIC	, new StaticRoutingPolicy());
		registerRouter(FAILOVER	, new FailoverRoutingPolicy());
		registerRouter(HA		, new HARoutingPolicy());
		
		
	}
	
	public static RoutingPolicyFactory getRoutingPolicyFactory() {
		if(routingPolicyFactory==null) {
			routingPolicyFactory=new RoutingPolicyFactory();
		}
		return routingPolicyFactory;
	}
	
	
	private void registerRouter(String policyName, RoutingPolicy policy) {
		
		policies.put(policyName, policy);
		
			
	}
	
	public RoutingPolicy getPolicy(String policy) throws UnavailableRoutingPolicyException {
		if(policy==null) {
			throw new UnavailableRoutingPolicyException(policy+" routing policy unavailable");
		}
		RoutingPolicy p=policies.get(policy.toLowerCase());
		if(p==null) {
			throw new UnavailableRoutingPolicyException(policy+" protocol policy unavailable");
		}
		else {
			if(LOG.isTraceEnabled()){
		          LOG.trace(">> Factoring RoutingPolicy ["+p.getName()+"]");
		    }
			return p;
		}
	}

	public RoutingPolicy getPolicy(Configuration config) throws UnavailableRoutingPolicyException {
		String name = config.getString("runtime-settings.routing-policy.policy[@name]");
		@SuppressWarnings("unchecked")
		List<String> targets = config.getList("runtime-settings.routing-policy.policy.militarized-zone-target.target");
		
		if(name==null) {
			throw new UnavailableRoutingPolicyException("routing policy unavailable");
		}
		RoutingPolicy p=policies.get(name);
		if(p==null) {
			throw new UnavailableRoutingPolicyException(name+" protocol policy unavailable");
		}
		else {
			p.setTargets(targets);
			
			return p;
		}
		
	}
	
	
	
	

}
