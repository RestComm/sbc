//TRY TYPING IN ONE OF THESE NUMBERS:
//
// 1234567890
// 0651985833
//


$('.number-dig').click(function(){
  //add animation
  addAnimationToButton(this);
  //add number
  var currentValue = $('.phoneString input').val();
  var valueToAppend = $(this).attr('name');
  $('.phoneString input').val(currentValue + valueToAppend);
  
  checkNumber();
});


var timeoutTimer = true;
var timeCounter = 0;
var timeCounterCounting = true;

$('.action-dig').click(function(){
  //add animation
  addAnimationToButton(this);
  if($(this).hasClass('goBack')){
    var currentValue = $('.phoneString input').val();
    var newValue = currentValue.substring(0, currentValue.length - 1);
    $('.phoneString input').val(newValue);
    checkNumber();
  }else if($(this).hasClass('call')){
    if($('.call-pad').hasClass('in-call')){
      setTimeout(function(){
        setToInCall();
      }, 500);
      timeCounterCounting = false;
      timeCounter = 0;
      hangUpCall();
      $('.pulsate').toggleClass('active-call');
      
      $('.phoneString input').val('');
      checkNumber();
    }else{
      $('.ca-status').text('Calling');
      setTimeout(function(){
        setToInCall();
        timeoutTimer = true;
        looper();
        //showActiveCallAfterAFewSeconds
        setTimeout(function(){
          timeoutTimer = false;
          timeCounterCounting = true;
          timeCounterLoop();
          
          $('.pulsate').toggleClass('active-call');
          $('.ca-status').animate({
            opacity: 0,
          }, 1000, function() {
            $(this).text('00:00');
            $('.ca-status').attr('data-dots', '');
            
            $('.ca-status').animate({
              opacity: 1,
            }, 1000);
          });
        },3000);
      },500);
    }
  }else{
    
  }
});

var timeCounterLoop = function(){
  
  if(timeCounterCounting){
    setTimeout(function(){
      var timeStringSeconds = '';
      var minutes = Math.floor(timeCounter/60.0);
      var seconds = timeCounter%60;
      if(minutes < 10){
        minutes = '0' + minutes;
      }
      if(seconds < 10){
        seconds = '0' + seconds;
      }
      $('.ca-status').text(minutes + ':' + seconds);
      
      timeCounter += 1;
      
      timeCounterLoop();
    }, 2000);
  }
};
 
var setToInCall = function(){
  $('.call-pad').toggleClass('in-call');
  $('.call-icon').toggleClass('in-call');
  $('.call-change').toggleClass('in-call');
  $('.ca-avatar').toggleClass('in-call');
};

var dots = 0;
var looper = function(){
  if(timeoutTimer){
    
   setTimeout(function(){
    if(dots > 3){
      dots = 0;
    }
    var dotsString = '';
    for(var i = 0; i < dots; i++){
      dotsString += '.';
    }
    $('.ca-status').attr('data-dots',dotsString);
    dots += 1;
     
    looper();
  }, 500); 
  }
};

var hangUpCall = function(){
  timeoutTimer = false;
};

var addAnimationToButton = function(thisButton){
  //add animation
  $(thisButton).removeClass('clicked');
  var _this = thisButton;
  setTimeout(function(){
    $(_this).addClass('clicked');
  },1);
};

var checkNumber = function(){
  var numberToCheck = $('.phoneString input').val();
  var contactMatt = {
    name: 'Osky Carriles',
    number: '123456789',
    image: 'img/osky.png',
    desc: 'SBC'
  };
  var contactHellogiov = {
    name: 'hellogiov',
    number: '0651985833',
    image: 'http://avatars-cdn.producthunt.com/207787/220',
    desc: 'Publicis Nurun'
  };
  if (numberToCheck.length > 0 && contactMatt.number.substring(0, numberToCheck.length) == numberToCheck) {
      //show this contact!
      showUserInfo(contactMatt);
  }else if(numberToCheck.length > 0 && contactHellogiov.number.substring(0, numberToCheck.length) == numberToCheck){
    showUserInfo(contactHellogiov);
  }else{
    hideUserInfo();
  }
};

var showUserInfo = function(userInfo){
  $('.avatar').attr('style', "background-image: url("+userInfo.image+")");
  if(!$('.contact').hasClass('showContact')){
    $('.contact').addClass('showContact');
  }
  $('.contact-name').text(userInfo.name);
  $('.contact-position').text(userInfo.desc);
  var matchedNumbers = $('.phoneString input').val();
  var remainingNumbers = userInfo.number.substring(matchedNumbers.length);
  $('.contact-number').html("<span>"+matchedNumbers+"</span>" + remainingNumbers);
  
  //update call elements
  $('.ca-avatar').attr('style', 'background-image: url('+userInfo.image+')');
  $('.ca-name').text(userInfo.name);
  $('.ca-number').text(userInfo.number);
  
};

var hideUserInfo = function(){
   $('.contact').removeClass('showContact');
};