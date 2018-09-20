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

package org.restcomm.sbc.media.dtls;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.crypto.tls.AlertDescription;
import org.bouncycastle.crypto.tls.AlertLevel;
import org.bouncycastle.crypto.tls.CertificateRequest;
import org.bouncycastle.crypto.tls.DefaultTlsServer;
import org.bouncycastle.crypto.tls.ExporterLabel;
import org.bouncycastle.crypto.tls.HashAlgorithm;
import org.bouncycastle.crypto.tls.ProtocolVersion;
import org.bouncycastle.crypto.tls.SRTPProtectionProfile;
import org.bouncycastle.crypto.tls.SignatureAlgorithm;
import org.bouncycastle.crypto.tls.SignatureAndHashAlgorithm;
import org.bouncycastle.crypto.tls.TlsECCUtils;
import org.bouncycastle.crypto.tls.TlsEncryptionCredentials;
import org.bouncycastle.crypto.tls.TlsFatalAlert;
import org.bouncycastle.crypto.tls.TlsSRTPUtils;
import org.bouncycastle.crypto.tls.TlsSignerCredentials;
import org.bouncycastle.crypto.tls.UseSRTPData;
import org.bouncycastle.util.Arrays;
import org.mobicents.media.server.impl.rtp.crypto.AlgorithmCertificate;
import org.mobicents.media.server.impl.rtp.crypto.CipherSuite;
import org.mobicents.media.server.impl.rtp.crypto.SRTPParameters;
import org.mobicents.media.server.impl.rtp.crypto.SRTPPolicy;




/**
 * 
 * This class represents the DTLS SRTP server connection handler.
 * 
 * The implementation follows the advise from Pierrick Grasland and Tim Panton on this forum thread:
 * http://bouncy-castle.1462172.n4.nabble.com/DTLS-SRTP-with-bouncycastle-1-49-td4656286.html
 * 
 * 
 * @author Ivelin Ivanov (ivelin.ivanov@telestax.com)
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 *
 */
public class DtlsSrtpServer extends DefaultTlsServer {
	
    private static final Logger LOGGER = Logger.getLogger(DtlsSrtpServer.class);

    // Certificate resources
    private final String[] certificateResources;
    private final String keyResource;
    private final AlgorithmCertificate algorithmCertificate;

	private String hashFunction = "";
    
    // the server response to the client handshake request
    // http://tools.ietf.org/html/rfc5764#section-4.1.1
	private UseSRTPData serverSrtpData;

	// Asymmetric shared keys derived from the DTLS handshake and used for the SRTP encryption/
	private byte[] srtpMasterClientKey;
	private byte[] srtpMasterServerKey;
	private byte[] srtpMasterClientSalt;
	private byte[] srtpMasterServerSalt;

	// Policies
	private SRTPPolicy srtpPolicy;
	private SRTPPolicy srtcpPolicy;

	private final ProtocolVersion minVersion;
	private final ProtocolVersion maxVersion;
	private final CipherSuite[] cipherSuites;

    public DtlsSrtpServer(ProtocolVersion minVersion, ProtocolVersion maxVersion, CipherSuite[] cipherSuites,
            String[] certificatesPath, String keyPath, AlgorithmCertificate algorithmCertificate) {
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
        this.cipherSuites = cipherSuites;
        this.certificateResources = certificatesPath;
        this.keyResource = keyPath;
        this.algorithmCertificate = algorithmCertificate;
    }

	public void notifyAlertRaised(short alertLevel, short alertDescription, String message, Exception cause) {
    	Level logLevel = (alertLevel == AlertLevel.fatal) ? Level.ERROR : Level.WARN; 
        LOGGER.log(logLevel, String.format("DTLS server raised alert (AlertLevel.%d, AlertDescription.%d, message='%s')", alertLevel, alertDescription, message), cause);
    }

    public void notifyAlertReceived(short alertLevel, short alertDescription) {
    	Level logLevel = (alertLevel == AlertLevel.fatal) ? Level.ERROR : Level.WARN; 
        LOGGER.log(logLevel, String.format("DTLS server received alert (AlertLevel.%d, AlertDescription.%d)", alertLevel, alertDescription));
    }
    
