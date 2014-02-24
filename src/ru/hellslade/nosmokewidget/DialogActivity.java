package ru.hellslade.nosmokewidget;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCaptchaDialog;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKRequest.VKRequestListener;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;

public class DialogActivity extends Activity implements OnClickListener {
	
	private final static String TAG = "DialogActivity";

	int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
	Intent resultValue;

	public final static String WIDGET_PREF = "widget_pref";
	public final static String WIDGET_DATE = "widget_date_";
	public final static String WIDGET_YEARS = "widget_years_";
	public final static String WIDGET_COUNT = "widget_count_";
	public final static String WIDGET_PRICE = "widget_price_";
	
	private ImageButton shareVkButton, shareOdnoklassnikiButton, shareTwitterButton, shareFacebookButton;
	private TextView configTitleTextView;
	
	private static String[] mVkScope = new String[]{VKScope.WALL, VKScope.PHOTOS};
	private static String mVkTokenKey = "vk_token_key";
	
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
	        fillUI(extras);
	    }
	    // и проверяем его корректность
	    if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
	        finish();
	    }
	    // формируем intent ответа
	    resultValue = new Intent();
	    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

	    // отрицательный ответ
	    setResult(RESULT_CANCELED, resultValue);
	    
	    VKAccessToken token = VKAccessToken.tokenFromSharedPreferences(this, mVkTokenKey);
	    VKSdk.initialize(vkListener, Constants.VK_API_ID, token);
//	    https://oauth.vk.com/blank.html#access_token=d7ca17c297f47746963a5eda22f2b6e190999a85949fcc15f200082f2ae525b6768a66c21752ecea08454&expires_in=86400&user_id=4884341

	}
	private void setupUI() {
		shareVkButton = (ImageButton) findViewById(R.id.shareVkButton);
		shareOdnoklassnikiButton = (ImageButton) findViewById(R.id.shareOdnoklassnikiButton);
		shareTwitterButton = (ImageButton) findViewById(R.id.shareTwitterButton);
		shareFacebookButton = (ImageButton) findViewById(R.id.shareFacebookButton);
		configTitleTextView = (TextView) findViewById(R.id.configTitleTextView);
		
		shareVkButton.setOnClickListener(this);
		shareOdnoklassnikiButton.setOnClickListener(this);
		shareTwitterButton.setOnClickListener(this);
		shareFacebookButton.setOnClickListener(this);
		configTitleTextView.setOnClickListener(this);
	}
	private void fillUI(Bundle extras) {
		SharedPreferences sp = getSharedPreferences(WIDGET_PREF, Context.MODE_PRIVATE);
		
		String widgetData = sp.getString(WIDGET_DATE + widgetID, null);
		if (widgetData == null)
			return;
		
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.shareVkButton:
				shareVk();
				//this.finish();
				break;
			case R.id.shareOdnoklassnikiButton:
				this.finish();
				break;
			case R.id.shareTwitterButton:
				this.finish();
				break;
			case R.id.shareFacebookButton:
				this.finish();
				break;
			case R.id.configTitleTextView:
				startConfigActivity();
				this.finish();
				break;
		}
	}
	VKSdkListener vkListener = new VKSdkListener() {
		@Override
		public void onTokenExpired(VKAccessToken expiredToken) {
			Log.v(TAG, "onTokenExpired");
			VKSdk.authorize(mVkScope, false, false);
		}
		@Override
		public void onCaptchaError(VKError captchaError) {
			Log.v(TAG, "onCaptchaError");
			new VKCaptchaDialog(captchaError).show();
		}
		@Override
		public void onAccessDenied(VKError authorizationError) {
			Log.v(TAG, "onAccessDenied");
			new AlertDialog.Builder(DialogActivity.this)
            .setMessage(authorizationError.errorMessage)
            .show();
		}
		@Override
        public void onReceiveNewToken(VKAccessToken newToken) {
			Log.v(TAG, "onReceiveNewToken");
            newToken.saveTokenToSharedPreferences(DialogActivity.this, mVkTokenKey);
            //publish();
        }
		@Override
		public void onAcceptUserToken(VKAccessToken token) {
			Log.v(TAG, "onAcceptUserToken");
			//publish();
			super.onAcceptUserToken(token);
		}
		@Override
		public void onRenewAccessToken(VKAccessToken token) {
			Log.v(TAG, "onRenewAccessToken");
			super.onRenewAccessToken(token);
		}
	};
	private void shareVk() {
		//VKSdk.authorize(mVkScope, false, true);
		publish();
	}
	private void publish() {
		// Непосредственно публикация
		int user_id = 0; // публикуем на свою страницу
		int group_id = 0; // публикуем на свою страницу
		VKImageParameters params = VKImageParameters.jpgImage(0.9f);
		Bitmap data = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
		VKUploadImage image = new VKUploadImage(data, params);
		File image_file = new File("/storage/emulated/0/DCIM/Camera/IMG_20140221_160549.jpg");
		VKParameters p = new VKParameters();
		//p.put("owner_id", 4884341);
//		p.put("friends_only", 0);
		p.put("message", "test");
//		VKRequest request = VKApi.wall().post(p);
		VKRequest request = VKApi.uploadWallPhotoRequest(image, user_id, group_id);
		request.executeWithListener(new VKRequestListener() { 
			@Override 
			public void onComplete(VKResponse response) { 
				//Do complete stuff
				Log.v(TAG, "onComplete");
				DialogActivity.this.finish();
			} 
			@Override 
			public void onError(VKError error) { 
				//Do error stuff 
				Log.v(TAG, "onError");
				String msg = "";;
				if (error.errorCode == VKError.VK_API_ERROR) {
					msg = "apiError " + error.apiError.errorMessage;
				} else {
					msg = "httpError " + error.httpError.getMessage();
				}
				Log.v(TAG, "onError " + msg);
			} 
			@Override
			public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) { 
				//I don't really believe in progress
				Log.v(TAG, "attemptFailed");
			} 
		});
		data.recycle();
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
