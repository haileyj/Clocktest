package com.jimoffice.omnisecondstestcalendar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import com.jimoffice.omniclocktest.R;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {


    long returnTime = 0; // Declare global variable returnTime

    // This method is used to exit the application and the timer when the Exit Button is pressed
    public void exitButton(View view) {
        System.exit(0); // Stops the AWT thread (and everything else)
    }

    // This subclass gets NTP network time and then starts the timer thread
    // The Timer is set immediately and is executed every 10 msec.
    public class SetUpNTPTimer extends Thread {
        public void run() {
            System.out.println("Setup timers and NTP time");
            Timer timer1 = new Timer();  // timer1: used for the 10ms sample clock & NTP time update

            // Start timer immediately and sample every 10msec
            timer1.scheduleAtFixedRate(new RemindTask(), 0, 10);

            final String TIME_SERVER = "time-a.nist.gov"; // NTP server

            // Get NTP time and store in global variable returntime
            // returnTime is the number of milliseconds from Jan 1, 1970
            NTPUDPClient timeClient = new NTPUDPClient();
            InetAddress inetAddress = null;
            try {
                inetAddress = InetAddress.getByName(TIME_SERVER);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            TimeInfo timeInfo = null;
            try {
                timeInfo = timeClient.getTime(inetAddress);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (timeInfo != null) {
                returnTime = timeInfo.getReturnTime();
            }
            /*
            SimpleDateFormat sdf3 = new SimpleDateFormat("hh:mm:ss:SSS aa MM/dd/yyyy GG z"); // Display Hour Minutes Secs
            String testit = sdf3.format(returnTime);
            System.out.println("Test Point 1");
            System.out.println("Server: " + TIME_SERVER);
            System.out.println("timeClient: " + timeClient);
            System.out.println("inetAddress: " + inetAddress);
            System.out.println("timeInfo: " + timeInfo);
            System.out.println("returnTime: " + returnTime);
            System.out.println("formatted returnTime: " + testit);
            */
        }
    }

    // Called when Timer triggers to update sample clock and NTP clock (every 10 msec)
    class RemindTask extends TimerTask {

        public void run() {
            returnTime += 10;
        }
    }

    // This subclass sets up a timer that starts a thread to update the OSD
    // The Timer is set immediately and is executed every 50msec.
    public class DisplayAllUI extends Thread {
        public void run() {
            System.out.println("Setup Timer for display output");
            Timer timer2 = new Timer();  // timer1: used for the 50ms display clock

            // Start display timer immediately and sample every 50msec
            timer2.scheduleAtFixedRate(new RemindTask2(), 0, 50);
        }
    }

    // Called when Timer triggers to update OSD (every 50 msec)
    class RemindTask2 extends TimerTask {

        public void run() {
            runOnUiThread(new Runnable() {

                public void run() {
                    updateOSD();
                }
            });
        }

    }

    // Method which updates the OSD
    public void updateOSD() {

        /* Set date format for the android display
         * sdf displays only seconds and 1 digit milliseconds
         * sdf2 displays Hour Minutes Seconds
        */
        SimpleDateFormat sdf = new SimpleDateFormat("ss:S");
        SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm:ss"); // Display Hour Minutes Secs
        SimpleDateFormat sdf4 = new SimpleDateFormat("ss:SSS"); // Display Hour Minutes Secs


        // Display NTP seconds
        TextView newText = (TextView) findViewById(R.id.displayChar);
        if (newText != null) {
            newText.setText(String.valueOf(sdf.format(returnTime))); // Display NTP seconds
        }

        // Display NTP Hours Minutes Seconds
        TextView ntpText = (TextView) findViewById(R.id.ntpClock);
        if (ntpText != null) {
            ntpText.setText(String.valueOf(sdf2.format(returnTime))); // Display NTP Time
        }

        // Display System seconds
        Calendar cal2 = Calendar.getInstance();
        TextView newText2 = (TextView) findViewById(R.id.displayChar2);
        if (newText2 != null) {
            newText2.setText(String.valueOf(sdf.format(cal2.getTime()))); // Display System Secs
        }

        // Display System Hours Minutes Seconds
        TextView systemText = (TextView) findViewById(R.id.systemClock); // Setup the TextView
        if (systemText != null) {
            systemText.setText(String.valueOf(sdf2.format(cal2.getTime()))); // Display Sys Time
        }

        Date date = new Date();
        long epoch = date.getTime();
        // System.out.println("cal2: " + epoch); // Test Code
        // System.out.println("returnTime: " + returnTime); // Test Code

        long substractDiff = epoch - returnTime;
        // System.out.println("Diff: " + substractDiff); // Test Code

        TextView diffTime = (TextView) findViewById(R.id.clockDiff); // Setup the TextView
        if (diffTime != null) {
            diffTime.setText(String.valueOf(sdf4.format(Math.abs(substractDiff)))); // Display Sys Time
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup thread for NTP Time and 10sec Timer
        MainActivity.SetUpNTPTimer setUpNTPTimer = new MainActivity.SetUpNTPTimer();
        setUpNTPTimer.start();

        // Setup thread for OSD Update Timer
        MainActivity.DisplayAllUI displayAllUI = new MainActivity.DisplayAllUI();
        displayAllUI.start();

        // System.exit(0);

    }
}
