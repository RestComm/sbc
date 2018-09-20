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

package org.restcomm.sbc.servlet.sip;

import java.io.IOException;
import java.nio.channels.DatagramChannel;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipApplicationSessionEvent;
import javax.servlet.sip.SipApplicationSessionListener;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;
import org.restcomm.chain.processor.impl.SIPMutableMessage;
import org.restcomm.sbc.bo.shiro.ShiroResources;
import org.restcomm.sbc.call.Call;
import org.restcomm.sbc.chain.impl.invite.DownstreamInviteProcessorChain;
import org.restcomm.sbc.chain.impl.invite.UpstreamInviteProcessorChain;

import org.restcomm.sbc.call.CallManager;
import org.restcomm.sbc.call.CallStateChanged;
import org.restcomm.sbc.managers.MessageUtil;
import org.restcomm.sbc.managers.RouteManager;
import org.restcomm.sbc.media.MediaController;
import org.restcomm.sbc.media.MediaSession;
import org.restcomm.sbc.media.MediaZone;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    7 sept. 2016 8:38:31
 * @class   SBCCallServlet.java
 *
 */
public class SBCCallServlet extends SipServlet implements SipApplicationSessionListener {	
	private static final long serialVersionUID = 1L;	
	
	
	private static transient Logger LOG = Logger.getLogger(SBCCallServlet.class);
	
	private UpstreamInviteProcessorChain upChain;
	private CallManager callManager;
	private DownstreamInviteProcessorChain dwChain;
	
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		
		
		callManager = (CallManager) ShiroResources.getInstance().get(CallManager.class);

		LOG.info("Call sip servlet has been started");
		
		if(LOG.isInfoEnabled()){
	          LOG.info(">> Call Servlet init()");
	    }
			
