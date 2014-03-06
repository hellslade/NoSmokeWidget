package ru.hellslade.nosmokewidget;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetMain extends AppWidgetProvider {
	public static final String APPWIDGET_DIALOG = "ru.hellslade.nosmokewidget.APPWIDGET_DIALOG";
	private final static String TAG = "WidgetMain";

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		Log.d(TAG, "onEnabled");
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Log.d(TAG, "onUpdate " + Arrays.toString(appWidgetIds));
		SharedPreferences sp = context.getSharedPreferences(Utils.WIDGET_PREF, Context.MODE_PRIVATE);
        for (int id : appWidgetIds) {
        	updateWidget(context, appWidgetManager, sp, id);
        }
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		Log.d(TAG, "onDeleted " + Arrays.toString(appWidgetIds));
		// Удаляем Preferences
	    Editor editor = context.getSharedPreferences(Utils.WIDGET_PREF, Context.MODE_PRIVATE).edit();
	    for (int widgetID : appWidgetIds) {
	    	editor.remove(Utils.WIDGET_DATE + widgetID);
	    	editor.remove(Utils.WIDGET_YEARS + widgetID);
	    	editor.remove(Utils.WIDGET_COUNT + widgetID);
	    	editor.remove(Utils.WIDGET_PRICE + widgetID);
	    }
	    editor.commit();
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		Log.d(TAG, "onDisabled");
	}
	
	static void updateWidget(Context context, AppWidgetManager appWidgetManager, SharedPreferences sp, int widgetID) {
		Log.d(TAG, "updateWidget " + widgetID);
		Resources res = context.getResources();
		// Читаем параметры Preferences
		String widgetData = sp.getString(Utils.WIDGET_DATE + widgetID, null);
		if (widgetData == null)
			return;
		String widgetCount = sp.getString(Utils.WIDGET_COUNT + widgetID, null);
		String widgetPrice = sp.getString(Utils.WIDGET_PRICE + widgetID, null);
		String widgetYears = sp.getString(Utils.WIDGET_YEARS + widgetID, null);
		
		// Настраиваем внешний вид виджета
		RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget);
		// Считаем количество дней
		String dtStart = widgetData;
	    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Utils.mLocale);
	    long daysCount = 0;
	    int count = 0, years = 0;
	    float price = 0;
	    try {  
	        Date date = format.parse(dtStart);
	        Calendar now = Calendar.getInstance();
	        daysCount = Utils.daysBetween(date, now.getTime());
	        widgetView.setTextViewText(R.id.noSmokeDays, String.format(res.getString(R.string.no_smoke_days), daysCount, Utils.getDaysLabel(context, daysCount)));
	        
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
	    
	    widgetView.setTextViewText(R.id.noSmokeCount, String.format(res.getString(R.string.no_smoke_count), count*daysCount));
	    widgetView.setTextViewText(R.id.noSmokePrice, String.format(res.getString(R.string.no_smoke_price), price*daysCount));
	    
	    widgetView.setTextViewText(R.id.SmokeDays, String.format(res.getString(R.string.smoke_days), years, Utils.getYearsLabel(context, years)));
	    widgetView.setTextViewText(R.id.SmokeCount, String.format(res.getString(R.string.smoke_count), years*365*count));
	    widgetView.setTextViewText(R.id.SmokePrice, String.format(res.getString(R.string.smoke_price), years*365*count*15)); // 15 затяжек за сигарету
	    
	    Intent dialogIntent = new Intent(context, DialogActivity.class);
	    dialogIntent.setAction(APPWIDGET_DIALOG);
	    dialogIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
	    PendingIntent pIntent = PendingIntent.getActivity(context, widgetID, dialogIntent, 0);
	    widgetView.setOnClickPendingIntent(R.id.configLayout, pIntent);
	    /*
	    // Конфигурационный экран
	    Intent configIntent = new Intent(context, ConfigActivity.class);
	    configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
	    configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
	    PendingIntent pIntent = PendingIntent.getActivity(context, widgetID, configIntent, 0);
	    widgetView.setOnClickPendingIntent(R.id.configLayout, pIntent);
	    */
		// Обновляем виджет
		appWidgetManager.updateAppWidget(widgetID, widgetView);
	}
}