int dustPin = 0;
float dustVal = 0;

int ledPower = 2;
int delayTime = 280;
int delayTime2 = 40;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  pinMode(ledPower,OUTPUT);
  pinMode(A0, OUTPUT);

}

void loop() {
  // put your main code here, to run repeatedly:
  digitalWrite(ledPower,LOW);
  delayMicroseconds(delayTime);
  dustVal= analogRead(dustPin);
  delayMicroseconds(delayTime2);
  digitalWrite(ledPower,HIGH);
  delayMicroseconds(5000);

  delay(3000);
  Serial.println(dustVal);
}
