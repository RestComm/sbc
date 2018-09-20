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

package org.restcomm.sbc.testsuite.sip;

import static org.cafesip.sipunit.SipAssert.assertLastOperationSuccess;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

import javax.sip.*;
import javax.sip.address.SipURI;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.cafesip.sipunit.Credential;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.jboss.arquillian.container.mss.extension.SipStackTool;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.restcomm.sbc.managers.Monitor;
import org.restcomm.sbc.testsuite.rest.CreateClientsTool;
import org.restcomm.sbc.testsuite.rest.MonitoringServiceTool;
import org.restcomm.sbc.testsuite.rest.RestcommCallsTool;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


/**
 * Client registration Test.
 * Maria client is using two sip clients to register
 *
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 */
@RunWith(Arquillian.class)
public class RegisterClientTest {

    private static final String version = org.restcomm.sbc.Version.getVersion();

    private static final byte[] bytes = new byte[]{118, 61, 48, 13, 10, 111, 61, 117, 115, 101, 114, 49, 32, 53, 51, 54, 53,
            53, 55, 54, 53, 32, 50, 51, 53, 51, 54, 56, 55, 54, 51, 55, 32, 73, 78, 32, 73, 80, 52, 32, 49, 50, 55, 46, 48, 46,
            48, 46, 49, 13, 10, 115, 61, 45, 13, 10, 99, 61, 73, 78, 32, 73, 80, 52, 32, 49, 50, 55, 46, 48, 46, 48, 46, 49,
            13, 10, 116, 61, 48, 32, 48, 13, 10, 109, 61, 97, 117, 100, 105, 111, 32, 54, 48, 48, 48, 32, 82, 84, 80, 47, 65,
            86, 80, 32, 48, 13, 10, 97, 61, 114, 116, 112, 109, 97, 112, 58, 48, 32, 80, 67, 77, 85, 47, 56, 48, 48, 48, 13, 10};
    private static final String body = new String(bytes);

    @ArquillianResource
    private Deployer deployer;
    @ArquillianResource
    URL deploymentUrl;

    private static SipStackTool tool1;
    private static SipStackTool tool2;
    private static SipStackTool tool3;

    // Maria is a Restcomm Client **without** VoiceURL. This Restcomm Client can dial anything.
    private SipStack aliceSipStack;
    private SipPhone alicePhone;
    private String aliceContact = "sip:alice@10.0.0.10:5060";
   

    // Alice is a Restcomm Client with VoiceURL. This Restcomm Client can register with Restcomm and whatever will dial the RCML
    // of the VoiceURL will be executed.
    private SipStack bobSipStack;
    private SipPhone bobPhone;
    private String bobContact = "sip:bob@10.0.0.10:5060";

    private String adminAccountSid = "ACae6e420f425248d6a26948c17a9e2acf";
    private String adminAuthToken = "77f8c12cc7b8f8423e5c38b035249166";

    @BeforeClass
    public static void beforeClass() throws Exception {
    	
    	
        tool1 = new SipStackTool("RegisterClientTest1");
        tool2 = new SipStackTool("RegisterClientTest2");
        tool3 = new SipStackTool("RegisterClientTest3");
    }

    @Before
    public void before() throws Exception {
    	
        bobSipStack = tool2.initializeSipStack(SipStack.PROTOCOL_UDP, "10.0.0.10" , "5061", "10.0.0.10:5060");
       
        bobPhone = bobSipStack.createSipPhone("10.0.0.10", SipStack.PROTOCOL_UDP, 5060, bobContact);
        

        aliceSipStack = tool1.initializeSipStack(SipStack.PROTOCOL_UDP, "10.0.0.10", "5062", "10.0.0.10:5060");
        alicePhone = aliceSipStack.createSipPhone("10.0.0.10", SipStack.PROTOCOL_UDP, 5060, aliceContact);

    }

    @After
    public void after() throws Exception {
        if (alicePhone != null) {
            alicePhone.dispose();
        }
        if (aliceSipStack != null) {
            aliceSipStack.dispose();
        }

        if (bobSipStack != null) {
            bobSipStack.dispose();
        }
        if (bobPhone != null) {
            bobPhone.dispose();
        }

        
        Thread.sleep(2000);
    }

    @Test
    public void testRegisterClients() throws ParseException, InterruptedException {


        SipURI boburi = bobSipStack.getAddressFactory().createSipURI("bob", "10.0.0.10:5060");
        SipURI aliceuri = bobSipStack.getAddressFactory().createSipURI("alice", "10.0.0.10:5060");
        System.out.println("Registering bob ...");
        assertTrue(bobPhone.register(boburi, "bob", "mobi2016", bobContact, 3600, 3600));
        assertTrue(alicePhone.register(aliceuri, "alice", "mobi2016", aliceContact, 3600, 3600));

        Thread.sleep(5000);

        assertTrue(bobPhone.unregister(bobContact, 0));
        assertTrue(alicePhone.unregister(aliceContact, 0));
    }

    @Test
    public void testRegisterClientAndRemoveItAfterNoResponseToOptions() throws ParseException, InterruptedException, SipException, InvalidArgumentException, IOException {
        
        SipURI uri = bobSipStack.getAddressFactory().createSipURI("bob", "10.0.0.10:5060");
        assertTrue(bobPhone.register(uri, "bob", "mobi2016", bobContact, 3600, 3600));

        Credential c = new Credential("10.0.0.10", "bob", "mobi2016");
        bobPhone.addUpdateCredential(c);

        assertTrue(MonitoringServiceTool.getInstance().getRegisteredUsers(deploymentUrl.toString(),adminAccountSid,adminAuthToken)==1);

        Thread.sleep(1000);
        bobPhone.listenRequestMessage();
        RequestEvent reqEvent = bobPhone.waitRequest(65000);
        assertTrue(reqEvent != null);
        assertTrue(reqEvent.getRequest().getMethod().equals(Request.OPTIONS));

        Thread.sleep(40000);

        assertTrue(MonitoringServiceTool.getInstance().getRegisteredUsers(deploymentUrl.toString(),adminAccountSid,adminAuthToken)==0);

    }


