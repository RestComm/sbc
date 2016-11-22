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
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;
import org.mobicents.media.server.impl.rtp.crypto.RawPacket;
import org.restcomm.sbc.ConfigurationCache;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    30 sept. 2016 19:46:58
 * @class   MediaZone.java
 *
 */
public class MediaZone  {
	
	protected static final int BUFFER=256;
	private static transient Logger LOG = Logger.getLogger(MediaZone.class);
	
	private static final int startPort	=ConfigurationCache.getMediaStartPort();
	private static final int endPort	=ConfigurationCache.getMediaEndPort();
	
	protected SocketAddress remoteAddress;

	protected String host;
	protected String name;
	private int logCounter=0;
	
	private boolean running;
	
	protected MediaZone mediaZonePeer;
	protected ExecutorService executorService;
	
	protected DatagramChannel rtpChannel;
	protected DatagramChannel rtcpChannel;
	
	protected int rtpPort;
	protected int rtcpPort;
	protected MediaMetadata metadata;
	
	
	public MediaZone(MediaMetadata metadata) throws IOException {
		this.metadata=metadata;
		this.host=metadata.getIp();	
		this.name=metadata.getMediaType();	
		this.rtpChannel = this.getAvailableChannel(host);
		
		if(!metadata.isRtcpMultiplexed()) {
			this.rtcpPort=rtpPort+1;
			InetSocketAddress address = new InetSocketAddress(host, rtcpPort);
			rtcpChannel = DatagramChannel.open();
			rtcpChannel.socket().bind(address);
		}
		
		
		
	}
	
	/** Constructor to attach to symetric port and multiplexed RTP/RTCP
	 * @throws IOException */
	public MediaZone(MediaMetadata metadata, int port) throws IOException {
		this.metadata=metadata;
		this.host=metadata.getIp();	
		this.name=metadata.getMediaType();	
		
		InetSocketAddress address = new InetSocketAddress(host, port);
		rtpChannel = DatagramChannel.open();
		rtpChannel.socket().bind(address);
		
		this.rtpPort=port;
		
		if(!metadata.isRtcpMultiplexed()) {
			this.rtcpPort=rtpPort+1;
			address = new InetSocketAddress(host, rtcpPort);
			rtcpChannel = DatagramChannel.open();
			rtcpChannel.socket().bind(address);
		}
		
	}
	
	/** Constructor to attach to symetric port 
	 * @throws IOException */
	public MediaZone(MediaMetadata metadata, int rtpPort, int rtcpPort) throws IOException {
		this.metadata=metadata;
		this.host=metadata.getIp();	
		this.name=metadata.getMediaType();	
		
		InetSocketAddress address = new InetSocketAddress(host, rtpPort);
		rtpChannel = DatagramChannel.open();
		rtpChannel.socket().bind(address);
		
		
		address = new InetSocketAddress(host, rtcpPort);
		rtcpChannel = DatagramChannel.open();
		rtcpChannel.socket().bind(address);
		
		this.rtpPort=rtpPort;
		this.rtcpPort=rtcpPort;
	}
	
	public String getHost() {
		return host;
	}

	public int getRTPPort() {
		return rtpPort;
	}
	
	public int getRTCPPort() {
		return rtcpPort;
	}

	public void start() throws UnknownHostException {
		if(isRunning()) {
			throw new IllegalStateException("Media Proxy is just running!");
		}
		setRunning(true);
		
		LOG.info("Starting "+this.toPrint());
		LOG.info("Starting "+this.toPrintPeer());
		LOG.info("Local Metadata "+this.metadata);
		LOG.info("Peer  Metadata "+mediaZonePeer.metadata);
		
		executorService = Executors.newSingleThreadExecutor();
		executorService.execute(new Proxy());
		
		if(!metadata.isRtcpMultiplexed()) {
			executorService = Executors.newSingleThreadExecutor();
			executorService.execute(new RTCPProxy());
		}
		
		if(!mediaZonePeer.isRunning())
			mediaZonePeer.start();	
		
	}
	
	public void finalize() throws IOException {	
		
		setRunning(false);
		
		LOG.info("Finalizing mediaZone "+this.toPrint());
		
		if(mediaZonePeer.isRunning())
			mediaZonePeer.finalize();
		
		if(rtpChannel!=null&&rtpChannel.isOpen()) {
        	rtpChannel.close();
        	rtcpChannel.close();
		}
		
		executorService.shutdown();
		
		
        
		
             
    }
	
	
	public String toPrint() {
		String value;
		
		value="      MediaZone "+(metadata.isRtcpMultiplexed()?"Muxed (":"(")+this.hashCode()+") "+name+" "+host+" mp:RTP("+rtpPort+") RTCP("+(rtcpPort)+")]";
		
		return value;
	}
	
