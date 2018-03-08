package com.cjlee.lottecard.lottecard;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class HomeActivity extends AppCompatActivity{
    /* IP adress */
    public static final String IP_ADDRESS = "10.10.17.122";

    private static final String TAG = HomeActivity.class.getSimpleName();

    Button btnRegister, btnMemberlist;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        btnRegister = findViewById(R.id.home_btn_register);
        btnMemberlist = findViewById(R.id.home_btn_memberlist);

        ButtonHandler buttonHandler = new ButtonHandler();

        btnRegister.setOnClickListener(buttonHandler);
        btnMemberlist.setOnClickListener(buttonHandler);
    }

    class ButtonHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.home_btn_register:
                    Intent intentRegister = new Intent(HomeActivity.this, RegisterActivity.class);
                    startActivity(intentRegister);
                    break;
                case R.id.home_btn_memberlist:
                    Log.v(TAG, "고객 명단 버튼 클릭");

                    Intent intentMemberlist = new Intent(HomeActivity.this, MemberListActivity.class);
                    startActivity(intentMemberlist);
                    break;
            }
        }
    }
}
