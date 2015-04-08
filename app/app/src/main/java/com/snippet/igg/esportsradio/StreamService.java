package com.snippet.igg.esportsradio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import android.os.Binder;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import wseemann.media.FFmpegMediaPlayer;

/**
 * Created by snippet on 14/9/2014.
 */
public class StreamService extends Service {
    private FFmpegMediaPlayer mp = new FFmpegMediaPlayer();
    Intent intent;
    public static final String SERVICE_UPDATE = "com.snippet.esportsradio.StreamService";
    public static final String STOP_MEDIA = "stop";
    private String game;
    private String stream;
    private String previewURL;

    @Override
    public IBinder onBind(Intent intent){
        return mpBinder;
    }
    public class LocalBinder extends Binder{
        StreamService getService(){
            return StreamService.this;
        }
    }
    private final IBinder mpBinder = new LocalBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() == STOP_MEDIA){
            mp.stop();
            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(420);
        }
        else {
            AsyncHttpClient client = new AsyncHttpClient();
            stream = intent.getStringExtra("Stream");
            game = intent.getStringExtra("Game");
            previewURL = intent.getStringExtra("PreviewURL");
            sendUpdate("Status", "Checking Server");
            client.get("http://musshorn.me:5000/?stream=" + stream, null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        Log.d("HTTP", "Request recieved");
                        String status = response.getString("status");
                        Log.d("HTTP", status);
                        if (status.equals("Live")) {

                            String url = response.getString("URL");
                            play(url);
                        }

                        //Wait 10s if the server is streaming content
                        if (status.equals("Loading")) {
                            sendUpdate("Status", "Server Buffering");
                            final String url = response.getString("URL");
                            Log.d("HTTP", "Server Buffering");
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    play(url);
                                }
                            }, 10000);
                        }

                    } catch (JSONException e) {
                        Log.d("JSON", e.toString());
                    }


                }
            });
        }
        return START_STICKY;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        intent = new Intent(SERVICE_UPDATE);
    }
    @Override
    public void onDestroy(){
        mp.stop();
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(420);
        super.onDestroy();
    }

    private void play(final String url){
        Intent notificationIntent = new Intent(this, StreamService.class);

        notificationIntent.setAction(STOP_MEDIA);
        Random r = new Random(); // uses a random number to stop PI being cached
        PendingIntent pi = PendingIntent.getService(this, r.nextInt(),
                notificationIntent,
                0);
        Intent playerIntent = new Intent(this, Player.class);
        playerIntent.putExtra("Game",game);
        playerIntent.putExtra("Stream",stream);
        playerIntent.putExtra("PreviewURL",previewURL);
        playerIntent.addCategory(Intent.CATEGORY_DEFAULT);
        playerIntent.setAction(Intent.ACTION_MAIN);

        PendingIntent ppi = PendingIntent.getActivity(this, r.nextInt(),
                playerIntent,
                0);


        Notification notification = new Notification.Builder(this)
                .setContentTitle("Esports radio")
                .setContentText("Now Playing: " + stream)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(ppi)
                .setAutoCancel(false)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_pause, "Stop", pi)
                .build();

        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nm.notify(420, notification);
        Log.d("HTTP", "Playing");
        sendUpdate("Status", "Playing");

        try {
            mp.reset();
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.setOnPreparedListener(new FFmpegMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(FFmpegMediaPlayer fFmpegMediaPlayer) {
                    fFmpegMediaPlayer.start();
                }
            });
            mp.setDataSource(url);
            mp.prepareAsync();

            mp.setOnBufferingUpdateListener(new FFmpegMediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(FFmpegMediaPlayer fFmpegMediaPlayer, int i) {
                    sendUpdate("Status", "Buffering... " + Integer.toString(i));
                }
            });
            mp.setOnCompletionListener(new FFmpegMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(FFmpegMediaPlayer fFmpegMediaPlayer) {
                    sendUpdate("Status", "Playback stopped.");
                }
            });
            mp.setOnInfoListener(new FFmpegMediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(FFmpegMediaPlayer fFmpegMediaPlayer, int i, int i2) {
                    sendUpdate("Status", "Info: " + Integer.toString(i) + " " + Integer.toString(i2));
                    return false;
                }
            });
            }catch(Exception e){
            Log.d("MP Error", e.toString());
        }
    }


    private void sendUpdate(String key, String message)
    {
        Intent intent = new Intent(SERVICE_UPDATE);
        intent.putExtra(key, message);
        sendBroadcast(intent);
    }
}
