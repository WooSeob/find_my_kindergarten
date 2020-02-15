package kr.studiows.findkindergarten;

import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.RequiresApi;

import net.daum.mf.map.api.MapPoint;

import org.json.*;

import java.io.IOException;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class KinderAPI {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String KAKAO_LOCAL_REST_API_KEY = "KakaoAK b192454e96582093c91ea27c3d0eff24";

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

    public static String getKinderDataURL(String si_gun_gu, int Type){
        // params : 시 군 구 (ex "용인시 수지구")
        // return : 입력 받은 시 군 구에 해당하는 시도 코드, 시군구 코드를 포함한 API 쿼리스트링
        AddrCodeConst code = new AddrCodeConst();
        String SIDO_CODE = code.get_SIDO_CODE(si_gun_gu);
        String SGG_CODE = code.get_SIGUNGU_CODE(si_gun_gu);
        String KINDER_API = "http://e-childschoolinfo.moe.go.kr/api/notice/basicInfo.do?key=";
        String NUSERY_API = "http://api.childcare.go.kr/mediate/rest/cpmsapi030/cpmsapi030/request?key=";
        String url = "";

        //TODO 시군구 코드 어린이집 기준 버그잡기

        if(Type == KinderData.TYPE_KINDER){
            url = KINDER_API + KinderData.KINDER_API_KEY + "&sidoCode=" + SIDO_CODE + "&sggCode=" + SGG_CODE;
        }else if(Type == KinderData.TYPE_NURSERY){
            url = NUSERY_API + KinderData.NURSERY_API_KEY + "&arcode=" + SGG_CODE + "&stcode=";
        }
        Log.d("URL", "getKinderDataURL: " + url);
        return url;
    }

    public static void getData(final String gu, final KinderDataController kinderDataController, int Type){
        if(!kinderDataController.getKinderData().isDataExist(Type, gu)){
            ProgressBar progressBar = kinderDataController.getUiController().getProgressBar();

            progressBar.setVisibility(View.VISIBLE);
            //데이터 없으면 다운로드 한다.
            if(Type == KinderData.TYPE_KINDER){
                getKinderData(gu, kinderDataController, progressBar);
            }else if (Type == KinderData.TYPE_NURSERY){
                getNurseryData(gu, kinderDataController, progressBar);
            }
        }else{
            // 데이터 이미 존재할 경우
        }

    }

    private static void getKinderData(final String gu, final KinderDataController kinderDataController, final ProgressBar progressBar){
        Request request = new Request.Builder()
                .url(getKinderDataURL(gu, KinderData.TYPE_KINDER))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    String receivedData = responseBody.string();
                    Log.d("KakaoGeoCoder", "Kinder data : " + receivedData);

                    KinderData kinderData = kinderDataController.getKinderData(); //TODO 나중에 수정
                    try{
                        //유치원 API 다운로드 성공했을때 호출됨.
                        JSONObject jsonObject = new JSONObject(receivedData);
                        Log.d("KakaoGeoCoder", "Kinder data : " + jsonObject);
                        JSONArray kinderArray = jsonObject.getJSONArray("kinderInfo");
                        // 시 군 구 별로 한꺼번에 불러온 데이터

                        // 해당 시 군 구의 새로운 KinderUnits 생성.
                        KinderData.KinderUnits newUnits = kinderData.newKinderUnits(gu, KinderData.TYPE_KINDER, kinderArray.length());

                        for(int i=0; i<kinderArray.length(); i++) {
                            JSONObject KinderObject = kinderArray.getJSONObject(i);
                            KinderData.Kinder k = newUnits.newKinder(JsonHelper.toMap(KinderObject));

                            //위도 경도 ReverseGeoCoding
                            KinderAPI.getLatLngJsonAsync(kinderDataController, newUnits, k);
                        }

                    }catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });

    }


    /**
     * 어린이집 정보공개 포털 (http://info.childcare.go.kr/info/main.jsp) 을 통한 API 사용 응답콜백
     * KinderAPI.getNurseryData(String, KinderDataController)
     *  1. 받아온 XML String을 XmlToJson 객체로 바꿔준다.
     *  2. XmlToJson객체에서 "response"의 value(배열)를 꺼내 JsonObject로 만들어준다
     *  3. TODO??
     *  4. 새로운 KinderUnits newUnits를 생성하고
     *  5. NurseryArray에서 JSONObject를 하나씩 추출해 Kinder 객체를 하나하나 생성해준다.
     *  6. 위경도 값이 있으면 setMarker(Kinder)해주고 newUnits에 추가, 없으면 지오코딩 시킨다.
     */
    private static void getNurseryData(final String gu, final KinderDataController kinderDataController, final ProgressBar progressBar){
        Request request = new Request.Builder()
                .url(getKinderDataURL(gu, KinderData.TYPE_NURSERY))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override public void onResponse(Call call, Response response) throws IOException {

                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    String data = responseBody.string();
                    XmlToJson xmlToJson = new XmlToJson.Builder(data).build();

                    KinderData kinderData = kinderDataController.getKinderData(); //TODO 나중에 수정

                    try{
                        //어린이집 API 다운로드 성공했을때 호출됨.
                        JSONObject jsonObject = xmlToJson.toJson().getJSONObject("response"); //어린이집 api 구조가 "response" 키의 value 가 배열임
                        Log.d("KakaoGeoCoder", "Nursery data : " + jsonObject);
                        JSONArray NurseryArray = jsonObject.getJSONArray("item");
                        // 시 군 구 별로 한꺼번에 불러온 데이터

                        // 해당 시 군 구의 새로운 NurseryUnits 생성.
                        KinderData.KinderUnits newUnits = kinderData.newKinderUnits(gu, KinderData.TYPE_NURSERY, NurseryArray.length());
                        for(int i=0; i<NurseryArray.length(); i++) {
                            //개별 어린이집 -> Nursery 클래스 매핑
                            JSONObject NurseryObject = NurseryArray.getJSONObject(i);
                            Log.d("Kinder API", i + ". 어린이집 : " + NurseryObject);

                            KinderData.Kinder k = newUnits.newKinder(JsonHelper.toMap(NurseryObject));

                            if(kinderDataController.isLatLngExist(k)){
                                kinderDataController.setMarker(k);
                                newUnits.addKinder(k);
                            }else{
                                //어린이집 위경도 없는 항목 reverseGeoCoding 시도하기
                                KinderAPI.getLatLngJsonAsync(kinderDataController, newUnits, k);
                            }
                        }

                    }catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private static void getLatLngJsonAsync(final KinderDataController controller, final KinderData.KinderUnits targetUnits, final KinderData.Kinder k){
        final String addr = k.getAddr();
        Request request = new Request.Builder()
                .url("https://dapi.kakao.com/v2/local/search/address.json?query=" + addr)
                .header("Authorization", KAKAO_LOCAL_REST_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    Headers responseHeaders = response.headers();

                    String data = responseBody.string();
                    //Log.d("KakaoGeoCoder", "data : " + data);
                    try{
                        JSONObject json = new JSONObject(data);
                        JSONArray responseArray = json.getJSONArray("documents");

                        JSONObject d = responseArray.getJSONObject(0);
                        //Log.d("KakaoGeoCoder", "유치원명 : " + k.getName() + " / 주소 : " +  addr);
                        //Log.d("KakaoGeoCoder", "d : " + d);
                        //Log.d("KakaoGeoCoder", "x : " + d.get("x") + ", y : " + d.get("y"));
                        controller.updateLatLng(k, (String)d.get("y"), (String)d.get("x"));
                        targetUnits.addKinder(k);

                    }catch (JSONException e) {
                        //GeoCoding 실패 했을경우.
                        Log.d("KakaoGeoCoder", "< !!! 에러 !!! > 유치원명 : " + k.getName() + " / 주소 : " +  addr);
                        targetUnits.addKinderToFailures(k);
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getLatLngJson(final KinderData.KinderUnits targetUnits, final KinderData.Kinder k) throws Exception {
        final String addr = (String)k.getAttr().get("addr");
        Request request = new Request.Builder()
                .url("https://dapi.kakao.com/v2/local/search/address.json?query=" + addr)
                .header("Authorization", KAKAO_LOCAL_REST_API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            Log.d("KakaoGeoCoder", "data : " + response.body().string());
            return response.body().string();
        }
    }
}

