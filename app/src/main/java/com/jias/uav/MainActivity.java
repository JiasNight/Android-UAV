package com.jias.uav;

import com.jias.uav.RockerView.Direction;
import com.jias.uav.RockerView.DirectionMode;
import com.jias.uav.RockerView.OnShakeListener;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends Activity {

    TextView text_position_left;
    TextView text_position_center;
    TextView text_position_right;
    TextView text_position_left_first;
    TextView text_position_right_first;
    TextView state;
    TextView fly_height;
    TextView youmen;
    TextView henggun;

    private Button is_exit; //退出
    private Button is_video; //播放视频
    private LinearLayout bac; //背景

    private LinearLayout visible_seekbar; //控件隐藏
    private Button is_visible;

    private Button settings; //设置

    private Button bluetooth_connect;

    private Button fly_start_stop;


    private SeekBar seekbar1;
    private SeekBar seekbar2;

    private Button circuit; //转换
    private RockerView left_round;
    private RockerView right_round;

    private Button fly_camera;//照相机
    public static final int TAKE_PHOTO = 1;
    private Uri imageUri;

    private SensorManager sm;
    //需要两个Sensor
    private Sensor aSensor;
    private Sensor mSensor;
    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];

    //创建播放视频的控件对象
    private CustomVideoView videoview;
    private boolean video = false;


    private long mExitTime = 0;     //退出时间


    public SharedPreferences sp;          //定义SharedPreferences


    //百度地图
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    public LocationClient mLocationClient;
    public BDLocationListener myListener_map = new MyLocationListener();
    private Button address;
    private LatLng latLng;
    private boolean isFirstLoc = true; // 是否首次定位



    public String bluetooth_address = "00:0E:0E:15:84:F3";
    public UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public BluetoothSocket socket;
    public OutputStream out;
    public byte[] data = new byte[34];
    public int x34 = 100;
    public int x56 = 1500;
    public int x78 = 1640;
    public int x910 = 1640;

    private void initdata() {
        data[0] = (byte) 0xAA;
        data[1] = (byte) 0xC0;
        data[2] = (byte) 0x1C;
        data[3] = (byte) (x34>>8);//油门高八位
        data[4] = (byte) (x34&0xff);//油门低八位

        data[5] = (byte) (x56>>8);//航向
        data[6] = (byte) (x56&0xff);//航向

        data[7] = (byte) (x78>>8);//横滚
        data[8] = (byte) (x78&0xff);//横滚

        data[9] = (byte) (x910>>8);//俯仰
        data[10] = (byte) (x910&0xff);//俯仰

        data[31] = (byte) 0x1C;
        data[32] = (byte) 0x0D;
        data[33] = (byte) 0x0A;
    }



    final SensorEventListener myListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                magneticFieldValues = sensorEvent.values;
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                accelerometerValues = sensorEvent.values;
            calculateOrientation();
        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };




    //指南针坐标获取设置
    private void calculateOrientation() {
        text_position_left = (TextView)findViewById(R.id.text_position_left);
        text_position_center = (TextView)findViewById(R.id.text_position_center);
        text_position_right = (TextView)findViewById(R.id.text_position_right);
        text_position_right_first = (TextView)findViewById(R.id.text_position_right_first);
        text_position_left_first = (TextView)findViewById(R.id.text_position_left_first);

        fly_height = findViewById(R.id.fly_height);//高度

        float[] values = new float[3];
        float[] A = new float[9];
        int val = 0;
        int val1 = 0;

        SensorManager.getRotationMatrix(A, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(A, values);
        // 要经过一次数据格式的转换，转换为度
        values[0] = (float) Math.toDegrees(values[0]);
        if(values[0] < 0){
            values[0] = values[0] + 360;
        }
        if(values[0] == 360){
            values[0] = 0;
        }
        val = (int)values[0];
        values[1] = (float) Math.toDegrees(values[1]);
        //values[2] = (float) Math.toDegrees(values[2]);
        val1 = (int)(values[1]);
        if(val1 < 0 ){
            val1 = val1 + 180;
        }
        fly_height.setText(val1 + "");
        if(val >= 0 && val < 5){
            text_position_left_first.setText((val + 10 ) + "°");
            text_position_left.setText((val + 5 ) + "°");
            text_position_center.setText("北");
            text_position_right.setText((360 + val - 5) + "°");
            text_position_right_first.setText((360 + val -10) + "°");
        }
        else if(val > 5 && val < 10){
            text_position_left_first.setText((val + 10 ) + "°");
            text_position_left.setText((val + 5 ) + "°");
            text_position_center.setText("北");
            text_position_right.setText((val - 5) + "°");
            text_position_right_first.setText((360 + val -10) + "°");
        }
        else if(val == 5) {
            text_position_left_first.setText((val + 10) + "°");
            text_position_left.setText((val + 5) + "°");
            text_position_center.setText("北");
            text_position_right.setText(0 + "°");
            text_position_right_first.setText((360 - val) + "°");
        }
        else if(val == 355) {
            text_position_left_first.setText(5 + "°");
            text_position_left.setText(0 + "°");
            text_position_center.setText("北");
            text_position_right.setText(350 + "°");
            text_position_right_first.setText(345 + "°");
        }
        else if(val == 350) {
            text_position_left_first.setText(0 + "°");
            text_position_left.setText(355 + "°");
            text_position_center.setText("北");
            text_position_right.setText(345 + "°");
            text_position_right_first.setText(340 + "°");
        }
        else if(val >= 40 && val <= 50){
            text_position_center.setText("东北");
        }
        else if(val >= 85 && val <= 95){
            text_position_center.setText("东");
        }
        else if(val >= 130 && val <= 140){
            text_position_center.setText("东南");
        }
        else if(val >= 175 && val <= 185){
            text_position_center.setText("南");
        }
        else if(val >= 225 && val <= 230){
            text_position_center.setText("西南");
        }
        else if(val >= 265 && val <= 275){
            text_position_center.setText("西");
        }
        else if(val >= 310 && val <= 320){
            text_position_center.setText("西北");
        }
        else if(val > 355 && val < 360){
            text_position_left_first.setText((val + 10 -360) + "°");
            text_position_left.setText((val + 5 - 360) + "°");
            text_position_center.setText("北");
            text_position_right.setText((val - 5) + "°");
            text_position_right_first.setText((val - 10) + "°");
        }
        else {
            text_position_right_first.setText((val - 10) + "°");
            text_position_right.setText((val - 5) + "°");
            text_position_center.setText(val + "°");
            text_position_left.setText((val + 5) + "°");
            text_position_left_first.setText((val + 10) + "°");
        }
    }






    //加载视频方法
    private void vide_initView() {
        //加载视频资源控件
        videoview = (CustomVideoView) findViewById(R.id.videoview);
        //设置播放加载路径
        videoview.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video));
        //播放
        videoview.start();
        //循环播放
        videoview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoview.start();
            }
        });
    }




    //百度地图设置
    private void initMap() {
        //获取地图控件引用
        mBaiduMap = mMapView.getMap();
        //普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMyLocationEnabled(true);
        //默认显示普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //开启交通图
        mBaiduMap.setTrafficEnabled(true);
        // 开启热力图
        //mBaiduMap.setBaiduHeatMapEnabled(true);
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mLocationClient = new LocationClient(getApplicationContext());//声明LocationClient类
        // 配置定位SDK参数
        initLocation();
        mLocationClient.registerLocationListener(myListener_map);//注册监听函数
        // 开启定位
        mLocationClient.start();
        //图片点击事件，回到定位点
        mLocationClient.requestLocation();
    }
    //配置定位SDK参数
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);
        option.setOpenGps(true);// 打开gps


        // 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }
    //实现BDLocationListener接口,BDLocationListener为结果监听接口，异步获取定位结果
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            // 设置定位数据
            mBaiduMap.setMyLocationData(locData);
            // 当不需要定位图层时关闭定位图层
            // mBaiduMap.setMyLocationEnabled(false);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

                if (location.getLocType() == BDLocation.TypeGpsLocation) {
                    // GPS定位结果
                    Toast.makeText(MainActivity.this, location.getAddrStr(), Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                    // 网络定位结果
                    Toast.makeText(MainActivity.this, location.getAddrStr(), Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
                    // 离线定位结果
                    Toast.makeText(MainActivity.this, location.getAddrStr(), Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeServerError) {
                    Toast.makeText(MainActivity.this, "服务器错误，请检查", Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    Toast.makeText(MainActivity.this, "网络错误，请检查", Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    Toast.makeText(MainActivity.this, "手机模式错误，请检查是否飞行", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }




    //摇杆位置方法
    public void initrokerview(){
        //找到RockerView控件
        RockerView roker=(RockerView) findViewById(R.id.left_round);
        //实时监测摇动方向
        roker.setOnShakeListener(DirectionMode.DIRECTION_8, new OnShakeListener() {
            //开始摇动时要执行的代码写在本方法里
            @Override
            public void onStart() {

            }
            //结束摇动时要执行的代码写在本方法里
            @Override
            public void onFinish() {
                Toast.makeText(MainActivity.this, "已复位", Toast.LENGTH_SHORT).show();
            }
            //摇动方向时要执行的代码写在本方法里
            @Override
            public void direction(Direction direction) {
                if (direction == RockerView.Direction.DIRECTION_CENTER){
                    state.setText("中心");
                }else if (direction == RockerView.Direction.DIRECTION_DOWN){
                    state.setText("下");
                }else if (direction == RockerView.Direction.DIRECTION_LEFT){
                    state.setText("左");
                }else if (direction == RockerView.Direction.DIRECTION_UP){
                    state.setText("上");
                }else if (direction == RockerView.Direction.DIRECTION_RIGHT){
                    state.setText("右");
                }else if (direction == RockerView.Direction.DIRECTION_DOWN_LEFT){
                    state.setText("左下");
                }else if (direction == RockerView.Direction.DIRECTION_DOWN_RIGHT){
                    state.setText("右下");
                }else if (direction == RockerView.Direction.DIRECTION_UP_LEFT){
                    state.setText("左上");
                }else if (direction == RockerView.Direction.DIRECTION_UP_RIGHT){
                    state.setText("右上");
                }
            }
        });
    }




    //返回重启加载
    @Override
    protected void onRestart() {
        //vide_initView();
        super.onRestart();
    }

    //防止锁屏或者切出的时候，音乐在播放
    @Override
    protected void onStop() {
        //videoview.stopPlayback();
        super.onStop();
    }

    //暂停视频播放
    @Override
    protected void onPause(){
        //sm.unregisterListener(myListener);
        //videoview.pause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStart(){
        //videoview.start();
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        state = findViewById(R.id.state);//状态

        bluetooth_connect = findViewById(R.id.BlueTooth_connect);
        fly_start_stop = findViewById(R.id.fly_start_stop);

        settings = findViewById(R.id.setting);//设置

        circuit = findViewById(R.id.circuit);//模式转换
        left_round = findViewById(R.id.left_round);//左边摇杆
        right_round = findViewById(R.id.right_round);//右边摇杆

        is_exit = findViewById(R.id.is_exit);//退出
        is_video = findViewById(R.id.is_video);//视频背景
        bac = findViewById(R.id.bac);//背景

        visible_seekbar = findViewById(R.id.visible_seekbar);//seekbar
        is_visible = findViewById(R.id.is_visible);//是否隐藏seekbar

        address = findViewById(R.id.fly_address);//显示百度地图
        mMapView = (MapView) findViewById(R.id.bmapView);

        sp=getSharedPreferences("fly", MODE_PRIVATE);//参数1为给保存数据的文件命名

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        aSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sm.registerListener(myListener, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(myListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);



        seekbar1 = (SeekBar)findViewById(R.id.verticalSeekBar1);
        youmen = findViewById(R.id.youmen);
        seekbar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                x34 = progress;
                data[3] = (byte) (progress>>8);
                data[4] = (byte) (progress&0xff);
                youmen.setText("油门值：" + x34);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekbar2 = (SeekBar)findViewById(R.id.verticalSeekBar2);
        henggun = findViewById(R.id.henggun);
        seekbar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                x56 = progress;
                data[5] = (byte) (progress>>8);
                data[6] = (byte) (progress&0xff);
                henggun.setText("横滚值：" + x56);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        //自定义摇杆方法
        initrokerview();


        //更新显示指南针数据的方法
        calculateOrientation();

        //加载视频数据
        //initView();

        //百度地图
        initMap();

        //无人机数据
        initdata();


    }


    //连接蓝牙线程
    private class BlueThread implements Runnable {
        boolean blue = false;
        @Override
        public void run() {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = adapter.getRemoteDevice(bluetooth_address);
            if(device == null) {
                Toast ts = Toast.makeText(MainActivity.this, "蓝牙连接失败！", Toast.LENGTH_SHORT);
                ts.show();
            }
            try {
                socket = device.createRfcommSocketToServiceRecord(uuid);
                socket.connect();
                out = socket.getOutputStream();
                blue = BluetoothAdapter.checkBluetoothAddress("00:0E:0E:15:84:F3");
            }catch(IOException e) {
            }
        }

    }

    //退出按钮事件
    public void exit(View v){
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            System.exit(0);
        }
    }


    //是否播放背景视频按钮事件
    public void play_video(View v){
        Toast.makeText(MainActivity.this, "背景切换", Toast.LENGTH_SHORT).show();
        if(video == true) {
            is_video.setBackgroundResource(R.mipmap.no_video); //图标
            bac.setBackgroundResource(R.mipmap.bac);//背景
            videoview.setVisibility(View.GONE);
            videoview.pause();
            //onPause();
            video = false;
        }else{
            mMapView.setVisibility(View.GONE);//地图隐藏
            vide_initView();
            bac.setBackgroundColor(Color.parseColor("#00ffffff")); //背景设置透明
            is_video.setBackgroundResource(R.mipmap.video);//图标
            videoview.setVisibility(View.VISIBLE);
            video = true;
        }
    }


    //设置按钮
    public void sys_set(View v){
        final Intent intent = new Intent();
        intent.setClass(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
        onPause();
        onStop();
    }


    //模式转换
    public void mod_circuit(View v) {
        if(left_round.getVisibility() == View.GONE && right_round.getVisibility() == View.GONE){
            Toast.makeText(MainActivity.this, "摇杆模式", Toast.LENGTH_SHORT).show();
            left_round.setVisibility(View.VISIBLE);//设置显示
            right_round.setVisibility(View.VISIBLE);
        }else{
            Toast.makeText(MainActivity.this, "按键模式", Toast.LENGTH_SHORT).show();
            left_round.setVisibility(View.GONE);//设置隐藏
            right_round.setVisibility(View.GONE);
        }
    }


    //是否显示seekbar
    public void seekbar_is_visibile(View v) {
        if(visible_seekbar.getVisibility() == View.GONE){
            visible_seekbar.setVisibility(View.VISIBLE);//设置显示
            is_visible.setBackgroundResource(R.mipmap.visibile);
            SharedPreferences.Editor spe = sp.edit();
            spe.putInt("x1", seekbar1.getProgress());             //设置要保存的数据，参数1为给数据命名，参数2为要保存的数据。
            spe.putInt("x2", seekbar2.getProgress());
            //可以保存多个数据
            spe.commit();
        }else{
            visible_seekbar.setVisibility(View.GONE);//设置隐藏
            is_visible.setBackgroundResource(R.mipmap.un_visible);
            int x1 = sp.getInt("x1", x34);       //读取数据，参数1为要读取数据的命名，参数2为如果没有数据时的值。
            int x2 = sp.getInt("x2", x78);
            //可以读取多个数据
            youmen.setText("油门值：" + x1);
            henggun.setText("横滚值：" + x2);
            Toast.makeText(MainActivity.this, "数据已保存！", Toast.LENGTH_SHORT).show();
        }
    }


    //是否显示百度地图
    public void address(View v) {
        if (mMapView.getVisibility() == View.GONE & video == false) {
            //普通地图
            //mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
            //卫星地图
            //mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
            //把定位点再次显现出来
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latLng);
            mBaiduMap.animateMapStatus(mapStatusUpdate);
            mMapView.setVisibility(View.VISIBLE);
        } else {
            mMapView.setVisibility(View.GONE);
            mMapView.onResume();
        }
    }

    //打开照相机
    public void camera(View v){
//        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
//        startActivityForResult(intent, TAKE_PHOTO);
        Toast.makeText(MainActivity.this, "无人机没有摄像头！", Toast.LENGTH_LONG).show();
    }

    //蓝牙连接
    public void bluetooth_connect(View v){
        Thread tr = new Thread(new BlueThread());
        tr.start();
        bluetooth_connect.setBackgroundResource(R.drawable.p_bluetooth_con);
        state.setText("已连接蓝牙");
        Toast.makeText(MainActivity.this, "蓝牙连接成功！", Toast.LENGTH_SHORT).show();
    }

    public class sendThread implements Runnable{
        @Override
        public void run() {
            try {
                while(flag) {
                    out.write(data);
                    Thread.sleep(5);
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }


    //开关
    public boolean flag = false;
    public void fly_start_stop(View v){
        if(flag == false) {
            flag = true;
            fly_start_stop.setBackgroundResource(R.drawable.start);
            Thread th = new Thread(new sendThread());
            th.start();
        }else {
            flag = false;
            fly_start_stop.setBackgroundResource(R.drawable.stop);
        }
    }

    //起飞
    public void fly_takeoff(View v){
        Toast.makeText(MainActivity.this, "此功能还有待开发！", Toast.LENGTH_SHORT).show();
    }

    //语音
    public void speech(View v){
        Toast.makeText(MainActivity.this, "此功能还有待开发！", Toast.LENGTH_SHORT).show();
    }
}


