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
 * @author Oscar Andres Carriles <ocarriles@eolos.la>.
 *******************************************************************************/

package org.restcomm.sbc.adapter.impl;

import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;
import org.restcomm.sbc.ConfigurationCache;
import org.restcomm.sbc.adapter.TransportAdapter;

/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    28/4/2016 10:48:28
 * @class   UDPTransportAdapter.java
 * @project Servlet2.5SBC
 *
 */
public class UDPTransportAdapter implements TransportAdapter {
	
	private static transient Logger LOG = Logger.getLogger(UDPTransportAdapter.class);
	
	private SipFactory sipFactory;
	
	
	public UDPTransportAdapter() {
		this.sipFactory=ConfigurationCache.getSipFactory();
		
	}
	
	public SipServletMessage adapt(SipServletMessage message) {
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> adapt() Adapting transport [->udp]");
	    }
		String user = ((SipURI) message.getFrom().getURI()).getUser();
		
		SipURI sipUri = sipFactory.createSipURI(user, ConfigurationCache.getRouteMZIPAddress());
		
		sipUri.setTransportParam("udp");
		
		sipUri.setPort(ConfigurationCache.getRouteMZPort());
		
		if(message instanceof SipServletRequest)
			((SipServletRequest) message).setRequestURI(sipUri);
	
		return message;
	}

}