    @Test
    public void testBobCallAlice() throws ParseException, InterruptedException {

        

        SipURI uri = aliceSipStack.getAddressFactory().createSipURI(null, "10.0.0.10:5060");

        assertTrue(bobPhone.register(uri, "bob", "mobi2016", bobContact, 3600, 3600));
        Thread.sleep(3000);
        assertTrue(alicePhone.register(uri, "alice", "mobi2016", aliceContact, 3600, 3600));
        Thread.sleep(3000);
     

        Credential c = new Credential("10.0.0.10", "bob", "mobi2016");
        bobPhone.addUpdateCredential(c);

        Credential c2 = new Credential("10.0.0.10", "alice", "mobi2016");
        alicePhone.addUpdateCredential(c2);

       
        Thread.sleep(1000);

        final SipCall aliceCall_1 = alicePhone.createSipCall();
        aliceCall_1.listenForIncomingCall();


        // Alice initiates a call to Maria
        final SipCall bobCall = bobPhone.createSipCall();
       // bobCall.initiateOutgoingCall(bobContact, aliceRestcommContact, null, body, "application", "sdp", null, null);
        assertLastOperationSuccess(bobCall);
        assertTrue(bobCall.waitForAuthorisation(3000));

        //According to issue 106: https://telestax.atlassian.net/browse/RESTCOMM-106
        //Restcomm will only use the last REGISTER address
        //Last REGISTRATION was from Maria-2 
        assertTrue(aliceCall_1.waitForIncomingCall(3000));
        assertTrue(aliceCall_1.sendIncomingCallResponse(100, "Trying-Alice-2", 1800));
        assertTrue(aliceCall_1.sendIncomingCallResponse(180, "Ringing-Alice-2", 1800));
        String receivedBody = new String(aliceCall_1.getLastReceivedRequest().getRawContent());
        assertTrue(aliceCall_1.sendIncomingCallResponse(Response.OK, "OK-Alice-2", 3600, receivedBody, "application", "sdp", null,
                null));

        assertTrue(!aliceCall_1.waitForIncomingCall(3000));


        assertTrue(bobCall.waitOutgoingCallResponse(5 * 1000));
        int bobResponse = bobCall.getLastReceivedResponse().getStatusCode();
        assertTrue(bobResponse == Response.TRYING || bobResponse == Response.RINGING);

        Dialog bobDialog = null;

        if (bobResponse == Response.TRYING) {
            assertTrue(bobCall.waitOutgoingCallResponse(5 * 1000));
            assertEquals(Response.RINGING, bobCall.getLastReceivedResponse().getStatusCode());
            bobDialog = bobCall.getDialog();
        }

        assertTrue(bobCall.waitOutgoingCallResponse(5 * 1000));
        assertEquals(Response.OK, bobCall.getLastReceivedResponse().getStatusCode());
        assertTrue(bobCall.sendInviteOkAck());
        assertTrue(bobCall.getDialog().equals(bobDialog));
        assertTrue(bobCall.getDialog().equals(bobDialog));

        assertTrue(!(bobCall.getLastReceivedResponse().getStatusCode() >= 400));

//        Thread.sleep(3000);
//        assertTrue(bobCall.disconnect());
//
//        assertTrue(aliceCall_2.waitForDisconnect(5 * 1000));
//        assertTrue(aliceCall_2.respondToDisconnect());

        //Check CDR
        JsonObject cdrs = RestcommCallsTool.getInstance().getCalls(deploymentUrl.toString(), adminAccountSid, adminAuthToken);
        assertNotNull(cdrs);
        JsonArray cdrsArray = cdrs.get("calls").getAsJsonArray();
        assertTrue(cdrsArray.size() == 1);

    }

    @Deployment(name = "ClientsEndpointTest", managed = true, testable = false)
    public static WebArchive createWebArchiveNoGw() {
    	System.setProperty("com.sun.management.jmxremote", "true");
    	//-Dcom.sun.management.jmxremote.port=<port> -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false 
       // WebArchive restcommArchive = ShrinkWrap.create(ZipImporter.class, "restcomm-sbc.war").importFrom(new File("/target/restcomm-sbc.war"))

       WebArchive restcommArchive = ShrinkWrap.create(ZipImporter.class, "restcomm-sbc.war").importFrom(new File("C:/Users/OCA/workspace-neon/restcomm-sbc/application/target/restcomm-sbc.war"))
                .as(WebArchive.class);
        
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "restcomm-sbc.war");
        
        archive = archive.merge(restcommArchive);
        archive.delete("/WEB-INF/sip.xml");
        archive.delete("/WEB-INF/web.xml");
        archive.delete("/WEB-INF/conf/sbc.xml");
        archive.delete("/WEB-INF/data/hsql/sbc.script");
        archive.addAsWebInfResource("sip.xml");
        archive.addAsWebInfResource("web.xml");

        archive.addAsWebInfResource("sbc.xml", "conf/sbc.xml");
        archive.addAsWebInfResource("sbc.script", "data/hsql/sbc.script");
       
        return archive;
    }
}
