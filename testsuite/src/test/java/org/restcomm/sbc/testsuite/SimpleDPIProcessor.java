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

package org.restcomm.sbc.testsuite;


import org.apache.log4j.Logger;
import org.restcomm.chain.ProcessorChain;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.ProcessorCallBack;
import org.restcomm.chain.processor.impl.DefaultDPIProcessor;
import org.restcomm.chain.processor.impl.ImmutableMessage;
import org.restcomm.chain.processor.impl.ProcessorParsingException;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    27/5/2016 13:47:06
 * @class   SimpleDPIProcessor.java
 *
 */
public class SimpleDPIProcessor extends DefaultDPIProcessor implements ProcessorCallBack {

	private static transient Logger LOG = Logger.getLogger(SimpleDPIProcessor.class);
	
	private String name="Simple DPI Processor";
	
	public SimpleDPIProcessor(ProcessorChain processorChain) {
		super(processorChain);
		
	}

	
	public SimpleDPIProcessor(String name, ProcessorChain processorChain) {
		super(name, processorChain);
		setName(name);
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
		LOG.debug(">> doProcess() Callback from processor: "+getName());	
		
		if(message==null||!(message instanceof ImmutableMessage)) {
			throw new ProcessorParsingException("Illegal Message data content");
		}
		ImmutableMessage m=(ImmutableMessage) message;
			
	
		String content=((StringBuffer) m.getContent()).toString();
		
		content="*"+content.replaceAll("big", "little");
		//m.setProperty("content", new StringBuffer(content));
		LOG.debug("<< doProcess() Callback from processor: "+getName());
		

		LOG.debug("<< doProcess() Callback from processor: "+getName());
		
		
	}
	
	@Override
	public String getVersion() {
		return "1.0.0";
	}


}