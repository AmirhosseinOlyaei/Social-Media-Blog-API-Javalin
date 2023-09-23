package DAO;

import Model.Message;
import java.util.List;

public interface MessageDAO {
    List<Message> getAllMessages();

    Message getMessageById(int id);

    List<Message> getMessagesByUser(int userId);

    boolean insertMessage(Message message);

    boolean updateMessage(Message message);

    boolean deleteMessage(int id);
}
