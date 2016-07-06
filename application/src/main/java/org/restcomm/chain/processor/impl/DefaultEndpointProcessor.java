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
import org.restcomm.chain.processor.EndpointProcessor;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    13/6/2016 18:31:26
 * @class   DefaultEndpointProcessor.java
 *
 */
public abstract class DefaultEndpointProcessor extends DefaultProcessor implements EndpointProcessor {

	public DefaultEndpointProcessor() {
		type=Type.CHAIN;
		
	}
	
	public DefaultEndpointProcessor(String name) {
		this();
		setName(name);
		
	}
	
	public DefaultEndpointProcessor(String name, ProcessorChain chain) {
		this(name);
		this.chain=chain;
		type=Type.SINGLE_PROCESSOR;
		
	}
	
	public DefaultEndpointProcessor(ProcessorChain chain) {
		this();
		this.chain=chain;
		type=Type.SINGLE_PROCESSOR;
		
	}
	
	
}
