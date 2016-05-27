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
 * @author Oscar Andres Carriles <ocarriles@eolos.la>.
 *******************************************************************************/
package org.restcomm.sbc.managers;

import javax.servlet.sip.Address;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;



import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;

public class MessageUtil {
	private static transient Logger logger = Logger.getLogger(MessageUtil.class);
	
	public static String B2BUA_FINGERPRINT_HEADER="X-SBC-fingerprint";
	
	public static void tracer(SipServletMessage message) {
		
		SipServletResponse response = null;
		SipServletRequest  request  = null;
		
		String uagent=message.getHeader("User-Agent");
		String server=message.getHeader("Server");
		
		if(message instanceof SipServletRequest) {
			request=(SipServletRequest) message;
		}
		else {
			response=(SipServletResponse) message;
		}
		
		String m=
				"B2BUALeg "+isB2BUALeg(message)+				
				" "+(message instanceof SipServletRequest?"REQUEST ":"RESPONSE")+				
				" "+message.getMethod()+
				" "+(message instanceof SipServletResponse?(response.getStatus()):request.isInitial())+
				" to "+message.getTo()+
				" from "+message.getFrom()+
				" ua "+(uagent==null?server:uagent);
		//logger.info(getOrigination(message));
		//logger.info(getApplicationData(message));
		logger.info(m);
		
	}
	
	public static String getRegion(SipServletMessage message) {
		return message.getInitialRemoteAddr()+":"+message.getInitialRemotePort()+", "+message.getInitialTransport();
		
	}
	public static boolean isB2BUALeg(SipServletMessage message) {
		String fingerprint=message.getHeader(MessageUtil.B2BUA_FINGERPRINT_HEADER);
		return (!(fingerprint==null||fingerprint.equals(""))?true:false);	
	}
	
	private static String getOrigination(SipServletMessage message) {
		String remote=(isB2BUALeg(message)?"Remote:Not stablished yet":"Remote:"+message.getRemoteAddr()+":"+message.getRemotePort()+", "+message.getTransport());
		String local =" Local :"+message.getLocalAddr() +":"+message.getLocalPort() +", "+message.getTransport();
		return remote+local;
		
	}
	
	private static String getApplicationData(SipServletMessage message) {
		return "APP-DATA:"+message.getSession().getApplicationSession().getApplicationName();
	}
	
	public static boolean isOwnedByMZDomain(SipServletMessage message) {
		return false;
		
	}
	
	

}
