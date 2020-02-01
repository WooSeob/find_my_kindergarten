package kr.studiows.findkindergarten;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

public class KinderData {
    public final static String KINDER_API_KEY = "b065a65e683d46d3abcff4f9780e5fd4";
    public final static int TYPE_KINDER = 1;
    public final static int TYPE_NURSERY = 2;

    private KinderDataController myController;
    private MapView mapView;

    private Set<String> CURRENT_GU = new HashSet<>();


    private HashMap<String, KinderUnits> KindersGroupedByGu = new HashMap<>();
    private Queue<KinderUnits> drawingQueue = new LinkedList<>();

    private UserLocation currentLoc = new UserLocation();

    //private HashMap<String, NurseryUnits>
    private static String TAG = "KinderData";

    public KinderData(KinderDataController myController) {
        this.myController = myController;
    }

    public UserLocation getUserLocation(){
        return currentLoc;
    }
    public void setMapView(MapView mapView) {
        this.mapView = mapView;
    }

    public Set<String> getCurrentGu(){
        return CURRENT_GU;
    }

    public boolean isDataExist(int Type, String Si_Gun_Gu){
        switch (Type){
            case TYPE_KINDER:
                if(KindersGroupedByGu.get(Si_Gun_Gu) != null){
                    return true;
                }else{
                    return false;
                }

            case TYPE_NURSERY:
                //TODO 나중에 어린이집부분도 동일하게 구현할것.
                break;
        }
        return false;
    }

    public void makeNewGroup(int Type, String Si_gun_gu, KinderUnits newUnits){
        switch (Type){
            case TYPE_KINDER:
                KindersGroupedByGu.put(Si_gun_gu, newUnits);
                break;

            case TYPE_NURSERY:
                break;
        }
    }

    public void setCurrentGu(Set<String> NewGu){
        //행정 구 포커싱이 바뀔때 호출
        CURRENT_GU.clear();
        CURRENT_GU.addAll(NewGu);

        myController.getKinderList().clear();
        for(String gu : CURRENT_GU){
            if(isDataExist(TYPE_KINDER, gu)){
                myController.updateKinderLists(getKinderUnitsFromGu(gu).getKinders());
            }
            //현재 구에 추가 되있지만 데이터가 없는 것들은 나중에 완료되면 추가될것임.
        }
    }
    public KinderUnits getKinderUnitsFromGu(String SiGunGu){
        if(isDataExist(TYPE_KINDER, SiGunGu)){
            return KindersGroupedByGu.get(SiGunGu);
        }else{
            Log.d(TAG, "getKinderUnitsFromGu : " + SiGunGu + " is now null !!!");
            return null;
        }
    }
    public KinderUnits newKinderUnits(String SiGunGu, int numKinders){
        return new KinderUnits(this, SiGunGu, numKinders);
    }


    public Set<String> getSiGunGuList(){
        return KindersGroupedByGu.keySet();
    }

    public static String getSGGfromAddress(String address){
        String[] splitedDist = address.split(" ");
        String result = "";
        //ex. address = "경기도 용인시 수지구"

        for(int i = 0; i < splitedDist.length; i++){
            // result = 용인시
            if( splitedDist[i].endsWith("시") || splitedDist[i].endsWith("군") ){
                result += splitedDist[i];
            }
            if(splitedDist[i].endsWith("구")){
                // result = 용인시 수지구
                result = result + " " + splitedDist[i];
            }
        }
        //Log.d(TAG, "getSGGfromAddress / input : " + address + "/ output : " + result.trim());
        return result.trim();
    }

    public static String getKinderDataURL(String si_gun_gu){
        // params : 시 군 구 (ex "용인시 수지구")
        // return : 입력 받은 시 군 구에 해당하는 시도 코드, 시군구 코드를 포함한 API 쿼리스트링
        AddrCodeConst code = new AddrCodeConst();
        String SIDO_CODE = code.get_SIDO_CODE(si_gun_gu);
        String SGG_CODE = code.get_SIGUNGU_CODE(si_gun_gu);

        return "http://e-childschoolinfo.moe.go.kr/api/notice/basicInfo.do?key=" + KINDER_API_KEY + "&sidoCode=" + SIDO_CODE + "&sggCode=" + SGG_CODE;
    }



