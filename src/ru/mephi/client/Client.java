package ru.mephi.client;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isEnd = false;

//    Get connection to the server, create buffered streams for convenient communication
    void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    // get the status of the game
    String getGameStatus() throws IOException {
        return in.readLine();
    }

//    Send input to the server, print response and check it
    void sendMessage(String msg) throws IOException, GameError {
        out.println(msg);

        String serverMsg = in.readLine();
        if (serverMsg.contains("Error")) {
            throw new GameError(serverMsg);
        }
    }

//    The main game loop: receive input, send it to the server
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Client client = new Client();
        try {
            client.startConnection("127.0.0.1", 8888);
        } catch (IOException e) {
            System.out.println("Error: Can't connect to the server");
            return;
        }

        System.out.println("Hello! Please type your email:");
        Scanner scanner = new Scanner(System.in);

        try {
            String email = scanner.nextLine();
            client.sendMessage(email);

            System.out.println("Please type your number:");
            String number = scanner.nextLine();
            client.sendMessage(number);

            System.out.println(client.getGameStatus());
        } catch (GameError e) {
            System.out.println(e.getMessage());
        } catch (IOException e ) {
            System.out.println("Error: server is down");
        }
    }
}

