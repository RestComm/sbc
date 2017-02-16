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
import java.nio.channels.DatagramChannel;
import org.apache.log4j.Logger;
import org.mobicents.media.io.ice.IceAuthenticatorImpl;
import org.mobicents.media.server.impl.rtp.SsrcGenerator;
import org.mobicents.media.server.io.sdp.format.RTPFormats;
import org.mobicents.media.server.spi.ConnectionMode;
import org.restcomm.sbc.media.dtls.DtlsConfiguration;
import org.restcomm.sbc.media.dtls.DtlsSrtpServerProvider;

/**
 * Abstract representation of a media channel with RTP and RTCP components.
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public abstract class MediaChannel {

	private static final Logger logger = Logger.getLogger(MediaChannel.class);
	

	// Media Session Properties
	protected final String mediaType;
	protected long ssrc;
	protected String cname;
	protected boolean rtcpMux;
	protected boolean open;
	private boolean ice;
	private boolean dtls;
	
	protected RTPFormats supportedFormats;
	protected RTPFormats offeredFormats;
	protected RTPFormats negotiatedFormats;
	protected boolean negotiated;
	
	// RTP Components
	
	protected RtpChannel rtpChannel;
	protected RtcpChannel rtcpChannel;
	
	
	// ICE components
	private final IceAuthenticatorImpl iceAuthenticator;


	@SuppressWarnings("unused")
	private String originalHost;


	@SuppressWarnings("unused")
	private int originalPort;

	/**
	 * Constructs a new media channel containing both RTP and RTCP components.
	 * 
	 * <p>
	 * The channel supports SRTP and ICE, but these features are turned off by
	 * default.
	 * </p>
	 * 
	 * @param mediaType
	 *            The type of media flowing in the channel
	 * @param originalPort 
	 * @param originalHost 
	 * @param wallClock
	 *            The wall clock used to synchronize media flows
	 * @param channelsManager
	 *            The RTP and RTCP channel provider
	 */
	protected MediaChannel(String mediaType, String originalHost, int originalPort) {
	    // Media Session Properties
	    this.mediaType = mediaType;
		this.ssrc = 0L;
		this.cname = "";
		this.rtcpMux = false;
		this.open = false;
		this.ice = false;
		this.dtls = false;
		
		this.originalHost=originalHost;
		this.originalPort=originalPort;
		

		// RTP Components
		
		this.rtpChannel = createRtpChannel(originalHost, originalPort);
		
		this.supportedFormats = new RTPFormats();
		this.offeredFormats = new RTPFormats();
		this.negotiatedFormats = new RTPFormats();
		this.negotiated = true;

		setFormats(this.supportedFormats);
		// ICE Components
		this.iceAuthenticator = new IceAuthenticatorImpl();
	}
	
	/**
	 * Gets the type of media handled by the channel.
	 * 
	 * @return The type of media
	 */
	public String getMediaType() {
		return mediaType;
	}

	/**
	 * Gets the synchronization source of the channel.
	 * 
	 * @return The unique SSRC identifier of the channel
	 */
	public long getSsrc() {
		return ssrc;
	}

	/**
	 * Sets the synchronization source of the channel.
	 * 
	 * @param ssrc
	 *            The unique SSRC identifier of the channel
	 */
	public void setSsrc(long ssrc) {
		this.ssrc = ssrc;
	}

	/**
	 * Gets the CNAME of the channel.
	 * 
	 * @return The CNAME associated with the channel
	 */
	public String getCname() {
		return cname;
	}

	/**
	 * Sets the CNAME of the channel.
	 * 
	 * <p>
	 * This attribute associates a media source with its endpoint, so it must be
	 * shared between all media channels owned by the same connection.
	 * </p>
	 * 
	 * @param cname The Canonical End-Point Identifier of the channel
	 */
	public void setCname(String cname) {
		this.cname = cname;
		
	}
	
    public String getExternalAddress() {
        if (this.rtpChannel.isBound()) {
            return this.rtpChannel.getExternalAddress();
        }
        return "";
    }
	
	/**
	 * Gets the address the RTP channel is bound to.
	 * 
	 * @return The address of the RTP channel. Returns empty String if RTP
	 *         channel is not bound.
	 */
	public String getRtpAddress() {
		if(this.rtpChannel.isBound()) {
			return this.rtpChannel.getLocalHost();
		}
		return "";
	}
	
	/**
	 * Gets the port where the RTP channel is bound to.
	 * 
	 * @return The port of the RTP channel. Returns zero if RTP channel is not
	 *         bound.
	 */
	public int getRtpPort() {
		if(this.rtpChannel.isBound()) {
			return this.rtpChannel.getLocalPort();
		}
		return 0;
	}

	/**
	 * Gets the address the RTCP channel is bound to.
	 * 
	 * @return The address of the RTCP channel. Returns empty String if RTCP
	 *         channel is not bound.
	 */
	public String getRtcpAddress() {
		if(this.rtcpMux) {
			return getRtpAddress();
		}
		
		
		return "";
	}
	
	/**
	 * Gets the port where the RTCP channel is bound to.
	 * 
	 * @return The port of the RTCP channel. Returns zero if RTCP channel is not
	 *         bound.
	 */
	public int getRtcpPort() {
		if(this.rtcpMux) {
			return getRtpPort();
		}
		
	
		return 0;
	}

	/**
	 * Enables the channel and activates it's resources.
	 */
	public void open() {
		// generate a new unique identifier for the channel
		this.ssrc = SsrcGenerator.generateSsrc();
		
		this.open = true;
		
		if(logger.isDebugEnabled()) {
			logger.debug(this.mediaType + " channel " + this.ssrc + " is open");
		}
	}

	/**
	 * Disables the channel and deactivates it's resources.
	 * 
	 * @throws IllegalStateException
	 *             When an attempt is done to deactivate the channel while
	 *             inactive.
	 */
	public void close() throws IllegalStateException {
		if (this.open) {
			// Close channels
			this.rtpChannel.close();
			
			
			if(logger.isDebugEnabled()) {
				logger.debug(this.mediaType + " channel " + this.ssrc + " is closed");
			}

			// Reset state
			reset();
			this.open = false;
		} else {
			throw new IllegalStateException("Channel is already inactive");
		}
	}

	/**
	 * Resets the state of the channel.
	 * 
	 * Should be invoked whenever there is intention of reusing the same channel
	 * for different calls.
	 */
	private void reset() {
		
		// Reset channels
		if (this.rtcpMux) {
			this.rtcpMux = false;
		}

		// Reset ICE
		if (this.ice) {
			disableICE();
		}

		// Reset WebRTC
		if (this.dtls) {
			disableDTLS();
		}
		
		// Reset statistics
		
		this.cname = "";
		this.ssrc = 0L;
	}

	/**
	 * Indicates whether the channel is active or not.
	 * 
	 * @return Returns true if the channel is active. Returns false otherwise.
	 */
	public boolean isOpen() {
		return open;
	}
	
	/**
	 * Indicates whether the channel is available (ready to use).
	 * 
	 * For regular SIP calls, the channel should be available as soon as it is
	 * activated.<br>
	 * But for WebRTC calls the channel will only become available as soon as
	 * the DTLS handshake completes.
	 * 
	 * @return Returns true if the channel is available. Returns false otherwise.
	 */
	public boolean isAvailable() {
		boolean available = this.rtpChannel.isAvailable();
		
		return available;
	}


	/**
	 * Sets the connection mode of the channel, affecting the receiving and
	 * transmitting capabilities of the underlying RTP component.
	 * 
	 * @param mode
	 *            The new connection mode of the RTP component
	 */
	public void setConnectionMode(ConnectionMode mode) {
		this.rtpChannel.updateMode(mode);
	}

	

	/**
	 * Binds the RTP and RTCP components to a suitable address and port.
	 * 
	 * @param isLocal
	 *            Whether the binding address is in local range.
	 * @param rtcpMux
	 *            Whether RTCP multiplexing is supported.<br>
	 *            If so, both RTP and RTCP components will be merged into one
	 *            channel only. Otherwise, the RTCP component will be bound to
	 *            the odd port immediately after the RTP port.
	 * @throws IOException
	 *             When channel cannot be bound to an address.
	 */
	public void bind(DatagramChannel localChannel, boolean rtcpMux) throws IOException, IllegalStateException {
		
		this.rtpChannel.setRtcpMux(rtcpMux);
		this.rtcpMux = rtcpMux;
		
		this.rtpChannel.bind(localChannel);
		
		if(logger.isDebugEnabled()) {
			logger.debug(this.mediaType + " RTP channel " + this.ssrc + " is bound to " + this.rtpChannel.getLocalHost() + ":" + this.rtpChannel.getLocalPort());
			if(rtcpMux) {
				logger.debug(this.mediaType + " is multiplexing RTCP");
			} 
		}
	}
	
	/**
	 * Indicates whether the media channel is multiplexing RTCP or not.
	 * 
	 * @return Returns true if using rtcp-mux. Returns false otherwise.
	 */
	public boolean isRtcpMux() {
		return this.rtcpMux;
	}

	/**
	 * Connected the RTP component to the remote peer.
	 * 
	 * <p>
	 * Once connected, the RTP component can only send/received traffic to/from
	 * the remote peer.
	 * </p>
	 * 
	 * @param address
	 *            The address of the remote peer
	 */
	public void connectRtp(SocketAddress address) {
		this.rtpChannel.setRemotePeer(address);
		
		if(logger.isDebugEnabled()) {
			logger.debug(this.mediaType + " RTP channel " + this.ssrc + " connected to remote peer " + address.toString());
		}
	}

	/**
	 * Connected the RTP component to the remote peer.
	 * 
	 * <p>
	 * Once connected, the RTP component can only send/received traffic to/from
	 * the remote peer.
	 * </p>
	 * 
	 * @param address
	 *            The address of the remote peer
	 * @param port
	 *            The port of the remote peer
	 */
	public void connectRtp(String address, int port) {
		this.connectRtp(new InetSocketAddress(address, port));
	}
	
	/**
	 * Binds the RTCP component to a suitable address and port.
	 * 
	 * @param isLocal
	 *            Whether the binding address must be in local range.
	 * @param port
	 *            A specific port to bind to
	 * @throws IOException
	 *             When the RTCP component cannot be bound to an address.
	 * @throws IllegalStateException
	 *             The binding operation is not allowed if ICE is active
	 */
	public void bindRtcp(boolean isLocal, int port) throws IOException, IllegalStateException {
		if(this.ice) {
			throw new IllegalStateException("Cannot bind when ICE is enabled");
		}
		
		this.rtcpMux = (port == this.rtpChannel.getLocalPort());
	}

	/**
	 * Connects the RTCP component to the remote peer.
	 * 
	 * <p>
	 * Once connected, the RTCP component can only send/received traffic to/from
	 * the remote peer.
	 * </p>
	 * 
	 * @param address
	 *            The address of the remote peer
	 */
	public void connectRtcp(SocketAddress remoteAddress) {
		this.connectRtcp(remoteAddress);
		
		if(logger.isDebugEnabled()) {
			logger.debug(this.mediaType + " RTCP channel " + this.ssrc + " has connected to remote peer " + remoteAddress.toString());
		}
	}

	/**
	 * Connects the RTCP component to the remote peer.
	 * 
	 * <p>
	 * Once connected, the RTCP component can only send/received traffic to/from
	 * the remote peer.
	 * </p>
	 * 
	 * @param address
	 *            The address of the remote peer
	 * @param port
	 *            A specific port to connect to
	 */
	public void connectRtcp(String address, int port) {
		this.connectRtcp(new InetSocketAddress(address, port));
		
	}
	/**
	 * Sets the supported codecs of the RTP components.
	 * 
	 * @param formats
	 *            The supported codecs resulting from SDP negotiation
	 */
	public void setFormats(RTPFormats formats) {
		this.negotiatedFormats=formats;
		this.rtpChannel.setFormatMap(formats);
		 
	}
	
	/**
	 * Gets the list of codecs <b>currently</b> applied to the Media Session.
	 * 
	 * @return Returns the list of supported formats if no codec negotiation as
	 *         happened over SDP so far.<br>
	 *         Returns the list of negotiated codecs otherwise.
	 */
	public RTPFormats getFormats() {
		if(this.negotiated) {
			return this.negotiatedFormats;
		}
		return this.supportedFormats;
	}
	
	/**
	 * Gets the supported codecs of the RTP components.
	 * 
	 * @return The codecs currently supported by the RTP component
	 */
	public RTPFormats getFormatMap() {
		return this.rtpChannel.getFormatMap();
	}
	
	
	/*
	 * ICE
	 */
	/**
	 * Enables ICE on the channel.
	 * 
	 * <p>
	 * An ICE-enabled channel will start an ICE Agent which gathers local
	 * candidates and listens to incoming STUN requests as a mean to select the
	 * proper address to be used during the call.
	 * </p>
	 * 
	 * @param externalAddress
	 *            The public address of the Media Server. Used for SRFLX
	 *            candidates.
	 * @param rtcpMux
	 *            Whether RTCP is multiplexed or not. Affects number of
	 *            candidates.
	 */
    public void enableICE(String externalAddress, boolean rtcpMux) {
        if (!this.ice) {
        	
            this.ice = true;
            this.rtcpMux = rtcpMux;
            this.iceAuthenticator.generateIceCredentials();
            
            // Enable ICE on RTP channels
            this.rtpChannel.enableIce(this.iceAuthenticator);
            
            if (logger.isDebugEnabled()) {
                logger.debug(this.mediaType + " channel " + this.ssrc + " enabled ICE");
            }
        }
    }

    /**
     * Disables ICE and closes ICE-related resources
     */
    public void disableICE() {
        if (this.ice) {
            this.ice = false;
            this.iceAuthenticator.reset();
            
            // Disable ICE on RTP channels
            this.rtpChannel.disableIce();
          

            if (logger.isDebugEnabled()) {
                logger.debug(this.mediaType + " channel " + this.ssrc + " disabled ICE");
            }
        }
    }

	/**
	 * Indicates whether ICE is active or not.
	 * 
	 * @return Returns true if ICE is enabled. Returns false otherwise.
	 */
	public boolean isIceEnabled() {
		return this.ice;
	}
	
	/**
	 * Gets the user fragment used in ICE negotiation.
	 * 
	 * @return The ICE ufrag. Returns an empty String if ICE is disabled on the
	 *         channel.
	 */
	public String getIceUfrag() {
	    return this.ice ? this.iceAuthenticator.getUfrag() : "";
	}

	/**
	 * Gets the password used in ICE negotiation.
	 * 
	 * @return The ICE password. Returns an empty String if ICE is disabled on
	 *         the channel.
	 */
	public String getIcePwd() {
	    return this.ice ? this.iceAuthenticator.getPassword() : "";
	}

	/*
	 * DTLS
	 */
	/**
	 * Enables DTLS on the channel. RTP and RTCP packets flowing through this
	 * channel will be secured.
	 * 
	 * <p>
	 * This method is used in <b>inbound</b> calls where the remote fingerprint is known.
	 * </p>
	 * 
	 * @param remoteFingerprint
	 *            The DTLS finger print of the remote peer.
	 */
    public void enableDTLS(String hashFunction, String remoteFingerprint) {
        if (!this.dtls) {
            this.rtpChannel.enableSRTP(hashFunction, remoteFingerprint);
            this.rtcpChannel.enableSRTCP(hashFunction, remoteFingerprint);
            this.dtls = true;

            if (logger.isDebugEnabled()) {
                logger.debug(this.mediaType + " channel " + this.ssrc + " enabled DTLS");
            }
        }
    }
	
    /**
     * Enables DTLS on the channel. RTP and RTCP packets flowing through this channel will be secured.
     * 
     * <p>
     * This method is used in <b>outbound</b> calls where the remote fingerprint is NOT known.<br>
     * Once the remote peer replies via SDP, the remote fingerprint must be set.
     * </p>
     * 
     * @throws IllegalStateException Cannot be invoked when DTLS is already enabled
     */
    public void enableDTLS() {
        if (!this.dtls) {
            this.rtpChannel.enableSRTP();
           
            this.dtls = true;

            if (logger.isDebugEnabled()) {
                logger.debug(this.mediaType + " channel " + this.ssrc + " enabled DTLS");
            }
        }
    }
    
    public void setRemoteFingerprint(String hashFunction, String fingerprint) {
        if (this.dtls) {
            this.rtpChannel.setRemoteFingerprint(hashFunction, fingerprint);
          
        }
    }

    /**
     * Disables DTLS and closes related resources.
     */
    public void disableDTLS() {
        if (this.dtls) {
            this.rtpChannel.disableSRTP();
            
            this.dtls = false;

            if (logger.isDebugEnabled()) {
                logger.debug(this.mediaType + " channel " + this.ssrc + " disabled DTLS");
            }
        }
    }
	
	/**
	 * Gets whether DTLS is enabled on the channel.
	 * 
	 * @return Returns true if DTLS is enabled. Returns false otherwise.
	 */
	public boolean isDtlsEnabled() {
		return this.dtls;
	}
	
	/**
	 * Gets the DTLS finger print.
	 * 
	 * @return The DTLS finger print. Returns an empty String if DTLS is not
	 *         enabled on the channel.
	 */
	public String getDtlsFingerprint() {
		if(this.dtls) {
			return this.rtpChannel.getWebRtcLocalFingerprint().toString();
		}
		return "";
	}

	
	public RtpChannel createRtpChannel(String originalHost, int originalPort)  {
	    
    	//Dtls Server Provider
		   
	    DtlsConfiguration configuration = new DtlsConfiguration();
	    
	    DtlsSrtpServerProvider dtlsServerProvider = null;
	    
	    
	        dtlsServerProvider = 
	        		new DtlsSrtpServerProvider(	configuration.getMinVersion(),
	        									configuration.getMaxVersion(),
	        									configuration.getCipherSuites(),
	        									configuration.getCertificatePath(), //System.getProperty("user.home")+"/certs/x509-server-ecdsa.cert.pem",
	        									configuration.getKeyPath(), //System.getProperty("user.home")+"/certs/x509-server-ecdsa.private.pem",
	        									configuration.getAlgorithmCertificate());
	        
	       
	    
	    rtcpChannel=new RtcpChannel(dtlsServerProvider);
    	return new RtpChannel(dtlsServerProvider,originalHost, originalPort);
    
    	
    }

	public IceAuthenticatorImpl getIceAuthenticator() {
		return iceAuthenticator;
	}

	public RtcpChannel getRtcpChannel() {
		return rtcpChannel;
	}
	
	public RtpChannel getRtpChannel() {
		return rtpChannel;
	}

}
