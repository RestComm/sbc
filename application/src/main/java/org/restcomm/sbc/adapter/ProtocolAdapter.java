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

package org.restcomm.sbc.adapter;

import java.io.IOException;
import java.net.NoRouteToHostException;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletResponse;
import org.apache.log4j.Logger;
import org.mobicents.media.server.io.sdp.SdpException;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.impl.SIPMutableMessage;
import org.restcomm.sbc.media.MediaController;
import org.restcomm.sbc.media.MediaSession;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    30 ago. 2016 13:27:49
 * @class   ProtocolAdapter.java
 *
 */
public abstract class ProtocolAdapter {
	private static transient Logger LOG = Logger.getLogger(ProtocolAdapter.class);
	/**
	 * TransportAdapter must be implemented for those transport
	 * specialized convertors to forward messages between them-
	 */
	
	/**
	 * Message adaptation service
	 * @param message
	 * @return adapted message to target transport
	 */
	public abstract Message adapt(Message message) throws NoRouteToHostException;
	
	/**
	 * Message adaptation service
	 * @param message
	 * @return adapted sdp to target transport
	 */
	protected abstract String adaptSdp(MediaController mediaController, String host) throws SdpException;
	
	public abstract String getProtocol();
	
	public Message adaptMedia(Message message) {
		SIPMutableMessage m = (SIPMutableMessage) message;
		SipServletMessage sm = m.getContent();
		MediaSession mediaSession;
		MediaController mediaController;

		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug(sm.getMethod() + " adapting SDP");
			}
			String host = message.getTargetLocalAddress();
			mediaSession = (MediaSession) m.getMetadata();
			
			if (sm instanceof SipServletResponse) {	
				mediaController = mediaSession.getAnswer();
				mediaSession.attach();
			} else {
				mediaController = mediaSession.getOffer();
			}

			String sdpContent = mediaController.getProxySdp(host);
			sdpContent = adaptSdp(mediaController, host);
/*
			if (m.getTarget() == Message.TARGET_MZ) {
				// always stream plain media to MZ
				if (ConfigurationCache.isMediaDecodingEnabled()) {
					sdpContent = mediaController.getUnsecureProxySdp(host);
				}
			} else if (getProtocol().equals(ProtocolAdapterFactory.PROTOCOL_WSS)) {
				// The answer is build upon the previous offer
				if (!mediaController.isOffer()) {
					// must reply in secure mode
					sdpContent = mediaController.getSecureProxySdp(host);
				}
			}
*/
			m.setMetadata(mediaSession);

			if (LOG.isDebugEnabled()) {
				LOG.debug("MDA " + m.getMetadata());
				LOG.debug(m.toString());
				LOG.debug("patched Content:\n" + sdpContent);
			}

			sm.setContent(sdpContent, "application/sdp");
			m.setContent(sm);

		} catch (IOException e) {
			LOG.error("No SDP content!", e);
			return m;

		} catch (SdpException e) {
			LOG.error("MediaMetadata invalid!", e);
			return m;
		}

		return m;
	}
	

}
