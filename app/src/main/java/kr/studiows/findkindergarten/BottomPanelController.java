package kr.studiows.findkindergarten;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.util.List;

public class BottomPanelController {
    private Context context;

    private KinderDataController mDataController;
    private MapView mapView;

    private ViewGroup bottomSheet;
    private ViewPager detailViewPager;
    private DetailViewAdapter mDetailViewAdapter;

    private BottomSheetBehavior bottomSheetBehavior;

    private ViewGroup vgUiContainer;

    private Button btnCurrentPos;
    private Button btnFilter;
    private Button btnFavorite;
    private Button btnList;

    private TextView content;

    private ViewGroup vgListPanel;
    private ViewGroup vgFilterPanel;
    private ViewGroup vgFavoritePanel;
    private ViewGroup vgEntireInfoPanel;

    private RecyclerView recyclerViewList;
    private RecyclerAdapter recyclerAdapter;

    private RecyclerView recyclerViewFavorite;
    private RecyclerAdapter recyclerAdapterFavorite;

    private List<ViewGroup> innerPanels = new ArrayList<>();
    private List<ViewGroup> panels = new ArrayList<>();

    public BottomPanelController(Context context){
        this.context = context;
    }
    public void setDataController(KinderDataController c){
        this.mDataController = c;
    }

    public void setMapView(MapView mapView){
        this.mapView = mapView;
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

        //얜뭐지
        content = rootView.findViewById(R.id.t_name);

        //리사이클러 뷰 세팅
        recyclerViewList = rootView.findViewById(R.id.rv_List);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerViewList.setLayoutManager(linearLayoutManager);
        recyclerAdapter = new RecyclerAdapter(mDataController.getKinderList(), mDataController.getFavoriteList(), this);
        recyclerViewList.setAdapter(recyclerAdapter);

        recyclerViewFavorite = rootView.findViewById(R.id.rv_FavoriteList);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(context);
        recyclerViewFavorite.setLayoutManager(linearLayoutManager2);
        recyclerAdapterFavorite = new RecyclerAdapter(mDataController.getFavoriteList(), mDataController.getFavoriteList(), this);
        recyclerViewFavorite.setAdapter(recyclerAdapterFavorite);


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


        vgUiContainer = rootView.findViewById(R.id.ui_container);
        vgUiContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                switch (action){
                    case MotionEvent.ACTION_DOWN:
                        //ui 컨테이너 여백 터치시 바텀시트 보여주기
                        changePanel(panels, bottomSheet);
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
                mapView.setMapCenterPoint(mDataController.getKinderList().get(position).getMarker().getMapPoint(), true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //바텀시트 콜백
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                recyclerAdapter.notifyDataSetChanged();
                recyclerAdapterFavorite.notifyDataSetChanged();
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });
    }

    public BottomSheetBehavior getBottomSheetBehavior(){
        return bottomSheetBehavior;
    }

    public static void changePanel(List<ViewGroup> panels, ViewGroup panelToShow){
        //PanelToShow만 Visible 로 설정하고 나머지는 모두 INVISIBLE로 설정함.
        for(ViewGroup vg : panels){
            if(vg != panelToShow){
                vg.setVisibility(View.INVISIBLE);
            }
        }
        panelToShow.setVisibility(View.VISIBLE);
    }

    public void showDetailViewPager(){
        if(detailViewPager.getVisibility() == View.INVISIBLE){
            changePanel(panels, detailViewPager);
        }
    }
    public void showEntireInformation(KinderData.Kinder k){
        /** DetailViewAdapter, RecyclerAdapter 로부터 호출됨
        **/
        //바텀시트 띄워주기
        changePanel(panels, bottomSheet);
        //전체 정보 뷰 보여주기
        changePanel(innerPanels, vgEntireInfoPanel);
        //바텀시트 익스펜디드 상태로 변경
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        //TODO (임시) 데이터로딩
        String text = "";
        for(Object key : k.getAttr().keySet()){
            text = text + (String)key + " : " + k.getAttr().get(key) + "\n";
        }
        content.setText(text);

    }

    public DetailViewAdapter getAdapter(){
        return mDetailViewAdapter;
    }
    public ViewPager getViewPager(){
        return detailViewPager;
    }
    public RecyclerAdapter getRecyclerAdapter() {
        return recyclerAdapter;
    }

    class btnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.change_map_center:
                    //현위치 버튼 클릭시
                    int ZOOM_LEVEL = 3;
                    MapPoint loc = mDataController.getKinderData().getUserLocation().getMapPoint();
                    if(loc != null){
                        mapView.setMapCenterPoint(mDataController.getKinderData().getUserLocation().getMapPoint(), true);
                        mapView.setZoomLevel(ZOOM_LEVEL, true);
                    }else{
                        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
                        mapView.setCurrentLocationRadius(100);
                        Toast.makeText(context, "현재 위치를 찾는중입니다..",Toast.LENGTH_SHORT);
                    }
                    break;

                case R.id.btn_search_filter:
                    //필터 버튼 클릭시
                    changePanel(innerPanels, vgFilterPanel);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    break;

                case R.id.btn_favorite:
                    //즐겨찾기 버튼 클릭시
                    changePanel(innerPanels, vgFavoritePanel);
                    recyclerAdapterFavorite.notifyDataSetChanged();
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                    //데이터
                    //mDataController.showFavorites();
                    break;

                case R.id.btn_listShow:
                    //리스트보기 버튼 클릭시
                    changePanel(innerPanels, vgListPanel);
                    recyclerAdapter.notifyDataSetChanged();
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                    //데이터로드
                    //mDataController.showLists();
                    break;

            }
        }
    }
    //TODO 뷰페이저를 스와이프하면 맵 센터도 바뀐다 그러면 또 리버스지오코딩 하게되고 current_gu가 바뀐다. 하지만 notifyDataSetChange를 호출하지 않으니 에러난다
//'C:\Users\byunw\AppData\Local\Android\Sdk\platform-tools\adb.exe devices'
}
