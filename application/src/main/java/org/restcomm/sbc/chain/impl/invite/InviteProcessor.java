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

package org.restcomm.sbc.chain.impl.invite;


import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import org.apache.log4j.Logger;
import org.restcomm.chain.ProcessorChain;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.ProcessorCallBack;
import org.restcomm.chain.processor.impl.DefaultProcessor;
import org.restcomm.chain.processor.impl.ProcessorParsingException;
import org.restcomm.chain.processor.impl.SIPMutableMessage;






/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    15 oct. 2016 9:34:59
 * @class   InviteProcessor.java
 *
 */
public class InviteProcessor extends DefaultProcessor implements ProcessorCallBack {
	/**
	 * 		
	 */
	private static transient Logger LOG = Logger.getLogger(InviteProcessor.class);
	private String name="INVITE Processor";
	
	//private CallManager callManager=CallManager.getCallManager();
	
	
	public InviteProcessor(ProcessorChain chain) {
		super(chain);
		this.chain=chain;	
	}
	
	public InviteProcessor(String name, ProcessorChain chain) {
		this(chain);
		setName(name);
	}
	

	private void processInviteRequest(SIPMutableMessage message) {
		SipServletRequest request=(SipServletRequest) message.getContent();
		//MediaSession mediaSession=null;
		
		//SipServletRequest oRequest=(SipServletRequest) request.getSession().getAttribute(MessageUtil.B2BUA_ORIG_REQUEST_ATTR);
		/*
		try {
			mediaSession=(MediaSession) message.getMetadata();
			mediaSession.getOffer().patchIPAddressAndPort(message.getSourceLocalAddress());
			
			if(LOG.isTraceEnabled()){
		          LOG.trace("MEDIA-Session "+mediaSession);
		    }
			
			
			message.setMetadata(mediaSession);
			
		} catch (SdpException e1) {
			LOG.error("Unavailable media port ",e1);
		} 
		
	
		oRequest.getSession().setAttribute(MessageUtil.MEDIA_MANAGER, mediaSession);
		*/
		message.setContent(request);	
	}
	
	private void processInviteResponse(SIPMutableMessage message) {
		SipServletResponse response=(SipServletResponse) message.getContent();
		
		//String callSessionId=response.getRequest().getSession().getId();
		//Call call=callManager.getCall(callSessionId);
		
		if(response.getStatus()==SipServletResponse.SC_OK) {
			//MediaSession mediaSession=(MediaSession) response.getRequest().getSession().getAttribute(MessageUtil.MEDIA_MANAGER);
			
			/*
			try {
				
				mediaSession.getAnswer().patchIPAddressAndPort(message.getSourceLocalAddress());
				
			//	metadata.setRtpPort(audioZone.getRTPPort());
			//	metadata.setRtcpPort(audioZone.getRTCPPort());
				
				if(LOG.isTraceEnabled()){
			          LOG.trace("MEDIA-Session "+mediaSession);
			    }
				
				message.setMetadata(mediaSession);
			//	peerAudioZone.attach(audioZone);
			//	mediaSession.addMediaZone(audioZone);
				
				
			} catch (SdpException e) {
				LOG.error(message.getSourceLocalAddress(),e);
			}  
			
			response.getSession().setAttribute(MessageUtil.MEDIA_MANAGER, mediaSession);
			*/
			message.setContent(response);	
		}
		
	}
	
	private void processByeRequest(SIPMutableMessage message)  {
		
		
	}
	
	private void processAckRequest(SIPMutableMessage message)  {
		
	}
	
	private void processPrackRequest(SIPMutableMessage message)  {
		
	}
	
	private void processInfoRequest(SIPMutableMessage message)  {
		
	}
	
	private void processCancelRequest(SIPMutableMessage message)  {	
		
		
	}
	
	private void processByeResponse(SIPMutableMessage message)  {
		
	}
	
	private void processInfoResponse(SIPMutableMessage message)  {
		
		
	}
	
	
	private void processCancelResponse(SIPMutableMessage message)  {	
		
	}
	/*
	private String getCallSessionId(SipServletRequest currentRequest) {
		SipServletRequest oRequest=(SipServletRequest) currentRequest.getSession().getAttribute(MessageUtil.B2BUA_ORIG_REQUEST_ATTR);
		return oRequest.getSession().getId();
	}

*/

	public String getName() {
		return name;
	}

	
	public int getId() {
		return this.hashCode();
	}


	@Override
	public void setName(String name) {
		this.name=name;
		
	}



	@Override
	public ProcessorCallBack getCallback() {
		return this;
	}

	@Override
	public void doProcess(Message message) throws ProcessorParsingException {
		SIPMutableMessage m  =(SIPMutableMessage) message;
		
		SipServletMessage sm = m.getContent();
		
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> doProcess()");
	    }
		
		if(sm instanceof SipServletRequest) {
			if(sm.getMethod().equals("INVITE"))
				processInviteRequest(m);
			else if(sm.getMethod().equals("BYE"))
				processByeRequest(m);
			else if(sm.getMethod().equals("ACK"))
				processAckRequest(m);
			else if(sm.getMethod().equals("PRACK"))
				processPrackRequest(m);
			else if(sm.getMethod().equals("CANCEL"))
				processCancelRequest(m);
			else if(sm.getMethod().equals("INFO"))
				processInfoRequest(m);
			else {
				if(LOG.isDebugEnabled()) {
					LOG.debug("Request METHOD "+sm.getMethod()+" not supported!");
				}
			}
		}
		if(sm instanceof SipServletResponse) {
			if(sm.getMethod().equals("INVITE"))
				processInviteResponse(m);
			else if(sm.getMethod().equals("BYE"))
				processByeResponse(m);
			else if(sm.getMethod().equals("CANCEL"))
				processCancelResponse(m);
			else if(sm.getMethod().equals("INFO"))
				processInfoResponse(m);
			else {
				if(LOG.isDebugEnabled()) {
					LOG.debug("Response METHOD "+sm.getMethod()+" not supported!");
				}
			}
		}
		
	}
	
	@Override
	public String getVersion() {
		return "1.0.0";
	}

}
