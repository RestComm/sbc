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
package org.restcomm.sbc.notification.impl;

import javax.servlet.sip.SipServletMessage;

import org.apache.log4j.Logger;
import org.restcomm.chain.ProcessorChain;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.ProcessorCallBack;
import org.restcomm.chain.processor.impl.DefaultProcessor;
import org.restcomm.chain.processor.impl.ProcessorParsingException;
import org.restcomm.chain.processor.Processor;

/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    6/6/2016 19:15:48
 * @class   SuspectActivityProcessor.java
 *
 */
public class SuspectActivityProcessor extends DefaultProcessor implements Processor, ProcessorCallBack {


	private static transient Logger LOG = Logger.getLogger(SuspectActivityProcessor.class);
	private String name;
	
	public SuspectActivityProcessor(ProcessorChain callback) {
		super(callback);
	}
	public SuspectActivityProcessor(String name,ProcessorChain callback) {
		super(name, callback);
	}
	
	
	public String getName() {
		return name;
	}



	public int getId() {
		return this.hashCode();
	}



	
	public SipServletMessage doProcess(SipServletMessage message) throws ProcessorParsingException {
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> doProcess() "+getName());
	    }
		return message;
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
		doProcess((SuspectActivityCache)message);
	}
	
	@Override
	public String getVersion() {
		return "1.0.0";
	}

	
}