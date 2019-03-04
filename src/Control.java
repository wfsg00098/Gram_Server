import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

public class Control {
    static Database db;
    static Map<String, Connect> Connections = new HashMap<>();
    static ServerSocket ss;
    static ServerSocket ss2;
    public static void main(String[] args) {
        System.out.println("服务端启动中...");
        db = new Database();
        try {
            System.out.println("监听端口" + Settings.LISTEN_PORT);
            ss = new ServerSocket(Settings.LISTEN_PORT);
            ss2 = new ServerSocket(Settings.LISTEN_PORT2);
            while (true) {
                new Connect(ss.accept()).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
