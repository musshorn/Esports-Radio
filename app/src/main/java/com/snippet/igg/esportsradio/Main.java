package com.snippet.igg.esportsradio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;


public class Main extends Activity   {
    ArrayList<String> games = new ArrayList<String>();
    ArrayList<String> LogoURLs = new ArrayList<String>();
    Handler mHandler = new Handler();
    private ListView lv;
    private GamesListAdapter adapter;

    public static void initImageLoader(Context context){
        File cacheDir = StorageUtils.getCacheDirectory(context);
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .taskExecutorForCachedImages(AsyncTask.THREAD_POOL_EXECUTOR)
                .threadPoolSize(3) // default
                .threadPriority(Thread.NORM_PRIORITY - 1) // default
                .tasksProcessingOrder(QueueProcessingType.FIFO) // default
                .defaultDisplayImageOptions(defaultOptions)
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .denyCacheImageMultipleSizesInMemory()
                .diskCache(new UnlimitedDiscCache(cacheDir)) // default
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
                .build();
        ImageLoader.getInstance().init(config);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == 1){
            String searchName = intent.getStringExtra("Game");
            Log.d("Found it?", "got ere");
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adapter = new GamesListAdapter(Main.this,games,LogoURLs);
        lv = (ListView)findViewById(R.id.gamesList);
        lv.setAdapter(adapter);
        AsyncHttpClient client = new AsyncHttpClient();
        initImageLoader(getApplicationContext());
        client.get("https://api.twitch.tv/kraken/games/top", null, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray top = response.getJSONArray("top");
                    for (int i = 0; i < top.length(); i++){
                        JSONObject game = top.getJSONObject(i).getJSONObject("game");
                        String name = game.getString("name");

                        JSONObject logos = game.getJSONObject("logo");
                        String logourl = logos.getString("medium");
                        addItem(name, logourl);
                    }

                } catch (JSONException e){
                    Log.d("JSON", "99 problems and the JSON's just one.");
                    Log.d("JSON", e.toString());
                }
                Log.d("HTTP", "Http request complete");

            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), StreamList.class);
                intent.putExtra("Game",games.get(i));
                startActivity(intent);
            }
        });
    }
    public void addItem(String game, String urlLogo){
        //adapter.addGame(game, );
        adapter.add(game);
        games.add(game);
        LogoURLs.add(urlLogo);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(),About.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}