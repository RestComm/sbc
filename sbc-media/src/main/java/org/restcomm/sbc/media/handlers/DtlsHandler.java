/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2015, Telestax Inc and individual contributors
 * by the @authors tag. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.restcomm.sbc.media.handlers;

import java.nio.channels.DatagramChannel;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.tls.DTLSServerProtocol;
import org.bouncycastle.crypto.tls.UDPTransport;
import org.mobicents.media.server.impl.rtp.crypto.PacketTransformer;
import org.mobicents.media.server.impl.rtp.crypto.SRTPPolicy;
import org.mobicents.media.server.impl.rtp.crypto.SRTPTransformEngine;
import org.mobicents.media.server.impl.srtp.DtlsListener;
import org.restcomm.sbc.media.MediaController;
import org.restcomm.sbc.media.dtls.DtlsSrtpServer;


/**
 * Handler to process DTLS packets.
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 *
 */
public class DtlsHandler  {

    private static final AtomicLong THREAD_COUNTER = new AtomicLong(0);

    private static final Logger logger = Logger.getLogger(DtlsHandler.class);

    public static final int DEFAULT_MTU = 1500;

    public final static int MAX_DELAY = 10000;
    
    private UDPTransport transport;


    // Network properties
    private int mtu;


    // DTLS Handshake properties
   
    private DatagramChannel channel;
    private volatile boolean handshakeComplete;
    private volatile boolean handshakeFailed;
    private volatile boolean handshaking;
   // private Thread worker;
    private String localHashFunction;
    private String remoteHashFunction;
    private String remoteFingerprint;
    private String localFingerprint;
    
    // for crypto
    private String localCryptoSuite;
    private String remoteCryptoSuite;
    private String remoteMasterkey;
    private String localMasterkey;
    private long startTime;

    private final List<DtlsListener> listeners;

    // SRTP properties
    // http://tools.ietf.org/html/rfc5764#section-4.2
    private PacketTransformer srtpEncoder;
    private PacketTransformer srtpDecoder;
    private PacketTransformer srtcpEncoder;
    private PacketTransformer srtcpDecoder;

    private DtlsSrtpServer server;

    public DtlsHandler() {
        

        // Network properties
        this.mtu = DEFAULT_MTU;
        

        // Handshake properties
        this.server = MediaController.getDTLSServer();
        this.handshakeComplete = false;
        this.handshakeFailed = false;
        this.handshaking = false;
        this.localHashFunction = "SHA-256";
        this.remoteHashFunction = "";
        this.remoteFingerprint = "";
        this.localFingerprint = "";
        
        this.localCryptoSuite = "";
        this.remoteCryptoSuite = "";
        this.remoteMasterkey = "";
        this.localMasterkey = "";
        this.startTime = 0L;

        this.listeners = new ArrayList<DtlsListener>();
       
    }

    public void setChannel(DatagramChannel channel) {
        this.channel = channel;
    }

