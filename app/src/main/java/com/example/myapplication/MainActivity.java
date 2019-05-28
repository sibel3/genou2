package com.example.myapplication;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView lblAngle,lblSpeed,lblConnection;
    private static final int BLUETOOTH_REQUEST = 1;
    BluetoothDevice connectedDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lblAngle= (TextView) findViewById(R.id.lblAngle);
        lblAngle.setText("0");
        lblSpeed = findViewById(R.id.lblSpeed);
        lblSpeed.setText("0");
        Button btnBluetooth = findViewById(R.id.btnBluetooth);
        Button btnCamera= findViewById(R.id.btnCamera);
        lblConnection =findViewById(R.id.lblConnection);

    }

    public void onClickBle(View view) {
        Intent startBluetoothHandler = new Intent(this, BluetoothHandler.class);
        startActivityForResult(startBluetoothHandler,BLUETOOTH_REQUEST);
    }

    public void onCamera(View view) {
        Intent startCamera= new Intent(this,CameraHandler.class);
        startActivity(startCamera);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == BLUETOOTH_REQUEST){
            if (resultCode== RESULT_OK){
                connectedDevice = data.getParcelableExtra("device");
                lblConnection.setText(("connected to " + connectedDevice.getName() + ": "+connectedDevice.getAddress()));

            }

        }
    }
}
