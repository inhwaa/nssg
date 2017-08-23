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

public class UploadPerformanceActivity extends AppCompatActivity{

    public static String s_genre = "";
    public static String s_region = "";
    public static String s_email;
    private static final String TAG = UploadPerformanceActivity.class.getSimpleName();

    private EditText edtSetTitle;

    private TextView date_view, time_view;

    private ImageView poster_view;
    private ImageButton btnSetImage;
    private Button btnSetDate;
    private Button btnSetTime;
    private Spinner spinnerSetGenre, spinnerSetRegion;

    private EditText edtKeyword;

    private EditText edtIntroPerformance;

    private TextView mPlaceDetailsText;

    private Button btnUpload;
    private Button btnCancel, btnMap;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_performance);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();
        s_email = user.get("email").toString();

        Date today = new Date ();
        Calendar cal = Calendar.getInstance ( );
        cal.setTime (today);

        year = cal.get ( cal.YEAR );
        month = cal.get ( cal.MONTH );
        day = cal.get ( cal.DATE ) ;
        hour = cal.get ( cal.HOUR_OF_DAY ) ;
        min = cal.get ( cal.MINUTE );

        edtSetTitle = (EditText) findViewById(R.id.edtSetTitle);
        poster_view = (ImageView) findViewById(R.id.btnSetImage);
        btnSetImage = (ImageButton) findViewById(R.id.imgbtn);
        date_view = (TextView) findViewById(R.id.date_tv);
        time_view = (TextView) findViewById(R.id.time_tv);
        btnSetDate = (Button) findViewById(R.id.btnSetDate);
        btnSetTime = (Button) findViewById(R.id.btnSetTime);
        spinnerSetGenre = (Spinner) findViewById(R.id.spinnerSetGenre);
        spinnerSetRegion = (Spinner) findViewById(R.id.spinnerSetRegion);

        //edtSetLocation = (EditText) findViewById(R.id.edtSetLocation);
        edtIntroPerformance = (EditText) findViewById(R.id.edtIntroPerformance);

        btnUpload = (Button) findViewById(R.id.btnUpload);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnMap = (Button) findViewById(R.id.findMap_button);

        //장르선택
        spinnerSetGenre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                s_genre = spinnerSetGenre.getSelectedItem().toString();
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //지역선택
        spinnerSetRegion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                s_region = spinnerSetRegion.getSelectedItem().toString();
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        btnSetImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                albumAction();
            }
        });
        btnSetDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "공연날짜를 선택하세요.", Toast.LENGTH_SHORT).show();
                showDialog(DIALOG_DATE);
            }
        });
        btnSetTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "공연시간을 선택하세요.", Toast.LENGTH_LONG).show();
                showDialog(DIALOG_TIME);
            }
        });
        //공연 업로드
        btnUpload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String title = edtSetTitle.getText().toString();
                String date = date_view.getText().toString();
                String time = time_view.getText().toString();
                String genre = s_genre;
                String region = s_region;
                String location = "공연장주소";
                // String location = mPlaceDetailsText.getText().toString();
                String content = edtIntroPerformance.getText().toString();
                String email = s_email;
                if (title.matches("")||date.matches("날짜 선택")||time.matches("시간 선택")||content.matches("")) {
                    Toast.makeText(getApplicationContext(), "모든 항목을 입력하세요", Toast.LENGTH_LONG).show();
                } else {
                    uploadPerformance(title, date, time, genre, region, location, content, email);
                    Toast.makeText(getApplicationContext(), "업로드.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //openAutocompleteActivity();
                Intent intent2 = new Intent(UploadPerformanceActivity.this, MapsActivityCurrentPlace.class);
                startActivityForResult(intent2, 0);
            }
        });

        // Retrieve the TextViews that will display details about the selected place.
        mPlaceDetailsText = (TextView) findViewById(R.id.place_details);

    } //end of oncreate

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DATE:
                dpd = new DatePickerDialog(UploadPerformanceActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Toast.makeText(getApplicationContext(),
                            year + "년 " + (month + 1) + "월 " + dayOfMonth + "일을 선택했습니다",
                            Toast.LENGTH_SHORT).show();
                        s_year = year;
                        s_month = month + 1;
                        s_day = dayOfMonth;
                        date_view.setText(s_year + "/" + s_month + "/" + s_day);
                    }
                        }, year, month, day); //오늘 날짜로 초기화 하고싶다
                return dpd;

            case DIALOG_TIME:
                TimePickerDialog tpd = new TimePickerDialog(UploadPerformanceActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Toast.makeText(getApplicationContext(),
                                    hourOfDay + "시 " + minute + "분을 선택했습니다",
                                    Toast.LENGTH_SHORT).show();
                            s_hour = hourOfDay;
                            s_min = minute;
                            time_view.setText(s_hour + ":" + s_min);
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
               // Toast.makeText(getApplicationContext(),image, Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (resultCode == 0) {
            if (data != null) {
                place_info = data.getStringExtra("place_info");
                // Format the place's details and display them in the TextView.
                mPlaceDetailsText.setText(data.getStringExtra("place_info"));
            }
        }
    }

    private void uploadPerformance(final String title, final String date, final String time, final String genre, final String region,
                                   final String location, final String content, final String email) {

        String tag_string_req = "req_uploadPerformance";
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_UPLOAD_PERFORMANCE, new Response.Listener<String>() {

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
                params.put("email", email);
                params.put("image", image);
                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
}