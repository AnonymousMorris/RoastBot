package Tutorial;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.CompletableFuture;
public class Bot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "" ;
    }

    @Override
    public String getBotToken() {
        return "";
    }

    @Override
    public void onUpdateReceived(Update update)  {
        System.out.println(update.getMessage().getText());
        System.out.println(update);

        if(update.getMessage().getFrom().getIsBot()){
            return;
        }


        RequestAIResponse AI = new RequestAIResponse();
        CompletableFuture<String> responseFuture = AI.que(update.getMessage().getText());
        responseFuture.thenAccept(requestId ->{
            System.out.print("got id, waiting for response");
            CompletableFuture<String>futureResponse = AI.getResponse(requestId);
            futureResponse.thenAccept(response ->{
                sendMessage(update.getMessage().getChatId(), response);
            });
        });
        System.out.print("received update");
    }
    public void sendMessage(Long recipient, String msg){
        System.out.print("Sending message");

        SendMessage sm = SendMessage.builder()
                .chatId(recipient.toString())
                .text(msg).build();
        try{
            execute(sm);
        }
        catch (TelegramApiException e){
            throw new RuntimeException(e);
        }
    }
}
