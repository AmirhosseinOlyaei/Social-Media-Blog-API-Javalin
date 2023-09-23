package DAO;

import Model.Message;
import Util.ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MessageDAOImpl implements MessageDAO {

    private static final String COLUMN_MESSAGE_ID = "message_id";
    private static final String COLUMN_POSTED_BY = "posted_by";
    private static final String COLUMN_MESSAGE_TEXT = "message_text";
    private static final String COLUMN_TIME_POSTED_EPOCH = "time_posted_epoch";

    private static final String GET_ALL_MESSAGES = "SELECT * FROM message";
    private static final String GET_MESSAGE_BY_ID = "SELECT * FROM message WHERE message_id = ?";
    private static final String GET_MESSAGES_BY_USER = "SELECT * FROM message WHERE posted_by = ?";
    private static final String INSERT_MESSAGE = "INSERT INTO message(posted_by, message_text, time_posted_epoch) VALUES (?, ?, ?)";
    private static final String UPDATE_MESSAGE = "UPDATE message SET posted_by = ?, message_text = ?, time_posted_epoch = ? WHERE message_id = ?";
    private static final String DELETE_MESSAGE = "DELETE FROM message WHERE message_id = ?";

    private Connection connection;

    public MessageDAOImpl() {
        connection = ConnectionUtil.getConnection();
    }

    @Override
    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(GET_ALL_MESSAGES);

            while (rs.next()) {
                Message message = new Message();
                message.setMessage_id(rs.getInt(COLUMN_MESSAGE_ID));
                message.setPosted_by(rs.getInt(COLUMN_POSTED_BY));
                message.setMessage_text(rs.getString(COLUMN_MESSAGE_TEXT));
                message.setTime_posted_epoch(rs.getLong(COLUMN_TIME_POSTED_EPOCH));
                messages.add(message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return messages;
    }

    @Override
    public Message getMessageById(int id) {
        Message message = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = connection.prepareStatement(GET_MESSAGE_BY_ID);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                message = new Message();
                message.setMessage_id(rs.getInt(COLUMN_MESSAGE_ID));
                message.setPosted_by(rs.getInt(COLUMN_POSTED_BY));
                message.setMessage_text(rs.getString(COLUMN_MESSAGE_TEXT));
                message.setTime_posted_epoch(rs.getLong(COLUMN_TIME_POSTED_EPOCH));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return message;
    }

    @Override
    public List<Message> getMessagesByUser(int userId) {
        List<Message> messages = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = connection.prepareStatement(GET_MESSAGES_BY_USER);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Message message = new Message();
                message.setMessage_id(rs.getInt(COLUMN_MESSAGE_ID));
                message.setPosted_by(rs.getInt(COLUMN_POSTED_BY));
                message.setMessage_text(rs.getString(COLUMN_MESSAGE_TEXT));
                message.setTime_posted_epoch(rs.getLong(COLUMN_TIME_POSTED_EPOCH));
                messages.add(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return messages;
    }

    @Override
    public boolean insertMessage(Message message) {
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(INSERT_MESSAGE);
            pstmt.setInt(1, message.getPosted_by());
            pstmt.setString(2, message.getMessage_text());
            pstmt.setLong(3, message.getTime_posted_epoch());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean updateMessage(Message message) {
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(UPDATE_MESSAGE);
            pstmt.setInt(1, message.getPosted_by());
            pstmt.setString(2, message.getMessage_text());
            pstmt.setLong(3, message.getTime_posted_epoch());
            pstmt.setInt(4, message.getMessage_id());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean deleteMessage(int id) {
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(DELETE_MESSAGE);
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
