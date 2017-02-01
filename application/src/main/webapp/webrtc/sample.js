/*
 * Oscar Carriles ocarriles@eolos.la
 */

var session;

var endButton   = document.getElementById('endCall');
var startButton = document.getElementById('startCall');
var phone       = document.getElementById('phone');
document.getElementById('1').addEventListener("click", function() { session.dtmf(1);}, false);
document.getElementById('2').addEventListener("click", function() { session.dtmf(2);}, false);
document.getElementById('3').addEventListener("click", function() { session.dtmf(3);}, false);
document.getElementById('4').addEventListener("click", function() { session.dtmf(4);}, false);
document.getElementById('5').addEventListener("click", function() { session.dtmf(5);}, false);
document.getElementById('6').addEventListener("click", function() { session.dtmf(6);}, false);
document.getElementById('7').addEventListener("click", function() { session.dtmf(7);}, false);
document.getElementById('8').addEventListener("click", function() { session.dtmf(8);}, false);
document.getElementById('9').addEventListener("click", function() { session.dtmf(9);}, false);
document.getElementById('0').addEventListener("click", function() { session.dtmf(0);}, false);

endButton.addEventListener("click", function () {
    session.bye();
    //alert("Call Ended");
}, false);

startButton.addEventListener("click", function () {
	//makes the call
	session = userAgent.invite(phone.value, options);
    //alert("Call Started");
	session.on('terminated', function (response) {
		 alert("Hangup");
	});
	session.on('accepted', function (data) {
		 alert("Accepted ");
	});

}, false);

var config = {
	
		// cloud
		  uri: '21@10.0.0.10:5083',
		  wsServers: ['wss://10.0.0.10:5083'],
		  //wServers: 'wss://cloud.eolos.la:5083',
		  authorizationUser: '21',
		  password: 'Demo1088', 
		 
		// media gw
		/*
		  uri: '1062@cloud.eolos.la:8088',
		  ws_servers: 'ws://cloud.eolos.la:8088/ws',
		  authorizationUser: '1062',
		  password: 'password', 
		 */
		  hackIpInContact: true,

		};

		var userAgent = new SIP.UA(config);


//here you determine whether the call has video and audio
var options = {
    media: {
        constraints: {
            audio: true,
            video: true
        }
    }
};

userAgent.on('invite', function (session) {
	 alert("Incomming call");
});






