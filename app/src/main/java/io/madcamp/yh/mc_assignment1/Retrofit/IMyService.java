package io.madcamp.yh.mc_assignment1.Retrofit;

import android.util.Pair;

import org.json.JSONObject;

import java.util.ArrayList;

import io.madcamp.yh.mc_assignment1.Tab1Fragment;
import io.reactivex.Observable;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface IMyService {
    @POST("register")
    @FormUrlEncoded
    Observable<String> registerUser(@Field("email") String email,
                                    @Field("name") String name,
                                    @Field("password") String password);

    @POST("login")
    @FormUrlEncoded
    Observable<String> loginUser(@Field("email") String email,
                                 @Field("password") String password);

    @POST("addcontacts")
    @FormUrlEncoded
    //Observable<String> addContacts(@Field("contacts") ArrayList<Pair<String,String>> contacts);
    Observable<String> addContacts(@Field("contacts") String j);


    @POST("getcontacts")
    @FormUrlEncoded
    Observable<String> getContacts(@Field("contacts") String j);

    @POST("upload")
    @FormUrlEncoded
    Observable<String> uploadImage(@Field("Image") String imageurl);

    @POST("download")
    @FormUrlEncoded
    Observable<String> downloadImage(@Field("Image") String imageurl);


}
