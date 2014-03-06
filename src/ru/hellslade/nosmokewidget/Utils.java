package ru.hellslade.nosmokewidget;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
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
        
        // Расчет ширины-высоты изображения в зависимости от dpi экрана
        float density = context.getResources().getDisplayMetrics().density;
        int dstWidth = (int)(250*density);
        int dstHeight = (int)(150*density);
        // Создаем битмап и канву
        Bitmap source = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(source);
        
        // Рисуем фон
        NinePatchDrawable npd = (NinePatchDrawable)context.getResources().getDrawable(R.drawable.frame);
        npd.setBounds(0, 0, dstWidth, dstHeight);
        npd.draw(canvas);
        
        // Рисуем лого в правом нижнем углу
        Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_icon);
        int width = logo.getWidth();
        int height = logo.getHeight();
        int left = (dstWidth - width)-10;
        int top = (dstHeight - height)-20;
        canvas.drawBitmap(logo, left, top, null);
        logo.recycle();
        
        // Считаем количество дней
        String dtStart = widgetData;
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", mLocale);
        try {  
            Date date = format.parse(dtStart);
            Calendar now = Calendar.getInstance();
            
            // 
            int[] dd = dayMonthYearBetween(date, now.getTime());
            
//            Log.v(TAG, "year " + dd[2]);
//            Log.v(TAG, "month " + dd[1]);
//            Log.v(TAG, "day " + dd[0]);
            
        } catch (ParseException e) {  
            Log.v(TAG, "Ошибка парсинга даты" + e.getMessage()); 
            return null;
        } catch (NumberFormatException e) {
            Log.v(TAG, "Введено неправильное число" + e.getMessage()); 
            return null;
        }
        
        Bitmap bitmap = source.copy(source.getConfig(), false);
        source.recycle();
        return bitmap;
    }
    
    private static int[] dayMonthYearBetween(Date start, Date end) {
        LocalDate sdate = new LocalDate(start);
        LocalDate edate = new LocalDate(end);
        
        PeriodType yearMonthDay = PeriodType.yearMonthDay();
        Period difference = new Period(sdate, edate, yearMonthDay);
        
        int day   = difference.getDays();
        int month = difference.getMonths();
        int year  = difference.getYears();
        
        return new int[]{day, month, year};
        
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
        
        // Создаем кисть для вывода текста
        float density = context.getResources().getDisplayMetrics().density;
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
        NinePatchDrawable npd = (NinePatchDrawable)context.getResources().getDrawable(R.drawable.frame);
        npd.setBounds(0, 0, dstWidth, dstHeight);
        npd.draw(canvas);
        // Рисуем лого в правом нижнем углу
        Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_icon);
        int width = logo.getWidth();
        int height = logo.getHeight();
        int left = (dstWidth - width)/2;
        int top = (dstHeight - height)/2;
        Paint p = new Paint();
        p.setAlpha(50);
        Matrix matrix = new Matrix();
        int scaleFactor = dstHeight/height;
        matrix.postScale(scaleFactor, scaleFactor, width/2, height/2);
        matrix.postTranslate(left, top);
        canvas.drawBitmap(logo, matrix, p);
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
            Log.v(TAG, "Ошибка парсинга даты" + e.getMessage()); 
            return null;
        } catch (NumberFormatException e) {
            Log.v(TAG, "Введено неправильное число" + e.getMessage()); 
            return null;
        }
        s = String.format(context.getResources().getString(R.string.no_smoke_count), count*daysCount);
        y += (int)(yoff+vert_space);
        canvas.drawText(s, x, y, mTextPaint);
        
        s = String.format(context.getResources().getString(R.string.no_smoke_price), price*daysCount);
        y += (int)(yoff+vert_space);
        canvas.drawText(s, x, y, mTextPaint);
        
        // Красный текст
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
    public static String getDaysLabel(Context context, long days) {
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
    public static String getYearsLabel(Context context, long years) {
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
