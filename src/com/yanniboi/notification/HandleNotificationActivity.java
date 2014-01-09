package com.yanniboi.notification;
 
import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
 
public class HandleNotificationActivity extends Activity {
 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.handle_notification_activity);
  
		// Set information text. We're doing this programmatically so we can
		// use HTML in the strings and make them behave what they are made for.
		String text = "Sample text";
		TextView t = (TextView) findViewById(R.id.textView2);
		t.setMovementMethod(LinkMovementMethod.getInstance());
		t.setText(text);
	}
}
