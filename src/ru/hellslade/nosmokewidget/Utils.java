package ru.hellslade.nosmokewidget;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.NinePatchDrawable;
import android.text.TextPaint;
import android.util.Log;

public class Utils {
    private static final String TAG = "Utils";
    
    public static final Locale mLocale = Locale.US;
    
    public final static String WIDGET_PREF = "widget_pref";
    
    public final static String WIDGET_DATE = "widget_date_";
    public final static String WIDGET_YEARS = "widget_years_";
    public final static String WIDGET_COUNT = "widget_count_";
    public final static String WIDGET_PRICE = "widget_price_";

    public static Bitmap getShareBitmap(Context context, int widgetId) {
        SharedPreferences sp = context.getSharedPreferences(WIDGET_PREF, Context.MODE_PRIVATE);
        
        String widgetData = sp.getString(WIDGET_DATE + widgetId, null);
        if (widgetData == null)
            return null;
        String widgetCount = sp.getString(WIDGET_COUNT + widgetId, null);
        String widgetPrice = sp.getString(WIDGET_PRICE + widgetId, null);
        String widgetYears = sp.getString(WIDGET_YEARS + widgetId, null);
        
        // ������ ������-������ ����������� � ����������� �� dpi ������
        float density = context.getResources().getDisplayMetrics().density;
        int dstWidth = (int)(250*density);
        int dstHeight = (int)(150*density);
        // ������� ������ � �����
        Bitmap source = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(source);
        
        // ������ ���
        NinePatchDrawable npd = (NinePatchDrawable)context.getResources().getDrawable(R.drawable.frame);
        npd.setBounds(0, 0, dstWidth, dstHeight);
        npd.draw(canvas);
        
        // ������� ���������� ����
        String dtStart = widgetData;
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", mLocale);
        long daysCount = 0;
        int count = 0, years = 0;
        float price = 0;
        try {  
            Date date = format.parse(dtStart);
            Calendar now = Calendar.getInstance();
            
            int now_day = now.get(Calendar.DAY_OF_MONTH);
            int now_month = now.get(Calendar.MONTH)+1;
            int now_year = now.get(Calendar.YEAR);
            
            int day = date.getDate();
            int month = date.getMonth()+1;
            int year = date.getYear()+1900;
            
//            Log.v(TAG, "day " + day);
//            Log.v(TAG, "month " + month);
//            Log.v(TAG, "year " + year);
            
            // ���� �� �������
            daysCount = daysBetween(date, now.getTime());
            
            // ���������� ������� � ����
            count = Integer.valueOf(widgetCount);
            // ��������� ����� �����
            price = Float.valueOf(widgetPrice);
            // ��� �������
            years = Integer.valueOf(widgetYears);
        } catch (ParseException e) {  
            Log.v(TAG, "������ �������� ����" + e.getMessage()); 
            return null;
        } catch (NumberFormatException e) {
            Log.v(TAG, "������� ������������ �����" + e.getMessage()); 
            return null;
        }
        
        Bitmap bitmap = source.copy(source.getConfig(), false);
        source.recycle();
        return bitmap;
    }
    
