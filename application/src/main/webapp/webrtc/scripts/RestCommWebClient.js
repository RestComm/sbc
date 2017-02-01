/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2015, Telestax Inc and individual contributors
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
 */

var wrtcClient;
var wrtcEventListener = undefined;
var wrtcConfiguration = undefined;
var localStream;
var remoteMedia;
var username;
var inCall = false;

// Trying to hide the .js dependencies from index.html. Here are some alternatives, but none of them worked 100% correctly for me. Let's keep them around in case we revisit in the future:
/* This is supposed to be jquery's way to include .js from here, but I had to change the order or <script> tags in index.html, which ruins the whole site for some reason
$.getScript("scripts/adapter.js", function() {
	console.log("Loaded adapter.js");
});
$.getScript("scripts/jain-sip.js", function() {
	console.log("Loaded jain-sip.js");
});
$.getScript("scripts/WebRTComm.js", function() {
	console.log("Loaded WebRTComm.js");
});
*/

/* Another way is adding requirejs as a dependency and use this (which again seems to be failing some times (arounds 1/10)
requirejs(["adapter"], function(adapter) {
	console.log("-- Loaded adapter.js");
});
requirejs(["jain-sip"], function(jainSip) {
	console.log("-- Loaded jain-sip.js");
});
requirejs(["WebRTComm"], function(webRTComm) {
	console.log("-- Loaded WebRTComm.js");
});
*/

/* Yet another way that again doesn't work 100% of the times, but we avoid a third party library dependency (have only tested with Chrom though)
// Helper to include .js files from a .js file instead of html
function include(file)
{
  var script  = document.createElement('script');
  script.src  = file;
  script.type = 'text/javascript';
  script.defer = true;

  document.getElementsByTagName('head').item(0).appendChild(script);
}

// Include prerequisites
include('scripts/adapter.js');
include('scripts/jain-sip.js');
include('scripts/WebRTComm.js');
*/

// WrtcEventListener callbacks
function WrtcEventListener(device)
{
	if (device.debugEnabled) {
		console.log("WrtcEventListener::WrtcEventListener constructor");
	}

	this.device = device;
}

// ---- General events (WebRTCommClient events)

// WebRTCommClient is ready
WrtcEventListener.prototype.onWebRTCommClientOpenedEvent = function() 
{
	if (this.device.debugEnabled) {
	   console.log("WrtcEventListener::onWebRTCommClientOpenedEvent");
	}

	this.device.status = 'ready';
	this.device.onReady(this.device);
};

// WebRTCommClient encountered an error when opening
WrtcEventListener.prototype.onWebRTCommClientOpenErrorEvent = function(error) 
{
	if (this.device.debugEnabled) {
   	console.log("WrtcEventListener::onWebRTCommClientOpenErrorEvent" + error);
	}

	this.device.onError("Error setting up Device" + error);
};

// WebRTCommClient shut down event (when WebRTCommClient.close() is complete)
WrtcEventListener.prototype.onWebRTCommClientClosedEvent = function() 
{
	if (this.device.debugEnabled) {
		console.log("WrtcEventListener::onWebRTCommClientClosedEvent");
	}

	// notify the listener that the device is shutting down
	this.device.onOffline(this.device);
};

WrtcEventListener.prototype.onGetUserMediaErrorEventHandler = function(error) 
{
	if (this.device.debugEnabled) {
		console.debug('WrtcEventListener::onGetUserMediaErrorEventHandler(): error=' + error);
	}

	this.device.onError("Media error: " + error);
};

// ---- Call related listeners (WebRTCommCall listener)

// Ringing for incoming calls 
WrtcEventListener.prototype.onWebRTCommCallRingingEvent = function(webRTCommCall) 
{
	if (this.device.debugEnabled) {
		console.log("WrtcEventListener::onWebRTCommCallRingingEvent");
	}

	if (webRTCommCall.incomingCallFlag == true) {
		// update connection status and notify Connection and Device listener (notice that both Device and Connection define listeners for disconnect event)
		this.device.connection = new Connection(this.device, 'pending');
		this.device.connection.isIncoming = true;
		this.device.connection.parameters = {
			'From': webRTCommCall.callerPhoneNumber, 
			'To': wrtcConfiguration.sip.sipUserName, 
			'Custom-Headers': webRTCommCall.customHeaders,
		};

		this.device.connection.webrtcommCall = webRTCommCall;
		//this.device.connection.onDisconnect = this.device.onDisconnect;
		var that = this;
/*
		setTimeout(function() {
				that.device.onIncoming(that.device.connection);
				}, 1);
*/
		this.device.onIncoming(this.device.connection);
		if (this.device.sounds.incomingEnabled) {
			this.device.sounds.audioRinging.play();
		}

		this.device.status = 'busy';
	}
};

