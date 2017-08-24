package com.inhwa.nan.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;

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
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Inhwa_ on 2017-05-17.
 */

public class ListOfMyPerformanceDetailActivity extends AppCompatActivity{
    public static final String PERFORMANCE = "performance";

    public static String s_genre = "";
    public static String s_region = "";
    public static String s_email;
    public static int PID;
    private static final String TAG = UploadPerformanceActivity.class.getSimpleName();

    private ImageView poster_view;
    private ImageButton img_change;
    private EditText ptitle, detail;
    private TextView pdate, ptime, plocation;
    private Spinner genre, region;

    private Button btnCancel, btnModify, btnDelete;

    private String place_info = "";

    private SQLiteHandler db;
    private SessionManager session;

    final int DIALOG_DATE = 1;
    final int DIALOG_TIME = 2;

    DatePickerDialog dpd;

    int s_year, s_month, s_day, s_hour, s_min; //selected play day and time
    int year, month, day, hour, min; //to initialize the date

    private Bitmap bitmap;
    private String image;

    private int PICK_IMAGE_REQUEST = 1;

    ByteArrayOutputStream byteArrayOutputStream;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploaded_performance);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();

        String image = user.get("image");

        Performance p = (Performance) getIntent().getSerializableExtra(PERFORMANCE);

        PID = p.getPID();

        poster_view = (ImageView) findViewById(R.id.iv);
        img_change = (ImageButton) findViewById(R.id.imgbtn);

        //등록했던 공연 포스터 불러오기
        Picasso.with(this).load(p.getImage()).into(poster_view);

        ptitle = (EditText) findViewById(R.id.edtSetTitle);
        pdate = (TextView) findViewById(R.id.date_tv);
        ptime = (TextView) findViewById(R.id.time_tv);
        detail = (EditText) findViewById(R.id.edtIntroPerformance);
        genre = (Spinner) findViewById(R.id.spinnerSetGenre);
        region = (Spinner) findViewById(R.id.spinnerSetRegion);
        plocation = (TextView) findViewById(R.id.place_details);

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        btnModify = (Button) findViewById(R.id.btnModify);

        //업로드 한 공연 정보 불러오기
        ptitle.setText(p.getTitle());
        pdate.setText(p.getPdate());
        ptime.setText(p.getPtime());
        //if (p.getRegion() == )
        detail.setText(p.getContent());
        //genre.getSelectedItem(p.getContent());
        plocation.setText(p.getLocation());
        img_change.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                albumAction();
            }
        });

        //수정 취소
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        //수정 삭제
        btnDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String title = ptitle.getText().toString();
                String date = pdate.getText().toString();
                String time = ptime.getText().toString();
                String genre = s_genre;
                String region = s_region;
                String location = plocation.getText().toString();
                String content = detail.getText().toString();
                String email = s_email;
                deletePerformance(PID);
                Toast.makeText(getApplicationContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        //수정한 공연 업로드
        btnModify.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String title = ptitle.getText().toString();
                String date = pdate.getText().toString();
                String time = ptime.getText().toString();
                String genre = s_genre;
                String region = s_region;
                String location = plocation.getText().toString();
                String content = detail.getText().toString();
                String email = s_email;
                if (title.matches("")||date.matches("날짜 선택")||time.matches("시간 선택")||content.matches("")) {
                    Toast.makeText(getApplicationContext(), "모든 항목을 입력하세요", Toast.LENGTH_LONG).show();
                } else {
                    editPerformance(title, date, time, genre, region, location, content, email);
                    Toast.makeText(getApplicationContext(), "업로드.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    } //end of oncreate

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DATE:
                dpd = new DatePickerDialog(ListOfMyPerformanceDetailActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                Toast.makeText(getApplicationContext(),
                                        year + "년 " + (month + 1) + "월 " + dayOfMonth + "일을 선택했습니다",
                                        Toast.LENGTH_SHORT).show();
                                s_year = year;
                                s_month = month + 1;
                                s_day = dayOfMonth;
                                pdate.setText(s_year + "/" + s_month + "/" + s_day);
                            }
                        }, year, month, day); //오늘 날짜로 초기화 하고싶다
                return dpd;

            case DIALOG_TIME:
                TimePickerDialog tpd = new TimePickerDialog(ListOfMyPerformanceDetailActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                Toast.makeText(getApplicationContext(),
                                        hourOfDay + "시 " + minute + "분을 선택했습니다",
                                        Toast.LENGTH_SHORT).show();
                                s_hour = hourOfDay;
                                s_min = minute;
                                ptime.setText(s_hour + ":" + s_min);
                            }
                        }, hour, min, false);
                return tpd;
        }
        return super.onCreateDialog(id);
    }

    private void albumAction() {
        Intent albumIntent = new Intent(Intent.ACTION_GET_CONTENT);
        albumIntent.setType("image/*");
        albumIntent.putExtra("crop", "true");
        albumIntent.putExtra("aspectX", 1);
        albumIntent.putExtra("aspectY", 1);
        albumIntent.putExtra("outputX", 300);
        albumIntent.putExtra("outputY", 300);
        albumIntent.putExtra("return-data", true);
        startActivityForResult(Intent.createChooser(albumIntent, "Select Image From Gallery"), PICK_IMAGE_REQUEST);
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                //Getting the Bitmap from Gallery
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //Setting the Bitmap to ImageView
                poster_view.setImageBitmap(bitmap);
                image = getStringImage(bitmap);
                Toast.makeText(getApplicationContext(),image, Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void editPerformance(final String title, final String date, final String time, final String genre, final String region,
                                   final String location, final String content, final String email) {

        String tag_string_req = "req_editPerformance";
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_EDIT_PEFORMANCE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    // Check for error node in json
                    if (!error) {
                        Toast.makeText(getApplicationContext(),jObj.getString("path"), Toast.LENGTH_LONG).show();
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() { //error
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                //     hideDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("title", title);
                params.put("date", date);
                params.put("time", time);
                params.put("genre", genre);
                params.put("region", region);
                params.put("location", location);
                params.put("content", content);
                params.put("image", image);
                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void deletePerformance(final int pid) {

        String tag_string_req = "req_deletePerformance";
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_DELETE_PEFORMANCE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    // Check for error node in json
                    if (!error) {
                        Toast.makeText(getApplicationContext(),jObj.getString("path"), Toast.LENGTH_LONG).show();
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() { //error
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                //     hideDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("performance_no", String.valueOf(pid));
                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
}