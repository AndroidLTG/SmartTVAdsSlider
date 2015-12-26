package com.vietdms.smarttvads;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private VideoView video;
    private ImageView image;
    private WebView web;
    private TextView txtH, txtF;
    private int check = 0;
    private String LINK_IMAGE = "http://sdgaustralia.com/wp-content/uploads/2015/02/2013-honda-msx125-detailed-pictures-photo-gallery_7.jpg";
    private String LINK_WEB = "http://www.honda.com.vn/vn/xe-may/san-pham/msx-125cc/";
    private String LINK_VIDEO = "http://www.androidbegin.com/tutorial/AndroidCommercial.3gp";
    private ProgressDialog pDialog;
    private int SECOND = 1;
    private Handler handler;
    private Animation anim;
    private String FOLDER = "MyAdsData";
    protected PowerManager.WakeLock mWakeLock;
    private ProgressDialog mProgressDialog;
    private final float VOLUME = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getId();
        loadData();// download and save to sdcard
        init();
    }

    private void loadData() {
        MyMethod.createFolder(FOLDER);
// instantiate it within the onCreate method
        if (MyMethod.isOnline(getApplicationContext()))
            MyMethod.download(getApplicationContext(), mProgressDialog, FOLDER, LINK_IMAGE, LINK_VIDEO);
        else showLayout(Layouts.Setting);

        mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                displayData(check);
            }
        });

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
                //Do something
                break;
            default:
                break;
        }
    }

    private enum Layouts {
        Video, Image, Webview, Setting
    }

    private void loadWebview() {
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(false);
        web.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        web.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            public void onPageFinished(WebView view, String url) {
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

            }
        });
        web.loadUrl(LINK_WEB);
    }

    private void showLayout(Layouts layout) {
        switch (layout) {
            case Video:
                video.setVisibility(View.VISIBLE);
                image.setVisibility(View.GONE);
                web.setVisibility(View.GONE);
                anim = AnimationUtils.loadAnimation(getApplicationContext(),
                        R.anim.fade_in);
                video.startAnimation(anim);

                loadVideo();
                check = 1;
                break;
            case Image:
                video.setVisibility(View.GONE);
                image.setVisibility(View.VISIBLE);
                web.setVisibility(View.GONE);
                anim = AnimationUtils.loadAnimation(getApplicationContext(),
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
                web.setVisibility(View.VISIBLE);
                anim = AnimationUtils.loadAnimation(getApplicationContext(),
                        R.anim.fade_in);
                web.startAnimation(anim);

                loadWebview();
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
                break;
            default:
                break;
        }
    }

    private void loadVideo() {
        // Create a progressbar
        try {
            String uriPath = Environment.getExternalStorageDirectory() + "/" + FOLDER + "/File2.3gp";
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
        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                displayData(check);
            }
        });
    }


    private void displayData(int check) {
        switch (check) {
            case 0:
                anim = AnimationUtils.loadAnimation(getApplicationContext(),
                        R.anim.fade_out);
                web.startAnimation(anim);
                web.destroy();
                web.destroyDrawingCache();
                showLayout(Layouts.Video);
                break;
            case 1:
                anim = AnimationUtils.loadAnimation(getApplicationContext(),
                        R.anim.fade_out);
                video.startAnimation(anim);
                video.stopPlayback();
                video.destroyDrawingCache();
                showLayout(Layouts.Image);
                break;
            case 2:
                anim = AnimationUtils.loadAnimation(getApplicationContext(),
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
        video = (VideoView) findViewById(R.id.vid);
        image = (ImageView) findViewById(R.id.img);
        web = (WebView) findViewById(R.id.web);
        mProgressDialog = new ProgressDialog(MainActivity.this);
        (findViewById(R.id.btnSetting)).setOnClickListener(this);

    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onDestroy() {
        this.mWakeLock.release();
        super.onDestroy();
    }
}
