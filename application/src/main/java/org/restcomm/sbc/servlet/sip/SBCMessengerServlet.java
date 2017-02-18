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
 */


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
import org.restcomm.sbc.chain.impl.messenger.UpstreamMessengerProcessorChain;
import org.restcomm.sbc.chain.impl.messenger.DownstreamMessengerProcessorChain;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    17 feb. 2017 18:24:27
 * @class   SBCMessengerServlet.java
 *
 */
public class SBCMessengerServlet extends SipServlet {	
	private static final long serialVersionUID = 1L;	
	private Configuration configuration;
	private SipFactory sipFactory;	
	
	private String routeMZIPAddress;
	
	private UpstreamMessengerProcessorChain upChain;
	private DownstreamMessengerProcessorChain dwChain;
	
	private static transient Logger LOG = Logger.getLogger(SBCMessengerServlet.class);
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> Messenger Servlet init()");
	    }
		super.init(servletConfig);
		sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
		final ServletContext context = servletConfig.getServletContext();
		configuration=(Configuration) context.getAttribute(Configuration.class.getName());
		ConfigurationCache.build(sipFactory, configuration);
		
      
        routeMZIPAddress=ConfigurationCache.getTargetHost();
		
		upChain=new UpstreamMessengerProcessorChain();
		LOG.info("Loading (v. "+upChain.getVersion()+") "+upChain.getName());
		dwChain=new DownstreamMessengerProcessorChain();
		LOG.info("Loading (v. "+dwChain.getVersion()+") "+dwChain.getName());
		
		
		
	}
	
	
	@Override
	protected void doMessage(SipServletRequest sipServletRequest) throws ServletException, IOException {	
		upChain.process(new SIPMutableMessage(sipServletRequest));		
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	protected void doResponse(SipServletResponse sipServletResponse) throws ServletException, IOException {		
		dwChain.process(new SIPMutableMessage(sipServletResponse));	
		super.doResponse(sipServletResponse);
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void doErrorResponse(SipServletResponse sipServletResponse) throws ServletException, IOException {	

	}
	
	
}
