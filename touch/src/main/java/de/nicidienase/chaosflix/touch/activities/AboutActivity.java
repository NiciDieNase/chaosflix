package de.nicidienase.chaosflix.touch.activities;


import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.Charset;

import de.nicidienase.chaosflix.R;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AboutActivity extends AppCompatActivity {

	private static final String TAG = AboutActivity.class.getSimpleName();
	private int depth = 0;
	private WebView webView;

	public AboutActivity() {
		// Required empty public constructor
	}


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
		super.onCreate(savedInstanceState, persistentState);
		setContentView(R.layout.fragment_about);

		webView = findViewById(R.id.web_view);
		webView.setNetworkAvailable(false);
		webView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url){
				if(url.startsWith("http")){
					depth++;
					return false;
				} else {
					url = url.replace("file:///android_asset/","");
					loadAsset(url);
					return true;
				}
			}
		});
		loadAsset("file:///android_asset/about.html");
	}

	private void loadAsset(String filename) {
		Observable.fromCallable(() -> {
			InputStream input = getAssets().open(filename);
			String webViewData = IOUtils.toString(input, Charset.defaultCharset());
			if(webViewData.startsWith("<!DOCTYPE html>")){
				depth = 0;
			}
			return webViewData;
		}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						s -> webView.loadDataWithBaseURL("file:///android_asset/",s,"text/html","utf-8","about:blank"),
						error -> Log.d(TAG,Log.getStackTraceString(error))
				);
	}

	@Override
	public void onBackPressed() {
		if(depth == 1){
			loadAsset("about.html");
		} else if (depth > 1){
			webView.goBack();
		}else {
			super.onBackPressed();
		}
	}
}
