/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
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
 */

package org.restcomm.sbc.media;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;


import org.apache.log4j.Logger;
import org.bouncycastle.crypto.tls.ProtocolVersion;
import org.mobicents.media.io.ice.IceAuthenticator;
import org.mobicents.media.io.ice.IceComponent;
import org.mobicents.media.io.ice.IceHandler;
import org.mobicents.media.io.ice.events.IceEventListener;
import org.mobicents.media.io.ice.events.SelectedCandidatesEvent;


import org.mobicents.media.server.impl.rtp.RtpListener;

import org.mobicents.media.server.impl.rtp.crypto.AlgorithmCertificate;
import org.mobicents.media.server.impl.rtp.crypto.CipherSuite;
import org.mobicents.media.server.impl.rtp.crypto.DtlsSrtpServerProvider;

import org.mobicents.media.server.impl.srtp.DtlsHandler;
import org.mobicents.media.server.impl.srtp.DtlsListener;

import org.mobicents.media.server.io.network.PortManager;
import org.mobicents.media.server.io.network.channel.MultiplexedChannel;
import org.mobicents.media.server.io.sdp.format.RTPFormats;

import org.mobicents.media.server.spi.ConnectionMode;
import org.mobicents.media.server.utils.Text;
import org.restcomm.sbc.media.dtls.DtlsConfiguration;

/**
 * 
 * @author Yulian Oifa
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 *
 */
public class RtpChannel extends MultiplexedChannel implements DtlsListener, IceEventListener, Channel {

    private static final Logger logger = Logger.getLogger(RtpChannel.class);

    /** Tells UDP manager to choose port to bind this channel to */
    private final static int PORT_ANY = -1;
    private final PortManager portManager;
    private final PortManager localPortManager;

    // UDP Manager properties
   
 
    private static final String LOCALHOST = "127.0.0.1";

 
    private String bindAddress;
    private String localBindAddress;
   
    // Channel attributes
    private boolean bound;

    // Remote peer
    private SocketAddress remotePeer;



    private RtpHandler rtpHandler;
    private DtlsHandler dtlsHandler;
    private IceHandler stunHandler;
    private RtcpHandler rtcpHandler; // only used when rtcp-mux is enabled


    // WebRTC
    private boolean ice;
    private boolean secure;
    private boolean rtcpMux;

    // Listeners
    private RtpListener rtpListener;

    public RtpChannel(DtlsSrtpServerProvider dtlsServerProvider) {
        // Initialize MultiplexedChannel elements
        super();
        
        
        // Channel attributes
        
        this.portManager = new PortManager();
        this.localPortManager = new PortManager();

        // UDP Manager properties
    
        this.bindAddress 	  = LOCALHOST;
        this.localBindAddress = LOCALHOST;
       
        this.bound = false;
      
        // Protocol Handlers
        
        this.rtpHandler = new RtpHandler();
      
        this.rtcpHandler = new RtcpHandler();
    
        this.dtlsHandler = new DtlsHandler(dtlsServerProvider);
    
        this.stunHandler = new IceHandler(IceComponent.RTP_ID, this);
       
        // WebRTC
        this.secure = false;
        this.rtcpMux = false;

        
    }
    
    public RtpChannel(DtlsSrtpServerProvider dtlsServerProvider, DatagramChannel channel) {  	
    	this(dtlsServerProvider);
    	this.dataChannel=channel;
    }
    
    
    
    public void setRtpListener(RtpListener listener) {
        this.rtpListener = listener;
    }


    public RTPFormats getFormatMap() {
        return this.rtpHandler.getFormatMap();
    }

    /**
     * Sets the connection mode of the channel.<br>
     * Possible modes: send_only, recv_only, inactive, send_recv, conference, network_loopback.
     * 
     * @param connectionMode the new connection mode adopted by the channel
     */
    public void updateMode(ConnectionMode connectionMode) {
        switch (connectionMode) {
            case SEND_ONLY:
                this.rtpHandler.setReceivable(false);
                this.rtpHandler.setLoopable(false);
               
                break;
            case RECV_ONLY:
                this.rtpHandler.setReceivable(true);
                this.rtpHandler.setLoopable(false);
               
                break;
            case INACTIVE:
                this.rtpHandler.setReceivable(false);
                this.rtpHandler.setLoopable(false);
               
                break;
            case SEND_RECV:
            case CONFERENCE:
                this.rtpHandler.setReceivable(true);
                this.rtpHandler.setLoopable(false);
               
                break;
            case NETWORK_LOOPBACK:
                this.rtpHandler.setReceivable(false);
                this.rtpHandler.setLoopable(true);
               
                break;
            default:
                break;
        }

        

    }

