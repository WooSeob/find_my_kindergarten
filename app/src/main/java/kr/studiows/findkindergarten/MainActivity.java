package kr.studiows.findkindergarten;

import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.MenuItem;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

public class MainActivity extends AppCompatActivity {
    FragmentManager fm;
    FragmentTransaction tran;
    fragHome Home;
    fragFind Find;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            fm = getSupportFragmentManager();
            tran = fm.beginTransaction();

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    tran.replace(R.id.main_frame, Home);
                    tran.commit();
                    return true;

                case R.id.navigation_find:
                    tran.replace(R.id.main_frame, Find);
                    tran.commit();

                    return true;

                case R.id.navigation_community:
                    return true;

                case R.id.navigation_mypage:
                    return true;

            }
            return false;
        }
    };



    /*
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fm = getSupportFragmentManager();
        tran = fm.beginTransaction();

        Home = new fragHome();
        Find = new fragFind();


        //홈으로 시작
        tran.replace(R.id.main_frame, Home);
        tran.commit();


    }


    //권한 요청에 대한 콜백
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1 : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Find.setGpsGranted(true);
                } else {
                    Find.setGpsGranted(false);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public void onBackPressed(){
        for (Fragment fragment: getSupportFragmentManager().getFragments()) {
            if (fragment.isVisible()) {
                //최상위 프래그먼트 가져오기.
                if(fragment instanceof fragFind){
                    // 지금 열려있는 프래그먼트가 fragFind 일때.
                    if(Find.onBackPressed()){
                        super.onBackPressed();
                    }
                }else{
                    // 지금 열려있는 프래그먼트가 fragFind 이외의 것일때
                    super.onBackPressed();
                }
            }
        }
    }
}
