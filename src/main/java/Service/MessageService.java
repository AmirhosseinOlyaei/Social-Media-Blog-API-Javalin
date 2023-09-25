package Service;

import Model.Message;
import DAO.MessageDAO;

import java.util.ArrayList;
import java.util.List;

public class MessageService {

    private final MessageDAO messageDAO;

    public MessageService(MessageDAO messageDAO) {
        this.messageDAO = messageDAO;
    }

    public List<Message> getAllMessages() {
        return messageDAO.getAllMessages();
    }

    public List<Message> getAllMessagesForUser(int accountId) {
        return messageDAO.getMessagesByUser(accountId);
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

    public ValidationResult addMessage(Message message) {
        ValidationResult validationResult = validateMessage(message);
        if (!validationResult.isValid()) {
            return validationResult;
        }

        boolean added = messageDAO.insertMessage(message);
        if (added) {
            return ValidationResult.valid();
        } else {
            return ValidationResult.error("Failed to post message");
        }
    }

    private ValidationResult validateMessage(Message message) {
        String text = message.getMessage_text();
        if (isNullOrBlank(text)) {
            return ValidationResult.error("Message text cannot be blank");
        } else if (text.length() > 254) {
            return ValidationResult.error("Message text exceeds 254 characters");
        } else if (!messageDAO.doesUserExist(message.getPosted_by())) {
            return ValidationResult.error("User not found in the database");
        }

        return ValidationResult.valid();
    }

    private boolean isNullOrBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // Define a method to delete a message by its ID
    public boolean deleteMessageById(int messageId) {
        return messageDAO.deleteMessageById(messageId);
    }

    // Other methods for message retrieval, addition, etc.

    // ... You can add more methods as needed.
}
