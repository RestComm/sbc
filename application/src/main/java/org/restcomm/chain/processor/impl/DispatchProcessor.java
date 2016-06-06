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

import org.restcomm.chain.ProcessorChain;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.Processor;
import org.restcomm.chain.processor.ProcessorCallBack;


/**
 * Specialized Object Processor responsible to check grammar and syntax
 * health of the UAC incoming sip message. 
 *
 */
/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    27/5/2016 13:37:39
 * @class   DispatchProcessor.java
 *
 */
public class DispatchProcessor extends DefaultProcessor 
	implements ProcessorCallBack {

	Processor.Status status=Processor.Status.IDLE;	
	private String name="#Low level dispatcher";
	
	public DispatchProcessor(String name,	ProcessorChain chain) {
		super(name, chain);
		setName(name);
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return this.hashCode();
	}

	@Override
	public Status getStatus() {
		return status; 
	}

	@Override
	public ProcessorCallBack getCallback() {
		return this;
	}

	@Override
	public void doProcess(Message message) throws ProcessorParsingException {
		
	}

	@Override
	public void setName(String name) {
		this.name=name;
		
	}

	
}