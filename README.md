AndroidYoutubeOverlay
=====================

Library for handling Youtube Player in ListView. 

Features:
---------

* Allows seamless transition betwean portrait and landscape (without rebufering) 
* Allows playback of one video at time
* Attaches it self to view in row and scrolls with it
* Works from API level 17 (like YT API)

Todo:
-----

* Support for views other than AbsListView

How to Use:
-----------

1. Add following line to your Activity declaration in AndroidManifest.xml
```
  android:configChanges="orientation|keyboardHidden|screenSize"
```

2. Use builder to create Fragment instance (Remember to set proper YT_DEVELOPER_KEY)
```
  ytPlayer = YoutubeOverlayFragment.newBuilder(YT_DEVELOPER_KEY, this).setListId(android.R.id.list).buildAndAdd();
```

3. Handle view click
```
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
```

4. Override onBackPressed, so fragment can handle back button while in landscape
```
  @Override
  public void onBackPressed() {
      if (ytPlayer.onBackPressed()) {
          // handled by fragment
      } else {
          super.onBackPressed();
      }
  }
  ```
