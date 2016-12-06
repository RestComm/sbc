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
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import org.apache.log4j.Logger;
import org.mobicents.media.server.io.sdp.SdpException;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.impl.SIPMutableMessage;
import org.restcomm.sbc.media.MediaController;
import org.restcomm.sbc.media.MediaSession;
import org.restcomm.sbc.managers.MessageUtil;





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
	
	public abstract String getProtocol();
	
	public Message adaptMedia(Message message) {
				SIPMutableMessage m=(SIPMutableMessage) message;
				SipServletMessage sm=m.getContent();
				MediaSession mediaSession;
				MediaController mediaController;
				
				
				if (sm.getContentLength() > 0 && sm.getContentType().equalsIgnoreCase("application/sdp")) {
					try {
						
						String host=message.getTargetLocalAddress();
						
						if(sm instanceof SipServletResponse) {
							SipServletResponse smr=(SipServletResponse) sm;
							mediaSession=(MediaSession) m.getMetadata();
							mediaController=mediaSession.getAnswer();
							mediaSession.attach();
							mediaController.setLocalProxy(host, false);
							
							//audioZone=(MediaZone) smr.getRequest().getSession().getAttribute(MessageUtil.MEDIA_MANAGER);	
							//mediaSession.getAnswer();
							//audioPort=audioZone.getMediaZonePeer().getRTPPort();
							//audioControlPort=audioZone.getMediaZonePeer().getRTCPPort();
							
						}
						else {
							SipServletRequest smr=(SipServletRequest) sm;
							SipServletRequest oRequest=(SipServletRequest) smr.getSession().getAttribute(MessageUtil.B2BUA_ORIG_REQUEST_ATTR);
							
							mediaSession=(MediaSession) m.getMetadata();	
							mediaController=mediaSession.getOffer();
							mediaController.setLocalProxy(host, true);
							//audioZone=(MediaZone) oRequest.getSession().getAttribute(MessageUtil.MEDIA_MANAGER);
							//audioPort=audioZone.getRTPPort();
							//audioControlPort=audioZone.getRTCPPort();
							
						}
						
							
						
							
							String sdpContent=mediaController.getSdp().toString();
							
							
							if(m.getTarget()==Message.TARGET_MZ) {
								//always stream plain media to MZ	
								sdpContent=mediaController.unSecureSdp().toString();
								
								
							}
							else if(mediaController.isSecure()) {
								// must reply in secure mode
							    sdpContent=mediaController.secureSdp().toString();		
							}
							
							sdpContent=mediaController.patchIPAddressAndPort(host).toString();
						
						
						m.setMetadata(mediaSession);
						
						if (LOG.isDebugEnabled()) {
							LOG.debug("MDA "+m.getMetadata());
							LOG.debug(m.toString());
							
							LOG.debug("patched Content:\n" + sdpContent);
						}
						
						
						sm.setContent(sdpContent, "application/sdp");
						
						m.setContent(sm);
						

					} catch (IOException e) {
						LOG.error("No SDP content!", e);
						return m;
					
					} catch (SdpException e) {
						LOG.error("MediaMetadata invalid!",e);
						return m;
					} 

				}
			
		return m;
	}
	

}
