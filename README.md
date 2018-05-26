RestComm Session Border Controller
============
The Restcomm Session Border Controller (SBC) is a device regularly deployed in Voice over Internet Protocol (VoIP) networks to exert control over the signaling and usually also the media streams involved in setting up, conducting, and tearing down telephone calls or other interactive media communications.

SBCs commonly maintain full session state and offer the following functions:

* Security – protect the network and other devices from:
  * Malicious attacks such as a denial-of-service attack (DoS) or distributed DoS
  * Toll fraud via rogue media streams
  * Topology hiding
  * Malformed packet protection
  * Encryption of signaling (via TLS and IPSec) and media (SRTP)
* WebRTC Gateway and Connectivity – allow different parts of the network to communicate through the use of a variety of techniques such as:
  * NAT traversal
  * SIP normalization via SIP message and header manipulation
  * IPv4 to IPv6 interworking
  * VPN connectivity
  * Protocol translations between SIP, SIP Over WebSockets
* Quality of service – the QoS policy of a network and prioritization of flows is usually implemented by the SBC. It can include such functions as:
  * Traffic policing
  * Registration throttling
  * Resource allocation
  * Rate limiting
  * Call admission control
  * ToS/DSCP bit setting
* Regulatory – many times the SBC is expected to provide support for regulatory requirements such as:
  * emergency calls prioritization and
  * lawful interception
* Media services – many of the new generation of SBCs also provide built-in digital signal processors (DSPs) to enable them to offer border-based media control and services such as:
  * Media encoding/decoding (SRTP/RTP)
  * WEBRTC termination and pass-thru
  * DTMF relay and interworking
  * Media transcoding
  * Tones and announcements
  * Data and fax interworking
  * Support for voice and video calls
* Statistics and billing information – since all sessions that pass through the edge of the network pass through the SBC, it is a natural point to gather statistics and usage-based information on these sessions.

Architecture
========

Java based and Follows RFC 5853 and built upon a B2BUA architecture (NAT & internal topology hiding)

SBC is built on [RestComm SIP Servlets](https://github.com/RestComm/sip-servlets).





[Try Restcomm Cloud NOW for FREE!](https://www.restcomm.com/sign-up/) Zero download and install required.


All Restcomm [docs](https://www.restcomm.com/docs/) and [downloads](https://www.restcomm.com/downloads/) are now available at [Restcomm.com](https://www.restcomm.com).


