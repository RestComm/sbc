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

package org.restcomm.chain.testsuite;


import org.apache.log4j.Logger;
import org.restcomm.chain.ProcessorChain;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.Processor;
import org.restcomm.chain.processor.ProcessorCallBack;
import org.restcomm.chain.processor.impl.DefaultProcessor;
import org.restcomm.chain.processor.impl.MutableMessage;
import org.restcomm.chain.processor.impl.ProcessorParsingException;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    21/5/2016 12:23:13
 * @class   SimpleProcessor.java
 *
 */
public class SimpleProcessor extends DefaultProcessor implements ProcessorCallBack {

	private static transient Logger LOG = Logger.getLogger(SimpleProcessor.class);
	
	private String name="Simple Processor";
	
	public SimpleProcessor(ProcessorChain processorChain) {
		super(processorChain);
		
	}

	
	public SimpleProcessor(String name, ProcessorChain processorChain) {
		super(name, processorChain);
		setName(name);
	}


	public void doProcess(Message message) throws ProcessorParsingException  { 
		LOG.debug(">> doProcess() Callback from processor: "+getName());	
		
		if(message==null||!(message instanceof Message)) {
			throw new ProcessorParsingException("Illegal Message data content");
		}
		
		String content=((StringBuffer) message.getContent()).toString();
		
		//content="*"+content.replaceAll("little", "big");
		content="*"+content;
		message.setContent(new StringBuffer(content));
		LOG.debug("<< doProcess() Callback from processor: "+getName());
		
		
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
	public double getVersion() {
		return 1.0;
	}



	

}