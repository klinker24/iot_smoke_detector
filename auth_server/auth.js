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
	var encryptedMessage = req.params['message'];
	res.writeHead(200, {'Content-Type': 'text/plain'});
	res.end(key.exportKey("pkcs8-private"));
});

// create a server
var server = http.createServer(handleRequest);

// start our server
server.listen(PORT, function() {
    console.log("Server listening on: http://localhost:%s", PORT);
});
