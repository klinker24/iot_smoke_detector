# Internet Enabled Smoke Detector (IoT)

This repo houses a comprehensive IoT application. It is designed with four parts:

- Android App
- Google App Engine Backend (with datastore, GCM support, and website)
- Raspberry Pi Authentication Server
- Arduino

## System Overview

The idea behind this project is to use the Arduino to read an analog signal with some air quality information, then send that information along the stack to be stored in the backend datastore. In the datastore, this information will be analyzed, and if there are any concerns, a push notification will be sent to the user's Android application to notify them that there could be a problem with the air quality in the sensor location. The basic flow looks like this:

1. The Android app is used to register your account.
2. When your account is registered with the GAE backend, an auth token is generated and returned to the Android app.
3. The Android app sends that auth token to the Raspberry Pi (via a Node.js server over LAN).
4. Since the Arduino reads analog signals much easier than Raspberry Pi, it is used to read in the air quality data.
5. The Arduino will read the air quality on a one minute interval and send the data over bluetooth to the Raspberry Pi.
6. When the Pi receives data, it will then use it's unique auth token to send that data up to the backend to be associated with the user account.
7. If that data is analyzed and there is a problem with the air quality, the backend will send a notification to the Android app[s] registered with that account.
8. The Android app and the website can also check the data manually. They are able to display the current air quality and the past air quality.

## Security Overview

One of the principle components of any IoT application should be security. Ours application provides a number of security measures to keep your data safe. 

- **Registration is tied to your Google account**  
Google App Engine has built in security measures that will force a user to be authenticated before accessing any data. Once these security measures are enabled, with every request, you get an associated user profile. This user profile is used to create an account for you within our app, and tie any data to your account. Registration in this application is done through the Android app.

- **Retrieving any air quality data requires authentication**  
The same way that we have tied registration to an account, retreiving data requires you to be authenticated with your Google account. This can be done on the website or on the Android app.

- **SSL**  
By default, Google App Engine uses SSL to encyrpt and protect the information you upload and receive from it.

- **Authenticating from Arduino/Raspberry Pi**  
Since these are lower level devices without a user facing interface, we didn't want to make the user log in with their Google account to them. From the backend, we generate a unique auth token for every account and send it to the Raspberry Pi. We send this auth token over a LAN connection, so the only way an intruder can get that info is if they are on your network. When it is sent, the auth token also is encrypted with RSA encryption. The Pi broadcasts it's public key, which the Android app uses to encrypt, then, the Pi is able to decrypt with it's unique private key.

- **Usage of the auth token**  
Even if an intruder were able to grab your auth token when it is transfered from the Android app to the Pi, the only usage for this token is inserting data to the database. So, while they can insert fake values, which could still be a malicous case for an air quality detector, they won't have access to any data on your account and it will not compromise the security of any other accounts on the system.

- **Intercepting air quality data**  
Since bluetooth isn't a very secure protocol, there is the possibility that someone listening could intercept - or add fake data - going between the Arduino and Raspberry Pi. This case is similar to the auth token usage. It has a malicous outcome, but will only affect the current user, and the Man-in-the-Middle has to be within bluetooth range.


## Future Considerations

Obviously there are malicious attacks that could happen to a user on the local scale, that we are not handling at this time. If this were a production application we would have to take those into further consideration. As a class project, we feel as though the security aspect is fairly good between all the moving peices, with the biggest (and one of the only) threats being an insecure bluetooth connection.

The website of the app would also need an overhaul for a production application. It works well, but the authentication with Google is a bit rough around the edges. Right now it is not set up to display past data either, only the current air quality. Mainly it would just need cleaned up a bit.

To analyze the data, we are just using a canned function that we created from some stock data that we received. For a production application, we would want to adjust this function over time to take into account the new data that our users are pushing to the backend.


## License

    Copyright 2016 Luke Klinker

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
