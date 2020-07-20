package com.pjs.pjs_sos;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.app.ActivityManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.util.Log;

import com.pjs.pjs_sos.API.EndPoint;
import com.pjs.pjs_sos.Data.Response.ResponseLogin;
import com.pjs.pjs_sos.Database.SharedPrefManager;

import java.util.List;

import androidx.core.app.ActivityCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST = 112;
    Intent mServiceIntent;
    private AppServices mSensorService;

    private EditText editTextUsername, editTextPassword;
    private CardView buttonLogin;
    private ActionBar actionBar;
    private ProgressBar progressBar;
    private CheckBox checkBoxLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//    inisiasi yang ada di xml
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        progressBar = findViewById(R.id.progressBar);
        checkBoxLogin = findViewById(R.id.checkBoxLogin);

        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
            if (!hasPermissions(MainActivity.this, PERMISSIONS)) {
                ActivityCompat.requestPermissions((Activity) MainActivity.this, PERMISSIONS, REQUEST);
            } else {
                runservices();
            }
        } else {
            runservices();
        }
        //addAutoStartup();

        //sembunyikan action bar biar rapih
        hideActionBar();

        // check sharedpref pada session sebelumnya
        checkLogin();

        //aksi tombol login ketika di klik btn login muncul progressbar terus manggil function login yang mana function login isi nya validasi input
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonLogin.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                userLogin();
            }
        });
        // ubah data login
        checkBoxLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SharedPrefManager.getInstance(MainActivity.this).saveState(isChecked);
            }
        });
    }




    // check login pada checkbox sebelummnya
    private void checkLogin() {
        Boolean loggedIn = SharedPrefManager.getInstance(this).getState();

        if (loggedIn) {
            Intent intent = new Intent(MainActivity.this,SOSActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }


    // validate input
    private void userLogin() {
        String Officer_ID = editTextUsername.getText().toString().trim();
        String PIN = editTextPassword.getText().toString().trim();

        if (Officer_ID.isEmpty()) {
            failLogin();
            editTextUsername.setError("Masukkan Username!");
            editTextUsername.requestFocus();
            return;
        }

        if (PIN.isEmpty()) {
            failLogin();
            editTextPassword.setError("Masukkan Password!");
            editTextPassword.requestFocus();
            return;
        }

//        Toast.makeText(getApplicationContext(), "Your officer_id : " + Officer_ID + " and your pin : " + PIN, Toast.LENGTH_SHORT).show();

        // login using API call
        Call<ResponseLogin> call = EndPoint
                .getInstance()
                .getApi()
                .login(Officer_ID, PIN);

        call.enqueue(new Callback<ResponseLogin>() {
            @Override
            public void onResponse(Call<ResponseLogin> call, Response<ResponseLogin> response) {
                ResponseLogin responseLogin = response.body();

                // kalau authenticated == TRUE
                if (responseLogin.isAuthenticated())
                {
                    // save user (response dari JSON) ke sharedPrefManager
                    SharedPrefManager
                            .getInstance(MainActivity.this)
                            .saveDataUser(responseLogin.getUserData());

                    // kemudian buka activity baru dan tutup yang login
                    Intent intent = new Intent(MainActivity.this, SOSActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    Toast.makeText(MainActivity.this, "Selamat Datang, " + responseLogin.getUserData().Officer_ID(), Toast.LENGTH_LONG).show();
                }
                else {
                    failLogin();
                    Toast.makeText(MainActivity.this, responseLogin.getMessage(), Toast.LENGTH_LONG).show();
                }
            }


            @Override
            public void onFailure(Call<ResponseLogin> call, Throwable t) {
                failLogin();
                Toast.makeText(MainActivity.this, "gagal!" + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

//    action bar
    private void hideActionBar() {
        actionBar = getSupportActionBar();

        // Hide ActionBar
        if (actionBar != null) {
            actionBar.hide();
        }
    }
// kalo login gagal
    private void failLogin(){
        progressBar.setVisibility(View.GONE);
        buttonLogin.setVisibility(View.VISIBLE);
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //startService(new Intent(this, AppServices.class));
                    runservices();
                } else {
                    Toast.makeText(MainActivity.this, "The app was not allowed to access your location", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.d ("APPLOG", true+"");
                return true;
            }
        }
        Log.d ("APPLOG", false+"");
        return false;
    }

    public void runservices()
    {
        mSensorService = new AppServices();
        mServiceIntent = new Intent(getApplicationContext(), mSensorService.getClass());
        if (!isMyServiceRunning(mSensorService.getClass())) {
            startService(mServiceIntent);
        }
    }

    private void addAutoStartup() {

        try {
            Intent intent = new Intent();
            String manufacturer = android.os.Build.MANUFACTURER;
            if ("xiaomi".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            } else if ("oppo".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
            } else if ("vivo".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
            } else if ("Letv".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"));
            } else if ("Honor".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
            }

            List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if  (list.size() > 0) {
                startActivity(intent);
            }
        } catch (Exception e) {
            Log.e("APPLOG" , String.valueOf(e));
        }


    }
}
