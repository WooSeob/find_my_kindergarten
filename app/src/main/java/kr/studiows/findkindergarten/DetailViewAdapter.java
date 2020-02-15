package kr.studiows.findkindergarten;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;
import java.util.PriorityQueue;

public class DetailViewAdapter extends PagerAdapter {

    private static final String TAG = "DetailViewAdapter";
    private List<KinderData.Kinder> kinders;
    private List<KinderData.Kinder> favorites;

    private BottomPanelController parent;

    private LayoutInflater layoutInflater;
    private Context context;

    public DetailViewAdapter(List<KinderData.Kinder> kinders, Context context, BottomPanelController parentController){
        //this.mKinderData = kinderdata;
        this.kinders = kinders;
        this.context = context;
        this.parent = parentController;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


    public DetailViewAdapter(List<KinderData.Kinder> kinders, List<KinderData.Kinder> favorites, Context context, BottomPanelController parentController){
        //this.mKinderData = kinderdata;
        this.kinders = kinders;
        this.favorites = favorites;
        this.context = context;
        this.parent = parentController;
    }

    public void setData(List<KinderData.Kinder> kinders){
        this.kinders = kinders;
    }
    @Override
    public int getCount() {
        return kinders.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.detail_panel, container, false);

        //List<KinderData.Kinder> kinders = mKinderData.getKinderListSortedByDistance();
        TextView name, type, addr;
        CheckBox favorite;

        name = view.findViewById(R.id.text_kinderName);
        type = view.findViewById(R.id.text_type);
        addr = view.findViewById(R.id.text_addr);
        favorite = view.findViewById(R.id.chbx_favorite);

        name.setText(kinders.get(position).getName());
        type.setText(kinders.get(position).getEstablishType());
        addr.setText(kinders.get(position).getAddr());
        favorite.setChecked(kinders.get(position).isFavorite());
        
        favorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d(TAG, "onCheckedChanged: ");
            }
        });
        favorite.setOnClickListener(new FavoriteClickListener(position, favorite));
        view.setOnClickListener(new CardClickListener(position));

        container.addView(view, 0);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

    class CardClickListener implements View.OnClickListener{
        private int pos;

        public CardClickListener(int position){
            this.pos = position;
        }
        @Override
        public void onClick(View view) {
            /** 유치원 마커를 클릭했을때 나오는
             *  디테일 뷰 에서 해당 뷰를 클릭했을때
             *  해당 유치원의 전체 정보를 보여주는 창으로 넘어감
            **/
            Log.d(TAG, "onClick: " + pos);
            Log.d(TAG, "onClick: " + kinders.get(pos).getName());
            parent.showEntireInformation(kinders.get(pos), pos);
        }
    }
    class FavoriteClickListener implements View.OnClickListener {
        private int pos;
        private CheckBox favorite;
        public FavoriteClickListener(int pos, CheckBox c){
            this.pos = pos;
            this.favorite = c;
        }

        @Override
        public void onClick(View view) {
            KinderData.Kinder thisKinder = kinders.get(pos);

            thisKinder.setFavorite(favorite.isChecked());
            if(favorite.isChecked()){
                favorites.add(thisKinder);
            }else{
                // 체크 해제 되면 favorite 리스트에서 제거.
                favorites.remove(thisKinder);
            }
            notifyDataSetChanged();
        }
    }
}
