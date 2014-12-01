package com.tiwence.rgbcolorchallenge;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.TextView;

public class RgbUtils {
	
	public static final String PREFS_NAME = "MyPrefsFile";
	
	public static Typeface mTypeFaceReg;
	public static Typeface mTypeFaceBold;
	
	public static int getHighscoreForUser(Context context, String playerName) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getInt(playerName, 0);
	}

	public static void saveHighscoreForUser(Context context,
			int score, String playerName) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor edit = prefs.edit();
		edit.putInt(playerName, score);
		edit.commit();
	}

	public static void setFontFace(Context context, ViewGroup mainLayout) {
		for (int i = 0; i < mainLayout.getChildCount(); i++) {
			Object child = mainLayout.getChildAt(i);
			if (child instanceof ViewGroup) {
				setFontFace(context, (ViewGroup)child);
			} else if (child instanceof TextView) {
				if (((TextView)child).getTypeface() != null) {
					if (((TextView)child).getTypeface().getStyle() == Typeface.BOLD) {
						mTypeFaceBold = Typeface.createFromAsset(context.getAssets(), "fonts/Quicksand-Bold.otf");
						((TextView)child).setTypeface(mTypeFaceBold);
					} else {
						mTypeFaceReg = Typeface.createFromAsset(context.getAssets(), "fonts/Quicksand-Regular.otf");
						((TextView)child).setTypeface(mTypeFaceReg);
					}
				} else {
					mTypeFaceReg = Typeface.createFromAsset(context.getAssets(), "fonts/Quicksand-Regular.otf");
					((TextView)child).setTypeface(mTypeFaceReg);
				}
			}
		}
	}
	
	
}
