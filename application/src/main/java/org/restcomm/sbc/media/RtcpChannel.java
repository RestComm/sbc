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
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;

import org.apache.log4j.Logger;
import org.mobicents.media.io.ice.IceAuthenticator;
import org.mobicents.media.io.ice.IceComponent;
import org.mobicents.media.io.ice.IceHandler;
import org.mobicents.media.io.ice.events.IceEventListener;
import org.mobicents.media.io.ice.events.SelectedCandidatesEvent;
import org.mobicents.media.server.impl.rtp.RtpListener;
import org.mobicents.media.server.impl.srtp.DtlsListener;
import org.mobicents.media.server.io.network.channel.MultiplexedChannel;
import org.mobicents.media.server.utils.Text;
import org.restcomm.sbc.media.dtls.DtlsHandler;
;


/**
 * Channel for exchanging RTCP traffic
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class RtcpChannel extends MultiplexedChannel implements DtlsListener, IceEventListener {

	private static final Logger logger = Logger.getLogger(RtcpChannel.class);

	// Channel attribute
	
	private boolean bound;

	// Protocol handler pipeline
	private static final int STUN_PRIORITY = 3; // a packet each 400ms
//	private static final int RTCP_PRIORITY = 2; // a packet each 5s
//	private static final int DTLS_PRIORITY = 1; // only for a handshake
//	private final static int PORT_ANY = -1;
	
	private RtcpHandler rtcpHandler;
	private DtlsHandler dtlsHandler;
	private IceHandler stunHandler;
	
	// WebRTC
	private boolean ice;
	private boolean secure;
	
	// Listeners
	private RtpListener rtpListener;

	public RtcpChannel() {
		// Initialize MultiplexedChannel elements
		super();
		
		this.bound = false;

		// Protocol Handler pipeline
		
		this.rtcpHandler = new RtcpHandler();
        this.dtlsHandler = new DtlsHandler();
        this.stunHandler = new IceHandler(IceComponent.RTCP_ID, this);
		
		// WebRTC
		this.secure = false;
	}

	public void setRemotePeer(SocketAddress remotePeer) {
		if(logger.isTraceEnabled()) {
        	logger.trace("RemotePeer  "+remotePeer.toString());
        	logger.trace("Datachannel "+dataChannel);
        }
		if (this.dataChannel != null) {
			if (this.dataChannel.isConnected()) {
				try {
					this.dataChannel.disconnect();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}	
			
			try {
				this.dataChannel.connect(remotePeer);
			} catch (IOException e) {
				logger.error("Can not connect to remote address. Check that you are not using local address (127.0.0.X)", e);
			}
			
		}
		
	}
	
	public void setRtpListener(RtpListener rtpListener) {
		this.rtpListener = rtpListener;
	}
	
	public boolean isAvailable() {
		// The channel is available is is connected
		boolean available = this.dataChannel != null && this.dataChannel.isConnected();
		// In case of WebRTC calls the DTLS handshake must be completed
		if(this.secure) {
			available = available && this.dtlsHandler.isHandshakeComplete();
		}
		return available;
	}

	public boolean isBound() {
		return bound;
	}

	private void onBinding() {
		
		if(this.secure) {
			this.stunHandler.setPipelinePriority(STUN_PRIORITY);
		}
		
		// Protocol Handler pipeline
		this.rtcpHandler.setChannel(this.dataChannel);
		
		/*
		if(this.secure) {   
			this.dtlsHandler.setChannel(this.dataChannel);
			this.dtlsHandler.addListener(this);
			this.handlers.addHandler(this.stunHandler);
			
			// Start DTLS handshake
			this.dtlsHandler.handshake();
		} */
	}
	
	public boolean canHandleRTCP(byte[] packet) {
		return rtcpHandler.canHandle(packet);
		
	}
	public boolean canHandleDTLS(byte[] packet) {
		return dtlsHandler.canHandle(packet);
		
	}
	public boolean canHandleICE(byte[] packet) {
		return stunHandler.canHandle(packet);
		
	}
	

	/**
	 * Binds the channel to an address and port
	 * 
	 * @param isLocal
	 *            whether the connection is local or not
	 * @param port
	 *            The RTCP port. Usually the RTP channel gets the even port and
	 *            RTCP channel get the next port.
	 * @throws IOException
	 *             When the channel cannot be openend or bound
	 */
	public void bind(boolean isLocal, int port) throws IOException {
		/*
		try {
			// Open this channel with UDP Manager on first available address
			this.selectionKey = udpManager.open(this);
			this.dataChannel = (DatagramChannel) this.selectionKey.channel();
		} catch (IOException e) {
			throw new SocketException(e.getMessage());
		}
	*/
		// activate media elements
		onBinding();

		// bind data channel
		//this.udpManager.bind(this.dataChannel, port, isLocal);
		this.bound = true;
	}

	@Deprecated
	public void bind(DatagramChannel channel) throws SocketException {
		// External channel must be bound already
		 this.dataChannel = channel;
	       
	        // activate media elements
	        onBinding();

	        // Only bind channel if necessary
	        if (!channel.socket().isBound()) {
	            //bind(channel, PORT_ANY);
	        }
	        
	        this.bound = true;
	        
	        if(logger.isTraceEnabled()) {
	        	logger.trace("bind(channel) bound      " + channel.socket().isBound() );
	    		logger.trace("bind(channel) connected  " + isConnected() );
	    		logger.trace("bind(channel) secure     " + secure );
	    		logger.trace("bind(channel) available  " + isAvailable());
	    	}
	}

	
	/**
	 * Checks whether the channel is secure or not.
	 * 
	 * @return Whether the channel handles regular RTCP traffic or SRTCP (secure).
	 */
	public boolean isSecure() {
		return secure;
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
	
    public void enableSRTCP(String hashFunction, String remotePeerFingerprint) {
        if (!this.secure) {
            this.secure = true;
            this.dtlsHandler.setRemoteFingerprint(hashFunction, remotePeerFingerprint);

            // setup the SRTCP handler
            this.rtcpHandler.enableSRTCP(this.dtlsHandler);

            // Add handler to pipeline to handle incoming DTLS packets
            this.dtlsHandler.setChannel(this.dataChannel);
            
        }
    }

	public void enableSRTCP() {
        if (!this.secure) {
            this.secure = true;

            // setup the SRTCP handler
            this.rtcpHandler.enableSRTCP(this.dtlsHandler);

            // Add handler to pipeline to handle incoming DTLS packets
            this.dtlsHandler.setChannel(this.dataChannel);
            
        }
	}
	
    public void setRemoteFingerprint(String hashFunction, String fingerprint) {
        this.dtlsHandler.setRemoteFingerprint(hashFunction, fingerprint);
    }

    public void disableSRTCP() {
        if (this.secure) {
            this.secure = false;

            // setup the DTLS handler
            if (this.dtlsHandler != null) {
                this.dtlsHandler.setRemoteFingerprint("", "");
            }
            this.dtlsHandler.resetLocalFingerprint();

            // setup the SRTCP handler
            this.rtcpHandler.disableSRTCP();
        }
    }
	
	public Text getDtlsLocalFingerprint() {
		if(this.secure) {
			return new Text(this.dtlsHandler.getLocalFingerprint());
		}
		return new Text("");
	}
	
	@Override
	public void close() {
		/*
		 * Instruct the RTCP handler to leave the RTP session.
		 * 
		 * This will result in scheduling an RTCP BYE to be sent. Since the BYE
		 * is not sent right away, the datagram channel can only be closed once
		 * the BYE has been sent. So, the handler is responsible for closing the
		 * channel.
		 */
		
		this.bound = false;
		super.close();
		reset();
	}
	
	public void reset() {
		this.rtcpHandler.reset();
		
		if(this.ice) {
		    disableIce();
		    this.stunHandler.reset();
		}
		
		if(this.secure) {
		    disableSRTCP();
			this.dtlsHandler.reset();
		}
	}

	
	@Override
	public void onDtlsHandshakeComplete() {
		logger.info("DTLS handshake completed for RTCP candidate.\nJoining RTP session.");
		
	}

	
	@Override
	public void onDtlsHandshakeFailed(Throwable e) {
		logger.info("DTLS handshake failed.");
		if(this.rtpListener != null) {
			this.rtpListener.onRtcpFailure(e);
		}
	}

    @Override
    public void onSelectedCandidates(SelectedCandidatesEvent event) {
    	logger.info("onSelectedCandidates(), handshake must begin with event="+event);
    	logger.info("Remote Peer="+event.getRemotePeer());
    	
        try {
        	logger.info("dataChannel Local="+dataChannel.getLocalAddress()+", Remote="+dataChannel.getRemoteAddress());
            // Connect channel to start receiving traffic from remote peer
            this.connect(event.getRemotePeer());

            if (this.secure) {
                // Start DTLS handshake
                this.dtlsHandler.handshake();
            }
        } catch (IOException e) {
            this.rtpListener.onRtcpFailure(e);
        }
    }

	public RtcpHandler getRtcpHandler() {
		return rtcpHandler;
	}

	public DtlsHandler getDtlsHandler() {
		return dtlsHandler;
	}

	public IceHandler getStunHandler() {
		return stunHandler;
	}

	
	
}
