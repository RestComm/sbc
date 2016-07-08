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

import java.util.Hashtable;

import org.restcomm.sbc.threat.Threat;

 /**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    14/5/2016 12:42:49
 * @class   ThreatManager.java
 * @project Servlet2.5SBC
 *
 */
public class ThreatManager {
	
	private Hashtable<String, Threat> threats=new Hashtable<String, Threat>();
	
	private static ThreatManager threatManager;
	
	private ThreatManager() {
		
	}
	
	public static ThreatManager getThreatManager() {
		if(threatManager==null) {
			threatManager=new ThreatManager();
		}
		return threatManager;
	}
	
	
	public Threat create(Threat.Type type, String user, String host, int port, String userAgent, String transport) {
		Threat threat=new Threat();
		threat.setHost(host);
		threat.setPort(port);
		threat.setUserAgent(userAgent);
		threat.setTransport(transport);
		threat.setUser(user);
		threat.setType(type);
		threats.put(host, threat);
		
		return threat;
			
	}
	
	public Threat getThreat(String host) {
		return threats.get(host);
	}
	
	
	public boolean match(String host) {
		Threat threat=getThreat(host);
		if(threat!=null && threat.getHost().equals(host) ) {
			return true;
		}
		return false;
		
	}

	
}
