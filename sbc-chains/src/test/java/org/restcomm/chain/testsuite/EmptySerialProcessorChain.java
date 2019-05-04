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
 */

package org.restcomm.chain.testsuite;

import org.apache.log4j.Logger;
import org.restcomm.chain.impl.DefaultSerialProcessorChain;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.Processor;
import org.restcomm.chain.processor.ProcessorCallBack;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    9/6/2016 9:21:46
 * @class   EmptySerialProcessChain.java
 *
 */
public class EmptySerialProcessorChain extends DefaultSerialProcessorChain implements ProcessorCallBack {

	private static transient Logger LOG = Logger.getLogger(EmptySerialProcessorChain.class);
	private String name="Empty Serial Process Chain";
	
	public EmptySerialProcessorChain() {
		setName("ESPC");
		
		
	}
	
	public void doProcess(Message message) {
		LOG.debug(">> doProcess() Callback from chain: "+getName());
		if(message!=null) {
			LOG.debug(">> doProcess() message ["+message+"]");
		}
		
	}

	@Override
	public ProcessorCallBack getCallback() {
		return this;
	}

	
	@Override
	public String getName() {
		return name;
		
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