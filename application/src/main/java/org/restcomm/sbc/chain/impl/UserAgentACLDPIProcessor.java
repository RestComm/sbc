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

package org.restcomm.sbc.chain.impl;

import java.util.ArrayList;
import java.util.Arrays;


import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;
import org.restcomm.chain.ProcessorChain;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.ProcessorCallBack;
import org.restcomm.chain.processor.impl.DefaultDPIProcessor;
import org.restcomm.chain.processor.impl.ProcessorParsingException;
import org.restcomm.chain.processor.impl.SIPMutableMessage;
import org.restcomm.sbc.managers.ThreatManager;
import org.restcomm.sbc.threat.Threat;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    27/5/2016 14:33:56
 * @class   DPIUserAgentACLProcessor.java
 *
 */
public class UserAgentACLDPIProcessor extends DefaultDPIProcessor implements ProcessorCallBack {

	private String name="ACL UA Processor";
	private static transient Logger LOG = Logger.getLogger(UserAgentACLDPIProcessor.class);
	
	private static String[] a= {	
			"sipcli",
			"sipvicious",
			"sip-scan",
			"sipsak",
			"sundayddr",
			"friendly-scanner",
			"iWar",
			"CSipSimple",
			"SIVuS",
			"Gulp",
			"sipv",
			"smap",
			"friendly-request",
			"VaxIPUserAgent",
			"VaxSIPUserAgent",
			"siparmyknife",
			"Test Agent"
	};

	private static ArrayList<String> attackers=new ArrayList<String>(Arrays.asList(a));
	
	public UserAgentACLDPIProcessor(ProcessorChain processorChain) {
			super(processorChain);
	}
	
	public UserAgentACLDPIProcessor(String name, ProcessorChain processorChain) {
			super(name, processorChain);
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return this.hashCode();
	}

	public SipServletMessage doProcess(SIPMutableMessage message) throws ProcessorParsingException {
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> doProcess()");
	    }
		SipServletMessage m=(SipServletMessage) message.getContent();
		
		String userAgent=m.getHeader("User-Agent");
		
		userAgent=(userAgent==null||"".equals(userAgent.trim()))?"Anonymous":userAgent;
		
		if (userAgent.equals("Anonymous")||isAttacker(userAgent)) {
			if(LOG.isInfoEnabled()){
		          LOG.info("THREAT: Forbidden access to threat-candidate UA "+userAgent);
		    }
			ThreatManager threatManager=ThreatManager.getThreatManager();
			threatManager.create(Threat.Type.BAD_UA,
					m.getFrom().getDisplayName(),
					m.getRemoteAddr(),
					0, 
					userAgent,
					m.getTransport());
			
			if(m instanceof SipServletRequest) {
				SipServletRequest request=(SipServletRequest) m;
				SipServletResponse response = request.createResponse(405, "Method not allowed");
				message.setContent(response);	
				message.unlink();
				
			}
			
		}
		return m;
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
		doProcess((SIPMutableMessage)message);
	}
	
	@Override
	public String getVersion() {
		return "1.0.0";
	}
	
	private boolean isAttacker(String ua) {
		
		for(String attackerUA:attackers) {
			if(ua.contains(attackerUA)){
				return true;
			}	
		}
		return false;
	}

}