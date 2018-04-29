package com.android.blindside;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.widget.AdapterView;
import java.util.Set;
import android.bluetooth.BluetoothDevice;
import android.widget.ListView;


import android.widget.AdapterView.OnItemClickListener;

public class BluetoothConnectionActivity extends Activity {

    // This is so that the main activity can get the the address of the device being used
    public static String EXTRA_ADDRESS = "deviceAddress";

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mPairedArrayAdapter;

    // Button buttonDevice;
    TextView textDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connection);
    }


    @Override
    public void onResume() {
        super.onResume();

        checkBluetooth();

        textDevice = findViewById(R.id.connection);
        // textDevice.setTextSize(40);
        textDevice.setText(" ");

        mPairedArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);

        ListView pairedList = findViewById(R.id.paired_devices);
        pairedList.setAdapter(mPairedArrayAdapter);
        pairedList.setOnItemClickListener(mClickListener);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if(pairedDevices.size() > 0) {
            findViewById(R.id.title).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mPairedArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = "No devices paired!";
            mPairedArrayAdapter.add(noDevices);
        }


    }

    private OnItemClickListener mClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            textDevice.setText("Connecting to Device . . .");

            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17); // use 17 here because it's length of device address

            Intent nextIntent = new Intent(BluetoothConnectionActivity.this, MainActivity.class);
            nextIntent.putExtra(EXTRA_ADDRESS, address);
            startActivity(nextIntent);
        }
    };


    private void checkBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            Toast.makeText(getBaseContext(), "Bluetooth error", Toast.LENGTH_SHORT).show();
        } else {
            if (mBluetoothAdapter.isEnabled()) {

            } else {
                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetoothIntent, 1);
            }
        }
    }
}