WrtcEventListener.prototype.onWebRTCommCallInProgressEvent = function(webRTCommCall) 
{
	if (this.device.debugEnabled) {
		console.log("WrtcEventListener::onWebRTCommCallInProgressEvent");
	}
};

// Outgoing call received RINGING event
WrtcEventListener.prototype.onWebRTCommCallRingingBackEvent = function(webRTCommCall) 
{
	if (this.device.debugEnabled) {
		console.log("WrtcEventListener::onWebRTCommCallRingingBackEvent");
	}

	if (this.device.sounds.outgoingEnabled) {
		this.device.sounds.audioCalling.play();
	}

	this.device.connection.webrtcommCall = webRTCommCall;
	//currentCall = webRTCommCall;
};

WrtcEventListener.prototype.onWebRTCommCallOpenErrorEvent = function(webRTCommCall, error) 
{
	if (this.device.debugEnabled) {
		console.log("WrtcEventListener::onWebRTCommCallOpenErrorEvent");
	}

	if (this.device.connection) {
		this.device.connection.onError("Error connecting " + error);
	}
};

WrtcEventListener.prototype.onWebRTCommCallErrorEvent = function(webRTCommCall, error) 
{
	if (this.device.debugEnabled) {
		console.log("WrtcEventListener::onWebRTCommCallErrorEvent");
	}

	if (this.device.connection) {
		this.device.connection.onError("Error in call: " + error);
	}
};

WrtcEventListener.prototype.onWebRTCommCallClosedEvent = function(webRTCommCall) 
{
	if (this.device.debugEnabled) {
		console.log("WrtcEventListener::onWebRTCommCallClosedEvent");
	}

	// update device & connection status and notify Connection and Device listener (notice that both Device and Connection define listeners for disconnect event)
	this.device.status = 'ready';
	this.device.connection.status = 'closed';
	// at the time that the call is closed webrtcomm also includes stats about the disconnected call, let's forward them to the user application
	this.device.connection.stats = webRTCommCall.stats;
	this.device.connection.onDisconnect(this.device.connection);
	if (this.device.onDisconnect != null) {
		this.device.onDisconnect(this.device.connection);
	}
};

WrtcEventListener.prototype.onWebRTCommCallOpenedEvent = function(webRTCommCall) 
{
	if (this.device.debugEnabled) {
		console.log("WrtcEventListener::onWebRTCommCallOpenedEvent: received remote stream");
	}

	//currentCall = webRTCommCall;
	this.device.connection.webrtcommCall = webRTCommCall;

	// add remote media to the remoteMedia html element
	remoteMedia.src = URL.createObjectURL(webRTCommCall.getRemoteBundledAudioVideoMediaStream() ||
				webRTCommCall.getRemoteVideoMediaStream() ||
				webRTCommCall.getRemoteAudioMediaStream());

	if (this.device.connection.isIncoming) {
		this.device.sounds.audioRinging.pause();
	}
	else {
		this.device.sounds.audioCalling.pause();
	}

	// update connection status and notify connection listener
	this.device.connection.status = 'open';
	this.device.onConnect(this.device.connection);

	inCall = true; 
};

WrtcEventListener.prototype.onWebRTCommCallCanceledEvent = function(webRTCommCall) 
{
	if (this.device.debugEnabled) {
		console.log("WrtcEventListener::onWebRTCommCallCanceledEvent");
	}

	this.device.sounds.audioRinging.pause();
	this.device.connection.webrtcommCall = undefined;
	this.device.onCancel(this.device.connection);
};

WrtcEventListener.prototype.onWebRTCommCallHangupEvent = function(webRTCommCall) 
{
	if (this.device.debugEnabled) {
		console.log("WrtcEventListener::onWebRTCommCallHangupEvent");
	}

	this.device.connection.webrtcommCall = undefined;
	//currentCall = undefined;
};

WrtcEventListener.prototype.onWebRTCommCallStatsEvent = function(webRTCommCall, stats) 
{
	if (this.device.debugEnabled) {
		console.log("WrtcEventListener::onWebRTCommCallStatsEvent");
	}

	if (this.device.connection.onStats != null) {
		this.device.connection.onStats(stats);
	}
};

// ---- Message related events

// Message arrived
WrtcEventListener.prototype.onWebRTCommMessageReceivedEvent = function(message) {
	if (this.device.debugEnabled) {
		console.log("WrtcEventListener::onWebRTCommMessageReceivedEvent");
	}

	if (this.device.sounds.outgoingEnabled) {
		this.device.sounds.audioMessage.play();
	}

	var parameters = {
		'From': message.from,
		'Text': message.text,
	};

	this.device.onMessage(parameters);
};

WrtcEventListener.prototype.onWebRTCommMessageSentEvent = function(message) {
	if (this.device.debugEnabled) {
		console.log("WrtcEventListener::onWebRTCommMessageSentEvent");
	}
};

