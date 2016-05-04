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
package org.restcomm.sbc.processor.impl;

import java.io.IOException;

import javax.servlet.sip.SipServletMessage;

import org.apache.log4j.Logger;
import org.restcomm.sbc.chain.ProcessorChain;
import org.restcomm.sbc.processor.Processor;
/**
 * 
 * @author  Oscar Andres Carriles <ocarriles@eolos.la>
 * @date    25/4/2016 9:54:10
 * @class   SipMessageSanityCheckProcessor.java
 * @project Servlet2.5SBC
 *
 */
/**
 * Specialized Message Processor responsible to hide topology
 * MZ Data. 
 *
 */
public class TopologyHideProcessor implements Processor {


	private static transient Logger LOG = Logger.getLogger(TopologyHideProcessor.class);

	
	public TopologyHideProcessor(ProcessorChain callback) {
		
	}
	
	
	
	public SipServletMessage process(SipServletMessage message)  {
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> process() "+getName());
	    }
		return message;
		
	}


	public String getName() {
		return "Topology Hide Processor";
	}



	public int getId() {
		return this.hashCode();
	}

	
}