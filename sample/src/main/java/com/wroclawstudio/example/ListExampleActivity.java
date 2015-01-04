package com.wroclawstudio.example;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.wroclawstudio.ytoverlay.YoutubeOverlayFragment;

/**
 * Created by jakubszczygiel on 15/11/14.
 */
public class ListExampleActivity extends Activity {


    public String[] youtubeUrls = new String[]{
            "1KOaT1vdLmc",
            "PZbkF-15ObM",
            "tvY7Nw1i6Kw",
            "YwmbbcMHiQ0",
            "2b03hoW0TKc",
    };
    private YoutubeOverlayFragment ytPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_example);

        ListView list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(new YoutubeListAdapter(this, youtubeUrls));

        ytPlayer = YoutubeOverlayFragment.newBuilder(Constants.YT_DEVELOPER_KEY, this).setScrollableViewId(android.R.id.list).buildAndAdd();

    }

    class YoutubeListAdapter extends ArrayAdapter<String> {

        private final LayoutInflater inflater;

        public YoutubeListAdapter(Context context, String[] list) {
            super(context, 0, list);
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.row_main, parent, false);
                holder = new ViewHolder();
                holder.rowMainImage = convertView.findViewById(R.id.rowMainImage);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final String videoId = getItem(position);
            holder.rowMainImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ytPlayer.onClick(holder.rowMainImage, videoId, position);
                }
            });

            return convertView;
        }

        class ViewHolder {
            View rowMainImage;
        }
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
