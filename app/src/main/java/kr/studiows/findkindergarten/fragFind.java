package kr.studiows.findkindergarten;

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

public class fragFind extends Fragment implements View.OnClickListener, MapView.POIItemEventListener, MapView.MapViewEventListener{
    public static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public final String KAKAO_MAP_API_KEY = "51e49db89617045c570a97ddbc60e7c7";
    public final String kinderData_API_KEY = "b065a65e683d46d3abcff4f9780e5fd4";

    private boolean gpsGranted = false;

    public MapView.MapViewEventListener mapViewEventListener;
    public MapView mapView;

    private KinderDataController kinderDataController;
    private BottomPanelController uiController;
    private boolean isDraged = false;

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

        uiController.setMapViewDraged(true);
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
        /**
         * 뒤로가기 누를때 호출
        **/
        return uiController.backAction();
    }

    public void updateSiGunGu(MapPoint mapPoint){
        String TAG = "updateSiGunGu";
        Log.d(TAG, "start update sigungu");
        double DELTA = 0.007;

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
        String TAG = "MapEventListenerTest";
        Log.d(TAG, "onMapViewCenterPointMoved: ");
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {
        String TAG = "MapEventListenerTest";
        Log.d(TAG, "onMapViewZoomLevelChanged: ");
    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
        String TAG = "MapEventListenerTest";
        Log.d(TAG, "onMapViewSingleTapped: ");
    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
        String TAG = "MapEventListenerTest";
        Log.d(TAG, "onMapViewDoubleTapped: ");
    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
        String TAG = "MapEventListenerTest";
        Log.d(TAG, "onMapViewLongPressed: ");
    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
        String TAG = "MapEventListenerTest";
        Log.d(TAG, "onMapViewDragStarted: ");
        if(mapView.getCurrentLocationTrackingMode() != MapView.CurrentLocationTrackingMode.TrackingModeOff )
        {
            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
        }
    }


    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
        String TAG = "MapEventListenerTest";
        Log.d(TAG, "onMapViewDragEnded: ");
        //사용자가 직접 터치 드래그 했을때만 새롭게 시군구를 감지하도록 함
        uiController.setMapViewDraged(true);
    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
        String TAG = "MapEventListenerTest";
        Log.d(TAG, "onMapViewMoveFinished: ");

        if(uiController.isMapViewDraged()){
            updateSiGunGu(mapPoint);
            uiController.setMapViewDraged(false);
        }
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

        KinderData.Kinder selectedKinder = kinderData.getKinderUnitsFromGu(kinderData.getKinderNurseryType(), gu).findKinderByMarker(mapPOIItem);

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

    public class SiGunGuExtractor implements MapReverseGeoCoder.ReverseGeoCodingResultListener{
        private int finishCount = 1;
        private Set<String> NewGu = new HashSet<>();

        public SiGunGuExtractor(){

        }

        @Override
        public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
            //TODO 전체 주소에서 구 를 추출해내는 로직 광역시 등에서 구 이름 중복되는 경우 해결할것.
            String foundGu = KinderAPI.getSGGfromAddress(s);
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
                int SearchType = kinderData.getKinderNurseryType();
                boolean isChanged = false;
                Log.d("map", "             #####  FINAL PROCESS  #####");

                // ## 새로 감지된 행정구에 대한 마커 다운로드 OR 드로잉
                Set<String> GusToDraw = new HashSet<>();
                for(String gu : NewGu){
                    if(!CURRENT_GU.contains(gu)){
                        Log.d("map", "New Gu detected" + gu);
                        GusToDraw.add(gu);
                        isChanged = true;

                        //KakaoGeoCoder.getData(String, KinderDataController, int) : 해당 구, 타입의 데이터가 없으면 다운로드한다.
                        KinderAPI.getData(gu, kinderDataController, SearchType);
                    }
                }

                //영역을 벗어난 행정구 찾아서 제거
                Set<String> GusToRemove = new HashSet<>();
                for(String gu : CURRENT_GU){
                    if(!NewGu.contains(gu)){
                        if(kinderData.isDataExist(SearchType, gu)){
                            Log.d("map", "Delete Markers : " + gu);
                            GusToRemove.add(gu);
                        }else{
                            //이전의 구를 아직 다운받는중인데 변경이 일어난경우
                        }
                        isChanged = true;
                    }
                }

                //변동사항 있을때만 CURRENT_GU 갱신
                if(isChanged){
                    kinderData.setCurrentGu(NewGu, GusToRemove, GusToDraw);
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
