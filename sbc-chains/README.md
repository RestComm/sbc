



[Try Restcomm Cloud NOW for FREE!](https://www.restcomm.com/sign-up/) Zero download and install required.


All Restcomm [docs](https://www.restcomm.com/docs/) and [downloads](https://www.restcomm.com/downloads/) are now available at [Restcomm.com](https://www.restcomm.com).


The Chain Sub-system Framework
==============================

What is it & what’s for?

An SBC is a machine designed to be in the middle of a transmission flow pipe in order to intercept, analyze and eventually mangle,
transform or drop some traffic packets targeted to a certain destination, usually a VoIP insecure SIP Proxy/Server.

This process accepts packet flow from an input source and delivers packets to an output destination based on some certain filtering
rules implemented at the business rule layer.

In order to perform such piping mechanism, we have to take into account some operational rules that have to be followed to be up to
the needs of traffic management issue and its further analysis, until the input data could reach the output side of what we will call
the chain subsystem.

The chain subsystem is an entity responsible of chaining processor modules, control their life-cycle and execution priority and
delivering of the processor output to the next electable module in the chain until all modules are traversed or one of the modules
reject the packet.
