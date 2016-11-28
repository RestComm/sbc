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
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;
import org.restcomm.chain.processor.impl.SIPMutableMessage;
import org.restcomm.sbc.bo.Call;
import org.restcomm.sbc.chain.impl.invite.DownstreamInviteProcessorChain;
import org.restcomm.sbc.chain.impl.invite.UpstreamInviteProcessorChain;
import org.restcomm.sbc.media.MediaZone;
import org.restcomm.sbc.media.MediaSession;
import org.restcomm.sbc.media.MediaSessionListener;
import org.restcomm.sbc.managers.CallManager;
import org.restcomm.sbc.managers.MessageUtil;
import org.restcomm.sbc.managers.RouteManager;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    7 sept. 2016 8:38:31
 * @class   SBCCallServlet.java
 *
 */
public class SBCCallServlet extends SipServlet implements MediaSessionListener {	
	private static final long serialVersionUID = 1L;	
	
	
	private static transient Logger LOG = Logger.getLogger(SBCCallServlet.class);
	
	private UpstreamInviteProcessorChain upChain;
	private CallManager callManager;
	private DownstreamInviteProcessorChain dwChain;
	
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		
		callManager=CallManager.getCallManager();
		
		LOG.info("Call sip servlet has been started");
		
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> init()");
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
		
		Call call=callManager.createCall(
				request.getSession().getId(),
				toURI.getUser(),
				fromURI.getUser(),
				direction,
				null,
				request.getFrom().getDisplayName());
		
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
		if(response.getStatus()==SipServletResponse.SC_TRYING) {
			return;
		}
		String callSessionId=getCallSessionId(response.getRequest());
		Call call=callManager.getCall(callSessionId);
		
		if(response.getStatus()==SipServletResponse.SC_RINGING) {
			call.setStatus(Call.Status.RINGING);	
		}
		
		else if(response.getStatus()>=SipServletResponse.SC_OK) {
			if(call!=null) {
				// Completed in the other leg?
				call.setStatus(response.getStatus(), response.getReasonPhrase());
				call.getMediaSession().stop();		
			}
			else {
				callSessionId=response.getRequest().getSession().getId();	
				call=callManager.getCall(callSessionId);
				
				if(call!=null) {
					// Completed in the other leg?
					call.setStatus(response.getStatus(), response.getReasonPhrase());
					call.getMediaSession().stop();
					
				}
			}
		
		}
		
		try {
			dwChain.process(new SIPMutableMessage(response));
		} catch (IllegalStateException e) {
			LOG.warn(e.getMessage()+" not forwarding message");
			return;
		}
		super.doResponse(response);
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
		SipServletResponse response = request.createResponse(SipServletResponse.SC_OK);
		response.send();
					
		MediaZone mediaZone=(MediaZone) request.getSession().getAttribute(MessageUtil.MEDIA_MANAGER);
		
		String callSessionId=getCallSessionId(request);	
		Call call=callManager.getCall(callSessionId);
		
		/*if(call!=null) {
			// Completed in the other leg?
			call.setStatus(Call.Status.COMPLETED);
			call.getMediaSession().stop();
			
		}
		else {
			callSessionId=request.getSession().getId();	
			call=callManager.getCall(callSessionId);
			
			if(call!=null) {
				// Completed in the other leg?
				call.setStatus(Call.Status.COMPLETED);
				call.getMediaSession().stop();
				
			}
		}
		*/
		//if(mediaZone!=null)
		//	mediaZone.finalize();
		//else 
		//	LOG.warn("NULL MediaZone on BYE :(");
		
		try {
			upChain.process(new SIPMutableMessage(request));
		} catch (IllegalStateException e) {
			LOG.warn(e.getMessage()+" not forwarding message");
		}	
		
		
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected void doAck(SipServletRequest request) throws ServletException, IOException {
		if(LOG.isDebugEnabled()) {
			LOG.debug("CALL ACK SES:"+request.getSession());	
			LOG.debug("Got Request ACK: "	+ request.getMethod()+" State:"+request.getSession().getState().toString());
			LOG.debug("RTP Session might start");				
		}
		
		String callSessionId=request.getSession().getId();	
		Call call=callManager.getCall(callSessionId);
		call.setStatus(Call.Status.BRIDGED);	
		
		MediaSession mediaSession=call.getMediaSession();
		mediaSession.addMediaSessionListener(this);
		mediaSession.start();
		
		//MediaZone mediaZone=(MediaZone) request.getSession().getAttribute(MessageUtil.MEDIA_MANAGER);	
		//mediaZone.addMediaZoneListener(this);
		//mediaZone.start();
		

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
		SipServletResponse response = request.createResponse(SipServletResponse.SC_OK);
		response.send();
		response = request.createResponse(SipServletResponse.SC_REQUEST_TERMINATED);
		response.send();
			
		upChain.process(new SIPMutableMessage(request));
		
		String callSessionId=request.getSession().getId();	
		Call call=callManager.getCall(callSessionId);
		call.setStatus(Call.Status.COMPLETED);	
		
		call.getMediaSession().stop();
		//MediaZone mediaZone=(MediaZone) request.getSession().getAttribute(MessageUtil.MEDIA_MANAGER);
		
		//mediaZone.finalize();
		
		
	}
	
	private String getCallSessionId(SipServletRequest currentRequest) {
		SipServletRequest oRequest=(SipServletRequest) currentRequest.getSession().getAttribute(MessageUtil.B2BUA_ORIG_REQUEST_ATTR);
		return oRequest.getSession().getId();
	}


	@Override
	public void onRTPTimeout(MediaSession session, MediaZone zone, String message) {
		if(LOG.isInfoEnabled())
			LOG.info("RTP Flow stuck on mediaType "+zone.getMediaType()+":"+message);
		/*
		SipServletRequest invite = ConfigurationCache.getSipFactory().createRequest(resp
				.getApplicationSession(), "INVITE", session
				.getRemoteParty(), secondPartyAddress);
		
		try {
			upChain. process(new SIPMutableMessage(callRequest.createCancel()));
		} catch (IOException e) {
			LOG.error("Cannot CANCEL on Media Timeout!");
		}
	*/
		
	}

}
