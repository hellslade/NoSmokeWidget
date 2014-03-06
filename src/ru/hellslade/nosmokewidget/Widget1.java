package ru.hellslade.nosmokewidget;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.util.Log;
import android.widget.RemoteViews;

public class Widget1 extends AppWidgetProvider {

	private final static String TAG = "Widget1";

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
		
		// Настраиваем внешний вид виджета
		RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_1);
		// Считаем количество дней
		String dtStart = widgetData;
	    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Utils.mLocale);
	    long daysCount = 0;
	    try {  
	        Date date = format.parse(dtStart);
	        Calendar now = Calendar.getInstance();
	        daysCount = Utils.daysBetween(date, now.getTime());
	        widgetView.setTextViewText(R.id.w1noSmokeDays, String.format(res.getString(R.string.w1_no_smoke_days), daysCount, Utils.getDaysLabel(context, daysCount)));
	    } catch (ParseException e) {  
	        e.printStackTrace(); 
	        return;
	    }
		
		// Обновляем виджет
		appWidgetManager.updateAppWidget(widgetID, widgetView);
	}
}