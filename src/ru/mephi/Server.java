package ru.mephi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {
    private ServerSocket serverSocket;
    private Queue<GameData> data = new ConcurrentLinkedQueue<>();
    private List<Session> sessions = new LinkedList<>();

//    Wait threads with clients
    void waitClients() {
        while (true) {
            try {
                for (Session session : sessions) {
                    session.join();
                }

                break;
            } catch (InterruptedException e) {}
        }
    }

//    Calculate the winner based on the client results
    void calculateResult() {
        float avg = 0;
        for (GameData data : data) {
            Float number = data.getDigit();
            avg += number;
        }

        avg = avg / data.size();

        float dist = Float.MAX_VALUE;
        GameData winner = null;
        for (GameData data: data) {
            float curDist = Math.abs(avg - data.getDigit());
            if (curDist < dist) {
                dist = curDist;
                winner = data;
            }
        }

        System.out.println("Winner is " + winner.getEmail());
    }

//    The server thread: accept new connections and logic for the collecting results
     void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(2000);
        System.out.println("Server was started");
        while (true) {
            Socket client = null;
            try {
                client = serverSocket.accept();
            } catch (SocketTimeoutException e) {
                if (data.size() > 5) {
                    waitClients();
                    calculateResult();
                    return;
                }
            } catch (IOException e) {
                System.out.println("Error: problems with client connection");
            }

            if (client != null) {
                Session session = new Session(client, data);
                sessions.add(session);

                session.start();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start(8888);
    }

    private static class Session extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private Queue<GameData> queueData;
        private GameData data = new GameData();

        Session(Socket socket, Queue<GameData> queueData) throws SocketException {
            this.clientSocket = socket;
            this.queueData = queueData;
            clientSocket.setSoTimeout(60000);
        }

        private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
                Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

//        Validate user email by regexp pattern
        private static boolean validate(String emailStr) {
            Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
            return matcher.find();
        }

//        Client thread: receive any input, validate it and save. Return response in case of success or error to client.
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                while (!clientSocket.isClosed()) {
                    String inputLine = in.readLine();
                    if (inputLine == null) {
                        continue;
                    }

                    System.out.printf("Input is %s\n", inputLine);
                    if (data.getEmail() == null) {
                        if (validate(inputLine)) {
                            data.setEmail(inputLine);
                            System.out.printf("Email %s is correct\n", inputLine);
                            out.println("OK");
                        } else {
                            out.println("Error: incorrect email");
                            break;
                        }
                    } else {
                        try {
                            Float number = Float.valueOf(inputLine);
                            if (number < 0 || number > 100) {
                                out.println("Error: The number is lower than 0 or higher than 100");
                                break;
                            }

                            System.out.printf("Number %f is correct\n", number);
                            data.setDigit(number);
                        } catch (NumberFormatException e) {
                            out.println("Error: It's not a float number");
                            break;
                        }

                        queueData.add(data);
                        out.println("Bye");
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                    out.close();
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

