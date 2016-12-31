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

package org.restcomm.sbc.media;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;
import org.bouncycastle.crypto.tls.TlsUtils;
import org.mobicents.media.io.ice.IceAuthenticator;
import org.mobicents.media.io.ice.IceHandler;
import org.mobicents.media.io.ice.events.IceEventListener;
import org.mobicents.media.io.ice.events.SelectedCandidatesEvent;
import org.mobicents.media.server.impl.rtp.RtpListener;
import org.mobicents.media.server.impl.rtp.crypto.RawPacket;
import org.mobicents.media.server.impl.srtp.DtlsListener;
import org.mobicents.media.server.io.network.channel.PacketHandlerException;
import org.mobicents.media.server.spi.ConnectionMode;
import org.restcomm.sbc.ConfigurationCache;
import org.restcomm.sbc.media.dtls.DtlsHandler;
import org.restcomm.sbc.media.srtp.RtpConnection;




/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    4 nov. 2016 16:27:31
 * @class   CryptoMediaZone.java
 *
 */
public class CryptoMediaZone extends MediaZone implements DtlsListener, RtpListener {
	
	
	private static transient Logger LOG = Logger.getLogger(CryptoMediaZone.class);
	
	private RtpConnection rtpConnection;
	private AudioChannel audioChannel;
	private IceAuthenticator iceAuthenticator;
    private RtcpChannel rtcpChannel;
    private RtpChannel rtpChannel;
    private IceHandler stunHandler;
    private DtlsHandler dtlsHandler;
	
	
	public CryptoMediaZone(MediaController controller, Direction direction, String mediaType, String originalHost, int originalRtpPort, int originalRtcpPort, boolean canMux, int proxyPort) throws UnknownHostException {
		super(controller, direction, mediaType, originalHost, originalRtpPort, originalRtcpPort, canMux, proxyPort);
		
		rtpConnection= new RtpConnection(originalHost, originalRtpPort);
		//suspend();
		
			
	}
	
	
	@Override
	public void setLocalProxy(String proxyHost) throws UnknownHostException, SocketException {
		super.setLocalProxy(proxyHost);	
		
	}
	
	
	private void attachChannel() {
		
		try {
			rtpConnection.setNegotiatedFormats(controller.getNegociatedFormats());	
			rtpConnection.bind();
			rtpConnection.setOtherParty(mediaZonePeer.channel, controller.getSdp().toString().getBytes());
			rtpConnection.setMode(ConnectionMode.SEND_RECV);
			
			
		} catch (Exception e) {
			LOG.error("Cannot set OtherParty!", e);
		}
		if(LOG.isTraceEnabled()) {
			LOG.trace("This  Party "+controller.toPrint());
			LOG.trace("Other Party "+controller.getOtherParty().toPrint());
			
		}
	
		controller.getOtherParty().setSecureSdp(rtpConnection.getLocalSdp());
		
		if(ConfigurationCache.isMediaDecryptionEnabled()){	
			audioChannel = rtpConnection.getAudioChannel();
			iceAuthenticator = audioChannel.getIceAuthenticator(); 
		    rtcpChannel = audioChannel.getRtcpChannel();
		    rtpChannel = audioChannel.getRtpChannel();
		    stunHandler=rtcpChannel.getStunHandler();
		    stunHandler.setAuthenticator(iceAuthenticator);
		    
		    dtlsHandler=rtcpChannel.getDtlsHandler();
		    dtlsHandler.setChannel(mediaZonePeer.channel);
		    
		    try {
				rtcpChannel.bind(mediaZonePeer.channel);
			} catch (SocketException e) {
				LOG.error("Cannot bind Channel", e);
			}
		    dtlsHandler.addListener(this);
		   
		    
		}
	    
	    
		
	}
	
	
	@Override
	public void start() throws UnknownHostException {	
		attachChannel();
		if(isRunning()) {
			LOG.warn("Media Proxy is just running, silently ignoring");
			return;
		}
		
		setRunning(true);
		
		executorService = Executors.newCachedThreadPool();
		
		if(ConfigurationCache.isMediaDecryptionEnabled()){
			mediaZonePeer.suspend();
			try {
				mediaZonePeer.socket.setSoTimeout(60000);
				socket.setSoTimeout(60000);
			} catch (SocketException e) {
				LOG.error(e);
			}
			executorService.execute(new HandShaker());
				
		}
		else {
			executorService.execute(new Proxy());
			if(!mediaZonePeer.isRunning())
				mediaZonePeer.start();	
		}
	
		
		
		if(LOG.isInfoEnabled()) {
			LOG.info("Started "+isRunning()+"->"+this.toPrint());		
		}
		
	}
	
	
	