    public class Kinder implements Comparable<Kinder>{
        private boolean isDisplayed = false;
        private boolean isReady = false;

        private boolean isFavorite = false;
        private double distance;
        private KinderUnits parent;
        private Map attr;
        //initialize required
        private MapPoint mpLocation = null;
        private MapPOIItem marker = null;
        /** Kinder Entry Point.
         *  1. 생성자 Kinder(Map attr)
         *  2. updateLatLng(String, String)
         *     -> setMarker()
        * */
        public Kinder(KinderUnits parent, Map attr){
            this.parent = parent;
            this.attr = attr;
        }


        public boolean isMapPointEmpty(){
            return mpLocation == null;
        }

        public boolean isFavorite(){
            return this.isFavorite;
        }
        //setter
        public void setFavorite(boolean b){
            this.isFavorite = b;
        }
        public void setLatLng(String la, String lo, MapPoint mp){
            attr.put("la", la.trim());
            attr.put("lo", lo.trim());
            mpLocation = mp;
        }
        public void setDistance(double d){
            this.distance = d;
        }
        public void setMarker(MapPOIItem marker){
            this.marker = marker;
            this.isReady = true;
        }

        public void drawMarker(MapView mapView){
            if(isReady && !isDisplayed){
                mapView.addPOIItem(marker);
                parent.increaseDisplayCount();
                isDisplayed = true;
            }else{
                Log.d(TAG, "Kinder, drawMarker : drawMarker(MapView) was called but marker is null");
            }
        }
        
        public void removeMarker(MapView mapView){
            if(isReady && isDisplayed){
                mapView.removePOIItem(marker);
                isDisplayed = false;
            }else{
                Log.d(TAG, "Kinder, removeMarker : removeMarker(MapView) was called but could not process. marker : " + marker + ", isDisplayed : " + isDisplayed);
            }
        }
        //getter
        public KinderUnits getParent(){
            return parent;
        }
        public double getDistance(){
            return distance;
        }
        public MapPOIItem getMarker(){
            return marker;
        }
        public Map getAttr(){
            return this.attr;
        }
        public String getLatLng(){
            return "lat : " + mpLocation.getMapPointGeoCoord().latitude + ", lng: " + mpLocation.getMapPointGeoCoord().longitude;
        }
        public String getName(){
            return (String)attr.get("kindername");
        }
        public String getAddr(){
            return (String)attr.get("addr");
        }
        public String getEstablishType(){
            return (String)attr.get("establish");
        }

        @Override
        public int compareTo(Kinder kinder) {
            if(distance > kinder.getDistance()){
                return 1;
            }else if(distance < kinder.getDistance()){
                return -1;
            }else{
                return 0;
            }
        }
    }


    public class KinderUnits{
        /* KinderData의 HashMap에 시,군,구 별 Key에 해당하는 Value가 될 클래스
        *
        * */
        private KinderData parent;
        private boolean isDisplayed = false; //마커가 맵에 그려져 있는지 아닌지를 나타내는 변수
        private boolean isComplete = false; //다운로드, 마커 세팅까지 모두 끝났음을 보장하는 변수

        private int displayedCount = 0;

        private String mySiGunGu = "";

        private int numKinders;
        private int numKindersToDraw = 0;

        private List<Kinder> Group_Family = new ArrayList<>();
        private List<Kinder> Group_Private = new ArrayList<>();
        private List<Kinder> Group_National = new ArrayList<>();

        private List<Kinder> Kinders = Collections.synchronizedList(new ArrayList<Kinder>());
        private List<Kinder> Failures = new ArrayList<>();


        public boolean isDisplayed(){
            if(isComplete && isDisplayed){
                return true;
            }else{
                Log.d(TAG, "isDisplayed : " + isDisplayed + ", isComplete : " + isComplete);
                return false;
            }
        }

        public void increaseDisplayCount(){
            displayedCount++;

            if(displayedCount == numKindersToDraw){
                this.isDisplayed = true;
            }
        }

        public KinderUnits(KinderData parent, String sigungu, int numKinders){
            Log.d(TAG, "KinderUnits : new(" + sigungu + ")");
            this.parent = parent;
            this.numKinders = numKinders;
            this.mySiGunGu = sigungu;
        }


