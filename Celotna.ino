#include <SoftwareSerial.h>

//Create software serial object to communicate with SIM800L
SoftwareSerial mySerial(3, 2); //SIM800L Tx & Rx is connected to Arduino #3 & #2
const int RELAY_PIN = 4;//to je pin za rele
String incomingSMS;

unsigned long casovnica[]={0,0,0,0,0,0,0,0,0,0};
boolean boilerStatus=false;


unsigned long startSeconds;
unsigned long currentSeconds;
int indexCasovnice;


void setup()
{
 pinMode(RELAY_PIN, OUTPUT);
 digitalWrite(RELAY_PIN, HIGH);
 
  //Begin serial communication with Arduino and Arduino IDE (Serial Monitor)
 Serial.begin(9600);
  //Begin serial communication with Arduino and SIM800L
  mySerial.begin(9600);

 
  Serial.println("Initializing...");
  delay(1000);
  
  mySerial.println("AT"); //Once the handshake test is successful, it will back to OK
  updateSerial();
  mySerial.println("AT+CSQ"); //Signal quality test, value range is 0-31 , 31 is the best
  updateSerial();
  mySerial.println("AT+CCID"); //Read SIM information to confirm whether the SIM is plugged
  updateSerial();
  mySerial.println("AT+CREG?"); //Check whether it has registered in the network
  updateSerial();
  mySerial.println("AT+CMGF=1"); // Configuring TEXT mode
  updateSerial();

 mySerial.println("AT+CMGDA=\"DEL ALL\"");//to naj bi zbrisalo vsa sporočila iz pomnilnika/SIM kartice
  updateSerial();
  
  /* mySerial.println("AT+CLTS=1");//za čas
  updateSerial();

  */mySerial.println("AT+CLTS?");//za čas
  updateSerial();/*
  
mySerial.println("AT&W");//za čas
  updateSerial();*/
  
  mySerial.println("AT+CCLK?");//za čas
  updateSerial();
  /*mySerial.println("AT+CCLK =\"15/06/23,13:28:00+28\"");//za čas
  updateSerial();*/


 
 /* mySerial.println("AT+CPMS=\"ME\"");//ta del je za to da izbereš drug pomnilnik kam shranjevati SMSe, ker na SIM-u zmanjka pomnilnika in potem SMSjev ne moreš več prejemati
  updateSerial();
 */
  mySerial.println("AT+CNMI=1,2,0,0,0"); // Decides how newly arrived SMS messages should be handled*/
  updateSerial();
  delay(1000);
  
}




void loop()
{
  
  prejmiSMS();
  currentSeconds=millis()/1000;

  findIndexCasovnice();
  
  if(indexCasovnice%2==0){
    digitalWrite(RELAY_PIN, HIGH);//to pomeni da na releju ne sveti zelena lučka
    delay(1000);
   
  }
  else if(indexCasovnice%2==1){
    digitalWrite(RELAY_PIN, LOW);
    delay(1000);
  }
  
 
  
/* Serial.print(casovnica[0]);
 Serial.print(" ");
 Serial.print(currentSeconds);
 Serial.print(" ");
 Serial.print(startSeconds+casovnica[0]);
 Serial.print(" ");
 Serial.println(currentSeconds<=(startSeconds+casovnica[0]));*/



 // digitalRead(RELAY_PIN) to ti da vrednost 0 ali 1, vrednost 0 je enaka LOW in 1 je enaka HIGH
}

void updateSerial()
{
  delay(500);
  while (Serial.available()) 
  {
    mySerial.write(Serial.read());//Forward what Serial received to Software Serial Port
  }
  while(mySerial.available()) 
  {
    Serial.write(mySerial.read());//Forward what Software Serial received to Serial Port
  }
}



void prejmiSMS()
{
  
  delay(60000);//lahko je 60000
//spodnji del kode prebere kar pride na serial port (prebere en po en character in ga nato prevede v String)
  String content = "";
  char character;
  
  while (mySerial.available())
  {
      character = mySerial.read();
      content.concat(character);
}

     // Serial.write(mySerial.read());//Forward what Software Serial received to Serial Port
if (content != "") {
      Serial.println(content);
  }

  if(content.indexOf("+CMT")>=0&&content.indexOf("OFF")>=0){
    startSeconds=millis()/1000;//z tem si postaviš začetni čas - ko si prejel SMS
    indexCasovnice=0;
    fromStringToArray(content);
  }
  
  
}



//vrne array v velikosti 10ih vklopov in izklopov (iz stringa prebere koliko časa mora biti še ugasnjen in kdaj se mora prižgati)
void fromStringToArray(String mySMS)
{
 // static unsigned long casovnica[10];
  for (int i = 0; i < 10; i=i+2) {
    Serial.println(mySMS.substring(mySMS.indexOf("OFF:")+4,mySMS.indexOf(";")));
    casovnica[i]=mySMS.substring(mySMS.indexOf("OFF:")+4,mySMS.indexOf(";")).toInt();
    mySMS=mySMS.substring(mySMS.indexOf(";")+1);
    Serial.println(mySMS.substring(mySMS.indexOf("ON:")+3,mySMS.indexOf(";")).toInt());
    casovnica[i+1]=mySMS.substring(mySMS.indexOf("ON:")+3,mySMS.indexOf(";")).toInt();
    mySMS=mySMS.substring(mySMS.indexOf(";")+1);
}
}

//logična funkcija ki ugotovi kateri odsek array časovnice je trenutno aktualen za rele
void findIndexCasovnice(){
  if(casovnica[0]<casovnica[1]){
  if(currentSeconds<=casovnica[0]+startSeconds){
      indexCasovnice=0;
    }
    else if(currentSeconds>=casovnica[9]+startSeconds&&casovnica[9]!=0){
      indexCasovnice=100;
    }
    else{
        for(int i=0;i<9;i++){
          if(currentSeconds>=casovnica[i]+startSeconds&&currentSeconds<=casovnica[i+1]+startSeconds&&casovnica[i+1]!=0){
            indexCasovnice=i+1;
            break;
            }
            else if(casovnica[i+1]==0){
              indexCasovnice=102;
            }
            }
    }}
    else{
      Serial.println("Check casovnica or there is no casovnica");
    }
 Serial.print("Index casovnice je");
 Serial.println(indexCasovnice);
}


void vklapljaneReleja(){
  digitalWrite(RELAY_PIN, HIGH);
  delay(1000);
  digitalWrite(RELAY_PIN, LOW);
  delay(1000);
}




//NAVODILA
//Iz androida moraš poslati SMS v formatu:
//OFF:_____;ON:_____;OFF:____;ON:___;OFF:_____;ON:_____;OFF:____;ON:___;OFF:_____;ON:_____;
//Pošlješ lahko največ 10 časov, točno tako kot je narejen zgornji primer
//Če pošlješ manj časov ni problema, je koda narejena tako, da ko mine zadnji čas, se rele izklopi
//Mora pa biti vedno najprej OFF in potem ON in vedno morata biti v paru!
//časi v SMSju morajo biti v sekundah, te sekunde pa so definirane tako:
//Čas pri OFF pomeni,da bo rele ugasnjen od čas poslanega SMSja do časa napisanega poleg OFF
//Čas pri ON pomeni da bo rele prižgan med časom OFF in do konca časa napisanega poleg časa ON po poteku vseh ciklov
//Za ročni vklop pošlješ SMS:
//OFF:0;ON:99999
//Za ročni izklop pošlješ SMS:
//OFF:0;ON:1
