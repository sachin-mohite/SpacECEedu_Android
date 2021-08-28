package com.spacECE.spaceceedu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.spacECE.spaceceedu.Authentication.Account;
import com.spacECE.spaceceedu.Authentication.LoginActivity;
import com.spacECE.spaceceedu.VideoLibrary.Topic;
import com.spacECE.spaceceedu.VideoLibrary.VideoLibrary_Activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawer;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("DEVICE TOKEN","In next line");
        //Android ID:
        //Log.i("DEVICE TOKEN : ",Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID));
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean firstStart = prefs.getBoolean("firstStart", true);
        if (firstStart) {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                Log.w("FCM TOKEN : ", "Fetching FCM registration token failed", task.getException());
                                return;
                            }
                            Log.d("FCM TOKEN : ", task.getResult());
                            sendTokenToServer(task.getResult());
                        }
                    });
            prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstStart", false);
            editor.apply();
        }

        //Firebase Cloud Messaging for PushNotification
        FirebaseMessaging.getInstance().subscribeToTopic("Notify");

        //Bottom navigation bar
        BottomNavigationView bottomNav = findViewById(R.id.Main_Bottom_Navigation);
        bottomNav.setOnItemSelectedListener(navListener);
        //Navigation Drawer
        drawer = findViewById(R.id.Main_NavView_drawer);
        //Toolbar support for navigationDrawer
        toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //NavigationDrawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //toolbar.setTitle("Hi "+account.getUsername()+" !");


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.Main_Fragment_layout,
                    new FragmentMain()).commit();
        }

        GetLists getLists= new GetLists();
        getLists.execute();
    }

    private void sendTokenToServer(String token) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                UsefulFunctions.UsingGetAPI("http://3.109.14.4/ConsultUs/api_token?email=null"+"&token="+token);
            }
        });
        thread.start();

    }


    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else{
        super.onBackPressed();
        }
    }


    NavigationBarView.OnItemSelectedListener navListener =
            new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            selectedFragment = new FragmentMain();
                            break;
                        case R.id.nav_profile:
                            selectedFragment = new FragmentProfile();
                            break;
                        case R.id.nav_help:
                            selectedFragment = new FragmentAbout();
                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.Main_Fragment_layout,
                            selectedFragment).commit();

                    return true;
                }
            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_main_activity,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.button_signOut:
                //Logout
                signOut();
                return true;
            case R.id.button_signIn:
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void signOut() {
        LoginActivity.ACCOUNT= new Account(null, null, null);
    }


    class GetLists extends AsyncTask<String, Void, JSONObject> {
        final private JSONObject[] apiCall = {null};

        @Override
        protected JSONObject doInBackground(String... strings) {
            try {
                apiCall[0] = UsefulFunctions.UsingGetAPI("http://3.109.14.4/SpacTube/api_all?uid=1&type=all");
                Log.i("Object Obtained: ", apiCall[0].toString());

                JSONArray jsonArray = null;
                JSONArray recentJsonArray = null;
                JSONArray trendingJsonArray = null;
                try {
                    jsonArray = apiCall[0].getJSONArray("data");
                    recentJsonArray = apiCall[0].getJSONArray("data_recent");
                    trendingJsonArray = apiCall[0].getJSONArray("data_trending");
                    Log.i("API : ", apiCall[0].toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i("JSON Error: ", "Key not found in JSON");
                }

                try {
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject response_element = new JSONObject(String.valueOf(jsonArray.getJSONObject(i)));

                        Topic newTopic = new Topic(response_element.getString("status"), response_element.getString("title"),
                                response_element.getString("v_id"), response_element.getString("filter"),
                                response_element.getString("length"), response_element.getString("v_url"),
                                response_element.getString("v_date"), response_element.getString("v_uni_no"),
                                response_element.getString("desc"), response_element.getString("cntlike"),
                                response_element.getString("cntdislike"), response_element.getString("views"),
                                response_element.getString("cntcomment"));
                        VideoLibrary_Activity.topicList.add(newTopic);
                        if (response_element.getString("status").equalsIgnoreCase("created")) {
                            VideoLibrary_Activity.paidTopicList.add(newTopic);
                        } else {
                            VideoLibrary_Activity.freeTopicList.add(newTopic);
                        }
                    }

                    Log.i("Paid : ", VideoLibrary_Activity.paidTopicList.toString() + "///" + VideoLibrary_Activity.paidTopicList.size());
                    Log.i("Free : ", VideoLibrary_Activity.freeTopicList.toString() + "///" + VideoLibrary_Activity.freeTopicList.size());

                    for (int i = 0; i < recentJsonArray.length(); i++) {

                        JSONObject response_element = new JSONObject(String.valueOf(jsonArray.getJSONObject(i)));

                        Topic newTopic = new Topic(response_element.getString("status"), response_element.getString("title"),
                                response_element.getString("v_id"), response_element.getString("filter"),
                                response_element.getString("length"), response_element.getString("v_url"),
                                response_element.getString("v_date"), response_element.getString("v_uni_no"),
                                response_element.getString("desc"), response_element.getString("cntlike"),
                                response_element.getString("cntdislike"), response_element.getString("views"),
                                response_element.getString("cntcomment"));
                        VideoLibrary_Activity.recentTopicList.add(newTopic);
                    }
                    Log.i("Recent : ", VideoLibrary_Activity.recentTopicList.toString());

                    for (int i = 0; i < trendingJsonArray.length(); i++) {

                        JSONObject response_element = new JSONObject(String.valueOf(jsonArray.getJSONObject(i)));

                        Topic newTopic = new Topic(response_element.getString("status"), response_element.getString("title"),
                                response_element.getString("v_id"), response_element.getString("filter"),
                                response_element.getString("length"), response_element.getString("v_url"),
                                response_element.getString("v_date"), response_element.getString("v_uni_no"),
                                response_element.getString("desc"), response_element.getString("cntlike"),
                                response_element.getString("cntdislike"), response_element.getString("views"),
                                response_element.getString("cntcomment"));
                        VideoLibrary_Activity.topicList.add(newTopic);
                        VideoLibrary_Activity.trendingTopicList.add(newTopic);
                    }
                    Log.i("TRENDING : ", VideoLibrary_Activity.trendingTopicList.toString());

                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                    Log.i("JSON Object : ", "Key not found");
                }
                VideoLibrary_Activity.ArrayDownloadCOMPLETED[0] = true;
                Log.i("VIDEOS:::::", VideoLibrary_Activity.topicList.toString());

                return null;
            }    catch (RuntimeException runtimeException){
                Log.i("RUNTIME EXCEPTION:::", "Server did not respons");
        }
            return null;
        }
}
}
