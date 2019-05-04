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
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.mobicents.media.server.impl.rtp.crypto.RawPacket;
import org.mobicents.media.server.io.sdp.SessionDescription;
import org.restcomm.sbc.ConfigurationCache;
import org.restcomm.sbc.media.srtp.RtpConnection;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    28 nov. 2016 22:12:36
 * @class   MediaZone.java
 *
 */
public class MediaZone  {
	
	protected static final int BUFFER= 8 * 1024;
	private static transient Logger LOG = Logger.getLogger(MediaZone.class);
	
	protected int originalRtpPort;
	protected int originalRtcpPort;
	protected boolean canMux;
	protected int rtpCountLog=ConfigurationCache.getRtpCountLog();
	protected String originalHost;
	protected String proxyHost;
	
	protected String mediaType;
	protected int logCounter=0;
	
	protected boolean running;
	protected boolean suspended;
	
	protected MediaZone mediaZonePeer;
	protected ExecutorService executorService;
	protected ExecutorService rtcpService;
	
	protected DatagramChannel channel;
	protected DatagramChannel rtcpChannel;
	protected DatagramSocket socket;
	protected DatagramSocket rtcpSocket;
	
	protected int packetsSentCounter=0;
	protected int packetsRecvCounter=0;
	
	protected int lastPacketsSentCounter=0;
	protected int lastPacketsRecvCounter=0;
	
	protected int proxyPort;
	protected Direction direction;
	
	protected InetSocketAddress proxyAddress;
	protected InetSocketAddress rtcpProxyAddress;
	private InetAddress originalAddress;
	protected MediaController controller;
	protected RtpConnection rtpConnection;
	
	public MediaZone(MediaController controller, Direction direction, String mediaType, String originalHost, int originalRtpPort, int originalRtcpPort, boolean canMux, int proxyPort) throws UnknownHostException {
		this.controller=controller;
		this.originalHost=originalHost;
		this.originalRtpPort=originalRtpPort;
		this.originalRtcpPort=originalRtcpPort;
		this.canMux=canMux;
		this.mediaType=mediaType;
		this.direction=direction;
		this.proxyPort=proxyPort;
		originalAddress=InetAddress.getByName(originalHost);
		rtpConnection= new RtpConnection(controller, originalHost, originalRtpPort);
		if(LOG.isTraceEnabled()) {
			LOG.trace("direction "+direction);
		}
			
	}
	
	public void setLocalProxy(String proxyHost) throws UnknownHostException, SocketException {
		this.proxyHost=proxyHost;
		
		proxyAddress = new InetSocketAddress(proxyHost, proxyPort);
		
		try {
			channel=DatagramChannel.open();
			channel.bind(proxyAddress);
		} catch (IOException e) {
			throw new SocketException(e.getMessage());
		}

		socket = channel.socket();
		
		if(LOG.isTraceEnabled()) {
			LOG.trace("Opened socket "+proxyAddress.toString()+" for "+this.toPrint());
		}
		
		if(!canMux) {
			rtcpProxyAddress = new InetSocketAddress(proxyHost, proxyPort+1);
			
			try {
				rtcpChannel=DatagramChannel.open();
				rtcpChannel.bind(rtcpProxyAddress);
			} catch (IOException e) {
				throw new SocketException(e.getMessage());
			}
	
			rtcpSocket = rtcpChannel.socket();
			
			if(LOG.isTraceEnabled()) {
				LOG.trace("Opened socket "+rtcpProxyAddress.toString()+" for "+this.toPrint());
			}
		}
		
		
		
		
	}
	
	public SessionDescription getLocalSdp() {
		return controller.getSdp();
	}
	
	protected synchronized boolean checkReady() {
		if(!isAttached())
			return false;
		if(originalHost==null || originalRtpPort==0)
			return false;
		if(proxyHost==null || proxyPort==0)
			return false;
		
		return true;
	}
	
	
	protected void fireProxyTimeoutEvent() {
		controller.getMediaSession().fireMediaTimeoutEvent(this);
	    
	}
	