WrtcEventListener.prototype.onWebRTCommMessageSendErrorEvent = function(message, error) {
	if (this.device.debugEnabled) {
		console.log("WrtcEventListener::onWebRTCommMessageSendErrorEvent");
	}

	this.device.onError("Error sending text message: " + error);
};


/**
 * @class Connection 
 * @classdesc <p>Connection represents a call. A Connection can be either incoming or outgoing. Connections are not created by themselves but as a result on an action on Device. For example to initiate an outgoing connection you call [Device.connect(parameters)]{@link Device#connect} which instantiates and returns a new Connection. On the other hand when an incoming connection arrives and you have previously registered a callback for receiving incoming connection events by calling [RestCommClient.Device.incoming(callback)]{@link Device#incoming}, you will be notified through that callback and be passed the new Connection object that you can use to control the connection.</p>
 * <p>When an incoming connection arrives it is considered 'pending' until it is either accepted with [Connection.accept()]{@link Connection#accept} or rejected with [Connection.reject()]{@link Connection#reject}. Once the connection is accepted the Connection transitions to 'open' state.</p>
 * 
 * <p>When an outgoing connection is created with [Device.connect(parameters)]{@link Connection#connect} it starts with state 'pending'. Once it starts ringing on the remote party it transitions to 'connecting'. When the remote party answers it, the Connection state transitions to 'open'.</p>
 * 
 * <p>Once an Connection (either incoming or outgoing) is established (i.e. 'open') media can start flowing over it. DTMF digits can be sent over to the remote party using [Connection.sendDigits(digits)]{@link Connection#sendDigits}. When done with the Connection you can disconnect it with [Connection.disconnect()]{@link Connection#disconnect}.</p>
 * @constructor
 * @public
 * @param  {status} Initial status for the Connection
 */
function Connection(device, status)
{
	// Device object where this Connection belongs to
	this.device = device;

	/**
	 * Status of the Connection. Possible values are: <b>pending</b>, <b>connecting</b>, <b>open</b>, <b>closed</b>
	 * @name Connection#status
	 * @type String
	 */
	this.status = status;
	/**
	 * Whether Connection's audio is muted or not
	 * @name Connection#muted
	 * @type Boolean
	 */
	this.muted = false;
	/**
	 * Whether Connection's video is muted or not
	 * @name Connection#videoMuted
	 * @type Boolean
	 */
	this.videoMuted = false;
	/**
	 * Callback for when there is a Connection error (internal)
	 * @name Connection#onError
	 * @type Function
	 * @ignore
	 */
	this.onError  = null;
	/**
	 * Callback for when Connection is disconnected (internal)
	 * @name Connection#onDisconnect
	 * @type Function
	 * @ignore
	 */
	this.onDisconnect = null;
	/**
	 * Callback for when Connection stats are available for the call. Whenever a call ends if this callback is set we are sent the media stats i.e. retrieved from PeerConnection.getStats() and normalized (internal)
	 * @name Connection#onStats
	 * @type Function
	 * @ignore
	 */
	this.onStats = null;
	/**
	 * Callback for when Connection's audio is muted/unmuted (internal)
	 * @name Connection#onMute
	 * @type Function
	 * @ignore
	 */
	this.onMute = null;
	/**
	 * Callback for when Connection's video is muted/unmuted (internal)
	 * @name Connection#onMuteVideo
	 * @type Function
	 * @ignore
	 */
	this.onMuteVideo = null;

	// not found in Twilio docs, but adding to be inline with our mobile SDKs
	/**
	 * Is the Connection incoming or outgoing
	 * @name Connection#isIncoming
	 * @type Function
	 */
	this.isIncoming = false;
	/**
	 * Parameters of this Connection
	 * @name Connection#parameters
	 * @type Function
	 */
	this.parameters = null;
	/**
	 * The underlying webrtc call structure, not to be exposed to the App
	 * @name Connection#webrtcommCall
	 * @private
	 * @type Object
	 */
	this.webrtcommCall = undefined;  // lower level call structure
}

/**
 * Accept an incoming call
 * @param {dictionary} parameters - Parameters for the connection <br>
 * <b>localMedia</b> : Local media stream, usually an HTML5 video or audio element <br>
 * <b>remoteMedia</b> : Remote media stream, usually an HTML5 video or audio element <br>
 * <b>videoEnabled</b> : Should we enable video internally when calling WebRTC getUserMedia() (boolean) <br>
 */
