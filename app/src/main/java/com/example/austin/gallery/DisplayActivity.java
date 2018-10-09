package com.example.austin.gallery;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.File;



public class DisplayActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_picture);
        Intent myIntent = getIntent();
        String path = myIntent.getStringExtra("path"); //get the path of the image from intent
        ImageView view = (ImageView) findViewById(R.id.display);
        view.setImageURI(Uri.fromFile(new File(path))); //display the image in full

        /*set up action bar, allow users return back to main activity*/
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                /*just finish this activity when user click return*/
                finish();
                return true;
                default:
                    return onOptionsItemSelected(item);
        }
    }
}
