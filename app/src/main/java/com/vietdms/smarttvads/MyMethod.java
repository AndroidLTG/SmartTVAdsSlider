package com.vietdms.smarttvads;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.VideoView;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

/**
 * Created by ${LTG} on ${10/12/1994}.
 */
public class MyMethod {

    public static final String NAMESPACE = "http://tempuri.org/";
    public static final String METHOD_NAME_REQUESTDEVICE = "AddTV_RequestAds";
    public static final String URL = "http://indico.vn:8101/mywebservice.asmx?WSDL";
    public static final String SOAP_ACTION_REQUESTDEVICE = "http://tempuri.org/AddTV_RequestAds";

    //SQLITE
    public static final String DATABASE_NAME = "ltg.db";
    public static final String TABLESHOW = "Schedule";
    public static final String ColumnRowID = "ROWID";
    public static final String ColumnSTT = "STT";
    public static final String ColumnTYPE = "TYPE";
    public static final String ColumnURL = "URL";
    public static final String ColumnBACKUPURL = "BACKUPURL";
    public static final String ColumnDURATION = "DURATION";
    public static final String ColumnSTARTDATE = "STARTDATE";
    public static final String ColumnVOLUME = "VOLUME";


    public static boolean isTableExists(SQLiteDatabase database, String tableName) {
        Cursor cursor = database.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();

                return true;
            }
            cursor.close();
        }
        return false;
    }
    //check if field exist
    public static boolean isFieldExist(SQLiteDatabase database, String tablename, String fildName, String fildValue) {
        String Query = "SELECT * FROM " + tablename + " WHERE " + fildName + " = '" + fildValue + "'";
        Cursor cursor = database.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }
    //create table save Schedule
    public static SQLiteDatabase createSchedule(SQLiteDatabase database) {
        try {

            if (database != null) {
                if (MyMethod.isTableExists(database, MyMethod.TABLESHOW))
                    return database;
                database.setLocale(Locale.getDefault());
                database.setVersion(1);
                String sql = "create table " + MyMethod.TABLESHOW + " ("
                        + MyMethod.ColumnRowID + " integer primary key autoincrement,"
                        + MyMethod.ColumnSTT + " integer,"
                        + MyMethod.ColumnTYPE + " integer, "
                        + MyMethod.ColumnURL + " text, "
                        + MyMethod.ColumnBACKUPURL + " text, "
                        + MyMethod.ColumnSTARTDATE + " text, "
                        + MyMethod.ColumnDURATION + " float, "
                        + MyMethod.ColumnVOLUME + " float)";
                database.execSQL(sql);
            }
        } catch (Exception e) {
        }
        return database;
    }


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

    public static void requestDevice(SQLiteDatabase database,Context context, final ProgressDialog mProgressDialog, final String deviceId) {
        mProgressDialog.setMessage("Đang tải dữ liệu");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
// execute this when the downloader must be fired
        final RequestTask requestTask = new RequestTask(database, deviceId, mProgressDialog);
        requestTask.execute();

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                requestTask.cancel(true);
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

    public static void loadVideo(final VideoView video, String folderName, String fileName, final float VOLUME) {
        // Create a progressbar
        try {
            String uriPath = Environment.getExternalStorageDirectory() + "/" + folderName + "/" + fileName;
            video.setVideoPath(uriPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            // Close the progress bar and play the video
            public void onPrepared(MediaPlayer mp) {
                mp.setVolume(VOLUME / 100, VOLUME / 100);
                video.start();

            }
        });

    }

    public static void loadWebview(final WebView web, final int positionWeb, String LINK_WEB) {
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(false);
        web.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);

                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                web.scrollTo(0, positionWeb);
            }
        });
        web.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        web.loadUrl(LINK_WEB);
    }
}