	public String toPrint() {
		String value;
		
		value="(CMZ "+direction+") "+this.hashCode()+" "+mediaType+", Origin "+originalHost+":"+originalRtpPort+", LocalProxy "+proxyHost+":"+proxyPort;
		if(mediaZonePeer!=null)
				value+="[("+mediaZonePeer.direction+") "+mediaZonePeer.hashCode()+" "+mediaZonePeer.mediaType+", Origin "+mediaZonePeer.originalHost+":"+mediaZonePeer.originalRtpPort+", LocalProxy "+mediaZonePeer.proxyHost+":"+mediaZonePeer.proxyPort+"]";
		return value;
	}
	
	
	@Override
	public void onDtlsHandshakeComplete() {
		if(LOG.isTraceEnabled()) {
			LOG.trace("Handshake completed");
		}
		setRunning(true);
		
		executorService = Executors.newSingleThreadExecutor();
		executorService.execute(new Proxy());
		
		
		if(!mediaZonePeer.isRunning())
			try {
				mediaZonePeer.start();
			} catch (UnknownHostException e) {
				LOG.error("Cannot start MediaZonePeer");
			}	
		
	}
	
	public String getLocalSdp() {
		return rtpConnection.getLocalDescriptor();
	}
	
	@Override
	public void onDtlsHandshakeFailed(Throwable t) {
		if(LOG.isTraceEnabled()) {
			LOG.error("Handshake failed");			
		}
		try {
			finalize();
		} catch (IOException e) {
			LOG.error("",e);
		}
		
	}
	@Override
	public void onRtpFailure(Throwable e) {
		LOG.error("RTP Failure ",e);
		
	}
	@Override
	public void onRtpFailure(String message) {
		LOG.error("RTP Failure "+message);
		
	}
	@Override
	public void onRtcpFailure(Throwable e) {
		LOG.error("RTCP Failure ",e);
		
	}
	@Override
	public void onRtcpFailure(String message) {
		LOG.error("RTCP Failure "+message);
		
	}
	
	
	
	public void sendData(DatagramPacket dgram) throws IOException {

		//dgram.setAddress(getOriginalAddress());
		//dgram.setPort(getOriginalPort());
		if(dgram==null) {
			return;
		}
		if(dgram.getData().length>8) {	
			
				if(LOG.isTraceEnabled()) {
					RawPacket rtp=new RawPacket(dgram.getData(),0,dgram.getLength());
					LOG.trace("-++>[("+CryptoMediaZone.this.hashCode()+") PayloadType "+rtp.getPayloadType()+"]("+this.mediaType+", "+this.direction+") LocalProxy "+mediaZonePeer.proxyHost+":"+mediaZonePeer.proxyPort+"/"+dgram.getAddress()+":"+dgram.getPort()+"["+dgram.getLength()+"]");		
				}
			
		}
			
		mediaZonePeer.socket.send(dgram);	
		
	}
	
	
	public DatagramPacket receiveData() throws IOException {
		byte[] buffer=new byte[BUFFER];
		DatagramPacket dgram=new DatagramPacket(buffer, BUFFER);
		if(mediaZonePeer.socket==null) {
			throw new IOException("NULL Socket on "+this.toPrint());
		}
		mediaZonePeer.socket.receive(dgram);
		if(dgram==null||dgram.getLength()<8){
			LOG.warn("RTPPacket too short, sending anyway "+this.toPrint());
			return dgram;		
		}
		
		//Log 1 of every 300 packets
		
			if(LOG.isTraceEnabled()) {
				RawPacket rtp=new RawPacket(dgram.getData(),0,dgram.getLength());
				LOG.trace("<++-[("+CryptoMediaZone.this.hashCode()+") PayloadType "+rtp.getPayloadType()+"]("+this.mediaType+", "+this.direction+") LocalProxy "+mediaZonePeer.proxyHost+":"+mediaZonePeer.proxyPort+"/"+dgram.getAddress()+":"+dgram.getPort()+"["+dgram.getLength()+"]");
			}
		// handle by RTCP stuff
		byte[] result=handle(dgram);
		
		if(result==null) {
			return null;
		}
		dgram.setData(result);
		
		return dgram;
		
	}
	
