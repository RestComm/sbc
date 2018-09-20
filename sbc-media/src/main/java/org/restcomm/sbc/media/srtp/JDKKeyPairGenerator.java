package org.restcomm.sbc.media.srtp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.EllipticCurve;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemWriter;

import javax.security.auth.x500.X500Principal;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.TBSCertificateStructure;
import org.bouncycastle.asn1.x509.V3TBSCertificateGenerator;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.PrincipalUtil;
import org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.x509.X509V1CertificateGenerator;
import org.bouncycastle.x509.extension.X509ExtensionUtil;


import org.bouncycastle.crypto.AsymmetricCipherKeyPair; 
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator; 
import org.bouncycastle.crypto.KeyGenerationParameters; 
import org.bouncycastle.crypto.params.ECDomainParameters; 
import org.bouncycastle.crypto.params.ECKeyGenerationParameters; 
import org.bouncycastle.crypto.params.ECPrivateKeyParameters; 
import org.bouncycastle.crypto.params.ECPublicKeyParameters; 
import org.bouncycastle.math.ec.ECConstants; 
import org.bouncycastle.math.ec.ECPoint; 



public class JDKKeyPairGenerator {
	
	protected final static Logger LOGGER = Logger.getLogger(JDKKeyPairGenerator.class);
	private ECParameterSpec ecSpec;
	
	public JDKKeyPairGenerator() {
				EllipticCurve curve = new EllipticCurve(
	            new ECFieldFp(new BigInteger("883423532389192164791648750360308885314476597252960362792450860609699839")), // q
	            new BigInteger("7fffffffffffffffffffffff7fffffffffff8000000000007ffffffffffc", 16), // a            
	            new BigInteger("6b016c3bdcf18941d0d654921475ca71a9db2fb27d1d37796185c2942c0a", 16)); // b
				ecSpec = new ECParameterSpec(
	            curve,
	            ECPointUtil.decodePoint(curve, Hex.decode("020ffa963cdca8816ccc33b8642bedf905c3d358573d3f27fbbd3b3cb9aaaf")), // G
	            new BigInteger("883423532389192164791648750360308884807550341691627752275345424702807307"), // n
	            1); // h
	
	}
	
	public KeyPair createKeyPair() {
		KeyPairGenerator g;
		KeyPair pair = null;
		try {
			Security.addProvider(new BouncyCastleProvider());
			g = KeyPairGenerator.getInstance("ECDSA", "BC");
			g.initialize(ecSpec, new SecureRandom());
			pair = g.generateKeyPair();
			
			writePemFile(pair.getPrivate(),"ECDSA PRIVATE KEY", "x509-server-ecdsa.private.pem");
			writePemFile(pair.getPublic(), "ECDSA PUBLIC KEY",  "x509-server-ecdsa.public.pem");
			
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return pair;
		
		
	}
	
	@SuppressWarnings("deprecation")
	public X509Certificate createCertificate(KeyPair keyPair) throws IOException, CertificateEncodingException {
		X509Certificate cert = null;
		
		Date startDate = new Date();              												// time from which certificate is valid
		Date expiryDate = new Date(startDate.getTime()+(3600*12*30*24*60*1000));             	// time after which certificate is not valid
		BigInteger serialNumber = BigInteger.valueOf(1234567890);
		
		// serial number for certificate
		
		X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();
		X500Principal              dnName = new X500Principal("CN=Test CA Certificate");
		certGen.setSerialNumber(serialNumber);
		certGen.setIssuerDN(dnName);
		certGen.setNotBefore(startDate);
		certGen.setNotAfter(expiryDate);
		certGen.setSubjectDN(dnName);                       // note: same as issuer
		certGen.setPublicKey(keyPair.getPublic());
		certGen.setSignatureAlgorithm("SHA1withECDSA");
		try {
			cert = certGen.generate(keyPair.getPrivate(), "BC");
		} catch (CertificateEncodingException | InvalidKeyException | IllegalStateException | NoSuchProviderException
				| NoSuchAlgorithmException | SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		final File publicKeyFile = new File(System.getProperty("user.home")+"/certs/x509-server-ecdsa.cert.pem");
		
	    final PemWriter publicPemWriter = new PemWriter(new FileWriter(publicKeyFile));
	    publicPemWriter.writeObject(new PemObject("CERTIFICATE", cert.getEncoded()));
	   
	    publicPemWriter.flush();
	    publicPemWriter.close();
		
		return cert;
	}
	
	private static void writePemFile(Key key, String description, String filename)
			throws FileNotFoundException, IOException {
		PemFile pemFile = new PemFile(key, description);
		filename=System.getProperty("user.home")+"/certs/"+filename;
		pemFile.write(filename);
		
		LOGGER.info(String.format("%s successfully writen in file %s.", description, filename));
		System.out.println(String.format("%s successfully writen in file %s.", description, filename));
	}
	
	
	
	
	public static void main(String argv[]) throws CertificateEncodingException, IOException {
		JDKKeyPairGenerator kpg = new JDKKeyPairGenerator();
		KeyPair pair=kpg.createKeyPair();
		kpg.createCertificate(pair);
	}

}
