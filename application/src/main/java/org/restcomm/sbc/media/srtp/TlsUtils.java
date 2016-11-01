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
 
import java.io.IOException; 
import java.io.InputStream; 
import java.io.InputStreamReader; 
 
import org.bouncycastle.asn1.pkcs.RSAPrivateKey; 
import org.bouncycastle.crypto.Digest; 
import org.bouncycastle.crypto.digests.SHA1Digest; 
import org.bouncycastle.crypto.digests.SHA256Digest; 
import org.bouncycastle.crypto.digests.SHA512Digest; 
import org.bouncycastle.crypto.params.AsymmetricKeyParameter; 
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters; 
import org.bouncycastle.crypto.tls.Certificate; 
import org.bouncycastle.crypto.tls.DefaultTlsAgreementCredentials; 
import org.bouncycastle.crypto.tls.DefaultTlsEncryptionCredentials; 
import org.bouncycastle.crypto.tls.DefaultTlsSignerCredentials; 
import org.bouncycastle.crypto.tls.SignatureAndHashAlgorithm; 
import org.bouncycastle.crypto.tls.TlsAgreementCredentials; 
import org.bouncycastle.crypto.tls.TlsContext; 
import org.bouncycastle.crypto.tls.TlsEncryptionCredentials; 
import org.bouncycastle.crypto.tls.TlsServerContext; 
import org.bouncycastle.crypto.tls.TlsSignerCredentials; 
import org.bouncycastle.crypto.util.PrivateKeyFactory; 
import org.bouncycastle.util.encoders.Base64; 
import org.bouncycastle.util.encoders.Hex; 
import org.bouncycastle.util.io.pem.PemObject; 
import org.bouncycastle.util.io.pem.PemReader; 
 
/**
 * Collection of utility functions for DTLS operations. 
 *  
 * @author Henrique Rosa (henrique.rosa@telestax.com) 
 * 
 */ 
public class TlsUtils { 
 
 private static final String SHA_1 = "sha-1"; 
 private static final String SHA_256 = "sha-256"; 
 private static final String SHA_512 = "sha-512"; 
 
 static final byte[] rsaCertData = Base64 
   .decode("MIICUzCCAf2gAwIBAgIBATANBgkqhkiG9w0BAQQFADCBjzELMAkGA1UEBhMCQVUxKDAmBgNVBAoMH1RoZSBMZWdpb2" 
     + "4gb2YgdGhlIEJvdW5jeSBDYXN0bGUxEjAQBgNVBAcMCU1lbGJvdXJuZTERMA8GA1UECAwIVmljdG9yaWExLzAtBgkq" 
     + "hkiG9w0BCQEWIGZlZWRiYWNrLWNyeXB0b0Bib3VuY3ljYXN0bGUub3JnMB4XDTEzMDIyNTA2MDIwNVoXDTEzMDIyNT" 
     + "A2MDM0NVowgY8xCzAJBgNVBAYTAkFVMSgwJgYDVQQKDB9UaGUgTGVnaW9uIG9mIHRoZSBCb3VuY3kgQ2FzdGxlMRIw" 
     + "EAYDVQQHDAlNZWxib3VybmUxETAPBgNVBAgMCFZpY3RvcmlhMS8wLQYJKoZIhvcNAQkBFiBmZWVkYmFjay1jcnlwdG" 
     + "9AYm91bmN5Y2FzdGxlLm9yZzBaMA0GCSqGSIb3DQEBAQUAA0kAMEYCQQC0p+RhcFdPFqlwgrIr5YtqKmKXmEGb4Shy" 
     + "pL26Ymz66ZAPdqv7EhOdzl3lZWT6srZUMWWgQMYGiHQg4z2R7X7XAgERo0QwQjAOBgNVHQ8BAf8EBAMCBSAwEgYDVR" 
     + "0lAQH/BAgwBgYEVR0lADAcBgNVHREBAf8EEjAQgQ50ZXN0QHRlc3QudGVzdDANBgkqhkiG9w0BAQQFAANBAHU55Ncz" 
     + "eglREcTg54YLUlGWu2WOYWhit/iM1eeq8Kivro7q98eW52jTuMI3CI5ulqd0hYzshQKQaZ5GDzErMyM="); 
 
