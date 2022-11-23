import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private static final int PORT = 8989;
    private static final String HOST = "localhost";

    public static void main(String[] args) { // клиентская часть


        try (Socket clientSocket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {


            String words = "  свойство ПРОДвижение свойство Однако  ";
            //String words = "бизнес";
            //String words = "такогоСловаНет";
            out.println(words);

            System.out.println("Ответ сервера: " + in.readLine());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
