package hjh.nit.com.timeddeleten.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import com.blankj.utilcode.util.EmptyUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hjh.nit.com.timeddeleten.receiver.AlarmReceiver;
import hjh.nit.com.timeddeleten.util.ConstValues;
import hjh.nit.com.timeddeleten.util.DeleteFileUtil;

public class CleanService extends Service {

    List<File> mFileList=new ArrayList<>();
    List<String> mFileNameList=new ArrayList<>();
    List<String> mFileListUser=new ArrayList<>();
    Handler mHandler;
    StringBuilder errorStr=new StringBuilder();
    private CleanAsyncTask mCleanAsyncTask;

    public CleanService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler=new Handler(Looper.getMainLooper());
        String dir=ConstValues.PATH_XITONG+ConstValues.PATH_FENSHEN;
        String dir1=ConstValues.PATH_XITONG_FENSHEN;
        String dir2=ConstValues.PATH_XITONG_WEIXIN;
        if (!dir.endsWith(File.separator))
            dir = dir + File.separator;
        File dirFile1 = new File(dir1);
        if (dirFile1.exists() && dirFile1.isDirectory()) {
            mFileList.add(dirFile1);
        }
        File dirFile2 = new File(dir2);
        if (dirFile2.exists() && dirFile2.isDirectory()) {
            mFileList.add(dirFile2);
        }
        File dirFile = new File(dir);
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            mHandler.post(new Runnable(){
                public void run(){
                    Toast.makeText(getApplicationContext(),"路径不存在",Toast.LENGTH_SHORT).show();
                }
            });
            onDestroy();
        }else {
            File[] files = dirFile.listFiles();
            for (int i = 0; i < files.length; i++) {
                if(files[i].isDirectory()) {
                    mFileList.add(new File(files[i].getAbsolutePath()+ConstValues.PATH_FENSHEN_WEIXIN+File.separator));
                }
            }
            for(int i = 0; i < mFileList.size(); i++){
                File[] filesUser=mFileList.get(i).listFiles();
                if(filesUser!=null){
                    for(int j=0;j<filesUser.length;j++){
                        if(filesUser[j].getName().length()>=20&&filesUser[j].isDirectory()){
                            mFileListUser.add(filesUser[j].getAbsolutePath());
                            mFileNameList.add(mFileList.get(i).getParentFile().getName());
                        }
                    }
                }

            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, final int flags, int startId) {
//        mHandler=new Handler(Looper.getMainLooper());
//        mHandler.post(new Runnable(){
//            public void run(){
//                Toast.makeText(getApplicationContext(),"开始清理",Toast.LENGTH_SHORT).show();
//            }
//        });
//        final StringBuilder errorStr=new StringBuilder();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                File dirFile;
//                for(int i=0;i<mFileList.size();i++){
//                    DeleteFileUtil.deleteDirectory(mFileList.get(i)+ConstValues.PATH_FENSHEN_WEIXIN_LOG);
//                }
//                for(int i=0;i<mFileListUser.size();i++){
//                    dirFile = new File(mFileListUser.get(i)+ConstValues.PATH_FENSHEN_ATTACHMENT);
//                    if(dirFile.exists()){
//                        DeleteFileUtil.deleteDirectory(mFileListUser.get(i)+ConstValues.PATH_FENSHEN_ATTACHMENT);
//                    }else {
//                        errorStr.append(mFileNameList.get(i)+"/..."+ConstValues.PATH_FENSHEN_ATTACHMENT+"未找到\n");
//                    }
//                    dirFile = new File(mFileListUser.get(i)+ConstValues.PATH_FENSHEN_IMAGE);
//                    if(dirFile.exists()) {
//                        DeleteFileUtil.deleteDirectory(mFileListUser.get(i) + ConstValues.PATH_FENSHEN_IMAGE);
//                    }else {
//                        errorStr.append(mFileNameList.get(i)+"/..."+ConstValues.PATH_FENSHEN_IMAGE+"未找到\n");
//                    }
//                    dirFile = new File(mFileListUser.get(i)+ConstValues.PATH_FENSHEN_IMAGE2);
//                    if(dirFile.exists()) {
//                        DeleteFileUtil.deleteDirectory(mFileListUser.get(i) + ConstValues.PATH_FENSHEN_IMAGE2);
//                    }else {
//                        errorStr.append(mFileNameList.get(i)+"/..."+ConstValues.PATH_FENSHEN_IMAGE2+"未找到\n");
//                    }
//                    dirFile = new File(mFileListUser.get(i)+ConstValues.PATH_FENSHEN_VIDEO);
//                    if(dirFile.exists()){
//                        DeleteFileUtil.deleteDirectory(mFileListUser.get(i)+ConstValues.PATH_FENSHEN_VIDEO);
//                    }else {
//                        errorStr.append(mFileNameList.get(i)+"/..."+ConstValues.PATH_FENSHEN_VIDEO+"未找到\n");
//                    }
//                    dirFile = new File(mFileListUser.get(i)+ConstValues.PATH_FENSHEN_VIDEO2);
//                    if(dirFile.exists()){
//                        DeleteFileUtil.deleteDirectory(mFileListUser.get(i)+ConstValues.PATH_FENSHEN_VIDEO2);
//                    }else {
//                        errorStr.append(mFileNameList.get(i)+"/..."+ConstValues.PATH_FENSHEN_VIDEO2+"未找到\n");
//                    }
//                }
//            }
//        }).run();
//        mHandler.post(new Runnable(){
//            public void run(){
//                if(mFileList.size()==0||mFileListUser.size()==0){
//                    Toast.makeText(getApplicationContext(), "未找到用户文件", Toast.LENGTH_LONG).show();
//                }else {
//                    if (EmptyUtils.isEmpty(errorStr.toString())) {
//                        Toast.makeText(getApplicationContext(), "清理完成", Toast.LENGTH_LONG).show();
//                    } else {
//                        Toast.makeText(getApplicationContext(), errorStr.toString(), Toast.LENGTH_LONG).show();
//                        System.out.print(errorStr.toString());
//                    }
//                }
//            }
//        });
//        Intent intent1=new Intent(CleanService.this,AlarmReceiver.class);
//        intent1.setAction("android.intent.action.CLEAN_SUCCESS");
//        sendBroadcast(intent1);
        mCleanAsyncTask=new CleanAsyncTask();
        mCleanAsyncTask.execute();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    class CleanAsyncTask extends AsyncTask{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mHandler=new Handler(Looper.getMainLooper());
            mHandler.post(new Runnable(){
                public void run(){
                    Toast.makeText(getApplicationContext(),"开始清理",Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            File dirFile;
            for(int i=0;i<mFileList.size();i++){
                DeleteFileUtil.deleteDirectory(mFileList.get(i)+ConstValues.PATH_FENSHEN_WEIXIN_LOG);
            }
            for(int i=0;i<mFileListUser.size();i++){
                dirFile = new File(mFileListUser.get(i)+ConstValues.PATH_FENSHEN_ATTACHMENT);
                if(dirFile.exists()){
                    DeleteFileUtil.deleteDirectory(mFileListUser.get(i)+ConstValues.PATH_FENSHEN_ATTACHMENT);
                }else {
                    errorStr.append(mFileNameList.get(i)+"/..."+ConstValues.PATH_FENSHEN_ATTACHMENT+"未找到\n");
                }
                dirFile = new File(mFileListUser.get(i)+ConstValues.PATH_FENSHEN_IMAGE);
                if(dirFile.exists()) {
                    DeleteFileUtil.deleteDirectory(mFileListUser.get(i) + ConstValues.PATH_FENSHEN_IMAGE);
                }else {
                    errorStr.append(mFileNameList.get(i)+"/..."+ConstValues.PATH_FENSHEN_IMAGE+"未找到\n");
                }
                dirFile = new File(mFileListUser.get(i)+ConstValues.PATH_FENSHEN_IMAGE2);
                if(dirFile.exists()) {
                    DeleteFileUtil.deleteDirectory(mFileListUser.get(i) + ConstValues.PATH_FENSHEN_IMAGE2);
                }else {
                    errorStr.append(mFileNameList.get(i)+"/..."+ConstValues.PATH_FENSHEN_IMAGE2+"未找到\n");
                }
                dirFile = new File(mFileListUser.get(i)+ConstValues.PATH_FENSHEN_VIDEO);
                if(dirFile.exists()){
                    DeleteFileUtil.deleteDirectory(mFileListUser.get(i)+ConstValues.PATH_FENSHEN_VIDEO);
                }else {
                    errorStr.append(mFileNameList.get(i)+"/..."+ConstValues.PATH_FENSHEN_VIDEO+"未找到\n");
                }
                dirFile = new File(mFileListUser.get(i)+ConstValues.PATH_FENSHEN_VIDEO2);
                if(dirFile.exists()){
                    DeleteFileUtil.deleteDirectory(mFileListUser.get(i)+ConstValues.PATH_FENSHEN_VIDEO2);
                }else {
                    errorStr.append(mFileNameList.get(i)+"/..."+ConstValues.PATH_FENSHEN_VIDEO2+"未找到\n");
                }
            }
            return errorStr;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            mHandler=new Handler(Looper.getMainLooper());
            mHandler.post(new Runnable(){
                public void run(){
                    if(mFileList.size()==0||mFileListUser.size()==0){
                        Toast.makeText(getApplicationContext(), "未找到用户文件", Toast.LENGTH_LONG).show();
                    }else {
                        if (EmptyUtils.isEmpty(errorStr.toString())) {
                            Toast.makeText(getApplicationContext(), "清理完成", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), errorStr.toString(), Toast.LENGTH_LONG).show();
                            System.out.print(errorStr.toString());
                        }
                    }
                }
            });
            Intent intent1=new Intent(CleanService.this,AlarmReceiver.class);
            intent1.setAction("android.intent.action.CLEAN_SUCCESS");
            sendBroadcast(intent1);
        }


    }
}