 static final byte[] dudRsaCertData = Base64 
   .decode("MIICUzCCAf2gAwIBAgIBATANBgkqhkiG9w0BAQQFADCBjzELMAkGA1UEBhMCQVUxKDAmBgNVBAoMH1RoZSBMZWdpb2" 
     + "4gb2YgdGhlIEJvdW5jeSBDYXN0bGUxEjAQBgNVBAcMCU1lbGJvdXJuZTERMA8GA1UECAwIVmljdG9yaWExLzAtBgkq" 
     + "hkiG9w0BCQEWIGZlZWRiYWNrLWNyeXB0b0Bib3VuY3ljYXN0bGUub3JnMB4XDTEzMDIyNTA1NDcyOFoXDTEzMDIyNT" 
     + "A1NDkwOFowgY8xCzAJBgNVBAYTAkFVMSgwJgYDVQQKDB9UaGUgTGVnaW9uIG9mIHRoZSBCb3VuY3kgQ2FzdGxlMRIw" 
     + "EAYDVQQHDAlNZWxib3VybmUxETAPBgNVBAgMCFZpY3RvcmlhMS8wLQYJKoZIhvcNAQkBFiBmZWVkYmFjay1jcnlwdG" 
     + "9AYm91bmN5Y2FzdGxlLm9yZzBaMA0GCSqGSIb3DQEBAQUAA0kAMEYCQQC0p+RhcFdPFqlwgrIr5YtqKmKXmEGb4Shy" 
     + "pL26Ymz66ZAPdqv7EhOdzl3lZWT6srZUMWWgQMYGiHQg4z2R7X7XAgERo0QwQjAOBgNVHQ8BAf8EBAMCAAEwEgYDVR" 
     + "0lAQH/BAgwBgYEVR0lADAcBgNVHREBAf8EEjAQgQ50ZXN0QHRlc3QudGVzdDANBgkqhkiG9w0BAQQFAANBAJg55PBS" 
     + "weg6obRUKF4FF6fCrWFi6oCYSQ99LWcAeupc5BofW5MstFMhCOaEucuGVqunwT5G7/DweazzCIrSzB0="); 
 
 static String fingerprint(String hashFunction, 
   org.bouncycastle.asn1.x509.Certificate c) throws IOException { 
  byte[] der = c.getEncoded(); 
  byte[] sha1 = digestOf(hashFunction, der); 
  byte[] hexBytes = Hex.encode(sha1); 
  String hex = new String(hexBytes, "ASCII").toUpperCase(); 
 
  StringBuffer fp = new StringBuffer(); 
  int i = 0; 
  fp.append(hex.substring(i, i + 2)); 
  while ((i += 2) < hex.length()) { 
   fp.append(':'); 
   fp.append(hex.substring(i, i + 2)); 
  } 
   
  switch (hashFunction) { 
   case SHA_1: 
    return SHA_1 + " " + fp.toString(); 
   case SHA_512: 
    return SHA_512 + " " + fp.toString(); 
   default: 
    return SHA_256 + " " + fp.toString(); 
  } 
 } 
 
 static byte[] digestOf(String hashFunction, byte[] input) { 
  Digest d; 
  switch (hashFunction) { 
  case SHA_1: 
   d = new SHA1Digest(); 
   break; 
 
  case SHA_512: 
   d = new SHA512Digest(); 
   break; 
 
  case SHA_256: 
  default: 
   d = new SHA256Digest(); 
   break; 
  } 
 
  d.update(input, 0, input.length); 
  byte[] result = new byte[d.getDigestSize()]; 
  d.doFinal(result, 0); 
  return result; 
 } 
 
