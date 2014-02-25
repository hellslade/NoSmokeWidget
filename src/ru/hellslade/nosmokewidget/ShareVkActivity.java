package ru.hellslade.nosmokewidget;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.NinePatchDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

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
	
	public final static String WIDGET_PREF = "widget_pref";
	public final static String WIDGET_DATE = "widget_date_";
	public final static String WIDGET_YEARS = "widget_years_";
	public final static String WIDGET_COUNT = "widget_count_";
	public final static String WIDGET_PRICE = "widget_price_";
	
	private static String[] mVkScope = new String[]{VKScope.WALL, VKScope.PHOTOS};
	private static String mVkTokenKey = "vk_token_key";
	
	private ImageView postImage;
	
	private TextPaint mTextPaint;
	private Bitmap mBitmap = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_vk_layout);
	    postImage = (ImageView)findViewById(R.id.postImage);
	    ((Button)findViewById(R.id.postButton)).setOnClickListener(this);
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
	    
	    VKAccessToken token = VKAccessToken.tokenFromSharedPreferences(this, mVkTokenKey);
	    VKSdk.initialize(vkListener, Constants.VK_API_ID, token);

	}
	
	private void fillUI(Bundle extras) {
		SharedPreferences sp = getSharedPreferences(WIDGET_PREF, Context.MODE_PRIVATE);
		
		String widgetData = sp.getString(WIDGET_DATE + widgetID, null);
		if (widgetData == null)
			this.finish();
		String widgetCount = sp.getString(ConfigActivity.WIDGET_COUNT + widgetID, null);
		String widgetPrice = sp.getString(ConfigActivity.WIDGET_PRICE + widgetID, null);
		String widgetYears = sp.getString(ConfigActivity.WIDGET_YEARS + widgetID, null);
		
		// Создаем кисть для вывода текста
		float density = getResources().getDisplayMetrics().density;
		mTextPaint = new TextPaint(TextPaint.DEV_KERN_TEXT_FLAG | TextPaint.LINEAR_TEXT_FLAG);
		int[] drawableState = new int[] { 0, 1 };
		mTextPaint.drawableState = drawableState;
		mTextPaint.density = density;
		mTextPaint.setTextSize(50);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setColor(Color.parseColor("#005500")); // Зеленый текст
		int dstWidth = (int)(250*density);
		int dstHeight = (int)(150*density);
		// Создаем битмап и канву
		Bitmap source = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(source);
		
		// Рисуем фон
		NinePatchDrawable npd = (NinePatchDrawable)getResources().getDrawable(R.drawable.frame);
		npd.setBounds(0, 0, dstWidth, dstHeight);
		npd.draw(canvas);
		// Рисуем лого в правом нижнем углу
    	Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.app_icon);
    	int width = logo.getWidth();
    	int height = logo.getHeight();
    	int left = (dstWidth - width)-10;
    	int top = (dstHeight - height)-20;
    	canvas.drawBitmap(logo, left, top, null);
    	logo.recycle();
    	// Расчеты для вывода текста
    	// Высота одной строки (задает смещение по у-координате)
        float yoff = mTextPaint.descent() - mTextPaint.ascent();
        // Всего строк 6
        float all_text_height = yoff*6; // Общая высота строк
        float all_vert_space = dstHeight - all_text_height; // общая высота пробелов между строками
        float vert_space = (all_vert_space/5)-10; // Междустрочный интервал
    	String s; // Выводимая строка
    	// Начальные координаты вывода строки
    	int x = 20;
    	int y = (int)yoff;
		
		// Считаем количество дней
		String dtStart = widgetData;
	    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
	    long daysCount = 0;
	    int count = 0, years = 0;
	    float price = 0;
	    try {  
	        Date date = format.parse(dtStart);
	        Calendar now = Calendar.getInstance();
	        daysCount = daysBetween(date, now.getTime());
	        s = String.format(getResources().getString(R.string.no_smoke_days), daysCount, getDaysLabel(this, daysCount));
	    	
	    	canvas.drawText(s, x, y, mTextPaint);
	    	
	        count = Integer.valueOf(widgetCount);
	        price = Float.valueOf(widgetPrice);
	        years = Integer.valueOf(widgetYears);
	    } catch (ParseException e) {  
	        Log.v(TAG, "Ошибка парсинга даты" + e.getMessage()); 
	        return;
	    } catch (NumberFormatException e) {
	    	Log.v(TAG, "Введено неправильное число" + e.getMessage()); 
	        return;
	    }
	    s = String.format(getResources().getString(R.string.no_smoke_count), count*daysCount);
    	y += (int)(yoff+vert_space);
    	canvas.drawText(s, x, y, mTextPaint);
    	
    	s = String.format(getResources().getString(R.string.no_smoke_price), price*daysCount);
    	y += (int)(yoff+vert_space);
    	canvas.drawText(s, x, y, mTextPaint);
    	
    	// Красный текст
    	mTextPaint.setColor(Color.parseColor("#990000"));
    	
    	s = String.format(getResources().getString(R.string.smoke_days), years, getYearsLabel(this, years));
    	y += (int)(yoff+vert_space);
    	canvas.drawText(s, x, y, mTextPaint);
    	
    	s = String.format(getResources().getString(R.string.smoke_count), years*365*count);
    	y += (int)(yoff+vert_space);
    	canvas.drawText(s, x, y, mTextPaint);
    	
    	s = String.format(getResources().getString(R.string.smoke_price), years*365*count*15);
    	y += (int)(yoff+vert_space);
    	canvas.drawText(s, x, y, mTextPaint);
	    
    	mBitmap = source.copy(source.getConfig(), false);
    	source.recycle();
    	postImage.setImageBitmap(mBitmap);
    	/*
    	try {
			mBitmap.compress(CompressFormat.JPEG, 90, new FileOutputStream(new File("/storage/emulated/0/Download/1.jpg")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	*/
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.postButton:
				new ShareVkTask().execute();
				break;
		}
	}
	private Bitmap getPhoto() {
		return mBitmap;
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
			new AlertDialog.Builder(ShareVkActivity.this)
            .setMessage(authorizationError.errorMessage)
            .show();
		}
		@Override
        public void onReceiveNewToken(VKAccessToken newToken) {
			Log.v(TAG, "onReceiveNewToken");
            newToken.saveTokenToSharedPreferences(ShareVkActivity.this, mVkTokenKey);
            shareVk();
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
	private void shareVk() {
		// Непосредственно публикация
		int user_id = 0; // публикуем на свою страницу
		int group_id = 0; // публикуем на свою страницу
		VKImageParameters params = VKImageParameters.jpgImage(0.9f);
		final Bitmap photo = getPhoto();
		VKUploadImage image = new VKUploadImage(photo, params);
		VKRequest request = VKApi.uploadWallPhotoRequest(image, user_id, group_id);
		request.executeWithListener(new VKRequestListener() { 
			@Override 
			public void onComplete(VKResponse response) { 
				//Do complete stuff
				Log.v(TAG, "onComplete");
				photo.recycle();
                VKPhoto photoModel = ((VKPhotoArray) response.parsedModel).get(0);
                makePost(String.format("photo%s_%s", photoModel.owner_id, photoModel.id));
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
	}
	private void makePost(String attachments) {
        VKRequest post = VKApi.wall().post(VKParameters.from(VKApiConst.ATTACHMENTS, attachments));
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
	/**
	 * This method also assumes endDate >= startDate
	**/
	public static long daysBetween(Date startDate, Date endDate) {
	  Calendar sDate = getDatePart(startDate);
	  Calendar eDate = getDatePart(endDate);

	  long daysBetween = 0;
	  while (sDate.before(eDate)) {
	      sDate.add(Calendar.DAY_OF_MONTH, 1);
	      daysBetween++;
	  }
	  return daysBetween;
	}
	public static Calendar getDatePart(Date date){
	    Calendar cal = Calendar.getInstance();       // get calendar instance
	    cal.setTime(date);      
	    cal.set(Calendar.HOUR_OF_DAY, 0);            // set hour to midnight
	    cal.set(Calendar.MINUTE, 0);                 // set minute in hour
	    cal.set(Calendar.SECOND, 0);                 // set second in minute
	    cal.set(Calendar.MILLISECOND, 0);            // set millisecond in second

	    return cal;                                  // return the date part
	}
	
	 /* Вернет правильное склонение количества дней
	 * <string-array name="days">
	 *   <item>день</item>
	 *   <item>дня</item>
	 *   <item>дней</item>
	 * </string-array>
	 * если предпоследня цифра 1, то "дней"
	 * иначе если последняя цифра 0 или >=5, то "дней"
	 * иначе если последняя цифра 2,3 или 4, то "дня"
	 * иначе "день".
	 * @param context
	 * @param days
	 * @return
	 */
	private static String getDaysLabel(Context context, long days) {
		// % -- mod
		// / -- div
		Resources res = context.getResources();
		String result = res.getStringArray(R.array.days)[0];
		
		long prevprev = (days % 100) / 10; // Предпоследняя цифра
		long prev = days % 10; // Последняя цифра
		
		if (prevprev == 1) {
			result = res.getStringArray(R.array.days)[2];
		} else if (prev == 0 || prev >= 5) {
			result = res.getStringArray(R.array.days)[2];
		} else if (prev >=2 && prev <= 4) {
			result = res.getStringArray(R.array.days)[1];
		} else {
			result = res.getStringArray(R.array.days)[0];
		}
		
		return result;
	}
	/**
	 * Вернет правильное склонение количества лет
	 * <string-array name="years">
	 *   <item>год</item>
	 *   <item>года</item>
	 *   <item>лет</item>
	 * </string-array>
	 * если предпоследня цифра 1, то "лет"
	 * иначе если последняя цифра 0 или >=5, то "лет"
	 * иначе если последняя цифра 2,3 или 4, то "года"
	 * иначе "год".
	 * @param context
	 * @param years
	 * @return
	 */
	private static String getYearsLabel(Context context, long years) {
		// % -- mod
		// / -- div
		Resources res = context.getResources();
		String result = res.getStringArray(R.array.years)[0];
		
		long prevprev = (years % 100) / 10; // Предпоследняя цифра
		long prev = years % 10; // Последняя цифра
		
		if (prevprev == 1) {
			result = res.getStringArray(R.array.years)[2];
		} else if (prev == 0 || prev >= 5) {
			result = res.getStringArray(R.array.years)[2];
		} else if (prev >=2 && prev <= 4) {
			result = res.getStringArray(R.array.years)[1];
		} else {
			result = res.getStringArray(R.array.years)[0];
		}
		
		return result;
	}
	
}
