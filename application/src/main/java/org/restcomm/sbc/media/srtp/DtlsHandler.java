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
 
package org.restcomm.sbc.media.srtp; 
 
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel; 
import java.security.SecureRandom; 
import java.util.ArrayList; 
import java.util.Iterator; 
import java.util.List; 
 
import org.apache.log4j.Logger; 
import org.bouncycastle.crypto.tls.DTLSServerProtocol;  
import org.restcomm.sbc.media.PacketTransformer; 
 
/**
 * Handler to process DTLS-SRTP packets 
 *  
 * @author Henrique Rosa (henrique.rosa@telestax.com) 
 *  
 */ 
public class DtlsHandler { 
 
 private static final Logger logger = Logger.getLogger(DtlsHandler.class); 
 
 private DtlsSrtpServer server; 
 private DatagramChannel channel; 
 private volatile boolean handshakeComplete; 
 private volatile boolean handshakeFailed; 
 private volatile boolean handshaking; 
 private Thread worker; 
 private String localHashFunction; 
 private String remoteHashFunction; 
 private String remoteFingerprint; 
 private String localFingerprint; 
 
 private final List<DtlsListener> listeners; 
 
 /**
  * Handles encryption of outbound RTP packets for a given RTP stream 
  * identified by its SSRC 
  *  
  * @see http://tools.ietf.org/html/rfc5764#section-4.2 
  */ 
 private PacketTransformer srtpEncoder; 
 
 /**
  * Handles decryption of inbound RTP packets for a given RTP stream 
  * identified by its SSRC 
  *  
  * @see http://tools.ietf.org/html/rfc5764#section-4.2 
  */ 
 private PacketTransformer srtpDecoder; 
 
 /**
  * Handles encryption of outbound RTCP packets for a given RTP stream 
  * identified by its SSRC 
  *  
  * @see http://tools.ietf.org/html/rfc5764#section-4.2 
  */ 
 private PacketTransformer srtcpEncoder; 
 
 /**
  * Handles decryption of inbound RTCP packets for a given RTP stream 
  * identified by its SSRC 
  *  
  * @see http://tools.ietf.org/html/rfc5764#section-4.2 
  */ 
 private PacketTransformer srtcpDecoder; 
 
 public DtlsHandler(final DatagramChannel channel) { 
  this.listeners = new ArrayList<DtlsListener>(); 
  this.server = new DtlsSrtpServer(); 
  this.channel = channel; 
  this.handshakeComplete = false; 
  this.handshakeFailed = false; 
  this.handshaking = false; 
  this.localHashFunction = "SHA-256"; 
  this.remoteHashFunction = ""; 
  this.remoteFingerprint = ""; 
  this.localFingerprint = ""; 
 } 
 
 public DtlsHandler() { 
  this(null); 
 } 
 
 public DatagramChannel getChannel() { 
  return this.channel; 
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
 
 public Text getLocalFingerprint() { 
  if(this.localFingerprint == null || this.localFingerprint.isEmpty()) { 
   this.localFingerprint = this.server.generateFingerprint(this.localHashFunction); 
  } 
  return new Text(this.localFingerprint); 
 } 
  
 public void resetLocalFingerprint() { 
  this.localFingerprint = ""; 
 } 
  
 public String getLocalHashFunction() { 
        return localHashFunction; 
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
  * Generates an SRTP encoder for outgoing RTP packets using keying material 
  * from the DTLS handshake. 
  */ 
 private PacketTransformer generateRtpEncoder() { 
  return new SRTPTransformEngine(getMasterServerKey(), 
    getMasterServerSalt(), getSrtpPolicy(), getSrtcpPolicy()) 
    .getRTPTransformer(); 
 } 
 
 /**
  * Generates an SRTP decoder for incoming RTP packets using keying material 
  * from the DTLS handshake. 
  */ 
 private PacketTransformer generateRtpDecoder() { 
  return new SRTPTransformEngine(getMasterClientKey(), 
    getMasterClientSalt(), getSrtpPolicy(), getSrtcpPolicy()) 
    .getRTPTransformer(); 
 } 
 
 /**
  * Generates an SRTCP encoder for outgoing RTCP packets using keying 
  * material from the DTLS handshake. 
  */ 
 private PacketTransformer generateRtcpEncoder() { 
  return new SRTPTransformEngine(getMasterServerKey(), 
    getMasterServerSalt(), getSrtpPolicy(), getSrtcpPolicy()) 
    .getRTCPTransformer(); 
 } 
 
 /**
  * Generates an SRTCP decoder for incoming RTCP packets using keying 
  * material from the DTLS handshake. 
  */ 
 private PacketTransformer generateRtcpDecoder() { 
  return new SRTPTransformEngine(getMasterClientKey(), 
    getMasterClientSalt(), getSrtpPolicy(), getSrtcpPolicy()) 
    .getRTCPTransformer(); 
 } 
 
 /**
  * Decodes an RTP Packet 
  *  
  * @param packet 
  *            The encoded RTP packet 
  * @return The decoded RTP packet. Returns null is packet is not valid. 
  */ 
 public byte[] decodeRTP(byte[] packet, int offset, int length) { 
  return this.srtpDecoder.reverseTransform(packet, offset, length); 
 } 
 
 /**
  * Encodes an RTP packet 
  *  
  * @param packet 
  *            The decoded RTP packet 
  * @return The encoded RTP packet 
  */ 
 public byte[] encodeRTP(byte[] packet, int offset, int length) { 
  return this.srtpEncoder.transform(packet, offset, length); 
 } 
 
 /**
  * Decodes an RTCP Packet 
  *  
  * @param packet 
  *            The encoded RTP packet 
  * @return The decoded RTP packet. Returns null is packet is not valid. 
  */ 
 public byte[] decodeRTCP(byte[] packet, int offset, int length) { 
  return this.srtcpDecoder.reverseTransform(packet, offset, length); 
 } 
 
 /**
  * Encodes an RTCP packet 
  *  
  * @param packet 
  *            The decoded RTP packet 
  * @return The encoded RTP packet 
  */ 
 public byte[] encodeRTCP(byte[] packet, int offset, int length) { 
  return this.srtcpEncoder.transform(packet, offset, length); 
 } 
 
 public void handshake() { 
  if (!handshaking && !handshakeComplete) { 
   this.handshaking = true; 
   this.worker = new Thread(new HandshakeWorker()); 
   this.worker.start(); 
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
  this.server = new DtlsSrtpServer(); 
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
 } 
 
 private class HandshakeWorker implements Runnable { 
 
  public void run() { 
   SecureRandom secureRandom = new SecureRandom(); 
   DTLSServerProtocol serverProtocol = new DTLSServerProtocol( 
     secureRandom); 
   NioUdpTransport transport = new NioUdpTransport(getChannel()); 
 
   try { 
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
    logger.error("DTLS handshake failed: " + e.getMessage(), e); 
 
    // Declare handshake as failed 
    handshakeComplete = false; 
    handshakeFailed = true; 
    handshaking = false; 
 
    // Warn listeners handshake completed 
    fireHandshakeFailed(e); 
   } 
  } 
 
 } 
 
 
}