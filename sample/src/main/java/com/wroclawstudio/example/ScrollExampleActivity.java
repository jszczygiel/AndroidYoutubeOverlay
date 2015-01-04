package com.wroclawstudio.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.wroclawstudio.ytoverlay.YoutubeOverlayFragment;

/**
 * Created by jakubszczygiel on 04/01/15.
 */
public class ScrollExampleActivity extends Activity {

    private static final String VIDEO_ID = "2b03hoW0TKc";

    private YoutubeOverlayFragment ytPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll_exmaple);
        ytPlayer = YoutubeOverlayFragment.newBuilder(Constants.YT_DEVELOPER_KEY, this).setScrollableViewId(R.id.scrollView).buildAndAdd();

        final View img = findViewById(R.id.rowMainImage);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ytPlayer.onClick(img, VIDEO_ID);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (ytPlayer.onBackPressed()) {
            // handled by fragment
        } else {
            super.onBackPressed();
        }
    }
}
