package com.company.my.alchoholic;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class mainpage extends AppCompatActivity {
    private ImageButton button1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainpage);
        button1=(ImageButton)findViewById(R.id.imageButton);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), loading.class);
                startActivity(intent);
               // Toast.makeText(getApplicationContext(),"메시지 알림.", Toast.LENGTH_SHORT).show(); // 짧게 표시

            }
        });
    }
}