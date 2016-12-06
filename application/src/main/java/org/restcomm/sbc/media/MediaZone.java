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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;
import org.mobicents.media.server.impl.rtp.crypto.RawPacket;
import org.restcomm.sbc.call.Call.Direction;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    28 nov. 2016 22:12:36
 * @class   MediaZone.java
 *
 */
public class MediaZone  {
	
	private static final int BUFFER=1024;
	private static transient Logger LOG = Logger.getLogger(MediaZone.class);
	
	private EventListenerList listenerList = new EventListenerList();
	
	private InetAddress remoteAddress;
	protected int remotePort;
	
	protected int originalPort;
	
	protected String originalHost;
	protected String proxyHost;
	
	protected String mediaType;
	private int logCounter=0;
	
	protected boolean running;
	
	protected MediaZone mediaZonePeer;
	protected ExecutorService executorService;
	
	protected DatagramSocket socket;
	
	protected int packetsSentCounter=0;
	protected int packetsRecvCounter=0;
	
	protected int lastPacketsSentCounter=0;
	protected int lastPacketsRecvCounter=0;
	
	protected int proxyPort;
	protected Direction direction;
	
	private InetAddress proxyAddress;
	private InetAddress originalAddress;
	
	public MediaZone(Direction direction, String mediaType, String originalHost, int originalPort) throws UnknownHostException {
		this.originalHost=originalHost;
		this.originalPort=originalPort;
		this.mediaType=mediaType;
		this.direction=direction;
		originalAddress=InetAddress.getByName(originalHost);
			
	}
	
