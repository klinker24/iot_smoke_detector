// require/import the HTTP module
var http = require('http');
var dispatcher = require('httpdispatcher');

// file system manipulation
var fs = require('fs');

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

dispatcher.onGet("/auth", function(req, res) {
	res.writeHead(200, {'Content-Type': 'text/plain'});
	res.end(key.exportKey("pkcs8-public-pem"));
});

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