        public void removeMarkers(){
            if(isDisplayed()){
                for(Kinder k : Kinders){
                    k.removeMarker(mapView);
                }
                displayedCount = 0;
                this.isDisplayed = false;
            }else{
                Log.d(TAG, "deleteMarkers: 완전히 로딩되지않았기 때문에 마커를 지울 수 없습니다.");
            }
        }

        public void drawMarkers(){
            for(Kinder k : Kinders){
                if(k != null){
                    k.drawMarker(mapView);
                }else{
                    Log.d(TAG, "drawMarkers : kinder is null");
                }
            }
        }
        public Kinder newKinder(Map attr){
            return new Kinder(this, attr);
        }

        public Kinder findKinderByMarker(MapPOIItem marker){
            Log.d(TAG, "findKinderByMarker : target : " + marker);

            for(Kinder k : Kinders){
                Log.d(TAG, "findKinderByMarker : found : " + k.getMarker());
                if(k.getMarker() == marker){
                    return k;
                }
            }
            //못찾은경우
            Log.d(TAG, "findKinderByMarker : can't find kinder");
            return null;
        }


        public void addKinder(Kinder k){
            Kinders.add(k);
            String estType = k.getEstablishType();
            if (estType.equals("사립(사인)")){

            }else if(estType.equals("공립(병설)")){

            }else if(estType.equals("")){

            }
            checkComplete();
        }
        public void addKinderToFailures(Kinder k){
            Failures.add(k);
            checkComplete();
        }
        private void checkComplete(){
            if((Kinders.size() + Failures.size()) == numKinders){
                Log.d(TAG, mySiGunGu + "down load finished. num of items : " + numKinders + ", drawable : " + Kinders.size());
                /*  Kinder.updateLatLng() 호출 후
                    addKinder() 를 호출 하기 때문에 해당 kinder 는 마커까지 세팅이 되있음을 보장함.
                    이부분이 다운로드가 모두 완료된 시점임.
                */
                isComplete = true;
                numKindersToDraw = Kinders.size();
                parent.makeNewGroup(KinderData.TYPE_KINDER, mySiGunGu, this);

                //이 Unit의 구가 현재 포커스 되있는 구라면 바로 마커 그려준다.
                if(getCurrentGu().contains(mySiGunGu)){
                    // 컨트롤러의 데이터 업데이트
                    myController.updateKinderLists(getKinders());
                    drawMarkers();
                }
                //else면 removeMarkers 해보기!!
                //TODO 정렬알고리즘 나중에 구현할것

            }else{
            }
        }
        public List<Kinder> getKinders(){
            return Kinders;
        }
        public String getSiGunGu(){
            return mySiGunGu;
        }
    }



}



/*
    유치원
    http://e-childschoolinfo.moe.go.kr/api/notice/basicInfo.do

    key
    b065a65e683d46d3abcff4f9780e5fd4

    key (API key(*필수))
    pageCnt (페이지당 목록수)
    currentPage (페이지 번호)
    sidoCode (시도코드(*필수))
    sggCode (시군구코드(*필수))

    //경기도 시도코드 41
    //용인시 수지구 시군구코드 41465
    http://e-childschoolinfo.moe.go.kr/api/notice/basicInfo.do?key=b065a65e683d46d3abcff4f9780e5fd4&sidoCode=41&sggCode=41465

     key : 행 번호
            , officeedu : 교육청명
            , subofficeedu : 교육지원청명
            , kindername : 유치원명
            , establish : 설립유형
            , rppnname : 대표자명
            , ldgrname : 원장명
            , edate : 설립일
            , odate  : 개원일
            , addr : 주소
            , telno : 전화번호
            , hpaddr : 홈페이지
            , opertime : 운영시간
            , clcnt3 : 만3세학급수
            , clcnt4 : 만4세학급수
            , clcnt5 : 만5세학급수
            , mixclcnt : 혼합학급수
            , shclcnt : 특수학급수
            , ppcnt3 : 만3세유아수
            , ppcnt4 : 만4세유아수
            , ppcnt5 : 만5세유아수
            , mixppcnt : 혼합유아수
            , shppcnt : 특수유아수
     */