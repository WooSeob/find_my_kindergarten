package kr.studiows.findkindergarten;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class BottomPanelController {
    private Context context;

    private KinderDataController mDataController;
    private MapView mapView;
    private boolean isMapViewDraged = false; // MapMoveFinished 에서 사용자 터치로 드래그한건지 아닌지 구분하기위함

    private ViewGroup bottomSheet;
    private ViewPager detailViewPager;
    private DetailViewAdapter mDetailViewAdapter;

    private BottomSheetBehavior bottomSheetBehavior;

    private ProgressBar progressBar;
    private ViewGroup vgUiContainer;

    private Button btnCurrentPos, btnFilter, btnFavorite, btnList;
    private TextView sigunguIndicator;

    private Stack<ViewGroup> UiStack = new Stack<>();
    private ViewGroup vgListPanel, vgFilterPanel, vgFavoritePanel, vgEntireInfoPanel, DisplayedView;

    private RecyclerView recyclerViewList;
    private RecyclerAdapter recyclerAdapter;

    private RecyclerView recyclerViewFavorite;
    private RecyclerAdapter recyclerAdapterFavorite;

    private FilterPanelManager filter;
    private EntireInfoPanelManager FullInfo;
    private ListPanelManager ListPanel;

    private List<ViewGroup> innerPanels = new ArrayList<>();
    private List<ViewGroup> panels = new ArrayList<>();

    public BottomPanelController(Context context){
        this.context = context;
    }
    public void setDataController(KinderDataController c){
        this.mDataController = c;
    }

    public FilterPanelManager getFilterPanelManager(){
        return filter;
    }
    public void setMapView(MapView mapView){
        this.mapView = mapView;
    }
    public boolean isMapViewDraged(){
        return isMapViewDraged;
    }
    public void setMapViewDraged(boolean b){
        isMapViewDraged = b;
    }

    public void setIsFavoriteOnly(boolean b){
        mDataController.setIsFavoriteOnly(b);
    }

    public void setBottomPanelController(ViewGroup rootView){
        //뷰페이저 설정
        detailViewPager = (ViewPager)rootView.findViewById(R.id.detail_view_pager);
        detailViewPager.setVisibility(View.INVISIBLE);
        //바텀시트 설정
        bottomSheet = rootView.findViewById(R.id.cdl_bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(rootView.findViewById(R.id.bottom_sheet));

        panels.add(detailViewPager);
        panels.add(bottomSheet);

        //하단 패널 설정
        vgFilterPanel = (ViewGroup)rootView.findViewById(R.id.filter_panel_container);
        vgFavoritePanel = (ViewGroup)rootView.findViewById(R.id.favorite_panel_container);
        vgListPanel = (ViewGroup)rootView.findViewById(R.id.list_panel_container);
        vgEntireInfoPanel = (ViewGroup)rootView.findViewById(R.id.entire_info_panel_container);

        //리스트에 패널들 추가
        innerPanels.add(vgFilterPanel);
        innerPanels.add(vgFavoritePanel);
        innerPanels.add(vgListPanel);
        innerPanels.add(vgEntireInfoPanel);

        filter = new FilterPanelManager(vgFilterPanel);
        FullInfo = new EntireInfoPanelManager(vgEntireInfoPanel);
        ListPanel = new ListPanelManager(vgListPanel);
        //얜뭐지

        sigunguIndicator = rootView.findViewById(R.id.sigungu_indicator);

        //리사이클러 뷰 세팅
        recyclerViewFavorite = rootView.findViewById(R.id.rv_FavoriteList);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(context);
        recyclerViewFavorite.setLayoutManager(linearLayoutManager2);
        recyclerAdapterFavorite = new RecyclerAdapter(mDataController.getFavoriteList(), mDataController.getFavoriteList(), this);
        recyclerViewFavorite.setAdapter(recyclerAdapterFavorite);

        recyclerViewList = rootView.findViewById(R.id.rv_List);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerViewList.setLayoutManager(linearLayoutManager);
        recyclerAdapter = new RecyclerAdapter(mDataController.getKinderList(), mDataController.getFavoriteList(), this);
        recyclerViewList.setAdapter(recyclerAdapter);




        //버튼 xml 매핑
        btnCurrentPos = rootView.findViewById(R.id.change_map_center);
        btnFilter = rootView.findViewById(R.id.btn_search_filter);
        btnFavorite = rootView.findViewById(R.id.btn_favorite);
        btnList = rootView.findViewById(R.id.btn_listShow);

        //버튼 클릭리스너 설정
        btnClickListener bListener = new btnClickListener();
        btnCurrentPos.setOnClickListener(bListener);
        btnFilter.setOnClickListener(bListener);
        btnFavorite.setOnClickListener(bListener);
        btnList.setOnClickListener(bListener);


        progressBar = rootView.findViewById(R.id.progressBar);
        vgUiContainer = rootView.findViewById(R.id.ui_container);
        vgUiContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                switch (action){
                    case MotionEvent.ACTION_DOWN:
                        //ui 컨테이너 여백 터치시 바텀시트 보여주기
                        backToBottomSheet();
                        break;
                }
                return false;
            }
        });

        //뷰페이저, 어댑터 설정
        mDetailViewAdapter = new DetailViewAdapter(mDataController.getKinderList(), mDataController.getFavoriteList(), context, this);
        detailViewPager.setAdapter(mDetailViewAdapter);
        detailViewPager.setPadding(100,0,100,0);
        detailViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //페이지 전환되면 호출
                mDetailViewAdapter.notifyDataSetChanged();
                Log.d("detailviewpager", "onPageSelected: isFavorite : " + mDataController.getKinderList().get(position).isFavorite());
                mapView.setMapCenterPoint(mDataController.getKinderList().get(position).getMarker().getMapPoint(), true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //바텀시트 콜백
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            int State;
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                recyclerAdapter.notifyDataSetChanged();
                recyclerAdapterFavorite.notifyDataSetChanged();
                String state = "";

                switch (i) {
                    case BottomSheetBehavior.STATE_DRAGGING: {
                        state = "DRAGGING";
                        break;
                    }
                    case BottomSheetBehavior.STATE_SETTLING: {
                        state = "SETTLING";
                        break;
                    }
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        state = "EXPANDED";
                        break;
                    }
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        state = "COLLAPSED";
                        break;
                    }
                    case BottomSheetBehavior.STATE_HIDDEN: {
                        state = "HIDDEN";
                        break;
                    }
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:{
                        state = "HALF_EXPANDED";
                        break;
                    }
                }
                Log.d("bottomSheetBehavior", "onStateChanged : " + state);

            }

            @Override
            public void onSlide(@NonNull View view, float v) {

                //Log.d("bottomsheetcallback", "onSlide: v : " + v);
            }
        });
    }


    public ProgressBar getProgressBar(){
        return progressBar;
    }

    public BottomSheetBehavior getBottomSheetBehavior(){
        return bottomSheetBehavior;
    }

    private void backToBottomSheet(){
        changePanel(panels, bottomSheet);
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        UiStack.clear();
    }
    public void changePanel(List<ViewGroup> panels, ViewGroup panelToShow){
        //PanelToShow만 Visible 로 설정하고 나머지는 모두 INVISIBLE로 설정함.
        for(ViewGroup vg : panels){
            if(vg != panelToShow){
                vg.setVisibility(View.INVISIBLE);
            }
        }
        panelToShow.setVisibility(View.VISIBLE);
        DisplayedView = panelToShow;
    }

    public void showDetailViewPager(){
        //바텀시트 -> 디테일뷰
        if(detailViewPager.getVisibility() == View.INVISIBLE){
            UiStack.push(DisplayedView);
            changePanel(panels, detailViewPager);
        }
    }
    public boolean backAction(){
        //뒤로가기 클릭시, 바텀시트가 열려있을때 호출
        if(UiStack.empty()){
            if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED){
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                return false;
            }else{
                return true;
            }
        }else{
            ViewGroup Next = UiStack.pop();
            if(bottomSheet.getVisibility() == View.VISIBLE){
                if(Next == detailViewPager){
                    if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                    changePanel(panels, Next);
                }else{
                    changePanel(innerPanels, Next);
                }
            }else{
                changePanel(panels, Next);
            }
            return false;
        }
    }

    public void setSigunguIndicator(String s){
        sigunguIndicator.setText(s);
    }


    public void showEntireInformation(KinderData.Kinder k, int position){
        /** DetailViewAdapter, RecyclerAdapter 로부터 호출됨
        **/
        //바텀시트 띄워주기
        UiStack.push(DisplayedView);
        if(detailViewPager.getVisibility() == View.VISIBLE){
            //디테일뷰 -> 상세정보
            changePanel(panels, bottomSheet);
        }else{
            //리스트뷰 -> 상세정보
        }
        changePanel(innerPanels, vgEntireInfoPanel);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);

        //맵뷰 중앙위치 변경
        MapPoint.GeoCoordinate mCenter = mDataController.getKinderList().get(position).getMarker().getMapPoint().getMapPointGeoCoord();
        MapPoint Center = MapPoint.mapPointWithGeoCoord(mCenter.latitude - 0.002, mCenter.longitude);
        mapView.setMapCenterPoint(Center, true);
        mapView.setZoomLevel(2, true);

        //TODO (임시) 데이터로딩
        FullInfo.updateData(k);
    }

    public DetailViewAdapter getDetailViewAdapter(){
        return mDetailViewAdapter;
    }
    public ViewPager getViewPager(){
        return detailViewPager;
    }
    public RecyclerAdapter getRecyclerAdapter() {
        return recyclerAdapter;
    }

    class PanelStateManager{

    }

    class ListPanelManager{
        ViewGroup v;
        TextView SortingMethod;

        public ListPanelManager(ViewGroup v){
            this.v = v;
            SortingMethod = v.findViewById(R.id.sorting_method);
        }
        public void updateContent(Set<String> gu, Set<String> type, int numKinder){
            String text;
            String sType = "";
            if(mDataController.getKinderData().getKinderNurseryType() == KinderData.TYPE_KINDER){
                sType = "유치원 ";
            }else{
                sType = "어린이집 ";
            }
            text = gu.toString() + " 기준 " + type.toString() + " 유형의 " + sType + numKinder + "개를 찾았습니다.";
            SortingMethod.setText(text);
        }
    }

    class EntireInfoPanelManager{
        ViewGroup panel;

        TextView title, distance, type
                , addr
                , officeedu
                , subofficeedu
                , rppnname
                , ldgrname
                , edate
                , telno
                , odate
                , hpaddr
                , opertime
                , clcnt3
                , clcnt4
                , clcnt5
                , mixclcnt
                , shclcnt
                , ppcnt3
                , ppcnt4
                , ppcnt5
                , mixppcnt
                , shppcnt;
        public EntireInfoPanelManager(ViewGroup v){
            this.panel = v;
            title = v.findViewById(R.id.text_kinderName);
            distance = v.findViewById(R.id.text_distance);
            type = v.findViewById(R.id.text_type);

            addr = v.findViewById(R.id.t_addr);
            subofficeedu = v.findViewById(R.id.t_subofficeedu);
            rppnname = v.findViewById(R.id.t_rppnname);
            ldgrname = v.findViewById(R.id.t_ldgrname);
            edate = v.findViewById(R.id.t_edate);
            telno = v.findViewById(R.id.t_telno);
            odate = v.findViewById(R.id.t_odate);
            hpaddr = v.findViewById(R.id.t_hpaddr);
            opertime = v.findViewById(R.id.t_opertime);
            clcnt3 = v.findViewById(R.id.t_clcnt3);
            clcnt4 = v.findViewById(R.id.t_clcnt4);
            clcnt5 = v.findViewById(R.id.t_clcnt5);
            mixclcnt = v.findViewById(R.id.t_mixclcnt);
            shclcnt = v.findViewById(R.id.t_shclcnt);
            ppcnt3 = v.findViewById(R.id.t_ppcnt3);
            ppcnt4 = v.findViewById(R.id.t_ppcnt4);
            ppcnt5 = v.findViewById(R.id.t_ppcnt5);
            mixppcnt = v.findViewById(R.id.t_mixppcnt);
            shppcnt = v.findViewById(R.id.t_shppcnt);
        }
        /**
         * 이름
         * 설립유형
         * 주소
         * 전화번호
         * 운영시간
         * 홈페이지
         */
        public void updateData(KinderData.Kinder k){
            title.setText(k.getName());
            String dist = String.valueOf(Math.ceil(k.getDistance())) + "m";
            distance.setText(dist);
            type.setText(k.getEstablishType());
            addr.setText(k.getAddr());
            telno.setText(k.getTelno());
            opertime.setText(k.getOperTime());
            hpaddr .setText(k.getHomepage());

            Map attr = k.getAttr();
            subofficeedu.setText((String)attr.get("subofficeedu"));
            rppnname.setText((String)attr.get("rppnname"));
            ldgrname .setText((String)attr.get("ldgrname"));
            edate.setText((String)attr.get("edate"));
            odate .setText((String)attr.get("odate"));
            clcnt3.setText((String)attr.get("clcnt3"));
            clcnt4.setText((String)attr.get("clcnt4"));
            clcnt5.setText((String)attr.get("clcnt5"));
            mixclcnt.setText((String)attr.get("mixclcnt"));
            shclcnt.setText((String)attr.get("shclcnt"));
            ppcnt3.setText((String)attr.get("ppcnt3"));
            ppcnt4.setText((String)attr.get("ppcnt4"));
            ppcnt5.setText((String)attr.get("ppcnt5"));
            mixppcnt.setText((String)attr.get("mixppcnt"));
            shppcnt .setText((String)attr.get("shppcnt"));

        }
    }
    class FilterPanelManager implements View.OnClickListener{
        private String TAG = "FilterPanelManager";
        Set<String> Types = new HashSet<>();
        int CheckedKinderType = KinderData.TYPE_KINDER;
        ViewGroup panel;
        RadioGroup kinderType;
        CheckBox showOnlyFavorites;
        CheckBox type_public_b, type_public_d, type_private_b, type_private_s;

        public FilterPanelManager(ViewGroup v){
            this.panel = v;
            kinderType = v.findViewById(R.id.group_type_picker);
            showOnlyFavorites = v.findViewById(R.id.chbx_favorite);
            type_public_b = v.findViewById(R.id.chbx_public_byeongseol);
            type_public_d = v.findViewById(R.id.chbx_public_danseol);
            type_private_b = v.findViewById(R.id.chbx_private_beobin);
            type_private_s = v.findViewById(R.id.chbx_private_sain);

            kinderType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if(i == R.id.rb_kinder){
                        CheckedKinderType = KinderData.TYPE_KINDER;
                    } else if(i == R.id.rb_nursery){
                        CheckedKinderType = KinderData.TYPE_NURSERY;
                    }
                    //옵션이 변경되면 KinderData에도 알려준다.
                    mDataController.getKinderData().setKinderNurseryType(CheckedKinderType);

                    Log.d(TAG, "onCheckedChanged: Type is " + CheckedKinderType);

                }
            });
            showOnlyFavorites.setOnClickListener(this);
            type_public_b.setOnClickListener(this);
            type_private_b.setOnClickListener(this);
            type_public_d.setOnClickListener(this);
            type_private_s.setOnClickListener(this);

            setIsFavoriteOnly(showOnlyFavorites.isChecked());
        }
        public int getKinderNurseryType(){
            return CheckedKinderType;
        }
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.chbx_favorite:
                    //즐겨찾기만 보기
                    setIsFavoriteOnly(showOnlyFavorites.isChecked());
                    mDataController.getKinderData().updateMarkers();
                    break;

                case R.id.chbx_public_byeongseol:   //필터 옵션 - 공립 병설
                    mDataController.getKinderData().setEstablishTypes(KinderData.TYPE_KINDER, "공립(병설)", type_public_b.isChecked());
                    break;

                case R.id.chbx_public_danseol:      //필터 옵션 - 공립 단설
                    mDataController.getKinderData().setEstablishTypes(KinderData.TYPE_KINDER,"공립(단설)", type_public_d.isChecked());
                    break;

                case R.id.chbx_private_beobin:      //필터 옵션 - 사립 법인
                    mDataController.getKinderData().setEstablishTypes(KinderData.TYPE_KINDER,"사립(법인)", type_private_b.isChecked());
                    break;

                case R.id.chbx_private_sain:        //필터 옵션 - 사립 사인
                    mDataController.getKinderData().setEstablishTypes(KinderData.TYPE_KINDER,"사립(사인)", type_private_s.isChecked());
                    break;
            }
        }
    }

    class btnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.change_map_center:
                    //현위치 버튼 클릭시
                    int ZOOM_LEVEL = 3;
                    MapPoint loc = mDataController.getKinderData().getUserLocation().getMapPoint();
                    isMapViewDraged = true;
                    if(loc != null){
                        mapView.setMapCenterPoint(mDataController.getKinderData().getUserLocation().getMapPoint(), true);
                        mapView.setZoomLevel(ZOOM_LEVEL, true);
                    }else{
                        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
                        mapView.setCurrentLocationRadius(100);
                        Toast.makeText(context, "현재 위치를 찾는중입니다..",Toast.LENGTH_SHORT);
                    }
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    break;

                case R.id.btn_search_filter:
                    //필터 버튼 클릭시
                    changePanel(innerPanels, vgFilterPanel);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                    break;

                case R.id.btn_favorite:
                    //즐겨찾기 버튼 클릭시
                    changePanel(innerPanels, vgFavoritePanel);
                    recyclerAdapterFavorite.notifyDataSetChanged();
                    if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                    }
                    //데이터
                    //mDataController.showFavorites();
                    break;

                case R.id.btn_listShow:
                    //리스트보기 버튼 클릭시
                    changePanel(innerPanels, vgListPanel);
                    recyclerAdapter.notifyDataSetChanged();
                    if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                    }
                    ListPanel.updateContent(mDataController.getKinderData().getCurrentGu(), mDataController.getKinderData().getTypes(), mDataController.getKinderList().size());
                    //데이터로드
                    //mDataController.showLists();
                    break;

            }
        }
    }
}
