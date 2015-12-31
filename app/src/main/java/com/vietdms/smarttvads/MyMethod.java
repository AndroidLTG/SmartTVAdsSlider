package com.vietdms.smarttvads;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
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
    public static final String TYPE_VIDEO = "VIDEO";
    public static final String TYPE_IMAGE = "PC";
    public static final String TYPE_WEB = "WEB";
    public static final String TYPE_ADS = "QC";
    public static String LINKWEB="";


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

    public static void loadDataFromSqlite(ArrayList<Ads> arrAds, SQLiteDatabase database) {
        if (MyMethod.isTableExists(database, MyMethod.TABLESHOW)) {
            if (database != null) {
                Cursor cursor = database.query(MyMethod.TABLESHOW, null, null, null, null, null, null);
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    Ads ads = new Ads();
                    ads.setSerial(cursor.getInt(cursor.getColumnIndex(MyMethod.ColumnSTT)));
                    ads.setType(cursor.getString(cursor.getColumnIndex(MyMethod.ColumnTYPE)));
                    ads.setUrl(cursor.getString(cursor.getColumnIndex(MyMethod.ColumnURL)));
                    ads.setBackupUrl(cursor.getString(cursor.getColumnIndex(MyMethod.ColumnBACKUPURL)));
                    ads.setExtension(cursor.getString(cursor.getColumnIndex(MyMethod.ColumnURL)));
                    ads.setDuration(cursor.getFloat(cursor.getColumnIndex(MyMethod.ColumnDURATION)));
                    if (ads.getType().contains(MyMethod.TYPE_VIDEO))
                        ads.setVolume(cursor.getFloat(cursor.getColumnIndex(MyMethod.ColumnVOLUME)));
                    else ads.setVolume(0f);
                    ads.setStartDate(cursor.getString(cursor.getColumnIndex(MyMethod.ColumnSTARTDATE)));
                    arrAds.add(ads);
                    cursor.moveToNext();
                }
                Log.w("SIZE LOAD : ", arrAds.size() + "");
                cursor.close();
            }
        }
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
                        + MyMethod.ColumnTYPE + " text, "
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
        final DownloadTask downloadTask = new DownloadTask(context, folder, mProgressDialog);
        downloadTask.execute(link);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downloadTask.cancel(true);
            }
        });
    }

    public static void requestDevice(ArrayList<Ads> arrAds,SQLiteDatabase database, Context context, final ProgressDialog mProgressDialog, final String deviceId,String folder) {
        mProgressDialog.setMessage("Đang quét thiết bị");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
        final DownloadTask downloadTask = new DownloadTask(context, folder, mProgressDialog);
        final RequestTask requestTask = new RequestTask(arrAds,downloadTask,context,database, deviceId, mProgressDialog);
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

    public static void loadImage(ImageView imageView, String folderName, String fileName) {
        try {
            String uriPath = Environment.getExternalStorageDirectory() + "/" + folderName + "/" + fileName;
            Bitmap bmp = BitmapFactory.decodeFile(uriPath);
            imageView.setImageBitmap(bmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public static void playVideo(MediaController mediaController, final VideoView video, String url, final float VOLUME) {
        Uri urlVideo = Uri.parse(url);
        mediaController.setAnchorView(video);
        video.setMediaController(mediaController);
        video.setVideoURI(urlVideo);
        video.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                // TODO Auto-generated method stub
                return false;
            }
        });
        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            public void onPrepared(MediaPlayer arg0) {
                arg0.setVolume(VOLUME / 100, VOLUME / 100);
                video.start();
            }
        });
    }

    public static void loadWebview(final WebView web, final int positionWeb, String LINK_WEB) {
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        web.setInitialScale(1);
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
        web.loadUrl(LINK_WEB);
    }
    public static void showToast(Context context,String t ) {
        Toast.makeText(context,t,Toast.LENGTH_SHORT).show();
    }
}
