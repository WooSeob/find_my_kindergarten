package kr.studiows.findkindergarten;

import android.util.Log;

import androidx.viewpager.widget.ViewPager;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class KinderDataController {
    // ##### Data
    private KinderData myKinderData;
    private PriorityQueue<KinderData.Kinder> KinderQueueSortedByDistance = new PriorityQueue<>();
    private List<KinderData.Kinder> SortedKinderList = new ArrayList<>();   //현재 구에 보여지는 유치원들이 거리순으로 정렬되 있음
    private List<KinderData.Kinder> FavoriteKinderList = new ArrayList<>(); //즐겨찾기 등록된 유치원들이 모여있는 리스트
    private String TAG = "KinderDataController";

    // ###### Objects
    private BottomPanelController uiController;
    private DetailViewAdapter vAdapter;
    private ViewPager viewPager;
    private RecyclerAdapter rAdapter;

    private MapView mapView;

    public KinderDataController(){
        myKinderData = new KinderData(this);
    }

    public void showFavorites(){

    }
    public void showLists(){

    }

    public KinderData getKinderData(){
        return myKinderData;
    }

    /*
       <<필수>> KinderDataController 생성후 setUiController, setMapView 순으로 무조건 호출할것.
    * */
    public void setUiController(BottomPanelController uiController){
        this.uiController = uiController;
        this.viewPager = uiController.getViewPager();
        this.vAdapter = uiController.getAdapter();
        this.rAdapter = uiController.getRecyclerAdapter();
    }
    public void setMapView(MapView mapView){
        this.mapView = mapView;
        this.myKinderData.setMapView(mapView);
    }


    public List<KinderData.Kinder> getKinderList(){
        return SortedKinderList;
    }
    public List<KinderData.Kinder> getFavoriteList(){
        return FavoriteKinderList;
    }

    public void updateLatLng(KinderData.Kinder k, String la, String lo){
        MapPoint myLocation = MapPoint.mapPointWithGeoCoord(Double.parseDouble(la), Double.parseDouble(lo));

        k.setLatLng(la, lo, myLocation);
        k.setDistance(myKinderData.getUserLocation().distanceWith(myLocation,"meter"));

        //마커 설정
        MapPOIItem marker = new MapPOIItem();
        marker.setItemName(k.getName() + "/" + k.getParent().getSiGunGu());
        marker.setTag(0);
        marker.setMapPoint(myLocation);
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        k.setMarker(marker);
    }

    public void updateKinderLists(List<KinderData.Kinder> kindersToAdd){
        //TODO 우선순위 큐 동기화 해야될듯. 이 메서드 어떤 스레드에서 호출되고 있는지 확인할것.
        KinderQueueSortedByDistance.addAll(kindersToAdd);
        while(!KinderQueueSortedByDistance.isEmpty()){
            SortedKinderList.add(KinderQueueSortedByDistance.poll());
        }
    }
    public void loadDetailView(KinderData.Kinder k){
        /** 마커 터치 했을때 호출되는 메서드.
         *  유치원 리스트에서 선택된 유치원의 인덱스(i)를 찾아서 뷰페이저에 넘겨줌
         *  @k : 선택된 유치원
        **/
        int i = 0;
        for(KinderData.Kinder kinder : SortedKinderList){
            if(k == kinder){
                Log.d(TAG, "loadDetailView: i : " + i);
                vAdapter.notifyDataSetChanged();
                viewPager.setCurrentItem(i);
                break;
            }
            i++;
        }
    }
}
