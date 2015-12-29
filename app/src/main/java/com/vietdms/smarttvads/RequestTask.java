package com.vietdms.smarttvads;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by ${LTG} on ${10/12/1994}.
 */
public class RequestTask extends AsyncTask<Void, Void, Boolean> {
    Exception error;
    private SQLiteDatabase database;
    private String deviceID;
    private String data;
    private ProgressDialog mProgressDialog;
    private JSONArray arrAds;

    public RequestTask(SQLiteDatabase database, String deviceID, ProgressDialog mProgressDialog) {
        this.database = database;
        this.deviceID = deviceID;
        this.mProgressDialog = mProgressDialog;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            if (!data.equals("{\"Table\":[]}")) {
                try {
                    parseData();// get companycode

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
            }
        } else {
            if (error != null) {
            }
        }
        mProgressDialog.dismiss();
    }

    private void parseData() throws JSONException {
        database.delete(MyMethod.TABLESHOW, null, null);
        JSONObject jsonObject = new JSONObject(data);
        arrAds = jsonObject.getJSONArray("Table");
        int nAds = arrAds.length();
        for (int i = 0; i < nAds; i++) {
            ContentValues cv = new ContentValues();
            cv.put(MyMethod.ColumnSTT, Integer.parseInt(arrAds.getJSONObject(i).getString("LineID")));
            cv.put(MyMethod.ColumnTYPE, Integer.parseInt(arrAds.getJSONObject(i).getString("Type")));
            cv.put(MyMethod.ColumnURL, arrAds.getJSONObject(i).getString("Url"));
            cv.put(MyMethod.ColumnBACKUPURL, arrAds.getJSONObject(i).getString("BackupUrl"));
            cv.put(MyMethod.ColumnSTARTDATE, arrAds.getJSONObject(i).getString("StartTime"));
            cv.put(MyMethod.ColumnDURATION, Float.parseFloat(arrAds.getJSONObject(i).getString("DurationTime")));
            cv.put(MyMethod.ColumnVOLUME, Float.parseFloat(arrAds.getJSONObject(i).getString("Volume")));
            database.insert(MyMethod.TABLESHOW, null, cv);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setMessage("Hoàn thành ");
    }


    @Override
    protected Boolean doInBackground(Void... params) {

        try {

            requestDevice(deviceID);
            return true;
        } catch (Exception e) {
            error = e;
            return false;
        }

    }

    private void requestDevice(String deviceID) {
        SoapObject request = new SoapObject(MyMethod.NAMESPACE, MyMethod.METHOD_NAME_REQUESTDEVICE);
        //Property which holds input parameters
        PropertyInfo pi = new PropertyInfo();
        pi.setName("deviceid");
        pi.setValue(deviceID);
        pi.setType(String.class);
        request.addProperty(pi);
        //Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        //Set output SOAP object
        envelope.setOutputSoapObject(request);
        //Create HTTP call object
        HttpTransportSE transportSE = new HttpTransportSE(MyMethod.URL);
        try {
            transportSE.call(MyMethod.SOAP_ACTION_REQUESTDEVICE, envelope);
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            data = response.toString();
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
    }
}