Connection.prototype.accept = function(parameters)
{
	if (this.device.debugEnabled) {
		console.log("Connection::accept():" + JSON.stringify(parameters));
	}

	this.device.sounds.audioRinging.pause();

	remoteMedia = parameters['remote-media'];
	this.parameters['video-enabled'] = parameters['video-enabled'];

	if (!parameters['fake-media']) {
		parameters['fake-media'] = false;
	}

	var that = this;
	// webrtc getUserMedia
	getUserMedia({audio:true, video:parameters['video-enabled'], fake: parameters['fake-media']}, 
			function(stream) {
				// got local stream as result of getUserMedia() -add it to localVideo html element
				if (that.debugEnabled) {
					console.log("Connection::accept(), received local WebRTC stream");
				}
				parameters['local-media'].src = URL.createObjectURL(stream);
				localStream = stream;

				var callConfiguration = {
							 displayName: wrtcConfiguration.sip.sipDisplayName,
							 localMediaStream: localStream,
							 audioMediaFlag: true,
							 videoMediaFlag: parameters['video-enabled'],
							 messageMediaFlag: false,
							 audioCodecsFilter: '',
							 videoCodecsFilter: ''
				};

				if (that.webrtcommCall) {
					that.webrtcommCall.accept(callConfiguration);
					that.status = 'open';
				}

				if (localStream.getVideoTracks().length > 0) {
					if (that.debugEnabled) {
						console.log("Connection::accept(): Using video device: " + localStream.getVideoTracks()[0].label);
					}
				}
				if (localStream.getAudioTracks().length > 0) {
					if (that.debugEnabled) {
						console.log("Connection::accept(): Using audio device: " + localStream.getAudioTracks()[0].label);
					}
				}
			},
			function(error) {
				console.log("Device::setup(), getUserMedia error: ", error);

				that.onError("Error in getUserMedia()" + error);
			}
	);

}

/**
 * Sends DTMF digits over this Connection
 * @param {String} digits - DTMF digits to send across the Connection
 */
Connection.prototype.sendDigits = function(digits)
{
	if (this.device.debugEnabled) {
		console.log("Connection::sendDigits(): " + JSON.stringify(digits));
	}

	if (this.webrtcommCall) {
		this.webrtcommCall.sendDTMF(digits);
	}
}


/**
 * Register callback to be notified when there is a Connection error
 * @param {function} callback - Callback to be invoked. Callback will be invoked as: callback(Connection)
 */
Connection.prototype.error = function(callback)
{
	// we are passed a callback, need to keep the listener for later use
	if (this.device.debugEnabled) {
		console.log("Connection: assign error callback");
	}

	this.onError = callback;
}

/**
 * This function has a dual purpose: a. if invoked with a single function
 * argument it registers a callback to be notified when the connection is
 * disconnected, and b. if invoked with no arguments it disconnects the connection
 * @param {function} callback - Callback to be invoked in (a). Callback will be invoked as: callback(Connection)
 */
Connection.prototype.disconnect = function(callback)
{
	if (callback !== undefined) {
		// we are passed a callback, need to keep the listener for later use
		if (this.device.debugEnabled) {
			console.log("Connection: assign disconnect callback");
		}

		this.onDisconnect = callback;
	}
	else {
		// we are not passed any argument, just disconnect
		if (this.device.debugEnabled) {
			console.log("Connection: disconnecting");
		}

		if (inCall) {
			this.webrtcommCall.close();
			this.webrtcommCall = undefined;
			inCall = false;
		}
		else {
			this.device.sounds.audioRinging.pause();
			this.device.sounds.audioCalling.pause();
		}

	}
}

/**
 * Assign callback to get call media stats when call is over
 * @param {function} callback - Callback to be invoked
 */
/*
Connection.prototype.stats = function(callback)
{
	// we are passed a callback, need to keep the listener for later use
	if (this.device.debugEnabled) {
		console.log("Connection: assign stats callback");
	}

	this.onStats = callback;
}
*/

/**
 * Get call media stats on-demand. Normally stats are returned in the end of the call, but you can request on-demand stats with this API
 * @param {function} callback - Callback to be invoked when stats are ready. Callback will be invoked as: callback(Dictionary). Dictionary will include the following keys: <br>
 * <b>media-type</b> : audio/video (ff only, until we figure it out for chrome) <br>
 * <b>direction</b> : inbound/outbound <br>
 * <b>bitrate</b> : bitrate in kbit/sec, like 250 kbit/sec (ff only) <br>
 * <b>packetsLost</b> : lost packet count <br>
 * <b>bytesTransfered</b>: bytes sent/received <br>
 * <b>packetsTransfered</b>: packets sent/received <br>
 * <b>jitter</b> : jitter for incoming packets <br>
 * <b>ssrc</b> : synchronization source for this stream, like 501954246
 */
Connection.prototype.getStats = function(callback)
{
	// we are passed a callback, need to keep the listener for later use
	if (this.device.debugEnabled) {
		console.log("Connection: assign getStats callback");
	}

	this.onStats = callback;
	this.webrtcommCall.getStats();
}

