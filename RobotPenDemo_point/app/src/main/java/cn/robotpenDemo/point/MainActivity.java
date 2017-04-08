package cn.robotpenDemo.point;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.codingmaster.slib.S;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.robotpen.model.DevicePoint;
import cn.robotpen.model.entity.SettingEntity;
import cn.robotpen.model.entity.note.NoteEntity;
import cn.robotpen.model.symbol.DeviceType;
import cn.robotpen.model.symbol.SceneType;
import cn.robotpen.pen.callback.RobotPenActivity;
import cn.robotpen.pen.model.RemoteState;
import cn.robotpen.pen.model.RobotDevice;
import cn.robotpenDemo.point.connect.BleConnectActivity;

/**
 * 建议统一继承{@line RobotPenActivity} 在父类中已经将服务的绑定和解绑进行了处理
 */
public class MainActivity extends RobotPenActivity {
    Handler mHandler;
    SettingEntity mSetting;
    @BindView(R.id.connect_deviceType)
    TextView connectDeviceType;
    @BindView(R.id.connect_senceType)
    TextView connectSenceType;
    @BindView(R.id.connect_deviceSize)
    TextView connectDeviceSize;
    @BindView(R.id.pen_isRoute)
    TextView penIsRoute;
    @BindView(R.id.pen_press)
    TextView penPress;
    @BindView(R.id.pen_original)
    TextView penOriginal;
    @BindView(R.id.connect_offest)
    TextView connectOffest;
    @BindView(R.id.gotoBle)
    Button gotoBle;
    @BindView(R.id.activity_usb)
    LinearLayout activityUsb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        checkSDPermission();
        ButterKnife.bind(this);
        S.init(true,1,"PP_WRITER");
        //屏幕常亮控制
        MainActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mHandler = new Handler();
        mSetting = new SettingEntity(this);//获取通过设置改的值例如横竖屏、压感等
        gotoBle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, BleConnectActivity.class));
            }
        });
        //getRobotPenService().startRobotPenService(this,false);
    }


    @Override
    public void onStateChanged(int i, String s) {
        switch (i) {
            case RemoteState.STATE_CONNECTED:
                Log.e("test","STATE_CONNECTED ");
                break;
            case RemoteState.STATE_DEVICE_INFO: //当出现设备切换时获取到新设备信息后执行的
                //whiteBoardView.initDrawArea();
                checkDeviceConn();
                break;
            case RemoteState.STATE_DISCONNECTED://设备断开
                Log.e("test","STATE_DISCONNECTED ");
                break;
        }
    }

    /**
     * 当服务服务连接成功后进行
     *
     * @param name
     * @param service
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
        checkDeviceConn();
    }

    public void checkDeviceConn() {
        if (getPenServiceBinder() != null) {
            try {
                RobotDevice device = getPenServiceBinder().getConnectedDevice();
                if (device != null) {
                    DeviceType type = DeviceType.toDeviceType(device.getDeviceVersion());
                    //判断当前设备与笔记设备是否一致
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPenServiceError(String s) {

    }

    @Override
    public void onPenPositionChanged(int deviceType, int x, int y, int presure, byte state) {
        // state  00 离开 0x10悬空 0x11按下
        super.onPenPositionChanged(deviceType, x, y, presure, state);
        Log.e("test","x: "+x+"  y:"+y);
        DevicePoint point = DevicePoint.obtain(deviceType, x, y, presure, state); //将传入的数据转化为点数据
        /**
         *DevicePoint 将会获取更多的信息
         **/
        connectDeviceType.setText(point.getDeviceType().name());
        boolean screenSetting = mSetting.isDirection();//获取横竖屏设置 默认为竖屏
        SceneType sceneType = SceneType.getSceneType(screenSetting, point.getDeviceType());//false为竖屏
        connectSenceType.setText(sceneType.name());
        connectDeviceSize.setText(point.getWidth() + "/" + point.getHeight());
        penIsRoute.setText(String.valueOf(point.isRoute()));
        penPress.setText(point.getPressure() + "/" + point.getPressureValue());
        penOriginal.setText(point.getOriginalX() + "/" + point.getOriginalY());
        connectOffest.setText(point.getOffsetX() + "/" + point.getOffsetY());
        /**
         *也可以根据x,y坐标点直接绘制
         **/
        //DeviceType dType = point.getDeviceType();//根据设备值转化为设备类型  也可以通过deviceType 直接转化
    }
}