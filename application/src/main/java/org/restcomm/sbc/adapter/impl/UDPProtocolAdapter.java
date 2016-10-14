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

import java.io.IOException;
import java.net.NoRouteToHostException;

import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletMessage;
import org.apache.log4j.Logger;
import org.restcomm.sbc.ConfigurationCache;
import org.restcomm.sbc.adapter.ProtocolAdapter;
import org.restcomm.sbc.managers.MessageUtil;
import org.restcomm.sbc.managers.ProtocolAdapterFactory;
import org.restcomm.sbc.managers.SdpUtils;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    30 ago. 2016 13:30:19
 * @class   UDPProtocolAdapter.java
 *
 */
public class UDPProtocolAdapter implements ProtocolAdapter {
	
	private static transient Logger LOG = Logger.getLogger(UDPProtocolAdapter.class);
	
	private SipFactory sipFactory;
	
	
	public UDPProtocolAdapter() {
		this.sipFactory=ConfigurationCache.getSipFactory();
		
	}
	
	public SipServletMessage adapt(SipServletMessage message) throws NoRouteToHostException {
		String sourceTransport=message.getInitialTransport();
		if(sourceTransport==null) {
			sourceTransport=ProtocolAdapterFactory.PROTOCOL_UDP;
		}
		if(LOG.isTraceEnabled()) {
			LOG.trace("o Contact "+message.getHeader("Contact"));
			LOG.trace("o Transport "+sourceTransport);
			//LOG.trace("o Message follows:\n"+message.toString());
			LOG.trace(">> adapt() Adapting protocol [->UDP]");
		}
		//SipServletRequest  oRequest = (SipServletRequest) message.getSession().getAttribute(MessageUtil.B2BUA_ORIG_REQUEST_ATTR);
		//String oTransport=oRequest.getTransport();
		/*
		if (message.getMethod().equals("INVITE") && message.getContentLength() > 0) {
			try {
				String sdpContent = SdpUtils.patch("application/sdp", message.getRawContent(),
						message.getRemoteAddr());
				sdpContent = SdpUtils.endWithNewLine(sdpContent);

				if (LOG.isDebugEnabled()) {
					LOG.debug("Session In state " + message.getSession().getState().name());
					LOG.debug("Content:\n" + sdpContent);
				}
				message.setContent(sdpContent, "application/sdp");

			} catch (IOException e) {
				LOG.error("No SDP content!", e);
				return message;
			} catch (SdpParseException e) {
				LOG.error("Bad SDP", e);
				return message;
			} catch (SdpException e) {
				LOG.error("Bad SDP treatment", e);
				return message;
			} catch (IllegalStateException e) {
				LOG.error("State SDP treatment", e);
				return message;
			}

		}
		*/
		return message;
	}

	@Override
	public String getProtocol() {
		return ProtocolAdapterFactory.PROTOCOL_UDP;
	}

}
