package kr.studiows.findkindergarten;

import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import org.json.*;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class KakaoGeoCoder {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String KAKAO_LOCAL_REST_API_KEY = "KakaoAK b192454e96582093c91ea27c3d0eff24";

    public static void getLatLngJsonAsync(final KinderDataController controller, final KinderData.KinderUnits targetUnits, final KinderData.Kinder k){
        final String addr = (String)k.getAttr().get("addr");
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
                    for (int i = 0, size = responseHeaders.size(); i < size; i++)
                    {

                    }

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

