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
package org.restcomm.sbc.chain.impl;

import java.io.IOException;

import javax.sdp.SdpException;

import javax.servlet.sip.SipServletMessage;
import org.apache.log4j.Logger;
import org.restcomm.chain.ProcessorChain;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.ProcessorCallBack;
import org.restcomm.chain.processor.impl.DefaultProcessor;
import org.restcomm.chain.processor.impl.ProcessorParsingException;
import org.restcomm.chain.processor.impl.SIPMutableMessage;
import org.restcomm.sbc.managers.RouteManager;
import org.restcomm.sbc.media.MediaMetadata;




/**
 * @author ocarriles@eolos.la (Oscar Andres Carriles)
 * @date 13 sept. 2016 18:10:42
 * @class NATHelperProcessor.java
 *
 */
public class IncomingDPIProcessor extends DefaultProcessor implements ProcessorCallBack {

	@SuppressWarnings("unused")
	private static transient Logger LOG = Logger.getLogger(IncomingDPIProcessor.class);
	
	public IncomingDPIProcessor(ProcessorChain callback) {
		super(callback);
	}

	public IncomingDPIProcessor(String name, ProcessorChain callback) {
		super(name, callback);
	}

	public String getName() {
		return "Incoming DPI Processor";
	}

	public int getId() {
		return this.hashCode();
	}

	@Override
	public void setName(String name) {
		this.name = name;

	}


	private void processMessage(SIPMutableMessage message) {

		if (RouteManager.isFromDMZ(message.getContent())) {
			message.setDirection(Message.SOURCE_DMZ);	
		}
		else {
			message.setDirection(Message.SOURCE_MZ);
		}
		
		message.setTarget(Message.TARGET_B2BUA);
	}

	@Override
	public ProcessorCallBack getCallback() {
		return this;
	}


	@Override
	public String getVersion() {
		return "1.0.0";
	}

	@Override
	public void doProcess(Message message) throws ProcessorParsingException {
		SIPMutableMessage m = (SIPMutableMessage) message;
		SipServletMessage sm=m.getContent();
		
		m.setSourceLocalAddress(sm.getLocalAddr());
		m.setSourceRemoteAddress(sm.getRemoteAddr());
		m.setSourceTransport(sm.getTransport().toUpperCase());
		
		if(sm.getContentLength()>0 &&
			sm.getContentType().equals("application/sdp")) {
			try {
				MediaMetadata metadata=MediaMetadata.build(MediaMetadata.MEDIATYPE_AUDIO, new String(sm.getRawContent()));
				m.setMetadata(metadata);
			} catch (IOException | org.mobicents.media.server.io.sdp.SdpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		processMessage(m);
		
		
	}

}