#include <ESP8266WiFi.h>
//Used from https://github.com/Imroy/pubsubclient
#include <PubSubClient.h>

const char* ssid = "Wifi_SSID"; //change
const char* password =  "Wifi_Password"; //change

const char* mqtt_server = ""; //change
const int mqtt_port = ; //change
const char* mqtt_user = ""; //change
const char* mqtt_pass = ""; //change
const char *mqtt_client_name = "";// Client connections cant have the same connection name
const char *mqtt_pub_topic = "AndroidRelaySwitch";
const char *mqtt_sub_topic = "ArduinoRelaySwitch";

const int relayPIN = D1;
WiFiClient espClient;
PubSubClient client(espClient, mqtt_server,  mqtt_port);  //instanciates client object
// the setup routine runs once when you press reset:
void setup() {                
  // initialize the digital pin as an output.
  pinMode(LED_BUILTIN, OUTPUT); 
  pinMode(relayPIN, OUTPUT);  
  digitalWrite(relayPIN, HIGH);
  digitalWrite(LED_BUILTIN, HIGH);
  Serial.begin(115200);
 
  setup_wifi();
  authenticateMQTT();

}

void setup_wifi() {

  delay(10);
  // We start by connecting to a WiFi network
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  randomSeed(micros());

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}


// the loop routine runs over and over again forever:
void loop() {
     if (client.connected())
          client.loop();
}

//Function is called when, a message is recieved in the MQTT server.
void callback(const MQTT::Publish& pub) {
    Serial.println("Callback method called");
    Serial.print(pub.topic());
    Serial.print(" => ");
   
    String flag = pub.payload_string();
    Serial.print(flag);
    if(flag=="on"){
      digitalWrite(relayPIN, LOW);
      digitalWrite(LED_BUILTIN, LOW);
      client.publish(MQTT::Publish(mqtt_pub_topic, "on").set_retain()
                .set_qos(1)
                .set_dup());
    }else if(flag == "off"){
      digitalWrite(relayPIN, HIGH);
      digitalWrite(LED_BUILTIN, HIGH);
      client.publish(MQTT::Publish(mqtt_pub_topic, "off").set_retain()
                .set_qos(1)
                .set_dup());
    }
}

void authenticateMQTT(){
     Serial.println("Authenticate to MQTT server....");
    //client object makes connection to server
    if (!client.connected()) {
        Serial.println("Connecting to MQTT server");
      //Authenticating the client object
      if (client.connect(MQTT::Connect(mqtt_client_name).set_auth(mqtt_user, mqtt_pass))) {
        Serial.println("Connected to MQTT server");
        //Subscribe code
        client.set_callback(callback);
        client.subscribe(MQTT::Subscribe()
                  .add_topic(mqtt_sub_topic));
      } else {
        Serial.println("Could not connect to MQTT server");   
      }
    }
}
