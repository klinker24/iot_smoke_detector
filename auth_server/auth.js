//Lets require/import the HTTP module
var http = require('http');
var dispatcher = require('httpdispatcher');

var NodeRSA = require('node-rsa');
var key = new NodeRSA({b: 1024});
console.log(key.exportKey("pkcs8-private"));
console.log(key.exportKey("pkcs8-public-pem"));

//Lets define a port we want to listen to
const PORT=8889; 

//We need a function which handles requests and send response
function handleRequest(request, response){
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
	res.writeHead(200, {'Content-Type': 'text/plain'});
	res.end(key.exportKey("pkcs8-private"));
});

//Create a server
var server = http.createServer(handleRequest);

//Lets start our server
server.listen(PORT, function() {
    //Callback triggered when server is successfully listening. Hurray!
    console.log("Server listening on: http://localhost:%s", PORT);
});