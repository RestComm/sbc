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

package org.restcomm.sbc.chain.impl.options;


import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import org.apache.log4j.Logger;
import org.restcomm.chain.ProcessorChain;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.ProcessorCallBack;
import org.restcomm.chain.processor.impl.DefaultProcessor;
import org.restcomm.chain.processor.impl.ProcessorParsingException;
import org.restcomm.chain.processor.impl.SIPMutableMessage;
import org.restcomm.sbc.ConfigurationCache;
import org.restcomm.sbc.managers.LocationManager;
import org.restcomm.sbc.managers.RouteManager;



/**
 * 
 * @author  Oscar Andres Carriles <ocarriles@eolos.la>
 * @date    25/4/2016 10:16:38
 * @class   RegistrarProcessor.java
 */
/**
 * Specialized Registrar Processor. 
 *
 */
public class OptionsProcessor extends DefaultProcessor implements ProcessorCallBack {
	
	private static transient Logger LOG = Logger.getLogger(OptionsProcessor.class);
	private String name="OPTIONS Processor";
	
	
	public OptionsProcessor(ProcessorChain chain) {
		super(chain);
		this.chain=chain;	
	}
	
	public OptionsProcessor(String name, ProcessorChain chain) {
		this(chain);
		setName(name);
	}
	

	private void processRequest(SIPMutableMessage message) {
		
		SipServletRequest request=(SipServletRequest) message.getContent();
		LocationManager locationManager=LocationManager.getLocationManager();
		SipServletResponse response;
		
		String user;
		
		
		if (RouteManager.isFromDMZ(request)) {
			user=((SipURI)(request.getFrom().getURI())).getUser();
		}
		else {
			user=((SipURI)(request.getTo().getURI())).getUser();
		}
		
		
		// Deals with DMZ expiration 
		// if DMZ registration is expired
		if(!locationManager.exists(user, ConfigurationCache.getDomain())) {
			response=request.createResponse(404, "Not found");

		}	
		else {
			response=request.createResponse(200, "Ok");
				
		}
			
		message.setContent(response);
		
	}
	
	private void processResponse(SIPMutableMessage message) {
		
	}

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
			processRequest(m);
		}
		if(sm instanceof SipServletResponse) {
			processResponse(m);
		}
		
	}
	
	@Override
	public String getVersion() {
		return "1.0.0";
	}

}
