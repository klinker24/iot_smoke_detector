# Arduino

We opted to use an Arduino with this project since it is extremely easy to read an analog signal (coming from the air quality sensor).

This part of the application will connect to the Raspberry Pi over bluetooh, then, use a serial output to send the data it collects to the Pi. It will read the data every 1 minute.