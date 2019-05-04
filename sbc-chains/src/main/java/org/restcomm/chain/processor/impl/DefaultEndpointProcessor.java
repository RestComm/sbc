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
import org.restcomm.chain.processor.ProcessorCallBack;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    13/6/2016 18:31:26
 * @class   DefaultEndpointProcessor.java
 *
 */
public  abstract class DefaultEndpointProcessor extends DefaultDPIProcessor implements EndpointProcessor {

	
	public DefaultEndpointProcessor(String name, ProcessorChain chain) {
		super(name, chain);
		type=Type.SINGLE_PROCESSOR;
		
	}
	
	public DefaultEndpointProcessor(ProcessorChain chain) {
		super(chain);
		type=Type.SINGLE_PROCESSOR;
		
	}

	@Override
	public abstract double getVersion();

	@Override
	public abstract String getName();

	@Override
	public abstract void setName(String name);
	@Override
	public abstract int getId();

	@Override
	public abstract ProcessorCallBack getCallback();
	
	
}
