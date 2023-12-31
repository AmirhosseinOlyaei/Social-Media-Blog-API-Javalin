package Controller;

import DAO.AccountDAOImpl;
import DAO.MessageDAOImpl;
import Model.Account;
import Model.Message;
import Model.ResponseMessage;
import Service.AccountService;
import Service.MessageService;
import Service.ValidationResult;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocialMediaController {

    private static final Logger log = LoggerFactory.getLogger(SocialMediaController.class);

    private AccountService accountService;
    private MessageService messageService;

    public SocialMediaController() {
        this.accountService = new AccountService(new AccountDAOImpl());
        this.messageService = new MessageService(new MessageDAOImpl());
    }

    public Javalin startAPI() {
        Javalin app = Javalin.create();
        registerExceptionHandlers(app);
        registerEndpoints(app);
        return app;
    }

    private void registerExceptionHandlers(Javalin app) {
        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(500).json(mapResponse("Server error"));
            log.error("Unexpected server error", e);
        });
    }

    private void registerEndpoints(Javalin app) {
        app.get("/accounts", this::getAllAccounts);
        app.get("/accounts/{accountId}/messages", this::getAllMessagesForUser);
        app.get("/messages/{id}", this::getMessageById);
        app.get("/messages", this::getAllMessages);
        app.post("/messages", this::postMessage);
        app.post("/register", this::registerUser);
        app.post("/login", this::loginUser);
        app.delete("/messages/{messageId}", this::deleteMessage);

        app.patch("/messages/{id}", ctx -> {
            // 1. Extract the message ID from the URL.
            int messageId = Integer.parseInt(ctx.pathParam("id"));

            // 2. Extract the new message text from the request body.
            String newMessageText = ctx.bodyAsClass(Message.class).getMessage_text();

            // 3. Call the updateMessageText method from the MessageService.
            ValidationResult validationResult = messageService.updateMessageText(messageId, newMessageText);

            // 4. Return the appropriate response based on the ValidationResult.
            if (validationResult.isValid()) {
                // Successfully updated the message. Return the updated message as the response.
                Message updatedMessage = messageService.getMessageById(messageId);
                ctx.status(200).json(updatedMessage);
            } else {
                String errorMessage = validationResult.getMessage();
                if (errorMessage.equals("Message text cannot be blank") ||
                        errorMessage.equals("Message text exceeds 254 characters") ||
                        errorMessage.equals("Message not found")) {
                    ctx.status(400).result(""); // Respond with 400 status and an empty body.
                } else {
                    ctx.status(500).result("Failed to update the message"); // Generic error response.
                }
            }
        });

    }

    private void getAllAccounts(Context ctx) {
        ctx.json(accountService.getAllAccounts());
    }

    private void getAllMessagesForUser(Context ctx) {
        int accountId = Integer.parseInt(ctx.pathParam("accountId"));
        List<Message> messages = messageService.getAllMessagesForUser(accountId);
        ctx.json(messages);
    }

    private void getMessageById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Message message = messageService.getMessageById(id);
        if (message == null) {
            ctx.status(200).result("");
        } else {
            ctx.json(message);
        }
    }

    private void getAllMessages(Context ctx) {
        List<Message> messages = messageService.getAllMessages();
        ctx.json(messages);
    }

    private void postMessage(Context ctx) {
        try {
            Message message = ctx.bodyAsClass(Message.class);
            ValidationResult validationResult = messageService.addMessage(message);

            if (validationResult.isValid()) {
                ctx.json(message);
            } else {
                if (validationResult.getMessage().equals("Message text exceeds 254 characters")
                        || validationResult.getMessage().equals("Message text cannot be blank")
                        || validationResult.getMessage().equals("User not found in the database")) {
                    ctx.status(400).json("");
                } else {
                    sendValidationErrorResponse(ctx, 400, validationResult.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

    private void sendValidationErrorResponse(Context ctx, int status, String message) {
        ctx.status(status).json(mapResponse(message));
    }

    private void registerUser(Context ctx) {
        try {
            Account account = ctx.bodyAsClass(Account.class);
            validateAccount(ctx, account); // This will throw an exception if validation fails.
            if (accountService.checkIfUserExists(account.getUsername())) {
                ctx.status(400).result("");
                return;
            }
            Account createdAccount = accountService.createAccount(account);
            if (createdAccount != null) {
                ctx.status(200).json(createdAccount);
            } else {
                sendErrorResponse(ctx, 500, "Failed to register user",
                        String.format("Failed to register user with username %s", account.getUsername()));
            }
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
        }
    }

    private void validateAccount(Context ctx, Account account) {
        if (isNullOrBlank(account.getUsername())) {
            ctx.status(400).result("");
            throw new IllegalArgumentException("Username cannot be blank");
        }

        if (isNullOrBlank(account.getPassword())) {
            ctx.status(400).result("");
            throw new IllegalArgumentException("Password cannot be blank");
        }

        if (account.getPassword().length() < 4) {
            ctx.status(400).result("");
            throw new IllegalArgumentException("Password must be at least 4 characters long");
        }
    }

    public void loginUser(Context ctx) {
        Account inputAccount = ctx.bodyAsClass(Account.class);

        if (isNullOrBlank(inputAccount.getUsername()) || isNullOrBlank(inputAccount.getPassword())) {
            sendErrorResponse(ctx, 400, "Invalid input");
            return;
        }

        // Get the stored account details from the database based on the username.
        Account existingAccount = accountService.getAccountByUsername(inputAccount.getUsername());

        // Check if the username exists and if the provided password matches.
        if (existingAccount == null || !existingAccount.getPassword().equals(inputAccount.getPassword())) {
            ctx.status(401).result(""); // Unauthorized
        } else {
            ctx.status(200).json(existingAccount); // Successfully authenticated
        }
    }

    private void authenticate(Context ctx, Account inputAccount) {
        Account existingAccount = accountService.getAccountByUsername(inputAccount.getUsername());
        if (existingAccount == null || !existingAccount.getPassword().equals(inputAccount.getPassword())) {
            // Modify the response to include an empty message
            ctx.status(401).json(mapResponse(""));
        } else {
            ctx.status(200).json(existingAccount);
        }
    }

    private void sendErrorResponse(Context ctx, int status, String message, String logMessage) {
        ctx.status(status).json(mapResponse(message));
        log.warn(logMessage);
    }

    private void sendErrorResponse(Context ctx, int status, String message) {
        ctx.status(status).json(mapResponse(message));
    }

    private ResponseMessage mapResponse(String message) {
        return new ResponseMessage(message);
    }

    private boolean isNullOrBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void deleteMessage(Context ctx) {
        try {
            int messageId = Integer.parseInt(ctx.pathParam("messageId"));
            Message message = messageService.getMessageById(messageId);

            if (message != null) {
                messageService.deleteMessageById(messageId);
                ctx.status(200).json(message);
            } else {
                ctx.status(200).result(""); // No content for a message that didn't exist
            }

        } catch (NumberFormatException e) {
            // Handle the case where the message ID is not a valid integer
            ctx.status(400);
        } catch (Exception e) {
            log.warn(e.getMessage());
            ctx.status(500);
        }
    }

}
