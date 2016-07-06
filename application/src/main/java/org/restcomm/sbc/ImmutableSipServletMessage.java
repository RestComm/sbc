package org.restcomm.sbc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;

import javax.servlet.sip.Address;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipSession;


public class ImmutableSipServletMessage  implements SipServletMessage {
	
	private SipServletMessage oMessage;

	public ImmutableSipServletMessage(SipServletMessage oMessage) {
		this.oMessage=oMessage;
	}

	@Override
	public void addAcceptLanguage(Locale arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addAddressHeader(String arg0, Address arg1, boolean arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addHeader(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addParameterableHeader(String arg0, Parameterable arg1,
			boolean arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Locale getAcceptLanguage() {
		return oMessage.getAcceptLanguage();
	}

	@Override
	public Iterator<Locale> getAcceptLanguages() {
		return oMessage.getAcceptLanguages();
	}

	@Override
	public Address getAddressHeader(String arg0) throws ServletParseException {
		return oMessage.getAddressHeader(arg0);
	}

	@Override
	public ListIterator<Address> getAddressHeaders(String arg0)
			throws ServletParseException {
		return oMessage.getAddressHeaders(arg0);
	}

	@Override
	public SipApplicationSession getApplicationSession() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SipApplicationSession getApplicationSession(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getAttribute(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCallId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getContent() throws IOException, UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Locale getContentLanguage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getContentLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getExpires() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Address getFrom() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHeader(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HeaderForm getHeaderForm() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<String> getHeaderNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListIterator<String> getHeaders(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInitialRemoteAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getInitialRemotePort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getInitialTransport() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLocalPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Parameterable getParameterableHeader(String arg0)
			throws ServletParseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListIterator<? extends Parameterable> getParameterableHeaders(
			String arg0) throws ServletParseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProtocol() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getRawContent() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRemotePort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SipSession getSession() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SipSession getSession(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Address getTo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTransport() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCommitted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUserInRole(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeAttribute(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeHeader(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAcceptLanguage(Locale arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAddressHeader(String arg0, Address arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setContent(Object arg0, String arg1)
			throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setContentLanguage(Locale arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setContentLength(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setContentType(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setExpires(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHeader(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHeaderForm(HeaderForm arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setParameterableHeader(String arg0, Parameterable arg1) {
		// TODO Auto-generated method stub
		
	}

}