	public void setLocalProxy(String proxyHost, boolean create) throws UnknownHostException, SocketException {
		this.proxyHost=proxyHost;
		if(create) {
			PortManager portManager=PortManager.getPortManager();
			this.proxyPort=portManager.getNextAvailablePort();
		}
		else {
			if(!isAttached()) {
				throw new SocketException("No attached Peer on MediaZone "+this.toPrint());	
			}
			this.proxyPort=mediaZonePeer.proxyPort;
		}
		InetSocketAddress address = new InetSocketAddress(proxyHost, proxyPort);
		if(LOG.isTraceEnabled()) {
			LOG.trace("Opening ProxyPort "+proxyHost+":"+proxyPort+" for "+this.toPrint());
		}
		this.socket = new DatagramSocket(address);
		if(LOG.isTraceEnabled()) {
			LOG.trace("Opened ProxyPort "+socket+" for "+this.toPrint());
		}
		socket.setSoTimeout(1000);
		proxyAddress=address.getAddress();
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
	
	protected void fireRTPTerminatedEvent(String message) {
	     // Guaranteed to return a non-null array
	     Object[] listeners = listenerList.getListenerList();
	     // Process the listeners last to first, notifying
	     // those that are interested in this event
	     for (int i = listeners.length-2; i>=0; i-=2) {
	         if (listeners[i]==MediaZoneListener.class) {             
	             ((MediaZoneListener)listeners[i+1]).onRTPTerminated(this, message);
	         }
	         
	     }
	 }
	
	public int getOriginalPort() {
		return originalPort;
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
	
	public void start() throws UnknownHostException {
		if(isRunning()) {
			LOG.warn("Media Proxy is just running, silently ignoring");
			return;
		}
		
		setRunning(true);
		
		if(LOG.isInfoEnabled()) {
			LOG.info("Starting "+this.toPrint());
			
			
		}
		executorService = Executors.newCachedThreadPool();
		executorService.execute(new Proxy());
		
		
		if(!mediaZonePeer.isRunning())
			mediaZonePeer.start();	
		
		if(LOG.isInfoEnabled()) {
			LOG.info("Started "+isRunning()+"->"+this.toPrint());		
		}
		
		
		
	}


	public void finalize() throws IOException {
		if(LOG.isInfoEnabled()) {
			LOG.info("Finalizing mediaZone "+this.toPrint());
		}
		//ensure not traffic
		setRunning(false);
		
		if(mediaZonePeer!=null) {
			setRunning(false);
			if(mediaZonePeer.socket!=null&&!mediaZonePeer.socket.isClosed()) {
				mediaZonePeer.socket.close();
				
				mediaZonePeer.socket=null;
				
			}
			if(mediaZonePeer.executorService!=null) {
				mediaZonePeer.executorService.shutdown();
				mediaZonePeer.executorService=null;
				mediaZonePeer=null;
			}	
			
		}
			
		if(socket!=null&&!socket.isClosed()) {
        	socket.close();
        	socket=null;
        	
		}
		
		if(executorService!=null) {
			executorService.shutdown();
			executorService=null;
		}
		
        this.fireRTPTerminatedEvent("End");    
    }
	
	
	public String toPrint() {
		String value;
		
		value="("+direction+") "+this.hashCode()+" "+mediaType+", Origin "+originalHost+":"+originalPort+", LocalProxy "+proxyHost+":"+proxyPort;
		if(mediaZonePeer!=null)
				value+="[("+mediaZonePeer.direction+") "+mediaZonePeer.hashCode()+" "+mediaZonePeer.mediaType+", Origin "+mediaZonePeer.originalHost+":"+mediaZonePeer.originalPort+", LocalProxy "+mediaZonePeer.proxyHost+":"+mediaZonePeer.proxyPort+"]";
		return value;
	}
	
	public void send(DatagramPacket dgram) throws IOException {
		if(mediaZonePeer.getRemotePort()==0) {
			if(logCounter==500){
				if(LOG.isTraceEnabled()) {
					LOG.trace("--->Not Ready to send yet to "+mediaZonePeer.toPrint());
				}
			}
			return;
		}
		dgram.setAddress(mediaZonePeer.getRemoteAddress());
		dgram.setPort(mediaZonePeer.getRemotePort());
		
		if(logCounter==500){
			if(LOG.isTraceEnabled()) {
				RawPacket rtp=new RawPacket(dgram.getData(),0,dgram.getLength());
				LOG.trace("--->[Codec "+rtp.getPayloadType()+"]("+this.direction+") LocalProxy "+proxyHost+":"+proxyPort+"/"+dgram.getAddress()+":"+dgram.getPort()+"["+dgram.getLength()+"]");
				
			}
		}
			
		mediaZonePeer.socket.send(dgram);
		
	}
	
	
	public DatagramPacket receive() throws IOException {
		byte[] buffer=new byte[BUFFER];
		DatagramPacket dgram=new DatagramPacket(buffer, BUFFER);
		
		if(socket==null) {
			throw new IOException("NULL Socket on "+this.toPrint());
		}
		socket.receive(dgram);
		
		this.setRemoteAddress(dgram.getAddress());
		this.setRemotePort(dgram.getPort());
		
		//Log 1 of every 500 packets
		if(logCounter==500){
			if(LOG.isTraceEnabled()) {
				RawPacket rtp=new RawPacket(dgram.getData(),0,dgram.getLength());
				LOG.trace("<---[Codec "+rtp.getPayloadType()+"]("+this.direction+") LocalProxy "+proxyHost+":"+proxyPort+"/"+dgram.getAddress()+":"+dgram.getPort()+"["+dgram.getLength()+"]");
			}
			logCounter=0;
		}
		logCounter++;
		
		return dgram;
		
	}
	
	public void attach(MediaZone mediaZone) {
		setMediaZonePeer(mediaZone);	
		mediaZone.setMediaZonePeer(this);
		
	}
	
	public boolean isAttached() {
		return mediaZonePeer!=null;
	}
	
	class Proxy implements Runnable {
		@Override
		public void run() {
			while(isRunning())	{
				try {
					send(receive());	
				} catch (IOException e) {
					LOG.error("("+MediaZone.this.toPrint()+") "+e.getMessage());
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
	
	public boolean isStreaming() {
		if(packetsSentCounter>lastPacketsSentCounter &&
		   packetsRecvCounter>lastPacketsRecvCounter) {
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

	private void setMediaZonePeer(MediaZone mediaZonePeer) {
		this.mediaZonePeer = mediaZonePeer;
	}
	
	public int getRemotePort() {
		return remotePort;
	}
	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}
	public void setRemoteAddress(InetAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}
	public InetAddress getRemoteAddress() {
		return remoteAddress;
	}
	protected boolean isRunning() {
		return running;
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
			otherZone.getOriginalPort()==this.getOriginalPort()	&&
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
		result = prime * result + ((originalHost == null) ? 0 : originalHost.hashCode());
		result = prime * result + ((originalPort == 0) ? 0 : originalPort);
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

	public InetAddress getProxyAddress() {
		return proxyAddress;
	}

	public InetAddress getOriginalAddress() {
		return originalAddress;
	}
	
	
}
