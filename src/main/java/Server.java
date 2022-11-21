import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Server {
    static Gson gson = new Gson();
    private int port;
    private BooleanSearchEngine engine;

    public Server(int port, BooleanSearchEngine engine) {
        this.port = port;
        this.engine = engine;
    }

    public void start() {

        try (ServerSocket serverSocket = new ServerSocket(port);) {
            System.out.println("Starting server at " + port + "...");
            while (true) {
                try (
                        Socket socket = serverSocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter out = new PrintWriter(socket.getOutputStream());
                ) {
                    // обработка одного подключения
                    String word = in.readLine(); //принимаем строку со словом от клиента
                    List<PageEntry> list = engine.search(word);

                    String reply = list.isEmpty() ? String.valueOf(list) : gson.toJson(list);
                    out.println(reply);

                }
            }
        } catch (IOException e) {
            System.out.println("Не могу стартовать сервер");
            e.printStackTrace();
        }
    }
}
