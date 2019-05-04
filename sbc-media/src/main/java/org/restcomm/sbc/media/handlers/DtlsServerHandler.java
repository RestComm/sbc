package org.restcomm.sbc.media.handlers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.security.SecureRandom;

import org.bouncycastle.crypto.tls.DTLSServerProtocol;
import org.bouncycastle.crypto.tls.DTLSTransport;
import org.bouncycastle.crypto.tls.DatagramTransport;
import org.bouncycastle.crypto.tls.UDPTransport;
import org.restcomm.sbc.media.dtls.DtlsConfiguration;
import org.restcomm.sbc.media.dtls.DtlsSrtpServer;
import org.restcomm.sbc.media.dtls.DtlsSrtpServerProvider;



/**
 * A simple test designed to conduct a DTLS handshake with an external DTLS client.
 * <p>
 * Please refer to GnuTLSSetup.html or OpenSSLSetup.html (under 'docs'), and x509-*.pem files in
 * this package (under 'src/test/resources') for help configuring an external DTLS client.
 * </p>
 */
public class DtlsServerHandler 
{
	DTLSServerProtocol serverProtocol;
	DtlsSrtpServer server;
	DatagramTransport transport;
	DatagramSocket socket;
	
    public DtlsServerHandler()
       
    {

        SecureRandom secureRandom = new SecureRandom();

        serverProtocol = new DTLSServerProtocol(secureRandom);

        //Dtls Server Provider
		   
	    DtlsConfiguration configuration = new DtlsConfiguration();
	    
	    DtlsSrtpServerProvider dtlsServerProvider = null;
	        
	    dtlsServerProvider = 
	        		new DtlsSrtpServerProvider(	configuration.getMinVersion(),
	        									configuration.getMaxVersion(),
	        									configuration.getCipherSuites(),
	        									configuration.getCertificatePath(), //System.getProperty("user.home")+"/certs/x509-server-ecdsa.cert.pem",
	        									configuration.getKeyPath(), 		//System.getProperty("user.home")+"/certs/x509-server-ecdsa.private.pem",
	        									configuration.getAlgorithmCertificate());   

        server = dtlsServerProvider.provide();
    }
    
    public void handshake(DatagramSocket socket) throws IOException {
    	int mtu = 1500;
        byte[] data = new byte[mtu];
        DatagramPacket packet = new DatagramPacket(data, mtu);

        socket.receive(packet);

        System.out.println("Accepting connection from " + packet.getAddress().getHostAddress() + ":" + socket.getPort());
        socket.connect(packet.getAddress(), packet.getPort());

        /*
         * NOTE: For simplicity, and since we don't yet have HelloVerifyRequest support, we just
         * discard the initial packet, which the client should re-send anyway.
         */

        transport = new UDPTransport(socket, mtu);
        
        // Uncomment to see packets
        //transport = new LoggingDatagramTransport(transport, System.out);
       
        DTLSTransport dtlsServer = serverProtocol.accept(server, transport);
        
        // Prepare the shared key to be used in RTP streaming
        server.prepareSrtpSharedSecret();
        
        /*
        
        byte[] buf = new byte[dtlsServer.getReceiveLimit()];

        while (!socket.isClosed())
        {
            try
            {
                int length = dtlsServer.receive(buf, 0, buf.length, 60000);
                if (length >= 0)
                {
                    System.out.write(buf, 0, length);
                    dtlsServer.send(buf, 0, length);
                }
            }
            catch (SocketTimeoutException ste)
            {
            }
        }
        dtlsServer.close();
		*/
        
    }
}
