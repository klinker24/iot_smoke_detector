# Auth Server for Raspberry Pi

This module serves as a simple node.js webserver that can serve up a public key and stores a private key so that we can encrypt user information with the public key and store it on the Raspberry Pi for use when we hit the backend.

Usage is simple:

* `GET /auth` will return the public key you should use to encrypt with
* `POST /auth` will take an encrypted string and decrypt it using it's private key

The app uses port `8889` for operation.

You can start the server by using `node auth` from the Raspberry Pi.