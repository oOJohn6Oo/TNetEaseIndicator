package com.example.neteaseindicator;

import android.graphics.Outline;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class Test extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        ImageView imageView = new ImageView(this);
        imageView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0,0, view.getWidth(),view.getHeight(),5);
            }
        });
    }
}
