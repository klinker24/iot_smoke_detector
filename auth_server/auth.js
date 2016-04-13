// require/import the HTTP module
var http = require('http');
var dispatcher = require('httpdispatcher');

// define an RSA public/private key to use
var NodeRSA = require('node-rsa');
var key = new NodeRSA({b: 1024});

// defines the port we want to listen on
const PORT=8889; 

// handles requests and send response
function handleRequest(request, response) {
    try {
    	console.log(request.url);
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
	var encryptedMessage = req.params['message'].split(' ').join('+');
	var decryptedMessage = key.decrypt(encryptedMessage, 'utf-8');
	res.writeHead(200, {'Content-Type': 'text/plain'});
	res.end("");

	// TODO do something with the decrypted message, which will contain a 
	//      user id and an auth token, separated by a comma. I think it
	//      would work well to set these as environment variables to easily
	//	be accessed from our other code that takes readings of air
	//      quality.
});

// create a server
var server = http.createServer(handleRequest);

// start our server
server.listen(PORT, function() {
    console.log("Server listening on: http://localhost:%s", PORT);
});
