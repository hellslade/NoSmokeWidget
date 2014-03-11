package ru.hellslade.nosmokewidget;

import java.io.IOException;

import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkTokenRequestListener;
import ru.ok.android.sdk.util.OkScope;
import android.app.Activity;
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

public class ShareOkActivity extends Activity implements OnClickListener {
    private final static String TAG = "ShareOkActivity";
    
    class ShareOkTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog pg = new ProgressDialog(ShareOkActivity.this);
        @Override
        protected void onPreExecute() {
            pg.setMessage(getResources().getString(R.string.share_ok_task_progress_caption));
            pg.setCancelable(false);
            pg.show();
            super.onPreExecute();
        }
        @Override
        protected Void doInBackground(Void... params) {
            shareOk();
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
    
    private static String[] mOkScope = new String[]{OkScope.PHOTO_CONTENT, OkScope.VALUABLE_ACCESS};
    
    private ImageView postImage;
    private EditText postText;
    
    private Bitmap mBitmap;
    
    protected Odnoklassniki mOdnoklassniki;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_ok_layout);
        postImage = (ImageView)findViewById(R.id.postImage);
        postText = (EditText)findViewById(R.id.postText);
        ((Button)findViewById(R.id.postButton)).setOnClickListener(this);
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
        mBitmap = Utils.getShareBitmap(this, widgetID);
        if (mBitmap == null) {
            Toast.makeText(this, "Не удалось получить изображение для поста", Toast.LENGTH_LONG).show();
            finish();
        }
        postImage.setImageBitmap(mBitmap);
        
        mOdnoklassniki = Odnoklassniki.createInstance(this, Constants.OK_APP_ID, Constants.OK_SECRET_KEY, Constants.OK_PUBLIC_KEY);
        mOdnoklassniki.setTokenRequestListener(new OkTokenRequestListener() {
            @Override
            public void onSuccess(String accessToken) {
                Toast.makeText(ShareOkActivity.this, "Recieved token : " + accessToken, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError() {
                Toast.makeText(ShareOkActivity.this, "Error getting token", Toast.LENGTH_SHORT).show();
                ShareOkActivity.this.finish();
            }
            @Override
            public void onCancel() {
                Toast.makeText(ShareOkActivity.this, "Authorization was canceled", Toast.LENGTH_SHORT).show();
                ShareOkActivity.this.finish();
            }
        });
        if (!mOdnoklassniki.hasAccessToken()) {
            mOdnoklassniki.requestAuthorization(this, false, mOkScope);
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.postButton:
                if (mOdnoklassniki.hasAccessToken()) {
                    Log.v(TAG, "accessToken " + mOdnoklassniki.getCurrentAccessToken());
                    new ShareOkTask().execute();
                } else {
                    mOdnoklassniki.requestAuthorization(this, false, mOkScope);
                }
                break;
        }
    }
    private void shareOk() {
        // Метод запускается из AsyncTask, работа с UI запрещена
//        mOdnoklassniki.request(apiMethod, params, httpMethod);
        try {
            String result = mOdnoklassniki.request("users.getCurrentUser", null, "get");
            Log.v(TAG, result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    @Override
    protected void onDestroy() {
        mOdnoklassniki.clearTokens(this);
        mOdnoklassniki.removeTokenRequestListener();
        super.onDestroy();
    }
}
