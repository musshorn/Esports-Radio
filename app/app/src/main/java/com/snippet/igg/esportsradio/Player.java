package com.snippet.igg.esportsradio;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.DecimalFormat;


public class Player extends Activity {

    private Handler mHandler = new Handler();
    private static long initialData = 0;
    private boolean state = false; // true = playing, false = stopped
    private String stream;
    private String game;
    private String previewURL;
    private Intent service;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageLoader il = ImageLoader.getInstance();
        if (initialData == 0)
            initialData = TrafficStats.getUidRxBytes(getApplicationInfo().uid);
       // getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_player);


        Intent intent = getIntent();
        stream = intent.getStringExtra("Stream");
        game = intent.getStringExtra("Game");
        previewURL = intent.getStringExtra("PreviewURL");

        final ImageButton bAction = (ImageButton)findViewById(R.id.btnAction);
        service = new Intent(getApplicationContext(), StreamService.class).setAction("Autoplay");
        service.putExtra("Stream",stream);
        service.putExtra("Game",game);
        service.putExtra("PreviewURL",previewURL);
        TextView tv = (TextView)findViewById(R.id.txtStreamer);
        tv.setText(stream);
        il.displayImage(previewURL,(ImageView)findViewById(R.id.imgPreview));
        bAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!state) {
                    Intent intent = new Intent(getApplicationContext(), StreamService.class);
                    intent.putExtra("Stream", stream).setAction("Play");
                    startService(intent);
                    bAction.setImageResource(R.drawable.stop);
                    state = true;
                }
                else{
                    Intent intent = new Intent(getApplicationContext(), StreamService.class);
                    intent.setAction(StreamService.STOP_MEDIA);
                    stopService(intent);
                    bAction.setImageResource(R.drawable.play);
                    state = false;
                }
            }
        });
    }

    @Override
    public void onNewIntent(Intent intent)
    {
        setIntent(intent);
        super.onNewIntent(intent);
        stream = intent.getStringExtra("Stream");
        game = intent.getStringExtra("Game");
        previewURL = intent.getStringExtra("PreviewURL");
    }

    @Override
    public void onDestroy(){
        unregisterReceiver(StreamReciever);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                Intent intent = new Intent();
                intent.putExtra("Stream", stream);
                intent.putExtra("Game", game);
                setResult(1,intent);
                finish();
                return true;
            case R.id.action_settings:
                intent = new Intent(getApplicationContext(),About.class);
                intent.putExtra("Stream", stream);
                intent.putExtra("Game", game);
                startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private Runnable mHandlerTask = new Runnable()
    {
        @Override
        public void run() {
            TextView tv = (TextView)findViewById(R.id.txtData);
            tv.setText(getFileSize(TrafficStats.getUidRxBytes(getApplicationInfo().uid) - initialData));
            mHandler.postDelayed(this, 1000);
        }
    };

    public static String getFileSize(long size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    @Override
    public void onResume(){
        Log.d("Resume","Called");
        super.onResume();
        mHandlerTask.run();
        startService(service);
        registerReceiver(StreamReciever, new IntentFilter(StreamService.SERVICE_UPDATE));
    }

    private BroadcastReceiver StreamReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(intent);
        }
    };

    private void updateUI(Intent intent){
        TextView tv = (TextView)findViewById(R.id.txtStatus);
        String response = intent.getStringExtra("Status");
        tv.setText(response);
    }
}