/**
 * This function has a dual purpose: a. if invoked with a single function
 * argument it registers a callback to be notified when the connection's audio is
 * muted/unmuted, and b. if invoked with a boolean argument it mutes/unmutes Connection's audio 
 * @param {Varies} arg1 - Callback to be invoked in (a), boolean mute flag in (b)
 */
Connection.prototype.mute = function(arg1)
{
	if (typeof arg1 == "function") {
		// we are passed a callback, need to keep the listener for later use
		var callback = arg1;
		if (this.device.debugEnabled) {
			console.log("Connection: assign mute callback");
		}

		this.onMute = callback;
	}
	else {
		// we are passed boolean argument, mute/unmute
		var muted = arg1;
		if (this.device.debugEnabled) {
			console.log("Connection::mute(): " + muted);
		}

		if (this.webrtcommCall) {
			if (muted) {
				this.webrtcommCall.muteLocalAudioMediaStream();
			}
			else {
				this.webrtcommCall.unmuteLocalAudioMediaStream();
			}

			this.muted = muted;

			// Notify asynchronously of the mute action
			var that = this;
			setTimeout(function() {
				 that.onMute(muted, that);
			}, 1);
		}
	}
}

/**
 * This function has a dual purpose: a. if invoked with a single function
 * argument it registers a callback to be notified when the connection's video is
 * muted/unmuted, and b. if invoked with a boolean argument it mutes/unmutes Connection's video 
 * @param {Varies} arg1 - Callback to be invoked in (a), boolean mute flag in (b)
 */
Connection.prototype.muteVideo = function(arg1)
{
	if (typeof arg1 == "function") {
		// we are passed a callback, need to keep the listener for later use
		var callback = arg1;
		if (this.device.debugEnabled) {
			console.log("Connection: assign mute callback");
		}

		this.onMuteVideo = callback;
	}
	else {
		// we are passed boolean argument, mute/unmute
		var muted = arg1;
		if (this.device.debugEnabled) {
			console.log("Connection::muteVideo(): " + muted);
		}

		if (this.webrtcommCall) {
			if (muted) {
				this.webrtcommCall.hideLocalVideoMediaStream();
			}
			else {
				this.webrtcommCall.showLocalVideoMediaStream();
			}

			this.videoMuted = muted;

			// Notify asynchronously of the mute action
			var that = this;
			setTimeout(function() {
				 that.onMuteVideo(muted, that);
			}, 1);
		}
	}
}

/**
 * Reject a pending (ringing) incoming connection
 * @function Device#reject
 */
Connection.prototype.reject = function() {
	this.device.sounds.audioRinging.pause();
	this.webrtcommCall.reject();
	this.status = 'closed';
	this.device.status = 'ready';
} 

/**
 * Ignore a pending (ringing) incoming connection. The connection is closed but no answer is sent to the caller
 * @function Device#ignore
 */
Connection.prototype.ignore = function() {
	this.device.sounds.audioRinging.pause();
	this.webrtcommCall.ignore();
	this.status = 'closed';
	this.device.status = 'ready';
} 

/**
 * Return the status of the connection
 * @function Device#status
 */
Connection.prototype.status = function() {
	return this.status;	
} 

Connection.prototype.parameters = function() {
	return this.parameters;
}

/**
 * RestCommClient is a namespace for Client Library entities
 * @namespace
 */
