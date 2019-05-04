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
import org.mobicents.media.server.io.sdp.SdpException;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.impl.SIPMutableMessage;
import org.restcomm.sbc.adapter.ProtocolAdapter;
import org.restcomm.sbc.managers.ProtocolAdapterFactory;
import org.restcomm.sbc.media.MediaController;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    30 ago. 2016 13:30:19
 * @class   UDPProtocolAdapter.java
 *
 */
public class UDPProtocolAdapter extends ProtocolAdapter {
	
	private static transient Logger LOG = Logger.getLogger(UDPProtocolAdapter.class);
	private SIPMutableMessage m;
	
	public UDPProtocolAdapter() {
		
	}
	
	public Message adapt(Message message) throws NoRouteToHostException {
		m=(SIPMutableMessage) message;
		SipServletMessage sm=m.getContent();
		
		String sourceTransport=m.getSourceTransport();
		if(LOG.isTraceEnabled()) {
			LOG.trace(">> adapt() Adapting protocol ["+sourceTransport+"->"+getProtocol()+"]");
		}
		if (sm.getContentLength() > 0 && sm.getContentType().equalsIgnoreCase("application/sdp")) {
			message=adaptMedia(message);
			
		}
		
		return m;
	}
	

	@Override
	public String getProtocol() {
		return ProtocolAdapterFactory.PROTOCOL_UDP;
	}

	@Override
	protected String adaptSdp(MediaController mediaController, String host) throws SdpException {

		String sdpContent;
		if(mediaController.isSecure()) {
			sdpContent = mediaController.getSAVPProxySdp(host);
		}
		sdpContent= mediaController.getAVPProxySdp(host);
			
		return sdpContent;

	}

}