 static TlsAgreementCredentials loadAgreementCredentials(TlsContext context, 
   String[] certResources, String keyResource) throws IOException { 
  Certificate certificate = loadCertificateChain(certResources); 
  AsymmetricKeyParameter privateKey = loadPrivateKeyResource(keyResource); 
  return new DefaultTlsAgreementCredentials(certificate, privateKey); 
 } 
 
 static TlsEncryptionCredentials loadEncryptionCredentials( 
   TlsContext context, String[] certResources, String keyResource) 
   throws IOException { 
  Certificate certificate = loadCertificateChain(certResources); 
  AsymmetricKeyParameter privateKey = loadPrivateKeyResource(keyResource); 
  return new DefaultTlsEncryptionCredentials(context, certificate, 
    privateKey); 
 } 
 
 static TlsSignerCredentials loadSignerCredentials(TlsContext context, 
   String[] certResources, String keyResource) throws IOException { 
  Certificate certificate = loadCertificateChain(certResources); 
  AsymmetricKeyParameter privateKey = loadPrivateKeyResource(keyResource); 
  return new DefaultTlsSignerCredentials(context, certificate, privateKey); 
 } 
 
 static TlsSignerCredentials loadSignerCredentials(TlsServerContext context, 
   String[] certResources, String keyResource, 
   SignatureAndHashAlgorithm signatureAndHashAlgorithm) 
   throws IOException { 
  Certificate certificate = loadCertificateChain(certResources); 
  AsymmetricKeyParameter privateKey = loadPrivateKeyResource(keyResource); 
 // return new DefaultTlsSignerCredentials(context, certificate, 
 //   privateKey, signatureAndHashAlgorithm); 
  return new DefaultTlsSignerCredentials(context, certificate, 
		    privateKey); 
 } 
 
 static Certificate loadCertificateChain(String[] resources) 
   throws IOException { 
  org.bouncycastle.asn1.x509.Certificate[] chain = new org.bouncycastle.asn1.x509.Certificate[resources.length]; 
  for (int i = 0; i < resources.length; ++i) { 
   chain[i] = loadCertificateResource(resources[i]); 
  } 
  return new Certificate(chain); 
 } 
 
 static org.bouncycastle.asn1.x509.Certificate loadCertificateResource( 
   String resource) throws IOException { 
  PemObject pem = loadPemResource(resource); 
  if (pem.getType().endsWith("CERTIFICATE")) { 
   return org.bouncycastle.asn1.x509.Certificate.getInstance(pem 
     .getContent()); 
  } 
  throw new IllegalArgumentException( 
    "'resource' doesn't specify a valid certificate"); 
 } 
 
 static AsymmetricKeyParameter loadPrivateKeyResource(String resource) 
   throws IOException { 
  PemObject pem = loadPemResource(resource); 
  if (pem.getType().endsWith("RSA PRIVATE KEY")) { 
   RSAPrivateKey rsa = RSAPrivateKey.getInstance(pem.getContent()); 
   return new RSAPrivateCrtKeyParameters(rsa.getModulus(), 
     rsa.getPublicExponent(), rsa.getPrivateExponent(), 
     rsa.getPrime1(), rsa.getPrime2(), rsa.getExponent1(), 
     rsa.getExponent2(), rsa.getCoefficient()); 
  } 
  if (pem.getType().endsWith("PRIVATE KEY")) { 
   return PrivateKeyFactory.createKey(pem.getContent()); 
  } 
  throw new IllegalArgumentException( 
    "'resource' doesn't specify a valid private key"); 
 } 
 
 static PemObject loadPemResource(String resource) throws IOException { 
  InputStream s = TlsUtils.class.getResourceAsStream(resource); 
  PemReader p = new PemReader(new InputStreamReader(s)); 
  PemObject o = p.readPemObject(); 
  p.close(); 
  return o; 
 } 

 
}

