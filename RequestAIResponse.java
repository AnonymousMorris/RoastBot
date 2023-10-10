package Tutorial;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class RequestAIResponse {
    OkHttpClient client;
    public RequestAIResponse(){
        client = new OkHttpClient();
    }
    public CompletableFuture<String> que(String postBody){
        Request request = new Request.Builder()
                .url("http://localhost:5000/")
                .post(RequestBody.create(MediaType.parse("text/plain"), postBody))
                .build();
        CompletableFuture<String> futureAns = new CompletableFuture<String>();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.print("request failed");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String ans = response.body().string();
                System.out.print(ans);
                futureAns.complete(ans);
            }
        });


        return futureAns;

    }
    public CompletableFuture<String> getResponse (String requestId){
        Request request = new Request.Builder()
                .url("http://localhost:5000/retrieve")
                .post(RequestBody.create(MediaType.parse("text/plain"), requestId))
                .build();
        CompletableFuture<String>futureResponse = new CompletableFuture<String>();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e){ System.out.print("response failed"); }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                int responseCode = response.code();
                if(responseCode == 202){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Call newCall = client.newCall(request);
                    newCall.enqueue(this);
                }
                else if(responseCode == 200){
                    String ans = response.body().string();
                    System.out.print(ans);
                    futureResponse.complete(ans);
                }
                else{
                    System.out.printf("error code: %d", responseCode);
                }
            }
        });
        return futureResponse;
    }
}
