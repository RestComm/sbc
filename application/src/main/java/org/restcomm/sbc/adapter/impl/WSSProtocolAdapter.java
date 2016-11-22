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

import java.net.NoRouteToHostException;
import javax.servlet.sip.SipServletMessage;
import org.apache.log4j.Logger;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.impl.SIPMutableMessage;
import org.restcomm.sbc.adapter.ProtocolAdapter;
import org.restcomm.sbc.managers.ProtocolAdapterFactory;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    14 nov. 2016 12:59:59
 * @class   WSSProtocolAdapter.java
 *
 */
public class WSSProtocolAdapter extends ProtocolAdapter {
	
	private static transient Logger LOG = Logger.getLogger(WSSProtocolAdapter.class);
	
	
	public WSSProtocolAdapter() {
		
	}
	
	public Message adapt(Message message) throws NoRouteToHostException {
		SIPMutableMessage m=(SIPMutableMessage) message;
		SipServletMessage sm=m.getContent();
		
		String sourceTransport=sm.getInitialTransport();
		if(sourceTransport==null) {
			sourceTransport=ProtocolAdapterFactory.PROTOCOL_WSS;
		}
		if(LOG.isTraceEnabled()) {
			LOG.trace("o Transport "+sourceTransport);
			//LOG.trace("o Message follows:\n"+message.toString());
			LOG.trace(">> adapt() Adapting protocol [->WSS]");
		}
		adaptMedia(message);
		
		return m;
	}

	@Override
	public String getProtocol() {
		return ProtocolAdapterFactory.PROTOCOL_WSS;
	}

}
