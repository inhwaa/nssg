package com.inhwa.nan.activity;

/**
 * Created by Inhwa_ on 2017-06-19.
 */

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.inhwa.nan.R;
import com.inhwa.nan.app.AppConfig;
import com.inhwa.nan.app.AppController;
import com.inhwa.nan.helper.SQLiteHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class PerformanceDetailActivity extends AppCompatActivity {

    public static final String PERFORMANCE = "performance";

    public TextView artist, region, genre, pdate, ptime, detail, title, price;

    private SQLiteHandler db;

    public ImageView poster;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();

        Performance p = (Performance) getIntent().getSerializableExtra(PERFORMANCE);

        artist = (TextView)findViewById(R.id.performance_artist);
        title = (TextView) findViewById(R.id.performance_title);
        pdate = (TextView) findViewById(R.id.performance_date);
        ptime = (TextView) findViewById(R.id.performance_time);
        region = (TextView) findViewById(R.id.performance_location);
        detail = (TextView) findViewById(R.id.performance_detail);
        price = (TextView) findViewById(R.id.performance_price);
        poster = (ImageView) findViewById(R.id.iv_poster);

        title.setText(p.getTitle());
        pdate.setText(p.getPdate());
        ptime.setText(p.getPtime());
        region.setText(p.getRegion());
        detail.setText(p.getContent());
        price.setText(p.getPrice()==0?"무료":String.valueOf(p.getPrice())+"원");

        //등록했던 공연 포스터 불러오기
        Picasso.with(this).load(p.getImage()).into(poster);
        //string performance_no = p.getPID().toString(); performance_no 알고싶을때 하면 됨...아마도

        final int artist_no = p.getArtist_no();
        loadArtistName(artist_no);
        artist.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(PerformanceDetailActivity.this, ArtistInfoActivity.class);
                intent.putExtra(ArtistInfoActivity.ARTIST_NO, artist_no);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // load introduction from server
    private void loadArtistName(final int artist_no) {
        String tag_string_req = "req_introduction";
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_DOWNLOAD_ARTIST, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        artist.setText(jObj.getString("artist_name"));
                    } else {
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Update Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                //     hideDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("artist_no", String.valueOf(artist_no));
                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
}
