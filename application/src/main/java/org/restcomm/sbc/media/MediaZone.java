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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.event.EventListenerList;

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
	
	private static int startPort	=ConfigurationCache.getMediaStartPort();
	private static int endPort	=ConfigurationCache.getMediaEndPort();
	
	private EventListenerList listenerList = new EventListenerList();
	
	protected SocketAddress remoteAddress;

	protected String host;
	protected String mediaType;
	private int logCounter=0;
	
	private boolean running;
	
	protected MediaZone mediaZonePeer;
	protected ExecutorService executorService;
	protected ScheduledExecutorService timeService;
	
	protected DatagramChannel rtpChannel;
	protected DatagramChannel rtcpChannel;
	
	protected int rtpPort;
	protected int rtcpPort;
	protected MediaMetadata metadata;
	
	protected int packetsSentCounter=0;
	protected int packetsRecvCounter=0;
	
	
	public MediaZone(MediaMetadata metadata) throws IOException {
		this.metadata=metadata;
		this.host=metadata.getIp();	
		this.mediaType=metadata.getMediaType();	
		this.rtpChannel = this.getAvailableChannel(host);
		//this.rtpChannel.configureBlocking(false);
		
		if(!metadata.isRtcpMultiplexed()) {
			this.rtcpPort=rtpPort+1;
			InetSocketAddress address = new InetSocketAddress(host, rtcpPort);
			rtcpChannel = DatagramChannel.open();
			rtcpChannel.socket().bind(address);
		}
		else {
			rtcpPort=-1;
		}
		
		
		
	}
	
	/** Constructor to attach to symetric port and multiplexed RTP/RTCP
	 * @throws IOException */
	public MediaZone(MediaMetadata metadata, int port) throws IOException {
		this.metadata=metadata;
		this.host=metadata.getIp();	
		this.mediaType=metadata.getMediaType();	
		
		InetSocketAddress address = new InetSocketAddress(host, port);
		rtpChannel = DatagramChannel.open();
		//rtpChannel.configureBlocking(false);
		rtpChannel.socket().bind(address);
		
		this.rtpPort=port;
		
		if(!metadata.isRtcpMultiplexed()) {
			this.rtcpPort=rtpPort+1;
			address = new InetSocketAddress(host, rtcpPort);
			rtcpChannel = DatagramChannel.open();
			rtcpChannel.socket().bind(address);
		}
		else {
			rtcpPort=-1;
		}
		
	}
	
	/** Constructor to attach to symetric port 
	 * @throws IOException */
	public MediaZone(MediaMetadata metadata, int rtpPort, int rtcpPort) throws IOException {
		this.metadata=metadata;
		this.host=metadata.getIp();	
		this.mediaType=metadata.getMediaType();	
		
		InetSocketAddress address = new InetSocketAddress(host, rtpPort);
		rtpChannel = DatagramChannel.open();
		rtpChannel.socket().bind(address);
		
		
		address = new InetSocketAddress(host, rtcpPort);
		rtcpChannel = DatagramChannel.open();
		rtcpChannel.socket().bind(address);
		
		this.rtpPort=rtpPort;
		this.rtcpPort=rtcpPort;
	}
	
	
	public void addMediaZoneListener(MediaZoneListener listener) {
	     listenerList.add(MediaZoneListener.class, listener);
	}
	
	protected void fireRTPTimeoutEvent(String message) {
	     // Guaranteed to return a non-null array
	     Object[] listeners = listenerList.getListenerList();
	     // Process the listeners last to first, notifying
	     // those that are interested in this event
	     for (int i = listeners.length-2; i>=0; i-=2) {
	         if (listeners[i]==MediaZoneListener.class) {             
	             ((MediaZoneListener)listeners[i+1]).onRTPTimeout(this, message);
	         }
	         
	     }
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
		
		if(LOG.isInfoEnabled()) {
			LOG.info("Starting "+this.toPrint());
			LOG.info("Starting "+this.toPrintPeer());
			LOG.info("Local Metadata "+this.metadata);
			LOG.info("Peer  Metadata "+mediaZonePeer.metadata);
		}
		executorService = Executors.newCachedThreadPool();
		executorService.execute(new Proxy());
		
		if(!metadata.isRtcpMultiplexed()&&rtcpPort>0) {
			executorService = Executors.newCachedThreadPool();
			executorService.execute(new RTCPProxy());
		}
		
		if(!mediaZonePeer.isRunning())
			mediaZonePeer.start();	
		
		if(LOG.isInfoEnabled()) {
			LOG.info("Started "+isRunning()+"->"+this.toPrint());		
		}
		
		timeService = Executors.newSingleThreadScheduledExecutor();
		timeService.scheduleWithFixedDelay(new MediaTimer(), 60, 60, TimeUnit.SECONDS);
		
	}
	
	public void finalize() throws IOException {
		if(LOG.isInfoEnabled()) {
			LOG.info("Finalizing mediaZone "+this.toPrint());
		}
		//ensure not traffic
		setRunning(false);
		
		if(mediaZonePeer!=null) {
			setRunning(false);
			if(mediaZonePeer.rtpChannel!=null&&mediaZonePeer.rtpChannel.isOpen()) {
				mediaZonePeer.rtpChannel.close();
				mediaZonePeer.rtcpChannel.close();
				mediaZonePeer.rtpChannel=null;
				mediaZonePeer.rtcpChannel=null;
			}
			if(mediaZonePeer.executorService!=null) {
				mediaZonePeer.executorService.shutdown();
				mediaZonePeer.executorService=null;
				mediaZonePeer=null;
			}	
			
		}
			
		if(rtpChannel!=null&&rtpChannel.isOpen()) {
        	rtpChannel.close();
        	rtcpChannel.close();
        	rtpChannel=null;
        	rtcpChannel=null;
		}
		
		if(executorService!=null) {
			executorService.shutdown();
			executorService=null;
		}
		if(timeService!=null) {
			timeService.shutdown();
			timeService=null;
		}
             
    }
	
	
	public String toPrint() {
		String value;
		
		value="      MediaZone "+(metadata.isRtcpMultiplexed()?"Muxed (":"(")+this.hashCode()+") "+mediaType+" "+host+" mp:RTP("+rtpPort+") RTCP("+(rtcpPort)+")]";
		
		return value;
	}
	
	public String toPrintPeer() {
		String value="";
		
		if(mediaZonePeer!=null)
				value="      MediaZone ["+mediaZonePeer.mediaType+" "+mediaZonePeer.host+" mp:RTP("+mediaZonePeer.rtpPort+") RTCP("+(mediaZonePeer.rtcpPort)+")]";
		
		return value;
	}
	
	public void rtpSend(ByteBuffer buffer) throws IOException {
		
		if(mediaZonePeer.getRemoteAddress()==null) {
			packetsSentCounter=0;
			
			return;
		}
			
		if(logCounter==500){
			if(LOG.isTraceEnabled()) {
				LOG.trace("--->RTP ("+this.hashCode()+") MM on "+host+":"+rtpPort+"/"+getRemoteAddress()+"["+buffer.array().length+"]");
				
			}
		}
		packetsSentCounter++;
		
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
		
		packetsRecvCounter++;
		
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
	
	public synchronized void attach(MediaZone mediaZone) {
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
	
	class MediaTimer implements Runnable {
	    @Override
	    public void run() {
	       
	       if(packetsSentCounter==0||packetsRecvCounter==0) {
	    	    // either leg is stuck	
	    	   fireRTPTimeoutEvent("Packets sent: "+packetsSentCounter+", received: "+packetsRecvCounter); 	
	       }
	       else {
	    	   packetsSentCounter=0;
	    	   packetsRecvCounter=0;	   
	       }
	    	        
	    }
	}
	
	public String getMediaType() {
		return mediaType;
	}

	public MediaZone getMediaZonePeer() {
		return mediaZonePeer;
	}

	private synchronized void setMediaZonePeer(MediaZone mediaZonePeer) {
		this.mediaZonePeer = mediaZonePeer;
	}
	
	
	
	private synchronized DatagramChannel getAvailableChannel(String host)  {
		DatagramChannel channel=null;
		InetSocketAddress address;
		
		int searchPort=startPort;
		if(startPort==0)
			startPort=5000;
		if(endPort<startPort)
			endPort=10000;
		
		// look for even ports;
		if(searchPort%2!=0)
			searchPort++;
		while (true) {
			//System.out.println("Looking an available DataChannel on "+host+":"+searchPort);
			if(LOG.isTraceEnabled()) {
				LOG.trace("Looking an available DataChannel on "+host+":"+searchPort);
			}
			try {		
		        address = new InetSocketAddress(host, searchPort);
		        channel = DatagramChannel.open();
				channel.socket().bind(address);
				rtpPort=searchPort;
				return channel;
			} catch (Exception e) {
				System.out.println("Error DataChannel on "+e.getMessage()+" but channel is open? "+channel.isOpen()+ " on "+host+":"+searchPort);
				try {
					channel.close();
					//System.out.println("but now is open? "+channel.isOpen());
				} catch (IOException e1) {
					// just loop
					
				}
				searchPort+=2;
				if(searchPort>endPort)
					searchPort=startPort;
				
			}
		}

	}
	
	
	public synchronized void setRemoteAddress(SocketAddress remoteAddress) {
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
	
	@Override
	public boolean equals(Object zone) {
		MediaZone otherZone=(MediaZone) zone;
		if (!(zone instanceof MediaZone)) {
			return false;
		}
		
		if (otherZone.getHost().equals(getHost())&&
			otherZone.getRTPPort()==getRTPPort()) {
			return true;
		}
		return false;
		
	}
	
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((mediaType == null) ? 0 : mediaType.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + rtpPort ;
		return result;

	}

	
	public static void main(String argv[]) {
		 String sdpText="v=0\n"+
				   "o=- 188809950206000236 2 IN IP4 127.0.0.1\n"+
				   "s=-\n"+
				   "t=0 0\n"+
				   "a=group:BUNDLE audio\n"+
				   "a=msid-semantic: WMS QTjs4Sqxmip7GwcQ1fZqeLcl4dEdAOzccBZN\n"+
				   "m=audio 54011 UDP/TLS/RTP/SAVPF 111 103 104 9 0 8 106 105 13 126\n"+
				   "c=IN IP4 192.168.88.3\n\r"+
				   "a=rtcp:54013 IN IP4 192.168.88.3\n"+
				   "a=candidate:2162125114 1 udp 2122260223 10.0.0.10 54010 typ host generation 0 network-id 2\n"+
				   "a=candidate:4221882981 1 udp 2122194687 192.168.88.2 54011 typ host generation 0 network-id 1\n"+
				   "a=candidate:2162125114 2 udp 2122260222 10.0.0.10 54012 typ host generation 0 network-id 2\n"+
				   "a=candidate:4221882981 2 udp 2122194686 192.168.88.2 54013 typ host generation 0 network-id 1\n"+
				   "a=candidate:3462174154 1 tcp 1518280447 10.0.0.10 9 typ host tcptype active generation 0 network-id 2\n"+
				   "a=candidate:3039243925 1 tcp 1518214911 192.168.88.2 9 typ host tcptype active generation 0 network-id 1\n"+
				   "a=candidate:3462174154 2 tcp 1518280446 10.0.0.10 9 typ host tcptype active generation 0 network-id 2\n"+
				   "a=candidate:3039243925 2 tcp 1518214910 192.168.88.2 9 typ host tcptype active generation 0 network-id 1\n"+
				   "a=candidate:229944241 2 udp 1685987070 181.165.120.41 54013 typ srflx raddr 192.168.88.2 rport 54013 generation 0 network-id 1\n"+
				   "a=candidate:229944241 1 udp 1685987071 181.165.120.41 54011 typ srflx raddr 192.168.88.2 rport 54011 generation 0 network-id 1\n"+
				   "a=ice-ufrag:lPqG\n"+
				   "a=ice-pwd:1iy3iKIrMn88BMA+abWT8hbZ\n"+
				   "a=fingerprint:sha-256 32:B6:92:32:22:5D:A4:0F:C4:B3:18:B7:23:FB:DF:82:6A:0C:13:82:AA:3E:89:6F:C7:83:1F:60:C2:9B:5D:68\n"+
				   "a=setup:actpass\n"+
				   "a=mid:audio\n"+
				   "a=extmap:1 urn:ietf:params:rtp-hdrext:ssrc-audio-level\n"+
				   "a=extmap:3 http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time\n"+
				   "a=sendrecv\n"+
				   "a=rtcp-mux\n"+
				   "a=rtpmap:111 opus/48000/2\n"+
				   "a=rtcp-fb:111 transport-cc\n"+
				   "a=fmtp:111 minptime=10;useinbandfec=1\n"+
				   "a=rtpmap:103 ISAC/16000\n"+
				   "a=rtpmap:104 ISAC/32000\n"+
				   "a=rtpmap:9 G722/8000\n"+
				   "a=rtpmap:0 PCMU/8000\n"+
				   "a=rtpmap:8 PCMA/8000\n"+
				   "a=rtpmap:106 CN/32000\n"+
				   "a=rtpmap:105 CN/16000\n"+
				   "a=rtpmap:13 CN/8000\n"+
				   "a=rtpmap:126 telephone-event/8000\n"+
				   "a=ssrc:819597054 cname:bHyZrjk3sM4Psqgb\n"+
				   "a=ssrc:819597054 msid:QTjs4Sqxmip7GwcQ1fZqeLcl4dEdAOzccBZN 3a3383c1-c87e-4739-9e94-19992cc433ff\n"+
				   "a=ssrc:819597054 mslabel:QTjs4Sqxmip7GwcQ1fZqeLcl4dEdAOzccBZN\n"+
				   "a=ssrc:819597054 label:3a3383c1-c87e-4739-9e94-19992cc433ff";
	
			MediaMetadata metadata;
			
			try {
				metadata = MediaMetadata.build(MediaMetadata.MEDIATYPE_AUDIO, sdpText);
				for(int i=0;i<10;i++) {
					Thread.sleep(1000);
					MediaZone zone=new MediaZone(metadata);
				}
				Thread.sleep(50000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		
	}
	

}