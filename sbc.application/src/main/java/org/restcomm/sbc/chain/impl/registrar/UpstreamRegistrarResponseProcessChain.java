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

package org.restcomm.sbc.chain.impl.registrar;


import java.io.IOException;

import javax.servlet.sip.SipServletMessage;

import org.restcomm.sbc.chain.ProcessorChainImpl;
import org.restcomm.sbc.processor.Processor;
import org.restcomm.sbc.processor.impl.B2BUABuilderProcessor;
import org.restcomm.sbc.processor.impl.RegistrarProcessor;
import org.restcomm.sbc.processor.impl.TopologyHideProcessor;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    3/5/2016 22:47:14
 * @class   UpstreamRegistrarResponseProcessChain.java
 * @project Servlet2.5SBC
 *
 */
public class UpstreamRegistrarResponseProcessChain extends ProcessorChainImpl {

	Processor c1;
	
	public UpstreamRegistrarResponseProcessChain() {
		// initialize the chain
		// works with original message
		          c1 = new RegistrarProcessor(this);
		Processor c2 = new B2BUABuilderProcessor(this);
		// works with B2BUA Leg message
		Processor c3 = new TopologyHideProcessor(this);
		
		// set the chain of responsibility
		link(c1, c2);
		link(c2, c3);
		link(c3, ProcessorChainImpl.DISPATCH);
		
		
			
	}
	
	public void processChain(SipServletMessage message) throws IOException {
		super.processChain(c1, message);
		
		
	}
	
	public static void main(String argv[]) {
		new UpstreamRegistrarRequestProcessChain();
		System.err.println("_________________________________________");
		new UpstreamRegistrarResponseProcessChain();
		
	}

	
	
}