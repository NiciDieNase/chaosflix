package de.nicidienase.chaosflix.touch.activities;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import de.nicidienase.chaosflix.R;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity {

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setTitle("About Chaosflix");
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		View aboutView = new AboutPage(this)
				.setImage(R.drawable.icon_notext_144x144)
				.setDescription("This is Chaosflix!")
				.addItem(new Element().setTitle("Title"))
				.addItem(new Element().setTitle("Version 0.2.4"))
				.addPlayStore("de.nicidienase.chaosflix")
				.addGitHub("nicidienase/chaosflix")
				.create();

		FrameLayout frame = findViewById(R.id.container);
		frame.addView(aboutView);
	}
}