    @Override
    public int getSelectedCipherSuite() throws IOException {
    	if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("> getSelectedCipherSuite()");
		}
        /*
         * TODO RFC 5246 7.4.3. In order to negotiate correctly, the server MUST check any candidate cipher suites against the
         * "signature_algorithms" extension before selecting them. This is somewhat inelegant but is a compromise designed to
         * minimize changes to the original cipher suite design.
         */

        /*
         * RFC 4429 5.1. A server that receives a ClientHello containing one or both of these extensions MUST use the client's
         * enumerated capabilities to guide its selection of an appropriate cipher suite. One of the proposed ECC cipher suites
         * must be negotiated only if the server can successfully complete the handshake while using the curves and point
         * formats supported by the client [...].
         */
        boolean eccCipherSuitesEnabled = supportsClientECCCapabilities(this.namedCurves, this.clientECPointFormats);

        int[] cipherSuites = getCipherSuites();
        for (int i = 0; i < cipherSuites.length; ++i) {
            int cipherSuite = cipherSuites[i];

            if (Arrays.contains(this.offeredCipherSuites, cipherSuite)
                    && (eccCipherSuitesEnabled || !TlsECCUtils.isECCCipherSuite(cipherSuite))
                    && org.bouncycastle.crypto.tls.TlsUtils.isValidCipherSuiteForVersion(cipherSuite, serverVersion)) {
	            	if(LOGGER.isTraceEnabled()) {
	        			LOGGER.trace("> SelectedCipherSuite="+cipherSuite);
	        		}
                return this.selectedCipherSuite = cipherSuite;
            }
        }
        throw new TlsFatalAlert(AlertDescription.handshake_failure);
    }

    public CertificateRequest getCertificateRequest() {
    	if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("> getCertificateRequest()");
		}
		Vector<SignatureAndHashAlgorithm> serverSigAlgs = null;
		if (org.bouncycastle.crypto.tls.TlsUtils.isSignatureAlgorithmsExtensionAllowed(serverVersion)) {
			short[] hashAlgorithms = new short[] { HashAlgorithm.sha512, HashAlgorithm.sha384, HashAlgorithm.sha256, HashAlgorithm.sha224, HashAlgorithm.sha1 };
			short[] signatureAlgorithms = new short[] { algorithmCertificate.getSignatureAlgorithm(), SignatureAlgorithm.ecdsa };

			serverSigAlgs = new Vector<SignatureAndHashAlgorithm>();
			for (int i = 0; i < hashAlgorithms.length; ++i) {		
				for (int j = 0; j < signatureAlgorithms.length; ++j) {
					if(LOGGER.isTraceEnabled()) {
						LOGGER.trace("CertificateRequest, hash="+hashAlgorithms[i]+", sign="+signatureAlgorithms[j]);
					}
					serverSigAlgs.addElement(new SignatureAndHashAlgorithm(hashAlgorithms[i], signatureAlgorithms[j]));
				}
			}
		}
		CertificateRequest cRequest = new CertificateRequest(new short[] { algorithmCertificate.getClientCertificate() }, serverSigAlgs, null);
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("> CertificateRequest="+algorithmCertificate.getClientCertificate());
		}
		return cRequest;
    }

    public void notifyClientCertificate(org.bouncycastle.crypto.tls.Certificate clientCertificate) throws IOException {
    	if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("> notifyClientCertificate("+clientCertificate+")");
		}
        Certificate[] chain = clientCertificate.getCertificateList();
        LOGGER.info(String.format("Received client certificate chain of length %d", chain.length));
        
        for (int i = 0; i != chain.length; i++) {
            Certificate entry = chain[i];
            LOGGER.info(String.format("WebRTC Client certificate fingerprint:%s (%s)", TlsUtils.fingerprint(this.hashFunction, entry), entry.getSubject()));
        }
    }

    protected ProtocolVersion getMaximumVersion() {
        return maxVersion;
    }

    protected ProtocolVersion getMinimumVersion() {
        return minVersion;
    }
    
    @Override
    protected TlsSignerCredentials getECDSASignerCredentials() throws IOException {
    	if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("> getECDSASignerCredentials()");
			LOGGER.trace("> context        ="+context.getServerVersion());
			LOGGER.trace("> CERT resources ="+certificateResources.length);
			LOGGER.trace("> KEY  resource  ="+keyResource);
			
		}
        TlsSignerCredentials ecdsaCredentials = TlsUtils.loadSignerCredentials(context, certificateResources, keyResource, new SignatureAndHashAlgorithm(HashAlgorithm.sha256, SignatureAlgorithm.ecdsa));
        if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("> ECDSASignerCredentials="+ecdsaCredentials.toString());
		}
        return ecdsaCredentials;
    }
    
    @Override
    protected TlsEncryptionCredentials getRSAEncryptionCredentials() throws IOException {
    	if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("> getRSAEncryptionCredentials()");
		}
        return TlsUtils.loadEncryptionCredentials(context, certificateResources, keyResource);
    }

    @SuppressWarnings("unchecked")
    protected TlsSignerCredentials getRSASignerCredentials() throws IOException {
    	if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("> getRASignerCredentials()");
		}
    	/*
         * TODO Note that this code fails to provide default value for the client supported
         * algorithms if it wasn't sent.
         */
        SignatureAndHashAlgorithm signatureAndHashAlgorithm = null;
		Vector<SignatureAndHashAlgorithm> sigAlgs = supportedSignatureAlgorithms;
        if (sigAlgs != null) {
            for (int i = 0; i < sigAlgs.size(); ++i) {
                SignatureAndHashAlgorithm sigAlg = sigAlgs.elementAt(i);
                if (sigAlg.getSignature() == SignatureAlgorithm.rsa) {
                    signatureAndHashAlgorithm = sigAlg;
                    break;
                }
            }

            if (signatureAndHashAlgorithm == null) {
                return null;
            }
        }
        return TlsUtils.loadSignerCredentials(context, certificateResources, keyResource, signatureAndHashAlgorithm);
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public Hashtable<Integer, byte[]> getServerExtensions() throws IOException {
    	if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("> getServerExtensions()");
		}
    	Hashtable<Integer, byte[]> serverExtensions = (Hashtable<Integer, byte[]>) super.getServerExtensions();
        if (TlsSRTPUtils.getUseSRTPExtension(serverExtensions) == null) {
            if (serverExtensions == null) {
            	serverExtensions = new Hashtable<Integer, byte[]>();
            }
            TlsSRTPUtils.addUseSRTPExtension(serverExtensions, serverSrtpData );
        }
        if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("ServerExtensions(size)="+serverExtensions.size());
		}
        return serverExtensions;
    }
    
    @SuppressWarnings("rawtypes")
	@Override
    public void processClientExtensions(Hashtable newClientExtensions) throws IOException {
    	super.processClientExtensions(newClientExtensions);
    	if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("> processClientExtensions()");
		}
    	// set to some reasonable default value
    	int chosenProfile = SRTPProtectionProfile.SRTP_AES128_CM_HMAC_SHA1_80;
    	UseSRTPData clientSrtpData = TlsSRTPUtils.getUseSRTPExtension(newClientExtensions);
    	
    	for (int profile : clientSrtpData.getProtectionProfiles()) {
    		switch (profile) {
    			case SRTPProtectionProfile.SRTP_AES128_CM_HMAC_SHA1_32:
    			case SRTPProtectionProfile.SRTP_AES128_CM_HMAC_SHA1_80:
    			case SRTPProtectionProfile.SRTP_NULL_HMAC_SHA1_32:
    			case SRTPProtectionProfile.SRTP_NULL_HMAC_SHA1_80:
    				chosenProfile  = profile;
    				break;
    			default:
    		}
    	}
    	
    	// server chooses a mutually supported SRTP protection profile
    	// http://tools.ietf.org/html/draft-ietf-avt-dtls-srtp-07#section-4.1.2
		int[] protectionProfiles = { chosenProfile };
    	
    	// server agrees to use the MKI offered by the client
    	serverSrtpData = new UseSRTPData(protectionProfiles, clientSrtpData.getMki());
    }
    
    public byte[] getKeyingMaterial(int length) {
    	if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("> getKeyingMaterial(len)="+length);
		}
        return context.exportKeyingMaterial(ExporterLabel.dtls_srtp, null, length);
    }

    /**
     * 
     * @return the shared secret key that will be used for the SRTP session
     */
    public void prepareSrtpSharedSecret() {
    	if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("> prepareSrtpSharedSecret()");
		}
    	SRTPParameters srtpParams = SRTPParameters.getSrtpParametersForProfile(serverSrtpData.getProtectionProfiles()[0]);
    	final int keyLen = srtpParams.getCipherKeyLength();
    	final int saltLen = srtpParams.getCipherSaltLength();
    	
    	srtpPolicy = srtpParams.getSrtpPolicy();
    	srtcpPolicy = srtpParams.getSrtcpPolicy();
    	
        srtpMasterClientKey = new byte[keyLen];
        srtpMasterServerKey = new byte[keyLen];
        srtpMasterClientSalt = new byte[saltLen];
        srtpMasterServerSalt = new byte[saltLen];
        
        // 2* (key + salt lenght) / 8. From http://tools.ietf.org/html/rfc5764#section-4-2
        // No need to divide by 8 here since lengths are already in bits
        byte[] sharedSecret = getKeyingMaterial(2 * (keyLen + saltLen));
        
        /*
         * 
         * See: http://tools.ietf.org/html/rfc5764#section-4.2
         * 
         * sharedSecret is an equivalent of :
         * 
         * struct {
         *     client_write_SRTP_master_key[SRTPSecurityParams.master_key_len];
         *     server_write_SRTP_master_key[SRTPSecurityParams.master_key_len];
         *     client_write_SRTP_master_salt[SRTPSecurityParams.master_salt_len];
         *     server_write_SRTP_master_salt[SRTPSecurityParams.master_salt_len];
         *  } ;
         *
         * Here, client = local configuration, server = remote.
         * NOTE [ivelin]: 'local' makes sense if this code is used from a DTLS SRTP client. 
         *                Here we run as a server, so 'local' referring to the client is actually confusing. 
         * 
         * l(k) = KEY length
         * s(k) = salt lenght
         * 
         * So we have the following repartition :
         *                           l(k)                                 2*l(k)+s(k)   
         *                                                   2*l(k)                       2*(l(k)+s(k))
         * +------------------------+------------------------+---------------+-------------------+
         * + local key           |    remote key    | local salt   | remote salt   |
         * +------------------------+------------------------+---------------+-------------------+
         */
        System.arraycopy(sharedSecret, 0, srtpMasterClientKey, 0, keyLen); 
        System.arraycopy(sharedSecret, keyLen, srtpMasterServerKey, 0, keyLen);
        System.arraycopy(sharedSecret, 2*keyLen, srtpMasterClientSalt, 0, saltLen);
        System.arraycopy(sharedSecret, (2*keyLen+saltLen), srtpMasterServerSalt, 0, saltLen);    	
    }
    
    public SRTPPolicy getSrtpPolicy() {
    	return srtpPolicy;
    }
    
    public SRTPPolicy getSrtcpPolicy() {
    	return srtcpPolicy;
    }
    
    public byte[] getSrtpMasterServerKey() {
    	return srtpMasterServerKey;
    }
    
    public byte[] getSrtpMasterServerSalt() {
    	return srtpMasterServerSalt;
    }
    
    public byte[] getSrtpMasterClientKey() {
    	return srtpMasterClientKey;
    }
    
    public byte[] getSrtpMasterClientSalt() {
    	return srtpMasterClientSalt;
    }
    
	/**
	 * Gets the fingerprint of the Certificate associated to the server.
	 * 
	 * @return The fingerprint of the server certificate. Returns an empty
	 *         String if the server does not contain a certificate.
	 */
	public String generateFingerprint(String hashFunction) {
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("> generateFingerPrint("+hashFunction+")");
		}
		try {
			this.hashFunction = hashFunction;
			org.bouncycastle.crypto.tls.Certificate chain = TlsUtils.loadCertificateChain(certificateResources);
			Certificate certificate = chain.getCertificateAt(0);
			return TlsUtils.fingerprint(this.hashFunction, certificate);
		} catch (IOException e) {
			LOGGER.error("Could not get local fingerprint: "+ e.getMessage());
			return "";
		}
	}

    @Override
    public int[] getCipherSuites() {
        int[] cipherSuites = new int[this.cipherSuites.length];
        for (int i = 0; i < this.cipherSuites.length; i++) {
        	if(LOGGER.isTraceEnabled()) {
    			LOGGER.trace("> getCipherSuites("+this.cipherSuites[i].name()+")");
    		}
            cipherSuites[i] = this.cipherSuites[i].getValue();
        }
        
        return cipherSuites;
    }

}
