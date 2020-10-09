package research;

import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class SendToMarket implements Runnable {
  @Override
  public void run() {

  }
}

class SendToBroker implements Runnable {
  @Override
  public void run() {

  }
}

public class MessageDispatcher {
  private static Executor pool = Executors.newFixedThreadPool(200);

  public static void sendToMarket(String line, HashMap<String, ClientAttachment> markets) {
    
  }

}
