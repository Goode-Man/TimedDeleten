package hjh.nit.com.timeddeleten.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.EmptyUtils;
import com.blankj.utilcode.util.SPUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import hjh.nit.com.timeddeleten.receiver.AlarmReceiver;
import hjh.nit.com.timeddeleten.util.ConstValues;

public class AutoCleanService extends Service {
    int cleanTimeHour,cleanTimeMinute;
    Calendar calendar;
    long mNow;
    AlarmManager mAlarmManager;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
    Handler mHandler;

    public AutoCleanService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("AutoClean", "run: onCreate");
        mHandler=new Handler(Looper.getMainLooper());
        if (!EmptyUtils.isEmpty(SPUtils.getInstance().getString(ConstValues.CLEAN_TIME))){
            cleanTimeHour=SPUtils.getInstance().getInt(ConstValues.CLEAN_TIME_HOUR);
            cleanTimeMinute=SPUtils.getInstance().getInt(ConstValues.CLEAN_TIME_MINUTE);
            calendar=Calendar.getInstance();
            mHandler.post(new Runnable(){
                public void run(){
                    Toast.makeText(getApplicationContext(),"自动清理服务已启动",Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            mHandler.post(new Runnable(){
                public void run(){
                    Toast.makeText(getApplicationContext(),"启动失败",Toast.LENGTH_SHORT).show();
                }
            });            onDestroy();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("AutoClean", "run: startCommand");
        mNow=System.currentTimeMillis();
        calendar.set(Calendar.HOUR_OF_DAY, cleanTimeHour);
        calendar.set(Calendar.MINUTE, cleanTimeMinute);
        calendar.set(Calendar.SECOND, 59);
        if(mNow>calendar.getTimeInMillis()){
            calendar.add(Calendar.DATE,1);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
//                Date date = new Date(mNow);
//                Date dates = new Date(calendar.getTimeInMillis());
//                System.out.println(simpleDateFormat.format(date));
//                System.out.println(simpleDateFormat.format(dates));
                mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent intent1=new Intent(AutoCleanService.this,AlarmReceiver.class);
                intent1.setAction("android.intent.action.AUTO_CLEAN");
                PendingIntent pi = PendingIntent.getBroadcast(AutoCleanService.this, 11, intent1, 0);
                mAlarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),pi);
            }
        }).run();
//        return super.onStartCommand(intent, flags, startId);
//        return START_STICKY;
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("AutoClean", "run: onDestroy");
        Intent intent1=new Intent(AutoCleanService.this,AlarmReceiver.class);
        intent1.setAction("android.intent.action.AUTO_CLEAN");
        PendingIntent pi = PendingIntent.getBroadcast(AutoCleanService.this, 11, intent1, 0);
        mAlarmManager.cancel(pi);
    }


}
