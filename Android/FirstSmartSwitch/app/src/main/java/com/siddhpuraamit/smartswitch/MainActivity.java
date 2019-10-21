package com.siddhpuraamit.smartswitch;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivityMyClass";
    TextView textView;
    MqttAndroidClient client;
    private String server = "";
    private String port = "";
    private String mServerUrl = "tcp://"+server+":"+port;
    private String mTopicSub = "AndroidRelaySwitch";
    private String mTopicPub = "ArduinoRelaySwitch";
    private String mMqttUserName = "";
    private String mMqttPassword = "";
    private ToggleButton toggleButton;
    private ImageView ivBulb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.tvInfo);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        ivBulb = (ImageView)findViewById(R.id.ivBulb);
        toggleButton.setText("ON");
        //getBatteryStatus();
        connect();

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    publish(client, "on");
                } else {
                    publish(client, "off");
                }
            }
        });
    }

    public void connect() {
        Log.d(TAG, "connect: ");
        String clientId = MqttClient.generateClientId();
        client =
                new MqttAndroidClient(this.getApplicationContext(), mServerUrl,
                        clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_DEFAULT);

        options.setCleanSession(false);
        options.setUserName(mMqttUserName);
        options.setPassword(mMqttPassword.toCharArray());
        try {
            final IMqttToken token = client.connect(options);
            //IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                    publish(client, mTopicPub);
                    subscribe(client, mTopicSub);
                    client.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable cause) {
                            showMessage("Connection Lost");
                            Log.d(TAG, "connectionLost: " + cause.getMessage());
                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) {
                            Log.d(TAG, message.toString());
                            switch(message.toString()){
                                case "on":
                                    ivBulb.setImageResource(R.drawable.bulb_on);
                                    textView.setText("Click below to OFF Switch");
                                    toggleButton.setText("OFF");
                                    //toggleButton.setChecked(true);
                                    break;
                                case "off":
                                    ivBulb.setImageResource(R.drawable.bulb_off);
                                    textView.setText("Click below to ON Switch");
                                    toggleButton.setText("ON");
                                    //toggleButton.setChecked(false);
                                    break;
                            }
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {
                            Log.d(TAG, "deliveryComplete: " + token.toString());
                        }
                    });
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");
                    showMessage("onFailure");

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void publish(MqttAndroidClient client, String payload) {
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(mTopicPub, message);
        } catch (Exception e) {
            showMessage(e.getMessage());
            e.printStackTrace();
        }
    }


    public void subscribe(MqttAndroidClient client, String topic) {
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    showMessage("Connection success");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    exception.printStackTrace();
                    showMessage(exception.getMessage());
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showMessage(e.getMessage());
        }
    }

    private void showMessage(final String message) {

        try {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}