package hjh.nit.com.timeddeleten.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.EmptyUtils;
import com.blankj.utilcode.util.SPUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import hjh.nit.com.timeddeleten.entity.AppInfo;
import hjh.nit.com.timeddeleten.receiver.AlarmReceiver;
import hjh.nit.com.timeddeleten.util.ConstValues;

public class AutoOpenStopService extends Service {
    private ArrayList<AppInfo> appList = new ArrayList<AppInfo>();
    private ArrayList<AppInfo> runAppList = new ArrayList<AppInfo>();
    private ActivityManager activityManager;
    private String mAppName;
    private int mCount=0;
    private int mKillCount=0;
    private AlarmManager mAlarmManager;
    private Calendar calendar;
    private Handler mHandler;
    private int openCloseTimeHour,openCloseTimeMinute,openCloseTimeSecond;


    public AutoOpenStopService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("AutoOpenStopService", "run: onCreate");
        mHandler=new Handler(Looper.getMainLooper());
        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (!EmptyUtils.isEmpty(SPUtils.getInstance().getString(ConstValues.APP_NAME))&&!EmptyUtils.isEmpty(SPUtils.getInstance().getString(ConstValues.OPEN_CLOSE_TIME))){
            mAppName=SPUtils.getInstance().getString(ConstValues.APP_NAME);
            openCloseTimeHour=SPUtils.getInstance().getInt(ConstValues.OPEN_CLOSE_TIME_HOUR);
            openCloseTimeMinute=SPUtils.getInstance().getInt(ConstValues.OPEN_CLOSE_TIME_MINUTE);
            openCloseTimeSecond=SPUtils.getInstance().getInt(ConstValues.OPEN_CLOSE_TIME_SECOND);
            getAppList(mAppName);
//            getRunAppList(this,appList);
            mHandler.post(new Runnable(){
                public void run(){
                    Toast.makeText(getApplicationContext(),"自动开启关闭已启动",Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            mHandler.post(new Runnable(){
                public void run(){
                    Toast.makeText(getApplicationContext(),"启动失败",Toast.LENGTH_SHORT).show();
                }
            });
            onDestroy();
        }
        if(appList.size()==0){
            onDestroy();
        }else {
            for(int i=0;i<appList.size()/2;i++){
                startActivity(appList.get(mCount).getAppIntent());
            }
            mCount=appList.size()/2;
//            if(runAppList.size()==0){
//                mHandler.post(new Runnable(){
//                    public void run(){
//                        Toast.makeText(getApplicationContext(),"没有指定启动中的程序",Toast.LENGTH_SHORT).show();
//                    }
//                });
//                onDestroy();
//            }
        }
        calendar=Calendar.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("AutoOpenStopService", "run: onStartCommand");
        if(appList.size()==0){
            mHandler.post(new Runnable(){
                public void run(){
                    Toast.makeText(getApplicationContext(),"未找到包含该文字的应用,服务关闭",Toast.LENGTH_SHORT).show();
                }
            });
            onDestroy();
        }else {
//            if(runAppList.size()==0) {
//                mHandler.post(new Runnable() {
//                    public void run() {
//                        Toast.makeText(getApplicationContext(), "未找到包含该文字的运行应用,服务关闭", Toast.LENGTH_SHORT).show();
//                    }
//                });
//                onDestroy();
//            }else{
            calendar.add(Calendar.HOUR_OF_DAY, openCloseTimeHour);
            calendar.add(Calendar.MINUTE, openCloseTimeMinute);
            calendar.add(Calendar.SECOND, openCloseTimeSecond);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    Intent intent1 = new Intent(AutoOpenStopService.this, AlarmReceiver.class);
                    intent1.setAction("android.intent.action.AUTO_OPEN_CLOSE");
                    PendingIntent pi = PendingIntent.getBroadcast(AutoOpenStopService.this, 1, intent1, 0);
                    mAlarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
                }
            }).run();
            if (mCount < appList.size()) {
                startActivity(appList.get(mCount).getAppIntent());
                Toast.makeText(getApplicationContext(), "启动" + appList.get(mCount).getAppName(), Toast.LENGTH_SHORT).show();
                mCount++;
            } else {
                mCount = 0;
                startActivity(appList.get(mCount).getAppIntent());
                Toast.makeText(getApplicationContext(), "启动" + appList.get(mCount).getAppName(), Toast.LENGTH_SHORT).show();
                mCount++;
            }
            if (mKillCount < appList.size()) {
                activityManager.killBackgroundProcesses(appList.get(mKillCount).getPkgName());
                Toast.makeText(getApplicationContext(), "关闭" + appList.get(mKillCount).getAppName(), Toast.LENGTH_SHORT).show();
                mKillCount++;
            } else {
                mKillCount = 0;
                activityManager.killBackgroundProcesses(appList.get(mKillCount).getPkgName());
                Toast.makeText(getApplicationContext(), "关闭" + appList.get(mKillCount).getAppName(), Toast.LENGTH_SHORT).show();
                mKillCount++;
            }
        }
//        }
        return super.onStartCommand(intent, flags, startId);
//        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("AutoOpenStopService", "run: onDestroy");
        if (mAlarmManager!=null) {
            Intent intent1 = new Intent(AutoOpenStopService.this, AlarmReceiver.class);
            intent1.setAction("android.intent.action.AUTO_OPEN_CLOSE");
            PendingIntent pi = PendingIntent.getBroadcast(AutoOpenStopService.this, 1, intent1, 0);
            mAlarmManager.cancel(pi);
        }
    }