	protected void fireProxyTerminatedEvent() {
		controller.getMediaSession().fireMediaTerminatedEvent(this);
	}
	
	protected void fireProxyReadyEvent() {
		controller.getMediaSession().fireMediaReadyEvent(this);
	}
	
	protected void fireProxyFailedEvent() {
		controller.getMediaSession().fireMediaFailedEvent(this);
	}
	
	public int getOriginalRtpPort() {
		return originalRtpPort;
	}
	
	public String getProxyHost() {
		return proxyHost;
	}
	
	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}
	
	public String getOriginalHost() {
		return originalHost;
	}	
	
	public int getProxyPort() {
		return proxyPort;
	}
	
	public void start() throws IOException {
		if(isRunning()) {
			LOG.warn("Media Proxy is just running, silently ignoring");
			return;
		}
		
		if(!checkReady()) {
			LOG.warn("Media Zone could not stablish proper routes, should dismiss? "+this.toPrint());
			
		}
		
		if(channel!=null && !channel.isConnected()) {
			LOG.debug("MUST Connect audio stream "+channel.getLocalAddress()+" to "+mediaZonePeer.getOriginalHost()+":"+mediaZonePeer.getOriginalRtpPort());
			//channel.connect(new InetSocketAddress(mediaZonePeer.getOriginalHost(), mediaZonePeer.getOriginalRtpPort()));
			
		}
		
		setRunning(true);
		
		executorService = Executors.newCachedThreadPool();
		executorService.execute(new Proxy());
		
		/*
		if(!mediaZonePeer.isRunning())
			mediaZonePeer.start();	
		*/
		if(LOG.isInfoEnabled()) {
			LOG.info("Started "+isRunning()+"->"+this.toPrint());		
		}
		
		if(!canMux) {	
			rtcpService = Executors.newCachedThreadPool();
			rtcpService.execute(new RtcpProxy());
			
			if(LOG.isInfoEnabled()) {
				LOG.info("Started "+isRunning()+"-> RtcpProxy");		
			}
		}
		
		
		
	}
	public void suspend() {
		if(LOG.isTraceEnabled()) {
			LOG.trace("Suspending mediaZone "+this.toPrint());
		}
		suspended=true;
		
	}
	
	public void resume() {
		if(LOG.isTraceEnabled()) {
			LOG.trace("Resuming mediaZone "+this.toPrint());
		}
		suspended=false;
	}

	public void finalize()  {
		//ensure not traffic
		setRunning(false);
	
		if(mediaZonePeer!=null) {
			setRunning(false);
			if(mediaZonePeer.socket!=null&&!mediaZonePeer.socket.isClosed()) {
				mediaZonePeer.socket.close();	
				mediaZonePeer.socket=null;
				if(LOG.isTraceEnabled()) {
					LOG.trace("Finalized mediaZone "+mediaZonePeer.toPrint());
				}
				
			}
			if(mediaZonePeer.executorService!=null) {
				mediaZonePeer.executorService.shutdown();
				mediaZonePeer.executorService=null;
				mediaZonePeer.fireProxyTerminatedEvent(); 
				
				
			}	
			if(!canMux) {
				if(mediaZonePeer.rtcpSocket!=null&&!mediaZonePeer.rtcpSocket.isClosed()) {
					mediaZonePeer.rtcpSocket.close();	
					mediaZonePeer.rtcpSocket=null;
					if(LOG.isTraceEnabled()) {
						LOG.trace("Finalized RTCP mediaZone ");
					}
					
				}
				if(mediaZonePeer.rtcpService!=null) {
					mediaZonePeer.rtcpService.shutdown();
					mediaZonePeer.rtcpService=null;		
				}	
			}
			
		}
		mediaZonePeer=null;
			
		if(socket!=null&&!socket.isClosed()) {
        	socket.close();
        	socket=null;
        	if(LOG.isTraceEnabled()) {
				LOG.trace("Finalized mediaZone "+toPrint());
			}
        	
		}
		
		if(executorService!=null) {
			executorService.shutdown();
			executorService=null;
			fireProxyTerminatedEvent(); 
		}
		
		if(!canMux) {
			if(rtcpSocket!=null&&!rtcpSocket.isClosed()) {
				rtcpSocket.close();	
				rtcpSocket=null;
				if(LOG.isTraceEnabled()) {
					LOG.trace("Finalized RTCP mediaZone ");
				}
				
			}
			if(rtcpService!=null) {
				rtcpService.shutdown();
				rtcpService=null;		
			}	
		}
		
           
    }
	
	
	public String toPrint() {
		String value;
		
		value="(UMZ "+direction+") "+this.hashCode()+" "+mediaType+", MUX("+canMux+") Origin "+originalHost+":"+originalRtpPort+"/"+originalRtcpPort+", LocalProxy "+proxyHost+":"+proxyPort;
		if(mediaZonePeer!=null)
				value+="[("+mediaZonePeer.direction+") "+mediaZonePeer.hashCode()+" "+mediaZonePeer.mediaType+", MUX("+mediaZonePeer.canMux+") Origin "+mediaZonePeer.originalHost+":"+mediaZonePeer.originalRtpPort+"/"+mediaZonePeer.originalRtcpPort+", LocalProxy "+mediaZonePeer.proxyHost+":"+mediaZonePeer.proxyPort+"]";
		return value;
	}
	
	public byte[] encodeRTP(byte[] data, int offset, int length) {
		/*
		if(LOG.isTraceEnabled()) {
			LOG.trace("VOID Decoding "+length+" bytes");
		}
		*/
		return ArrayUtils.subarray(data, offset, length);			
	}
	
	public byte[] decodeRTP(byte[] data, int offset, int length) {
		/*
		if(LOG.isTraceEnabled()) {
			LOG.trace("VOID Encoding "+length+" bytes");
		}
		*/
		return ArrayUtils.subarray(data, offset, length);		
	}
	
	public void send(DatagramPacket dgram) throws IOException {
		
		if(dgram==null)
			return;
		
		dgram.setAddress(mediaZonePeer.getOriginalAddress());
		dgram.setPort(mediaZonePeer.getOriginalRtpPort());
		//dgram.setData(mediaZonePeer.encodeRTP(dgram.getData(), 0, dgram.getLength()), 0, dgram.getLength() );	
		//LOG.trace("--->("+this.mediaType+", "+this.direction+") LocalProxy "+proxyHost+":"+proxyPort+"/"+dgram.getAddress()+":"+dgram.getPort()+"["+dgram.getLength()+"]");	
		//LOG.trace("---> via socket "+toPrint(socket));	
		if(dgram.getData().length>8) {
				if(logCounter==rtpCountLog){
					RawPacket rtp=new RawPacket(dgram.getData(),0,dgram.getLength());
					LOG.trace("--->[PayloadType "+rtp.getPayloadType()+"]("+this.mediaType+", "+this.direction+") LocalProxy "+proxyHost+":"+proxyPort+"/"+dgram.getAddress()+":"+dgram.getPort()+"["+dgram.getLength()+"]");
					logCounter=0;
				}	
		}
		else {
			LOG.warn("--->[PayloadType ?]("+this.mediaType+", "+this.direction+") LocalProxy "+proxyHost+":"+proxyPort+"/"+dgram.getAddress()+":"+dgram.getPort()+"["+dgram.getLength()+"]");
		}
		
		packetsSentCounter++;
					
		socket.send(dgram);	
		
		
	}
	
	byte[] rbuffer=new byte[BUFFER];
	DatagramPacket rdgram=new DatagramPacket(rbuffer, BUFFER);
	public DatagramPacket receiveRtcp() throws IOException {
	
		if(mediaZonePeer.rtcpSocket==null) {
			throw new IOException("NULL Socket on "+this.toPrint());
		}
		mediaZonePeer.rtcpSocket.receive(rdgram);
		
		if(rdgram==null||rdgram.getLength()<8){
			LOG.warn("RTCPPacket too short, not sending ["+(rdgram!=null?rdgram.getLength():"NULL")+"]");
			rdgram=new DatagramPacket(rbuffer, BUFFER);
			return null;
					
		}
		
		//LOG.trace("<---RTCP("+this.mediaType+", "+this.direction+") LocalProxy "+proxyHost+":"+(proxyPort+1)+"/"+rdgram.getAddress()+":"+rdgram.getPort()+"["+rdgram.getLength()+"]");	
		//LOG.trace("<---RTCP via socket "+toPrint(mediaZonePeer.rtcpSocket));		
		
		
			
		return rdgram;
		
	}
	
	public void sendRtcp(DatagramPacket rdgram) throws IOException {
		
		if(rdgram==null)
			return;
		
		rdgram.setAddress(mediaZonePeer.getOriginalAddress());
		rdgram.setPort(mediaZonePeer.getOriginalRtcpPort());
		
		
		//LOG.trace("--->RTCP("+this.mediaType+", "+this.direction+") LocalProxy "+proxyHost+":"+(proxyPort+1)+"/"+rdgram.getAddress()+":"+rdgram.getPort()+"["+rdgram.getLength()+"]");
		//LOG.trace("--->RTCP via socket "+toPrint(rtcpSocket));	
		rtcpSocket.send(rdgram);	
		
	}
	
	byte[] buffer=new byte[BUFFER];
	DatagramPacket dgram=new DatagramPacket(buffer, BUFFER);
	public DatagramPacket receive() throws IOException {
	
		if(mediaZonePeer.socket==null) {
			throw new IOException("NULL Socket on "+this.toPrint());
		}
		
		
		mediaZonePeer.socket.receive(dgram);
		
		if(dgram==null||dgram.getLength()<8){
			LOG.warn("RTPPacket too short on "+this.toPrint(mediaZonePeer.socket)+" not sending ["+(dgram!=null?dgram.getLength():"NULL")+"]");
			dgram=new DatagramPacket(buffer, BUFFER);
			return null;
					
		}
		//LOG.trace("<---("+this.mediaType+", "+this.direction+") LocalProxy "+proxyHost+":"+proxyPort+"/"+dgram.getAddress()+":"+dgram.getPort()+"["+dgram.getLength()+"]");		
		//LOG.trace("<--- via socket "+toPrint(mediaZonePeer.socket));	
		dgram.setData(mediaZonePeer.encodeRTP(dgram.getData(), 0, dgram.getLength()));
			
		logCounter++;
		
		if(logCounter==rtpCountLog){	
			RawPacket rtp=new RawPacket(dgram.getData(),0,dgram.getLength());
			LOG.trace("<---[PayloadType "+rtp.getPayloadType()+"]("+this.mediaType+", "+this.direction+") LocalProxy "+proxyHost+":"+proxyPort+"/"+dgram.getAddress()+":"+dgram.getPort()+"["+dgram.getLength()+"]");		
		}
		
		packetsRecvCounter++;
			
		return dgram;
		
	}
	
	public final void attach(MediaZone mediaZone) {
		if(!isAttached()) {
			setMediaZonePeer(mediaZone);		
		}
		if(!mediaZone.isAttached()) {
			mediaZone.attach(this);
		}
		
		if(checkReady()) {
			// Ready to start
			this.fireProxyReadyEvent();
		}	
		
	}
	
	public boolean isAttached() {
		return mediaZonePeer!=null;
	}
	
	class Proxy implements Runnable {
		@Override
		public void run() {
			while(isRunning())	{
				if(isSuspended()){
					LOG.warn("("+MediaZone.this.toPrint()+") already suspended");
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						continue;
					}
					continue;
				}
				try {
					send(receive());	
				} catch (Exception e) {
					if(!isRunning()||!mediaZonePeer.isRunning()) {
						LOG.warn("("+MediaZone.this.toPrint()+") not running, returning");
						return;
					}
						
					//LOG.error("("+MediaZone.this.toPrint()+") "+e.getMessage());
					continue;
					
				}		
			}
			LOG.trace("Ending Media proxy process "+MediaZone.this.toPrint());
		}	
	}
	
	class RtcpProxy implements Runnable {
		@Override
		public void run() {
			while(isRunning())	{
				if(isSuspended()){
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						continue;
					}
					continue;
				}
				try {
					sendRtcp(receiveRtcp());	
				} catch (Exception e) {
					if(!isRunning()||!mediaZonePeer.isRunning())
						return;
					//LOG.error("("+MediaZone.this.toPrint()+") "+e.getMessage());
					continue;
					
				}		
			}	
		}	
	}
	
	public boolean isStreaming() {
		if(LOG.isTraceEnabled()) {
			LOG.trace("Packets stats on "+this.toPrint());
			LOG.trace("Packets total/sent "+packetsSentCounter+"/"+lastPacketsSentCounter);
			LOG.trace("Packets total/recv "+packetsRecvCounter+"/"+lastPacketsRecvCounter);
		}
		if(packetsSentCounter>lastPacketsSentCounter &&
		   packetsRecvCounter>lastPacketsRecvCounter &&
		   packetsSentCounter>0 &&
		   packetsRecvCounter>0) {
				lastPacketsSentCounter=packetsSentCounter;
				lastPacketsRecvCounter=packetsRecvCounter;	
				return true; 	
       }
       else {
    	   return false;   
       }
		
	}
	
	
	public String getMediaType() {
		return mediaType;
	}

	public MediaZone getMediaZonePeer() {
		return mediaZonePeer;
	}

	protected void setMediaZonePeer(MediaZone mediaZonePeer) {
		this.mediaZonePeer = mediaZonePeer;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public boolean isSuspended() {
		return suspended;
	}
	
	protected synchronized void setRunning(boolean running) {
		this.running=running;
	}
	
	public int getPacketsSentCounter() {
		return packetsSentCounter;
	}
	public int getPacketsRecvCounter() {
		return packetsRecvCounter;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}
	
	@Override
	public boolean equals(Object zone) {
		MediaZone otherZone=(MediaZone) zone;
		if (!(zone instanceof MediaZone)) {
			return false;
		}
		
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
	
	public enum Direction {
		OFFER ("*offer"),
        ANSWER("answer");

        private final String text;

        private Direction(final String text) {
            this.text = text;
        }

        public static Direction getValueOf(final String text) {
        	Direction[] values = values();
            for (final Direction value : values) {
                if (value.toString().equals(text)) {
                    return value;
                }
            }
            throw new IllegalArgumentException(text + " is not a valid call direction.");
        }


        @Override
        public String toString() {
            return text;
        }
    }

	public Direction getDirection() {
		return direction;
	}

	public InetSocketAddress getProxyAddress() {
		return proxyAddress;
	}

	public InetAddress getOriginalAddress() {
		return originalAddress;
	}

	public MediaController getController() {
		return controller;
	}

	public boolean canMux() {
		return canMux;
	}

	public DatagramChannel getChannel() {
		return channel;
	}

	public RtpConnection getRtpConnection() {
		return rtpConnection;
	}

	public int getOriginalRtcpPort() {
		return originalRtcpPort;
	}

	private String toPrint(DatagramSocket socket) {
		return "Socket Bound to "+socket.getLocalSocketAddress()+" Connected to "+socket.getRemoteSocketAddress();
	}
	
	public enum Packet {
		DTLS ("dtls"),
        ICE("ice"),
        RTCP("rtcp"),
        RTP("rtp");

        private final String text;

        private Packet(final String text) {
            this.text = text;
        }

        public static Packet getValueOf(final String text) {
        	Packet[] values = values();
            for (final Packet value : values) {
                if (value.toString().equals(text)) {
                    return value;
                }
            }
            throw new IllegalArgumentException(text + " is not a valid packet.");
        }

        @Override
        public String toString() {
            return text;
        }
    }
	
}
