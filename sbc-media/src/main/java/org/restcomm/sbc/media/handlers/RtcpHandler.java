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

package org.restcomm.sbc.media.handlers;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.log4j.Logger;
import org.mobicents.media.server.impl.rtcp.RtcpHeader;
import org.mobicents.media.server.impl.rtcp.RtcpPacket;
import org.mobicents.media.server.impl.rtcp.RtcpPacketType;
import org.mobicents.media.server.impl.rtp.RtpPacket;

import org.mobicents.media.server.io.network.channel.PacketHandlerException;


/**
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class RtcpHandler implements PacketHandler {

    private static final Logger logger = Logger.getLogger(RtcpHandler.class);

    /** Time (in ms) between SSRC Task executions */
    private static final long SSRC_TASK_DELAY = 7000;

    /* Core elements */
    private DatagramChannel channel;
    private ByteBuffer byteBuffer;
   

    /** The elapsed time (milliseconds) since an RTCP packet was transmitted */
    private long tp;
    /** The time interval (milliseconds) until next scheduled transmission time of an RTCP packet */
    private long tn;

    /** Flag that is true if the application has not yet sent an RTCP packet */
    private AtomicBoolean initial;

    /** Flag that is true once the handler joined an RTP session */
    private AtomicBoolean joined;

    /* WebRTC */
    /** Checks whether communication of this channel is secure. WebRTC calls only. */
    private boolean secure;

    /** Handles the DTLS handshake and encodes/decodes secured packets. For WebRTC calls only. */
    private DtlsHandler dtlsHandler;

    public RtcpHandler() {
        
       
        this.byteBuffer = ByteBuffer.allocateDirect(RtpPacket.RTP_PACKET_MAX_SIZE);

        // rtcp stuff
       
        this.tp = 0;
        this.tn = -1;
        this.initial = new AtomicBoolean(true);
        this.joined = new AtomicBoolean(false);

        // webrtc
        this.secure = false;
        this.dtlsHandler = null;
        
    }

    

    public void setChannel(DatagramChannel channel) {
    	if(logger.isTraceEnabled()) {
    		logger.trace("Setting Channel connected? "+channel.isConnected());
    	}
        this.channel = channel;
    }

    /**
     * Gets whether the handler is in initial stage.<br>
     * The handler is in initial stage until it has sent at least one RTCP packet during the current RTP session.
     * 
     * @return true if not rtcp packet has been sent, false otherwise.
     */
    public boolean isInitial() {
        return initial.get();
    }

    /**
     * Gets whether the handler is currently joined to an RTP Session.
     * 
     * @return Return true if joined. Otherwise, returns false.
     */
    public boolean isJoined() {
        return joined.get();
    }

   

    /**
     * Secures the channel, meaning all traffic is SRTCP.
     * 
     * SRTCP handlers will only be available to process traffic after a DTLS handshake is completed.
     * 
     * @param remotePeerFingerprint The DTLS fingerprint of the remote peer. Use to setup DTLS keying material.
     */
    public void enableSRTCP(DtlsHandler dtlsHandler) {
        this.dtlsHandler = dtlsHandler;
        this.secure = true;
    }

    /**
     * Disables secure layer on the channel, meaning all traffic is treated as plain RTCP.
     */
    public void disableSRTCP() {
        this.dtlsHandler = null;
        this.secure = false;
    }

    @Override
    public boolean canHandle(byte[] packet) {
        return canHandle(packet, packet.length, 0);
    }

    @Override
    public boolean canHandle(byte[] packet, int dataLength, int offset) {
        byte b0 = packet[offset];
        int b0Int = b0 & 0xff;

        // Differentiate between RTP, STUN and DTLS packets in the pipeline
        // https://tools.ietf.org/html/rfc5764#section-5.1.2
        if (b0Int > 127 && b0Int < 192) {
            // RTP version field must equal 2
            int version = (b0 & 0xC0) >> 6;
            if (version == RtpPacket.VERSION) {
                // The payload type field of the first RTCP packet in a compound
                // packet must be equal to SR or RR.
                int type = packet[offset + 1] & 0x000000FF;
                if (type == RtcpHeader.RTCP_SR || type == RtcpHeader.RTCP_RR) {
                    /*
                     * The padding bit (P) should be zero for the first packet of a compound RTCP packet because padding should
                     * only be applied, if it is needed, to the last packet.
                     */
                    int padding = (packet[offset] & 0x20) >> 5;
                    if (padding == 0) {
                        /*
                         * TODO The length fields of the individual RTCP packets must add up to the overall length of the
                         * compound RTCP packet as received. This is a fairly strong check.
                         */
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public byte[] handle(byte[] packet, InetSocketAddress localPeer, InetSocketAddress remotePeer)
            throws PacketHandlerException {
        return handle(packet, packet.length, 0, localPeer, remotePeer);
    }

    @Override
    public byte[] handle(byte[] packet, int dataLength, int offset, InetSocketAddress localPeer, InetSocketAddress remotePeer)
            throws PacketHandlerException {
        // Do NOT handle data if have not joined RTP session
        if(!this.joined.get()) {
            return null;
        }
        
        // Do NOT handle data while DTLS handshake is ongoing. WebRTC calls only.
        if (this.secure && !this.dtlsHandler.isHandshakeComplete()) {
            return null;
        }

        // Check if incoming packet is supported by the handler
        if (!canHandle(packet, dataLength, offset)) {
            logger.warn("Cannot handle incoming packet!");
            throw new PacketHandlerException("Cannot handle incoming packet");
        }

        // Decode the RTCP compound packet
        RtcpPacket rtcpPacket = new RtcpPacket();
        if (this.secure) {
            byte[] decoded = this.dtlsHandler.decodeRTCP(packet, offset, dataLength);
            if (decoded == null || decoded.length == 0) {
                logger.warn("Could not decode incoming SRTCP packet. Packet will be dropped.");
                return null;
            }
            rtcpPacket.decode(decoded, 0);
        } else {
            rtcpPacket.decode(packet, offset);
        }

        // Trace incoming RTCP report
        if (logger.isDebugEnabled()) {
            logger.debug("\nRECEIVED " + rtcpPacket.toString());
        }

        // RTCP handler does not send replies
        return null;
    }

    private void sendRtcpPacket(RtcpPacket packet) throws IOException {
        // Do NOT attempt to send packet if have not joined RTP session
        if(this.joined.get()) {
            return;
        }
        
        // DO NOT attempt to send packet while DTLS handshake is ongoing
        if (this.secure && !this.dtlsHandler.isHandshakeComplete()) {
            return;
        }

        RtcpPacketType type = packet.hasBye() ? RtcpPacketType.RTCP_BYE : RtcpPacketType.RTCP_REPORT;
        if (this.channel != null && channel.isOpen() && channel.isConnected()) {
            // decode packet
            byte[] data = new byte[RtpPacket.RTP_PACKET_MAX_SIZE];
            packet.encode(data, 0);
            int dataLength = packet.getSize();

            // If channel is secure, convert RTCP packet to SRTCP. WebRTC calls only.
            if (this.secure) {
                data = this.dtlsHandler.encodeRTCP(data, 0, dataLength);
                dataLength = data.length;
            }

            // prepare buffer
            byteBuffer.clear();
            byteBuffer.rewind();
            byteBuffer.put(data, 0, dataLength);
            byteBuffer.flip();
            byteBuffer.rewind();

            // trace outgoing RTCP report
            if (logger.isDebugEnabled()) {
                logger.debug("\nSENDING " + packet.toString());
            }

            // Make double sure channel is still open and connected before sending
            if (channel.isOpen() && channel.isConnected()) {
                // send packet
                // XXX Should register on RTP statistics IF sending fails!
                this.channel.send(this.byteBuffer, this.channel.getRemoteAddress());
            } else {
                // cancel packet transmission
                if (logger.isDebugEnabled()) {
                    logger.debug("Could not send " + type + " packet because channel is closed or disconnected.");
                }
                return;
            }
            // If we send at least one RTCP packet then initial = false
            this.initial.set(false);

        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Could not send " + type + " packet because channel is closed or disconnected.");
            }
        }
    }

    public synchronized void reset() {
        if (joined.get()) {
            throw new IllegalStateException("Cannot reset handler while is part of active RTP session.");
        }

        
        this.tp = 0;
        this.tn = -1;
        this.initial.set(true);
        this.joined.set(false);

        if (this.secure) {
            disableSRTCP();
        }
    }

    /**
     * Disconnects and closes the datagram channel used to send and receive RTCP traffic.
     */
    private void closeChannel() {
        if (this.channel != null) {
            if (this.channel.isConnected()) {
                try {
                    this.channel.disconnect();
                } catch (IOException e) {
                    logger.warn(e.getMessage(), e);
                }
            }

            if (this.channel.isOpen()) {
                try {
                    this.channel.close();
                } catch (IOException e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }
    }

    public int compareTo(PacketHandler o) {
        if (o == null) {
            return 1;
        }
        return 0;
    }



}
