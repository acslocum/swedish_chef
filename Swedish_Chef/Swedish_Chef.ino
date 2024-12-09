int analogSensorPin = A5;  
int analogSensorValue = 0;  //  the value coming from the output cable on the Reaper
int outputPin = 2;

int analogOut = 3; // use only pin 3! not 5 or 6! 

int incomingByte = 0; // for incoming serial data

void setup() {
   pinMode(2, OUTPUT);     // output to the Punk Rocker's motion sensor port

  Serial.begin(9600);
  Serial.println("starting");
}

void loop(){

  if (Serial.available() > 0) {
      // read the incoming byte:
      incomingByte = Serial.read();

      if(incomingByte > 0) {
        Serial.println(" *** PLAYING ***");
    
        // the motion sensor in the Punk Rocker appears to be listening for a change in state
        // so it has get both a HIGH and LOW here in order to think motion happened and continuously play a sequence
        digitalWrite(outputPin, LOW);
        delay(200);
        digitalWrite(outputPin, HIGH); // make the Punk Rocker think it saw motion
    
    
        analogWrite(analogOut, analogSensorValue); // send analog signal back to next Reaper Band prop

        delay(100);
        digitalWrite(outputPin, LOW); // tell the motion sensor port on the Punk Rocker that nothing is happening

      } else {
        digitalWrite(outputPin, LOW); // tell the motion sensor port on the Punk Rocker that nothing is happening
        Serial.println(" *** Stopping ***");
      }
      
  }
  delay(500);
}
