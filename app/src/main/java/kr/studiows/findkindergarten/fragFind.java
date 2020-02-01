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
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;


import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class fragFind extends Fragment implements View.OnClickListener, MapView.MapViewEventListener , MapView.POIItemEventListener{
    public static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public final String KAKAO_MAP_API_KEY = "51e49db89617045c570a97ddbc60e7c7";
    public final String kinderData_API_KEY = "b065a65e683d46d3abcff4f9780e5fd4";


    private Queue<Set<String>> GuChangeTasks = new LinkedList<>();

    private boolean gpsGranted = false;

    public MapView.MapViewEventListener mapViewEventListener;
    public MapView mapView;

    private KinderDataController kinderDataController;
    private BottomPanelController uiController;

    public fragFind() {
        // Required empty public constructor
    }

    public void setGpsGranted(boolean b){
        this.gpsGranted = b;
    }

    @Override
    public void onStart(){
        super.onStart();
        mapView.setMapViewEventListener(this);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_find, container, false);

        //맵뷰
        mapView = new MapView(getActivity());
        kinderDataController = new KinderDataController();
        uiController = new BottomPanelController(getContext());

        uiController.setDataController(kinderDataController);
        uiController.setMapView(mapView);
        uiController.setBottomPanelController(rootView);

        kinderDataController.setUiController(uiController);
        kinderDataController.setMapView(mapView);

        ViewGroup mapViewContainer = (ViewGroup) rootView.findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);

        //Fragment 내에서 맵뷰이벤트 리스너를 사용하기 위해선 아래 코드 필요함
        mapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());
        mapView.setPOIItemEventListener(this);
        //유저 위치 객체(마커, 원 포함)

        mapView.setCurrentLocationEventListener(kinderDataController.getKinderData().getUserLocation());

        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
        mapView.setCurrentLocationRadius(100);

        updateSiGunGu(mapView.getMapCenterPoint());

        //MapPoint testLoc = MapPoint.mapPointWithGeoCoord(37.307135, 127.081089);
        //mapView.setMapCenterPoint(testLoc, true);
        return rootView;
    }


    @Override
    public void onClick(View view){
        Log.d("map", "onclick!!!!");
        switch (view.getId()){
        }
    }

    public boolean onBackPressed(){
        if (uiController.getBottomSheetBehavior().getState() == BottomSheetBehavior.STATE_EXPANDED){
            uiController.getBottomSheetBehavior().setState(BottomSheetBehavior.STATE_COLLAPSED);
            return false;
        }else{
            return true;
        }
    }

    public void updateSiGunGu(MapPoint mapPoint){
        double DELTA = 0.006;

        MapReverseGeoCoder.ReverseGeoCodingResultListener resultListener = new SiGunGuExtractor();
        MapPoint.GeoCoordinate center = mapPoint.getMapPointGeoCoord();
        MapPoint mp1, mp2, mp3, mp4;
        mp1 = MapPoint.mapPointWithGeoCoord(center.latitude + DELTA,center.longitude + DELTA);
        mp2 = MapPoint.mapPointWithGeoCoord(center.latitude - DELTA,center.longitude + DELTA);
        mp3 = MapPoint.mapPointWithGeoCoord(center.latitude + DELTA,center.longitude - DELTA);
        mp4 = MapPoint.mapPointWithGeoCoord(center.latitude - DELTA,center.longitude - DELTA);

        MapReverseGeoCoder rgc1 = new MapReverseGeoCoder(KAKAO_MAP_API_KEY, mp1, resultListener, getActivity());
        MapReverseGeoCoder rgc2 = new MapReverseGeoCoder(KAKAO_MAP_API_KEY, mp2, resultListener, getActivity());
        MapReverseGeoCoder rgc3 = new MapReverseGeoCoder(KAKAO_MAP_API_KEY, mp3, resultListener, getActivity());
        MapReverseGeoCoder rgc4 = new MapReverseGeoCoder(KAKAO_MAP_API_KEY, mp4, resultListener, getActivity());

        rgc1.startFindingAddress();
        rgc2.startFindingAddress();
        rgc3.startFindingAddress();
        rgc4.startFindingAddress();
    }

    /*
    -------------------------------------      카카오맵 이벤트 리스너      -----------------------------------------------------------
     */
    @Override
    public void onMapViewInitialized(MapView mapView) {
        Log.d("map", "initialized");
    }

    @Override
    public void onMapViewCenterPointMoved(final MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
        if(mapView.getCurrentLocationTrackingMode() != MapView.CurrentLocationTrackingMode.TrackingModeOff )
        {
            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
        }
    }


    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
        Log.d("map", "Map Move Finished");
        //mapView.removeAllPOIItems();
        //TODO 사람손으로 직접 드래그 했을때만 리버스 지오코딩 하도록 변경할것 !!
        updateSiGunGu(mapPoint);
    }

    /**
    * 마커 리스너
    * */
    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
        //마커 터치한 경우 호출
        //TODO KinderUnits을 CURRENT_GU기반으로 받아오기 때문에. 지도 줌레벨이 낮을경우 시 군 구 경계를 넘어선곳을 터치하는경우 대응해줘야함

        KinderData kinderData = kinderDataController.getKinderData(); //TODO 나중에 수정
        String gu = mapPOIItem.getItemName().split("/")[1];

        Log.d("map", "marker selected : " + gu);

        KinderData.Kinder selectedKinder = kinderData.getKinderUnitsFromGu(gu).findKinderByMarker(mapPOIItem);

        if(selectedKinder != null){
            kinderDataController.loadDetailView(selectedKinder);
            uiController.showDetailViewPager();
        }else{
            Log.d("map", "selectedKinder == null , gu : " + gu);
        }
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }

    /*
    * 여기까지 카카오맵 이벤트 리스너
    * */


    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
        private final View mCalloutBalloon = null;

        public CustomCalloutBalloonAdapter() {
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }
    }

    public class KinderAPI extends AsyncTask<Void, Void, String> {

        private String SiGunGu;
        private String url;
        private ContentValues values;

        public KinderAPI(String url, ContentValues values) {
            this.url = url;
            this.values = values;
        }

        public KinderAPI(String sigungu, String url, ContentValues values) {
            this.SiGunGu = sigungu;
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

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(String receivedData) {
            super.onPostExecute(receivedData);

            KinderData kinderData = kinderDataController.getKinderData(); //TODO 나중에 수정
            try{
                //유치원 API 다운로드 성공했을때 호출됨.
                JSONObject jsonObject = new JSONObject(receivedData);
                JSONArray kinderArray = jsonObject.getJSONArray("kinderInfo");
                // 시 군 구 별로 한꺼번에 불러온 데이터

                // 해당 시 군 구의 새로운 KinderUnits 생성.
                KinderData.KinderUnits newUnits = kinderData.newKinderUnits(this.SiGunGu, kinderArray.length());

                for(int i=0; i<kinderArray.length(); i++)
                {
                    JSONObject KinderObject = kinderArray.getJSONObject(i);
                    KinderData.Kinder k = newUnits.newKinder(JsonHelper.toMap(KinderObject));
                    //Log.d("Kinder API", "유치원 명 : " + k.getName() + ", 주소 : " + k.getAddr());
                    KakaoGeoCoder.getLatLngJsonAsync(kinderDataController, newUnits, k);
                }

            }catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
        }
    }

    public class SiGunGuExtractor implements MapReverseGeoCoder.ReverseGeoCodingResultListener{
        private int finishCount = 1;
        private Set<String> NewGu = new HashSet<>();

        public SiGunGuExtractor(){

        }

        @Override
        public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
            //TODO 전체 주소에서 구 를 추출해내는 로직 광역시 등에서 구 이름 중복되는 경우 해결할것.
            String foundGu = KinderData.getSGGfromAddress(s);
            if(foundGu.equals("")){
                // 구 를 추출해내지 못했을 때 처리
                return;
            }
            NewGu.add(foundGu);

            if(finishCount < 4){
                finishCount++;
            }else{
                // 마지막 호출
                KinderData kinderData = kinderDataController.getKinderData(); //TODO 나중에 수정
                Set<String> CURRENT_GU = kinderData.getCurrentGu();

                boolean isChanged = false;
                Log.d("map", "             #####  FINAL PROCESS  #####");

                // ## 새로 감지된 행정구에 대한 마커 다운로드 OR 드로잉
                for(String gu : NewGu){
                    if(!CURRENT_GU.contains(gu)){
                        Log.d("map", "New Gu detected" + gu);
                        isChanged = true;

                        if(!kinderData.isDataExist(KinderData.TYPE_KINDER, gu)){
                            //데이터 없으면 다운로드 한다.
                            KinderAPI downloadFromGu = new KinderAPI(gu, KinderData.getKinderDataURL(gu), null);
                            downloadFromGu.execute();
                            //TODO 어린이집 데이터 불러오기 추가할것.
                        }else{
                            //데이터 있으면 그려준다.
                            kinderData.getKinderUnitsFromGu(gu).drawMarkers();
                        }
                    }
                }

                //영역을 벗어난 행정구 찾아서 제거
                for(String gu : CURRENT_GU){
                    if(!NewGu.contains(gu)){
                        if(kinderData.isDataExist(KinderData.TYPE_KINDER, gu)){
                            Log.d("map", "Delete Markers : " + gu);
                            kinderData.getKinderUnitsFromGu(gu).removeMarkers();
                        }else{
                            //이전의 구를 아직 다운받는중인데 변경이 일어난경우
                        }
                        isChanged = true;
                    }
                }

                //변동사항 있을때만 CURRENT_GU 갱신
                if(isChanged){
                    kinderData.setCurrentGu(NewGu);
                }
                Log.d("map", "Revers GeoCoding current gu : " + CURRENT_GU);
            }
            return;
        }
        @Override
        public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
            Log.d("map", "Revers GeoCoding FAILED");
        }

    }
}

