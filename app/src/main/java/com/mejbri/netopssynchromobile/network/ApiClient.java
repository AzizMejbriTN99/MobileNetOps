package com.mejbri.netopssynchromobile.network;

import android.content.Intent;
import android.content.Context;
import com.mejbri.netopssynchromobile.ui.LoginActivity;
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
                .addInterceptor(chain -> {
                    Response response = chain.proceed(chain.request());
                    if (response.code() == 401) {
                        SessionManager.clear(appContext);
                        Intent intent = new Intent(appContext, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        appContext.startActivity(intent);
                    }
                    return response;
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
