package hjh.nit.com.timeddeleten.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import hjh.nit.com.timeddeleten.service.AutoCleanService;
import hjh.nit.com.timeddeleten.service.AutoOpenStopService;
import hjh.nit.com.timeddeleten.service.CleanService;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.AUTO_CLEAN")){
            Log.i("AlarmReceiver", "onReceive: android.intent.action.AUTO_CLEAN");
            Intent intentClean = new Intent(context, CleanService.class);
            context.startService(intentClean);
            Intent intentAuto = new Intent(context, AutoCleanService.class);
            context.startService(intentAuto);
        }else if(intent.getAction().equals("android.intent.action.CLEAN_SUCCESS")){
            Log.i("AlarmReceiver", "onReceive: android.intent.action.CLEAN_SUCCESS");
            Intent intentClean = new Intent(context, CleanService.class);
            context.stopService(intentClean);
        }else if(intent.getAction().equals("android.intent.action.AUTO_OPEN_CLOSE")){
            Log.i("AlarmReceiver", "onReceive: android.intent.action.AUTO_OPEN_CLOSE");
            Intent intentClean = new Intent(context, AutoOpenStopService.class);
            context.startService(intentClean);
        }

    }
}
