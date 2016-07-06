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

import org.apache.log4j.Logger;
import org.restcomm.chain.processor.impl.SIPMutableMessage;

import org.restcomm.sbc.chain.impl.options.DownstreamOptionsProcessorChain;
import org.restcomm.sbc.chain.impl.options.UpstreamOptionsProcessorChain;


public class SBCOptionsServlet extends SipServlet {	
	private static final long serialVersionUID = 1L;	
	
	
	private static transient Logger LOG = Logger.getLogger(SBCOptionsServlet.class);
	
	private UpstreamOptionsProcessorChain upChain;
	private DownstreamOptionsProcessorChain dwChain;
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		
		LOG.info("OPTIONS sip servlet has been started");
		super.init(servletConfig);
		
		upChain=new UpstreamOptionsProcessorChain();
		LOG.info("Loading (v. "+upChain.getVersion()+") "+upChain.getName());
		dwChain=new DownstreamOptionsProcessorChain();
		LOG.info("Loading (v. "+dwChain.getVersion()+") "+dwChain.getName());
		
		
		
	}
	
	
	@Override
	protected void doOptions(SipServletRequest request) throws ServletException,
			IOException {

		if(request.isInitial()) {
		    upChain.process(new SIPMutableMessage(request));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		
		dwChain.process(new SIPMutableMessage(sipServletResponse));
		super.doResponse(sipServletResponse);
	}
	
	

}
