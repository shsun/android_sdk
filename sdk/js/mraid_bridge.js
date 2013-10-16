(function() {

   var mraidview = window.mraidview = {};

 
 
   /****************************************************/
   /********** PROPERTIES OF THE ORMMA BRIDGE **********/
   /****************************************************/
 
   /** Expand Properties */
   var expandProperties = {
        useBackground:false,
        backgroundColor:'#ffffff',
        backgroundOpacity:1.0,
        lockOrientation:false
    };
 
 
   /** The set of listeners for ORMMA Native Bridge Events */
   var listeners = { };
 
   /** Holds the current dimension values */
   dimensions : {};
        
   /** A Queue of Calls to the Native SDK that still need execution */
   var nativeCallQueue = [ ];
 
   /** Identifies if a native call is currently in progress */
   var nativeCallInFlight = false;
 
   /** timer for identifying iframes */
   var timer;
   var totalTime;

 
 
   /**********************************************/
   /************* JAVA ENTRY POINTS **************/
   /**********************************************/

   /**
    * Called by the JAVA SDK when an asset has been fully cached.
    *
    * @returns string, "OK"
    */
   mraidview.fireAssetReadyEvent = function( alias, URL ) {
      var handlers = listeners["assetReady"];
      if ( handlers != null ) {
         for ( var i = 0; i < handlers.length; i++ ) {
            handlers[i]( alias, URL );
         }
      }
 
      return "OK";
   };
 
 
   /**
    * Called by the JAVA SDK when an asset has been removed from the
	* cache at the request of the creative.
    *
    * @returns string, "OK"
    */
   mraidview.fireAssetRemovedEvent = function( alias ) {
      var handlers = listeners["assetRemoved"];
      if ( handlers != null ) {
         for ( var i = 0; i < handlers.length; i++ ) {
            handlers[i]( alias );
         }
      }
 
      return "OK";
   };
 
 
   /**
    * Called by the JAVA SDK when an asset has been automatically
	* removed from the cache for reasons outside the control of the creative.
    *
    * @returns string, "OK"
    */
   mraidview.fireAssetRetiredEvent = function( alias ) {
      var handlers = listeners["assetRetired"];
      if ( handlers != null ) {
         for ( var i = 0; i < handlers.length; i++ ) {
            handlers[i]( alias );
         }
      }
 
      return "OK";
   };
 
 
   /**
	* Called by the JAVA SDK when various state properties have changed.
    *
    * @returns string, "OK"
	*/
   mraidview.fireChangeEvent = function( properties ) {
      var handlers = listeners["change"];
      if ( handlers != null ) {
         for ( var i = 0; i < handlers.length; i++ ) {
		    handlers[i]( properties );
         }
      }
 
      return "OK";
   };
 
 
   /**
    * Called by the JAVA SDK when an error has occured.
    *
    * @returns string, "OK"
    */
   mraidview.fireErrorEvent = function( message, action ) {
      var handlers = listeners["error"];
      if ( handlers != null ) {
         for ( var i = 0; i < handlers.length; i++ ) {
            handlers[i]( message, action );
         }
      }
 
      return "OK";
   };
 
 
   /**
    * Called by the JAVA SDK when the user shakes the device.
    *
    * @returns string, "OK"
    */
   mraidview.fireShakeEvent = function() {
      var handlers = listeners["shake"];
      if ( handlers != null ) {
         for ( var i = 0; i < handlers.length; i++ ) {
            handlers[i]();
         }
      }
 
      return "OK";
   };
 
 
   
 
 
   /**
    *
    */
   mraidview.showAlert = function( message ) {
      	MRAIDUtilityControllerBridge.showAlert( message );
   };
 
 
   /*********************************************/
   /********** INTERNALLY USED METHODS **********/
   /*********************************************/
 
 
   /**
    *
    */
   mraidview.zeroPad = function( number ) {
      var text = "";
      if ( number < 10 ) {
         text += "0";
      }
	  text += number;
      return text;
   }
 
 
 
 
   /***************************************************************************/
   /********** LEVEL 0 (not part of spec, but required by public API **********/
   /***************************************************************************/
 
   /**
    *
    */
   mraidview.activate = function( event ) {
   		 MRAIDUtilityControllerBridge.activate(event);
   };

 
   /**
    *
    */
   mraidview.addEventListener = function( event, listener ) {
      var handlers = listeners[event];
	  if ( handlers == null ) {
		 // no handlers defined yet, set it up
         listeners[event] = [];
         handlers = listeners[event];
      }
 
      // see if the listener is already present
	  for ( var handler in handlers ) {
	     if ( listener == handler ) {
		    // listener already present, nothing to do
			return;
		}
	  }
 
      // not present yet, go ahead and add it
      handlers.push( listener );
   };


   /**
    *
    */
   mraidview.deactivate = function( event ) {
	   MRAIDUtilityControllerBridge.deactivate(event);
   };

 
   /**
    *
    */
   mraidview.removeEventListener = function( event, listener ) {
	  var handlers = listeners[event];
	  if ( handlers != null ) {
         handlers.remove( listener );
	  }
   };
 

 
   /*****************************/
   /********** LEVEL 1 **********/
   /*****************************/

   /**
    *
    */
   mraidview.close = function() {
   try {
   	 	MRAIDDisplayControllerBridge.close();
	  } catch ( e ) {
	     mraidview.showAlert( "close: " + e );
	  }
   };
 
 
   /**
    *
    */
   mraidview.expand = function( dimensions, URL ) {
	  try {
		 this.dimensions = dimensions;
		 MRAIDDisplayControllerBridge.expand(mraidview.stringify(dimensions), URL, mraidview.stringify(expandProperties));
	  } catch ( e ) {
	     mraidview.showAlert( "executeNativeExpand: " + e + ", dimensions = " + dimensions  + ", URL = " + URL + ", expandProperties = " + expandProperties);
	  }
   };

 
   /**
    *
    */
   mraidview.hide = function() {
   try {
	  MRAIDDisplayControllerBridge.hide();
	  } catch ( e ) {
	     mraidview.showAlert( "hide: " + e );
	  }
   };

 
   /**
    *
    */
   mraidview.open = function( URL, controls ) {
	  // the navigation parameter is an array, break it into its parts
	  var back = false;
	  var forward = false;
	  var refresh = false;
	  if ( controls == null ) {
		 back = true;
		 forward = true;
		 refresh = true;
	  }
	  else {
		 for ( var i = 0; i < controls.length; i++ ) {
			if ( ( controls[i] == "none" ) && ( i > 0 ) ) {
			   // error
			   self.fireErrorEvent( "none must be the only navigation element present.", "open" );
			   return;
			}
			else if ( controls[i] == "all" ) {
			   if ( i > 0 ) {
				   // error
				   self.fireErrorEvent( "none must be the only navigation element present.", "open" );
				   return;
				}
				
				// ok
				back = true;
				forward = true;
				refresh = true;
			}
			else if ( controls[i] == "back" ) {
				back = true;
			}
			else if ( controls[i] == "forward" ) {
				forward = true;
			}
			else if ( controls[i] == "refresh" ) {
				refresh = true;
			}
	     }
	  }
	
	 try{
	  MRAIDDisplayControllerBridge.open(URL, back, forward, refresh);
   		} catch ( e ) {
	     mraidview.showAlert( "open: " + e );
	  }
   
   };
   
   /**
   *
   */
  mraidview.openMap = function( POI, fullscreen ) {
      try{
    	  MRAIDDisplayControllerBridge.openMap(POI, fullscreen);
      } catch ( e ) {
	     mraidview.showAlert( "openMap: " + e );
	  }
  };

   
  /**
  *
  */
  mraidview.playAudio = function( URL, properties ) {
	  
	  alert('playAudio');
	
	var autoPlay = false, controls = false, loop = false, position = false, 
	    startStyle = 'normal', stopStyle = 'normal';
	 
    if ( properties != null ) {
        
        if ( ( typeof properties.autoplay != "undefined" ) && ( properties.autoplay != null ) ) {
            autoPlay = true;
        }
       
        if ( ( typeof properties.controls != "undefined" ) && ( properties.controls != null ) ) {
        	controls = true;
        }
        
        if ( ( typeof properties.loop != "undefined" ) && ( properties.loop != null ) ) {
        	loop = true;
        }
        
        if ( ( typeof properties.position != "undefined" ) && ( properties.position != null ) ) {
        	position = true;
        }
        
        //TODO check valid values...           
        
        if ( ( typeof properties.startStyle != "undefined" ) && ( properties.startStyle != null ) ) {
             startStyle = properties.startStyle;
        }
        
        if ( ( typeof properties.stopStyle != "undefined" ) && ( properties.stopStyle != null ) ) {
            stopStyle = properties.stopStyle;
        }  
        
        if(startStyle =='normal') {
        	position = true;
        }
        
 		 if(position) {
       		autoPlay = true;
       		controls = false;
       		loop = false;
       		stopStyle = 'exit';
       	}

       	if(loop) {
           stopStyle = 'normal'; 
           controls = true;
        }
        
        if(!autoPlay) {
        	controls = true;
        }
               	
       	if (!controls) {
			stopStyle = 'exit';
       }
    }  
    
    try{
  	  MRAIDDisplayControllerBridge.playAudio(URL, autoPlay, controls, loop, position, startStyle, stopStyle);
    } 
    catch ( e ) {
	     mraidview.showAlert( "playAudio: " + e );
	}     
 };
 
 
  /**
   *
   */
  mraidview.playVideo = function( URL, properties ) {
	 var audioMuted = false, autoPlay = false, controls = false, loop = false, position = [-1, -1, -1, -1], 
	    startStyle = 'normal', stopStyle = 'normal';
     if ( properties != null ) {
         
         if ( ( typeof properties.audio != "undefined" ) && ( properties.audio != null ) ) {
             audioMuted = true;
         }
         
         if ( ( typeof properties.autoplay != "undefined" ) && ( properties.autoplay != null ) ) {
             autoPlay = true;
         }
        
         if ( ( typeof properties.controls != "undefined" ) && ( properties.controls != null ) ) {
         	controls = true;
         }
         
         if ( ( typeof properties.loop != "undefined" ) && ( properties.loop != null ) ) {
         	loop = true;
         }
         
         if ( ( typeof properties.position != "undefined" ) && ( properties.position != null ) ) {
        	 inline = new Array(4);
        	 
        	 inline[0] = properties.position.top;
        	 inline[1] = properties.position.left;
        	 
             if ( ( typeof properties.width != "undefined" ) && ( properties.width != null ) ) {
            	 inline[2] =  properties.width;
             }
             else{
                 //TODO ERROR
             }
             
             if ( ( typeof properties.height != "undefined" ) && ( properties.height != null ) ) {
            	 inline[3] =  properties.height;
             }
             else{
                 //TODO ERROR
             }
         }
       

         if ( ( typeof properties.startStyle != "undefined" ) && ( properties.startStyle != null ) ) {
             startStyle = properties.startStyle;
         }
        
         if ( ( typeof properties.stopStyle != "undefined" ) && ( properties.stopStyle != null ) ) {
            stopStyle = properties.stopStyle;
         }  
         
		if (loop) {
			stopStyle = 'normal';
			controls = true;
		}

	    if (!autoPlay)
	        controls = true;
		        
	  	if (!controls) {
			stopStyle = 'exit';
		} 
		
		if(position[0]== -1 || position[1] == -1)   {
			startStyle = "fullscreen";
		}      
     }    
     
     try{
     	  MRAIDDisplayControllerBridge.playVideo(URL, audioMuted, autoPlay, controls, loop, position, startStyle, stopStyle);
       } 
       catch ( e ) {
   	     mraidview.showAlert( "playVideo: " + e );
   	}     

  };

   
   
 
   /**
    *
    */
   mraidview.resize = function( width, height ) {
   try {
	  MRAIDDisplayControllerBridge.resize(width, height);
	  } catch ( e ) {
	     mraidview.showAlert( "resize: " + e );
	  }
   };

   
   mraidview.getExpandProperties = function(){
	   return expandProperties;
   }
   
 
   /**
    *
    */
   mraidview.setExpandProperties = function( properties ) {
	  expandProperties = properties;
   };

 
   /**
    *
    */
   mraidview.show = function() {
   try{
	  MRAIDDisplayControllerBridge.show();
	  } catch ( e ) {
	     mraidview.showAlert( "show: " + e );
	  }
   };
 
 
 
   /*****************************/
   /********** LEVEL 2 **********/
   /*****************************/

   /**
    *
    */
   mraidview.createEvent = function( date, title, body ) {
      	var msecs=(date.getTime()-date.getMilliseconds());

		try {		
		MRAIDUtilityControllerBridge.createEvent(msecs.toString(), title, body);
		} catch ( e ) {
	     mraidview.showAlert( "createEvent: " + e );
	  }
		
   };
 
   /**
    *
    */
   mraidview.makeCall = function( phoneNumber ) {
   try {
	  MRAIDUtilityControllerBridge.makeCall(phoneNumber);
	  } catch ( e ) {
	     mraidview.showAlert( "makeCall: " + e );
	  }
   };
 
 
   /**
    *
    */
   mraidview.sendMail = function( recipient, subject, body ) {
   try {
	  MRAIDUtilityControllerBridge.sendMail(recipient, subject, body);
	  } catch ( e ) {
	     mraidview.showAlert( "sendMail: " + e );
	  }
   };
 

   /**
    *
    */
   mraidview.sendSMS = function( recipient, body ) {
   try {
	  MRAIDUtilityControllerBridge.sendSMS(recipient, body);
	  } catch ( e ) {
	     mraidview.showAlert( "sendSMS: " + e );
	  }
   };
 
   /**
    *
    */
   mraidview.setShakeProperties = function( properties ) {
   };
 
 
 
   /*****************************/
   /********** LEVEL 3 **********/
   /*****************************/

   /**
    *
    */
   mraidview.addAsset = function( URL, alias ) {
	 
   };
   /**
    *
    */
   mraidview.request = function( URI, display ) {
	  
   }; 
   /**
    *
    */
   mraidview.removeAsset = function( alias ) {
   };
   
   
   mraidview.stringify = function(args) {
    if (typeof JSON === "undefined") {
        var s = "";
        var len = args.length;
        var i;
        if (typeof len == "undefined"){
        	return mraidview.stringifyArg(args);
        }
        for (i = 0; i < args.length; i++) {
            if (i > 0) {
                s = s + ",";
            }
            s = s + mraidview.stringifyArg(args[i]);
        }
        s = s + "]";
        return s;
    } else {
        return JSON.stringify(args);
    }
};

	mraidview.stringifyArg = function(arg) {
        var s, type, start, name, nameType, a;
            type = typeof arg;
            s = "";
            if ((type === "number") || (type === "boolean")) {
                s = s + args;
            } else if (arg instanceof Array) {
                s = s + "[" + arg + "]";
            } else if (arg instanceof Object) {
                start = true;
                s = s + '{';
                for (name in arg) {
                    if (arg[name] !== null) {
                        if (!start) {
                            s = s + ',';
                        }
                        s = s + '"' + name + '":';
                        nameType = typeof arg[name];
                        if ((nameType === "number") || (nameType === "boolean")) {
                            s = s + arg[name];
                        } else if ((typeof arg[name]) === 'function') {
                            // don't copy the functions
                            s = s + '""';
                        } else if (arg[name] instanceof Object) {
                            s = s + this.stringify(args[i][name]);
                        } else {
                            s = s + '"' + arg[name] + '"';
                        }
                        start = false;
                    }
                }
                s = s + '}';
            } else {
                a = arg.replace(/\\/g, '\\\\');
                a = a.replace(/"/g, '\\"');
                s = s + '"' + a + '"';
            }
        mraidview.showAlert("json:"+ s);
		return s;
	}
   
   })();
