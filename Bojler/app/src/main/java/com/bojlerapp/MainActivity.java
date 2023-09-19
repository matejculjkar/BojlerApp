package com.bojlerapp;

//arduino sprejme sms v formatu: OFF:sssss;ON:_____;OFF:____;...............ON:____; tega ima 10 polj,to pomeni 5 ciklov
//časi so v sekundah, te sekunde arduino pretvori v array long z 10 mesti long[10], če imaš manj zahtevne SMSje
//ni problematično, samo pazi da vedno pošlješ v paru - torej OFF...ON...

import static android.os.Build.VERSION.SDK_INT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerTabStrip;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    static String phoneNo =  "ENTER PHONE NUMBER";

    String message = "TEST";

    String[] permissionsArray= {
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE};

    FragmentPagerAdapter adapterViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager vpPager = (ViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);



        checkPermission(permissionsArray);

        //Parametri a = new Parametri(50, "15:00");
        //System.out.println("Čas gretja:"+" "+Parametri.convertSecondsToString(a.calculateSecondsFromNowToStart("09:00")));








        /*TextView a = (TextView) findViewById(R.id.a);
        a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSMS(phoneNo, message);
            }
        });*/






    }

    public static void sendSMS(String phoneNo, String message) {
        //checkPermission(permissionsArray);
        SmsManager.getDefault().sendTextMessage(phoneNo, null, message, null,null);
    }

    public void checkPermission(String[] permissionsArray) {
        if (ContextCompat.checkSelfPermission(this, permissionsArray[0]) == PackageManager.PERMISSION_DENIED||
                ContextCompat.checkSelfPermission(this, permissionsArray[1]) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, permissionsArray, 100);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int i=0;i<permissions.length;i++){
            if (i==0) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("Odobreno"+permissions[i]);
                } else {
                    Toast.makeText(MainActivity.this, "Sent SMS Denied", Toast.LENGTH_SHORT).show();
                }
            }

            if (i==1) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("Odobreno"+permissions[i]);
                } else {
                    Toast.makeText(MainActivity.this, "Phone State Denied", Toast.LENGTH_SHORT).show();
                }
            }

/*
            if (i==2) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("Odobreno"+permissions[i]);
                } else {
                    Toast.makeText(MainActivity.this, "Write Storage Denied", Toast.LENGTH_SHORT).show();
                }
            }

            if (i==3) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("Odobreno"+permissions[i]);
                } else {
                    Toast.makeText(MainActivity.this, "Read Storage Denied", Toast.LENGTH_SHORT).show();
                }
            }*/
        }




    }}




 class MyPagerAdapter extends FragmentPagerAdapter {
    private static int NUM_ITEMS = 2;

    public MyPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: // Fragment # 0 - This will show FirstFragment
                return FirstFragment.newInstance(String.valueOf(0), "Status");
            case 1: // Fragment # 0 - This will show FirstFragment different title
                return SecondFragment.newInstance(String.valueOf(1), "Program Page");
            default:
                return null;
        }
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        String title=null;
        if(position==0){
            title="STATUS";
        }
        else if(position==1){
            title="PROGRAM PAGE";
        }

        return title;

    }

}
