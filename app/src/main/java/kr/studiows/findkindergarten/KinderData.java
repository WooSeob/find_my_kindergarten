package kr.studiows.findkindergarten;

import android.util.Log;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.Set;

public class KinderData {
    public final static String NURSERY_API_KEY = "89a20d8e18334933859fbe05125b96f5";
    public final static String KINDER_API_KEY = "b065a65e683d46d3abcff4f9780e5fd4";

    public final static int TYPE_KINDER = 0;
    public final static int TYPE_NURSERY = 1;
    private int searchType = TYPE_KINDER; //유치원, 어린이집 중 어떤것을 탐색할건지 정보를 담는 변수

    private KinderDataController myController;
    private MapView mapView;


    private Set<String> KinderTypes = new HashSet<>();
    private Set<String> NurseryTypes = new HashSet<>();
    private Set<String> establishTypes = KinderTypes; //레퍼런스만 있는변수


    private Set<String> CURRENT_GU = new HashSet<>();

    private HashMap<String, KinderUnits> KindersGroupedByGu = new HashMap<>();
    private HashMap<String, KinderUnits> NurseryGroupedByGu = new HashMap<>();

    private List<Kinder> Displayed = new ArrayList<>();


    private UserLocation currentLoc = new UserLocation();

    //private HashMap<String, NurseryUnits>
    private static String TAG = "KinderData";
    public int getKinderNurseryType(){
        return searchType;
    }
    public void setKinderNurseryType(int Type){
        //변경이 발생한경우에만 호출

        //기존 마커들 모두 지우기
        for(String gu : CURRENT_GU){
            if(isDataExist(searchType, gu)){
                getKinderUnitsFromGu(searchType, gu).removeMarkers();
            }
        }

        //옵션필터 재설정해주기.
        if(Type == TYPE_KINDER){
            establishTypes = KinderTypes;
        }else{
            establishTypes = NurseryTypes;
        }

        //타입변경후 새로그리기
        this.searchType = Type;

        for(String gu : CURRENT_GU){
            if(isDataExist(searchType, gu)){
                getKinderUnitsFromGu(searchType, gu).drawMarkers(myController.getIsFavoriteOnly(), establishTypes);
            }
        }

        myController.updateKinderLists(Displayed);

        //데이터 없으면 다운로드.
        for(String gu : CURRENT_GU){
            KinderAPI.getData(gu, myController, searchType);
        }

    }
    public void setEstablishTypes(int Type,String option, boolean isAdd){
        Log.d(TAG, "setType: type : " + option + ", isAdd : " + isAdd );
        Set<String> options;
        if(Type == TYPE_KINDER){
            options = KinderTypes;
        }else{
            options = NurseryTypes;
        }

        if(isAdd){
            options.add(option);
        }else{
            options.remove(option);
        }
        updateMarkers();
    }

    public Set<String> getTypes(){
        return establishTypes;
    }
    public KinderData(KinderDataController myController) {
        this.myController = myController;
        //TODO 임시
        KinderTypes.add("공립(병설)");
        KinderTypes.add("공립(단설)");
        KinderTypes.add("사립(사인)");
        KinderTypes.add("사립(법인)");

        NurseryTypes.add("국공립");
        NurseryTypes.add("사회복지법인");
        NurseryTypes.add("법인·단체등");
        //TODO 민간개인, 민간?
        NurseryTypes.add("민간개인");
        NurseryTypes.add("민간");
        NurseryTypes.add("가정");
        NurseryTypes.add("협동");
        NurseryTypes.add("직장");
    }
    public void addToDisplayedList(List<Kinder> DisplayedList){
        Displayed.addAll(DisplayedList);
    }