    public void addListener(DtlsListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public boolean isHandshakeComplete() {
        return handshakeComplete;
    }

    public boolean isHandshakeFailed() {
        return handshakeFailed;
    }

    public boolean isHandshaking() {
        return handshaking;
    }
    public String getLocalMasterkey() {
        if (this.localMasterkey == null || this.localMasterkey.isEmpty()) {
            this.localMasterkey = this.server.generateFingerprint(this.localCryptoSuite);
        }
        return localMasterkey;
    }

    public void resetLocalMasterkey() {
        this.localMasterkey = "";
    }
    
    public String getLocalHashFunction() {
        return localHashFunction;
    }

    public String getRemoteCryptoSuite() {
        return remoteCryptoSuite;
    }
    
    public String getLocalCryptoSuite() {
        return localCryptoSuite;
    }

    public String getRemoteMasterkeyValue() {
        return remoteMasterkey;
    }

    public String getRemoteMasterkey() {
        return remoteCryptoSuite + " inline:" + remoteMasterkey;
    }

    public void setRemoteMasterkey(String cryptoSuite, String key) {
        this.remoteCryptoSuite = cryptoSuite;
        this.remoteMasterkey = key;
    }
    
    
    public String getLocalFingerprint() {
        if (this.localFingerprint == null || this.localFingerprint.isEmpty()) {
            this.localFingerprint = this.server.generateFingerprint(this.localHashFunction);
        }
        return localFingerprint;
    }

    public void resetLocalFingerprint() {
        this.localFingerprint = "";
    }

    

    public String getRemoteHashFunction() {
        return remoteHashFunction;
    }

    public String getRemoteFingerprintValue() {
        return remoteFingerprint;
    }

    public String getRemoteFingerprint() {
        return remoteHashFunction + " " + remoteFingerprint;
    }

    public void setRemoteFingerprint(String hashFunction, String fingerprint) {
        this.remoteHashFunction = hashFunction;
        this.remoteFingerprint = fingerprint;
    }

    private byte[] getMasterServerKey() {
        return server.getSrtpMasterServerKey();
    }

    private byte[] getMasterServerSalt() {
        return server.getSrtpMasterServerSalt();
    }

    private byte[] getMasterClientKey() {
        return server.getSrtpMasterClientKey();
    }

    private byte[] getMasterClientSalt() {
        return server.getSrtpMasterClientSalt();
    }

    private SRTPPolicy getSrtpPolicy() {
        return server.getSrtpPolicy();
    }

    private SRTPPolicy getSrtcpPolicy() {
        return server.getSrtcpPolicy();
    }

    public PacketTransformer getSrtpDecoder() {
        return srtpDecoder;
    }

    public PacketTransformer getSrtpEncoder() {
        return srtpEncoder;
    }

    public PacketTransformer getSrtcpDecoder() {
        return srtcpDecoder;
    }

    public PacketTransformer getSrtcpEncoder() {
        return srtcpEncoder;
    }

    /**
     * Generates an SRTP encoder for outgoing RTP packets using keying material from the DTLS handshake.
     */
    private PacketTransformer generateRtpEncoder() {
        return new SRTPTransformEngine(getMasterServerKey(), getMasterServerSalt(), getSrtpPolicy(), getSrtcpPolicy())
                .getRTPTransformer();
    }

    /**
     * Generates an SRTP decoder for incoming RTP packets using keying material from the DTLS handshake.
     */
    private PacketTransformer generateRtpDecoder() {
        return new SRTPTransformEngine(getMasterClientKey(), getMasterClientSalt(), getSrtpPolicy(), getSrtcpPolicy())
                .getRTPTransformer();
    }

    /**
     * Generates an SRTCP encoder for outgoing RTCP packets using keying material from the DTLS handshake.
     */
    private PacketTransformer generateRtcpEncoder() {
        return new SRTPTransformEngine(getMasterServerKey(), getMasterServerSalt(), getSrtpPolicy(), getSrtcpPolicy())
                .getRTCPTransformer();
    }

    /**
     * Generates an SRTCP decoder for incoming RTCP packets using keying material from the DTLS handshake.
     */
    private PacketTransformer generateRtcpDecoder() {
        return new SRTPTransformEngine(getMasterClientKey(), getMasterClientSalt(), getSrtpPolicy(), getSrtcpPolicy())
                .getRTCPTransformer();
    }

    /**
     * Decodes an RTP Packet
     * 
     * @param packet The encoded RTP packet
     * @return The decoded RTP packet. Returns null is packet is not valid.
     */
    public byte[] decodeRTP(byte[] packet, int offset, int length) {
    	if(this.handshaking||srtpDecoder==null) {
    		return packet;
    	}
        return this.srtpDecoder.reverseTransform(packet, offset, length);
    }

    /**
     * Encodes an RTP packet
     * 
     * @param packet The decoded RTP packet
     * @return The encoded RTP packet
     */
    public byte[] encodeRTP(byte[] packet, int offset, int length) {
    	if(this.handshaking||srtpEncoder==null) {
    		return packet;
    	}
        return this.srtpEncoder.transform(packet, offset, length);
    }

    /**
     * Decodes an RTCP Packet
     * 
     * @param packet The encoded RTP packet
     * @return The decoded RTP packet. Returns null is packet is not valid.
     */
    public byte[] decodeRTCP(byte[] packet, int offset, int length) {
        return this.srtcpDecoder.reverseTransform(packet, offset, length);
    }

    /**
     * Encodes an RTCP packet
     * 
     * @param packet The decoded RTP packet
     * @return The encoded RTP packet
     */
    public byte[] encodeRTCP(byte[] packet, int offset, int length) {
        return this.srtcpEncoder.transform(packet, offset, length);
    }

    public void handshake() {
        if (!handshaking && !handshakeComplete) {
            this.handshaking = true;
            this.startTime = System.currentTimeMillis();
            handshaker();        
        }
    }

    private void fireHandshakeComplete() {
        if (this.listeners.size() > 0) {
            Iterator<DtlsListener> iterator = listeners.iterator();
            while (iterator.hasNext()) {
                iterator.next().onDtlsHandshakeComplete();
            }
        }
    }

    private void fireHandshakeFailed(Throwable e) {
        if (this.listeners.size() > 0) {
            Iterator<DtlsListener> iterator = listeners.iterator();
            while (iterator.hasNext()) {
                iterator.next().onDtlsHandshakeFailed(e);
            }
        }
    }

    public void reset() {
        // XXX try not to create the server every time!
        
        this.channel = null;
        this.srtcpDecoder = null;
        this.srtcpEncoder = null;
        this.srtpDecoder = null;
        this.srtpEncoder = null;
        this.remoteHashFunction = "";
        this.remoteFingerprint = "";
        this.localFingerprint = "";
        this.handshakeComplete = false;
        this.handshakeFailed = false;
        this.handshaking = false;
        this.startTime = 0L;
        this.listeners.clear();
    }

   
    public boolean canHandle(byte[] packet) {
        return canHandle(packet, packet.length, 0);
    }

   
    public boolean canHandle(byte[] packet, int dataLength, int offset) {
        // https://tools.ietf.org/html/rfc5764#section-5.1.2
        int contentType = packet[offset] & 0xff;
        return (contentType > 19 && contentType < 64);
    }

    
    private boolean hasTimeout() {
        return (System.currentTimeMillis() - this.startTime) > MAX_DELAY;
    }

    public void handshaker() {
         	
            SecureRandom secureRandom = new SecureRandom();
            DTLSServerProtocol serverProtocol = new DTLSServerProtocol(secureRandom);
            
            
            if(logger.isTraceEnabled()) {
            	logger.trace("DTLSHandler handshake started");
            }
            try {
            	transport = new UDPTransport(channel.socket(), mtu);
                // Perform the handshake in a non-blocking fashion
                serverProtocol.accept(server, transport);
                
                // Prepare the shared key to be used in RTP streaming
                server.prepareSrtpSharedSecret();

                // Generate encoders for DTLS traffic
                srtpDecoder = generateRtpDecoder();
                srtpEncoder = generateRtpEncoder();
                srtcpDecoder = generateRtcpDecoder();
                srtcpEncoder = generateRtcpEncoder();

                // Declare handshake as complete
                handshakeComplete = true;
                handshakeFailed = false;
                handshaking = false;

                // Warn listeners handshake completed
                fireHandshakeComplete();
            } catch (Exception e) {
                logger.error("DTLS handshake failed. Reason:", e);

                // Declare handshake as failed
                handshakeComplete = false;
                handshakeFailed = true;
                handshaking = false;

                // Warn listeners handshake completed
                fireHandshakeFailed(e);
            }
        
    }

	public UDPTransport getTransport() {
		return transport;
	}

}
