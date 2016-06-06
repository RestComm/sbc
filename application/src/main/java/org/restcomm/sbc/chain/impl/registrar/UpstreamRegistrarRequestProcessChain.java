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

import javax.servlet.sip.SipServletMessage;

import org.restcomm.chain.impl.DefaultSerialProcessorChain;
import org.restcomm.chain.impl.MalformedProcessorChainException;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.Processor;
import org.restcomm.chain.processor.ProcessorCallBack;
import org.restcomm.chain.processor.impl.DispatchProcessor;
import org.restcomm.chain.processor.impl.ProcessorParsingException;
import org.restcomm.sbc.chain.processor.impl.B2BUABuilderProcessor;
import org.restcomm.sbc.chain.processor.impl.SanityCheckProcessor;
import org.restcomm.sbc.chain.processor.impl.RegistrarProcessor;
import org.restcomm.sbc.chain.processor.impl.TransportAdaptProcessor;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    3/5/2016 22:47:02
 * @class   UpstreamRegistrarRequestProcessChain.java
 *
 */
public class UpstreamRegistrarRequestProcessChain extends DefaultSerialProcessorChain implements ProcessorCallBack {

	

	public UpstreamRegistrarRequestProcessChain() {
		
		// initialize the chain
		// works with original message
		Processor c1 = new SanityCheckProcessor(this);
		Processor c2 = new RegistrarProcessor(this);
		Processor c3 = new B2BUABuilderProcessor(this);
		// works with B2BUA Leg message
		Processor c4 = new TransportAdaptProcessor(this);
		
		
		// set the chain of responsibility
		
		try {
			link(c1, c2);
			link(c2, c3);
			link(c3, c4);
			link(c4, new DispatchProcessor("Dispatch", this));
		} catch (MalformedProcessorChainException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	public SipServletMessage doProcess(SipServletMessage message) {
		return message;
	
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
	public ProcessorCallBack getCallback() {
		return this;
	}
	@Override
	public void doProcess(Message message) throws ProcessorParsingException {	
		doProcess((SipServletMessage) message.getWrappedObject());
	}
	

}