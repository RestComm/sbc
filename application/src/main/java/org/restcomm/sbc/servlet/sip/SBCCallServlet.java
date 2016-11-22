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
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.restcomm.chain.processor.impl.SIPMutableMessage;
import org.restcomm.sbc.ConfigurationCache;
import org.restcomm.sbc.chain.impl.invite.DownstreamInviteProcessorChain;
import org.restcomm.sbc.chain.impl.invite.UpstreamInviteProcessorChain;
import org.restcomm.sbc.media.MediaZone;
import org.restcomm.sbc.managers.MessageUtil;
import org.restcomm.sbc.managers.RouteManager;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    7 sept. 2016 8:38:31
 * @class   SBCCallServlet.java
 *
 */
public class SBCCallServlet extends SipServlet {	
	private static final long serialVersionUID = 1L;	
	
	private Configuration configuration;
	private SipFactory sipFactory;	
	
	private String routeMZIPAddress;
	
	private static transient Logger LOG = Logger.getLogger(SBCCallServlet.class);
	
	private UpstreamInviteProcessorChain upChain;
	
	private DownstreamInviteProcessorChain dwChain;
	private boolean callStablished=false;
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		
		LOG.info("Call sip servlet has been started");
		
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> init()");
	    }
		super.init(servletConfig);
		sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
		final ServletContext context = servletConfig.getServletContext();
		configuration=(Configuration) context.getAttribute(Configuration.class.getName());
		ConfigurationCache.build(sipFactory, configuration);
		
	      routeMZIPAddress=ConfigurationCache.getTargetHost();
		  
		
		if(LOG.isDebugEnabled()){
			
			LOG.debug("Route MZ Target:"+routeMZIPAddress);
			LOG.debug("Registration Throttling enabled:"+ConfigurationCache.isRegThrottleEnabled());
			LOG.debug("UATTL:"+ConfigurationCache.getRegThrottleUATTL());
			LOG.debug("MZTTL:"+ConfigurationCache.getRegThrottleMZTTL());
	    }
		super.init(servletConfig);
		
		upChain=new UpstreamInviteProcessorChain();
		LOG.info("Loading (v. "+upChain.getVersion()+") "+upChain.getName());
		dwChain=new DownstreamInviteProcessorChain();
		LOG.info("Loading (v. "+dwChain.getVersion()+") "+dwChain.getName());
		
		
		
	}
	
	
	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {
							
		if(LOG.isTraceEnabled()) {	
			LOG.trace("CALL REQUEST DMZ:"+RouteManager.isFromDMZ(request));	
			LOG.trace("CALL REQUEST SES:"+request.getSession());	
		}
		
		upChain. process(new SIPMutableMessage(request));
		
		
		
	}
	
	@Override
	protected void doInfo(SipServletRequest request) throws ServletException,
			IOException {
							
		if(LOG.isTraceEnabled()) {	
			LOG.trace("INFO REQUEST DMZ:"+RouteManager.isFromDMZ(request));	
		}
		
		upChain. process(new SIPMutableMessage(request));
		
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doResponse(SipServletResponse response)
			throws ServletException, IOException {
		
		// dismissing
		if(response.getStatus()==SipServletResponse.SC_TRYING) {
			return;
		}
		/*
		if(response.getStatus()==SipServletResponse.SC_RINGING) {
			response.setStatus(SipServletResponse.SC_SESSION_PROGRESS);
		}
		*/
		dwChain.process(new SIPMutableMessage(response));
		super.doResponse(response);
	}
	
	
	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		if(LOG.isTraceEnabled()) {
			LOG.trace("Bye Request "+request.getMethod());
		}
		SipServletResponse response = request.createResponse(SipServletResponse.SC_OK);
		response.send();
		
		upChain.process(new SIPMutableMessage(request));
		
		try {
			MediaZone mediaZone=(MediaZone) request.getSession().getAttribute(MessageUtil.MEDIA_MANAGER);
			mediaZone.finalize();
		
		} catch(RuntimeException e) {
			LOG.error(e);
		}

	}
	
	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected void doAck(SipServletRequest request) throws ServletException,
			IOException {
		if(LOG.isDebugEnabled()) {
			LOG.debug("CALL ACK SES:"+request.getSession());	
			LOG.debug("Got Request ACK: "	+ request.getMethod()+" State:"+request.getSession().getState().toString());
			LOG.debug("RTP Session might start");
			
			
		}
		try {
			MediaZone mediaZone=(MediaZone) request.getSession().getAttribute(MessageUtil.MEDIA_MANAGER);	
			mediaZone.start();
		} catch(RuntimeException e) {
			LOG.error(e);
		}
		callStablished=true;
		

	}
	
	@Override
	protected void doCancel(SipServletRequest request) throws ServletException,
			IOException {
		if(LOG.isTraceEnabled()) {
			LOG.trace("Cancel Request "+request.getMethod());
		}
		SipServletResponse response = request.createResponse(SipServletResponse.SC_OK);
		response.send();
		response = request.createResponse(SipServletResponse.SC_REQUEST_TERMINATED);
		response.send();
		
		
		upChain.process(new SIPMutableMessage(request));
		
		try {
		MediaZone mediaZone=(MediaZone) request.getSession().getAttribute(MessageUtil.MEDIA_MANAGER);
		mediaZone.finalize();
		} catch (RuntimeException e) {
			LOG.error(e);
		}
		
	}

}
