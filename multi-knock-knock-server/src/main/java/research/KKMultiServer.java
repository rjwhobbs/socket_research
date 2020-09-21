package research;

// find source code here:
// https://docs.oracle.com/javase/tutorial/networking/sockets/examples/KKMultiServer.java
// https://docs.oracle.com/javase/tutorial/networking/sockets/examples/KKMultiServerThread.java
// https://docs.oracle.com/javase/tutorial/networking/sockets/examples/KnockKnockProtocol.java

import java.net.*;
import java.io.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class KnockKnockProtocol {
  private static final int WAITING = 0;
  private static final int SENTKNOCKKNOCK = 1;
  private static final int SENTCLUE = 2;
  private static final int ANOTHER = 3;

  private static final int NUMJOKES = 5;

  private int state = WAITING;
  private int currentJoke = 0;

  private String[] clues = { "Turnip", "Little Old Lady", "Atch", "Who", "Who" };
  private String[] answers = { "Turnip the heat, it's cold in here!",
          "I didn't know you could yodel!",
          "Bless you!",
          "Is there an owl in here?",
          "Is there an echo in here?" };

  public String processInput(String theInput) {
    String theOutput = null;

    if (state == WAITING) {
      theOutput = "Knock! Knock!";
      state = SENTKNOCKKNOCK;
    } else if (state == SENTKNOCKKNOCK) {
      if (theInput.equalsIgnoreCase("Who's there?")) {
        theOutput = clues[currentJoke];
        state = SENTCLUE;
      } else {
        theOutput = "You're supposed to say \"Who's there?\"! " +
                "Try again. Knock! Knock!";
      }
    } else if (state == SENTCLUE) {
      if (theInput.equalsIgnoreCase(clues[currentJoke] + " who?")) {
        theOutput = answers[currentJoke] + " Want another? (y/n)";
        state = ANOTHER;
      } else {
        theOutput = "You're supposed to say \"" +
                clues[currentJoke] +
                " who?\"" +
                "! Try again. Knock! Knock!";
        state = SENTKNOCKKNOCK;
      }
    } else if (state == ANOTHER) {
      if (theInput.equalsIgnoreCase("y")) {
        theOutput = "Knock! Knock!";
        if (currentJoke == (NUMJOKES - 1))
          currentJoke = 0;
        else
          currentJoke++;
        state = SENTKNOCKKNOCK;
      } else {
        theOutput = "Bye.";
        state = WAITING;
      }
    }
    return theOutput;
  }
}

//class KKMultiServerThread extends Thread {
//  private Socket socket = null;
//
//  public KKMultiServerThread(Socket socket) {
////    super("KKMultiServerThread");
//    this.socket = socket;
//  }
//
//  public void run() {
//
//    try (
//            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//            BufferedReader in = new BufferedReader(
//                    new InputStreamReader(
//                            socket.getInputStream()));
//    ) {
//      String inputLine, outputLine;
//      KnockKnockProtocol kkp = new KnockKnockProtocol();
//      outputLine = kkp.processInput(null);
//      out.println(outputLine);
//
//      while ((inputLine = in.readLine()) != null) {
//        System.out.println("This client is running on: " + Thread.currentThread().getName());
//        outputLine = kkp.processInput(inputLine);
//        out.println(outputLine);
//        if (outputLine.equals("Bye"))
//          break;
//      }
//      socket.close();
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//  }
//}

public class KKMultiServer {
  public static void main(String[] args) throws IOException {

    if (args.length != 1) {
      System.err.println("Usage: java KKMultiServer <port number>");
      System.exit(1);
    }

    int portNumber = Integer.parseInt(args[0]);
    boolean listening = true;
    Executor pool = Executors.newFixedThreadPool(2);

    try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
      while (listening) {
        final Socket socket = serverSocket.accept();
        Runnable runnable = new Runnable() {
          @Override
          public void run() {
            try (
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()));
            ) {
              String inputLine, outputLine;
              KnockKnockProtocol kkp = new KnockKnockProtocol();
              outputLine = kkp.processInput(null);
              out.println(outputLine);

              while ((inputLine = in.readLine()) != null) {
                System.out.println("This client is running on: " + Thread.currentThread().getName());
                outputLine = kkp.processInput(inputLine);
                out.println(outputLine);
                if (outputLine.equals("Bye"))
                  break;
              }
              socket.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        };
        pool.execute(runnable);
        // Old multithreaded code.
//        new KKMultiServerThread(serverSocket.accept()).start();
      }
    } catch (IOException e) {
      System.err.println("Could not listen on port " + portNumber);
      System.exit(-1);
    }
  }
}