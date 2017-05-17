package com.bhavya.solution.airtimeChallenge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.bhavya.solution.airtimeChallenge.commandservice.CommandService;

/**
 *
 *Main activity that contains Button which when clicked binds to CommandService(IntentService)
 *
 */
public class  MainActivity extends AppCompatActivity {

    Button button;
    MyReceiver mReceiver;
    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * called when activity is being created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onCLick of Button and starting Service");
                Intent intent = new Intent(MainActivity.this, CommandService.class);
                startService(intent);
            }
        });
        mReceiver = new MyReceiver();
    }

    /**
     * onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver,
                new IntentFilter("com.intent.airtime.CHALLENGE_COMPLETED_ACTION"));

    }

    /**
     * onDestroy()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    /**
     * MyRECEIVER that receives an intent when msg is
     * succesfully posted to the SERVER
     */
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received intent");
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    context);
            alertDialogBuilder.setTitle("AIRTIME Coding Challenge");
            alertDialogBuilder
                    .setMessage("posted result to http://challenge2.airtime.com:10001/")
                    .setCancelable(true)
                    .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            MainActivity.this.finish();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }
}

