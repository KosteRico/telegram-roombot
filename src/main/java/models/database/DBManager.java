package models.database;

import constants.StateId;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import utills.UpdateUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class DBManager {

    private static volatile DBManager instance;

    private volatile Connection connection;

    private DBManager() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC driver wasn't found!");
            return;
        }

        try {
            connection = getConnection();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static Connection getConnection() throws SQLException {
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        return DriverManager.getConnection(dbUrl);
    }

    public static DBManager getInstance() {
        final DBManager currentInstance;
        if (instance == null) {
            synchronized (DBManager.class) {
                if (instance == null) {
                    instance = new DBManager();
                }
                currentInstance = instance;
            }
        } else {
            currentInstance = instance;
        }
        return currentInstance;
    }

    public void swapDates(Update nowUpdate) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "select swap_dates(?)"
        );
        statement.setLong(1, UpdateUtils.getChatId(nowUpdate));
        statement.executeUpdate();
    }

    public synchronized ResultSet getSortedSchedule() {
        try {
            return connection.createStatement().executeQuery(
                    "select get_sorted_schedule()");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public synchronized void updatePersonChatId(Update update) {
        try {
            String[] strings = update.getCallbackQuery().getData().split("-");

            long id = UpdateUtils.getChatId(update);

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "update persons set chatid = ? where chatid is null and firstname = ? and lastname = ?"
            )) {

                preparedStatement.setLong(1, id);
                preparedStatement.setString(2, strings[0]);
                preparedStatement.setString(3, strings[1]);

                preparedStatement.executeUpdate();
            }
        } catch (SQLException ignored) {
        }
    }

    public synchronized int getStateId(Update update) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "select chatid, stateid from persons where chatid = ?"
        );
        statement.setLong(1, UpdateUtils.getChatId(update));
        ResultSet set = statement.executeQuery();
        set.next();
        int res = set.getInt("stateId");
        statement.close();
        set.close();
        return res;
    }

    public String getFirstName(Update update) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "select firstname from persons where chatid = ?"
        );
        statement.setLong(1, UpdateUtils.getChatId(update));
        ResultSet set = statement.executeQuery();
        set.next();

        String res = set.getString("firstname");

        statement.close();
        set.close();

        return res;
    }

    public synchronized void setStateId(Update update, StateId stateId) throws SQLException {
        setStateId(UpdateUtils.getChatId(update), stateId);
    }

    public synchronized void setStateId(long chatId, StateId stateId) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "update persons set stateid = ? where chatid = ?"
        )) {

            preparedStatement.setInt(1, stateId.id());
            preparedStatement.setLong(2, chatId);

            preparedStatement.executeUpdate();
        }
    }

    public synchronized boolean hasMenChatIdInTable(Update update) throws SQLException {
        ResultSet tablePersons = getTable();

        while (tablePersons.next()) {
            long chatId = tablePersons.getLong("chatid");
            if (chatId != 0 && UpdateUtils.getChatId(update) == chatId) {
                return true;
            }
        }
        tablePersons.close();
        return false;
    }

    public List<Long> getChatIdsExceptFor(Update update) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "select firstname, chatid from persons where not chatid = ?"
        );

        statement.setLong(1, UpdateUtils.getChatId(update));
        ResultSet set = statement.executeQuery();

        List<Long> res = new LinkedList<>();

        while (set.next()) {
            res.add(set.getLong("chatid"));
        }

        statement.close();
        set.close();

        return res;
    }

    public synchronized void incrementTopPersonSchedule() {
        try {
            connection.createStatement().execute("select increment_top_person_schedule()");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public synchronized void postpone() {
        try {
            Statement statement = connection.createStatement();

            statement.execute("select postpone_schedule()");

            statement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public synchronized ResultSet getTable() {
        try {
            return connection.createStatement().executeQuery(
                    "select * from persons"
            );
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public synchronized ResultSet getRow(long chatId) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "select * from persons where chatid = ?;"
            );

            statement.setLong(1, chatId);
            return statement.executeQuery();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private ResultSet getTop() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet set = statement.executeQuery("select * from get_top_of_sorted_schedule()");
        //statement.close();
        return set;
    }

    public Date getTopDate() throws SQLException {
        ResultSet closest = getTop();
        closest.next();
        Date date = closest.getDate("date");
        closest.close();
        return date;
    }

    public boolean hasTrashOutMan() throws SQLException {
        ResultSet set = connection.createStatement().executeQuery("select has_trashout_man()");
        set.next();
        boolean ans = set.getBoolean(1);
        set.close();
        return ans;
    }

    public long getClosestChatId() throws SQLException {
        ResultSet set = getTop();
        set.next();

        long res = set.getLong("chatid");
        set.close();

        return res;
    }

    public boolean isTrashOutManTop() throws SQLException {
        ResultSet set = getTop();

        set.next();

        boolean res = set.getBoolean("trash_out");
        set.close();

        return res;
    }

    public boolean isTrashOutMan(long id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "select is_trash_out, chatid" +
                        " from get_sorted_schedule()" +
                        " where chatid = ?;"
        );

        statement.setLong(1, id);

        ResultSet set = statement.executeQuery();
        set.next();

        boolean res = set.getBoolean("is_trash_out");

        statement.close();
        set.close();

        return res;
    }

    public boolean isTrashOutMan(Update update) throws SQLException {
        return isTrashOutMan(UpdateUtils.getChatId(update));
    }

    public synchronized void setTrashOutMan(boolean b) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "update trash_schedule set trash_out = ? " +
                        "where trash_schedule.date = (select min(date) from trash_schedule);"
        );

        statement.setBoolean(1, b);

        statement.executeUpdate();
        statement.close();
    }

    public Date getDateByUpdate(Update update) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "select chatid, date" +
                        " from " +
                        "get_sorted_schedule()" +
                        " where chatid = ?;"
        );

        statement.setLong(1, UpdateUtils.getChatId(update));

        ResultSet set = statement.executeQuery();
        set.next();

        Date date = set.getDate("date");
        set.close();
        statement.close();

        return date;
    }

    public void addMessageForDeletion(Message message) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "insert into messages_for_deletion (chat_id, message_id) " +
                        "values (?, ?)"
        );

        statement.setLong(1, message.getChatId());
        statement.setInt(2, message.getMessageId());

        statement.executeUpdate();

        statement.close();
    }

    public List<DeleteMessage> getDeleteMessages() throws SQLException {

        ResultSet set = connection.createStatement().executeQuery(
                "select * from messages_for_deletion"
        );

        List<DeleteMessage> deleteMessages = new LinkedList<>();

        while (set.next()) {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(set.getLong("chat_id"));
            deleteMessage.setMessageId(set.getInt("message_id"));
            deleteMessages.add(deleteMessage);
        }

        set.close();
        connection.createStatement().execute(
                "delete from messages_for_deletion"
        );

        return deleteMessages;
    }

    public void deleteMessageForDeletion(long chatId) throws SQLException {
        PreparedStatement statement = connection
                .prepareStatement("delete from messages_for_deletion where chat_id = ?");
        statement.setLong(1, chatId);
        statement.executeUpdate();
        statement.close();
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
