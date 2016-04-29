#include <dht.h>

#include <Arduino.h>
#include <SPI.h>
#include <dht.h>
#if not defined (_VARIANT_ARDUINO_DUE_X_) && not defined (_VARIANT_ARDUINO_ZERO_)
  #include <SoftwareSerial.h>
#endif
#include "Adafruit_BluefruitLE_SPI.h"
#include "BluefruitConfig.h"

#define COV_RATIO                   0.02            //ug/mmm / mv
#define NO_DUST_VOLTAGE             400            //mv
#define SYS_VOLTAGE                 5000
#define FACTORYRESET_ENABLE         1
#define MINIMUM_FIRMWARE_VERSION    "0.6.6"
#define MODE_LED_BEHAVIOUR          "MODE"
#define DHT11_PIN                   7               // pin for temp/humidity sensor

const int iled = 2;                                            // drive the led of sensor
const int vout = 0;                                            // analog input

float density, voltage;
int   adcvalue;
float temp, humidity, dew, relativeHumidity;

Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_CS, BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);
dht DHT;

// dewPoint function NOAA From: http://playground.arduino.cc/Main/DHT11Lib
// reference (1) : http://wahiduddin.net/calc/density_algorithms.htm
// reference (2) : http://www.colorado.edu/geography/weather_station/Geog_site/about.htm
//
float dewPoint(float celsius, float humidity)
{
  // (1) Saturation Vapor Pressure = ESGG(T)
  float RATIO = 373.15 / (273.15 + celsius);
  float RHS = -7.90298 * (RATIO - 1);
  RHS += 5.02808 * log10(RATIO);
  RHS += -1.3816e-7 * (pow(10, (11.344 * (1 - 1/RATIO ))) - 1) ;
  RHS += 8.1328e-3 * (pow(10, (-3.49149 * (RATIO - 1))) - 1) ;
  RHS += log10(1013.246);

  // factor -3 is to adjust units - Vapor Pressure SVP * humidity
  float VP = pow(10, RHS - 3) * humidity;

  // (2) DEWPOINT = F(Vapor Pressure)
  float T = log(VP/0.61078);   // temp var
  return (241.88 * T) / (17.558 - T);
}

// Relative Humdity Function
//Valid for Depoint between 0-50 C
//Valid for Celcius between 0-60 C
//Valid for Relative Humidty 1% - 100%
//reference (1) : http://andrew.rsmas.miami.edu/bmcnoldy/humidity_conversions.pdf
float relativeHum(float temp, float dewPoint)
{
  //constants
  float a = 17.625;
  float b = 243.04;
  
  float top = exp((a*dewPoint)/(b + dewPoint));
  float bottom = exp((a*temp)/(b + temp));
  float RH = 100*(top/bottom);
  return RH;
}

void setup(void)
{
  while (!Serial);
  delay(500);

  pinMode(iled, OUTPUT);
  digitalWrite(iled, LOW);
  
  Serial.begin(115200);

  if ( !ble.begin(VERBOSE_MODE) )
  {
    error(F("Couldn't find Bluefruit, make sure it's in CoMmanD mode & check wiring?"));
  }

  if ( FACTORYRESET_ENABLE )
  {
    /* Perform a factory reset to make sure everything is in a known state */
    Serial.println(F("Performing a factory reset: "));
    if ( ! ble.factoryReset() ){
      error(F("Couldn't factory reset"));
    }
  }

  ble.echo(false);
  ble.info();
  ble.verbose(false);

  while (! ble.isConnected()) {
    delay(500);
  }

  if ( ble.isVersionAtLeast(MINIMUM_FIRMWARE_VERSION) )
  {
    ble.sendCommandCheckOK("AT+HWModeLED=" MODE_LED_BEHAVIOUR);
  }
}

void loop(void)
{
  // get adcvalue
  digitalWrite(iled, HIGH);
  delayMicroseconds(280);
  adcvalue = analogRead(vout);
  digitalWrite(iled, LOW);
  
  adcvalue = filter(adcvalue);
  
  // covert voltage (mv)
  voltage = (SYS_VOLTAGE / 1024.0) * adcvalue * 11;

  // voltage to density
  if(voltage >= NO_DUST_VOLTAGE)
  {
    voltage -= NO_DUST_VOLTAGE;
    density = voltage * COV_RATIO;
  }
  else
  {
    density = 0;
  }
  
  // read temp and humidity
  int chk = DHT.read11(DHT11_PIN);
  delay(10);
  temp = DHT.temperature;
  humidity = DHT.humidity;
  dew = dewPoint(temp,humidity);
  relativeHumidity = relativeHum(temp,dew);

  // display the result
  Serial.print("The current dust concentration is: ");
  Serial.print(density);
  Serial.print(" ug/m3\n");
  Serial.print("The current temperature is: ");
  Serial.print(temp);
  Serial.print("\n");
  Serial.print("The current humidity is: ");
  Serial.print(humidity);
  Serial.print("\n\n\n");
  Serial.print("The current dew Point is: ");
  Serial.print(dew);
  Serial.print("\n\n\n");
  Serial.print("The current relative humidity is: ");
  Serial.print(relativeHumidity);
  Serial.print("\n\n\n");

  // send the result over bluetooth. this will be formatted as <density>,<temperature>,<humidity>
  ble.print("AT+BLEUARTTX=");
  ble.print(density);
  ble.print(",");
  ble.print(temp);
  ble.print(",");
  ble.println(relativeHumidity);
  

  if (! ble.waitForOK() ) {
    Serial.println(F("Failed to send?"));
  }

  // wait 30 seconds to get the new data
  delay(30000);
}

int filter(int m)
{
  static int flag_first = 0, _buff[10], sum;
  const int _buff_max = 10;
  int i;

  if (flag_first == 0)
  {
    flag_first = 1;

    for(i = 0, sum = 0; i < _buff_max; i++)
    {
      _buff[i] = m;
      sum += _buff[i];
    }
    return m;
  }
  else
  {
    sum -= _buff[0];
    for(i = 0; i < (_buff_max - 1); i++)
    {
      _buff[i] = _buff[i + 1];
    }
    _buff[9] = m;
    sum += _buff[9];

    i = sum / 10.0;
    return i;
  }
}

void error(const __FlashStringHelper*err) {
  Serial.println(err);
  while (1);
}