    public static Bitmap getShareBitmap_old(Context context, int widgetId) {
        TextPaint mTextPaint;
        SharedPreferences sp = context.getSharedPreferences(WIDGET_PREF, Context.MODE_PRIVATE);
        
        String widgetData = sp.getString(WIDGET_DATE + widgetId, null);
        if (widgetData == null)
            return null;
        String widgetCount = sp.getString(WIDGET_COUNT + widgetId, null);
        String widgetPrice = sp.getString(WIDGET_PRICE + widgetId, null);
        String widgetYears = sp.getString(WIDGET_YEARS + widgetId, null);
        
        // ������� ����� ��� ������ ������
        float density = context.getResources().getDisplayMetrics().density;
        mTextPaint = new TextPaint(TextPaint.DEV_KERN_TEXT_FLAG | TextPaint.LINEAR_TEXT_FLAG);
        int[] drawableState = new int[] { 0, 1 };
        mTextPaint.drawableState = drawableState;
        mTextPaint.density = density;
        mTextPaint.setTextSize(50);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(Color.parseColor("#005500")); // ������� �����
        int dstWidth = (int)(250*density);
        int dstHeight = (int)(150*density);
        // ������� ������ � �����
        Bitmap source = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(source);
        
        // ������ ���
        NinePatchDrawable npd = (NinePatchDrawable)context.getResources().getDrawable(R.drawable.frame);
        npd.setBounds(0, 0, dstWidth, dstHeight);
        npd.draw(canvas);
        // ������ ���� � ������ ������ ����
        Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_icon);
        int width = logo.getWidth();
        int height = logo.getHeight();
        int left = (dstWidth - width)-10;
        int top = (dstHeight - height)-20;
        canvas.drawBitmap(logo, left, top, null);
        logo.recycle();
        // ������� ��� ������ ������
        // ������ ����� ������ (������ �������� �� �-����������)
        float yoff = mTextPaint.descent() - mTextPaint.ascent();
        // ����� ����� 6
        float all_text_height = yoff*6; // ����� ������ �����
        float all_vert_space = dstHeight - all_text_height; // ����� ������ �������� ����� ��������
        float vert_space = (all_vert_space/5)-10; // ������������� ��������
        String s; // ��������� ������
        // ��������� ���������� ������ ������
        int x = 20;
        int y = (int)yoff;
        
        // ������� ���������� ����
        String dtStart = widgetData;
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", mLocale);
        long daysCount = 0;
        int count = 0, years = 0;
        float price = 0;
        try {  
            Date date = format.parse(dtStart);
            Calendar now = Calendar.getInstance();
            daysCount = daysBetween(date, now.getTime());
            s = String.format(context.getResources().getString(R.string.no_smoke_days), daysCount, getDaysLabel(context, daysCount));
            
            canvas.drawText(s, x, y, mTextPaint);
            
            count = Integer.valueOf(widgetCount);
            price = Float.valueOf(widgetPrice);
            years = Integer.valueOf(widgetYears);
        } catch (ParseException e) {  
            Log.v(TAG, "������ �������� ����" + e.getMessage()); 
            return null;
        } catch (NumberFormatException e) {
            Log.v(TAG, "������� ������������ �����" + e.getMessage()); 
            return null;
        }
        s = String.format(context.getResources().getString(R.string.no_smoke_count), count*daysCount);
        y += (int)(yoff+vert_space);
        canvas.drawText(s, x, y, mTextPaint);
        
        s = String.format(context.getResources().getString(R.string.no_smoke_price), price*daysCount);
        y += (int)(yoff+vert_space);
        canvas.drawText(s, x, y, mTextPaint);
        
        // ������� �����
        mTextPaint.setColor(Color.parseColor("#990000"));
        
        s = String.format(context.getResources().getString(R.string.smoke_days), years, getYearsLabel(context, years));
        y += (int)(yoff+vert_space);
        canvas.drawText(s, x, y, mTextPaint);
        
        s = String.format(context.getResources().getString(R.string.smoke_count), years*365*count);
        y += (int)(yoff+vert_space);
        canvas.drawText(s, x, y, mTextPaint);
        
        s = String.format(context.getResources().getString(R.string.smoke_price), years*365*count*15);
        y += (int)(yoff+vert_space);
        canvas.drawText(s, x, y, mTextPaint);
        
        Bitmap bitmap = source.copy(source.getConfig(), false);
        source.recycle();
        /*
        try {
            mBitmap.compress(CompressFormat.JPEG, 90, new FileOutputStream(new File("/storage/emulated/0/Download/1.jpg")));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        */
        return bitmap;
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
    
     /* ������ ���������� ��������� ���������� ����
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
    public static String getDaysLabel(Context context, long days) {
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
    public static String getYearsLabel(Context context, long years) {
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
    
}
