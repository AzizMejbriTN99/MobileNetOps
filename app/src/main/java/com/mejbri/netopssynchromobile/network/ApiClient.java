package com.mejbri.netopssynchromobile.network;

import android.content.Context;
import com.mejbri.netopssynchromobile.util.SessionManager;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:5600/";

    private static Retrofit retrofit;
    private static Context appContext;

    public static void init(Context context) {
        appContext = context.getApplicationContext();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    String token = SessionManager.getToken(appContext);
                    Request original = chain.request();
                    Request request = token != null
                            ? original.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .build()
                            : original;
                    return chain.proceed(request);
                })
                .addInterceptor(logging)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static <T> T create(Class<T> service) {
        return retrofit.create(service);
    }
}
