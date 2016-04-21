var noble = require('noble');
// Check if BLE adapter is powered on
noble.on('stateChange', function(state) {
  if(state === 'poweredOn') {
    console.log('Powered on!');
    noble.startScanning();
  }
});

//Register function to receive newly discovered devices
noble.on('discover', function(device) {
  if(device.address === 'e3:0e:74:88:38:5e') {
    console.log('Found device: ' + device.address);
    //found our device, now connect to it
    //Be sure to turn off scanning before connecting
    noble.stopScanning();
    device.connect(function(error) {
      // Once connected, we need to kick off service discovery
      device.discoverAllServicesAndCharacteristics(function(error, services, characteristics) {
        //Discovery done! Find characteristics we care about
        var uartTx = null;
        var uartRx = null;
        //look for UART service characteristic
        characteristics.forEach(function(ch, chID) {
          if (ch.uuid === '6e400002b5a3f393e0a9e50e24dcca9e') {
            uartTx = ch;
            console.log("Found UART Tx characteristic");
          }
          if (ch.uuid === '6e400003b5a3f393e0a9e50e24dcca9e') {
            uartRx = ch;
            console.log("Found UART Rx characteristic");
          }
        });
        //Check if we found UART Tx characteristic
        if (!uartTx) {
          console.log('Failed to find UART Tx Characteristic! ');
          process.exit();
        }
        //Check if we found UART Rx characteristic
        if (!uartRx) {
          console.log('Failed to find UART Rx Characteristic! ');
          process.exit();
        }
        //set up listener for console input
        //when console input is received, send it to uartTx
        var stdin = process.openStdin();
        stdin.addListener("data", function (d) {
          // d will have a linefeed at the end.  Get rid ofit with trim
          var inStr = d.toString().trim();
          //Can only send 20 bytes in a Bluetooth LE packet
          //so truncate string if it is too long
          if (inStr.length > 20) {
            inStr = inStr.slice(0, 19);
          }
          console.log("Sent: " + inStr);
          uartTx.write(new Buffer(inStr));
        });
        // Now set up listener to receive data from uartRx
        //and display on console
        uartRx.notify(true);
        uartRx.on('read', function(data, isNotification) {
          console.log ("Received: " + data.toString());
        });
      });  //end of device.discover
    });   //end of device.connect
  }      //end of if (device.address...
});     //end of noble.on   
    
