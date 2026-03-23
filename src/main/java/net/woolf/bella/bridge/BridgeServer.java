package net.woolf.bella.bridge;

import net.woolf.bella.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class BridgeServer {

  private final Main plugin;
  private final Logger logger;
  private ServerSocket serverSocket;
  private final ExecutorService executor = Executors.newCachedThreadPool();
  private volatile boolean running = false;

  public BridgeServer(
      Main plugin
  ) {
    this.plugin = plugin;
    this.logger = plugin.getLogger();
  }

  public void start(
      int port
  ) {
    if ( !plugin.configManager.config.getBoolean( "bridge.enabled", true ) )
      return;

    try {
      serverSocket = new ServerSocket( port, 50, InetAddress.getLoopbackAddress() );
      running = true;
      executor.submit( this::acceptLoop );
      logger.info( "Bridge server started on port " + port );
    } catch ( IOException e ) {
      logger.severe( "Failed to start bridge server on port " + port + ": " + e.getMessage() );
    }
  }

  public void stop() {
    running = false;
    try {
      if ( serverSocket != null )
        serverSocket.close();
    } catch ( IOException ignored ) {
    }
    executor.shutdownNow();
  }

  private void acceptLoop() {
    while ( running ) {
      try {
        Socket client = serverSocket.accept();
        executor.submit( () -> handleClient( client ) );
      } catch ( IOException e ) {
        if ( running )
          logger.warning( "Bridge accept error: " + e.getMessage() );
      }
    }
  }

  private void handleClient(
      Socket socket
  ) {
    try (socket;
        BufferedReader in = new BufferedReader(
            new InputStreamReader( socket.getInputStream() ) );
        PrintWriter out = new PrintWriter(
            new OutputStreamWriter( socket.getOutputStream() ), true )) {

      BridgeHandler handler = new BridgeHandler( plugin );
      String line;
      while ( ( line = in.readLine() ) != null ) {
        String response = handler.handle( line.trim() );
        out.println( response );
      }
    } catch ( IOException ignored ) {
    }
  }
}
