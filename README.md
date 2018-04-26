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

Downloads
========
Simply download the source code version of your choice and build from source.

Want to Contribute ? 
========
[See our Contributors Guide](https://github.com/RestComm/Restcomm-Conect/wiki/Contribute-to-RestComm) and our [Open Source Playbook](https://docs.google.com/document/d/1RZz2nd2ivCK_rg1vKX9ansgNF6NpK_PZl81GxZ2MSnM/edit?usp=sharing)

Issue Tracking and Roadmap
========
[Issue Tracker](https://github.com/RestComm/sbc/issues)

Questions ?
========
Please ask your question on [StackOverflow](http://stackoverflow.com/questions/tagged/restcomm) or the Google [public forum](http://groups.google.com/group/restcomm)

License
========

RestComm SBC is lead by [TeleStax, Inc.](http://www.telestax.com/), [Eolos](http://www.eolos.la/) and developed collaboratively by a community of individual and enterprise contributors.

RestComm SBC is licensed under dual license policy. The default license is the Free Open Source GNU Affero GPL v3.0. Alternatively a commercial license can be obtained from Telestax ([contact form](http://www.telestax.com/contactus/#InquiryForm))

Acknowledgements
========
[See who has been contributing to RestComm](http://www.telestax.com/opensource/acknowledgments/)
