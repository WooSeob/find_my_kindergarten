package kr.studiows.findkindergarten;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Map;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemViewHolder> {
    private final static String TAG = "RecyclerAdapter";
    private List<KinderData.Kinder> kinders;
    private List<KinderData.Kinder> favorites;

    private BottomPanelController parent;

    public RecyclerAdapter(List<KinderData.Kinder> kinders, BottomPanelController parentController){
        this.kinders = kinders;
        this.parent = parentController;
    }
    public RecyclerAdapter(List<KinderData.Kinder> kinders, List<KinderData.Kinder> favorites, BottomPanelController parentController){
        this.kinders = kinders;
        this.favorites = favorites;
        this.parent = parentController;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.onBind(kinders.get(position), position);
    }

    @Override
    public int getItemCount() {
        return kinders.size();
    }


    class ItemViewHolder extends RecyclerView.ViewHolder{
        private TextView title, distance, addr, type, rate;
        //TODO 나중에 체크박스말고 다른걸로 변경할것
        private CheckBox favorite;
        private KinderData.Kinder thisKinder;
        private int thisPosition;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            distance = itemView.findViewById(R.id.distance);
            addr = itemView.findViewById(R.id.addr);
            type = itemView.findViewById(R.id.type);
            rate = itemView.findViewById(R.id.rate);
            favorite = itemView.findViewById(R.id.chbx_favorite);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /** 리스트 뷰에서 아이템을 클릭했을때
                     *  해당 유치원의 전체 정보를 보여주는 창으로 넘어감
                     **/
                    //홀더가 생성될때 kinder를 매개변수로 받아서 작업하기 때문에 thisKinder는 null 이 아님이 보장됨
                    parent.showEntireInformation(thisKinder, thisPosition);
                }
            });

            favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    thisKinder.setFavorite(favorite.isChecked());
                    if(favorite.isChecked()){
                        favorites.add(thisKinder);
                    }else{
                        // 체크 해제 되면 favorite 리스트에서 제거.
                        favorites.remove(thisKinder);
                    }

                    parent.getDetailViewAdapter().notifyDataSetChanged();

                    Log.d(TAG, "onClick: " + thisKinder.getName() + " favorite : " + thisKinder.isFavorite());
                }
            });

        }

        void onBind(KinderData.Kinder k, int pos){
            this.thisKinder = k;
            this.thisPosition = pos;
            String dist = String.valueOf(Math.ceil(k.getDistance())) + "m";
            title.setText(k.getName());
            distance.setText(dist);
            addr.setText(k.getAddr());
            type.setText(k.getEstablishType());
            rate.setText("★ 4.8");

            favorite.setChecked(k.isFavorite());
        }
    }
}
