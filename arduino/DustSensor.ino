#include <Arduino.h>
#include <SPI.h>
#include <dht.h>
#if not defined (_VARIANT_ARDUINO_DUE_X_) && not defined (_VARIANT_ARDUINO_ZERO_)
  #include <SoftwareSerial.h>
#endif
#include "Adafruit_BluefruitLE_SPI.h"
#include "BluefruitConfig.h"

#define COV_RATIO                   0.2            //ug/mmm / mv
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
float temp, humidity;

Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_CS, BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);
dht DHT;

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

  // send the result over bluetooth. this will be formatted as <density>,<temperature>,<humidity>
  ble.print("AT+BLEUARTTX=");
  ble.print(density);
  ble.print(",");
  ble.print(temp);
  ble.print(",");
  ble.println(humidity);

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
