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

package org.restcomm.chain.processor.impl;

import java.io.IOException;

import javax.servlet.sip.SipServletMessage;

import org.apache.log4j.Logger;
import org.restcomm.chain.ProcessorChain;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.ProcessorCallBack;



/**
 * Specialized Object Processor responsible to check grammar and syntax
 * health of the UAC incoming sip message. 
 *
 */
/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    27/5/2016 13:37:18
 * @class   DispatchDPIProcessor.java
 *
 */
public class DispatchDPIProcessor extends DefaultEndpointProcessor 
	implements ProcessorCallBack {
	

	private static transient Logger LOG = Logger.getLogger(DispatchDPIProcessor.class);
	
	private String name="#Low level DPI dispatcher";
	
	
	public DispatchDPIProcessor(String name, ProcessorChain chain) {
		super(name, chain);
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return this.hashCode();
	}


	@Override
	public ProcessorCallBack getCallback() {
		return this;
	}


	@Override
	public void setName(String name) {
		this.name=name;
		
	}


	@Override
	public void doProcess(Message message) throws ProcessorParsingException {
		SipServletMessage m=(SipServletMessage) message.getProperty("content");
		try {
			m.send();
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
		
	}

	@Override
	public String getVersion() {
		return "1.0.0";
	}

	
}