package cz.vut.feec.lazarov.smartgrid;

import javax.net.ssl.*;
import java.io.*;
import java.net.SocketException;
import java.security.KeyStore;

public class SecureChannel {

    private static final int delay = 1000; //in millis
    private static final String[] protocols = new String[]{"TLSv1.3"};
    private static final String[] cipher_suites = new String[]{"TLS_AES_128_GCM_SHA256"};

    public static void main(String[] args) throws Exception {
        try (EchoServer server = new EchoServer(666, "Server1")) {
            new Thread(server).start();
            String message = "Like most of life's problems, this one can be solved with bending!";

            EchoClient client = new EchoClient("localhost", 666, "Client1");

            String data = (String) client.sendAndReceiveData("message1");
            System.out.printf("Client received: %s\n", data);
            data = (String) client.sendAndReceiveData("message2");
            System.out.printf("Client received: %s\n", data);

            Thread.sleep(delay);

            client.close();
            server.close();
        }
    }

    public static class EchoClient implements AutoCloseable {
        private final SSLSocket socket;
        private final ObjectOutputStream outputStream;
        private final ObjectInputStream inputStream;
        private final String name;

        public EchoClient(String host, int port, String name) throws Exception {
            this.socket = createSocket(host, port);
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            this.inputStream = new ObjectInputStream(socket.getInputStream());
            this.name = name;
        }

        public Object sendAndReceiveData(Object data) throws Exception {
            System.out.printf("[%s] Client sending data\n", name);
            outputStream.writeObject(data); // Send data to server

            data = inputStream.readObject(); // Receive data from server
            System.out.printf("[%s] Client received data\n", name);

            if (data == null) {
                System.out.printf("[%s] Client not received data\n", name);
                throw new IOException("no data received");
            }

            return data;
        }

        public static SSLSocket createSocket(String host, int port) throws Exception {
            SSLSocket socket = (SSLSocket) createSSLContext().getSocketFactory().createSocket(host, port);
            socket.setEnabledProtocols(protocols);
            socket.setEnabledCipherSuites(cipher_suites);

            return socket;
        }

        @Override
        public void close() throws Exception {
            outputStream.close();
            inputStream.close();
            socket.close();
        }
    }

    public static class EchoServer implements Runnable, AutoCloseable {
        private final SSLServerSocket sslServerSocket;
        private ObjectOutputStream outputStream;
        private ObjectInputStream inputStream;
        private final String name;
        private volatile boolean serverRun = true;

        public EchoServer(int port, String name) throws Exception {
            this.sslServerSocket = createServerSocket(port);
            this.name = name;
        }

        public int port() {
            return sslServerSocket.getLocalPort();
        }

        @Override
        public void close() {
            try {
                serverRun = false;

                if (sslServerSocket != null && !sslServerSocket.isClosed()) {
                    sslServerSocket.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }

                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            System.out.printf("[%s] Server started on port %d%n", name, port());

            while (serverRun) {
                try {
                    SSLSocket socket = (SSLSocket) sslServerSocket.accept();
                    System.out.printf("[%s] Client connected to server\n", name);
                    inputStream = new ObjectInputStream(socket.getInputStream());
                    outputStream = new ObjectOutputStream(socket.getOutputStream());

                    Object dataReceived;

                    //Receive data from client
                    while ((dataReceived = inputStream.readObject()) != null) {
                        System.out.printf("[%s] Server received data\n", name);

                        Object dataToSend = createResponse(dataReceived); //Create response

                        System.out.printf("[%s] Server sending data to client\n", name);
                        outputStream.writeObject(dataToSend); //Send data to client
                    }
                } catch (EOFException | SocketException e1) {
                    System.out.printf("[%s] Client disconnected from server\n", name);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.printf("[%s] Server exception: %s\n", name, e.getMessage());
                }
            }

            System.out.printf("[%s] Server stopped\n", name);
        }

        protected Object createResponse(Object dataReceived) {
            return dataReceived;
        }

        private static SSLServerSocket createServerSocket(int port) throws Exception {
            SSLServerSocket socket = (SSLServerSocket) createSSLContext().getServerSocketFactory().createServerSocket(port);
            socket.setEnabledProtocols(protocols);
            socket.setEnabledCipherSuites(cipher_suites);

            return socket;
        }
    }

    public static SSLContext createSSLContext() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("keystore"), "passphrase".toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, "passphrase".toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return sslContext;
    }
}