    private void onBinding() {
//        // Set protocol handler priorities
//        this.rtpHandler.setPipelinePriority(RTP_PRIORITY);
//        if (this.rtcpMux) {
//            this.rtcpHandler.setPipelinePriority(RTCP_PRIORITY);
//        }
//        if (this.secure) {
//            this.stunHandler.setPipelinePriority(STUN_PRIORITY);
//        }
//
//        // Configure protocol handlers
       
      
        if (this.rtcpMux) {
            this.rtcpHandler.setChannel(this.dataChannel);
         
        }
//
//        if (this.secure) {
//            this.dtlsHandler.setPipelinePriority(DTLS_PRIORITY);
//            this.dtlsHandler.setChannel(this.dataChannel);
//            this.dtlsHandler.addListener(this);
//            this.handlers.addHandler(this.stunHandler);
//
//            // Start DTLS handshake
//            this.dtlsHandler.handshake();
//        }
    }
   
    
    /**
     * Binds socket to global bind address and specified port.
     * 
     * @param channel the channel
     * @param port the port to bind to
     * @throws IOException
     */
    public void bindLocal(DatagramChannel channel, int port) throws IOException {
        // select port if wildcarded
        if (port == PORT_ANY) {
            port = localPortManager.next();
        }

        // try bind
        IOException ex = null;
        for (int q = 0; q < 100; q++) {
            try {
                channel.bind(new InetSocketAddress(localBindAddress, port));
                ex = null;
                break;
            } catch (IOException e) {
                ex = e;
                logger.info("Failed trying to bind " + localBindAddress + ":" + port);
                port = localPortManager.next();
            }
        }

        if (ex != null) {
            throw ex;
        }
    }
    
    /**
     * Binds socket to global bind address and specified port.
     * 
     * @param channel the channel
     * @param port the port to bind to
     * @throws IOException
     */
    public void bind(DatagramChannel channel, int port) throws IOException {
        // select port if wildcarded
        if (port == PORT_ANY) {
            port = portManager.next();
        }

        // try bind
        IOException ex = null;
        for (int q = 0; q < 100; q++) {
            try {
                channel.bind(new InetSocketAddress(bindAddress, port));
                ex = null;
                break;
            } catch (IOException e) {
                ex = e;
                logger.info("Failed trying to bind " + bindAddress + ":" + port);
                port = portManager.next();
            }
        }

        if (ex != null) {
            throw ex;
        }
    }
    
    public void bind(DatagramChannel channel) throws IOException, SocketException {
       
        this.dataChannel = channel;
       
        // activate media elements
        onBinding();

        // Only bind channel if necessary
        if (!channel.socket().isBound()) {
            bind(channel, PORT_ANY);
        }
        this.bound = true;
    }

    public boolean isBound() {
        return this.bound;
    }
    
    public boolean isConnected() {
        return this.dataChannel != null && this.dataChannel.isConnected();
    }

    public boolean isAvailable() {
        // The channel is available is is connected
        boolean available = this.dataChannel != null && this.dataChannel.isConnected();
        // In case of WebRTC calls the DTLS handshake must be completed
        if (this.secure) {
            available = available && this.dtlsHandler.isHandshakeComplete();
        }
        return available;
    }

    public void setRemotePeer(SocketAddress address) {
        this.remotePeer = address;
        
        if (this.dataChannel != null) {
            if (this.dataChannel.isConnected()) {
                try {
                    disconnect();
                } catch (IOException e) {
                    logger.error(e);
                }
            }

              try {
                    this.dataChannel.connect(address);
                } catch (IOException e) {
                    logger.info("Can not connect to remote address , please check that you are not using local address - 127.0.0.X to connect to remote");
                    logger.error(e.getMessage(), e);
                }
            }
        
    }

    public String getExternalAddress() {
        return getExternalAddress();
    }
    
    

    public boolean hasExternalAddress() {
        return notEmpty(getExternalAddress());
    }

    private boolean notEmpty(String text) {
        return text != null && !text.isEmpty();
    }
    
    public void enableIce(IceAuthenticator authenticator) {
        if(!this.ice) {
            this.ice = true;
            this.stunHandler.setAuthenticator(authenticator);
            this.handlers.addHandler(this.stunHandler);
        }
    }
    
    public void disableIce() {
        if(this.ice) {
            this.ice = false;
            this.handlers.removeHandler(this.stunHandler);
        }
    }

