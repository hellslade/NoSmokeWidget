package ru.hellslade.nosmokewidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCaptchaDialog;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKRequest.VKRequestListener;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKPhoto;
import com.vk.sdk.api.model.VKPhotoArray;
import com.vk.sdk.api.model.VKWallPostResult;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;

public class ShareVkActivity extends Activity implements OnClickListener {
	
	private final static String TAG = "ShareVkActivity";

	class ShareVkTask extends AsyncTask<Void, Void, Void> {
		ProgressDialog pg = new ProgressDialog(ShareVkActivity.this);
		@Override
		protected void onPreExecute() {
			pg.setMessage(getResources().getString(R.string.share_vk_task_progress_caption));
			pg.setCancelable(false);
			pg.show();
			super.onPreExecute();
		}
		@Override
		protected Void doInBackground(Void... params) {
			shareVk();
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			if (pg != null) {
				pg.dismiss();
			}
			super.onPostExecute(result);
		}
	}
	
	int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
	
	
	private static String[] mVkScope = new String[]{VKScope.WALL, VKScope.PHOTOS};
	private static String mVkTokenKey = "vk_token_key";
	
	private ImageView postImage;
	private EditText postText;
	
	private Bitmap mBitmap;
	
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
            new AlertDialog.Builder(ShareVkActivity.this)
            .setMessage(authorizationError.errorMessage)
            .show();
        }
        @Override
        public void onReceiveNewToken(VKAccessToken newToken) {
            Log.v(TAG, "onReceiveNewToken");
            newToken.saveTokenToSharedPreferences(ShareVkActivity.this, mVkTokenKey);
//            shareVk();
        }
        @Override
        public void onAcceptUserToken(VKAccessToken token) {
            Log.v(TAG, "onAcceptUserToken");
            super.onAcceptUserToken(token);
        }
        @Override
        public void onRenewAccessToken(VKAccessToken token) {
            Log.v(TAG, "onRenewAccessToken");
            super.onRenewAccessToken(token);
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_vk_layout);
	    postImage = (ImageView)findViewById(R.id.postImage);
	    postText = (EditText)findViewById(R.id.descriptionEditText);
	    ((Button)findViewById(R.id.postButton)).setOnClickListener(this);
		// извлекаем ID конфигурируемого виджета
	    Intent intent = getIntent();
	    Bundle extras = intent.getExtras();
	    if (extras != null) {
	        widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
	        
	    }
	    // и провер€ем его корректность
	    if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
	        finish();
	    }
	    mBitmap = Utils.getShareBitmap(this, widgetID);
        if (mBitmap == null) {
            Toast.makeText(this, "Ќе удалось получить изображение дл€ поста", Toast.LENGTH_LONG).show();
            finish();
        }
        postImage.setImageBitmap(mBitmap);
	    
	    VKAccessToken token = VKAccessToken.tokenFromSharedPreferences(this, mVkTokenKey);
	    VKSdk.initialize(vkListener, Constants.VK_APP_ID, token);
	    VKSdk.authorize(mVkScope, false, false);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.postButton:
				new ShareVkTask().execute();
				break;
		}
	}
	private void shareVk() {
		// Ќепосредственно публикаци€
		int user_id = 0; // публикуем на свою страницу
		int group_id = 0; // публикуем на свою страницу
		VKImageParameters params = VKImageParameters.jpgImage(0.9f);
//		final Bitmap photo = getPhoto();
		VKUploadImage image = new VKUploadImage(mBitmap, params);
		VKRequest request = VKApi.uploadWallPhotoRequest(image, user_id, group_id);
		request.executeWithListener(new VKRequestListener() { 
			@Override 
			public void onComplete(VKResponse response) { 
				//Do complete stuff
				Log.v(TAG, "onComplete");
//				photo.recycle();
                VKPhoto photoModel = ((VKPhotoArray) response.parsedModel).get(0);
                String play_link = "https://play.google.com/store/apps/details?id=ru.hellslade.nosmokewidget";
                makePost(String.format("photo%s_%s,%s", photoModel.owner_id, photoModel.id, play_link));
			} 
			@Override 
			public void onError(VKError error) { 
				//Do error stuff 
				Log.v(TAG, "onError");
				showError(error);
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
	}
	private void makePost(String attachments) {
        VKRequest post = VKApi.wall().post(VKParameters.from(VKApiConst.ATTACHMENTS, attachments, VKApiConst.MESSAGE, postText.getText()));
        post.setModelClass(VKWallPostResult.class);
        post.executeWithListener(new VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Log.v(TAG, "makePost onComplete");
                ShareVkActivity.this.finish();
            }
            @Override
            public void onError(VKError error) {
                showError(error);
            }
        });
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
	private void showError(VKError error) {
        new AlertDialog.Builder(this)
                .setMessage(error.errorMessage)
                .setPositiveButton("OK", null)
                .show();
        if (error.httpError != null)
            Log.w("Test", "Error in request or upload", error.httpError);
    }
	
}
