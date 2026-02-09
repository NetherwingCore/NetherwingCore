package br.net.dd.netherwingcore.bnetserver.server;

import javax.net.ssl.SSLSocket;
import java.io.*;

import static br.net.dd.netherwingcore.common.logging.Log.log;

public class ClientHandler implements Runnable {

    private final SSLSocket clientSocket;

    public ClientHandler(SSLSocket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            log("Client connected: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

            // Exemplo de loop de comunicação
            while (!clientSocket.isClosed()) {
                try {
                    // Lê o tamanho da mensagem (exemplo: protocolo com prefixo de tamanho)
                    int length = in.readInt();
                    System.out.println("Received message length: " + length);
                    byte[] data = new byte[length];
                    in.readFully(data);

                    log("Received " + length + " bytes from client.");

                    // Aqui você processa a mensagem conforme o protocolo Battle.net
                    // Por enquanto, vamos apenas ecoar de volta
                    out.writeInt(length);
                    out.write(data);
                    out.flush();

                } catch (EOFException e) {
                    log("Client disconnected gracefully.");
                    break;
                }
            }

        } catch (IOException e) {
            log("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                log("Connection closed with client.");
            } catch (IOException e) {
                log("Error closing client socket: " + e.getMessage());
            }
        }
    }
}
