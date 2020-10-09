package research;

public class App {
  public static void main(String[] args) {
    System.out.println("Hello from whisper chat.");
    AsyncWhisperChatServer asyncWhisperChatServer = new AsyncWhisperChatServer();



    asyncWhisperChatServer.acceptBroker();
    asyncWhisperChatServer.acceptMarket();
  }
}
