package kr.studiows.findkindergarten;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;


public class fragFind extends Fragment implements View.OnClickListener {
    public static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean gpsGranted = false;
    private UserLocation userPoint;
    private KinderData kData;

    private Panel filterPanel;
    private FrameLayout panelContainer;

    public fragFind() {
        // Required empty public constructor
    }

    public void setGpsGranted(boolean b){
        this.gpsGranted = b;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_find, container, false);

        //맵뷰
        final MapView mapView = new MapView(getActivity());
        ViewGroup mapViewContainer = (ViewGroup) rootView.findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
            //맵뷰 이벤트리스너 등록
        mapView.setMapViewEventListener(new MapView.MapViewEventListener() {
            @Override
            public void onMapViewInitialized(MapView mapView) {
                Log.d("map", "inittialized");
            }

            @Override
            public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {
                Log.d("map", "centerpoint moved");
            }

            @Override
            public void onMapViewZoomLevelChanged(MapView mapView, int i) {
                Log.d("map", "ZoomLevelChanged");
            }

            @Override
            public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
                Log.d("map", "SingleTapped");
            }

            @Override
            public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

            }

            @Override
            public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

            }

            @Override
            public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

            }

            @Override
            public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

            }

            @Override
            public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

            }
        });

        //유저 위치 객체(마커, 원 포함)
        userPoint = new UserLocation(mapView);

        //TODO 버튼 : 현위치 기준 지도 중심이동
        /*
        Button btnCenter = (Button)rootView.findViewById(R.id.change_map_center);
        btnCenter.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                userPoint.moveMapCenterToUser();
            }
        });

        Button btnFilter = rootView.findViewById(R.id.search_filter);
        btnFilter.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.d("fragFind", "onClick: ");
                if(filterPanel.getVisible() == true){
                    filterPanel.slideDownToHide();
                    panelContainer.setVisibility(View.INVISIBLE);
                }else{
                    panelContainer.setVisibility(View.VISIBLE);
                    filterPanel.slideUpToShow();
                }
            }
        });*/
        panelContainer = (FrameLayout)rootView.findViewById(R.id.panel_container);

        filterPanel = new Panel(getActivity());


        panelContainer.addView(filterPanel);
        panelContainer.setVisibility(View.VISIBLE);


        //지도 중심 사용자 위치로 이동
        //userPoint.moveMapCenterToUser();

        //데이터 불러오기
        String API_KEY = "b065a65e683d46d3abcff4f9780e5fd4";
        String SIDO_CODE = "41";
        String SGG_CODE = "41465";
        String getKinderDataURL =
                "http://e-childschoolinfo.moe.go.kr/api/notice/basicInfo.do?key=" + API_KEY + "&sidoCode=" + SIDO_CODE + "&sggCode=" + SGG_CODE;
        NetworkTask networkTask = new NetworkTask(getKinderDataURL, null);
        networkTask.execute();

        return rootView;
    }

    @Override
    public void onClick(View view){
        Log.d("map", "onclick!!!!");
        switch (view.getId()){
        }
    }

    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private ContentValues values;

        public NetworkTask(String url, ContentValues values) {

            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... params) {

            String result; // 요청 결과를 저장할 변수.
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, values); // 해당 URL로 부터 결과물을 얻어온다.

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
            Log.d("data_receive", s);
        }
    }

    public class UserLocation{

        private MapView mapView;
        private boolean prev = false;

        private MapPoint CURRENT_LOC_POINT = null;

        private MapPOIItem centerMarker;
        private MapCircle centerCircle;

        public UserLocation(MapView m){
            this.mapView = m;
        }

        public boolean checkPermission(){
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                Log.d("map", "gps Denied");
                //요청
                //TODO 요청을 해줘야되는데 ...
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                } else {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                }
                return false;

            }else{//권한 있을때
                return true;
            }
        }


        public void moveMapCenterToUser(){
            if(prev) {
                mapView.removePOIItem(centerMarker);
                mapView.removeCircle(centerCircle);
            }

            //위치 가져와서 MapPoint만들기
            if(checkPermission()) {
                LocationManager lm = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
                Location currentLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                CURRENT_LOC_POINT = MapPoint.mapPointWithGeoCoord(currentLoc.getLatitude(), currentLoc.getLongitude());

                mapView.setMapCenterPoint(CURRENT_LOC_POINT, true);

                //중심점 마커 띄우기
                centerMarker = new MapPOIItem();
                centerMarker.setItemName("Current User Point");
                centerMarker.setTag(0);
                centerMarker.setMapPoint(CURRENT_LOC_POINT);
                centerMarker.setMarkerType(MapPOIItem.MarkerType.RedPin); // 기본으로 제공하는 BluePin 마커 모양.
                centerMarker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
                mapView.addPOIItem(centerMarker);

                //현위치에 원 추가
                int rad = 70;
                int circleColor = Color.argb(70,255,0,0);
                centerCircle = new MapCircle(CURRENT_LOC_POINT, rad, circleColor, circleColor);

                mapView.addCircle(centerCircle);
                prev = true;
            }
        }
    }

}
