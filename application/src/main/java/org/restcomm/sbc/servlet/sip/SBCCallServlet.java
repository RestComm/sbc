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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;
import org.restcomm.chain.processor.impl.SIPMutableMessage;
import org.restcomm.sbc.chain.impl.invite.DownstreamInviteProcessorChain;
import org.restcomm.sbc.chain.impl.invite.UpstreamInviteProcessorChain;
import org.restcomm.sbc.chain.impl.registrar.DownstreamRegistrarProcessorChain;
import org.restcomm.sbc.chain.impl.registrar.UpstreamRegistrarProcessorChain;
import org.restcomm.sbc.managers.LocationManager;
import org.restcomm.sbc.managers.MessageUtil;

public class SBCCallServlet extends SipServlet {	
	private static final long serialVersionUID = 1L;	
	
	private String MZIP  ="192.168.0.2";
	private String DMZIP ="192.168.88.2";
	private int DMZPORT=5080;
	private int MZPORT =5080;
	
	private static transient Logger LOG = Logger.getLogger(SBCCallServlet.class);
	
	private UpstreamInviteProcessorChain upChain;
	private DownstreamInviteProcessorChain dwChain;
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		
		LOG.info("the simple sip servlet has been started");
		super.init(servletConfig);
		
		upChain=new UpstreamInviteProcessorChain();
		LOG.info("Loading (v. "+upChain.getVersion()+") "+upChain.getName());
		dwChain=new DownstreamInviteProcessorChain();
		LOG.info("Loading (v. "+dwChain.getVersion()+") "+dwChain.getName());
		
		
		
	}
	
	
	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
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
	
	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		System.err.println("Got BYE: "
				+ request.getMethod());
		SipServletResponse response = request.createResponse(200);
		response.send();

		SipSession session = request.getSession();		
		SipSession linkedSession = request.getB2buaHelper().getLinkedSession(session);
		SipServletRequest newRequest = linkedSession.createRequest("BYE");
		System.err.println(newRequest);
		newRequest.send();

	}
	
	@Override
	protected void doCancel(SipServletRequest request) throws ServletException,
			IOException {		
		System.err.println("Got CANCEL: " + request.toString());
		SipSession session = request.getSession();
		B2buaHelper b2buaHelper = request.getB2buaHelper();
		SipSession linkedSession = b2buaHelper.getLinkedSession(session);
		SipServletRequest originalRequest = (SipServletRequest)linkedSession.getAttribute("originalRequest");
		SipServletRequest  cancelRequest = b2buaHelper.getLinkedSipServletRequest(originalRequest).createCancel();				
		System.err.println("forkedRequest = " + cancelRequest);			
		cancelRequest.send();
	}

}
