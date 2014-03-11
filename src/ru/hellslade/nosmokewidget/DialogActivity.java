package ru.hellslade.nosmokewidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.vk.sdk.VKUIHelper;

public class DialogActivity extends Activity implements OnClickListener {
	
	private final static String TAG = "DialogActivity";

	int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;

	private ImageButton shareVkButton, shareOdnoklassnikiButton, shareTwitterButton, shareFacebookButton, shareInstagramButton;
	private TextView configTitleTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog);
		setupUI();
		// извлекаем ID конфигурируемого виджета
	    Intent intent = getIntent();
	    Bundle extras = intent.getExtras();
	    if (extras != null) {
	        widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
	    }
	    // и проверяем его корректность
	    if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
	        finish();
	    }
	}
	private void setupUI() {
		shareVkButton = (ImageButton) findViewById(R.id.shareVkButton);
		shareOdnoklassnikiButton = (ImageButton) findViewById(R.id.shareOdnoklassnikiButton);
		shareTwitterButton = (ImageButton) findViewById(R.id.shareTwitterButton);
		shareFacebookButton = (ImageButton) findViewById(R.id.shareFacebookButton);
		shareInstagramButton = (ImageButton) findViewById(R.id.shareInstagramButton);
		configTitleTextView = (TextView) findViewById(R.id.configTitleTextView);
		
		shareVkButton.setOnClickListener(this);
		shareOdnoklassnikiButton.setOnClickListener(this);
		shareTwitterButton.setOnClickListener(this);
		shareFacebookButton.setOnClickListener(this);
		shareInstagramButton.setOnClickListener(this);
		configTitleTextView.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.shareVkButton:
				Intent shareVkIntent = new Intent(DialogActivity.this, ShareVkActivity.class);
				shareVkIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
				startActivity(shareVkIntent);
				this.finish();
				break;
			case R.id.shareOdnoklassnikiButton:
			    Intent shareOkIntent = new Intent(DialogActivity.this, ShareOkActivity.class);
			    shareOkIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
                startActivity(shareOkIntent);
                this.finish();
				break;
			case R.id.shareTwitterButton:
				this.finish();
				break;
			case R.id.shareFacebookButton:
				this.finish();
				break;
			case R.id.shareInstagramButton:
				this.finish();
				break;
			case R.id.configTitleTextView:
				startConfigActivity();
				this.finish();
				break;
		}
	}
	@Override 
	protected void onResume() { 
		super.onResume(); 
		VKUIHelper.onResume(this); 
	}
	@Override 
	protected void onDestroy() { 
		super.onDestroy(); 
		VKUIHelper.onDestroy(this); 
	} 

	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
		VKUIHelper.onActivityResult(requestCode, resultCode, data); 
	} 
	private void startConfigActivity() {
		// Конфигурационный экран
	    Intent configIntent = new Intent(this, ConfigActivity.class);
	    configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
	    configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
	    startActivity(configIntent);
	}
}
