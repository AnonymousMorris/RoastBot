package Tutorial;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/*
This sends a request to the server for a response to the message and receives a request id
 */
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


    /*
    This method pings the server with the request id to check if the request is done processing
     */
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
                //checks the code to see if it's still processing
                if(responseCode == 202){
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Call newCall = client.newCall(request);
                    newCall.enqueue(this);
                }
                //received an answer
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
