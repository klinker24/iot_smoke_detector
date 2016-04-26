/**
 * Provides methods for the IoT Smoke Web UI and interaction with the
 * IoT Smoke API.
 */

var appengine = appengine || {};

appengine.CLIENT_ID =
    '846797458679-hh9rprrj2gocpo4sbn9sd8iskro6jcj3.apps.googleusercontent.com';
appengine.SCOPES =
    'https://www.googleapis.com/auth/userinfo.email';

appengine.signedIn = false;

// Loads the application UI after the user has completed auth.
appengine.userAuthed = function() {
  console.log("user authorized function");

  var request = gapi.client.oauth2.userinfo.get().execute(function(resp) {
    if (!resp.code) {
      appengine.signedIn = true;
      document.getElementById('email').textContent = resp.email;
      document.getElementById('userPicture').src = resp.picture;
      document.getElementById('signinText').textContent = 'Sign out';
      appengine.listReadings();
    }
  });
};

/**
 * Handles the auth flow, with the given value for immediate mode.
 * @param {boolean} mode Whether or not to use immediate mode.
 * @param {Function} callback Callback to call on completion.
 */
appengine.signin = function(mode, callback) {
  console.log("sign in function");

  gapi.auth.authorize({client_id: appengine.CLIENT_ID,
      scope: appengine.SCOPES, immediate: mode},
      callback);
};

// Presents the user with the authorization popup.
appengine.auth = function() {
  console.log("auth function");

  if (!appengine.signedIn) {
    appengine.signin(false, appengine.userAuthed);
  } else {
    appengine.signedIn = false;

    var dataDiv = document.getElementById('dataDisplay');
    var statusDiv = document.getElementById('dataStatus');

    document.getElementById('signinText').textContent = 'Sign in';

    dataDiv.innerHTML = '';
    statusDiv = 'Please sign in.';
  }
};

/**
 * Prints a response to the response box
 * param {Object} response Response to display
 */
appengine.print = function(response) {
  console.log("print function");

  var dataDiv = document.getElementById('dataDisplay');
  var statusDiv = document.getElementById('dataStatus');

  if (response && response.error) {
    dataDiv.innerHTML = 'Account Setup Needed.';
    statusDiv.innerHTML = 'Please use the Android app to set up an account.';
  } else {
    if (!response.items) {
      dataDiv.innerHTML = '';
      statusDiv.innerHTML = 'No data has been received.';
    } else {
      dataDiv.innerHTML = '<b>Temperature:</b> ' + response.items[0].temperature + '°F</br>' + 
                          '<b>Relative Humidity:</b> ' + response.items[0].relativeHumidity + '%</br>' +
                          '<b>Particle Density:</b> ' + response.items[0].particleDensity + ' </h2>';

      if (response.items[0].temperature > 100) {
        statusDiv.innerHTML = 'Woah, it is really hot - there may be an issue!';
      } else {
        statusDiv.innerHTML = 'Everything looks good!';
      }
    }
  }
};

// gets the air quality readings for the authenticated user
appengine.listReadings = function(id) {
  console.log("list readings function");

  var dataDiv = document.getElementById('dataDisplay');
  var statusDiv = document.getElementById('dataStatus');

  dataDiv.innerHTML = '';
  statusDiv.innerHTML = 'Loading data...'

  gapi.client.airQuality.listReadings().execute(
      function(resp) {
        appengine.print(resp);
        setTimeout(appengine.listReadings, 30000); // refresh the data every 30 seconds
      });
};

// Enables the button callbacks in the UI.
appengine.setButtonClicks = function() {
  console.log("set button clicks function");

  // get button variables
  var signinButton = document.getElementById('signinButton');

  // add the click events
  signinButton.addEventListener('click', appengine.auth);
};

// Initializes the application.
// Loads the OAuth and airQuality APIs asynchronously
appengine.init = function(apiRoot) {
  console.log("init function");

  var dataDiv = document.getElementById('dataDisplay');
  var statusDiv = document.getElementById('dataStatus');

  document.getElementById('signinText').textContent = 'Sign in';

  dataDiv.innerHTML = '';
  statusDiv = 'Please sign in.';

  var apisToLoad;
  var callback = function() {
    if (--apisToLoad == 0) {
      appengine.setButtonClicks();
      appengine.signin(true, appengine.userAuthed);
    }
  }

  apisToLoad = 2; // must match number of calls to gapi.client.load()
  gapi.client.load('airQuality', 'v1', callback, apiRoot);
  gapi.client.load('oauth2', 'v2', callback);
};