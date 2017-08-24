package com.inhwa.nan.activity;

import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.inhwa.nan.R;
import com.inhwa.nan.app.AppConfig;
import com.inhwa.nan.app.AppController;
import com.inhwa.nan.helper.SQLiteHandler;
import com.inhwa.nan.helper.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Inhwa_ on 2017-06-19.
 */

public class ListOfMyPerformanceActivity extends AppCompatActivity {

    private SQLiteHandler db;
    private SessionManager session;

    private RecyclerView recyclerView;
    private PerformanceAdapter adapter;
    private List<Performance> performanceList;
    private String[] subject;
    private String[] imagesubject;
    private int selection;
    private int position;
    public static final String EXTRA_POSITION = "position";
    public static final String EXTRA_SELECTION = "selection";

    private String email;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performance_list);
    }

    protected void onResume() {
        super.onResume();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        position = getIntent().getIntExtra(EXTRA_POSITION, 0);
        selection = getIntent().getIntExtra(EXTRA_SELECTION, 0);
        Resources resources = getResources();

        initCollapsingToolbar();

        performanceList = new ArrayList<>();
        adapter = new PerformanceAdapter(this, performanceList, 1);

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(dpToPx(10)));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());
        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();

        email = user.get("email").toString();

        preparePerformances();
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

    /**
     * Initializing collapsing toolbar
     * Will show and hide the toolbar title on scroll
     */
    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("업로드한 공연");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(false);
    }

    private void preparePerformances() {
        // email로 찾기
        String tag_string_req = "req_per_email";
        StringRequest strReq = new StringRequest(Request.Method.POST,
        AppConfig.URL_MY_PERFORMANCE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jObj = new JSONObject(response);
                    JSONArray jArry = jObj.getJSONArray("performances");
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        for (int i = 0; i < jArry.length(); i++) {
                            JSONObject performance = jArry.getJSONObject(i);
                            int PID = performance.getInt("performance_no");
                            String title = performance.getString("title");
                            String content = performance.getString("content");
                            String region = performance.getString("region");
                            String genre = performance.getString("genre");
                            String pdate = performance.getString("perform_date");
                            String ptime = performance.getString("perform_time");
                            String image = performance.getString("image");
                            String location = performance.getString("location");

                            // Performance class 생성, 리스트에 추가한다.
                            Performance p = new Performance(PID, title, content, region, genre, pdate, ptime, image, location); //수정삭제 가능한 페이지로 변경
                            performanceList.add(p);
                        }
                        adapter.notifyDataSetChanged();

                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // php 에 parameter 보내기
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spacing;

        public GridSpacingItemDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            outRect.top = spacing; // item top
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}
