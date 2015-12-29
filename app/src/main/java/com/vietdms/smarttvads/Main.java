package com.vietdms.smarttvads;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

import java.util.ArrayList;


public class Main extends AppCompatActivity implements View.OnClickListener {
    private SQLiteDatabase database;
    private VideoView video;
    private ImageView image;
    private WebView web;
    private Button btnSetting;
    private int check = 0;
    private String LINK_IMAGE = "http://sdgaustralia.com/wp-content/uploads/2015/02/2013-honda-msx125-detailed-pictures-photo-gallery_7.jpg";
    private String LINK_WEB = "http://m.24h.com.vn";
    private String LINK_VIDEO = "http://www.androidbegin.com/tutorial/AndroidCommercial.3gp";
    private Handler handler;
    private Animation anim;
    private String FOLDER = "MyAdsData";
    protected PowerManager.WakeLock mWakeLock;
    private ProgressDialog mProgressDialog;
    private final float VOLUME = 0;
    private final int OPENWIFI = 1;
    private final int positionWeb = 1000;
    private ArrayList<Integer> arrInteger = new ArrayList<>();
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getId();
        loadData();// download and save to sdcard
        init();
    }

    private void loadData() {
        database = context.openOrCreateDatabase(MyMethod.DATABASE_NAME, SQLiteDatabase.CREATE_IF_NECESSARY, null);
        MyMethod.createFolder(FOLDER);
        MyMethod.createSchedule(database);
        if (MyMethod.isOnline(context)) {
            MyMethod.download(context, mProgressDialog, FOLDER, LINK_IMAGE, LINK_VIDEO);
            MyMethod.requestDevice(database, context, mProgressDialog, "tientest");
        } else showLayout(Layouts.Setting);
        mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                displayData(check);
            }
        });
//        addData(); CHECK RANDOM, DONT DELETE
    }

    private void addData() {
        arrInteger.add(1);
        arrInteger.add(2);
        arrInteger.add(3);
        arrInteger.add(4);
        arrInteger.add(5);
    }

    private void init() {
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();
        handler = new Handler();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSetting:
                WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                if (wifi.isWifiEnabled()) {
                    if (MyMethod.isOnline(context)) loadData();
                    else
                        startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), OPENWIFI);
                } else {
                    startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), OPENWIFI);
                }
//                //CHECK RANDOM , DONT DELETE
//                int chon = MyMethod.ramdomPerfect(arrInteger);
//                if (chon != -1)
//                    Toast.makeText(v.getContext(), chon + "", Toast.LENGTH_SHORT).show();
//                else {
//                    addData();
//                    btnSetting.performClick();
//                }
//                //Do something
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case OPENWIFI:
                btnSetting.setText(getString(R.string.loadagain));
                btnSetting.performClick();
                break;
            default:
                break;
        }
    }

    private enum Layouts {
        Video, Image, Webview, Setting
    }


    private void showLayout(Layouts layout) {
        switch (layout) {
            case Video:
                video.setVisibility(View.VISIBLE);
                image.setVisibility(View.GONE);
                btnSetting.setVisibility(View.GONE);
                web.setVisibility(View.GONE);
                anim = AnimationUtils.loadAnimation(context,
                        R.anim.fade_in);
                video.startAnimation(anim);

                MyMethod.loadVideo(video, FOLDER, "File2.3gp", VOLUME);
                video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        displayData(check);
                    }
                });
                check = 1;
                break;
            case Image:
                video.setVisibility(View.GONE);
                btnSetting.setVisibility(View.GONE);
                image.setVisibility(View.VISIBLE);
                web.setVisibility(View.GONE);
                anim = AnimationUtils.loadAnimation(context,
                        R.anim.fade_in);
                image.startAnimation(anim);

                image.setImageResource(R.mipmap.ic_launcher);
                check = 2;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after 5s = 5000ms
                        displayData(check);

                    }
                }, 1000 * 10);
                break;
            case Webview:
                video.setVisibility(View.GONE);
                image.setVisibility(View.GONE);
                btnSetting.setVisibility(View.GONE);
                web.setVisibility(View.VISIBLE);
                anim = AnimationUtils.loadAnimation(context,
                        R.anim.fade_in);
                web.startAnimation(anim);

                MyMethod.loadWebview(web, positionWeb, LINK_WEB);
                check = 0;

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after 5s = 5000ms
                        displayData(check);

                    }
                }, 1000 * 10);
                break;
            case Setting:
                video.setVisibility(View.GONE);
                image.setVisibility(View.GONE);
                web.setVisibility(View.GONE);
                btnSetting.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }


    private void displayData(int check) {
        switch (check) {
            case 0:
                anim = AnimationUtils.loadAnimation(context,
                        R.anim.fade_out);
                web.startAnimation(anim);
//                web.destroy();
                web.destroyDrawingCache();
                showLayout(Layouts.Video);
                break;
            case 1:
                anim = AnimationUtils.loadAnimation(context,
                        R.anim.fade_out);
                video.startAnimation(anim);
                video.destroyDrawingCache();
                showLayout(Layouts.Image);
                break;
            case 2:
                anim = AnimationUtils.loadAnimation(context,
                        R.anim.fade_out);
                image.startAnimation(anim);
                image.destroyDrawingCache();
                showLayout(Layouts.Webview);
                break;
            default:
                break;
        }
    }

    private void getId() {
        context = getApplicationContext();
        video = (VideoView) findViewById(R.id.vid);
        image = (ImageView) findViewById(R.id.img);
        web = (WebView) findViewById(R.id.web);
        btnSetting = (Button) findViewById(R.id.btnSetting);
        mProgressDialog = new ProgressDialog(Main.this);
        btnSetting.setOnClickListener(this);

    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onDestroy() {
        this.mWakeLock.release();
        Log.w(getString(R.string.app_name), "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        Log.w(getString(R.string.app_name), "onRestart");
        super.onRestart();
    }

    @Override
    protected void onStart() {
        Log.w(getString(R.string.app_name), "onStart");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.w(getString(R.string.app_name), "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.w(getString(R.string.app_name), "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.w(getString(R.string.app_name), "onResume");
        super.onResume();
    }

}
