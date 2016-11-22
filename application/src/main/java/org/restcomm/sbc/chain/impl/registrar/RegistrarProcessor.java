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

package org.restcomm.sbc.chain.impl.registrar;



import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import org.apache.log4j.Logger;
import org.restcomm.chain.ProcessorChain;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.ProcessorCallBack;
import org.restcomm.sbc.ConfigurationCache;
import org.restcomm.chain.processor.impl.DefaultProcessor;
import org.restcomm.chain.processor.impl.ProcessorParsingException;
import org.restcomm.chain.processor.impl.SIPMutableMessage;
import org.restcomm.sbc.bo.Location;
import org.restcomm.sbc.bo.LocationNotFoundException;
import org.restcomm.sbc.chain.impl.registrar.RegistrarProcessor;
import org.restcomm.sbc.managers.LocationManager;
import org.restcomm.sbc.managers.MessageUtil;


import gov.nist.javax.sip.header.SIPHeader;



/**
 * 
 * @author  Oscar Andres Carriles <ocarriles@eolos.la>
 * @date    25/4/2016 10:16:38
 * @class   RegistrarProcessor.java
 */
/**
 * Specialized Registrar Processor. 
 *
 */
public class RegistrarProcessor extends DefaultProcessor implements ProcessorCallBack {
	
	private static transient Logger LOG = Logger.getLogger(RegistrarProcessor.class);
	private String name="REGISTRAR Processor";
	
	
	public RegistrarProcessor(ProcessorChain chain) {
		super(chain);
		this.chain=chain;	
		
	}
	
	public RegistrarProcessor(String name, ProcessorChain chain) {
		this(chain);
		setName(name);
	}
	

	/**
	 * Throttling management 
	 * @param dmzRequest
	 * @return message
	 */
	
	private void processRequest(SIPMutableMessage message) {
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> processRequest()");
	    }
		
		SipServletRequest dmzRequest=(SipServletRequest) message.getContent();
		SipServletRequest oRequest=(SipServletRequest) dmzRequest.getSession().getAttribute(MessageUtil.B2BUA_ORIG_REQUEST_ATTR);
		
		int expires=dmzRequest.getExpires();
		Address contactAddress = null;
		SipURI fromURI;
		
		if(ConfigurationCache.isRegThrottleEnabled() && expires >0) {
			LocationManager locationManager=LocationManager.getLocationManager();
	
			try {
				contactAddress = dmzRequest.getAddressHeader("Contact");
				fromURI = (SipURI) dmzRequest.getFrom().getURI();
				
			} catch (ServletParseException e) {
				LOG.error("Cannot get Contact Address!");
				return;
			}
			SipURI uri = (SipURI) contactAddress.getURI();
			String user = uri.getUser();
			String domain = fromURI.getHost();
			
			int mzExpiry=ConfigurationCache.getRegThrottleMZTTL();
			int uaExpiry=ConfigurationCache.getRegThrottleUATTL();
			
			dmzRequest.setExpires(mzExpiry);
			
			if(LOG.isDebugEnabled()){
		          LOG.debug("expires="+expires+" ua="+uaExpiry+" mz="+mzExpiry);
		    }
			
			
			expires=uaExpiry;	
			
			// Deals with DMZ expiration 
			// if DMZ registration is expired
			if(!locationManager.exists(user, domain)) {	
				// if it does not come from pre-authenticated uri
				// continue chain processing
				message.setContent(dmzRequest);
				return;
				
			}
			
			if(LOG.isDebugEnabled()){
		          LOG.debug("Registration Throttle Replying to originator");
		    }
			
			
			SipServletResponse dmzResponse = dmzRequest.createResponse(200, "Ok");
			
			dmzResponse.setHeader("Max-Expires", ""+uaExpiry);
			dmzResponse.setHeader("Min-Expires", ""+uaExpiry);
			dmzResponse.setHeader("Expires"    , ""+uaExpiry);
			contactAddress.setExpires(uaExpiry);
			
			dmzResponse.setAddressHeader("Contact", contactAddress);
			
			if(LOG.isDebugEnabled()){
		          //LOG.debug(locationManager.getLocation(user, domain));
		    }
			
			// must break chain here and send back to UA
			// after method termination
			message.setContent(dmzResponse);
			message.unlink();
			return;
			
		}
		