    private void getAppList(String appName) {
        PackageManager pm = this.getPackageManager();
        // Return a List of all packages that are installed on the device.
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            // 判断系统/非系统应用
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)    // 非系统应用
            {
                //判断是否是需要对象
                int result1 = packageInfo.applicationInfo.loadLabel(pm).toString().indexOf(appName);
                if(result1!=-1){
                    AppInfo info = new AppInfo();
                    info.setAppName(packageInfo.applicationInfo.loadLabel(pm).toString());
                    info.setPkgName(packageInfo.packageName);
                    info.setAppIcon(packageInfo.applicationInfo.loadIcon(pm));
                    // 获取该应用安装包的Intent，用于启动该应用
                    info.setAppIntent(pm.getLaunchIntentForPackage(packageInfo.packageName));
                    appList.add(info);
                }else{
                    //非名字匹配应用
                }
            } else {
                // 系统应用　　　　　　　　
            }
        }
    }

    private void getRunAppList(Context context,ArrayList<AppInfo> appLists) {
        if (appLists.size()!=0){
            for(int i=0;i<appLists.size();i++){
                int uid = getPackageUid(context,appLists.get(i).getPkgName());
                if (uid > 0) {
                    boolean rstA = isAppRunning(context, appLists.get(i).getPkgName());
                    boolean rstB = isProcessRunning(context, uid);
                    if (rstA || rstB) {
//                    if (rstA) {
                        //指定包名的程序正在运行中
                        runAppList.add(appLists.get(i));
                    } else {
                        //指定包名的程序未在运行中
                    }
                }
            }
        }

    }

    private static int getPackageUid(Context context, String packageName) {
            try {
                ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(packageName, 0);
                if (applicationInfo != null) {
                    return applicationInfo.uid;
                }
            } catch (Exception e) {
                return -1;
            }
            return -1;
        }

    private static boolean isProcessRunning(Context context, int uid) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServiceInfos = am.getRunningServices(200);
        if (runningServiceInfos.size() > 0) {
            for (ActivityManager.RunningServiceInfo appProcess : runningServiceInfos){
                if (uid == appProcess.uid) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isAppRunning(Context context, String packageName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        if (list.size() <= 0) {
            return false;
        }
        for (ActivityManager.RunningTaskInfo info : list) {
            if (info.baseActivity.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }
}
