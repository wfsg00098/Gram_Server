import net.sf.json.JSONObject;

import java.sql.*;
import java.util.Vector;

class Database {
    private Statement state = null;


    Database() {
        try {
            System.out.println("注册驱动");
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("连接到数据库");
            Connection conn = DriverManager.getConnection(Settings.DB_URL, Settings.DB_USER, Settings.DB_PASS);
            state = conn.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("失败");
            System.exit(-100);
        }
        System.out.println("成功");
    }

    boolean IfUserExists(String username) {
        String sql;
        ResultSet rs;
        sql = "select * from chat.user where username='" + username + "'";
        try {
            rs = state.executeQuery(sql);
            rs.last();
            return rs.getRow() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    String GetUserNick(String username) {
        String sql;
        ResultSet rs;
        sql = "select nickname from chat.user where username='" + username + "'";
        try {
            rs = state.executeQuery(sql);
            rs.first();
            return rs.getString(1);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    String GetUserBio(String username) {
        String sql;
        ResultSet rs;
        sql = "select bio from chat.user where username='" + username + "'";
        try {
            rs = state.executeQuery(sql);
            rs.first();
            return rs.getString(1);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    String GetUserEmail(String username) {
        String sql;
        ResultSet rs;
        sql = "select email from chat.user where username='" + username + "'";
        try {
            rs = state.executeQuery(sql);
            rs.first();
            return rs.getString(1);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    String GetUserOnline(String username) {
        String sql;
        ResultSet rs;
        sql = "select Online from chat.user where username='" + username + "'";
        try {
            rs = state.executeQuery(sql);
            rs.first();
            return rs.getString(1);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    synchronized boolean SetUserPass(String username, String password) {
        String sql;
        ResultSet rs;
        sql = "update chat.user set password='" + password + "' where username = '" + username + "'";
        try {
            state.executeUpdate(sql);
            sql = "select * from chat.user where username='" + username + "' and password = '" + password + "'";
            rs = state.executeQuery(sql);
            rs.last();
            return rs.getRow() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    synchronized boolean SetUserNick(String username, String nick) {
        String sql;
        ResultSet rs;
        sql = "update chat.user set nickname='" + nick + "' where username = '" + username + "'";
        try {
            state.executeUpdate(sql);
            sql = "select * from chat.user where username='" + username + "' and nickname = '" + nick + "'";
            rs = state.executeQuery(sql);
            rs.last();
            return rs.getRow() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    synchronized boolean SetUserBio(String username, String bio) {
        String sql;
        ResultSet rs;
        sql = "update chat.user set bio='" + bio + "' where username = '" + username + "'";
        try {
            state.executeUpdate(sql);
            sql = "select * from chat.user where username='" + username + "' and bio = '" + bio + "'";
            rs = state.executeQuery(sql);
            rs.last();
            return rs.getRow() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    synchronized boolean SetUserEmail(String username, String email) {
        String sql;
        ResultSet rs;
        sql = "update chat.user set email='" + email + "' where username = '" + username + "'";
        try {
            state.executeUpdate(sql);
            sql = "select * from chat.user where username='" + username + "' and email = '" + email + "'";
            rs = state.executeQuery(sql);
            rs.last();
            return rs.getRow() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    synchronized boolean SetUserOnline(String username, String online) {
        String sql;
        ResultSet rs;
        sql = "update chat.user set online='" + online + "' where username = '" + username + "'";
        try {
            state.executeUpdate(sql);
            sql = "select * from chat.user where username='" + username + "' and online = '" + online + "'";
            rs = state.executeQuery(sql);
            rs.last();
            return rs.getRow() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    synchronized boolean CreateUser(String username, String password, String nickname, String email, String bio) {
        String sql;
        ResultSet rs;
        sql = "insert into chat.user values('" + username + "','" + password + "','" + nickname + "','" + email + "','offline','" + bio + "')";
        try {
            state.executeUpdate(sql);
            sql = "create table chat." + username + "_friend (username text, request text)";
            state.executeUpdate(sql);
            sql = "create table chat." + username + "_message (direction text, username text, type text, message text, time text)";
            state.executeUpdate(sql);
            sql = "create table chat." + username + "_unread (json text)";
            state.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    synchronized boolean Login(String username, String password) {
        String sql;
        ResultSet rs;
        sql = "select * from chat.user where username='" + username + "' and password = '" + password + "'";
        try {
            rs = state.executeQuery(sql);
            rs.last();
            if (rs.getRow() <= 0) return false;
            sql = "update chat.user set online='online' where username='" + username + "'";
            state.executeUpdate(sql);
            sql = "select * from chat.user where username='" + username + "' and online = 'online'";
            rs = state.executeQuery(sql);
            rs.last();
            return rs.getRow() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    synchronized boolean Logout(String username) {
        String sql;
        ResultSet rs;
        sql = "update chat.user set online='offline' where username='" + username + "'";
        try {
            state.executeUpdate(sql);
            sql = "select * from chat.user where username='" + username + "' and online = 'offline'";
            rs = state.executeQuery(sql);
            rs.last();
            return rs.getRow() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    synchronized void LogSendMessage(String username, String receiver, String type, String message, String time) {
        String sql;
        sql = "insert into chat." + username + "_message values('Send','" + receiver + "','" + type + "','" + message + "','" + time + "')";
        try {
            state.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    synchronized void LogReceiveMessage(String username, String receiver, String type, String message, String time) {
        String sql;
        sql = "insert into chat." + username + "_message values('Receive','" + receiver + "','" + type + "','" + message + "','" + time + "')";
        try {
            state.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Vector<String> GetUserUnread(String username) {
        String sql;
        ResultSet rs;
        Vector<String> result = new Vector<>();
        sql = "select * from chat." + username + "_unread";
        try {
            rs = state.executeQuery(sql);
            while (rs.next()) {
                result.add(rs.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    synchronized boolean AddUserUnread(String username, String message) {
        String sql;
        sql = "insert into chat." + username + "_unread values('" + message + "')";
        try {
            state.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    Vector<String> GetUserFriend(String username) {
        String sql;
        ResultSet rs;
        Vector<String> result = new Vector<>();
        sql = "select username from chat." + username + "_friend where request='friend'";
        try {
            rs = state.executeQuery(sql);
            while (rs.next()) {
                result.add(rs.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    synchronized boolean AddUserFriend(String username, String target) {
        String sql;
        ResultSet rs;
        sql = "insert into chat." + username + "_friend values('" + target + "','wait')";
        try {
            state.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        sql = "insert into chat." + target + "_friend values('" + username + "','request')";
        try {
            state.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    synchronized boolean AgreeAddFriendRequest(String username, String target) {
        String sql;
        ResultSet rs;
        sql = "update chat." + username + "_friend set request='friend' where username='" + target + "'";
        try {
            state.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        sql = "update chat." + target + "_friend set request='friend' where username='" + username + "'";
        try {
            state.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    synchronized boolean DeleteUserFriend(String username, String target) {
        String sql;
        ResultSet rs;
        sql = "delete from chat." + username + "_friend where username='" + target + "'";
        try {
            state.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        sql = "delete from chat." + target + "_friend where username='" + username + "'";
        try {
            state.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    JSONObject GetUserMessage(String username, String target, int count, boolean desc) {
        JSONObject js = new JSONObject();
        String des;
        if (desc) des = "order by time desc";
        else des = "";
        String limit;
        if (count > 0) limit = " limit 0," + count;
        else limit = "";
        String sql = "select * from chat." + username + "_message where username='" + target + "'" + des + limit;
        try {
            ResultSet rs = state.executeQuery(sql);
            int s = 0;
            while (rs.next()) {
                s++;
                js.put("direction"+s,rs.getString(1));
                js.put("type" + s, rs.getString(3));
                js.put("message" + s, rs.getString(4));
                js.put("time" + s, rs.getString(5));
            }
            js.put("count", String.valueOf(s));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return js;
    }

    synchronized void ClearUnread(String username) {
        String sql = "delete from chat." + username + "_unread";
        try {
            state.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
