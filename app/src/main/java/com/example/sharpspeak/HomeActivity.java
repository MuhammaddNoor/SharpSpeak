package com.example.sharpspeak;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import com.google.android.material.button.MaterialButton;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // ربط الزر من التصميم
        MaterialButton startChatButton = findViewById(R.id.startChatButton);

        // إعطاء أمر الانتقال لصفحة المحادثة عند الضغط
        startChatButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);

            // نستخدم finish() لكي لا يعود المستخدم لصفحة البدء إذا ضغط زر الرجوع بهاتفه
            finish();
        });
    }
}