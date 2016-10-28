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
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;

import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.impl.SIPMutableMessage;
import org.restcomm.sbc.media.MediaManager;
import org.restcomm.sbc.managers.MessageUtil;
import org.restcomm.sbc.managers.SdpUtils;




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
	public abstract void adapt(Message message) throws NoRouteToHostException;
	
	public abstract String getProtocol();
	
	public void adaptMedia(Message message) {
				SIPMutableMessage m=(SIPMutableMessage) message;
				SipServletMessage sm=m.getContent();
				MediaManager audioManager;
				int audioPort;
				
				if (sm.getContentLength() > 0 && sm.getContentType().equalsIgnoreCase("application/sdp")) {
					try {
						
						if(sm instanceof SipServletResponse) {
							SipServletResponse smr=(SipServletResponse) sm;
							audioManager=(MediaManager) smr.getRequest().getSession().getAttribute(MessageUtil.MEDIA_MANAGER);	
							audioPort=audioManager.getMediaManagerPeer().getPort();
							
						}
						else {
							SipServletRequest smr=(SipServletRequest) sm;
							SipServletRequest oRequest=(SipServletRequest) smr.getSession().getAttribute(MessageUtil.B2BUA_ORIG_REQUEST_ATTR);
							audioManager=(MediaManager) oRequest.getSession().getAttribute(MessageUtil.MEDIA_MANAGER);
							audioPort=audioManager.getPort();
							
						}
						
						String host=message.getTargetLocalAddress();
						
						
						String sdpContent = SdpUtils.patch("application/sdp", sm.getRawContent(),
								host);
						sdpContent = SdpUtils.fix("audio", audioPort, sdpContent);
						sdpContent = SdpUtils.endWithNewLine(sdpContent);
						
						if (LOG.isDebugEnabled()) {
							LOG.debug("<=> "+m.getDirection());
							LOG.debug("SLA "+m.getSourceLocalAddress());
							LOG.debug("SRA "+m.getSourceRemoteAddress());
							LOG.debug("TLA "+m.getTargetLocalAddress());
							LOG.debug("TRA "+m.getTargetRemoteAddress());
							
							LOG.debug("Audio port " + audioManager.getPort());
							if(audioManager.getMediaManagerPeer()!=null)
								LOG.debug("Audio peer port " + audioManager.getMediaManagerPeer().getPort());
							LOG.debug("patched Content:\n" + sdpContent);
						}
						
						sm.setContent(sdpContent, "application/sdp");
						

					} catch (IOException e) {
						LOG.error("No SDP content!", e);
						return;
					} catch (SdpParseException e) {
						LOG.error("Bad SDP", e);
						return;
					} catch (SdpException e) {
						LOG.error("Bad SDP treatment", e);
						return;
					} catch (RuntimeException e) {
						LOG.error("State SDP treatment", e);
						return;
					} 

				}
			
		
	}
	

}
