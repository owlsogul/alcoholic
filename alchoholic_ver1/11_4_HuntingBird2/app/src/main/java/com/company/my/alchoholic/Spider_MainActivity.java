package com.company.my.alchoholic;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

public class Spider_MainActivity extends AppCompatActivity {
    myDBAdapter dbAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.spider_activity_main);

        // Statusbar 감추기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // getSupportActionBar().hide();
        setTitle("독거미의 나비 사냥");
        dbAdapter = new myDBAdapter(this);
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("점수");

                dbAdapter.open();
                String score = dbAdapter.Open();
                dbAdapter.close();
                if(score.equals("1")){
                    Intent intent = new Intent(getApplicationContext(), success.class);
                    startActivity(intent);
                    finish();}
                else{
                    Intent intent = new Intent(getApplicationContext(), fail.class);
                    startActivity(intent);
                    finish();}

            }
        }, 20000);//10초동안 진행
        //점수 결과 확인 ->점수로 성공 실패 페이지 띄우기
    }

    @Override
    public void onBackPressed() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}

