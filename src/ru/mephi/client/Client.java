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

//    Send input to the server, print response and check it
    void sendMessage(String msg) throws IOException, ClassNotFoundException {
        out.println(msg);

        String serverMsg = in.readLine();
        System.out.println(serverMsg);

        if (serverMsg.contains("Error") || serverMsg.equals("Bye")) {
            isEnd = true;
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

        System.out.println("Hello! Please type your email, press enter, and after that, type your number and press enter again:");
        Scanner scanner = new Scanner(System.in);
        while (!client.isEnd()) {
            String inputLine = scanner.nextLine();
            client.sendMessage(inputLine);
        }
    }

//    The criteria of the end of the game
    private boolean isEnd() {
        return isEnd;
    }
}

