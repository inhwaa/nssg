package com.inhwa.nan.activity;

import android.app.Activity;

/**
 * Created by Inhwa_ on 2017-08-24.
 */

import android.content.Intent;
import android.os.Bundle;


public class Splash_Activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Thread.sleep(4000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        startActivity(new Intent(this,LoginActivity.class));
        finish();
    }
}
