// SmsRetrieverModule.java
package com.otpdetectorrn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class SmsRetrieverModule extends ReactContextBaseJavaModule {

    private static final String MODULE_NAME = "SmsRetrieverModule";
    private static final String EVENT_SMS_RETRIEVED = "SmsRetrieved";

    private ReactApplicationContext reactContext;

    public SmsRetrieverModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @NonNull
    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @ReactMethod
    public void startSmsRetrieval() {
        SmsRetrieverClient client = SmsRetriever.getClient(reactContext);
        client.startSmsRetriever().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // SMS retrieval has started successfully
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle failure
            }
        });
    }

    // Register a broadcast receiver to listen for the SMS retrieval event
    private void registerReceiver() {
        SmsRetrieverReceiver receiver = new SmsRetrieverReceiver();
        IntentFilter intentFilter = new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);
        reactContext.registerReceiver(receiver, intentFilter);
    }

    // Send the retrieved SMS to the JavaScript side
    private void sendEvent(String message) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(EVENT_SMS_RETRIEVED, message);
    }

    // BroadcastReceiver to handle SMS retrieval events
    private class SmsRetrieverReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
                    if (status != null) {
                        switch (status.getStatusCode()) {
                            case CommonStatusCodes.SUCCESS:
                                // SMS retrieval success
                                String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                                sendEvent(message);
                                break;
                            case CommonStatusCodes.TIMEOUT:
                                // SMS retrieval timed out
                                break;
                        }
                    }
                }
            }
        }
    }
}
