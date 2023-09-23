package Service;

import Model.Message;
import DAO.MessageDAO;
import java.util.List;

public class MessageService {

    private final MessageDAO messageDAO;

    // Constructor modified to accept a MessageDAO object
    public MessageService(MessageDAO messageDAO) {
        this.messageDAO = messageDAO;
    }

    /**
     * Add a new message.
     * 
     * @param message - The message to be added.
     * @return - True if the operation was successful, False otherwise.
     */
    public boolean addMessage(Message message) {
        return messageDAO.insertMessage(message);
    }

    /**
     * Retrieve a specific message by its ID.
     * 
     * @param messageId - The ID of the message.
     * @return - The Message object if found, null otherwise.
     */
    public Message getMessageById(int messageId) {
        return messageDAO.getMessageById(messageId);
    }

    /**
     * Retrieve all messages by a specific user.
     * 
     * @param postedBy - The ID of the user.
     * @return - List of Message objects.
     */
    public List<Message> getMessagesByUser(int postedBy) {
        return messageDAO.getMessagesByUser(postedBy);
    }

    // ... You can add more methods as needed.
}
