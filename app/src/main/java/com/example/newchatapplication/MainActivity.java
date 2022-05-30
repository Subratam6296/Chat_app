package com.example.newchatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.newchatapplication.chats.ChatFragment;
import com.example.newchatapplication.requests.ReceivedFriendRequestFragment;
import com.example.newchatapplication.findfrnd.FindFriendFragment;
import com.example.newchatapplication.profile.ProfileActivity;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {


    private TabLayout tabLayout;
    private ViewPager viewPager2;
    private boolean doubleBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tabLayout = findViewById(R.id.tabMain);
        viewPager2 = findViewById(R.id.viewPagerMain);
        setViewPager();

    }


    private void setViewPager(){

        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.chat_layout_tab));
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.frnd_request_tab));
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.frnds_tab));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        Adapter adapter = new Adapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager2.setAdapter(adapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        viewPager2.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.updateProfile:
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);

        }

        return super.onOptionsItemSelected(item);
    }


    class Adapter extends FragmentPagerAdapter {


        public Adapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }


        @Override
        public Fragment getItem(int position) {


            switch (position){

                case 0:
                    ChatFragment chatFragment = new ChatFragment();
                    return chatFragment;
                case 1:
                    return new ReceivedFriendRequestFragment();
                case 2:
                    return new FindFriendFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return tabLayout.getTabCount();
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        if(tabLayout.getSelectedTabPosition()>0){

            tabLayout.selectTab(tabLayout.getTabAt(0));
        }else {

            if(doubleBackPressed){

                finishAffinity();
            }else{
                doubleBackPressed = true;
                Toast.makeText(MainActivity.this, getString(R.string.pres_back_again), Toast.LENGTH_LONG).show();

                android.os.Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackPressed = false;
                    }
                }, 2000);

            }

        }
    }
}