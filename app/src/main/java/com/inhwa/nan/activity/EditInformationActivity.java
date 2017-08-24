package com.inhwa.nan.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by luvthemoon on 2017. 6. 26..
 */

public class EditInformationActivity extends Activity {

    final Context context = this;
    final int CHOICE = 1;

    private Bitmap bitmap;
    private String image;

    private TextView tv_name;
    private TextView tv_email;
    private EditText edit_nickname;

    private ProgressDialog pDialog;

    Button cancel, modify;
    ImageButton change;
    ImageView profile;

    //Uri photoURI, albumURI = null;
    Boolean album = false;

    private SQLiteHandler db;
    private SessionManager session;

    ByteArrayOutputStream byteArrayOutputStream;

    boolean check = true;

    private int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editinfo);

        session = new SessionManager(getApplicationContext());

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();

        String name = user.get("name");
        String email = user.get("email");
        String sub_name = user.get("sub_name");
        String image = user.get("image");

        profile = (ImageView) findViewById(R.id.imageV);
        modify = (Button) findViewById(R.id.btnModify);
        cancel = (Button) findViewById(R.id.btnCancel2);
        change = (ImageButton) findViewById(R.id.changeImg);

        tv_name = (TextView) findViewById(R.id.nameEt);
        tv_email = (TextView) findViewById(R.id.emailEt);
        edit_nickname = (EditText) findViewById(R.id.nnEt);

        tv_name.setText(name);
        tv_email.setText(email);
        edit_nickname.setText(sub_name);
        Picasso.with(getApplicationContext()).invalidate("");
        Picasso.with(this).load(image).memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE).into(profile);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        byteArrayOutputStream = new ByteArrayOutputStream();

        change.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                albumAction();
            }
        });

        modify.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                editProfile(tv_email.getText().toString(), edit_nickname.getText().toString());
            }

        });

        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_SHORT).show();
                finish();
            }

        });
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
        Log.i("onActivityResult", "CALL");
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri filePath = data.getData();
            try {
                //Getting the Bitmap from Gallery
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //Setting the Bitmap to ImageView
                profile.setImageBitmap(bitmap);
                image = getStringImage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // load Profile to Server
    private void editProfile(final String email, final String nickname) {
        String tag_string_req = "req_editProfile";
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_EDIT_PROFILE, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        if(image!=null) {
                            String imagepath = jObj.getString("imagepath");
                            db.updateUser(email, nickname, imagepath);
                        }

                        else {
                            HashMap<String, String> user = db.getUserDetails();
                            db.updateUser(email, nickname, user.get("image"));
                        }
                        Toast.makeText(getApplicationContext(), "성공적으로 수정되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
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
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                //     hideDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("nickname", nickname);
                if (image!=null) params.put("image", image);
                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
}




