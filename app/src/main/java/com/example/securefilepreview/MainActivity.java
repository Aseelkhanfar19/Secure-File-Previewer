package com.example.securefilepreview;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button sendBtn = findViewById(R.id.sendBtn);
        Button receiveBtn = findViewById(R.id.recieveBtn);

        sendBtn.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SendActivity.class)));


        receiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent receive_page = new Intent(MainActivity.this, ReceiveActivity.class);
                startActivity(receive_page);
            }
        });
    }
}
