package com.inhwa.nan.activity;

/**
 * Created by Inhwa_ on 2017-06-19.
 */

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.MenuItem;
import android.widget.ImageView;

import com.inhwa.nan.R;
import com.inhwa.nan.helper.SQLiteHandler;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

/**
 * Provides UI for the Detail page with Collapsing Toolbar.
 */
public class PerformanceDetailActivity extends AppCompatActivity {
    private SQLiteHandler db;

    public static final String EXTRA_POSITION = "position";

    ImageView performance_image;
    private Bitmap bitmap;
    private String image;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();

        String image = user.get("image");

        // Set Collapsing Toolbar layout to the screen
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        // Set title of Detail page
        // collapsingToolbar.setTitle(getString(R.string.item_title));

        performance_image = (ImageView) findViewById(R.id.image);
        Picasso.with(getApplicationContext()).invalidate("");
        Picasso.with(this).load(image).memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE).into(performance_image);
        image = getStringImage(bitmap);
        //performance_image.setImageBitmap(image);

        int postion = getIntent().getIntExtra(EXTRA_POSITION, 0);
        Resources resources = getResources();

        String[] performance_title = resources.getStringArray(R.array.performance);
        collapsingToolbar.setTitle(performance_title[postion % performance_title.length]);

//        String[] placeDetails = resources.getStringArray(R.array.place_details);
//        TextView placeDetail = (TextView) findViewById(R.id.place_detail);
//        placeDetail.setText(placeDetails[postion % placeDetails.length]);
//
//        String[] placeLocations = resources.getStringArray(R.array.place_locations);
//        TextView placeLocation =  (TextView) findViewById(R.id.place_location);
//        placeLocation.setText(placeLocations[postion % placeLocations.length]);
//
//        TypedArray placePictures = resources.obtainTypedArray(R.array.places_picture);
//        ImageView placePicutre = (ImageView) findViewById(R.id.image);
//        placePicutre.setImageDrawable(placePictures.getDrawable(postion % placePictures.length()));
//
//        placePictures.recycle();
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
//                NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
