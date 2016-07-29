package com.example.razer.sample_1;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

/**
 * Created by Razer on 2016-05-23.
 */
public class MapTest extends Activity {
    static  final LatLng BUSAN = new LatLng(35.168781, 129.057663);
    private GoogleMap map;
    private Double myLat = 0.0;
    private Double myLng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapview_page);
//        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
//        Marker busan = map.addMarker(new MarkerOptions().position(BUSAN).title("Busan"));
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(BUSAN, 15));
//        map.animateCamera(CameraUpdateFactory.zoomTo(10),2000, null);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map = mapFragment.getMap();

        //현재 위치로 가는 버튼 표시
        try{
            map.setMyLocationEnabled(true);
        }
        catch (SecurityException e){;}

        map.moveCamera(CameraUpdateFactory.newLatLngZoom( BUSAN, 15));//초기 위치...수정필요

        MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                String msg = "lon: "+location.getLongitude()+" -- lat: "+location.getLatitude();
                myLat = location.getLatitude();
                myLng = location.getLongitude();
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                drawMarker(location);
            }
        };

        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(getApplicationContext(), locationResult);

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            public void onMapLongClick(LatLng point) {
                String text = "[장시간 클릭시 이벤트] latitude =" + point.latitude + ", longitude ="
                        + point.longitude;
//                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG)
//                        .show();

                map.clear();

                map.addMarker(new MarkerOptions()
                        .position(point)
                        .snippet("Lat:" + point.latitude + "   Lng:" + point.longitude)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .title("현재위치"));

                UserSetting.setUser_lat(point.latitude);
                UserSetting.setUser_long(point.longitude);

                //자신의 현재위치에서 지정한 위치까지의 거리
                Toast.makeText(getApplicationContext(), "" + MainActivity.calcDistance(point.latitude, point.longitude, myLat, myLng), Toast.LENGTH_SHORT).show();

            }
        });

    }





    private void drawMarker(Location location) {

        //기존 마커 지우기
        map.clear();
        LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

        //currentPosition 위치로 카메라 중심을 옮기고 화면 줌을 조정한다. 줌범위는 2~21, 숫자클수록 확대
        map.moveCamera(CameraUpdateFactory.newLatLngZoom( currentPosition, 17));
        map.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);

        //마커 추가
        map.addMarker(new MarkerOptions()
                .position(currentPosition)
                .snippet("Lat:" + location.getLatitude() + "Lng:" + location.getLongitude())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title("현재위치"));

    }
}
