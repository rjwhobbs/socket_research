package research;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AsyncWhisperChatServer {

  private static BufferedReader blockerReader = new BufferedReader(new InputStreamReader(System.in));
  private static Pattern p = Pattern.compile("^\\\\(\\d+)\\s+(.+)");
  private static Executor pool = Executors.newFixedThreadPool(200);

  private HashMap<String, ClientAttachment> brokers = new HashMap<>();
  private HashMap<String, ClientAttachment> markets = new HashMap<>();
  private static int brokersIndex = 1;
  private static int marketsIndex = 1;

  public void acceptBroker() {
    try (final AsynchronousServerSocketChannel brokerChannel = AsynchronousServerSocketChannel.open()) {
      InetSocketAddress hostAddress = new InetSocketAddress("localhost", 5000);
      brokerChannel.bind(hostAddress);
      while (true) {
        brokerChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
          @Override
          public void completed(AsynchronousSocketChannel result, Object attachment) {
            if (result.isOpen()) {
              brokerChannel.accept(null, this);
              try {
                registerBroker(result);
              } catch (ExecutionException e) {
                System.out.println("Router Error in acceptBroker(): ");
                e.printStackTrace();
              } catch (InterruptedException e) {
                System.out.println("Router Error in acceptBroker(): ");
                e.printStackTrace();
              }
              System.out.println("Broker Connected");
            }
          }

          private void registerBroker(AsynchronousSocketChannel client) throws ExecutionException, InterruptedException {
            String brokerID = Integer.toString(brokersIndex);
            String welcomeMsg = "Welcome to whisper chat, your ID is " + brokerID + "\n";
            ++brokersIndex;
            client.write(ByteBuffer.wrap(welcomeMsg.getBytes())).get();
            ClientAttachment clientAttachment = new ClientAttachment(client, brokerID);
            brokers.put(brokerID, clientAttachment);
            System.out.println(brokers.entrySet());
            client.read(clientAttachment.buffer, clientAttachment, new BrokerReadHandler());
          }

          @Override
          public void failed(Throwable exc, Object attachment) {
            System.out.println("Router Error in acceptBroker(): " + exc.getMessage());
          }
        });
        System.out.println("Listening on port 5000");
        blocker();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void acceptMarket() {
    try (final AsynchronousServerSocketChannel marketChannel = AsynchronousServerSocketChannel.open()) {
      InetSocketAddress hostAddress = new InetSocketAddress("localhost", 5001);
      marketChannel.bind(hostAddress);

      while (true) {
        marketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
          @Override
          public void completed(AsynchronousSocketChannel result, Object attachment) {
            if (result.isOpen()) {
              marketChannel.accept(null, this);
              try {
                registerMarket(result);
              } catch (ExecutionException e) {
                System.out.println("Router Error in acceptMarket(): ");
                e.printStackTrace();
              } catch (InterruptedException e) {
                System.out.println("Router Error in acceptMarket(): ");
                e.printStackTrace();
              }
              System.out.println("Market Connected");
            }
          }

          private void registerMarket(AsynchronousSocketChannel client) throws ExecutionException, InterruptedException {
            String marketID = Integer.toString(marketsIndex);
            String welcomeMsg = "Welcome to whisper chat, your ID is " + marketID + "\n";
            ++marketsIndex;
            client.write(ByteBuffer.wrap(welcomeMsg.getBytes())).get();
            ClientAttachment clientAttachment = new ClientAttachment(client, marketID);
            markets.put(marketID, clientAttachment);
            System.out.println(markets.entrySet());
            client.read(clientAttachment.buffer, clientAttachment, new MarketReadHandler());
          }

          @Override
          public void failed(Throwable exc, Object attachment) {
            System.out.println("Router Error in acceptMarket(): " + exc.getMessage());
          }
        });
        System.out.println("Listening on port 5001");
        blocker();
      }
    } catch (Exception e) {

    }
    System.out.println("Listening on port 5001");
  }

  class BrokerReadHandler implements CompletionHandler<Integer, ClientAttachment> {

    @Override
    public void completed(Integer result, ClientAttachment attachment) {
      if (result != -1) {
        attachment.buffer.flip();
        int limit = attachment.buffer.limit();
        byte[] bytes = new byte[limit];
        attachment.buffer.get(bytes, 0, limit);
        String line = new String(bytes);
        System.out.println(line);
        sendToMarket(line, attachment.id);
        attachment.buffer.clear();
        attachment.client.read(attachment.buffer, attachment, this);
      }
    }

    @Override
    public void failed(Throwable exc, ClientAttachment attachment) {
      System.out.println("Failed method in BrokerHandler called: " + exc.getMessage());
    }
  }

  class MarketReadHandler implements CompletionHandler<Integer, ClientAttachment> {

    @Override
    public void completed(Integer result, ClientAttachment attachment) {
      if (result != -1) {
        attachment.buffer.flip();
        int limit = attachment.buffer.limit();
        byte[] bytes = new byte[limit];
        attachment.buffer.get(bytes, 0, limit);
        String line = new String(bytes);
        System.out.println(line);
        sendToBroker(line, attachment.id);
        attachment.buffer.clear();
        attachment.client.read(attachment.buffer, attachment, this);
      }
    }

    @Override
    public void failed(Throwable exc, ClientAttachment attachment) {

    }
  }

  private void sendToMarket(String msg, String senderId) {
    pool.execute(new SendToMarket(msg, senderId));
  }

  private void sendToBroker(String msg, String senderId) {
    pool.execute(new SendToBroker(msg, senderId));
  }

  class SendToMarket implements Runnable {
    private String msg;
    private String senderId;

    SendToMarket(String msg, String senderId) {
      this.msg = msg.trim();
      this.senderId = senderId;
    }

    @Override
    public void run() {
      Matcher m = p.matcher(msg);
      String marketId;
      String extractedMsg;

      if (m.find()) {
        marketId = m.group(1);
        extractedMsg = m.group(2) + "\n";

        ClientAttachment clientAttachment = markets.get(marketId);
        if (clientAttachment != null) {
          try {
            clientAttachment.client.write(ByteBuffer.wrap(extractedMsg.getBytes())).get();
          } catch (InterruptedException e) {
            System.out.println("Error sending to market:");
            e.printStackTrace();
          } catch (ExecutionException e) {
            System.out.println("Error sending to market:");
            e.printStackTrace();
          }
        }
        else {
          printToSender("Market can't be found.\n");
        }
      }
      else {
        printToSender("Bad message format. usage: \\<id> <your message>.\n");
      }
    }

    private void printToSender(String message) {
      ClientAttachment sendingClient = brokers.get(senderId);
      if (sendingClient != null) {
        try {
          sendingClient.client.write(ByteBuffer.wrap(message.getBytes())).get();
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (ExecutionException e) {
          e.printStackTrace();
        }
      }
    }
  }

  class SendToBroker implements Runnable {
    private String msg;
    private String senderId;

    SendToBroker(String msg, String senderId) {
      this.msg = msg.trim();
      this.senderId = senderId;
    }

    @Override
    public void run() {
      Matcher m = p.matcher(msg);
      String brokerId;
      String extractedMsg;

      if (m.find()) {
        brokerId = m.group(1);
        extractedMsg = m.group(2) + "\n";

        ClientAttachment clientAttachment = brokers.get(brokerId);
        if (clientAttachment != null) {
          try {
            clientAttachment.client.write(ByteBuffer.wrap(extractedMsg.getBytes())).get();
          } catch (InterruptedException e) {
            System.out.println("Error sending to broker:");
            e.printStackTrace();
          } catch (ExecutionException e) {
            System.out.println("Error sending to broker:");
            e.printStackTrace();
          }
        }
        else {
          printToSender("Broker can't be found.\n");
        }
      }
      else {
        printToSender("Bad message format. usage: \\<id> <your message>.\n");
      }
    }

    private void printToSender(String message) {
      ClientAttachment sendingClient = markets.get(senderId);
      if (sendingClient != null) {
        try {
          sendingClient.client.write(ByteBuffer.wrap(message.getBytes())).get();
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (ExecutionException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void blocker() {
    try {
      blockerReader.readLine();
      // bruh... do you even block
      blocker();
    }
    catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }

}
