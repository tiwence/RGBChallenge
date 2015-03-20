package com.tiwence.rgbcolorchallengegame;

import java.util.ArrayList;

import com.tiwence.rgbcolorchallenge.R;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends Fragment implements OnClickListener {

	private View mAboutView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		mAboutView = inflater.inflate(R.layout.fragment_about, container, false);

		RgbUtils.setFontFace(getActivity(), (ViewGroup)mAboutView);

		TextView versionNumberTextView = (TextView) mAboutView.findViewById(R.id.textViewVersionNumber);

		try {
			String aboutText = this.getString(R.string.about_text, getString(R.string.app_name),  
					getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
			versionNumberTextView.setText(aboutText);
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		mAboutView.findViewById(R.id.buttonShareApp).setOnClickListener(this);
		mAboutView.findViewById(R.id.buttonContact).setOnClickListener(this);
		mAboutView.findViewById(R.id.buttonReview).setOnClickListener(this);
		
		
		
		return mAboutView;
	}

	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonShareApp:
			shareApp();
			break;
		case R.id.buttonContact:
			contact();
			break;
		case R.id.buttonReview:
			addReview();
			break;
		default:
			break;
		}
	}

	private void addReview() {
		Intent webIntent = new Intent(Intent.ACTION_VIEW);
		webIntent.setData(Uri.parse(RgbUtils.PLAYSTORE_URL));
		startActivity(webIntent);
	}

	private void contact() {
		Intent contactIntent = new Intent(Intent.ACTION_SEND);
		contactIntent.setType("text/html");
		contactIntent.putExtra(android.content.Intent.EXTRA_EMAIL, "tiwence.inc@gmail.com");
		contactIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, this.getResources().getString(R.string.contact_subject));
		startActivity(Intent.createChooser(contactIntent, this.getResources().getString(R.string.contact_via)));
	}

	private void shareApp() {
		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, this.getResources().getString(R.string.share_subject));
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, RgbUtils.PLAYSTORE_URL);
		startActivity(Intent.createChooser(sharingIntent, this.getResources().getString(R.string.share_via)));
	}
}
