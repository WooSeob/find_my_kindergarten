package kr.studiows.findkindergarten;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

public class BottomPanelControler {
    private ViewGroup bottomSheet;
    private ViewPager detailViewPager;
    private DetailViewAdapter mDetailViewAdapter;

    private Button btnFilter;
    private Button btnFavorite;
    private Button btnList;
    private ViewGroup vgListPanel;
    private ViewGroup vgFilterPanel;
    private ViewGroup vgFavoritePanel;

    private List<ViewGroup> innerPanels = new ArrayList<>();
    private List<ViewGroup> panels = new ArrayList<>();


    public void setBottomPanelControler(ViewGroup rootView){
        //뷰페이저 설정
        detailViewPager = (ViewPager)rootView.findViewById(R.id.detail_view_pager);
        //바텀시트 설정
        bottomSheet = rootView.findViewById(R.id.cdl_bottom_sheet);
        panels.add(detailViewPager);
        panels.add(bottomSheet);

        //하단 패널 설정
        vgFilterPanel = (ViewGroup)rootView.findViewById(R.id.filter_panel_container);
        vgFavoritePanel = (ViewGroup)rootView.findViewById(R.id.favorite_panel_container);
        vgListPanel = (ViewGroup)rootView.findViewById(R.id.list_panel_container);
        innerPanels.add(vgFilterPanel);
        innerPanels.add(vgFavoritePanel);
        innerPanels.add(vgListPanel);
        btnFilter = rootView.findViewById(R.id.btn_search_filter);
        btnFilter.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                changePanel(innerPanels, vgFilterPanel);
            }
        });
        btnFavorite = rootView.findViewById(R.id.btn_favorite);
        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePanel(innerPanels, vgFavoritePanel);
            }
        });
        btnList = rootView.findViewById(R.id.btn_listShow);
        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePanel(innerPanels, vgListPanel);
            }
        });
    }

    public static void changePanel(List<ViewGroup> panels, ViewGroup panelToShow){
        //PanelToShow만 Visible 로 설정하고 나머지는 모두 INVISIBLE로 설정함.
        for(ViewGroup vg : panels){
            vg.setVisibility(View.INVISIBLE);
        }
        panelToShow.setVisibility(View.VISIBLE);
    }

    public void setDetailViewPager(KinderData kd, Context context){
        Log.d("ddddd", "rrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr " + kd.getKinderListSortedByDistance().size());
        BottomPanelControler.changePanel(panels, detailViewPager);
        mDetailViewAdapter = new DetailViewAdapter(kd.getKinderListSortedByDistance(), context);
        detailViewPager.setAdapter(mDetailViewAdapter);
        detailViewPager.setPadding(100,0,100,0);
    }
}
