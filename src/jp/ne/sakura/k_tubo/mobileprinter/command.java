package jp.ne.sakura.k_tubo.mobileprinter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

public class command extends Activity {
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		WebView webView = new WebView(getApplicationContext());
		webView.loadUrl("file:///android_asset/command.html");
		setContentView(webView);
	}
	
	public void commandActivity(View v) {
		finish();
	}
}
