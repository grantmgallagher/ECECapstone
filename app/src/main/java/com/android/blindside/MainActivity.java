package com.android.blindside;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.graphics.Color;
import android.os.Handler;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import java.util.UUID;
import android.content.Intent;
import android.widget.Toast;
import java.io.IOException;
import android.bluetooth.BluetoothDevice;
import android.view.View;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends Activity {

    Button alertLeft, alertCenter, alertRight;

    Handler bluetoothIncoming;

    final int handlerState = 0;
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket bluetoothSocket = null;
    private StringBuilder receivedData = new StringBuilder();


    private ConnectedThread mConnectionThread;

    private static final UUID BLUETOOTHMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alertLeft = findViewById(R.id.alert_left);
        alertCenter = findViewById(R.id.alert_center);
        alertRight = findViewById(R.id.alert_right);

        final String delimiters = "[#+~]";

        bluetoothIncoming = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {                                     //if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    receivedData.append(readMessage);                                      //keep appending to string until ~
                    int endOfLineIndex = receivedData.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = receivedData.substring(0, endOfLineIndex);    // extract string

                        int dataLength = dataInPrint.length();                          //get length of data received

                        if (dataInPrint.charAt(0) == '#')                             //if it starts with # we know it is what we are looking for
                        {
                            String[] sweep = dataInPrint.split(delimiters);

                            String sector0 = sweep[1];
                            String sector1 = sweep[2];
                            String sector2 = sweep[3];
                            //this is for battery percentage
                            //String battery = sweep[3];

                            //this is for the LEFT Side view or right side of sweep
                            //GREEN First
                            if(Integer.parseInt(sector2)>=25000 || Integer.parseInt(sector2)<50){
                                setLeftGreen();
                                //now YELLOW
                            } else if (Integer.parseInt(sector2)<25000 && Integer.parseInt(sector2)>=10000){
                                setLeftYellow();
                                //finally RED
                            } else if (Integer.parseInt(sector2)<10000 && Integer.parseInt(sector2)>=50){
                                setLeftRed();
                            }

                            //this is for the MIDDLE view or center of sweep
                            //GREEN First
                            if(Integer.parseInt(sector1)>=25000 || Integer.parseInt(sector1)<50){
                                setCenterGreen();
                                //now YELLOW
                            } else if (Integer.parseInt(sector1)<25000 && Integer.parseInt(sector1)>=10000){
                                setCenterYellow();
                                //finally RED
                            } else if (Integer.parseInt(sector1)<10000 && Integer.parseInt(sector1)>=50){
                                setCenterRed();
                            }

                            //this is for the RIGHT Side view or left side of sweep
                            //GREEN First
                            if(Integer.parseInt(sector0)>=25000 || Integer.parseInt(sector0)<50){
                                setRightGreen();
                                //now YELLOW
                            } else if (Integer.parseInt(sector0)<25000 && Integer.parseInt(sector0)>=10000){
                                setRightYellow();
                                //finally RED
                            } else if (Integer.parseInt(sector0)<10000 && Integer.parseInt(sector0)>=50){
                                setRightRed();
                            }
                        }
                        receivedData.delete(0, receivedData.length());                    //clear all string data
                        dataInPrint = " ";
                    }
                }
            }
        };

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBluetooth();
    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException{
        return device.createRfcommSocketToServiceRecord(BLUETOOTHMODULEUUID);
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getIntent();

        address = intent.getStringExtra(BluetoothConnectionActivity.EXTRA_ADDRESS);

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

        try {
            bluetoothSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }

        try
        {
            bluetoothSocket.connect();
        } catch (IOException e) {
            try
            {
                bluetoothSocket.close();
            } catch (IOException e2) {

            }
        }

        mConnectionThread = new ConnectedThread(bluetoothSocket);
        mConnectionThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            bluetoothSocket.close();
        } catch (IOException e2) {

        }
    }
    private void checkBluetooth() {
        if(bluetoothAdapter == null) {
            Toast.makeText(getBaseContext(), "Bluetooth error", Toast.LENGTH_SHORT).show();
        } else {
            if (bluetoothAdapter.isEnabled()) {

            } else {
                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetoothIntent, 1);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                //Create I/O streams for connection
                tempIn = socket.getInputStream();
                tempOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tempIn;
            mmOutStream = tempOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);

                    bluetoothIncoming.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
    }



    // These 3 methods set the left button to a color
    public void setLeftGreen() {
        alertLeft.setBackgroundColor(Color.GREEN);
    }
    public void setLeftYellow() {
        alertLeft.setBackgroundColor(Color.YELLOW);
    }
    public void setLeftRed() {
        alertLeft.setBackgroundColor(Color.RED);
    }


    // These 3 methods set the center button to a color
    public void setCenterGreen() {
        alertCenter.setBackgroundColor(Color.GREEN);
    }
    public void setCenterYellow() {
        alertCenter.setBackgroundColor(Color.YELLOW);
    }
    public void setCenterRed() {
        alertCenter.setBackgroundColor(Color.RED);
    }


    // These 3 methods set the right button to a color
    public void setRightGreen() {
        alertRight.setBackgroundColor(Color.GREEN);
    }
    public void setRightYellow() {
        alertRight.setBackgroundColor(Color.YELLOW);
    }
    public void setRightRed() {
        alertRight.setBackgroundColor(Color.RED);
    }
}
