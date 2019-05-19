package hjh.nit.com.timeddeleten.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectChangeListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.blankj.utilcode.util.EmptyUtils;
import com.blankj.utilcode.util.SPUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hjh.nit.com.timeddeleten.R;
import hjh.nit.com.timeddeleten.service.AutoCleanService;
import hjh.nit.com.timeddeleten.service.AutoOpenStopService;
import hjh.nit.com.timeddeleten.util.ConstValues;
import hjh.nit.com.timeddeleten.util.OpenSystenPermissionSettingUtil;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn_set_time)
    Button mSetTime;
    @BindView(R.id.btn_start_clean)
    Button mStartClean;
    @BindView(R.id.btn_stop_clean)
    Button mStopClean;
    @BindView(R.id.btn_start_suspension)
    Button getmStartSuspension;
    @BindView(R.id.tv_show_time)
    TextView mShowTime;
    @BindView(R.id.et_input_pag)
    EditText mInputPag;
    @BindView(R.id.btn_set_interval_time)
    Button mSetIntervalTime;
    @BindView(R.id.tv_interval_time)
    TextView mIntervalTime;

    SimpleDateFormat df=new SimpleDateFormat("HH:mm");
    SimpleDateFormat dfInterval=new SimpleDateFormat("HH时mm分ss秒");

    TimePickerView pvTime;
    TimePickerView pvIntervalTime;
    public RxPermissions mRxPermissions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
//        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.KILL_BACKGROUND_PROCESSES});
        initIntervalTimePicker();
        initTimePicker();
        if (!EmptyUtils.isEmpty(SPUtils.getInstance().getString(ConstValues.CLEAN_TIME))){
            mShowTime.setText("当前设置时间："+SPUtils.getInstance().getString(ConstValues.CLEAN_TIME));
        }
        if (!EmptyUtils.isEmpty(SPUtils.getInstance().getString(ConstValues.OPEN_CLOSE_TIME))){
            mIntervalTime.setText("当前设置时间："+SPUtils.getInstance().getString(ConstValues.OPEN_CLOSE_TIME));
        }
        if (!EmptyUtils.isEmpty(SPUtils.getInstance().getString(ConstValues.APP_NAME))){
            mInputPag.setText(SPUtils.getInstance().getString(ConstValues.APP_NAME));
        }
    }

    @OnClick({R.id.btn_start_suspension,R.id.btn_start_clean,R.id.btn_stop_clean,R.id.btn_set_time,R.id.btn_clean,R.id.btn_stop_suspension,R.id.btn_set_interval_time})
    void onClicked(View view){
        switch(view.getId()){
            case R.id.btn_set_time:
                pvTime.show(view);
                break;
            case R.id.btn_clean:
//                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.KILL_BACKGROUND_PROCESSES});
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE});
                Intent intents = new Intent(MainActivity.this, hjh.nit.com.timeddeleten.service.CleanService.class);
                Toast.makeText(MainActivity.this,"清除服务启动中",Toast.LENGTH_SHORT).show();
                startService(intents);
                break;
            case R.id.btn_start_clean:
