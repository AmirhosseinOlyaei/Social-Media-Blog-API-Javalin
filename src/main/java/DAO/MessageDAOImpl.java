package DAO;

import Model.Message;
import Util.ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageDAOImpl implements MessageDAO {

    private static final Logger log = LoggerFactory.getLogger(MessageDAOImpl.class);

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
    private static final String CHECK_USER_EXISTENCE = "SELECT COUNT(*) FROM account WHERE account_id = ?";

    private Connection connection;

    public MessageDAOImpl() {
        connection = ConnectionUtil.getConnection();
    }

    @Override
    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(GET_ALL_MESSAGES)) {

            while (rs.next()) {
                Message message = extractMessageFromResultSet(rs);
                messages.add(message);
            }

        } catch (Exception e) {
            handleError(e);
        }
        return messages;
    }

    @Override
    public Message getMessageById(int id) {
        Message message = null;
        try (PreparedStatement pstmt = connection.prepareStatement(GET_MESSAGE_BY_ID)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    message = extractMessageFromResultSet(rs);
                }
            }
        } catch (Exception e) {
            handleError(e);
        }
        return message;
    }

    @Override
    public List<Message> getMessagesByUser(int userId) {
        List<Message> messages = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(GET_MESSAGES_BY_USER)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Message message = extractMessageFromResultSet(rs);
                    messages.add(message);
                }
            }
        } catch (Exception e) {
            handleError(e);
        }
        return messages;
    }

    @Override
    public boolean insertMessage(Message message) {
        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_MESSAGE, Statement.RETURN_GENERATED_KEYS)) {
            setPreparedStatementForMessage(pstmt, message);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 1) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int generatedMessageId = rs.getInt(1);
                        message.setMessage_id(generatedMessageId);
                    }
                }
                return true;
            }
        } catch (Exception e) {
            handleError(e);
        }
        return false;
    }

    @Override
    public boolean updateMessage(Message message) {
        try (PreparedStatement pstmt = connection.prepareStatement(UPDATE_MESSAGE)) {
            setPreparedStatementForMessage(pstmt, message);
            pstmt.setInt(4, message.getMessage_id());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows == 1;

        } catch (Exception e) {
            handleError(e);
        }
        return false;
    }

    @Override
    public boolean deleteMessage(int id) {
        try (Connection connection = ConnectionUtil.getConnection()) {
            // Create a PreparedStatement to delete a message by its ID
            String sql = "DELETE FROM messages WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                // Set the ID parameter
                preparedStatement.setInt(1, id);

                // Execute the delete statement
                int rowsDeleted = preparedStatement.executeUpdate();

                // If one or more rows were deleted, return true; otherwise, return false
                return rowsDeleted > 0;
            }
        } catch (SQLException e) {
            // Handle any SQL exceptions here, e.g., log the error
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteMessageById(int messageId) {
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(DELETE_MESSAGE);
            pstmt.setInt(1, messageId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows == 1;
        } catch (Exception e) {
            log.error("Error while deleting message by ID: " + messageId, e);
            return false; // Handle the error and return false in case of an exception.
        } finally {
            // Ensure that the PreparedStatement is closed in the finally block
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    log.error("Error while closing PreparedStatement", e);
                }
            }
        }
    }

    @Override
    public boolean doesUserExist(int userId) {
        try (PreparedStatement pstmt = connection.prepareStatement(CHECK_USER_EXISTENCE)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            handleError(e);
        }
        return false;
    }

    private Message extractMessageFromResultSet(ResultSet rs) throws Exception {
        Message message = new Message();
        message.setMessage_id(rs.getInt(COLUMN_MESSAGE_ID));
        message.setPosted_by(rs.getInt(COLUMN_POSTED_BY));
        message.setMessage_text(rs.getString(COLUMN_MESSAGE_TEXT));
        message.setTime_posted_epoch(rs.getLong(COLUMN_TIME_POSTED_EPOCH));
        return message;
    }

    private void setPreparedStatementForMessage(PreparedStatement pstmt, Message message) throws Exception {
        pstmt.setInt(1, message.getPosted_by());
        pstmt.setString(2, message.getMessage_text());
        pstmt.setLong(3, message.getTime_posted_epoch());
    }

    private void handleError(Exception e) {
        log.error("An error occurred in MessageDAOImpl", e);
    }

}
