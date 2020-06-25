package com.example.peiqi.activity;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.ScaleAnimation;

import com.example.peiqi.R;
import com.example.peiqi.bean.MessageBean;
import com.example.peiqi.bean.MessageListBean;
import com.example.peiqi.common.Constants;
import com.example.peiqi.common.GlobalValue;
import com.example.peiqi.http.HttpCallback;
import com.example.peiqi.http.HttpManager;
import com.example.peiqi.req.HttpRequest;
import com.example.peiqi.utils.ByteUtils;
import com.example.peiqi.utils.ToastUtils;
import com.example.peiqi.ws.WsEventListener;
import com.example.peiqi.ws.WsManager;

import java.util.UUID;

import androidx.annotation.Nullable;

/**
 * 启动页
 *
 * @author bohan.chen
 */
public class SplashActivity extends AppCompatActivity implements WsEventListener {
    private WsManager wsManager;
    private String uuid;
    private Handler mMainHandler;


    public AMap aMap;
    public MapView mMapView;
    private UiSettings mUiSettings;
    private CheckBox mStyleCheckbox;
    private SharedPreferences sp;//编辑器

    public Marker PeiQiMarker;//佩奇标签
    public LatLng latLng;//地理位置
    public MarkerOptions markerOption;
    double latitude;
    double longitude;
    int bloodOxygen=0,heartRate=0;
    String p;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getHistoryRecord();
        connectDevice();
        mMapView = findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);//创建地图
        MapInit();
        mStyleCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //isChecked = mStyleCheckbox.isChecked();
                if(isChecked) {
                    sp = getSharedPreferences("isChecked", 0);
                    // 使用编辑器来进行操作
                    SharedPreferences.Editor edit = sp.edit();
                    // 将勾选的状态保存起来
                    edit.putBoolean("choose", true); // 这里的choose就是一个key 通过这个key我们就可以得到对应的值
                    // 最好我们别忘记提交一下
                    edit.commit();
                    aMap.setMapType(AMap.MAP_TYPE_NIGHT);//夜景地图模式
                }else{
                    sp = getSharedPreferences("isChecked", 0);
                    // 使用编辑器来进行操作
                    SharedPreferences.Editor edit = sp.edit();
                    // 将勾选的状态保存起来
                    edit.putBoolean("choose", false); // 这里的choose就是一个key 通过这个key我们就可以得到对应的值
                    // 最好我们别忘记提交一下
                    edit.commit();
                    aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 矢量地图模式
                }
            }
        });



        System.out.println("onCreate");
    }
    /**
     * 初始化AMap对象
     */
    private void MapInit() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            modeEcho();
            mUiSettings =  aMap.getUiSettings();
            mUiSettings.setCompassEnabled(true);//显示指南针
            mUiSettings.setScaleControlsEnabled(true);//显示比例尺
            mUiSettings.setLogoBottomMargin(-69);//隐藏logo

            MyLocationStyle myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。

            aMap.moveCamera(CameraUpdateFactory.zoomTo(aMap.getCameraPosition().zoom));
            aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
            aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
            aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        }
    }
    /**
     * 软件设置回显
     */
    private void modeEcho() {
        mStyleCheckbox = findViewById(R.id.nightmap);
        // 要想回显CheckBox的状态 我们需要取得数据
        // [1] 还需要获得SharedPreferences
        sp = getSharedPreferences("isChecked", 0);
        boolean result = sp.getBoolean("choose", false); // 这里就是开始取值了 false代表的就是如果没有得到对应数据我们默认显示为false
        if(result){
            aMap.setMapType(AMap.MAP_TYPE_NIGHT);//夜景地图模式
        }else{
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 矢量地图模式
        }
        // 把得到的状态设置给CheckBox组件
        mStyleCheckbox.setChecked(result);
    }

    /**点击事件
     * 佩奇位置和信息更新
     * @param view
     */
    public void PeiQiLocatedUpdate(View view) {
        if(PeiQiMarker!=null)
            PeiQiMarker.remove();
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    sendMessage("hhh");
                    wsManager.subDevice();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.start();

    }
    /**
     * 获取聊天记录
     */
    private void getHistoryRecord() {
        HttpManager.getInstance().addToken2Header(Constants.HTTP_TOKEN);
        mMainHandler = new Handler(Looper.getMainLooper());
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    HttpRequest.getChatHisData(1, GlobalValue.orgId, getString(R.string.yuanziyun), new HttpCallback<MessageListBean>() {
                        @Override
                        public void onSuccess(MessageListBean data) {
                            if (data != null) {
                                for (MessageBean item : data.getItems()) {
                                    item.setName("old");
                                    item.setHex_packet(item.getHex_packet().replace(getString(R.string.yuanziyun), ""));
                                    item.setNormalData(item.getHex_packet());
                                    item.setHexData(ByteUtils.str2HexStr(item.getHex_packet()));
                                }
                            }
                        }

                        @Override
                        public void onError(int code, String errMsg) {
                            //ToastUtils.showShort(errMsg);
                        }

                        @Override
                        public void onFinish() {
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }
    /**
     * 连接设备
     */
    private void connectDevice() {
        wsManager = new WsManager();
        uuid = UUID.randomUUID().toString();
        String url = Constants.WS_SERVER_ADDRESS + Constants.HTTP_TOKEN + "/org/" + GlobalValue.orgId + "?token=" + uuid;
        wsManager.init(url,getString(R.string.yuanziyun), this);
    }
    /**
     * 发送数据
     */
    private void sendMessage(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            byte[] dataByte;

            MessageBean messageBean = new MessageBean();
            messageBean.setName("TX");
            dataByte = msg.getBytes();
            messageBean.setNormalData(msg);
            messageBean.setHexData(ByteUtils.str2HexStr(msg));
            messageBean.setDataByte(dataByte);
            if (wsManager.sendMsg(dataByte)) {

            } else {
                ToastUtils.showShort("发送失败");
            }
        }
    }
    @Override
    public void onFailed(final String errMsg) {

    }
    @Override
    public void onRecMessage(final MessageBean data) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                String str=data.getNormalData();
                System.out.println(str);
                longitude=Double.valueOf(str.substring(str.indexOf("longitude:")+"longitude:".length(),str.indexOf(",latitude")));
                latitude=Double.valueOf(str.substring(str.indexOf("latitude:")+"latitude:".length(),str.indexOf(",heartRate")));
                heartRate= Integer.valueOf(str.substring(str.indexOf("heartRate:")+"heartRate:".length(),str.indexOf(",bloodOxygen")));
                bloodOxygen=Integer.valueOf(str.substring(str.indexOf("bloodOxygen:")+"bloodOxygen:".length(),str.length()));
                latLng = new LatLng(latitude+0.00055,longitude+0.00675);
                p="状态:上线"+"\n"+"心率:"+String.valueOf(heartRate)+"\n"+"血氧:"+String.valueOf(bloodOxygen);
                markerOption = new MarkerOptions()
                        .position(latLng)
                        .title("用户:佩奇")
                        .snippet(p)
                        .visible(true)
                        .icon(BitmapDescriptorFactory
                                .fromBitmap(BitmapFactory
                                        .decodeResource(getResources(),R.drawable.peiqi)))
                        .setFlat(true);//设置marker平贴地图效果

                Animation animation = new ScaleAnimation(0,1,0,1);
                animation.setInterpolator(new LinearInterpolator());
                animation.setDuration(1000);//整个移动所需要的时间

                CameraPosition cameraPosition = new CameraPosition(latLng, aMap.getCameraPosition().zoom, 0, 30);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                aMap.moveCamera(cameraUpdate);

                PeiQiMarker=aMap.addMarker(markerOption);
                PeiQiMarker.setAnimation(animation); /*设置动画*/
                PeiQiMarker.startAnimation();//*开始动画*/
                PeiQiMarker.showInfoWindow();//展示消息
            }
        });
    }

    @Override
    public void onConnect() {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
//                ToastUtils.showShort("设备已连接");
            }
        });
    }

    @Override
    public void onDisconnect() {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
 //               ToastUtils.showShort("设备已断开");
            }
        });
    }

    @Override
    protected void onStart() {// 应用程序界面用户可见时调用
        super.onStart();
        System.out.println("onStart");
    }
    @Override
    protected void onResume() {// 应用程序界面获得焦点时间时调用
        super.onResume();
        mMapView.onResume();
        System.out.println("onResume");
    }
    @Override
    protected void onRestart() {// 当界面再次可见时被调用
        super.onRestart();
        System.out.println("onRestart");
    }
    @Override
    protected void onPause() {    // 界面失去焦点时调用
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
        System.out.println("onPause");
    }
    @Override
    protected void onStop() { // 当界面不可见时调用
        super.onStop();
        System.out.println("onStop");
    }
    @Override
    protected void onDestroy() { // 当界面被销毁时调用
        System.out.println("onDestroy");
        mMapView.onDestroy();
        wsManager.close();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }
    /**
     * 在任意现成当中都可以调用弹出吐司的方法
     * @param result
     */
    private void showToastInAnyThread(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SplashActivity.this, result, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
