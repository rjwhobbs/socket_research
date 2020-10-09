package research;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class App2 {
  public static void main(String[] args) {
    System.out.println("Hello from whisper chat.");
    final AsyncWhisperChatServer asyncWhisperChatServer = new AsyncWhisperChatServer();

    Executor pool = Executors.newFixedThreadPool(2);

    pool.execute(new Runnable() {
      @Override
      public void run() {
        asyncWhisperChatServer.acceptBroker();
      }
    });

    pool.execute(new Runnable() {
      @Override
      public void run() {
        asyncWhisperChatServer.acceptMarket();
      }
    });
  }
}
