package com.wroclawstudio.ytoverlay;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ScrollView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.primitives.Ints;

import java.util.ArrayList;

public class YoutubeOverlayFragment extends YouTubePlayerFragment implements YouTubePlayer.OnInitializedListener {

    private static final String HIDEABLE_VIEWS = "HIDEABLE_VIEWS";
    private static final String SCROLLABLE_VIEW_ID = "SCROLLABLE_VIEW_ID";
    private static final String VIDEO_ID = "VIDEO_ID";
    private static final String YT_DEVELOPER_KEY = "YT_DEVELOPER_KEY";

    private YouTubePlayer player; // yt player
    private String videoId; // yt video id
    private boolean fullscreen; // holds current player playback mode
    private String ytKey;

    private View videoContainer; // video container
    private View attachedImgContainer; // img container to which video container is attached
    private Display display; // used for getting screen size
    private View attachedListView; // listView to which fragment is attached
    private int[] hideableViews; // videoContainer ids that are hidden in fullscreen playback
    private View actionBarView; // actionBarView

    private YoutubeOverlayFragment() {
        setRetainInstance(true);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogHelper.logMessage("onCreate: " + savedInstanceState);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.videoId = getArguments().getString(VIDEO_ID, null);
            this.hideableViews = getArguments().getIntArray(HIDEABLE_VIEWS);
            this.attachedListView = getActivity().findViewById(getArguments().getInt(SCROLLABLE_VIEW_ID));
            this.ytKey = getArguments().getString(YT_DEVELOPER_KEY);

        }
        initialize(ytKey, this);
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        display = wm.getDefaultDisplay();
        actionBarView = getActionBarView(getActivity());
    }

    private View getActionBarView(Activity activity) {
        Window window = activity.getWindow();
        View v = window.getDecorView();
        int resId = getResources().getIdentifier("action_bar_container", "id", "android");
        return v.findViewById(resId);
    }

    /**
     * Set fragment color to black and hides it after creation
     *
     * @return fragemnt videoContainer container
     */
    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        videoContainer = super.onCreateView(layoutInflater, viewGroup, bundle);
        if (videoContainer != null) {
            videoContainer.setBackgroundColor(Color.BLACK);
            videoContainer.setVisibility(View.GONE);
        }
        return videoContainer;
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, final YouTubePlayer player, boolean restored) {
        LogHelper.logMessage("onInitializationSuccess: " + restored + " videoId: " + videoId);
        this.player = player;
        player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI);
        player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
        player.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {

            @Override
            public void onFullscreen(boolean fullscreen) {
                YoutubeOverlayFragment.this.fullscreen = fullscreen;
            }
        });
        if (!restored && videoId != null) {
            player.loadVideo(videoId);
        }
    }

    /**
     * Handles screen orientation change. Assigns proper size and position of video container based on screen orientation.
     *
     * @param newConfig new screen configuration
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LogHelper.logMessage("onConfigurationChanged: is landscape" + (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE));
        super.onConfigurationChanged(newConfig);
        if (videoId != null && player != null) {
            ViewGroup.LayoutParams params = videoContainer.getLayoutParams();
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (getActivity() != null) {
                    actionBarView.setVisibility(View.GONE);
                    for (int id : hideableViews) {
                        getActivity().findViewById(id).setVisibility(View.GONE);
                    }
                }
                Point size = new Point();
                display.getRealSize(size);
                params.height = size.y;
                params.width = size.x;
                LogHelper.logMessage("x:" + size.x + " y:" + size.y);
                videoContainer.setX(0);
                videoContainer.setY(0);
                player.setFullscreen(true);

            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (getActivity() != null) {
                    actionBarView.setVisibility(View.VISIBLE);
                    for (int id : hideableViews) {
                        getActivity().findViewById(id).setVisibility(View.VISIBLE);
                    }
                }
                setVideoPostion(this.attachedImgContainer);
                player.setFullscreen(false);
            }
            videoContainer.setLayoutParams(params);
        }

    }


    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult result) {
        LogHelper.logMessage("onInitializationFailure:" + result.name());
        this.player = null;
    }

    /**
     * Exit fullscreen mode of yt player
     */
    public void exitFullScreen() {
        player.setFullscreen(false);
    }

    /**
     * @return returns if fragment is in fullscreen mode
     */
    public boolean isFullScreen() {
        return fullscreen;
    }

    /**
     * Starts playback of passed youtubeID if player is available and youtubeID is not null
     *
     * @param videoId yt video id to be played
     */
    public void setVideoId(String videoId) {
        this.videoId = videoId;
        if (player != null && this.videoId != null) {
            player.loadVideo(this.videoId);
            videoContainer.setVisibility(View.VISIBLE);
        } else {
            ViewGroup.LayoutParams params = videoContainer.getLayoutParams();
            params.height = 0;
            params.width = 0;
            videoContainer.setLayoutParams(params);
            videoContainer.setVisibility(View.GONE);
        }
    }

    /**
     * Attaches to img container handling img container scrolling, which updates video container position.
     *
     * @param attachedContainer img container to which video container is going to be attached
     * @param position          list item position of img container
     */
    private void attachToView(final View attachedContainer, final int position) {
        this.attachedImgContainer = attachedContainer;
        setVideoPostion(this.attachedImgContainer);
        this.attachedImgContainer.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (!fullscreen) {
                    setVideoPostion(YoutubeOverlayFragment.this.attachedImgContainer);
                }
            }
        });
        if (attachedListView instanceof AbsListView) {
            ((AbsListView) attachedListView).setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if ((position < (firstVisibleItem + visibleItemCount)) && (position >= firstVisibleItem)) {
                        YoutubeOverlayFragment.this.videoContainer.setVisibility(View.VISIBLE);
                    } else {
                        YoutubeOverlayFragment.this.videoContainer.setVisibility(View.GONE);
                    }
                }
            });
        }

    }

    private void attachToView(View attachedContainer) {
        this.attachedImgContainer = attachedContainer;
        setVideoPostion(this.attachedImgContainer);
        this.attachedImgContainer.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (!fullscreen) {
                    setVideoPostion(YoutubeOverlayFragment.this.attachedImgContainer);
                }
            }
        });
    }

    /**
     * Updates video container position based on attachedView current position
     *
     * @param attachedView videoContainer based on which video container position is updated
     */
    private void setVideoPostion(View attachedView) {
        int postion[] = new int[2];
        attachedView.getLocationOnScreen(postion);
        videoContainer.setX(postion[0]);
        videoContainer.setY(postion[1] - getStatusBarHeight() - getActionBarHeight());
        ViewGroup.LayoutParams params = videoContainer.getLayoutParams();
        params.height = attachedView.getHeight();
        params.width = attachedView.getWidth();
        videoContainer.setLayoutParams(params);
    }

    /**
     * Returns ActionBarHeight
     *
     * @return ActionBarHeight
     */
    private int getActionBarHeight() {
        if (actionBarView != null) {
            return actionBarView.getHeight();
        }
        return 0;
    }

    /**
     * Returns status bar height
     *
     * @return status bar height
     */
    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * Handles exiting from yt full screen videoContainer
     *
     * @param activity activity in which on backpressed is handled
     * @return returns if action was handled
     */
    public static boolean onBackPressed(Activity activity) {
        if (activity != null) {
            FragmentManager fragmentManager = activity.getFragmentManager();
            YoutubeOverlayFragment yt = (YoutubeOverlayFragment) fragmentManager.findFragmentByTag(YoutubeOverlayFragment.class.getName());
            if (yt != null) {
                yt.onBackPressed();
            }
        }
        return false;
    }

    /**
     * Handles exiting from yt full screen videoContainer
     *
     * @return returns if action was handled
     */
    public boolean onBackPressed() {
        if (isFullScreen()) {
            exitFullScreen();
            return true;
        }
        return false;
    }

    /**
     * Performs video playback of passed videoId in fragment video container.
     * Video container is attached to img container of an row item.
     *
     * @param activity activity context in which adapter is set
     * @param img      img container to which video container is going to be attached
     * @param videoId  yt video id which is going to be played
     * @param position list item position of img container
     */
    public static void onClick(Activity activity, View img, String videoId, int position) {
        if (activity != null) {
            FragmentManager fragmentManager = activity.getFragmentManager();
            YoutubeOverlayFragment yt = (YoutubeOverlayFragment) fragmentManager.findFragmentByTag(YoutubeOverlayFragment.class.getName());
            if (yt != null) {
                yt.onClick(img, videoId, position);
            }
        }
    }

    /**
     * Used for handling onclick in AbsListView. Overlay takes size of view passed.
     *
     * @param videoId  yt video Id
     * @param view     view to which yt overlay attaches it self
     * @param position position in list for handling player visibility
     */
    public void onClick(View view, String videoId, int position) {
        setVideoId(videoId);
        attachToView(view, position);
    }

    /**
     * Used for handling onclick in ScrollView. Overlay takes size of view passed.
     *
     * @param videoId yt video Id
     * @param view    view to which yt overlay attaches it self
     */
    public void onClick(View view, String videoId) {
        setVideoId(videoId);
        attachToView(view);
    }

    public static Builder newBuilder(String ytKey, Activity activity) {
        return new Builder(ytKey, activity);
    }

    public static class Builder {

        private final String ytKey;
        private int scrollableViewId;
        private ArrayList<Integer> hideableViews;
        private Activity activity;

        public Builder(String ytKey, Activity activity) {
            this.ytKey = ytKey;
            this.activity = activity;
            this.hideableViews = new ArrayList<>();
        }

        /**
         * Sets id of scrollable view to which overlay will attach. Required
         *
         * @param scrollableViewId viewId to hide
         * @return returns Builder
         */
        public Builder setScrollableViewId(int scrollableViewId) {
            this.scrollableViewId = scrollableViewId;
            return this;
        }

        /**
         * View id that is at the same hierarchy level as yt overlay. Will be hidden while player is in landscape mode
         * Optional
         *
         * @param viewId viewId to hide
         * @return returns Builder
         */
        public Builder addHideableView(int viewId) {
            hideableViews.add(viewId);
            return this;
        }

        /**
         * Builds Fragment and adds it to view hierarchy
         * @return returns YoutubeOverlayFragment instance that was added to hierarchy
         */
        public YoutubeOverlayFragment buildAndAdd() {
            Preconditions.checkNotNull(activity);
            Preconditions.checkArgument(!Strings.isNullOrEmpty(ytKey));
            Preconditions.checkArgument(scrollableViewId != 0);

            FragmentManager fragmentManager = activity.getFragmentManager();
            View scrollableView = activity.findViewById(scrollableViewId);
            YoutubeOverlayFragment yt = (YoutubeOverlayFragment) fragmentManager.findFragmentByTag(YoutubeOverlayFragment.class.getName());
            if (yt != null) {
                fragmentManager.beginTransaction().remove(yt).commit();
                fragmentManager.executePendingTransactions();
            }
            yt = new YoutubeOverlayFragment();
            Bundle bundle = new Bundle();
            bundle.putIntArray(YoutubeOverlayFragment.HIDEABLE_VIEWS, Ints.toArray(hideableViews));
            bundle.putString(YoutubeOverlayFragment.YT_DEVELOPER_KEY, ytKey);
            bundle.putInt(SCROLLABLE_VIEW_ID, scrollableViewId);
            yt.setArguments(bundle);
            fragmentManager.beginTransaction().add(((View) scrollableView.getParent()).getId(), yt, YoutubeOverlayFragment.class.getName()).commit();
            return yt;
        }

    }
}