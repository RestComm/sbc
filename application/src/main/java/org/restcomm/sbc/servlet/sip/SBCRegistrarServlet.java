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
import org.restcomm.sbc.chain.impl.registrar.UpstreamRegistrarProcessorChain;
import org.restcomm.sbc.chain.impl.registrar.DownstreamRegistrarProcessorChain;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    3/5/2016 22:50:15
 * @class   SBCRegistrarServlet.java
 *
 */
public class SBCRegistrarServlet extends SipServlet {	
	private static final long serialVersionUID = 1L;	
	private Configuration configuration;
	private SipFactory sipFactory;	
	private String routeMZTransport;
	private int    routeMZPort;
	private String mzIface;
	private String mzIPAddress;
	private String mzTransport;
	private int mzPort;
	private String dmzIface;
	private String dmzIPAddress;
	private String dmzTransport;
	private int dmzPort;
	private String routeMZIPAddress;
	
	private UpstreamRegistrarProcessorChain upChain;
	private DownstreamRegistrarProcessorChain dwChain;
	
	private static transient Logger LOG = Logger.getLogger(SBCRegistrarServlet.class);
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> init()");
	    }
		super.init(servletConfig);
		sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
		final ServletContext context = servletConfig.getServletContext();
		configuration=(Configuration) context.getAttribute(Configuration.class.getName());
		ConfigurationCache.build(sipFactory, configuration);
		
        mzIface    =ConfigurationCache.getMzIface();
        mzIPAddress=ConfigurationCache.getMzIPAddress();
        mzTransport=ConfigurationCache.getMzTransport();
        mzPort     =ConfigurationCache.getMzPort();
        
        dmzIface    =ConfigurationCache.getDmzIface();
        dmzIPAddress=ConfigurationCache.getDmzIPAddress();
        dmzTransport=ConfigurationCache.getDmzTransport();
        dmzPort     =ConfigurationCache.getDmzPort();
      
        routeMZIPAddress=ConfigurationCache.getRouteMZIPAddress();
		routeMZTransport=ConfigurationCache.getRouteMZTransport();
		routeMZPort     =ConfigurationCache.getRouteMZPort();
		
		if(LOG.isDebugEnabled()){
			LOG.debug("MZ :"+mzIface+", "+mzIPAddress+":"+mzPort+", "+mzTransport);
			LOG.debug("DMZ:"+dmzIface+", "+dmzIPAddress+":"+dmzPort+", "+dmzTransport);
			LOG.debug("Route MZ Target:"+routeMZIPAddress+":"+routeMZPort+", "+routeMZTransport);
			LOG.debug("Registration Throttling enabled:"+ConfigurationCache.isRegThrottleEnabled());
			LOG.debug("MaxUATTL:"+ConfigurationCache.getRegThrottleMaxUATTL());
			LOG.debug("MinRETTL:"+ConfigurationCache.getRegThrottleMinRegistartTTL());
	    }
		
		upChain=new UpstreamRegistrarProcessorChain();
		LOG.info("Loading (v. "+upChain.getVersion()+") "+upChain.getName());
		dwChain=new DownstreamRegistrarProcessorChain();
		LOG.info("Loading (v. "+dwChain.getVersion()+") "+dwChain.getName());
		
		
		
	}
	
	@Override
	protected void doRegister(SipServletRequest sipServletRequest) throws ServletException, IOException {
		
		if(sipServletRequest.isInitial()) {
		    upChain.process(new SIPMutableMessage(sipServletRequest));
		}
		
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