		try {
			//contactAddress=routeManager.getRegistrationContactAddress(dmzRequest);
			dmzRequest.setAddressHeader(MessageUtil.B2BUA_ORIG_CONTACT_ADDR, dmzRequest.getAddressHeader("Contact"));
		
		} catch (ServletParseException e) {
			LOG.error("ERROR",e);
			SipServletResponse dmzResponse = dmzRequest.createResponse(401, "Not Found");
			message.setContent(dmzResponse);	
			message.unlink();
			return;
		} 
		
		//dmzRequest.setAddressHeader("Contact", contactAddress);
		message.setContent(dmzRequest);
		
		
		
	}
	
	private void processResponse(SIPMutableMessage message) {
		
		SipServletResponse mzResponse=(SipServletResponse) message.getContent();
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> processResponse()");
	          LOG.trace(">> response received: \n"+mzResponse.toString());
	         
	    }
		SipServletRequest oRequest=(SipServletRequest) mzResponse.getSession().getAttribute(MessageUtil.B2BUA_ORIG_REQUEST_ATTR);
		
		Location location=null;
		
		if(mzResponse.getStatus()== SipServletResponse.SC_OK) {
			LocationManager locationManager=LocationManager.getLocationManager();
			Address address = null;
			SipURI uri = null;
			
			try {
				
				address=mzResponse.getRequest().getAddressHeader("Contact");
				uri=(SipURI) address.getURI();
				
				LOG.info("URI "+uri.toString());
				
			} catch (ServletParseException e) {
				LOG.error("ERROR",e);
			}
			String user = ((SipURI) mzResponse.getFrom().getURI()).getUser();
			String domain = ((SipURI) mzResponse.getTo().getURI()).getHost();
			int mzExpires = mzResponse.getRequest().getExpires();
			//int dmzExpires= mzExpires;
			
			if(mzExpires<=0) {
				location=locationManager.unregister(user, domain);
				if(LOG.isDebugEnabled()){
			          LOG.debug("UNREGISTER "+location.getUser());
			    }
				
			}	
			else {	
				if(ConfigurationCache.isRegThrottleEnabled()) {	
						mzExpires=ConfigurationCache.getRegThrottleMZTTL();
						//dmzExpires=ConfigurationCache.getRegThrottleUATTL();		
				}
				
				int cSeq=getCSeq(mzResponse.getRequest());
				String callerID=mzResponse.getCallId();
				
				if(!locationManager.exists(user, domain)) {
					location=new Location(uri.getUser(), domain, uri.getHost(), uri.getPort(), uri.getTransportParam());
					locationManager.register(location, mzResponse.getRequest().getHeader("User-Agent"), mzResponse.getCallId(), getCSeq(mzResponse.getRequest()) , mzExpires);
					if(LOG.isDebugEnabled()){
				          LOG.debug("REGISTER new "+location);
				    }
				}
				else {
					try {
						location=locationManager.getLocation(user, domain);
					} catch (LocationNotFoundException e) {
						LOG.error("No Binding ",e);
					}
					
					if(location.getcSeq()<cSeq && callerID.equals(location.getCallID())) {
						locationManager.register(location, mzResponse.getRequest().getHeader("User-Agent"), mzResponse.getCallId(), getCSeq(mzResponse.getRequest()) , mzExpires);
						if(LOG.isDebugEnabled()){
					          LOG.debug("REGISTER update "+location);
					    }
					}
					else {
						locationManager.register(location, mzResponse.getRequest().getHeader("User-Agent"), mzResponse.getCallId(), getCSeq(mzResponse.getRequest()) , mzExpires);
						if(LOG.isDebugEnabled()){	  
					          LOG.debug("REGISTER new Dialog "+location);
					    }
					}
					
					
				}
				
			}
		}
		
		message.setContent(mzResponse);
		
		
	}
	private int getCSeq(SipServletRequest request)  {
		String s = request.getHeader(SIPHeader.CSEQ);
		try {
			return Integer.parseInt(s.substring(0, s.indexOf(' ')));
		} catch (Exception e) {
			return -1;
		}
	}

	public String getName() {
		return name;
	}

	
	public int getId() {
		return this.hashCode();
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
		SIPMutableMessage m  =(SIPMutableMessage) message;
		
		SipServletMessage sm = m.getContent();
		
		
		
		if(sm instanceof SipServletRequest) {
			processRequest(m);
		}
		if(sm instanceof SipServletResponse) {
			processResponse(m);
		}
		
	}
	
	@Override
	public String getVersion() {
		return "1.0.0";
	}

}
