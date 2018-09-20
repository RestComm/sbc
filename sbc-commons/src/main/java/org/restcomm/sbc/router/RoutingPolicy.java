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
package org.restcomm.sbc.router;

import java.util.List;

import javax.servlet.sip.SipURI;

import org.restcomm.sbc.ConfigurationCache;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    17 mar. 2017 6:58:52
 * @class   RoutingPolicy.java
 *
 */
public abstract class RoutingPolicy {
	
	
	public abstract List<String> getTargets();
	public abstract void setTargets(List<String> targets);
	public abstract String getName();
	public abstract SipURI getCandidate();
	
	public SipURI getURI(int order) {
		if(order>getTargets().size()) {
			return null;
		}
		String target=getTargets().get(order);
		
		String [] address=target.split(":");
		SipURI uri=ConfigurationCache.getSipFactory().createSipURI("", address[1]);
		uri.setPort(Integer.parseInt(address[2]));
		uri.setTransportParam(address[0]);
		
		return uri;
		
	}
	
}
