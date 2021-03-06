package com.vietdms.smarttvads;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ${LTG} on ${10/12/1994}.
 */
public class DownloadTask extends AsyncTask<String, Integer, String> {
    private Context context;
    private PowerManager.WakeLock mWakeLock;
    private String folder;
    private ProgressDialog mProgressDialog;

    private int noOfURLs;
    private int noUrlLoad;

    public DownloadTask(Context context, String folder, ProgressDialog mProgressDialog) {
        this.context = context;
        this.folder = folder;
        this.mProgressDialog = mProgressDialog;
    }

    @Override
    protected String doInBackground(String... sUrl) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            noOfURLs = sUrl.length;
            for (int i = 0; i < sUrl.length; i++) {
                URL url = new URL(sUrl[i]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Máy chủ trả về HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }
                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/" + folder + "/File" + (i + 1) + "." + sUrl[i].charAt(sUrl[i].length() - 3) + sUrl[i].charAt(sUrl[i].length() - 2) + sUrl[i].charAt(sUrl[i].length() - 1));

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);

                }
                noUrlLoad++;
            }

        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();
        mProgressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgress(progress[0]);
        mProgressDialog.setMessage("Hoàn thành " + noUrlLoad + "/" + noOfURLs);
        if(noUrlLoad==1) mProgressDialog.dismiss();
    }

    @Override
    protected void onPostExecute(String result) {
        mWakeLock.release();

        if (result != null)
            Toast.makeText(context, context.getString(R.string.error) + result, Toast.LENGTH_LONG).show();
    }
}