    public void deleteFromDisplayedList(List<Kinder> DisplaydList){
        Displayed.removeAll(DisplaydList);
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
                if(NurseryGroupedByGu.get(Si_Gun_Gu) != null){
                    return true;
                }else{
                    return false;
                }
        }
        return false;
    }

    public void makeNewGroup(int Type, String Si_gun_gu, KinderUnits newUnits){
        switch (Type){
            case TYPE_KINDER:
                KindersGroupedByGu.put(Si_gun_gu, newUnits);
                break;

            case TYPE_NURSERY:
                NurseryGroupedByGu.put(Si_gun_gu, newUnits);
                break;
        }
    }

    public void updateMarkers(){
        markerDrawingHandler(CURRENT_GU, CURRENT_GU);
    }

    public void markerDrawingHandler(Set<String> GusToRemove, Set<String> GusToDraw){
        //지우기
        if(GusToRemove != null){
            for(String gu : GusToRemove){
                if(isDataExist(searchType, gu)){
                    getKinderUnitsFromGu(searchType, gu).removeMarkers();
                }
            }
        }
        //그리기
        for(String gu : GusToDraw){
            if(isDataExist(searchType, gu)){
                getKinderUnitsFromGu(searchType, gu).drawMarkers(myController.getIsFavoriteOnly(), establishTypes);
            }
        }

        myController.updateKinderLists(Displayed);

        //myController.getvAdapter().notifyDataSetChanged();
        //myController.getListAdapter().notifyDataSetChanged();

    }
    public void setCurrentGu(Set<String> NewGu, Set<String> GusToRemove, Set<String> GusToDraw){
        //행정 구 포커싱이 바뀔때 호출
        //TODO test 마커 모두삭제
        markerDrawingHandler(GusToRemove, GusToDraw);

        CURRENT_GU.clear();
        CURRENT_GU.addAll(NewGu);

        //인디케이터 업데이트
        myController.getUiController().setSigunguIndicator(NewGu.toString());
    }
    public KinderUnits getKinderUnitsFromGu(int Type, String SiGunGu){
        if(isDataExist(Type, SiGunGu)){
            if(Type == TYPE_KINDER){
                return KindersGroupedByGu.get(SiGunGu);
            }else{
                return NurseryGroupedByGu.get(SiGunGu);
            }
        }else{
            Log.d(TAG, "getKinderUnitsFromGu : " + SiGunGu + " is now null !!!");
            return null;
        }
    }

    public KinderUnits newKinderUnits(String SiGunGu, int Type, int numKinders){
        return new KinderUnits(this, Type, SiGunGu, numKinders);
    }


    public Set<String> getSiGunGuList(){
        return KindersGroupedByGu.keySet();
    }


    public class Kinder implements Comparable<Kinder>{
        private int Type;
        private boolean isDisplayed = false;
        private boolean isReady = false;

        private boolean isFavorite = false;
        private double distance;
        private KinderUnits parent;
        private Map attr;
        //initialize required
        private MapPoint mpLocation = null;
        private MapPOIItem marker = null;
        /** Kinder 클래스 생성 시나리오.
         *  1. 생성자 Kinder(Map attr)
         *  2. updateLatLng(String, String)
         *     -> setMarker()
        * */

        public Kinder(KinderUnits parent, Map attr){
            this.parent = parent;
            this.Type = parent.Type;
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
            if(b){
                parent.addKinderToFavorites(this);
            }else{
                parent.removeKinderFromFavorites(this);
            }
            this.isFavorite = b;
        }
        public void setLatLng(String la, String lo){
            attr.put("la", Double.parseDouble(la.trim()));
            attr.put("lo", Double.parseDouble(lo.trim()));
        }

        public void setDistance(double d){
            this.distance = d;
        }
        public void setMarker(MapPOIItem marker, MapPoint mp){
            this.marker = marker;
            this.mpLocation = mp;
            this.isReady = true;
        }

        private void drawMarker(MapView mapView){
            if(isReady && !isDisplayed){
                mapView.addPOIItem(marker);
                isDisplayed = true;
                parent.getDisplayedList().add(this);
            }else{
                Log.d(TAG, "Kinder, drawMarker : drawMarker(MapView) was called but could't marker : " + marker + ", isDisplayed : " + isDisplayed);
            }
        }
        
        private void removeMarker(MapView mapView){
            if(isReady && isDisplayed){
                mapView.removePOIItem(marker);
                isDisplayed = false;
                parent.getDisplayedList().remove(this);
            }else{
                Log.d(TAG, "Kinder, removeMarker : removeMarker(MapView) was called but could not process. marker : " + marker + ", isDisplayed : " + isDisplayed);
            }
        }
        /**
         * 이름
         * 설립유형
         * 주소
         * 전화번호
         * 운영시간
         * 홈페이지
        */
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
            //TODO 유치원 어린이집 구분에 따라 값 받아올수 있도록
            return (String)attr.get(Key.name(this.Type));
        }
        public String getEstablishType(){
            return (String)attr.get(Key.estType(this.Type));
        }
        public String getAddr(){
            return (String)attr.get(Key.addr(this.Type));
        }
        public String getTelno(){
            return (String)attr.get(Key.telno(this.Type));
        }
        public String getOperTime(){
            return (String)attr.get(Key.optime(this.Type));
        }
        public String getHomepage(){
            return (String)attr.get(Key.homepage(this.Type));
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

    public class KinderUnits {
        /* KinderData의 HashMap에 시,군,구 별 Key에 해당하는 Value가 될 클래스
         *
         * */
        private KinderData parent;
        private int Type;
        private boolean isDisplayed = false; //마커가 맵에 그려져 있는지 아닌지를 나타내는 변수
        private boolean isComplete = false; //다운로드, 마커 세팅까지 모두 끝났음을 보장하는 변수

        private int displayedCount = 0;

        private String mySiGunGu = "";
        private int numKinders;

        private List<Kinder> Kinders = Collections.synchronizedList(new ArrayList<Kinder>());
        private List<Kinder> Favorites = Collections.synchronizedList(new ArrayList<Kinder>());

        private List<Kinder> Displayed = new ArrayList<>();

        private List<Kinder> Failures = new ArrayList<>();

        public KinderUnits(KinderData parent, int Type, String sigungu, int numKinders) {
            Log.d(TAG, "KinderUnits : new(" + sigungu + ")");
            this.parent = parent;
            this.Type = Type;
            this.numKinders = numKinders;
            this.mySiGunGu = sigungu;
        }

        public void addKinderToFavorites(Kinder k) {
            Favorites.add(k);
        }

        public void removeKinderFromFavorites(Kinder k) {
            Favorites.remove(k);
        }

        public boolean isDisplayed() {
            if (isComplete && isDisplayed) {
                return true;
            } else {
                Log.d(TAG, "isDisplayed : " + isDisplayed + ", isComplete : " + isComplete);
                return false;
            }
        }

        public void removeMarkers() {
            List<Kinder> target = new ArrayList<>(Displayed);
            parent.deleteFromDisplayedList(Displayed);
            for (Kinder k : target) {
                k.removeMarker(mapView);
            }
        }

        public void drawMarkers(boolean isFavoriteOnly, Set<String> estTypes) {
            Log.d(TAG, "drawMarkers: isFavoriteOnly : " + isFavoriteOnly);
            if (isFavoriteOnly) {
                for (Kinder k : Favorites) {
                    if (k != null) {
                        k.drawMarker(mapView);
                    } else {
                        Log.d(TAG, "drawMarkers : kinder is null");
                    }
                }
            } else {
                for (Kinder k : Kinders) {
                    if (k != null) {
                        //TODO 어린이집, 유치원 마커 드로잉(옵션기반) 로직 정리하기
                        if (searchType == this.Type) {
                            if(estTypes.contains(k.getEstablishType())){
                                //유치원 설립구분에 해당되면 마커 그려준다.
                                k.drawMarker(mapView);
                            }else{
                                Log.d(TAG, "drawMarkers: EstablishType : " + k.getEstablishType());
                            }
                        }
                    } else {
                        Log.d(TAG, "drawMarkers : kinder is null");
                    }
                }
            }
            parent.addToDisplayedList(Displayed);
        }

        public Kinder newKinder(Map attr) {
            return new Kinder(this, attr);
        }

        public Kinder findKinderByMarker(MapPOIItem marker) {
            Log.d(TAG, "findKinderByMarker : target : " + marker);

            for (Kinder k : Kinders) {
                Log.d(TAG, "findKinderByMarker : found : " + k.getMarker());
                if (k.getMarker() == marker) {
                    return k;
                }
            }
            //못찾은경우
            Log.d(TAG, "findKinderByMarker : can't find kinder");
            return null;
        }


        public void addKinder(Kinder k) {
            Kinders.add(k);

            checkComplete();
        }

        public void addKinderToFailures(Kinder k) {
            //위경도를 찾을수 없는 Kinder들
            Failures.add(k);
            checkComplete();
        }

        private void checkComplete() {
            if ((Kinders.size() + Failures.size()) == numKinders) {
                Log.d(TAG, mySiGunGu + "down load finished. num of items : " + numKinders + ", drawable : " + Kinders.size());
                /*  Kinder.updateLatLng() 호출 후
                    addKinder() 를 호출 하기 때문에 해당 kinder 는 마커까지 세팅이 되있음을 보장함.
                    이부분이 다운로드가 모두 완료된 시점임.
                */
                isComplete = true;
                parent.makeNewGroup(this.Type, mySiGunGu, this);

                //이 Unit의 구가 현재 포커스 되있는 구라면 바로 마커 그려준다.
                if (getCurrentGu().contains(mySiGunGu)) {
                    // 컨트롤러의 데이터 업데이트
                    // 해당하는 구 그려주기
                    Set<String> thisGu = new HashSet<>();
                    thisGu.add(mySiGunGu);
                    markerDrawingHandler(null, thisGu);
                }
            } else {
            }
        }

        public List<Kinder> getKinders() {
            return Kinders;
        }

        public String getSiGunGu() {
            return mySiGunGu;
        }

        public List<Kinder> getDisplayedList() {
            return Displayed;
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