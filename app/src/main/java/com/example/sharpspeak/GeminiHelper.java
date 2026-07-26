package com.example.sharpspeak;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiHelper {

    // 🔴 ضع مفتاح Groq اوGMINAIالخاص بك هنا (الذي تبدأ حروفه بـ gsk_)
    //الكود جاهز 100% مش ضايل عليك الا هاذا
    //تمنياتي لك بالفائده ان شاء الله
    private static final String API_KEY = "";
    private static final OkHttpClient client = new OkHttpClient();

    public interface GeminiCallback {
        void onSuccess(String responseText);
        void onError(Throwable throwable);
    }

    public static void askGemini(String userPrompt, GeminiCallback callback) {
        String url = "https://api.groq.com/openai/v1/chat/completions";

        try {
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are an English tutor. Reply naturally, gently correct grammar, and suggest better vocabulary.");

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt);

            JSONArray messages = new JSONArray();
            messages.put(systemMessage);
            messages.put(userMessage);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "openai/gpt-oss-20b");
            jsonBody.put("messages", messages);
            jsonBody.put("temperature", 0.7);

            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    // إرجاع رسالة الخطأ التقنية مباشرة لتظهر على الشاشة
                    new Handler(Looper.getMainLooper()).post(() ->
                            callback.onError(new Exception("Network Fail: " + e.getMessage()))
                    );
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseData);

                            String reply = jsonResponse
                                    .getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");

                            new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(reply));

                        } catch (Exception e) {
                            new Handler(Looper.getMainLooper()).post(() ->
                                    callback.onError(new Exception("JSON Parse Error: " + e.getMessage()))
                            );
                        }
                    } else {
                        // طباعة رقم الخطأ القادم من السيرفر (مثل 401 أو 403 أو 429)
                        String errorBody = response.body() != null ? response.body().string() : "No body";
                        new Handler(Looper.getMainLooper()).post(() ->
                                callback.onError(new Exception("Server Error Code " + response.code() + ": " + errorBody))
                        );
                    }
                }
            });

        } catch (Exception e) {
            callback.onError(e);
        }
    }
}