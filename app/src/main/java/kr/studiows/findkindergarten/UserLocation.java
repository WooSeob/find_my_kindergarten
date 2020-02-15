package kr.studiows.findkindergarten;

import android.util.Log;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

public class UserLocation implements MapView.CurrentLocationEventListener{
    private static final String TAG = "UserLocation";
    private MapPoint CURRENT_LOC_POINT = null;

    public UserLocation(){
    }

    public MapPoint getMapPoint(){
        return CURRENT_LOC_POINT;
    }


    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
        this.CURRENT_LOC_POINT = mapPoint;
        mapView.setMapCenterPoint(getMapPoint(), true);
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }
    /**
     * 두 지점간의 거리 계산
     *
     * @param unit 거리 표출단위
     * @return
     */
    public double distanceWith(MapPoint mp, String unit) {
        if(CURRENT_LOC_POINT != null){
            double lat1, lon1, lat2, lon2;
            lat1 = mp.getMapPointGeoCoord().latitude;
            lon1 = mp.getMapPointGeoCoord().longitude;
            lat2 = CURRENT_LOC_POINT.getMapPointGeoCoord().latitude;
            lon2 = CURRENT_LOC_POINT.getMapPointGeoCoord().longitude;

            double theta = lon1 - lon2;
            double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

            dist = Math.acos(dist);
            dist = rad2deg(dist);
            dist = dist * 60 * 1.1515;

            if (unit.equals("kilometer")) {
                dist = dist * 1.609344;
            } else if(unit.equals("meter")){
                dist = dist * 1609.344;
            }

            //Log.d(TAG, "distance : " + dist);
            return (dist);
        }else{
            Log.d(TAG, "distance: CURRENT_LOC_POINT is Null !");
            return 0;
        }
    }
    // This function converts decimal degrees to radians
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
}