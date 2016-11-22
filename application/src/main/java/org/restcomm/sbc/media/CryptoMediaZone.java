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
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.tls.ProtocolVersion;
import org.mobicents.media.server.impl.rtp.RtpListener;
import org.mobicents.media.server.impl.rtp.crypto.AlgorithmCertificate;
import org.mobicents.media.server.impl.rtp.crypto.CipherSuite;
import org.mobicents.media.server.impl.rtp.crypto.DtlsSrtpServerProvider;
import org.mobicents.media.server.impl.srtp.DtlsHandler;
import org.mobicents.media.server.impl.srtp.DtlsListener;
import org.restcomm.sbc.media.MediaZone.Proxy;
import org.restcomm.sbc.media.MediaZone.RTCPProxy;
import org.restcomm.sbc.media.dtls.DtlsConfiguration;




/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    4 nov. 2016 16:27:31
 * @class   CryptoMediaZone.java
 *
 */
public class CryptoMediaZone extends MediaZone implements DtlsListener, RtpListener {
	
	
	private static transient Logger LOG = Logger.getLogger(CryptoMediaZone.class);
	private DtlsHandler dtlsHandler;
	private RtpChannel channel;
	
	public CryptoMediaZone(MediaMetadata metadata, int port) throws IOException {
		super(metadata, port);
		channel.bind(rtpChannel, port);
		
		
	}
	public CryptoMediaZone(MediaMetadata metadata) throws IOException {
		super(metadata);
		init(metadata);

		channel.setChannel(rtpChannel);
		
	}
	
	private void init(MediaMetadata metadata) {
		//Dtls Server Provider
		   
	    CipherSuite[] cipherSuites = new DtlsConfiguration().getCipherSuites();
	    
	    AlgorithmCertificate algorithmCertificate = AlgorithmCertificate.RSA;
	    
	    try {
	        DtlsSrtpServerProvider dtlsServerProvider = 
	        		new DtlsSrtpServerProvider(	ProtocolVersion.DTLSv10,
	        									ProtocolVersion.DTLSv12,
	        									//metadata.getCipherSuites(),
	        									cipherSuites,
	        									System.getProperty("user.home")+"/certs/id_rsa.public",
	        									System.getProperty("user.home")+"/certs/id_rsa.private",
	        									algorithmCertificate);
	        
	        channel=new RtpChannel(dtlsServerProvider);
	        
	        String fingerprint = metadata.getFingerprint();
	        String hash = metadata.getFingerAlgorithm();
	        /*
	        dtlsHandler = new DtlsHandler(dtlsServerProvider);
	        dtlsHandler.setRemoteFingerprint(hash, fingerprint);   
	        
	        dtlsHandler.setChannel(rtpChannel);  
	    	dtlsHandler.addListener(this);
	    	*/
	    	channel.setChannel(rtpChannel);
	    	
	    	channel.setRtpListener(this);
	    	
	    } catch (RuntimeException e){
	    	LOG.error("DTLS",e);
	    }
		
	}
	
	@Override
	public void start() throws UnknownHostException {
		super.start();
		try {
			channel.setRemotePeer(getRemoteAddress());
			
		} 
		catch (RuntimeException e) {
			LOG.error("Runtime", e);
		}
		LOG.info("AVAILABLE:"+channel.isAvailable());
		LOG.info("BOUND    :"+channel.isBound());
		LOG.info("CONNECTED:"+channel.isConnected());
		LOG.info("LOCAL FIN:"+channel.getWebRtcLocalFingerprint());
		
		channel.enableSRTP();
	
		
	}
	
	@Override
	public ByteBuffer rtpReceive() throws IOException {
		ByteBuffer buffer=super.rtpReceive();
		byte[] packet=buffer.array();
		
		boolean canHandle = dtlsHandler.canHandle(packet);
		if(LOG.isTraceEnabled()) {
			if(canHandle)
				LOG.trace("Handling RTP DTLS Packet? "+dtlsHandler.canHandle(packet));
		}
			
		return buffer;
		
	}
	
	@Override
	public ByteBuffer rtcpReceive() throws IOException {
		ByteBuffer buffer=super.rtcpReceive();
		byte[] packet=buffer.array();
		
		boolean canHandle = dtlsHandler.canHandle(packet);
		if(LOG.isTraceEnabled()) {
			if(canHandle)
				LOG.trace("Handling RTCP DTLS Packet? "+dtlsHandler.canHandle(packet));
		}
								
		
		return buffer;
		
	}
	
	@Override
	public String toPrint() {
		String value;
		
		value="CryptoMediaZone "+(metadata.isRtcpMultiplexed()?"Muxed (":"(")+this.hashCode()+") "+name+" "+host+" mp:SRTP("+rtpPort+") SRTCP("+(rtcpPort)+")]";
		
		return value;
	}
	
	@Override
	public String toPrintPeer() {
		String value="";
		
		if(mediaZonePeer!=null)
				value="CryptoMediaZone ["+mediaZonePeer.name+" "+mediaZonePeer.host+" mp:SRTP("+mediaZonePeer.rtpPort+") SRTCP("+(mediaZonePeer.rtcpPort)+")]";
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
		
		if(!metadata.isRtcpMultiplexed()) {
			executorService = Executors.newSingleThreadExecutor();
			executorService.execute(new RTCPProxy());
		}
		if(!mediaZonePeer.isRunning())
			try {
				mediaZonePeer.start();
			} catch (UnknownHostException e) {
				LOG.error("Cannot start MediaZonePeer");
			}	
		
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
	

}