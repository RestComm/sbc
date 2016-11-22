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
import org.restcomm.sbc.media.CryptoMediaZone;
import org.restcomm.sbc.media.MediaMetadata;
import org.restcomm.sbc.media.MediaZone;
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
				MediaZone audioZone;
				int audioPort;
				int audioControlPort;
				
				if (sm.getContentLength() > 0 && sm.getContentType().equalsIgnoreCase("application/sdp")) {
					try {
						
						if(sm instanceof SipServletResponse) {
							SipServletResponse smr=(SipServletResponse) sm;
							audioZone=(MediaZone) smr.getRequest().getSession().getAttribute(MessageUtil.MEDIA_MANAGER);	
							audioPort=audioZone.getMediaZonePeer().getRTPPort();
							audioControlPort=audioZone.getMediaZonePeer().getRTCPPort();
							
						}
						else {
							SipServletRequest smr=(SipServletRequest) sm;
							SipServletRequest oRequest=(SipServletRequest) smr.getSession().getAttribute(MessageUtil.B2BUA_ORIG_REQUEST_ATTR);
							audioZone=(MediaZone) oRequest.getSession().getAttribute(MessageUtil.MEDIA_MANAGER);
							audioPort=audioZone.getRTPPort();
							audioControlPort=audioZone.getRTCPPort();
							
						}
						
						String host=message.getTargetLocalAddress();
						
						MediaMetadata metadata=MediaMetadata.build(MediaMetadata.MEDIATYPE_AUDIO, new String(sm.getRawContent()));
						
						metadata.setRtpPort(audioPort);
						metadata.setRtcpPort(audioControlPort);
						metadata.setIp(host);
						
						String sdpContent=metadata.getSdp().toString();
						
						
						if(m.getTarget()==Message.TARGET_MZ) {
							//always stream plain media to MZ	
							sdpContent=metadata.unSecureSdp().toString();
							
							
						}
						else if(audioZone instanceof CryptoMediaZone) {
							// must reply in secure mode
						    sdpContent=metadata.secureSdp().toString();		
						}
						
						sdpContent=metadata.patch(sdpContent).toString();
						
						
						m.setMetadata(metadata);
						
						if (LOG.isDebugEnabled()) {
							LOG.debug("MDA "+m.getMetadata());
							LOG.debug(m.toString());
							
							LOG.debug("Audio port " + audioZone.getRTPPort());
							if(audioZone.getMediaZonePeer()!=null)
								LOG.debug("Audio peer port " + audioZone.getMediaZonePeer().getRTPPort());
							LOG.debug("patched Content:\n" + sdpContent);
						}
						
						
						sm.setContent(sdpContent, "application/sdp");
						
						m.setContent(sm);
						

					} catch (IOException e) {
						LOG.error("No SDP content!", e);
						return m;
					
					} catch (RuntimeException e) {
						LOG.error("State SDP treatment", e);
						return m;
					} catch (org.mobicents.media.server.io.sdp.SdpException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 

				}
			
		return m;
	}
	

}
