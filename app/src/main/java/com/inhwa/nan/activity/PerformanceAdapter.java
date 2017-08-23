package com.inhwa.nan.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.inhwa.nan.R;
import com.inhwa.nan.app.AppConfig;
import com.inhwa.nan.app.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by Inhwa_ on 2017-06-19.
 */

public class PerformanceAdapter extends RecyclerView.Adapter<PerformanceAdapter.MyViewHolder> {

    private Context mContext;
    private List<Performance> performanceList;
//    private List<Performance> myperformanceList;

    public int like_count = 0;
    public int scrap_count = 0;
    public int VIEWTYPE;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView region;
        public TextView genre;
        public TextView pdate;
        public TextView ptime;
        public ImageView image;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.card_title);
            region = (TextView) view.findViewById(R.id.card_region);
            genre = (TextView) view.findViewById(R.id.card_genre);
            pdate = (TextView) view.findViewById(R.id.card_date);
            ptime = (TextView) view.findViewById(R.id.card_time);
            image = (ImageView) view.findViewById(R.id.card_image);

           itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();

                    if(VIEWTYPE == 0) {
                        Intent intent = new Intent(context, PerformanceDetailActivity.class);
                        intent.putExtra(PerformanceDetailActivity.PERFORMANCE, performanceList.get(getAdapterPosition()));
                        context.startActivity(intent);
                    }else if(VIEWTYPE == 1){
                        Intent intent = new Intent(context, ListOfMyPerformanceDetailActivity.class);
                        intent.putExtra(PerformanceDetailActivity.PERFORMANCE, performanceList.get(getAdapterPosition()));
                        context.startActivity(intent);
                    }
                }
            });

       /*     itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, PerformanceDetailActivity.class);
                    intent.putExtra(PerformanceDetailActivity.PERFORMANCE, myperformanceList.get(getAdapterPosition()));
                    context.startActivity(intent);
                }
            });*/



       /*     itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, PerformanceDetailActivity.class);
                    intent.putExtra(PerformanceDetailActivity.PERFORMANCE, myperformanceList.get(getAdapterPosition()));
                    context.startActivity(intent);
                }
            });*/

            final CheckBox like_Button = (CheckBox) itemView.findViewById(R.id.chk_like);
            like_Button.setChecked(like_count == 1);
            like_Button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (like_count == 0) {
                        Snackbar.make(v, "좋아요를 눌렀습니다.", Snackbar.LENGTH_SHORT).show();
                        like_count++;
                    } else if (like_count == 1) {
                        Snackbar.make(v, "좋아요를 취소했습니다.", Snackbar.LENGTH_SHORT).show();
                        like_count--;
                    }
                }
            });

            final ImageButton shareImageButton = (ImageButton) itemView.findViewById(R.id.share_button);
            shareImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (scrap_count == 1) {
                        Snackbar.make(v, "스크랩 되었습니다.", Snackbar.LENGTH_SHORT).show();
                        shareImageButton.setColorFilter(Color.YELLOW);
                        scrap_count--;
                    } else if (scrap_count == 0) {
                        shareImageButton.setEnabled(false);
                        Snackbar.make(v, "이미 스크랩 되었습니다.", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }



    public PerformanceAdapter(Context mContext, List<Performance> albumList, int viewtype) {
        this.mContext = mContext;
        this.performanceList = albumList;
        VIEWTYPE = viewtype;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.performance_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Performance performance = performanceList.get(position);
        holder.region.setText(performance.getRegion());
        holder.title.setText(performance.getTitle());
        holder.genre.setText(performance.getGenre());
        holder.pdate.setText(performance.getPdate());
        holder.ptime.setText(performance.getPtime());
        Glide.with(mContext).load(performance.getImage()).into(holder.image);
        checkLikeState("pih0902@naver.com",50);
    }

    @Override
    public int getItemCount() {
        return performanceList.size();
    }

    // Check like state
    private void checkLikeState(final String email, final int performance_no) {
        String tag_string_req = "req_likestate";
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_CHECK_LIKE, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        Log.d(TAG, "like!" + jObj.getInt("like_state"));
//                        if(jObj.getInt("like_state")==0) like_count=0;
//                        else like_count=1;
//                        Log.d(TAG, "like count " + like_count);
                    } else {
                        String errorMsg = jObj.getString("error_msg");
                        Log.d(TAG, "errorMsg: " + errorMsg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG, "JSONException: " + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Update Error: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("performance_no", String.valueOf(performance_no));
                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
}
