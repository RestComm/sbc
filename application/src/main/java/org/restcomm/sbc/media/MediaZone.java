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
import org.apache.log4j.Logger;
import org.restcomm.sbc.ConfigurationCache;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    30 sept. 2016 19:46:58
 * @class   MediaZone.java
 *
 */
public class MediaZone  {
	
	private static final int BUFFER=256;
	private static transient Logger LOG = Logger.getLogger(MediaZone.class);
	
	private static final int startPort	=ConfigurationCache.getMediaStartPort();
	private static final int endPort	=ConfigurationCache.getMediaEndPort();
	
	private InetAddress remoteAddress;
	private int remotePort;
	
	private String host;
	private String name;
	private int logCounter=0;
	
	private boolean running;
	
	private MediaZone mediaZonePeer;
	private ExecutorService executorService;
	
	private DatagramSocket socket;
	
	private int port;
	
	public MediaZone(String name, String host) throws UnknownHostException {
		this.host=host;
		this.name=name;
		socket=getAvailableSocket(host);	
		
	}
	/** Constructor to attach to symetric port */
	public MediaZone(String name, String host, int port) throws UnknownHostException, SocketException {
		this.host=host;
		this.name=name;
		InetSocketAddress address = new InetSocketAddress(host, port);
		socket = new DatagramSocket(address);
		socket.setSoTimeout(1000);
		this.port=port;
	}
	
	
	public String getHost() {
		return host;
	}


	public int getPort() {
		return port;
	}

	public void start() throws UnknownHostException {
		if(isRunning()) {
			throw new IllegalStateException("Media Proxy is just running!");
		}
		setRunning(true);
		
		LOG.info("Starting mediaZone "+this.toPrint());
		executorService = Executors.newSingleThreadExecutor();
		executorService.execute(new Proxy());
		
		if(!mediaZonePeer.isRunning())
			mediaZonePeer.start();	
		
	}
	
	public void finalize() {	
		
		setRunning(false);
		
		LOG.info("Finalizing mediaZone "+this.toPrint());
		
		if(mediaZonePeer.isRunning())
			mediaZonePeer.finalize();
		
		if(socket!=null&&!socket.isClosed())
        	socket.close();
		
		executorService.shutdown();
		
		
        
		
             
    }
	
	
	public String toPrint() {
		String value;
		
		value="("+this.hashCode()+")"+name+" "+host+" mp:"+port;
		if(mediaZonePeer!=null)
				value+="["+mediaZonePeer.name+" "+mediaZonePeer.host+" mp:"+mediaZonePeer.port+"]";
		return value;
	}
	
	public void send(DatagramPacket dgram) throws IOException {
		if(mediaZonePeer.getRemotePort()==0) {
			// still no reception at the other part
			return;
		}
		dgram.setAddress(mediaZonePeer.getRemoteAddress());
		dgram.setPort(mediaZonePeer.getRemotePort());
		
		if(logCounter<10){
			if(LOG.isTraceEnabled()) {
				LOG.trace("--->("+this.hashCode()+") MM on "+host+":"+port+"/"+dgram.getAddress()+":"+dgram.getPort()+"["+dgram.getLength()+"]");
				
			}
		}
			
		mediaZonePeer.socket.send(dgram);
		
	}
	
	
	public DatagramPacket receive() throws IOException {
		
		byte[] buffer=new byte[BUFFER];
		DatagramPacket dgram=new DatagramPacket(buffer, BUFFER);
		socket.receive(dgram);
		
		setRemoteAddress(dgram.getAddress());
		setRemotePort(dgram.getPort());
		
		RtpPacket rtp=new RtpPacket(dgram.getData(),0,dgram.getLength());
		
		if(logCounter<10){
			if(LOG.isTraceEnabled()) {
				LOG.trace("<---["+rtp.toString()+"]("+this.hashCode()+") MM on "+host+":"+port+"/"+dgram.getAddress()+":"+dgram.getPort()+"["+dgram.getLength()+"]");
			}
			logCounter++;
		}
		return dgram;
		
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
					send(receive());	
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
	
	private DatagramSocket getAvailableSocket(String host) throws UnknownHostException {
		int searchPort=startPort;
		while (true) {
			try {
				
		        InetSocketAddress address = new InetSocketAddress(host, searchPort);
		        socket = new DatagramSocket(address);
				socket.setSoTimeout(1000);
				port=searchPort;
				return socket;
			} catch (SocketException e) {
				searchPort++;
				if(searchPort>endPort)
					searchPort=startPort;
				
			}
		}

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
	public boolean isRunning() {
		return running;
	}
	
	private synchronized void setRunning(boolean running) {
		this.running=running;
	}
	

}
