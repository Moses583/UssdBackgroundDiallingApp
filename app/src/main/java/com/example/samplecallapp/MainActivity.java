package com.example.samplecallapp;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Button button;
    private Spinner spinner;
    private Map<Integer, Integer> simMap;
    private ArrayList<String> simNames;
    private ArrayList<Integer> slotIndex;
    private EditText editText;
    String[] permissions = new String[]{
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,

    };
    TelephonyManager manager;
    TelephonyManager.UssdResponseCallback callback;
    Handler handler;
    DBHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        helper = new DBHelper(this);


        simMap = new HashMap<>();
        simNames = new ArrayList<>();
        slotIndex = new ArrayList<>();

        button = findViewById(R.id.callNumber);
        spinner = findViewById(R.id.selectSim);
        editText = findViewById(R.id.getUssd);



//        listSimInfo();
        manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        findViewById(R.id.checkTransactions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ResponsesActivity.class));
            }
        });


        findViewById(R.id.executeFailed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myService()) {
                    // If the service is running, stop it
                    Intent intent = new Intent(MainActivity.this, MyService.class);
                    stopService(intent);
                } else {
                    // If the service is not running, start it
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Intent intent = new Intent(MainActivity.this, MyService.class);
                        startForegroundService(intent);
                    }
                }
            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 101);
                    return;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String dialSim1;
                    int subscriptionId = 0;
                    dialSim1 = spinner.getSelectedItem().toString();
                    if (dialSim1.contains("SIM: 1")) {
                        subscriptionId = simMap.get(0);
                    } else if (dialSim1.contains("SIM: 2")) {
                        subscriptionId = simMap.get(1);
                    }
                    String ussdRequest = editText.getText().toString();
                    dialUssdCode(ussdRequest,subscriptionId);
                }
            }
        });

        findViewById(R.id.requestPermissions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasLocationPermission()){
                    listSimInfo();
                }else{
                    if(shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE)){
                        showRationaleDialog();
                    }else{
                        requestMultiple.launch(new String[]{Manifest.permission.CALL_PHONE,Manifest.permission.ACCESS_COARSE_LOCATION});
                    }
                }
            }
        });

    }
    private void showRationaleDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissions needed!")
                .setMessage("All the permissions requested are needed for the app to function")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestMultiple.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION});
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private ActivityResultLauncher<String> request = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean o) {
            if(o){
                listSimInfo();
            }else{
                Toast.makeText(MainActivity.this, "permission needed", Toast.LENGTH_SHORT).show();
            }
        }
    });
    private ActivityResultLauncher<String[]> requestMultiple = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> o) {
            boolean allGranted = true;
            for (String key: o.keySet()){
                allGranted = allGranted && o.get(key);
            }
            if(allGranted){
                Toast.makeText(MainActivity.this, "permissions granted", Toast.LENGTH_SHORT).show();
                listSimInfo();
            }else{
                Toast.makeText(MainActivity.this, "permissions needed", Toast.LENGTH_SHORT).show();
            }

        }
    });

    private boolean hasCallPermission(){
        return checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
    }
    private boolean hasLocationPermission(){
        return checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }



    private void dialUssdCode(String ussdRequest, int subscriptionId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            callback = new TelephonyManager.UssdResponseCallback() {
                @Override
                public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                    super.onReceiveUssdResponse(telephonyManager, request, response);
                    Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                    insertResponse(response.toString(),"*100*4*1*1#",subscriptionId);
                }

                @Override
                public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {
                    super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode);
                    insertResponse("Failed with the following error: "+String.valueOf(failureCode),"*100*4*1*1#",subscriptionId);
                }
            };
            handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                }
            };
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createForSubscriptionId(subscriptionId).sendUssdRequest(ussdRequest, callback, handler);
        }
    }

    public void insertResponse(String response,String ussdCode, int subscriptionId){
        boolean checkInsertData = helper.insertResponse(response, ussdCode, subscriptionId);
        if (checkInsertData){
            Toast.makeText(this, "response added successfully", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Insertion unsuccessful", Toast.LENGTH_SHORT).show();
        }
    }

    public void listSimInfo(){
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE},101);
            return;
        }
        List<SubscriptionInfo> infoList = SubscriptionManager.from(MainActivity.this).getActiveSubscriptionInfoList();
        for (SubscriptionInfo info:
                infoList) {
            simMap.put(info.getSimSlotIndex(),info.getSubscriptionId());
            String slot = String.valueOf(info.getSimSlotIndex()+1);
            simNames.add(info.getCarrierName().toString()+" SIM: "+slot);
            slotIndex.add(info.getSimSlotIndex());
        }
        ArrayAdapter adapter1 = new ArrayAdapter(MainActivity.this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,simNames);
        spinner.setAdapter(adapter1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                listSimInfo();
            }
        }
    }

    public boolean myService(){
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo info :
                manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MyService.class.getName().equalsIgnoreCase(info.service.getClassName())){
                return true;
            }
        }
        return false;
    }


}