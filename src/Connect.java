import net.sf.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;


class Connect extends Thread {
    private Socket socket;
    private Socket socket2;
    private String username = null;
    private BufferedReader br = null;
    private PrintWriter pw = null;
    private BufferedReader br2 = null;
    private PrintWriter pw2 = null;
    private boolean EXIT = false;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    private class FileReceive extends Thread {
        String fileName;
        String receiver;
        FileReceive(String fileName, String recv) {
            this.fileName = fileName;
            receiver = recv;
        }
        @Override
        public void run() {
            try {
                ServerSocket ss = new ServerSocket(Settings.FILE_RECEIVE_PORT);
                JSONObject json = new JSONObject();
                json.put("type", "ReadyToReceive");
                SendJSON2(json);
                Socket FileRecv = ss.accept();
                ss.close();
                DataInputStream dis = new DataInputStream(new BufferedInputStream(FileRecv.getInputStream()));
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
                byte[] buffer = new byte[Settings.BUFFER_SIZE];
                while (true) {
                    int read;
                    read = dis.read(buffer);
                    if (read == -1) {
                        break;
                    }
                    dos.write(buffer, 0, read);
                    dos.flush();
                }
                dis.close();
                dos.close();
                FileRecv.close();
                if (!Control.db.GetUserOnline(receiver).equals("offline")) {
                    Control.Connections.get(receiver).StartFileSend(fileName, username);
                } else {
                    JSONObject js = new JSONObject();
                    js.put("type", "FileRequest");
                    js.put("filename", fileName);
                    js.put("from", username);
                    js.put("length", String.valueOf(new File(fileName).length()));
                    Control.db.AddUserUnread(receiver, js.toString());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class FileSend extends Thread {
        String fileName;
        FileSend(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public void run() {
            try {
                ServerSocket ss = new ServerSocket(Settings.FILE_SEND_PORT);
                Socket FileSend = ss.accept();
                ss.close();
                DataInputStream dis = new DataInputStream(new FileInputStream(fileName));
                DataOutputStream dos = new DataOutputStream(FileSend.getOutputStream());
                byte[] buffer = new byte[Settings.BUFFER_SIZE];
                while (true) {
                    int read;
                    read = dis.read(buffer);
                    if (read == -1) {
                        break;
                    }
                    dos.write(buffer, 0, read);
                    dos.flush();
                }
                dis.close();
                dos.close();
                FileSend.close();
                new File(fileName).delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    Connect(Socket socket) {
        this.socket = socket;
        try {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            System.out.println("Socket1 建立成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            socket2 = Control.ss2.accept();
            br2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
            pw2 = new PrintWriter(new OutputStreamWriter(socket2.getOutputStream()));
            System.out.println("Socket2 建立成功");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        while (!EXIT) {
            try {
                Judge(JSONObject.fromObject(br.readLine()));
            } catch (java.net.SocketTimeoutException e) {
                if (EXIT) {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        try {
            Control.db.SetUserOnline(username, "offline");
            br.close();
            pw.close();
            socket.close();
            br2.close();
            pw2.close();
            socket2.close();
            Control.Connections.remove(username);
            System.out.println(username + "断开连接");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void Judge(JSONObject json) {
        try {
            if (json.isNullObject()) {
                EXIT = true;
                return;
            }
            String type = json.getString("type");
            switch (type) {
                case "TextMessage":
                    TextMessage(json);
                    break;
                case "FileMessage":
                    FileMessage(json);
                    break;
                case "FileRequest":
                    FileRequest(json);
                    break;
                case "ShakeMessage":
                    ShakeMessage(json);
                    break;
                case "CreateGroup":
                    CreateGroup(json);
                    break;
                case "JoinGroup":
                    JoinGroup(json);
                    break;
                case "LeaveGroup":
                    LeaveGroup(json);
                    break;
                case "GroupTextMessage":
                    GroupTextMessage(json);
                    break;
                case "GetGroupNickname":
                    GetGroupNickname(json);
                    break;
                case "Login":
                    Login(json);
                    break;
                case "GetGroup":
                    GetGroup();
                    break;
                case "GetMessage":
                    GetMessage(json);
                    break;
                case "GetFriend":
                    GetFriend();
                    break;
                case "AddFriend":
                    AddFriend(json);
                    break;
                case "AgreeAdd":
                    AgreeAdd(json);
                    break;
                case "RejectAdd":
                    RejectAdd(json);
                    break;
                case "DelFriend":
                    DelFriend(json);
                    break;
                case "Logout":
                    Logout();
                    break;
                case "GetInfo":
                    GetInfo(json);
                    break;
                case "SetBio":
                    SetBio(json);
                    break;
                case "SetNick":
                    SetNick(json);
                    break;
                case "SetPass":
                    SetPass(json);
                    break;
                case "SetEmail":
                    SetEmail(json);
                    break;
                case "Register":
                    Register(json);
                    break;
                default:
                    System.out.println("接收到未知消息类型 ---- " + type);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            EXIT = true;
        }
    }

    private synchronized void SendJSON(JSONObject json) {  //事先不知道的
        pw.write(json.toString() + "\n");
        pw.flush();
    }

    private synchronized void SendJSON2(JSONObject json) {  //事先知道的
        pw2.write(json.toString() + "\n");
        pw2.flush();
    }

    private synchronized void Register(JSONObject json) {
        String username = json.getString("username");
        String password = json.getString("password");
        String nickname = json.getString("nickname");
        String email = json.getString("email");
        String bio = json.getString("bio");
        JSONObject js = new JSONObject();
        js.put("type", "Register_info");
        js.put("status", "failed");
        if (!Control.db.IfUserExists(username)) {
            if (Control.db.CreateUser(username, password, nickname, email, bio)) {
                js.put("status", "success");
            }
        }
        SendJSON2(js);
    }

    private void Login(JSONObject json) {
        String username = json.getString("username");
        String password = json.getString("password");
        JSONObject result = new JSONObject();
        result.put("type", "Login");
        if (Control.db.Login(username, password)) {
            if (Control.Connections.get(username) != null) {
                result.put("status", "duplicated");
                SendJSON2(result);
                System.out.println(username + "禁止重复登录");
            } else {
                this.username = username;
                result.put("status", "success");
                SendJSON2(result);
                Control.Connections.put(username, this);
                System.out.println(username + "成功登录");
                Vector<String> unread = Control.db.GetUserUnread(username);
                for (String s : unread) {
                    JSONObject js = JSONObject.fromObject(s);
                    SendJSON(js);
                }
                Control.db.ClearUnread(username);
            }
        } else {
            result.put("status", "failed");
            SendJSON2(result);
            System.out.println(username + "失败登录");
        }
    }

    private void Logout() {
        if (Control.db.Logout(username)) {
            Control.Connections.remove(username);
            JSONObject js = new JSONObject();
            js.put("type", "Logout");
            SendJSON2(js);
            EXIT = true;
        }
    }

    private void TextMessage(JSONObject json) {
        String receiver = json.getString("receiver");
        String message = json.getString("message");
        String time = dateFormat.format(new Date());
        JSONObject js = new JSONObject();
        js.put("type", "TextMessage");
        js.put("from", username);
        js.put("message", message);
        js.put("Time", time);
        Control.db.LogSendMessage(username, receiver, "TextMessage", message, time);
        Control.db.LogReceiveMessage(receiver, username, "TextMessage", message, time);
        String status = Control.db.GetUserOnline(receiver);
        if (!status.equals("offline")) {
            Control.Connections.get(receiver).SendJSON(js);
        } else {
            Control.db.AddUserUnread(receiver, js.toString());
        }
    }

    private void FileMessage(JSONObject json) {
        String receiver = json.getString("receiver");
        String filename = json.getString("filename");
        String time = dateFormat.format(new Date());
        new FileReceive(filename, receiver).start();
        Control.db.LogSendMessage(username, receiver, "FileMessage", filename, time);
        Control.db.LogReceiveMessage(receiver, username, "FileMessage", filename, time);
    }


    private void StartFileSend(String fileName, String from) {
        JSONObject js = new JSONObject();
        js.put("type", "FileRequest");
        js.put("filename", fileName);
        js.put("length", String.valueOf(new File(fileName).length()));
        js.put("from", from);
        SendJSON(js);
    }

    private void FileRequest(JSONObject json) {
        String filename = json.getString("filename");
        String status = json.getString("status");
        if (status.equals("accepted")) {
            new FileSend(filename).start();
        } else {
            new File(filename).delete();
        }
    }


    private void ShakeMessage(JSONObject json) {
        String receiver = json.getString("receiver");
        String time = dateFormat.format(new Date());
        JSONObject js = new JSONObject();
        js.put("type", "ShakeMessage");
        js.put("from", username);
        js.put("Time", time);
        Control.db.LogSendMessage(username, receiver, "ShakeMessage", "", time);
        Control.db.LogReceiveMessage(receiver, username, "ShakeMessage", "", time);
        String status = Control.db.GetUserOnline(receiver);
        if (!status.equals("offline")) {
            Control.Connections.get(receiver).SendJSON(js);
        } else {
            Control.db.AddUserUnread(receiver, js.toString());
        }
    }


    private void SetNick(JSONObject json) {
        String nickname = json.getString("nickname");
        Control.db.SetUserNick(username, nickname);
    }

    private void SetPass(JSONObject json) {
        String password = json.getString("password");
        Control.db.SetUserPass(username, password);
    }

    private void SetEmail(JSONObject json) {
        String email = json.getString("email");
        Control.db.SetUserEmail(username, email);
    }

    private void SetBio(JSONObject json) {
        String bio = json.getString("Bio");
        Control.db.SetUserBio(username, bio);
    }

    private void GetInfo(JSONObject json) {
        String user = json.getString("username");
        JSONObject result = new JSONObject();
        result.put("type", "GetInfo");
        result.put("nickname", Control.db.GetUserNick(user));
        result.put("email", Control.db.GetUserEmail(user));
        result.put("online", Control.db.GetUserOnline(user));
        result.put("bio", Control.db.GetUserBio(user));
        SendJSON2(result);
    }

    private void GetFriend() {
        JSONObject result = new JSONObject();
        Vector<String> s = Control.db.GetUserFriend(username);
        result.put("count", s.size());
        for (int i = 0; i < s.size(); i++) {
            result.put("username" + i, s.get(i));
            result.put("nickname" + i, Control.db.GetUserNick(s.get(i)));
        }
        SendJSON2(result);
    }

    private void AddFriend(JSONObject json) {
        String target = json.getString("target");
        if (Control.db.IfUserExists(target)) {
            Control.db.AddUserFriend(username, target);
            JSONObject result = new JSONObject();
            result.put("type", "AddWait");
            result.put("reason", "Waiting");
            SendJSON2(result);
            JSONObject js = new JSONObject();
            js.put("type", "AddRequest");
            js.put("from", username);
            String status = Control.db.GetUserOnline(target);
            if (!status.equals("offline")) {
                Control.Connections.get(target).SendJSON(js);
            } else {
                Control.db.AddUserUnread(target, js.toString());
            }
        } else {
            JSONObject result = new JSONObject();
            result.put("type", "AddFail");
            result.put("reason", "UserNotExists");
            SendJSON2(result);
        }
    }

    private void AgreeAdd(JSONObject json) {
        String target = json.getString("target");
        Control.db.AgreeAddFriendRequest(username, target);
        String status = Control.db.GetUserOnline(target);
        JSONObject js = new JSONObject();
        js.put("type", "AddAgreed");
        js.put("from", username);
        if (!status.equals("offline")) {
            Control.Connections.get(target).SendJSON(js);
        } else {
            Control.db.AddUserUnread(target, js.toString());
        }
    }

    private void RejectAdd(JSONObject json) {
        String target = json.getString("target");
        Control.db.DeleteUserFriend(username, target);
        String status = Control.db.GetUserOnline(target);
        JSONObject js = new JSONObject();
        js.put("type", "AddRejected");
        js.put("from", username);
        if (!status.equals("offline")) {
            Control.Connections.get(target).SendJSON(js);
        } else {
            Control.db.AddUserUnread(target, js.toString());
        }
    }

    private void DelFriend(JSONObject json) {
        String target = json.getString("target");
        Control.db.DeleteUserFriend(username, target);
        String status = Control.db.GetUserOnline(target);
        JSONObject js = new JSONObject();
        js.put("type", "WasDeleted");
        js.put("from", username);
        if (!status.equals("offline")) {
            Control.Connections.get(target).SendJSON(js);
        } else {
            Control.db.AddUserUnread(target, js.toString());
        }
    }

    private void GetMessage(JSONObject json) {
        String target = json.getString("username");
        int count = Integer.parseInt(json.getString("count"));
        boolean desc;
        String order = json.getString("order");
        desc = order.equals("desc");
        JSONObject js = Control.db.GetUserMessage(username, target, count, desc);
        js.put("type","GetMessage");
        SendJSON2(js);
    }

    private void CreateGroup(JSONObject json) {
        String groupname = json.getString("groupname");
        JSONObject js = new JSONObject();
        js.put("type", "CreateGroup");
        if (!Control.db.IfGroupExists(groupname)) {
            String groupnickname = json.getString("groupnickname");
            if (Control.db.CreateGroup(groupname, groupnickname)) {
                js.put("status", "success");
            } else {
                js.put("status", "failed");
            }
        } else {
            js.put("status", "duplicated");
        }
        SendJSON2(js);
    }

    private void JoinGroup(JSONObject json) {
        String groupname = json.getString("groupname");
        JSONObject js = new JSONObject();
        js.put("type", "JoinGroup");
        if (Control.db.IfGroupExists(groupname)) {
            if (Control.db.JoinGroup(username, groupname)) {
                js.put("status", "success");
            } else {
                js.put("status", "failed");
            }
        } else {
            js.put("status", "GroupNotExists");
        }
        SendJSON2(js);
    }

    private void LeaveGroup(JSONObject json) {
        String groupname = json.getString("groupname");
        JSONObject js = new JSONObject();
        js.put("type", "LeaveGroup");
        if (Control.db.LeaveGourp(username, groupname)) {
            js.put("status", "success");
        } else {
            js.put("status", "failed");
        }
        SendJSON2(js);
    }

    private void GroupTextMessage(JSONObject json) {
        String groupname = json.getString("fromGroup");
        String message = json.getString("message");
        String time = dateFormat.format(new Date());
        Vector<String> users = Control.db.GetGroupUser(groupname);
        JSONObject js = new JSONObject();
        js.put("type", "GroupTextMessage");
        js.put("from", username);
        js.put("fromGroup", groupname);
        js.put("Time", time);
        js.put("message", message);
        for (String s : users) {
            if (s.equals(username)) continue;
            String status = Control.db.GetUserOnline(s);
            if (!status.equals("offline")) {
                Control.Connections.get(s).SendJSON(js);
            } else {
                Control.db.AddUserUnread(s, js.toString());
            }
        }
    }

    private void GetGroupNickname(JSONObject json) {
        String groupname = json.getString("groupname");
        JSONObject js = new JSONObject();
        js.put("type", "GetGroupNickname");
        String groupnickname = Control.db.GetGroupNickname(groupname);
        js.put("GroupNickname", groupnickname);
        SendJSON2(js);
    }

    private void GetGroup() {
        Vector<String> v = Control.db.GetUserGroup(username);
        JSONObject result = new JSONObject();
        int count = v.size();
        result.put("count", String.valueOf(count));
        for (int i = 0; i < count; i++) {
            result.put("groupname" + i, v.get(i));
        }
        SendJSON2(result);
    }
}
