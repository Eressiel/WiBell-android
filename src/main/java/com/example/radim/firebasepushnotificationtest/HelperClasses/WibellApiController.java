package com.example.radim.firebasepushnotificationtest.HelperClasses;

import android.app.Application;
import android.os.StrictMode;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Radim on 5. 9. 2016.
 */
public class WibellApiController extends Application {
    public static final String TAG = WibellApiController.class.getName();


    private RequestQueue mRequestQueue;
    private static WibellApiController mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mRequestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    public static synchronized WibellApiController getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public <T> void add(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancel() {
        mRequestQueue.cancelAll(TAG);
    }

}