	byte[] buffer=new byte[BUFFER];
	DatagramPacket dgram=new DatagramPacket(buffer, BUFFER);
	
	class HandShaker implements Runnable {
		@Override
		public void run() {
			while(isRunning())	{
				try {
					sendData(receiveData());	
				} catch (IOException e) {
					LOG.error("("+CryptoMediaZone.this.toPrint()+") "+e.getMessage());
					try {
						finalize();
					} catch (Throwable e1) {
						LOG.error("Cannot finalize stream!", e1);
					}
					break;
					
				}		
			}	
		}	
	}
	
	public byte[] handle(DatagramPacket dgram) {
		
		byte[] packet=dgram.getData();
		
		InetSocketAddress local  = new InetSocketAddress(mediaZonePeer.proxyHost, proxyPort);
		InetSocketAddress remote = new InetSocketAddress(dgram.getAddress().getHostAddress(), dgram.getPort());
	    
		if(rtcpChannel.canHandleRTCP(packet)) {
			if(LOG.isTraceEnabled()) {
				LOG.trace("RTCP Packet present");
			}
			return dgram.getData();
		}
		else if(rtcpChannel.canHandleDTLS(packet)) {
			if(LOG.isTraceEnabled()) {		
				LOG.trace("DTLS Packet present ");	
			}
			
			try {
				dtlsHandler.handle(
						packet,
						local,
						remote);
				if(LOG.isTraceEnabled()) {
					LOG.trace("replying from "+local.toString()+" to "+remote.toString());
				}
				
				return null;
			} catch (PacketHandlerException e) {
				LOG.error("Cannot handle DTLS", e);
				return null;
			}
				
			
		}
		else if(rtcpChannel.canHandleICE(packet)) {
			if(LOG.isTraceEnabled()) {
				LOG.trace("STUN-ICE Packet present");
			}
			
			 try {
				byte[] response=stunHandler.handle(
						packet,
						local,
						remote);
				if(LOG.isTraceEnabled()) {
					LOG.trace("ufrag SecurityCheck PASSED, replying from "+local.toString()+" to "+remote.toString());
				}
				return response;
			} catch (PacketHandlerException e) {
				LOG.error("Cannot handle ICE", e);
				return dgram.getData();
			}
			
		}	
		else {
			if(LOG.isTraceEnabled()) {
				LOG.trace("??? Packet present");
			}
		}
		return dgram.getData();
	}
	
	
	@Override
	public boolean equals(Object zone) {

		if (!(zone instanceof CryptoMediaZone)) {
			return false;
		}
		CryptoMediaZone otherZone=(CryptoMediaZone) zone;
		
		if (otherZone.getOriginalHost().equals(this.getOriginalHost()) &&
			otherZone.getController().equals(this.getController()) &&
			otherZone.getOriginalRtpPort()==this.getOriginalRtpPort()	&&
			otherZone.getMediaType().equals(this.getMediaType())&&
			otherZone.getDirection().equals(this.getDirection())) {
			return true;
		}
		return false;
		
	}
	
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((controller == null) ? 0 : controller.hashCode());
		result = prime * result + ((originalHost == null) ? 0 : originalHost.hashCode());
		result = prime * result + ((originalRtpPort == 0) ? 0 : originalRtpPort);
		result = prime * result + ((mediaType == null) ? 0 : mediaType.hashCode());
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		return result;

	}


	
	

}