/*
*
*

public class UserLocation implements MapView.CurrentLocationEventListener{

        private MapView mapView;
        private boolean prev = false;

        private MapPoint CURRENT_LOC_POINT = null;

        private MapPOIItem centerMarker;
        private MapCircle centerCircle;

        public UserLocation(MapView m){
            this.mapView = m;
        }

        public MapPoint getMapPoint(){
            return CURRENT_LOC_POINT;
        }
        public boolean checkPermission(){
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                Log.d("map", "gps Denied");
                //요청
                //요청 안되는거 해결할것.
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
                centerMarker.setMarkerType(MapPOIItem.MarkerType.RedPin); // 기본으로 제공하는 RedPin 마커 모양.
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

        @Override
        public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {

        }

        @Override
        public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

        }

        @Override
        public void onCurrentLocationUpdateFailed(MapView mapView) {

        }

        @Override
        public void onCurrentLocationUpdateCancelled(MapView mapView) {

        }
    }

        MapPOIItem mpLeftTop = new MapPOIItem();
        MapPOIItem mpLeftBt = new MapPOIItem();
        MapPOIItem mpRightTop = new MapPOIItem();
        MapPOIItem mpRightBt = new MapPOIItem();

        mpLeftTop.setMapPoint(cLeftTop);
        mpLeftTop.setItemName("1");
        mpLeftTop.setTag(0);
        mpLeftTop.setMarkerType(MapPOIItem.MarkerType.RedPin); // 기본으로 제공하는 RedPin 마커 모양.

        mpLeftBt.setMapPoint(cLeftBottom);
        mpLeftBt.setItemName("2");
        mpLeftBt.setTag(0);
        mpLeftBt.setMarkerType(MapPOIItem.MarkerType.RedPin); // 기본으로 제공하는 RedPin 마커 모양.

        mpRightTop.setMapPoint(cRightTop);
        mpRightTop.setItemName("3");
        mpRightTop.setTag(0);
        mpRightTop.setMarkerType(MapPOIItem.MarkerType.RedPin);

        mpRightBt.setMapPoint(cRightBottom);
        mpRightBt.setItemName("4");
        mpRightBt.setTag(0);
        mpRightBt.setMarkerType(MapPOIItem.MarkerType.RedPin);

        mapView.addPOIItem(mpLeftTop);
        mapView.addPOIItem(mpLeftBt);
        mapView.addPOIItem(mpRightTop);
        mapView.addPOIItem(mpRightBt);
*/

