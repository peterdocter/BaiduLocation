package com.and2long.baidulocation.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.and2long.baidulocation.R;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.Poi;

import java.util.List;

import static com.and2long.baidulocation.global.MyApplication.mLocationClient;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int MY_PERMISSIONS_REQUEST = 100;  //位置权限请求码
    private TextView tvLocationDesc;
    private Button btnGetLocation;
    private ProgressDialog progressDialog;
    private Button btnShowMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.activity_main);

        btnGetLocation = (Button) findViewById(R.id.btn_get_location);
        btnShowMap = (Button) findViewById(R.id.btn_show_map);
        tvLocationDesc = (TextView) findViewById(R.id.tv_location_desc);

        btnGetLocation.setOnClickListener(this);
        btnShowMap.setOnClickListener(this);

        registerLocationListener();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.positioning));
    }


    /**
     * 注册定位监听。
     */
    private void registerLocationListener() {
        mLocationClient.registerLocationListener(new BDLocationListener() {
            private int count = 0;

            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                //Receive Location
                StringBuffer sb = new StringBuffer(256);
                sb.append("time : ");
                sb.append(bdLocation.getTime());
                sb.append("\nerror code : ");
                sb.append(bdLocation.getLocType());
                sb.append("\nlatitude : ");
                sb.append(bdLocation.getLatitude());
                sb.append("\nlontitude : ");
                sb.append(bdLocation.getLongitude());
                sb.append("\nradius : ");
                sb.append(bdLocation.getRadius());
                sb.append("\ncity : ");
                sb.append(bdLocation.getCity());
                if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                    sb.append("\nspeed : ");
                    sb.append(bdLocation.getSpeed());// 单位：公里每小时
                    sb.append("\nsatellite : ");
                    sb.append(bdLocation.getSatelliteNumber());
                    sb.append("\nheight : ");
                    sb.append(bdLocation.getAltitude());// 单位：米
                    sb.append("\ndirection : ");
                    sb.append(bdLocation.getDirection());// 单位度
                    sb.append("\naddr : ");
                    sb.append(bdLocation.getAddrStr());
                    sb.append("\ndescribe : ");
                    sb.append("gps定位成功");

                } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                    sb.append("\naddr : ");
                    sb.append(bdLocation.getAddrStr());
                    //运营商信息
                    sb.append("\noperationers : ");
                    sb.append(bdLocation.getOperators());
                    sb.append("\ndescribe : ");
                    sb.append("网络定位成功");
                } else if (bdLocation.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                    sb.append("\ndescribe : ");
                    sb.append("离线定位成功，离线定位结果也是有效的");
                } else if (bdLocation.getLocType() == BDLocation.TypeServerError) {
                    sb.append("\ndescribe : ");
                    sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
                } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkException) {
                    sb.append("\ndescribe : ");
                    sb.append("网络不同导致定位失败，请检查网络是否通畅");
                } else if (bdLocation.getLocType() == BDLocation.TypeCriteriaException) {
                    sb.append("\ndescribe : ");
                    sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
                }
                sb.append("\nlocationdescribe : ");
                sb.append(bdLocation.getLocationDescribe());// 位置语义化信息
                List<Poi> list = bdLocation.getPoiList();// POI数据
                if (list != null) {
                    sb.append("\npoilist size = : ");
                    sb.append(list.size());
                    for (Poi p : list) {
                        sb.append("\npoi= : ");
                        sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                    }
                }
                Log.i("BaiduLocationApiDem", sb.toString());
                count++;
                Log.i("定位次数", "onReceiveLocation: " + count);
                mLocationClient.stop();
                progressDialog.dismiss();
                tvLocationDesc.setText(sb.toString());
            }
        });
    }

    /**
     * 检查是否具有权限
     */
    public void checkPermissionsAndDoNext() {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)) {
            //如果app之前请求过该权限,被用户拒绝, 这个方法就会返回true.
            //如果用户之前拒绝权限的时候勾选了对话框中"Don’t ask again"的选项,那么这个方法会返回false.
            //如果设备策略禁止应用拥有这条权限, 这个方法也返回false.
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                //提示用户需要权限
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.help)
                        .setCancelable(false)
                        .setMessage(R.string.message_need_permission)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(R.string.setting, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //进入设置中的应用信息详情页，让用户手动授权
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .create()
                        .show();
            } else {
                //没有权限，请求权限。
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST);
            }
        } else {
            //具有权限，执行操作
            progressDialog.show();
            mLocationClient.start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //已授权
                    progressDialog.show();
                    mLocationClient.start();
                } else {
                    //拒绝

                }
                break;


        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_get_location:
                //请求位置信息权限
                checkPermissionsAndDoNext();
                break;

        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
