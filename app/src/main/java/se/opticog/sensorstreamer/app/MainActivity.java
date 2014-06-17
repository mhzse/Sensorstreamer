package se.opticog.sensorstreamer.app;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Handler;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;


public class MainActivity extends Activity implements SensorEventListener {
    private ServerSocket serverSocket;
    Handler updateConversationHandler;
    Thread serverThread = null;
    public static final int SERVERPORT = 6000;
    private TextView clientMsgText;
    private PrintWriter output;

    private SensorManager mSensorManager;
    private Sensor acc;
    private TextView accTextValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InetAddress ownIP;
        TextView ipAddressText = (TextView) findViewById(R.id.ipAddressText);
        accTextValues = (TextView) findViewById(R.id.accText);
        //ipAddressText.setText("fdafa");
        //System.out.println("fdsafa");

        updateConversationHandler = new Handler();
        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();
        clientMsgText = (TextView) findViewById(R.id.clientText);


        Log.w("myApp", "test log !!!");
        Log.d("myApp", "lfgdks");


        String IP;
        WifiManager wim = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> wifiConfigs = wim.getConfiguredNetworks();

        if (wifiConfigs != null && wifiConfigs.size() > 0) {
            WifiConfiguration wc = wifiConfigs.get(0);
            IP = Formatter.formatIpAddress(wim.getConnectionInfo().getIpAddress());
            ipAddressText.setText("IP: " + IP);
        } else {
            ipAddressText.setText("No network!");
        }


        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        acc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        String allSensors = "";
        EditText listOfSensors = (EditText) findViewById(R.id.sensorList);
        for (int i = 0; i < deviceSensors.size(); i++) {
            allSensors += deviceSensors.get(i).getName() + "\n";
        }
        listOfSensors.setText(allSensors);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // The light sensor returns a single value.
        // Many sensors return 3 values, one for each axis.
        //float lux = event.values[0];
        // Do something with this sensor value.

        /*
        if( output != null )
        {
            output.write(Float.toString(event.values[0]) + " " + Float.toString(event.values[1]) + " " + Float.toString(event.values[2]));
        }
        */
        accTextValues.setText(Float.toString(event.values[0]) + " " + Float.toString(event.values[1]) + " " + Float.toString(event.values[2]));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    class ServerThread implements Runnable
    {

        public void run()
        {
            Socket socket = null;
            try
            {
                serverSocket = new ServerSocket(SERVERPORT);


            } catch (IOException e)
            {
                e.printStackTrace();
            }

            while (!Thread.currentThread().isInterrupted())
            {
                try
                {
                    socket = serverSocket.accept();

                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }

        }
    }


    class CommunicationThread implements Runnable
    {
        private Socket clientSocket;
        private BufferedReader input;
        private PrintWriter output;

        public CommunicationThread(Socket clientSocket)
        {
            this.clientSocket = clientSocket;
            try
            {
                //this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
                this.output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())),true);

            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        public void run()
        {
            while (!Thread.currentThread().isInterrupted())
            {
                /*
                try
                {
                    Thread.sleep(500);
                    output.write("hi from server!");
                }
                catch( InterruptedException e )
                {
                    e.printStackTrace();
                }
                */
                output.write("hi from server!");
                /*
                try
                {
                    String read = input.readLine();

                    updateConversationHandler.post(new updateUIThread(read));


                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                */
            }
        }
    }

    class updateUIThread implements Runnable
    {
        private String msg;

        public updateUIThread(String str)
        {
            this.msg = str;
        }

        @Override
        public void run()
        {
            clientMsgText.setText("Client: "+ msg + "\n");
        }
    }
}

