package ece.bikeblindspotdetector;


import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.widget.Toast;
import java.io.InputStream;
import android.os.Handler;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 *
 */
public class TrafficViewerFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    Button alert_Left;
    Button alert_Center;
    Button alert_Right;

    Handler bluetoothIn;

    final int handlerState = 0;                        //used to identify handler message
    private BluetoothDevice device;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    private final String DEVICE_NAME="BlindSide";

    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_traffic_viewer, container, false);

        alert_Left = (Button) view.findViewById(R.id.alert_left);
        alert_Center = (Button) view.findViewById(R.id.alert_center);
        alert_Right = (Button) view.findViewById(R.id.alert_right);

        final String delimiters = "[#+~]";

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {                                     //if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);                                      //keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string

                        int dataLength = dataInPrint.length();                          //get length of data received

                        if (dataInPrint.charAt(0) == '#')                             //if it starts with # we know it is what we are looking for
                        {
                            String[] sweep = dataInPrint.split(delimiters);

                            String sector0 = sweep[0];
                            String sector1 = sweep[1];
                            String sector2 = sweep[2];
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
                        recDataString.delete(0, recDataString.length());                    //clear all string data
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

        return view;
    }

    /*
    The following 9 methods set each individual buttons to the respective color stated in the method
    name. These are used to change the screen output so that the user can see where traffic is in
    relation to themselves
     */

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //Gets set of currently paired devices
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

//Need to add in exception

        for (BluetoothDevice iterator : pairedDevices) {
            if(iterator.getName().equals(DEVICE_NAME)){
                device = iterator;
                break;
            }
        }

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getActivity().getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getActivity().getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
    }

    /*
    These 3 methods set the left button to a color
     */
    public void setLeftGreen()
    {
        alert_Left.setBackgroundColor(Color.GREEN);
    }
    public void setLeftYellow()
    {
        alert_Left.setBackgroundColor(Color.YELLOW);
    }
    public void setLeftRed()
    {
        alert_Left.setBackgroundColor(Color.RED);
    }

    /*
    These 3 methods set the center button to a color
     */
    public void setCenterGreen()
    {
        alert_Center.setBackgroundColor(Color.GREEN);
    }
    public void setCenterYellow()
    {
        alert_Center.setBackgroundColor(Color.YELLOW);
    }
    public void setCenterRed()
    {
        alert_Center.setBackgroundColor(Color.RED);
    }

    /*
    These 3 methods set the right button to a color
     */
    public void setRightGreen()
    {
        alert_Right.setBackgroundColor(Color.GREEN);
    }
    public void setRightYellow()
    {
        alert_Right.setBackgroundColor(Color.YELLOW);
    }
    public void setRightRed()
    {
        alert_Right.setBackgroundColor(Color.RED);
    }
}