		upChain=new UpstreamInviteProcessorChain();
		LOG.info("Loading (v. "+upChain.getVersion()+") "+upChain.getName());
		dwChain=new DownstreamInviteProcessorChain();
		LOG.info("Loading (v. "+dwChain.getVersion()+") "+dwChain.getName());	
		
		
	}
	
	
	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,	IOException {
		
		String direction="outbound";
		if(RouteManager.isFromDMZ(request)) {
			direction="inbound";
		}
		if(LOG.isTraceEnabled()) {	
			LOG.trace("CALL REQUEST DMZ:"+direction);	
			LOG.trace("CALL REQUEST SES:"+request.getSession());	
		}
		
		SipURI fromURI 	= (SipURI) request.getFrom().getURI();
		SipURI toURI 	= (SipURI) request.getTo().  getURI();
		String displayName=request.getFrom().getDisplayName();
		
		Call call=callManager.getCall(request.getSession().getId());
		/*
		 * By now reuse the call in a Re-INVITE
		 */
		
		call=callManager.createCall(
				call,
				request.getSession().getId(),
				toURI.getUser(),
				fromURI.getUser(),
				direction,
				null,
				displayName==null?"Unknown":displayName);
			
		
		// Controls expiration time of this leg
		request.getApplicationSession().setExpires(0);
		
		request.getSession().setAttribute(MessageUtil.CALL_MANAGER, call);
		
		upChain. process(new SIPMutableMessage(request));	
		
	}
	
	@Override
	protected void doInfo(SipServletRequest request) throws ServletException, IOException {
							
		if(LOG.isTraceEnabled()) {	
			LOG.trace("INFO REQUEST DMZ:"+RouteManager.isFromDMZ(request));	
		}
		
		upChain. process(new SIPMutableMessage(request));
		
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doResponse(SipServletResponse response) throws ServletException, IOException {
		
		// dismissing
		/*
		if(response.getStatus()==SipServletResponse.SC_TRYING) {
			return;
		}
		*/
		String callSessionId=getCallSessionId(response.getRequest());
		
		if(response.getStatus()==SipServletResponse.SC_RINGING) {
			callManager.changeCallStatus(callSessionId, CallStateChanged.State.RINGING);
			response.setStatus(SipServletResponse.SC_SESSION_PROGRESS, "Session Progress");
			if (LOG.isTraceEnabled()) {
				LOG.trace("180 Detected->183");
			}

		}
		
		if(response.getMethod().equals("INVITE")) {
			if(response.getStatus()>SipServletResponse.SC_OK) {
				Call call=callManager.getCall(callSessionId);
				if(call!=null) {
					// Completed in the other leg?
					callManager.changeCallStatus(callSessionId, response.getStatus(), response.getReasonPhrase());
						
				}
				else {
					callSessionId=response.getRequest().getSession().getId();	
					call=callManager.getCall(callSessionId);
					
					if(call!=null) {
						// Completed in the other leg?
						callManager.changeCallStatus(callSessionId, response.getStatus(), response.getReasonPhrase());
					
						
					}
				}
			
			}
			
		}
		
		try {
			dwChain.process(new SIPMutableMessage(response));
		} catch (IllegalStateException e) {	
			LOG.warn("===========\nNot forwarding message:\n"+response.toString());
			LOG.warn("===========\nNot forwarded  message===");
			return;
		}
		//super.doResponse(response);
		
	}
	
	
	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected void doBye(SipServletRequest request) throws ServletException, IOException {
		if(LOG.isDebugEnabled()) {
			LOG.debug("CALL BYE SES:"+request.getSession());	
			LOG.debug("Got Request BYE: "	+ request.getMethod()+" State:"+request.getSession().getState().toString());
			LOG.debug("RTP Session might end");				
		}
		//SipServletResponse response = request.createResponse(SipServletResponse.SC_OK);
		//response.send();
		
		String callSessionId=getCallSessionId(request);
				
		Call call=null;
		if(callSessionId!=null)
			call=callManager.getCall(callSessionId);
		if(call!=null) {
			callManager.changeCallStatus(callSessionId, CallStateChanged.State.COMPLETED);
				
		}
		else {
			callSessionId=request.getSession().getId();	
			call=callManager.getCall(callSessionId);				
			if(call!=null) {
				callManager.changeCallStatus(callSessionId, CallStateChanged.State.COMPLETED);
								
			}
		}
				
		try {
			upChain.process(new SIPMutableMessage(request));
		} catch (IllegalStateException e) {
			LOG.warn("===========\nNot forwarding message:\n"+request.toString());
			LOG.warn("===========\nNot forwarded  message===");
			
		}	
		
		
				
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected void doAck(SipServletRequest request) throws ServletException, IOException {
		String callSessionId=request.getSession().getId();	
		callManager.changeCallStatus(callSessionId, CallStateChanged.State.IN_PROGRESS);
		
		
		if(LOG.isDebugEnabled()) {
			MediaSession mediaSession=callManager.getCall(callSessionId).getMediaSession();
			MediaController mediaController=mediaSession.getOffer();
			MediaZone mediaZone= mediaController.getMediaZone(MediaController.MEDIATYPE_AUDIO);
			DatagramChannel offerChannel = mediaZone.getChannel();
			DatagramChannel answerChannel = mediaZone.getMediaZonePeer().getChannel();
			LOG.debug("CALL ACK SES:"+request.getSession());	
			LOG.debug("Got Request ACK: "	+ request.getMethod()+" State:"+request.getSession().getState().toString());
			LOG.debug("RTP Session might start");	
			LOG.debug("Expecting (offer)  audio stream "+offerChannel.getRemoteAddress()+"===>"+offerChannel.getLocalAddress());
			LOG.debug("Expecting (answer) audio stream "+answerChannel.getRemoteAddress()+"===>"+answerChannel.getLocalAddress());
			
		}
		// Get control of session expiration
			
		
			
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected void doPrack(SipServletRequest request) throws ServletException, IOException {
		if(LOG.isDebugEnabled()) {
			LOG.debug("CALL PRACK SES:"+request.getSession());	
			LOG.debug("Got Request PRACK: "	+ request.getMethod()+" State:"+request.getSession().getState().toString());
					
		}
		//SipServletResponse response = request.createResponse(SipServletResponse.SC_OK);
		//response.send();
		upChain.process(new SIPMutableMessage(request));

	}
	
	@Override
	protected void doCancel(SipServletRequest request) throws ServletException,
			IOException {
		if(LOG.isDebugEnabled()) {
			LOG.debug("CALL CANCEL SES:"+request.getSession());	
			LOG.debug("Got Request CANCEL: "	+ request.getMethod()+" State:"+request.getSession().getState().toString());
			LOG.debug("RTP Session might end");				
		}
		
		//SipServletResponse response = request.createResponse(SipServletResponse.SC_OK);
		//response.send();
		//response = request.createResponse(SipServletResponse.SC_REQUEST_TERMINATED);
		//response.send();
			
		upChain.process(new SIPMutableMessage(request));
		
		String callSessionId=request.getSession().getId();	
		
		callManager.changeCallStatus(callSessionId, CallStateChanged.State.COMPLETED);	
		
	}
	
	private String getCallSessionId(SipServletRequest currentRequest) {
		if(!currentRequest.getSession().isValid())
			return null;
		SipServletRequest oRequest=(SipServletRequest) currentRequest.getSession().getAttribute(MessageUtil.B2BUA_ORIG_REQUEST_ATTR);
		return oRequest.getSession().getId();
	}


	@Override
	public void sessionCreated(SipApplicationSessionEvent ev) {
		LOG.info("Session Created ");	
		
	}


	@Override
	public void sessionDestroyed(SipApplicationSessionEvent ev) {
		LOG.info("Session Destroyed");	
		
	}


	@Override
	public void sessionExpired(SipApplicationSessionEvent ev) {
		LOG.info("Session Expired ");	
		
	}


	@Override
	public void sessionReadyToInvalidate(SipApplicationSessionEvent ev) {
		LOG.info("Session Ready to invalidate ");	
		
	}


}
