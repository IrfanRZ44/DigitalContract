package com.pertamina.digitalcontract.util;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;

public class FontAwasomeTextView extends androidx.appcompat.widget.AppCompatTextView {

	private Context context;

	public FontAwasomeTextView(Context context) {
		super(context);
		this.context = context;
		createView();
	}

	public FontAwasomeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		createView();
	}

	private void createView(){
		setGravity(Gravity.CENTER);
		setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/FontAwesome.otf"));
//		setTypeface(FontTypeface.get("FontAwesome.otf", context));
	}
}
