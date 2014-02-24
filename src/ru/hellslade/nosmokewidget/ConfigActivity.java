package ru.hellslade.nosmokewidget;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.vk.sdk.VKUIHelper;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

public class ConfigActivity extends Activity implements OnClickListener {
	
	private final static String TAG = "ConfigActivity";

	int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
	Intent resultValue;

	public final static String WIDGET_PREF = "widget_pref";
	public final static String WIDGET_DATE = "widget_date_";
	public final static String WIDGET_YEARS = "widget_years_";
	public final static String WIDGET_COUNT = "widget_count_";
	public final static String WIDGET_PRICE = "widget_price_";
	
	private EditText editTextYears, editTextCount, editTextPrice;
	private DatePicker datePicker;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config);
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
	    
	    ((Button)findViewById(R.id.buttonOk)).setOnClickListener(this);
	    datePicker.setMaxDate(Calendar.getInstance().getTimeInMillis());
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
	private void setupUI() {
		editTextYears = (EditText) findViewById(R.id.editTextYears);
		editTextCount = (EditText) findViewById(R.id.editTextCount);
		editTextPrice = (EditText) findViewById(R.id.editTextPrice);
		datePicker = (DatePicker) findViewById(R.id.datePicker);
	}
	private void fillUI(Bundle extras) {
		SharedPreferences sp = this.getSharedPreferences(ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE);
		
		String widgetData = sp.getString(ConfigActivity.WIDGET_DATE + widgetID, null);
		if (widgetData == null)
			return;
		String widgetCount = sp.getString(ConfigActivity.WIDGET_COUNT + widgetID, null);
		String widgetPrice = sp.getString(ConfigActivity.WIDGET_PRICE + widgetID, null);
		String widgetYears = sp.getString(ConfigActivity.WIDGET_YEARS + widgetID, null);
		
		int count = 0, years = 0;
	    float price = 0;
	    count = Integer.valueOf(widgetCount);
        price = Float.valueOf(widgetPrice);
        years = Integer.valueOf(widgetYears);
		
		editTextYears.setText(String.valueOf(years));
		editTextCount.setText(String.valueOf(count));
		editTextPrice.setText(String.valueOf(price));
		
		SimpleDateFormat  format = new SimpleDateFormat("dd.MM.yyyy");
		Log.v(TAG, "date " + widgetData);
	    try {
	    	Date date = format.parse(widgetData);
	    	datePicker.updateDate(date.getYear()+1900, date.getMonth(), date.getDate());
	    } catch (ParseException e) {  
	        Log.v(TAG, "Неудалось распарсить дату " + e.getMessage()); 
	    }
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.buttonOk:
				okClick();
				break;
		}
	}
	private void okClick() {
		// Записываем значения с экрана в Preferences
	    SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
	    Editor editor = sp.edit();
	    // Перед записью нужно проверить формат даты
	    int day = datePicker.getDayOfMonth();
		int month = datePicker.getMonth() + 1;
		int year = datePicker.getYear();
		String dtStart = String.format("%s.%s.%s", day, month, year);
	    // Зачем проверять формат, если используется DatePicker???
		/*
		SimpleDateFormat  format = new SimpleDateFormat("dd.MM.yyyy");  
	    try {  
	        Date date = format.parse(dtStart);  
	    } catch (ParseException e) {  
	        e.printStackTrace(); 
	        finish();
	    }
	    */
	    editor.putString(WIDGET_DATE + widgetID, dtStart);
	    editor.putString(WIDGET_YEARS + widgetID, editTextYears.getText().toString());
	    editor.putString(WIDGET_COUNT + widgetID, editTextCount.getText().toString());
	    editor.putString(WIDGET_PRICE + widgetID, editTextPrice.getText().toString());
	    editor.commit();
	    
	    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
	    AppWidgetProviderInfo info = AppWidgetManager.getInstance(this).getAppWidgetInfo(widgetID);
	    Log.v(TAG, info.provider.getClassName());
	    String className = info.provider.getClassName();
	    if (className.equalsIgnoreCase("ru.hellslade.nosmokewidget.WidgetMain")) {
	    	WidgetMain.updateWidget(this, appWidgetManager, sp, widgetID);
	    } else if (className.equalsIgnoreCase("ru.hellslade.nosmokewidget.Widget1")) {
	    	Widget1.updateWidget(this, appWidgetManager, sp, widgetID);
	    } else if (className.equalsIgnoreCase("ru.hellslade.nosmokewidget.Widget2")) {
	    	Widget2.updateWidget(this, appWidgetManager, sp, widgetID);
	    } else if (className.equalsIgnoreCase("ru.hellslade.nosmokewidget.Widget3")) {
	    	Widget3.updateWidget(this, appWidgetManager, sp, widgetID);
	    } else if (className.equalsIgnoreCase("ru.hellslade.nosmokewidget.Widget4")) {
	    	Widget4.updateWidget(this, appWidgetManager, sp, widgetID);
	    } else if (className.equalsIgnoreCase("ru.hellslade.nosmokewidget.Widget5")) {
	    	Widget5.updateWidget(this, appWidgetManager, sp, widgetID);
	    }
	    
	    // положительный ответ 
	    setResult(RESULT_OK, resultValue);
	    
	    Log.d(TAG, "finish config " + widgetID);
	    finish();
	}
}
