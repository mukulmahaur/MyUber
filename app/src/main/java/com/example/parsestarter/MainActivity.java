package com.example.parsestarter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MainActivity extends AppCompatActivity {

    public void redirectActivity(){
        if(ParseUser.getCurrentUser().get("riderOrDriver").equals("rider")){
            Intent intent = new Intent(this,RiderActivity.class);
            startActivity(intent);
        }
        else if(ParseUser.getCurrentUser().get("riderOrDriver").equals("driver")){
            Intent intent = new Intent(this,ViewRequestsActivity.class);
            startActivity(intent);
        }
    }

    public void getStarted(View view){
        final Switch thisswitch = (Switch)findViewById(R.id.usertypeswitch);
        Log.i("switch value",String.valueOf(thisswitch.isChecked()));
        ParseUser.getCurrentUser().put("riderOrDriver",thisswitch.isChecked()?"driver":"rider");
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Log.i("logging in as",thisswitch.isChecked()?"driver":"rider");
                redirectActivity();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        if(ParseUser.getCurrentUser()== null){
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if(e==null){
                        Log.i("anonymous","anonymous");
                    }else{
                        Log.i("notanonymous","notanonymous");

                    }
                }
            });
        }else{
            if(ParseUser.getCurrentUser().get("riderOrDriver")!=null){
                Log.i("logging in as", String.valueOf(ParseUser.getCurrentUser().get("riderOrDriver")));
                redirectActivity();
            }
        }
    }
}
