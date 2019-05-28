package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class BluetoothHandler extends Activity implements AdapterView.OnItemClickListener {

    private static final String TAG= "BluetoothHandler";

    private final static int REQUEST_ENABLE_BT = 0;
    private final static int REQUEST_DISCOVER_BT = 1;
    private final static UUID MY_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");

    private BluetoothAdapter mBluetoothAdapter ;
    private ArrayList<BluetoothDevice> mBLEDevices = new ArrayList<>();
    private BluetoothDevice selectedDevice;

    private ArrayList<String> deviceNames= new ArrayList<>();
    private ArrayList<String> HWaddresses= new ArrayList<>();
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCallback bluetoothGattCallback;

     ListView listDevice;
    private Switch switchEnable;
    Button btnSearch,btnPairing;
    private boolean isBonded = false;

    protected void onCreate(Bundle SavedInstance) {
        //set up the window
        super.onCreate(SavedInstance);
        setContentView(R.layout.bluetooth_finder);
        listDevice = findViewById(R.id.listDevice);
        switchEnable= findViewById(R.id.switchBluetooth);
        btnSearch= findViewById(R.id.btnSearch);
        btnPairing = findViewById(R.id.btnPair);
        mBluetoothAdapter =BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null){

            Log.d(TAG,"no bluetooth adapter");
        }
        if (mBluetoothAdapter.isEnabled()){
            switchEnable.setChecked(true);
            Log.d(TAG, "Bluetooth already enabled");
        }
        else{
            switchEnable.setChecked(false);
            Log.d(TAG, "Bluetooth not enabled");
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(broadcastReceiver,filter);

        listDevice.setOnItemClickListener(BluetoothHandler.this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(receiver);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState()== BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG,"BOND_BONDED");
                    isBonded= true;
                }
                if (device.getBondState()== BluetoothDevice.BOND_BONDING) Log.d(TAG,"BOND_BONDing");
                if (device.getBondState()== BluetoothDevice.BOND_NONE) Log.d(TAG,"BOND_None");
            }
        }
    };

    public void onEnableOnOff(View view) {

        Log.d(TAG, "switch activated");

        if (!switchEnable.isChecked()){
            Log.d(TAG,"Disabeling the bluetooth");
            mBluetoothAdapter.disable();
        }
        else{
            Log.d(TAG, "Starting bluetooth");
            Intent startBluetooth = new Intent(mBluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(startBluetooth,REQUEST_ENABLE_BT);
            Log.d(TAG, "Bluettoth started");
        }
    }

    public void btnDiscover(View view) {
        Log.d(TAG,"Starting discovery");

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }

        checkBTPermissions();
        mBluetoothAdapter.startDiscovery();
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver,intentFilter);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,  "broadcast Receving");
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();// MAC address
                mBLEDevices.add(device);
                if (deviceName==null) deviceName= "No name";
                deviceNames.add(deviceName);
                HWaddresses.add(deviceHardwareAddress);
                DeviceListAdapter blueAdapter = new DeviceListAdapter(context,R.layout.bluetooth_list,mBLEDevices);
                listDevice.setAdapter(blueAdapter);

            }
        }
    };

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mBluetoothAdapter.cancelDiscovery();
        String name = deviceNames.get(position);
        String address = HWaddresses.get(position);
        Log.d(TAG, "connecting to "+name+ ": "+address);
        if(Build.VERSION.SDK_INT> Build.VERSION_CODES.JELLY_BEAN_MR2){
            mBLEDevices.get(position).createBond();

            selectedDevice = mBLEDevices.get(position);
            ConnectThread connectThread = new ConnectThread(selectedDevice,this);
            connectThread.start();
        }
    }

    public void pairing(View view) {
        if (isBonded) {
            Intent returnedDevice = new Intent();
            returnedDevice.putExtra("device", selectedDevice);
            setResult(RESULT_OK, returnedDevice);
        }
        else setResult(RESULT_CANCELED);
        finish();
    }


    class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {
        private LayoutInflater mLayoutInflater;
        private ArrayList<BluetoothDevice> mDevices;
        private int  mViewResourceId;
        TextView lblAddress,lblName;

        public DeviceListAdapter(Context context, int tvResourceId, ArrayList<BluetoothDevice> devices){
            super(context, tvResourceId,devices);
            this.mDevices = devices;
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mViewResourceId = tvResourceId;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.bluetooth_list, null);
            lblName = view.findViewById(R.id.bluetoothName);
            lblAddress = view.findViewById(R.id.bluetoothAddress);
            lblName.setText(deviceNames.get(i));
            lblAddress.setText(HWaddresses.get(i));
            return view;

        }
    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private Context mContext;

        public ConnectThread(BluetoothDevice device, Context context) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;
            mContext = context;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter.cancelDiscovery();

            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            bluetoothGatt = selectedDevice.connectGatt(mContext,true,bluetoothGattCallback);

        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }



}

