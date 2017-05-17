package com.bhavya.solution.airtimeChallenge.commandservice;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;

/**
 * Created by bhavya.narra on 11/5/2016.
 */


public class CommandService extends IntentService {

    private Context mContext;
    private CommandServiceStub mStub;
    private static final String TAG = "AirTime-Service";

    public CommandService() {
        super("CommandService");

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate of IntentService");
        mContext = getApplicationContext();
        mStub = new CommandServiceStub(mContext);
    }

    /**
     * This method is called when
     * an application binds to this service
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d(TAG, "calling OnHandleIntent and action is: "+ intent.getAction());
            final String action = intent.getAction();
                try {
                    mStub.getDrones();
                } catch(JSONException ex1) {
                    ex1.printStackTrace();
                } catch (IOException ex2) {
                    ex2.printStackTrace();
                }
        }
    }

   @Override
    public void onDestroy(){
        super.onDestroy();
    }
}
