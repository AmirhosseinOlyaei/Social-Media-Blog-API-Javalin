package DAO;

import Model.Message;
import java.util.List;

public interface MessageDAO {
    List<Message> getAllMessages();

    Message getMessageById(int id);

    List<Message> getMessagesByUser(int userId);

    boolean insertMessage(Message message);

    boolean updateMessageText(int messageId, String newText);

    boolean deleteMessageById(int messageId);

    boolean doesUserExist(int userId);

}
