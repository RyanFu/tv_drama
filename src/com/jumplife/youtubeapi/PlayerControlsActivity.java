/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jumplife.youtubeapi;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStyle;
import com.google.android.youtube.player.YouTubePlayer.PlaylistEventListener;
import com.google.android.youtube.player.YouTubePlayerView;
import com.jumplife.tvdrama.R;

import android.content.Intent;
import android.net.Uri;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple YouTube Android API demo application demonstrating the use of {@link YouTubePlayer}
 * programmatic controls.
 */
public class PlayerControlsActivity extends YouTubeFailureRecoveryActivity{

	private static final String KEY_CURRENTLY_SELECTED_ID = "currentlySelectedId";

	private static String[] ENTRIES;
	private YouTubePlayerView youTubePlayerView;
	private YouTubePlayer player;
	private TextView stateText;
	private TextView partText;
	private ImageButton playButton;
	private ImageButton preButton;
	private ImageButton nextButton;
	private Boolean isplay = true;

	private MyPlaylistEventListener playlistEventListener;
	private MyPlayerStateChangeListener playerStateChangeListener;
	private MyPlaybackEventListener playbackEventListener;

	private int currentlySelectedPosition;
	private String currentlySelectedId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_controls);
		initView();
		setListener();
	}

	public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        	player.setFullscreen(true);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){        
        	player.setFullscreen(false);
        }
    }
	
	public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
			boolean wasRestored) {
		if (player == null) {
			Toast.makeText(PlayerControlsActivity.this, getResources().getString(R.string.error_player_other), Toast.LENGTH_LONG).show();
	    	setControlsEnabled(false);
    		PlayerControlsActivity.this.finish();
    		Uri uri = Uri.parse(
    				"http://www.youtube.com/watch?v=" + ENTRIES[currentlySelectedPosition]);
    		Intent it = new Intent(Intent.ACTION_VIEW, uri);
    		startActivity(it);
		} else {
			this.player = player;

		    playlistEventListener = new MyPlaylistEventListener();
		    playerStateChangeListener = new MyPlayerStateChangeListener();
		    playbackEventListener = new MyPlaybackEventListener();
			player.setPlaylistEventListener(playlistEventListener);
			player.setPlayerStateChangeListener(playerStateChangeListener);
			player.setPlaybackEventListener(playbackEventListener);	    
		    player.setPlayerStyle(PlayerStyle.DEFAULT);
			if (!wasRestored) {
				playVideoAtSelection();
			}
			setControlsEnabled(true);
		}
	}

	private void initView() {
		Bundle extras = getIntent().getExtras();
        
		ENTRIES = extras.getString("youtube_ids").split(",");
		currentlySelectedPosition = extras.getInt("youtube_index");
        
		youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_view);
		playButton = (ImageButton) findViewById(R.id.play_button);
		preButton = (ImageButton) findViewById(R.id.pre_button);
		nextButton = (ImageButton) findViewById(R.id.next_button);
		stateText = (TextView) findViewById(R.id.state_text);
	    partText = (TextView) findViewById(R.id.section_id);
		partText.setText("Part " + (currentlySelectedPosition+1));
		setPlayButtonView();
		
		youTubePlayerView.initialize(DeveloperKey.DEVELOPER_KEY, this);
	}
	
	private void setPlayButtonView() {
		if(isplay)
			playButton.setBackgroundResource(R.drawable.imagebutton_stop);
		else
			playButton.setBackgroundResource(R.drawable.imagebutton_play);
	}
	
	private void setListener() {
		playButton.setOnClickListener(new OnClickListener(){
	    	public void onClick(View v) {
	    		isplay = !isplay;
	    		setPlayButtonView();
	    		if(isplay)
	    			player.play();
	    		else
	    			player.pause();
	    	}
	    });
	    preButton.setOnClickListener(new OnClickListener(){
	    	public void onClick(View v) {
	    		currentlySelectedPosition = currentlySelectedPosition - 1;
	    		if(currentlySelectedPosition < 0)
	    			currentlySelectedPosition = ENTRIES.length - 1;
				playVideoAtSelection();
	    	}
	    });
	    nextButton.setOnClickListener(new OnClickListener(){
	    	public void onClick(View v) {
	    		currentlySelectedPosition = currentlySelectedPosition + 1;
	    		if(currentlySelectedPosition >= ENTRIES.length)
	    			currentlySelectedPosition = 0;
				playVideoAtSelection();
	    	}
	    });
	}
	
	@Override
	protected YouTubePlayer.Provider getYouTubePlayerProvider() {
		return youTubePlayerView;
	}

	private void playVideoAtSelection() {
		if (ENTRIES[currentlySelectedPosition] != currentlySelectedId && player != null) {
			currentlySelectedId = ENTRIES[currentlySelectedPosition];
			partText.setText("Part " + (currentlySelectedPosition+1));
			player.cueVideo(ENTRIES[currentlySelectedPosition]);
			isplay = true;
			setPlayButtonView();
			player.play();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		state.putString(KEY_CURRENTLY_SELECTED_ID, currentlySelectedId);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		currentlySelectedId = state.getString(KEY_CURRENTLY_SELECTED_ID);
	}

	private void updateText() {
		stateText.setText(String.format("Current state: %s %s %s",
				playerStateChangeListener.playerState, playbackEventListener.playbackState,
				playbackEventListener.bufferingState));
	}

	private void log(String message) {
		//logString.append(message + "\n");
		//eventLog.setText(logString);
	}

	private void setControlsEnabled(boolean enabled) {
		playButton.setEnabled(enabled);
	}

	private String formatTime(int millis) {
		int seconds = millis / 1000;
		int minutes = seconds / 60;
		int hours = minutes / 60;

		return (hours == 0 ? "" : hours + ":")
				+ String.format("%02d:%02d", minutes % 60, seconds % 60);
	}

	private String getTimesText() {
		int currentTimeMillis = player.getCurrentTimeMillis();
		int durationMillis = player.getDurationMillis();
		return String.format("(%s/%s)", formatTime(currentTimeMillis), formatTime(durationMillis));
	}

	private final class MyPlaylistEventListener implements PlaylistEventListener {
		public void onNext() {
			log("NEXT VIDEO");
		}
	
	    public void onPrevious() {
	    	log("PREVIOUS VIDEO");
	    }
	
	    public void onPlaylistEnded() {
	    	log("PLAYLIST ENDED");
		}
	
	}

	private final class MyPlaybackEventListener implements PlaybackEventListener {
	    String playbackState = "NOT_PLAYING";
	    String bufferingState = "";
	    public void onPlaying() {
	      playbackState = "PLAYING";
	      isplay = true;
	      setPlayButtonView();
	      updateText();
	      log("\tPLAYING " + getTimesText());
	    }

	    public void onBuffering(boolean isBuffering) {
	      bufferingState = isBuffering ? "(BUFFERING)" : "";
	      updateText();
	      log("\t\t" + (isBuffering ? "BUFFERING " : "NOT BUFFERING ") + getTimesText());
	    }

	    public void onStopped() {
	      playbackState = "STOPPED";
	      updateText();
	      log("\tSTOPPED");
	    }

	    public void onPaused() {
	      playbackState = "PAUSED";
	      isplay = false;
	      setPlayButtonView();
	      updateText();
	      log("\tPAUSED " + getTimesText());
	    }

	    public void onSeekTo(int endPositionMillis) {
	      log(String.format("\tSEEKTO: (%s/%s)",
	          formatTime(endPositionMillis),
	          formatTime(player.getDurationMillis())));
	    }
	}

	private final class MyPlayerStateChangeListener implements PlayerStateChangeListener {
		String playerState = "UNINITIALIZED";

		public void onLoading() {
			playerState = "LOADING";
			updateText();
			log(playerState);
		}

		public void onLoaded(String videoId) {
			playerState = String.format("LOADED %s", videoId);
			updateText();
			log(playerState);
			player.play();
		}

		public void onAdStarted() {
			playerState = "AD_STARTED";
			updateText();
			log(playerState);
		}

		public void onVideoStarted() {
			playerState = "VIDEO_STARTED";
			updateText();
			log(playerState);
		}

	    public void onVideoEnded() {
	    	playerState = "VIDEO_ENDED";
	    	updateText();
	    	log(playerState);
	    	currentlySelectedPosition = currentlySelectedPosition + 1;
    		if(currentlySelectedPosition >= ENTRIES.length)
    			currentlySelectedPosition = 0;
			playVideoAtSelection();
	    }

	    public void onError(ErrorReason reason) {
	    	playerState = "ERROR (" + reason + ")";
	    	/*if (reason == ErrorReason.UNEXPECTED_SERVICE_DISCONNECTION) {
	    		// When this error occurs the player is released and can no longer be used.
	    		player = null;
	    		setControlsEnabled(false);
	    	} else if(reason == ErrorReason.EMBEDDING_DISABLED) {
	    		PlayerControlsActivity.this.finish();
	    		Uri uri = Uri.parse(
	    				"http://www.youtube.com/watch?v=" + ENTRIES[currentlySelectedPosition]);
        		Intent it = new Intent(Intent.ACTION_VIEW, uri);
        		startActivity(it);
	    	}*/
	    	Toast.makeText(PlayerControlsActivity.this, getResources().getString(R.string.error_player_other), Toast.LENGTH_LONG).show();
	    	setControlsEnabled(false);
    		PlayerControlsActivity.this.finish();
    		Uri uri = Uri.parse(
    				"http://www.youtube.com/watch?v=" + ENTRIES[currentlySelectedPosition]);
    		Intent it = new Intent(Intent.ACTION_VIEW, uri);
    		startActivity(it);
    		
	    	updateText();
	    	log(playerState);
	    }

	}
}