//                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.KILL_BACKGROUND_PROCESSES});
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE});
                if(!EmptyUtils.isEmpty(SPUtils.getInstance().getString(ConstValues.CLEAN_TIME))){
                    Intent intent = new Intent(MainActivity.this, hjh.nit.com.timeddeleten.service.AutoCleanService.class);
                    Toast.makeText(MainActivity.this,"定时清理服务启动中",Toast.LENGTH_SHORT).show();
                    startService(intent);
                }else {
                    Toast.makeText(MainActivity.this,"清理时间未设置",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_stop_clean:
                Intent intent1 = new Intent(MainActivity.this, hjh.nit.com.timeddeleten.service.AutoCleanService.class);
                stopService(intent1);
                Toast.makeText(MainActivity.this,"定时清理服务已关闭",Toast.LENGTH_LONG).show();
                break;
            case R.id.btn_set_interval_time:
                pvIntervalTime.show(view);
                break;
            case R.id.btn_start_suspension:
                if(!EmptyUtils.isEmpty(mInputPag.getText().toString())&&!EmptyUtils.isEmpty(SPUtils.getInstance().getString(ConstValues.OPEN_CLOSE_TIME))){
                    if(SPUtils.getInstance().getString(ConstValues.APP_NAME)!=mInputPag.getText().toString().trim()) {
                        SPUtils.getInstance().put(ConstValues.APP_NAME, mInputPag.getText().toString().trim());
                    }
                    Intent intent2 = new Intent(MainActivity.this, hjh.nit.com.timeddeleten.service.AutoOpenStopService.class);
                    Toast.makeText(MainActivity.this,"定时启动服务启动中",Toast.LENGTH_SHORT).show();
                    startService(intent2);
                }else {
                    Toast.makeText(MainActivity.this,"应用名称未填写或时间未选择",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_stop_suspension:
                Intent intent3 = new Intent(MainActivity.this, hjh.nit.com.timeddeleten.service.AutoOpenStopService.class);
                stopService(intent3);
                Toast.makeText(MainActivity.this,"定时启动服务已关闭",Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }

    private void initTimePicker() {//Dialog 模式下，在底部弹出

        pvTime = new TimePickerBuilder(this, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                mShowTime.setText("当前设置时间："+df.format(date));
                SPUtils.getInstance().put(ConstValues.CLEAN_TIME,df.format(date));
                SPUtils.getInstance().put(ConstValues.CLEAN_TIME_HOUR,date.getHours());
                SPUtils.getInstance().put(ConstValues.CLEAN_TIME_MINUTE,date.getMinutes());
            }
        })
                .setTimeSelectChangeListener(new OnTimeSelectChangeListener() {
                    @Override
                    public void onTimeSelectChanged(Date date) {
                    }
                })
                .setType(new boolean[]{false, false, false, true, true, false})
                .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
                .addOnCancelClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                })
                .build();

        Dialog mDialog = pvTime.getDialog();
        if (mDialog != null) {

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM);

            params.leftMargin = 0;
            params.rightMargin = 0;
            pvTime.getDialogContainerLayout().setLayoutParams(params);

            Window dialogWindow = mDialog.getWindow();
            if (dialogWindow != null) {
                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim);//修改动画样式
                dialogWindow.setGravity(Gravity.BOTTOM);//改成Bottom,底部显示
                dialogWindow.setDimAmount(0.1f);
            }
        }
    }

    private void initIntervalTimePicker() {//Dialog 模式下，在底部弹出

        pvIntervalTime = new TimePickerBuilder(this, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                mIntervalTime.setText("当前设置间隔时间："+dfInterval.format(date));
                SPUtils.getInstance().put(ConstValues.OPEN_CLOSE_TIME,dfInterval.format(date));
                SPUtils.getInstance().put(ConstValues.OPEN_CLOSE_TIME_HOUR,date.getHours());
                SPUtils.getInstance().put(ConstValues.OPEN_CLOSE_TIME_MINUTE,date.getMinutes());
                SPUtils.getInstance().put(ConstValues.OPEN_CLOSE_TIME_SECOND,date.getSeconds());
            }
        })
                .setTimeSelectChangeListener(new OnTimeSelectChangeListener() {
                    @Override
                    public void onTimeSelectChanged(Date date) {
                    }
                })
                .setType(new boolean[]{false, false, false, true, true, true})
                .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
                .addOnCancelClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                })
                .build();
    }

    private void requestPermissions(String... permission) {
        mRxPermissions = new RxPermissions(this);
        mRxPermissions.request(permission)
                .subscribe(new io.reactivex.functions.Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {

                        } else {
                            finish();
                            Toast.makeText(MainActivity.this, "请授予存储权限,以便功能正常使用!", Toast.LENGTH_SHORT).show();
                            OpenSystenPermissionSettingUtil.settingPermissionActivity(MainActivity.this);
                        }
                    }
                });
    }

}
