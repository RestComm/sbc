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
 * @date    8/6/2016 19:59:21
 * @class   UnlinkProcessor.java
 *
 */
public class UnlinkProcessor extends DefaultProcessor implements ProcessorCallBack {

	private static transient Logger LOG = Logger.getLogger(UnlinkProcessor.class);

	private String name="Unlink Processor";
	
	public UnlinkProcessor(ProcessorChain processorChain) {
		super(processorChain);	
	}

	
	public UnlinkProcessor(String name, ProcessorChain processorChain) {
		super(name, processorChain);
		setName(name);
	}


	public void doProcess(Message message) throws ProcessorParsingException  { 
		LOG.debug(">> doProcess() Callback from processor: "+getName());	
		
		if(message==null||!(message instanceof MutableMessage)) {
			throw new ProcessorParsingException("Illegal Message data content");
		}
		
		message.unlink();
		
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