var RestCommClient = {
	/**
	 * @class Device
	 * @classdesc <p>A Device represents an abstraction of a communications device able to make and receive calls, send and receive messages etc. Remember that in order to be notified of RestComm Client events you need to 'register' for interesting events by passing callbacks. If you want to initiate a media connection towards another party you use [Device.connect(parameters)]{@link Device#connect} which returns a Connection object representing the new outgoing connection. From then on you can act on the new connection by applying Connection methods on the handle you got from [Device.connect(parameters)]{@link Device#connect}. If there’s an incoming connection and you have previously registered a callback for receiving incoming connection events by calling [Device.incoming(callback)]{@link Device#incoming}, you will be notified through that callback. At that point you can use Connection methods to accept or reject the connection.</p>
	 * <p>As far as instant messages are concerned you can send a text message using [Device.sendMessage(parameters)]{@link Device#sendMessage} and you will be notified of an incoming message if you have previously registered a callback for incoming messages by calling [Device.message(callback)]{@link Device#message}.</p>
	 * @public
	 */
	Device: {
		// --- Callbacks for Device events
		/** 
		 * Callback called when Device is ready (internal)
		 * @name Device#onReady
		 * @type Function
		 * @ignore
		 */
		onReady: null,
		/** 
		 * Called if there's an error with the Device (internal)
		 * @name Device#onError
		 * @type Function
		 * @ignore
		 */
		onError: null,
		/** 
		 * Called when Connection state changes (internal)
		 * @name Device#onConnect
		 * @type Function
		 * @ignore
		 */
		onConnect: null,
		/** 
		 * Called when incoming Connection arrives (internal)
		 * @name Device#onIncoming
		 * @type Function
		 * @ignore
		 */
		onIncoming: null,
		/** 
		 * Called when incoming text message arrives (internal)
		 * @name Device#onMessage
		 * @type Function
		 * @ignore
		 */
		onMessage: null,
		/** 
		 * Called when Connection disconnects (internal)
		 * @name Device#onDisconnect
		 * @type Function
		 * @ignore
		 */
		onDisconnect: null,
		/** 
		 * Called when Device goes offline (internal)
		 * @name Device#onOffline
		 * @type Function
		 * @ignore
		 */
		onOffline: null,
		/** 
		 * Called when an incoming call is cancelled by the caller before being accepted (internal)
		 * @name Device#onCancel
		 * @type Function
		 * @ignore
		 */
		onCancel: null,
		/** 
		 * Called when there is a presence update for the client (Not Implemented yet)
		 * @name Device#onDisconnect
		 * @type Function
		 * @ignore
		 */
		onPresence: null,

		/**
		 * Status of the Device. Possible values are: <b>ready</b>, <b>offline</b>, <b>busy</b>. When 'ready' a Device is connected to RestComm and able to receive and make Connections. When 'offline' the Device is not connected to RestComm. When 'busy' the Device is connected to RestComm but already has an active Connection and hence cannot make or receive calls.
		 * @name Device#status
		 * @type String
		 */
		status: 'offline',
		/**
		 * Current Connection belonging to Device
		 * @name Device#connection
		 * @type Object
		 */
		connection: null,

		// is debug logging enabled
		debugEnabled: false,

		sounds: {
			// sound files to be used for various events
			soundRinging: 'scripts/sounds/ringing.mp3',
			soundCalling: 'scripts/sounds/calling.mp3',
			soundMessage: 'scripts/sounds/message.mp3',

			// audio objects to handle sound playback
			audioRinging: null,
			audioCalling: null,
			audioMessage: null,

			incomingEnabled: true,
			outgoingEnabled: true,
			disconnectEnabled: true,

			incoming: function(mute) {
				if (typeof mute == 'boolean') {
					this.incomingEnabled = mute;	
				}
				else {
					return this.incomingEnabled;
				}
			}, 

			outgoing: function(mute) {
				if (typeof mute == 'boolean') {
					this.outgoingEnabled = mute;	
				}
				else {
					return this.outgoingEnabled;
				}
			}, 

			disconnect: function(mute) {
				if (typeof mute == 'boolean') {
					this.disconnectEnabled = mute;	
				}
				else {
					return this.disconnectEnabled;
				}
			}, 
		},

		/**
		 * Setup RestComm Web Client SDK 'Device' entity
		 * @function Device#setup
		 * @param {string} parameters - Parameters for the Device entity: <br>
		 * <b>username</b> : Username for the client, i.e. <i>web-sdk</i> <br>
		 * <b>password</b> : Password to be used in client authentication, i.e. <i>1234</i> <br>
		 * <b>registrar</b> : URL for the registrar, i.e. <i>wss://cloud.restcomm.com:5063</i> <br>
		 * <b>domain</b> : domain to be used, i.e. <i>cloud.restcomm.com</i> <br>
		 * <b>debug</b> : Enable debug logging in browser console <br>
		 */
		setup: function(parameters) {
			if ('debug' in parameters && parameters['debug'] == true) {
				this.debugEnabled = true;
			}

			if (this.debugEnabled) {
				console.log("Device::setup(): " + JSON.stringify(parameters));
			}

			// if parameters.registrar is either unset or empty we should function is registrar-less mode
			//if (parameters['registrar'] && parameters['registrar'] != "") {
			// let's default to register until https://github.com/Mobicents/webrtcomm/issues/24 is fixed
			var register = true;
			if ('register' in parameters && parameters['register'] == false) {
				register = false;
			}

			// Once https://github.com/Mobicents/webrtcomm/issues/24 is fixed we can remove these lines and pass down registrar and domain to webrtcomm
			if (!parameters['registrar'] || parameters['registrar'] == "") {
				console.log("Device::setup(): registrar has not been provided. Defaulting to wss://cloud.restcomm.com:5063");
				parameters['registrar'] = 'wss://cloud.restcomm.com:5063';
			}
			if (!parameters['domain'] || parameters['domain'] == "") {
				console.log("Device::setup(): domain has not been provided. Defaulting to cloud.restcomm.com");
				parameters['domain'] = 'cloud.restcomm.com';
			}

			// setup WebRTClient
			wrtcConfiguration = {
				communicationMode: WebRTCommClient.prototype.SIP,
				sip: {
					sipUserAgent: 'TelScale RestComm SBC Web Client 1.0.0 BETA4',
					sipRegisterMode: register,
					sipOutboundProxy: parameters['registrar'],
					sipDomain: parameters['domain'],
					sipDisplayName: parameters['username'],
					sipUserName: parameters['username'],
					sipLogin: parameters['username'],
					sipPassword: parameters['password'],
				},
				RTCPeerConnection: {
					iceServers: undefined,
					//stunServer: 'stun.l.google.com:19302',
					stunServer: undefined,
					turnServer: undefined,
					turnLogin: undefined,
					turnPassword: undefined,
				}
			};

			username = parameters['username'];

			// setup sounds
			/*
			this.soundRinging = parameters['ringing-sound'];
			this.soundCalling = parameters['calling-sound'];
			this.soundMessage = parameters['message-sound'];
			*/

			this.sounds.audioRinging = new Audio(this.sounds.soundRinging);
			this.sounds.audioRinging.loop = true;
			this.sounds.audioCalling = new Audio(this.sounds.soundCalling);
			this.sounds.audioCalling.loop = true;
			this.sounds.audioMessage = new Audio(this.sounds.soundMessage);

			// create listener to retrieve webrtcomm events
			wrtcEventListener = new WrtcEventListener(this);

			// initialize webrtcomm facilities through WebRTCommClient and register with RestComm
			wrtcClient = new WebRTCommClient(wrtcEventListener);
			wrtcClient.open(wrtcConfiguration);
		},

		/**
		 * Register callback to be notified when Device is ready
		 * @function Device#ready
		 * @param {function} callback - Callback to be invoked. Callback will be invoked as: callback(Device)
		 */
		ready: function(callback) {
			if (this.debugEnabled) {
				console.log("Device::ready(), assigning ready callback");
			}

			this.onReady = callback;
		},

		/**
		 * Register callback to be notified when an incoming call arrives. That callback provides a Connection object to the App
		 * @function Device#incoming
		 * @param {function} callback - Callback to be invoked. Callback will be invoked as: callback(Connection)
		 */
		incoming: function(callback) {
			if (this.debugEnabled) {
				console.log("Device::incoming(), assigning incoming callback");
			}

			this.onIncoming = callback;
		},

		/**
		 * Register callback to be notified when the device goes offline
		 * @function Device#offline
		 * @param {function} callback - Callback to be invoked. Callback will be invoked as: callback(Device)
		 */
		offline: function(callback) {
			if (this.debugEnabled) {
				console.log("Device::offline(), assigning offline callback");
			}

			this.onOffline = callback;
		},

		/**
		 * Register callback to be notified when an incoming call is canceled from the caller
		 * @function Device#cancel
		 * @param {function} callback - Callback to be invoked. Callback will be invoked as: callback(Connection)
		 */
		cancel: function(callback) {
			if (this.debugEnabled) {
				console.log("Device::cancel(), assigning cancel callback");
			}

			this.onCancel = callback;
		},

		/**
		 * Register callback to be notified when an incoming message arrives
		 * @function Device#message
		 * @param {function} callback - Callback to be invoked. Callback will be invoked as: callback(parameters). Parameters are:<br>
		 * <b>From</b> : Originator of the text message, i.e. <i>bob</i> <br>
		 * <b>Text</b> : Actual text of the message
		 */
		message: function(callback) {
			if (this.debugEnabled) {
				console.log("Device::message(), assigning message callback");
			}

			this.onMessage = callback;
		},

		/**
		 * Register callback to be notified when there's a presence event (Not implemented yet)
		 * @function Device#presence
		 * @param {function} callback - Callback to be invoked
		 */
		presence: function(callback) {
			if (this.debugEnabled) {
				console.log("Device::presence(), assigning presence callback");
			}

			console.log("Device::presence(), Presence is not implemented yet in RestComm Web Client library");

			this.onPresence = callback;
		},

		/**
		 * Register callback to be notified when there's a Device error and convey a string describing the error
		 * @function Device#error
		 * @param {function} callback - Callback to be invoked. Callback will be invoked as: callback(String)
		 */
		error: function(callback) {
			if (this.debugEnabled) {
				console.log("Device::error(), assigning error callback");
			}

			this.onError = callback;
		},

		/**
		 * This function has a dual purpose: a. if invoked with a single function
		 * argument it registers a callback to be notified when there's an update
		 * in the Connection like transitioning to Connected, etc, and b. if
		 * invoked with two optional non function arguments it initiates a call towards a
		 * remote party with the given params
		 * @function Device#connect
		 * @param {varies} arg1 - Callback to be invoked (a) or params (b). In (a) callback will be invoked as: callback(Connection)
		 * @param {dictionary} arg2 - Parameters for the connection: <br>
		 * <b>username</b> : Username for the called party, i.e. <i>+1235@cloud.restcomm.com</i> <br>
		 * <b>localMedia</b> : Local media stream, usually an HTML5 video or audio element <br>
		 * <b>remoteMedia</b> : Remote media stream, usually an HTML5 video or audio element <br>
		 * <b>videoEnabled</b> : Should we enable video for this call (boolean) <br>
		 */
		connect: function(arg1, arg2) {
			if (typeof arg1 == "function") {
				// we are passed a callback, need to keep the listener for later use
				if (this.debugEnabled) {
					console.log("Device::connect(), assigning connect callback");
				}
				var callback = arg1;
				this.onConnect = callback;
			}
			else {
				// we are passed regular arguments, let's connect
				if (this.debugEnabled) {
					console.log("Device::connect(): " + JSON.stringify(arg1));
				}

				var parameters = arg1;
				// not implemented yet
				var audioConstraints = arg2;

				// store remote media element for later
				remoteMedia = parameters['remote-media'];

				this.connection = new Connection(this, 'connecting');
				this.connection.parameters = {
					'From': wrtcConfiguration.sip.sipUserName, 
					'To': parameters['username'], 
					'video-enabled': parameters['video-enabled'],
				};

				if (!parameters['fake-media']) {
					parameters['fake-media'] = false;
				}
				var that = this;
				// webrtc getUserMedia
				getUserMedia({audio:true, video:parameters['video-enabled'], fake: parameters['fake-media']}, 
						function(stream) {
							// got local stream as result of getUserMedia() -add it to localVideo html element
							if (that.debugEnabled) {
								console.log("Device::connect(), received local WebRTC stream");
							}
							parameters['local-media'].src = URL.createObjectURL(stream);
							localStream = stream;

							var callConfiguration = {
										 displayName: wrtcConfiguration.sip.sipDisplayName,
										 localMediaStream: localStream,
										 audioMediaFlag: true,
										 videoMediaFlag: parameters['video-enabled'],
										 messageMediaFlag: false,
										 audioCodecsFilter: '',
										 videoCodecsFilter: ''
							};

							that.connection.webrtcommCall = wrtcClient.call(parameters['username'], callConfiguration);

							that.status = 'busy';

							if (localStream.getVideoTracks().length > 0) {
								if (that.debugEnabled) {
									console.log("Device::connect(): Using video device: " + localStream.getVideoTracks()[0].label);
								}
							}
							if (localStream.getAudioTracks().length > 0) {
								if (that.debugEnabled) {
									console.log("Device::connect(): Using audio device: " + localStream.getAudioTracks()[0].label);
								}
							}
						},
						function(error) {
							console.log("Device::connect(), getUserMedia error: ", error);

							that.onError("Error in getUserMedia()" + error);
						}
				);

				return this.connection;
			}
		},

		/**
		 * Send text message
		 * @function Device#sendMessage
		 * @param {dictionary} parameters - Parameters for the message: <br>
		 * <b>username</b> : target URI <br>
		 * <b>message</b> : text message to send
		 */
		sendMessage: function(parameters) {
			// right now we are not interested in sending message directly to an ongoing call since it will complicate the API. But let's leave this piece of code
			// around because we might need it in the future
			/*
			if (this.connection && this.connection.webrtcommCall && this.connection.webrtcommCall.peerConnectionState === 'established') {
				this.connection.webrtcommCall.sendMessage(parameters.message);
			}
			*/
			
			if (this.debugEnabled) {
				console.log("Device::sendMessage(): " + JSON.stringify(parameters));
			}
			wrtcClient.sendMessage(parameters['username'], parameters['message']);
		},

		/**
		 * Register callback to be notified when Connection is disconnected
		 * @function Device#disconnect
		 * @param {function} callback - Callback to be invoked. Callback will be invoked as: callback(Connection)
		 */
		disconnect: function(callback) {
			if (this.debugEnabled) {
				console.log("Device::disconnect(), assigning disconnect callback");
			}

			this.onDisconnect = callback;
		},

		/**
		 * Disconnect all active Connections
		 * @function Device#disconnectAll
		 */
		disconnectAll: function() {
			if (this.debugEnabled) {
				console.log("Device::disconnectAll()");
			}

			this.connection.disconnect();
			if (this.onDisconnect) {
				this.onDisconnect(this);
			}
		},

		/**
		 * Return the status of the Device
		 * @function Device#status
		 */
		status: function() {
			if (this.debugEnabled) {
				console.log("Device::status()");
			}

			return this.status;
		},

		/**
		 * Return the active Connection
		 * @function Device#activeConnection
		 */
		activeConnection: function() {
			if (this.debugEnabled) {
				console.log("Device::activeConnection()");
			}

			return this.connection;
		},

		/**
		 * Terminate the Device. To be able to use Device again you need to call [RestCommClient.Device.setup(parameters)]{@link Device#setup}
		 * @function Device#destroy
		 */
		destroy: function() {
			if (this.debugEnabled) {
				console.log("Device::destroy()");
			}

			wrtcClient.close();
			this.status = 'offline';
		}
	}
}