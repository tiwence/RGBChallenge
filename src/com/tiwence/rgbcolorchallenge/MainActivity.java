package com.tiwence.rgbcolorchallenge;

import java.util.Random;

import android.support.v7.app.ActionBarActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class MainActivity extends ActionBarActivity implements OnClickListener {

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
	
	int mCurrentScore = 0;

	DiskView[] choices;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mInstructionsTextView = (TextView) this.findViewById(R.id.instructionsTextView);
		mScoreTextView = (TextView) this.findViewById(R.id.scoreTextView);

		mNextButton = (Button) this.findViewById(R.id.nextButton);
		mNextButton.setOnClickListener(this);
		mPlayAgainButton = (Button) this.findViewById(R.id.buttonPlayAgain);
		mPlayAgainButton.setOnClickListener(this);
		mShareButton = (Button) this.findViewById(R.id.buttonShare);
		mShareButton.setOnClickListener(this);

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

		loadNextValues();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
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
			mScoreTextView.setText("Your score : " + mCurrentScore);
			mViewFlipper.showPrevious();
		} else if (v == mNextButton) {
			loadNextValues();
			
			mViewFlipper.showPrevious();
		} else if (v == mShareButton) {
			
		}
	}

	private void displayResultView(DiskView v, boolean isRightChoice) {
		mResultLayout.setBackgroundColor(Color.rgb(v.getRed(), v.getGreen(), v.getBlue()));
		if (isRightChoice) {
			mCurrentScore++;
			mResultTextView.setText("Good !!");
			mResultScoreTextView.setText("Your score : " + mCurrentScore);
			mScoreTextView.setText("Your score : " + mCurrentScore);
			mPlayAgainButton.setVisibility(View.GONE);
			mNextButton.setVisibility(View.VISIBLE);
			mShareButton.setVisibility(View.GONE);
		} else {
			mResultTextView.setText("Fuck it :'(");
			mResultScoreTextView.setText("Final score : " + mCurrentScore);
			mPlayAgainButton.setVisibility(View.VISIBLE);
			mNextButton.setVisibility(View.GONE);
			mShareButton.setVisibility(View.VISIBLE);
		}
		mViewFlipper.showNext();
	}

	private int[] generateNewRgbValues() {
		int[] values = new int[3];
		for (int i = 0; i < 3; i++) {
			Random rn = new Random();
			int randomNum =  rn.nextInt(255);
			values[i] = randomNum;
		}
		Log.d("MainActivity", "Colors gen : " + values[0] + ", " + values[1] + ", " + values[2] + ")");

		return values;
	}

	private int generateRightChoice() {
		Random rn = new Random();
		int randomNum =  rn.nextInt(3);
		return randomNum;
	}

	private void loadNextValues() {
		int[] rightValues = generateNewRgbValues();
		displayInstructionValues(rightValues);
		int rightIndex = generateRightChoice();
		for (int i = 0; i < choices.length; i++) {
			if (rightIndex == i) {
				choices[i].setDiskViewColor(rightValues);
				choices[i].setRightChoice(true);
			} else {
				choices[i].setDiskViewColor(generateNewRgbValues());
				choices[i].setRightChoice(false);
			}
		}
	}

	private void displayInstructionValues(int[] rightValues) {
		this.mInstructionsTextView.setText("rgb(" + rightValues[0] + ", " + rightValues[1] + "," + rightValues[2] + ")");
	}
}
