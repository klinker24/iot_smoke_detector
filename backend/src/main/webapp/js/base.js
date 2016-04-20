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
  var request = gapi.client.oauth2.userinfo.get().execute(function(resp) {
    if (!resp.code) {
      appengine.signedIn = true;
      document.querySelector('#signinButton').textContent = 'Sign out';
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
  gapi.auth.authorize({client_id: appengine.CLIENT_ID,
      scope: appengine.SCOPES, immediate: mode},
      callback);
};

// Presents the user with the authorization popup.
appengine.auth = function() {
  if (!appengine.signedIn) {
    appengine.signin(false, appengine.userAuthed);
  } else {
    appengine.signedIn = false;

    var signinRequiredDiv = document.getElementById('signinRequired');
    var loadingDiv = document.getElementById('loadingData');
    var dataDiv = document.getElementById('data');

    document.querySelector('#signinButton').textContent = 'Sign in';

    signinRequired.style.display = 'block';
    loadingDiv.style.display = 'none';
    dataDiv.style.display = 'none';
  }
};

/**
 * Prints a response to the response box
 * param {Object} response Response to display
 */
appengine.print = function(response) {
  var signinRequiredDiv = document.getElementById('signinRequired');
  var loadingDiv = document.getElementById('loadingData');
  var dataDiv = document.getElementById('data');

  signinRequired.style.display = 'none';
  loadingDiv.style.display = 'none';
  dataDiv.style.display = 'block';

  if (response && response.error) {
    dataDiv.innerHTML = '<b>Error Code:</b> ' + response.error.code + ' [' + response.error.message +']';
  } else {
    if (!response.items) {
      dataDiv.innerHTML = 'No data has been recorded! </h2>';
    } else {
      dataDiv.innerHTML = response.items[0].data + ' </h2>';
    }
  }
};

// gets the air quality readings for the authenticated user
appengine.listReadings = function(id) {
  var signinRequiredDiv = document.getElementById('signinRequired');
  var loadingDiv = document.getElementById('loadingData');
  var dataDiv = document.getElementById('data');

  signinRequired.style.display = 'none';
  loadingDiv.style.display = 'block';
  dataDiv.style.display = 'none';

  gapi.client.airQuality.listReadings().execute(
      function(resp) {
        appengine.print(resp);
      });
};

// Enables the button callbacks in the UI.
appengine.setButtonClicks = function() {
  // get button variables
  var signinButton = document.querySelector('#signinButton');

  // add the click events
  signinButton.addEventListener('click', appengine.auth);
};

// Initializes the application.
// Loads the OAuth and airQuality APIs asynchronously
appengine.init = function(apiRoot) {
  var apisToLoad;
  var callback = function() {
    if (--apisToLoad == 0) {
      appengine.setButtonClicks();
    }
  }

  apisToLoad = 2; // must match number of calls to gapi.client.load()
  gapi.client.load('airQuality', 'v1', callback, apiRoot);
  gapi.client.load('oauth2', 'v2', callback);
};