    public void enableSRTP(String hashFunction, String remotePeerFingerprint) {
        if (!this.secure) {
            this.secure = true;
            this.dtlsHandler.setRemoteFingerprint(hashFunction, remotePeerFingerprint);
            this.dtlsHandler.addListener(this);

            // Setup the RTP handler
          
            this.rtpHandler.enableSrtp(this.dtlsHandler);

            // Setup the RTCP handler. RTCP-MUX channels only!
            if (this.rtcpMux) {
                this.rtcpHandler.enableSRTCP(this.dtlsHandler);
            }

            // Add handler to pipeline to handle incoming DTLS packets
            this.dtlsHandler.setChannel(this.dataChannel);
            this.handlers.addHandler(this.dtlsHandler);
        }
    }

    public void enableSRTP() {
        if (!this.secure) {
            this.secure = true;

            // Setup the RTP handler
        
            this.rtpHandler.enableSrtp(this.dtlsHandler);

            // Setup the RTCP handler. RTCP-MUX channels only!
            if (this.rtcpMux) {
                this.rtcpHandler.enableSRTCP(this.dtlsHandler);
            }
            
            // Add handler to pipeline to handle incoming DTLS packets
            this.dtlsHandler.setChannel(this.dataChannel);
            this.dtlsHandler.addListener(this);
            this.handlers.addHandler(this.dtlsHandler);
        }
    }

    public void setRemoteFingerprint(String hashFunction, String fingerprint) {
        this.dtlsHandler.setRemoteFingerprint(hashFunction, fingerprint);
    }

    public void disableSRTP() {
        if (this.secure) {
            this.secure = false;

            // setup the DTLS handler
            this.handlers.removeHandler(dtlsHandler);
            this.dtlsHandler.setRemoteFingerprint("", "");
            this.dtlsHandler.resetLocalFingerprint();

            // Setup the RTP handler
           
            this.rtpHandler.disableSrtp();

            // Setup the RTCP handler
            if (this.rtcpMux) {
                this.rtcpHandler.disableSRTCP();
            }
        }
    }

    public Text getWebRtcLocalFingerprint() {
        if (this.secure) {
            return new Text(this.dtlsHandler.getLocalFingerprint());
        }
        return new Text();
    }

    public void close() {
        
        super.close();
        reset();
        this.bound = false;
    }

    private void reset() {
        

        // RTP reset
      
        this.rtpHandler.reset();
       

        // RTCP reset
        if (this.rtcpMux) {
            this.rtcpHandler.reset();
            this.rtcpMux = false;
        }
        
        if(this.ice) {
            disableIce();
            this.stunHandler.reset();
        }

        // DTLS reset
        if (this.secure) {
            disableSRTP();
            this.dtlsHandler.reset();
        }
    }

    public void onDtlsHandshakeComplete() {
        logger.info("DTLS handshake completed for RTP candidate.");
        
    }

    public void onDtlsHandshakeFailed(Throwable e) {
        if (rtpListener != null) {
            this.rtpListener.onRtpFailure(e);
        }
    }

   
    @Override
    public void onSelectedCandidates(SelectedCandidatesEvent event) {
            // Connect channel to start receiving traffic from remote peer
//            this.connect(event.getRemotePeer());

            if (this.secure) {
                // Start DTLS handshake
                this.dtlsHandler.handshake();
            }
    }
    public void setChannel(DatagramChannel rtpChannel) {
		this.dataChannel=rtpChannel;
		
	}
    
    public static void main(String argv[]) throws IOException {
    	//Dtls Server Provider
		   
	    CipherSuite[] cipherSuites = new DtlsConfiguration().getCipherSuites();
	    
	    AlgorithmCertificate algorithmCertificate = AlgorithmCertificate.RSA;
	    DtlsSrtpServerProvider dtlsServerProvider = null;
	    
	    
	        dtlsServerProvider = 
	        		new DtlsSrtpServerProvider(	ProtocolVersion.DTLSv10,
	        									ProtocolVersion.DTLSv12,
	        									cipherSuites,
	        									System.getProperty("user.home")+"/certs/id_rsa.public",
	        									System.getProperty("user.home")+"/certs/id_rsa.private",
	        									algorithmCertificate);
	        
	       
	       
	    
    	RtpChannel channel=new RtpChannel(dtlsServerProvider);
    	
		DatagramChannel rtpChannel = DatagramChannel.open();
    	channel.open(rtpChannel);
    	channel.bind(rtpChannel);
    	
    }

	

}
