// require/import the HTTP module
var http = require('http');
var dispatcher = require('httpdispatcher');

// file system manipulation
var fs = require('fs');

// bluetooth module
var noble = require('noble');

// helps to send post requests to server
var request = require('request');

// define an RSA public/private key to use
var NodeRSA = require('node-rsa');
var key = new NodeRSA({b: 1024});

// see if we already have an auth token defined
var authToken = null;

fs.readFile('auth.txt', 'utf8', function(err, data) {
    if (!err) {
        console.log("Found auth token: " + data);
        authToken = data;
    }
});

// defines the port we want to listen on
const PORT=8889; 

// handles requests and send response
function handleRequest(request, response) {
    try {
    	dispatcher.dispatch(request, response);
    } catch(err) {
    	console.log(err);
    }
}

// when we receive a get request at /auth
dispatcher.onGet("/auth", function(req, res) {
	res.writeHead(200, {'Content-Type': 'text/plain'});
	res.end(key.exportKey("pkcs8-public-pem"));
});

// when we receive a post request to /auth
dispatcher.onPost("/auth", function(req, res) {
	var encryptedMessage = JSON.parse(req.body).message;
	var decryptedMessage = key.decrypt(encryptedMessage, 'utf-8');
	res.writeHead(200, {'Content-Type': 'text/plain'});
	res.end("");

	console.log("Found auth token: " + decryptedMessage);

    authToken = decryptedMessage;
    fs.writeFile("auth.txt", authToken, { flag: 'w' }, function (err) {});
});

// create a server
var server = http.createServer(handleRequest);

// start our server
server.listen(PORT, function() {
    console.log("Server listening on: http://localhost:%s", PORT);
});

// start searching for bluetooth devices
noble.on('stateChange', function(state) {
  if(state === 'poweredOn') {
    noble.startScanning();
  }
});

// discover bluetooth devices that we can get data from
noble.on('discover', function(device) {

  // TODO change this to the correct mac address
  if (device.address === 'f0:5f:f6:ef:43:ef') {
    console.log('Found device: ' + device.address);

    // found our device, now connect to it
    noble.stopScanning();

    device.connect(function(error) {

      // Once connected, we need to kick off service discovery
      device.discoverAllServicesAndCharacteristics(function(error, services, characteristics) {

        // discovery done! Find characteristics we care about
        var uartRx = null;
        characteristics.forEach(function(ch, chID) {
          if (ch.uuid === '6e400003b5a3f393e0a9e50e24dcca9e') {
            uartRx = ch;
          }
        });

        // check if we found UART Rx characteristic
        if (!uartRx) {
          console.log('Failed to find UART Rx Characteristic! ');
          process.exit();
        }

        // wait to get data from the arduino and upload it to server where we can parse it
        uartRx.notify(true);
        uartRx.on('read', function(data, isNotification) {
          var array = data.toString().split(",");

          var url = "https://uiowa-iot-smoke.appspot.com/_ah/api/airQuality/v1/collectionresponse_airqualityrecord/";
          url = url + encodeURIComponent(array[1]);
          url = url + "/";
          url = url + encodeURIComponent(array[2]);
          url = url + "/";
          url = url + encodeURIComponent(array[0]);
          url = url + "/";
          url = url + encodeURIComponent(authToken);

          request.post(url, { form: {} }, function (error, response, body) {});
          console.log("Uploaded data: " + data.toString());
        });
      });
    });
  }
});