	public String toPrintPeer() {
		String value="";
		
		if(mediaZonePeer!=null)
				value="      MediaZone ["+mediaZonePeer.name+" "+mediaZonePeer.host+" mp:RTP("+mediaZonePeer.rtpPort+") RTCP("+(mediaZonePeer.rtcpPort)+")]";
		
		return value;
	}
	
	public void rtpSend(ByteBuffer buffer) throws IOException {
		if(mediaZonePeer.getRemoteAddress()==null) {
			return;
		}
			
		if(logCounter==500){
			if(LOG.isTraceEnabled()) {
				LOG.trace("--->RTP ("+this.hashCode()+") MM on "+host+":"+rtpPort+"/"+getRemoteAddress()+"["+buffer.array().length+"]");
				
			}
		}
			
		mediaZonePeer.rtpChannel.send(buffer, mediaZonePeer.getRemoteAddress());
		
	}
	
	
	public ByteBuffer rtpReceive() throws IOException {
		
		ByteBuffer buffer=ByteBuffer.allocate(BUFFER);
		
		SocketAddress socketAddress = rtpChannel.receive(buffer);
		buffer.flip();
		
		setRemoteAddress(socketAddress);
		
		
		byte[] data=buffer.array();
		
		RawPacket rtp=new RawPacket(data, 0, data.length);
		
		//Log 1 of every 500 packets
		if(logCounter==500){
			if(LOG.isTraceEnabled()) {
				LOG.trace("<---RTP ("+this.hashCode()+") [SSRC "+rtp.getSSRC()+", PayloadType: "+rtp.getPayloadType()+"] MM on "+host+":"+rtpPort+"/"+socketAddress+":"+"["+data.length+"]");
			}
			logCounter=0;
		}
		logCounter++;
		
		return buffer;
		
	}
	
	public void rtcpSend(ByteBuffer buffer) throws IOException {
		if(mediaZonePeer.getRemoteAddress()==null) {
			return;
		}
		
		if(LOG.isTraceEnabled()) {
			LOG.trace("--->RTCP("+this.hashCode()+") MM on "+host+":"+rtpPort+"/"+getRemoteAddress()+"["+buffer.array().length+"]");		
		}
		
		mediaZonePeer.rtcpChannel.send(buffer, mediaZonePeer.getRemoteAddress());
		
	}
	
	
	public ByteBuffer rtcpReceive() throws IOException {
		
		ByteBuffer buffer=ByteBuffer.allocate(BUFFER);
		
		InetSocketAddress socketAddress = (InetSocketAddress) rtcpChannel.receive(buffer);
		buffer.flip();
		
		setRemoteAddress(socketAddress);
		
		byte[] data=buffer.array();
		
		RawPacket rtp=new RawPacket(data,0,data.length);
		
		if(LOG.isTraceEnabled()) {
			LOG.trace("<---RTCP("+this.hashCode()+") [SSRC "+rtp.getRTCPSSRC()+"] MM on "+host+":"+rtpPort+"/"+socketAddress+":"+"["+data.length+"]");
		}
		
		return buffer;
		
	}
	
	public void attach(MediaZone mediaZone) {
		this.mediaZonePeer=mediaZone;
		mediaZonePeer.setMediaZonePeer(this);
		
	}
	
	class Proxy implements Runnable {
		@Override
		public void run() {
			while(isRunning())	{
				try {
					rtpSend(rtpReceive());	
				} catch (IOException e) {
					LOG.error("("+MediaZone.this.hashCode()+") "+e.getMessage());
					break;
				}		
			}	
		}	
	}
	
	class RTCPProxy implements Runnable {
		@Override
		public void run() {
			while(isRunning())	{
				try {
					rtcpSend(rtcpReceive());	
				} catch (IOException e) {
					LOG.error("("+MediaZone.this.hashCode()+") "+e.getMessage());
					break;
				}		
			}	
		}	
	}
	
	public String getName() {
		return name;
	}

	public MediaZone getMediaZonePeer() {
		return mediaZonePeer;
	}

	private void setMediaZonePeer(MediaZone mediaZonePeer) {
		this.mediaZonePeer = mediaZonePeer;
	}
	
	
	
	private synchronized DatagramChannel getAvailableChannel(String host)  {
		int searchPort=startPort;
		// look for even ports;
		if(searchPort%2!=0)
			searchPort++;
		while (true) {
			try {
				
		        InetSocketAddress address = new InetSocketAddress(host, searchPort);
		        DatagramChannel channel = DatagramChannel.open();
		        //channel.configureBlocking(false);
				channel.socket().bind(address);
				rtpPort=searchPort;
				return channel;
			} catch (Exception e) {
				searchPort+=2;
				if(searchPort>endPort)
					searchPort=startPort;
				
			}
		}

	}
	
	
	public void setRemoteAddress(SocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}
	public SocketAddress getRemoteAddress() {
		return remoteAddress;
	}
	public boolean isRunning() {
		return running;
	}
	
	protected synchronized void setRunning(boolean running) {
		this.running=running;
	}
	

}