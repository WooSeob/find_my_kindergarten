package kr.studiows.findkindergarten;

import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class KinderData {

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
     */
    public KinderData(){

    }

}
