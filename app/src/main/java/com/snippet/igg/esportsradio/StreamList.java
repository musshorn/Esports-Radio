package com.snippet.igg.esportsradio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class StreamList extends Activity {
    private StreamListAdapter adapter;
    ArrayList<String> streamers = new ArrayList<String>();
    ArrayList<String> LogoURLs = new ArrayList<String>();
    ArrayList<String> streamerStatus = new ArrayList<String>();
    ArrayList<String> previewURLs = new ArrayList<String>();
    ArrayList<Integer> streamerViewers = new ArrayList<Integer>();
    private ListView lv;
    private String searchName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_list);
        adapter = new StreamListAdapter(StreamList.this,streamers,LogoURLs,streamerStatus,streamerViewers);
        lv = (ListView)findViewById(R.id.streamersList);
        lv.setAdapter(adapter);
        Intent intent = getIntent();
        searchName = intent.getStringExtra("Game");
        search(searchName);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startPlayer(i);
            }
        });
    }
    public void startPlayer(int i){
        Intent intent = new Intent(this, Player.class);
        intent.putExtra("Stream",streamers.get(i));
        intent.putExtra("Game",searchName);
        intent.putExtra("PreviewURL",previewURLs.get(i));
        startActivityForResult(intent,1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == 1){
            String searchName = intent.getStringExtra("Game");
            search(searchName);
        }
    }

    public void search(String gameName){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("https://api.twitch.tv/kraken/search/streams?q=" + gameName, null, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray top = response.getJSONArray("streams");
                    for (int i = 0; i < top.length(); i++){
                        JSONObject channel = top.getJSONObject(i).getJSONObject("channel");
                        String name = channel.getString("name");
                        int viewers = top.getJSONObject(i).getInt("viewers");

                        String logos = channel.getString("logo");
                        String status = channel.getString("status");
                        String preview = top.getJSONObject(i).getJSONObject("preview").getString("large");
                        previewURLs.add(preview);
                        addItem(name,logos, status, viewers);
                    }

                } catch (JSONException e){
                    Log.d("JSON", "99 problems and the JSON's just one.");
                    Log.d("JSON", e.toString());
                }
                Log.d("HTTP", "Http request complete");
            }
        });
    }

    public void addItem(String game, String urlLogo, String status, int viewers){
        adapter.add(game);
        streamers.add(game);
        LogoURLs.add(urlLogo);
        streamerStatus.add(status);
        streamerViewers.add(viewers);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stream_list, menu);
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
