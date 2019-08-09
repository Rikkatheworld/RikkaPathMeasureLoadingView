package com.rikkatheworld.rikkapathmeasureanimation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.btn);
        final RikkaLoadingView loadingView = findViewById(R.id.loading_view);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingView.startAnim();
            }
        });
    }
}
