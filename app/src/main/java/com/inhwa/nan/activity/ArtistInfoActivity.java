package com.inhwa.nan.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.inhwa.nan.helper.SessionManager;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by luvthemoon on 2017. 8. 24..
 */

public class ArtistInfoActivity extends AppCompatActivity {

    private SQLiteHandler db;
    private SessionManager session;

    ImageView profile;
    TextView artist_name, artist_intro;

    ByteArrayOutputStream byteArrayOutputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_info);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();

        String name = user.get("name");
        String email = user.get("email");
        String image = user.get("image");
        String artistname = user.get("sub_name");

        profile = (ImageView) findViewById(R.id.iv_profile);
        artist_name = (TextView) findViewById(R.id.tv_artistname);
        artist_intro = (TextView) findViewById(R.id.tv_artistintro);

        // 이미지 session
        Picasso.with(getApplicationContext()).invalidate("");
        Picasso.with(this).load(image).memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE).into(profile);
        byteArrayOutputStream = new ByteArrayOutputStream();

        artist_name.setText(artistname);
        loadIntroduction(email);
    }

    // load introduction from server
    private void loadIntroduction(final String email) {
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
                        artist_intro.setText(jObj.getString("introduction"));
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
                params.put("email", email);
                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
}
