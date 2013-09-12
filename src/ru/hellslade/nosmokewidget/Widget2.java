package ru.hellslade.nosmokewidget;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.util.Log;
import android.widget.RemoteViews;

public class Widget2 extends AppWidgetProvider {

	private final static String TAG = "Widget2";

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		Log.d(TAG, "onEnabled");
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Log.d(TAG, "onUpdate " + Arrays.toString(appWidgetIds));
		SharedPreferences sp = context.getSharedPreferences(ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE);
        for (int id : appWidgetIds) {
        	updateWidget(context, appWidgetManager, sp, id);
        }
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		Log.d(TAG, "onDeleted " + Arrays.toString(appWidgetIds));
		// ������� Preferences
	    Editor editor = context.getSharedPreferences(ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit();
	    for (int widgetID : appWidgetIds) {
	    	editor.remove(ConfigActivity.WIDGET_DATE + widgetID);
	    	editor.remove(ConfigActivity.WIDGET_YEARS + widgetID);
	    	editor.remove(ConfigActivity.WIDGET_COUNT + widgetID);
	    	editor.remove(ConfigActivity.WIDGET_PRICE + widgetID);
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
		// ������ ��������� Preferences
		String widgetData = sp.getString(ConfigActivity.WIDGET_DATE + widgetID, null);
		if (widgetData == null)
			return;
		
		// ����������� ������� ��� �������
		RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_2);
		// ������� ���������� ����
		String dtStart = widgetData;
	    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
	    long daysCount = 0;
	    try {  
	        Date date = format.parse(dtStart);
	        Calendar now = Calendar.getInstance();
	        daysCount = daysBetween(date, now.getTime());
	        widgetView.setTextViewText(R.id.w2noSmokeDays, String.format(res.getString(R.string.w2_no_smoke_days), daysCount, getDaysLabel(context, daysCount)));
	    } catch (ParseException e) {  
	        e.printStackTrace(); 
	        return;
	    }
		
		// ��������� ������
		appWidgetManager.updateAppWidget(widgetID, widgetView);
	}
	/**
	 * ������ ���������� ��������� ���������� ����
	 * <string-array name="days">
	 *   <item>����</item>
	 *   <item>���</item>
	 *   <item>����</item>
	 * </string-array>
	 * ���� ������������ ����� 1, �� "����"
	 * ����� ���� ��������� ����� 0 ��� >=5, �� "����"
	 * ����� ���� ��������� ����� 2,3 ��� 4, �� "���"
	 * ����� "����".
	 * @param context
	 * @param days
	 * @return
	 */
	private static String getDaysLabel(Context context, long days) {
		// % -- mod
		// / -- div
		Resources res = context.getResources();
		String result = res.getStringArray(R.array.days)[0];
		
		long prevprev = (days % 100) / 10; // ������������� �����
		long prev = days % 10; // ��������� �����
		
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
	 * ������ ���������� ��������� ���������� ���
	 * <string-array name="years">
	 *   <item>���</item>
	 *   <item>����</item>
	 *   <item>���</item>
	 * </string-array>
	 * ���� ������������ ����� 1, �� "���"
	 * ����� ���� ��������� ����� 0 ��� >=5, �� "���"
	 * ����� ���� ��������� ����� 2,3 ��� 4, �� "����"
	 * ����� "���".
	 * @param context
	 * @param years
	 * @return
	 */
	private static String getYearsLabel(Context context, long years) {
		// % -- mod
		// / -- div
		Resources res = context.getResources();
		String result = res.getStringArray(R.array.years)[0];
		
		long prevprev = (years % 100) / 10; // ������������� �����
		long prev = years % 10; // ��������� �����
		
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
}