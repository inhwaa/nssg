package com.inhwa.nan.activity;

/**
 * Created by Inhwa_ on 2017-06-19.
 */

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.inhwa.nan.R;
import com.inhwa.nan.helper.SQLiteHandler;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class PerformanceDetailActivity extends AppCompatActivity {

    public static final String PERFORMANCE = "performance";
    public TextView artist, region, genre, pdate, ptime, detail, title;
    private SQLiteHandler db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();

        String image = user.get("image");

        Performance p = (Performance) getIntent().getSerializableExtra(PERFORMANCE);

        artist = (TextView)findViewById(R.id.performance_artist);
        title = (TextView) findViewById(R.id.performance_title);
        pdate = (TextView) findViewById(R.id.performance_date);
        ptime = (TextView) findViewById(R.id.performance_time);
        region = (TextView) findViewById(R.id.performance_location);
        detail = (TextView) findViewById(R.id.performance_detail);

        title.setText(p.getTitle());
        pdate.setText(p.getPdate());
        ptime.setText(p.getPtime());
        region.setText(p.getRegion());
        detail.setText(p.getContent());
        //string performance_no = p.getPID().toString(); performance_no 알고싶을때 하면 됨...아마도

        artist.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(PerformanceDetailActivity.this, ArtistInfoActivity.class);
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
}
