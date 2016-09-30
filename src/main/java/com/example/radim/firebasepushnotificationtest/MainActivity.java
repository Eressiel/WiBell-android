package com.example.radim.firebasepushnotificationtest;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.radim.firebasepushnotificationtest.HelperClasses.CustomJsonRequest;
import com.example.radim.firebasepushnotificationtest.HelperClasses.WibellApiController;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.util.HashMap;

import butterknife.Bind;

public class MainActivity extends AppCompatActivity {
    private static final String FIREBASETOKEN = "FirebaseToken";
    private static final String FIREBASETOKENSTATUS = "FirebaseTokenStatus";
    private static final String WIBELLID = "WibellID";
    private static final String BASEPATH = "wibell.jecool.net/public/v1/";
    private WibellApiController apiController = WibellApiController.getInstance();

    private String firebaseToken;
    private String wibellID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseMessaging.getInstance().subscribeToTopic("test");
        firebaseToken = FirebaseInstanceId.getInstance().getToken();
        saveFirebaseToken(firebaseToken);

        Boolean logged = getInfoFromPreferences();
        if(!logged){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            syncAddedWibells();
        }
    }

    private void saveFirebaseToken(String firebaseToken){
        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(FIREBASETOKEN, firebaseToken);
        editor.commit();
    }

    protected Boolean getInfoFromPreferences(){
        TextView WBStatus = (TextView)findViewById(R.id.WibellStatus);
        TextView WBID = (TextView) findViewById(R.id.AddedWibellID);

        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String status = prefs.getString(FIREBASETOKENSTATUS, "inactive");
        wibellID = prefs.getString(WIBELLID, "");

        WBStatus.setText(status);
        WBID.setText(wibellID);
        return prefs.getBoolean("logged", false);
    }

    public void addWibell(View view) {
        Button _addWibellButton = (Button)findViewById(R.id.btnAddWibell);
        _addWibellButton.setEnabled(false);
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Adding WiBell...");
        progressDialog.show();

        final EditText wibellIDToAdd = (EditText)findViewById(R.id.WibellID);
        SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("firebasetoken", firebaseToken);
        params.put("wibell_ID", wibellIDToAdd.getText().toString());
        //params.put("email", prefs.getString("email", "null"));
        CustomJsonRequest request = new CustomJsonRequest
                (Request.Method.POST, BASEPATH + "devicetoring", new JSONObject(params), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        TextView WBStatus = (TextView)findViewById(R.id.WibellStatus);
                        TextView WBID = (TextView) findViewById(R.id.AddedWibellID);
                        try {
                            WBStatus.setText(response.getString("status"));
                            WBID.setText(wibellIDToAdd.getText().toString());

                            Log.d("MAIN ACTIVITY:", "onResponse");
                        } catch (Exception e) {
                            WBStatus.setText("Error (inactive)");
                            WBID.setText("");
                            Log.d("MAIN ACTIVITY:", "EXCEPTION");
                        }
                        progressDialog.dismiss();
                        Button _addWibellButton = (Button)findViewById(R.id.btnAddWibell);
                        _addWibellButton.setEnabled(true);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getBaseContext(), error.getMessage() + error.getCause(), Toast.LENGTH_LONG).show();
                        Log.d("MAIN ACTIVITY:", "onErrorResponse : " + error.getMessage() + error.getCause());
                        progressDialog.dismiss();
                        Button _addWibellButton = (Button)findViewById(R.id.btnAddWibell);
                        _addWibellButton.setEnabled(true);                    }
                });
        apiController.add(request);
    }

    public void syncAddedWibells(){
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Syncing data...");
        progressDialog.show();

        //SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        CustomJsonRequest request = new CustomJsonRequest
                (Request.Method.GET, BASEPATH + "devicetoring/" + firebaseToken, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        TextView WBStatus = (TextView)findViewById(R.id.WibellStatus);
                        TextView WBID = (TextView) findViewById(R.id.AddedWibellID);
                        try {
                            WBStatus.setText(response.getString("status"));
                            WBID.setText(response.getString("wibell_ID"));


                        } catch (Exception e) {
                            WBStatus.setText("Error (inactive)");
                            WBID.setText("");
                            Log.d("MAIN ACTIVITY:", "EXCEPTION:" + e.getMessage());
                        }
                        progressDialog.dismiss();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getBaseContext(), error.getMessage() + error.getCause(), Toast.LENGTH_LONG).show();
                        Log.d("MAIN ACTIVITY:", "onErrorResponse, Message: " + error.getMessage() + ", "+ error.getCause());
                        progressDialog.dismiss();
                    }
                });
        apiController.add(request);
    }

    @Override
    protected void onStop(){
        super.onStop();
        apiController.cancel();
    }
}
