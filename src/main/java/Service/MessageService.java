package Service;

import Model.Message;
import DAO.MessageDAO;
import java.util.List;

public class MessageService {

    private final MessageDAO messageDAO;

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

        validateMessage(message);

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

    /**
     * Validates the message before adding.
     * 
     * @param message - The message to be validated.
     */
    private void validateMessage(Message message) {
        // Check if the message text is not blank and under 255 characters
        if (message.getMessage_text() == null || message.getMessage_text().trim().isEmpty()
                || message.getMessage_text().length() > 255) {
            throw new IllegalArgumentException("Invalid message text");
        }

        // Check if the user exists (assuming there's a method in MessageDAO to do that)
        if (!messageDAO.doesUserExist(message.getPosted_by())) {
            throw new IllegalArgumentException("User not found");
        }
    }

    // ... You can add more methods as needed.
}
