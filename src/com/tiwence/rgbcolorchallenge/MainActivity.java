package com.tiwence.rgbcolorchallenge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.android.gms.games.leaderboard.Leaderboards.LoadPlayerScoreResult;
import com.google.android.gms.plus.Plus;

@SuppressLint("NewApi")
public class MainActivity extends ActionBarActivity implements OnClickListener, 
GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	private static final int REQUEST_LEADERBOARD = 8001;
	private static final int RC_SIGN_IN = 9001;

	private static final long BG_ANIM_DURATION = 1500;
	private static final int RANDOM_DELTA = 30;


	private static final String MY_AD_UNIT_ID = "ca-app-pub-4388926327304197/7345907268";
	private static final String MY_AD_UNIT_ID_INTERSTITIAL = "ca-app-pub-4388926327304197/1188295660";
	private static final String SONY_DEVICE_ID = "1A4CC3FE34C3E36807787C97F1A4E3A6";
	private static final String HTC_ONE_DEVICE_ID = "AFDDEDC3D21429E3120DE4924DAF2514";

	private boolean mResolvingConnectionFailure = false;
	private boolean mAutoStartSignInflow = false;
	private boolean mLeaderboardSignInFlow = false;
	private boolean mSignInClicked = false;

	GoogleApiClient mGoogleApiClient;

	TextView mInstructionsTextView;
	TextView mScoreTextView;

	ViewFlipper mViewFlipper;

	DiskView choice1;
	DiskView choice2;
	DiskView choice3;

	RelativeLayout mResultLayout;
	TextView mResultTextView;
	TextView mResultScoreTextView;

	Button mPlayAgainButton;
	Button mNextButton;
	Button mShareButton;
	Button mLeaderboardButton;

	//Ads
	private InterstitialAd interstitial;
	private AdView mAdView;

	int mCurrentScore = 0;

	DiskView[] choices;

	private Animation mPulse;

	private String[] mBadAnswerSentences;
	private String[] mGoodAnswerSentences;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().hide();

		setContentView(R.layout.activity_main);

		RgbUtils.setFontFace(getApplicationContext(), (ViewGroup)this.findViewById(R.id.mainLayout));

		mInstructionsTextView = (TextView) this.findViewById(R.id.instructionsTextView);
		mScoreTextView = (TextView) this.findViewById(R.id.scoreTextView);

		mNextButton = (Button) this.findViewById(R.id.nextButton);
		mNextButton.setOnClickListener(this);
		mPulse = AnimationUtils.loadAnimation(this, R.anim.pulse);

		mPlayAgainButton = (Button) this.findViewById(R.id.buttonPlayAgain);
		mPlayAgainButton.setOnClickListener(this);
		mShareButton = (Button) this.findViewById(R.id.buttonShare);
		mShareButton.setOnClickListener(this);
		mLeaderboardButton = (Button) this.findViewById(R.id.buttonLeaderboard);
		mLeaderboardButton.setOnClickListener(this);
		findViewById(R.id.buttonAbout).setOnClickListener(this);
		findViewById(R.id.buttonRules).setOnClickListener(this);

		mViewFlipper = (ViewFlipper) this.findViewById(R.id.viewFlipperContainer);
		mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.abc_fade_in));
		mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.abc_fade_out));

		choice1 = (DiskView) this.findViewById(R.id.choice1);
		choice2 = (DiskView) this.findViewById(R.id.choice2);
		choice3 = (DiskView) this.findViewById(R.id.choice3);
		choice1.setOnClickListener(this);
		choice2.setOnClickListener(this);
		choice3.setOnClickListener(this);

		choices = new DiskView[] {choice1, choice2, choice3};

		mResultLayout = (RelativeLayout) this.findViewById(R.id.resultLayout);
		mResultTextView = (TextView) this.findViewById(R.id.textViewResult);
		mResultScoreTextView = (TextView) this.findViewById(R.id.textViewFinalScore);

		mGoogleApiClient = new GoogleApiClient.Builder(this)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this)
		.addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
		.addApi(Games.API).addScope(Games.SCOPE_GAMES)
		.build();

		mBadAnswerSentences = getResources().getStringArray(R.array.bad_answer_sentences);
		mGoodAnswerSentences = getResources().getStringArray(R.array.good_answer_sentences);

		setupAdsLogic();
		loadNextValues();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mGoogleApiClient.connect();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mAdView.resume();
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	protected void onPause() {
		mAdView.pause();
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}

	@Override
	protected void onDestroy() {
		mAdView.destroy();
		if (interstitial.isLoaded()) {
			interstitial.show();
		}
		super.onDestroy();
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == RC_SIGN_IN) {
			mSignInClicked = false;
			mResolvingConnectionFailure = false;
			if (resultCode == RESULT_OK) {
				mGoogleApiClient.connect();
			} else {
				Toast.makeText(getApplicationContext(), 
						this.getResources().getString(R.string.signin_failure), Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		if (v instanceof DiskView) {
			if(((DiskView)v).isRightChoice()) {
				displayResultView((DiskView)v, true);
			} else {
				displayResultView((DiskView)v, false);
			}
		} else if (v == mPlayAgainButton) {
			loadNextValues();
			mCurrentScore = 0;
			mAdView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
			mScoreTextView.setText(this.getString(R.string.your_score, mCurrentScore));
			mViewFlipper.showPrevious();
		} else if (v == mNextButton) {
			loadNextValues();
			mScoreTextView.setText(this.getString(R.string.your_score, mCurrentScore));
			mAdView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
			mViewFlipper.showPrevious();
		} else if (v == mShareButton) {
			shareCurrentScore();
		} else if (v == mLeaderboardButton) {
			if (mGoogleApiClient.isConnected()) {
				startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, 
						getResources().getString(R.string.rgb_color_challenge_leaderboard_id)), REQUEST_LEADERBOARD);
			} else {
				mLeaderboardSignInFlow = true;
				mGoogleApiClient.connect();
			}
		} else if (v.getId() == R.id.buttonAbout) {
			switchFragment(new AboutFragment());
		} else if (v.getId() == R.id.buttonRules) {
			switchFragment(new RulesFragment());
		}
	}
	
	private void switchFragment(Fragment fragment) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
		fragmentTransaction.replace(R.id.mainContainer, fragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	private void setupAdsLogic() {
		//Banner
		mAdView = new AdView(this);
		mAdView.setAdUnitId(MY_AD_UNIT_ID);
		mAdView.setAdSize(AdSize.SMART_BANNER);
		((LinearLayout)this.findViewById(R.id.mainLayout)).addView(mAdView);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				0, 0.1f);
		mAdView.setLayoutParams(params);
		((LinearLayout)this.findViewById(R.id.mainLayout)).requestLayout();
		//AdRequest adRequest = new AdRequest.Builder().build();

		AdRequest adRequest = new AdRequest.Builder()
		.addTestDevice(SONY_DEVICE_ID)
		.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
		.addTestDevice(HTC_ONE_DEVICE_ID)
		.build();

		mAdView.loadAd(adRequest);

		//Interstitial
		interstitial = new InterstitialAd(this);
		interstitial.setAdUnitId(MY_AD_UNIT_ID_INTERSTITIAL);
		/*interstitial.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
				//interstitial.show();
			}

			@Override
			public void onAdClosed() {
			}
		});*/
		interstitial.loadAd(adRequest);
	}

	private void shareCurrentScore() {
		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		String shareBody = this.getString(R.string.share_body, mCurrentScore, RgbUtils.PLAYSTORE_URL);
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, 
				this.getResources().getString(R.string.share_subject));
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
		startActivity(Intent.createChooser(sharingIntent, 
				this.getResources().getString(R.string.share_via)));
	}

	private void displayResultView(DiskView v, boolean isRightChoice) {
		int bgColor = Color.rgb(v.getRed(), v.getGreen(), v.getBlue());
		mResultLayout.setBackgroundColor(bgColor);
		mAdView.setBackgroundColor(bgColor);
		mNextButton.clearAnimation();
		mPlayAgainButton.clearAnimation();
		if (isRightChoice) {
			mCurrentScore++;
			checkHighscore();
			showResultText(true);
			mResultScoreTextView.setText(this.getString(R.string.your_score, mCurrentScore));
			mPlayAgainButton.setVisibility(View.GONE);
			mNextButton.setVisibility(View.VISIBLE);
			mPulse.reset();
			mNextButton.startAnimation(mPulse);
			mShareButton.setVisibility(View.GONE);
			mLeaderboardButton.setVisibility(View.GONE);
		} else {
			showResultText(false);
			mResultScoreTextView.setText(this.getString(R.string.final_score, mCurrentScore));
			mPulse.reset();
			mPlayAgainButton.startAnimation(mPulse);
			mPlayAgainButton.setVisibility(View.VISIBLE);
			mNextButton.setVisibility(View.GONE);
			mShareButton.setVisibility(View.VISIBLE);
			mLeaderboardButton.setVisibility(View.VISIBLE);
		}
		mViewFlipper.showNext();
	}

	private void showResultText(boolean isGood) {
		String resultText = "";
		Random rn = new Random();
		int randomNum = 0;
		if (isGood) {
			randomNum =  rn.nextInt(mGoodAnswerSentences.length);
			resultText = mGoodAnswerSentences[randomNum];
		} else {
			randomNum =  rn.nextInt(mBadAnswerSentences.length);
			resultText = mBadAnswerSentences[randomNum];
		}

		mResultTextView.setText(resultText);
	}

	private void checkHighscore() {	
		String playerName = "player";
		if (mGoogleApiClient.isConnected())
			playerName = Games.Players.getCurrentPlayer(mGoogleApiClient).getDisplayName();
		int savedHighscore =  RgbUtils.getHighscoreForUser(getApplicationContext(), playerName);

		if (savedHighscore < mCurrentScore) {
			RgbUtils.saveHighscoreForUser(getApplicationContext(), mCurrentScore, playerName);
			if (mGoogleApiClient.isConnected()) {
				Games.Leaderboards.submitScore(mGoogleApiClient, 
						getResources().getString(R.string.rgb_color_challenge_leaderboard_id), mCurrentScore);
			}
			Toast.makeText(getApplicationContext(), 
					getResources().getString(R.string.new_highscore), Toast.LENGTH_LONG).show();
		}

		unlockAchievementIfNeeded();
	}

	private void unlockAchievementIfNeeded() {
		if (mGoogleApiClient.isConnected()) {
			switch(mCurrentScore) {
			case 1:
				Games.Achievements.unlock(mGoogleApiClient, getString(R.string.first_point_achievement_id));
				break;
			case 5:
				Games.Achievements.unlock(mGoogleApiClient, getString(R.string.five_points_achievement_id));
				break;
			case 10:
				Games.Achievements.unlock(mGoogleApiClient, getString(R.string.ten_points_achievement_id));
				break;
			case 15:
				Games.Achievements.unlock(mGoogleApiClient, getString(R.string.fifteen_points_achievement_id));
				break;
			case 20: 
				Games.Achievements.unlock(mGoogleApiClient, getString(R.string.twenty_points_achievement_id));
				break;
			case 100:
				Games.Achievements.unlock(mGoogleApiClient, getString(R.string.unstoppable_achievement_id));
				break;
			default:
				break;
			}
		}
	}

	private ArrayList<Integer> generateNewRgbValues() {
		ArrayList<Integer> values = new ArrayList<Integer>(3);
		for (int i = 0; i < 3; i++) {
			Random rn = new Random();
			int randomNum =  rn.nextInt(255);
			values.add(randomNum);
		}
		
		Log.d("Right value", values.toString());
		
		return values;
	}

	/*private int[] generateNewRgbValues(int[] solution) {
		int[] values = new int[3];
		ArrayList<Integer> excludedValues = new ArrayList<Integer>();
		for (int i = 0; i < 3; i++) { //loop on right rgb values
			Random rn = new Random();
			int randomNum =  getRandomWithExclusion(rn, 0, 255, generateArrayOfExcludedValues(solution[i]));
			values[i] = randomNum;
		}
		return values;
	}*/
	
	private ArrayList<Integer> generateNewRgbValues(ArrayList<ArrayList<Integer>> choices) {
		//ArrayList<Integer> excludedValues = new ArrayList<Integer>();
		ArrayList<Integer> newChoice = new ArrayList<Integer>(3);

		ArrayList<Integer> reds = new ArrayList<Integer>();
		ArrayList<Integer> greens = new ArrayList<Integer>();
		ArrayList<Integer> blues = new ArrayList<Integer>();
		
		for (ArrayList<Integer> choice : choices) {
			for (int i = 0; i < choice.size(); i ++) {
				switch (i) {
				case 0:
					reds.addAll(generateArrayOfExcludedValues(choice.get(i)));
					break;
				case 1:
					greens.addAll(generateArrayOfExcludedValues(choice.get(i)));
					break;
				case 2:
					blues.addAll(generateArrayOfExcludedValues(choice.get(i)));
					break;
				default:
					break;
				}
			}
		}
		
		for (int i = 0; i < 3; i++) {
			Random rn = new Random();
			switch (i) {
			case 0:
				newChoice.add(i, getRandomWithExclusion(rn, 0, 255, reds));
				break;
			case 1:
				newChoice.add(i, getRandomWithExclusion(rn, 0, 255, greens));
				break;
			case 2:
				newChoice.add(i, getRandomWithExclusion(rn, 0, 255, blues));
				break;
			default:
				break;
			}
		}
		
		Log.d("New choice", newChoice.toString());
		
		return newChoice;
	}

	private ArrayList<Integer> generateArrayOfExcludedValues(int range) {
		int start = range - RANDOM_DELTA;
		if (start < 0) start = 0;
		int end = range + RANDOM_DELTA;
		if (end > 255) end = 255;

		ArrayList<Integer> excluded = new ArrayList<Integer>(end - start);

		for (int i = 0; i < excluded.size(); i++) {
			 excluded.add(start + i);
		}

		return excluded;
	}

	private int getRandomWithExclusion(Random rnd, int start, int end, Integer... exclude) {
		int random = start + rnd.nextInt(end - start + 1 - exclude.length);
		for (int ex : exclude) {
			if (random < ex) {
				break;
			}
			random++;
		}
		return random;
	}
	
	private int getRandomWithExclusion(Random rnd, int start, int end, ArrayList<Integer> exclude) {
		int random = start + rnd.nextInt(end - start + 1 - exclude.size());
		for (int ex : exclude) {
			if (random < ex) {
				break;
			}
			random++;
		}
		return random;
	}

	private int generateRightChoice() {
		Random rn = new Random();
		int randomNum =  rn.nextInt(3);
		return randomNum;
	}

	private void loadNextValues() {
		ArrayList<ArrayList<Integer>>turnValues = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> rightValues = generateNewRgbValues();
		turnValues.add(rightValues);
		displayInstructionValues(rightValues);
		int rightIndex = generateRightChoice();
		for (int i = 0; i < choices.length; i++) {
			if (rightIndex == i) {
				choices[i].setDiskViewColor(rightValues);
				choices[i].setRightChoice(true);
			} else {
				ArrayList<Integer> otherChoice = generateNewRgbValues(turnValues);
				turnValues.add(otherChoice);
				choices[i].setDiskViewColor(otherChoice);
				choices[i].setRightChoice(false);
			}
		}
	}

	private void displayInstructionValues(ArrayList<Integer> rightValues) {
		String rgb = this.getResources().getString(R.string.rgb, rightValues.get(0), rightValues.get(1), rightValues.get(2));
		this.mInstructionsTextView.setText(rgb);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (mResolvingConnectionFailure) {
			return;
		}

		// if the sign-in button was clicked or if auto sign-in is enabled,
		// launch the sign-in flow
		if (mSignInClicked || mAutoStartSignInflow || mLeaderboardSignInFlow) {
			mAutoStartSignInflow = false;
			mSignInClicked = false;
			mResolvingConnectionFailure = true;

			//User need to sign in
			if (!BaseGameUtils.resolveConnectionFailure(this,
					mGoogleApiClient, result,
					RC_SIGN_IN, this.getResources().getString(R.string.unknown_error))) {
				mResolvingConnectionFailure = false;
			}		
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// The player is signed in. Hide the sign-in button and allow the
		// player to proceed.

		loadCurrentHighscore();

		if (mLeaderboardSignInFlow) {
			mLeaderboardSignInFlow = false;
			startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, 
					getResources().getString(R.string.rgb_color_challenge_leaderboard_id)), REQUEST_LEADERBOARD);
		}
	}

	private void loadCurrentHighscore() {
		PendingResult<LoadPlayerScoreResult> psr = Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient, 
				getResources().getString(R.string.rgb_color_challenge_leaderboard_id), 
				LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC);

		psr.setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
			@Override
			public void onResult(LoadPlayerScoreResult result) {
				if (result.getScore() != null && Integer.parseInt(result.getScore().getDisplayScore()) > 0) {
					RgbUtils.saveHighscoreForUser(getApplicationContext(), 
							Integer.parseInt(result.getScore().getDisplayScore()), 
							Games.Players.getCurrentPlayer(mGoogleApiClient).getDisplayName());
				}
			}
		});
	}

	@Override
	public void onConnectionSuspended(int cause) {
		mGoogleApiClient.connect();
	}
}
