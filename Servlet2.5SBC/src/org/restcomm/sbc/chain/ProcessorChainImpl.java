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
 */

package org.restcomm.sbc.chain;

import java.io.IOException;
import java.util.Hashtable;

import javax.servlet.sip.SipServletMessage;

import org.restcomm.sbc.processor.Processor;
import org.restcomm.sbc.processor.ProcessorParsingException;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    3/5/2016 22:46:25
 * @class   ProcessorChainImpl.java
 * @project Servlet2.5SBC
 *
 */
public class ProcessorChainImpl implements ProcessorChain {
	
	public static Processor DISPATCH;
	
	
	private Hashtable<Integer, Processor> processors=new Hashtable<Integer, Processor>();
	
	
	public ProcessorChainImpl() {
		DISPATCH=new Dispatcher();
		
	}
	
	public void link(Processor processor, Processor nextInChain) {	
		processors.put(processor.getId(), nextInChain);
	}
	
	
	public void unlink(Processor processor) {
		processors.put(processor.getId(), ProcessorChainImpl.DISPATCH);
		
	}
	
	
	public final void processChain(Processor processor, SipServletMessage message) throws IOException {
			
			if(processor!=null) {
				message=processor.process(message);
				processor=this.getNextLink(processor);
				processChain(processor, message);
			}
		
		
	}
	
	

	public Processor getNextLink(Processor processor) {
		return processors.get(processor.getId());
	}
	
	class Dispatcher implements Processor {
			
		
		public String getName() {
			return "#Low level dispatcher";
		}

		public SipServletMessage process(SipServletMessage message) throws ProcessorParsingException {
			try {
				message.send();
			} catch (IOException e) {
				throw new ProcessorParsingException(e.getMessage());
			}
			return message;			
		}

		public int getId() {
			return this.hashCode();
		}

		
		
	}

	
	

}
