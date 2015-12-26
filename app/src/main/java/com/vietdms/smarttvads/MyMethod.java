package com.vietdms.smarttvads;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by ${LTG} on ${10/12/1994}.
 */
public class MyMethod {


    public static void createFolder(String foldername) {
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + foldername);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
            Log.w(foldername, "Tạo folder thành công");
        } else {
            // Do something else on failure
            Log.w(foldername, "Tạo folder thất bại");
        }

        //Xoa rong folder
        File dir = new File(Environment.getExternalStorageDirectory(), foldername);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }

    }

    public static void download(Context context, final ProgressDialog mProgressDialog, final String folder, String... link) {
        mProgressDialog.setMessage("Đang tải dữ liệu");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
// execute this when the downloader must be fired
        final DownloadTask downloadTask = new DownloadTask(context, folder, mProgressDialog);
        downloadTask.execute(link);

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downloadTask.cancel(true);
            }
        });


    }
    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    public static boolean hasData(String pathfolder) {
        File directory = new File(pathfolder);
        File[] contents = directory.listFiles();
        return (contents == null || contents.length == 0) ? false : true;
    }

    public static boolean checkExistFile(File A, File B) {
        boolean result = false;
        //Do something

        return result;
    }

    public static int ramdomPerfect(ArrayList<Integer> list) {
        Random rand = new Random();
        while (list.size() > 0) {
            int index = rand.nextInt(list.size());
            return list.remove(index);
        }
        return -1;

    }


}
