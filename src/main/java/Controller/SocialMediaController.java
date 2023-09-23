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
        app.get("/messages/{id}", this::getMessageById);
        app.post("/messages", this::postMessage);
        app.post("/register", this::registerUser);
        app.post("/login", this::loginUser);
        app.delete("/messages/{messageId}", this::deleteMessage);

        // Add a new DELETE endpoint for deleting a message by ID
        app.delete("/messages/{id}", ctx -> {
            // Parse the message ID from the request parameter
            int messageId = Integer.parseInt(ctx.pathParam("id"));

            // Call the deleteMessageById method from your MessageService to delete the
            // message by its ID
            boolean deleted = messageService.deleteMessageById(messageId);

            if (deleted) {
                // Message deleted successfully, return a 200 response
                ctx.status(200).result("Message deleted successfully");
            } else {
                // Failed to delete the message, return an error response (e.g., 404 Not Found)
                ctx.status(404).result("Message not found");
            }
        });

    }

    private void getAllAccounts(Context ctx) {
        ctx.json(accountService.getAllAccounts());
    }

    private void getMessageById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Message message = messageService.getMessageById(id);
        if (message == null) {
            sendErrorResponse(ctx, 404, "Message not found", String.format("Message with ID %d not found", id));
        } else {
            ctx.json(message);
        }
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
                    ctx.status(400).json(""); // Return an empty response body with status code 400
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
            ctx.status(400).result(""); // Respond with 400 status and empty body.
            throw new IllegalArgumentException("Username cannot be blank");
        }

        if (isNullOrBlank(account.getPassword())) {
            ctx.status(400).result(""); // Respond with 400 status and empty body.
            throw new IllegalArgumentException("Password cannot be blank");
        }

        if (account.getPassword().length() < 4) {
            ctx.status(400).result(""); // Respond with 400 status and empty body.
            throw new IllegalArgumentException("Password must be at least 4 characters long");
        }
    }

    public void loginUser(Context ctx) {
        Account inputAccount = ctx.bodyAsClass(Account.class);

        if (isNullOrBlank(inputAccount.getUsername()) || isNullOrBlank(inputAccount.getPassword())) {
            sendErrorResponse(ctx, 400, "Invalid input");
            return;
        }

        // Check if the password is valid.
        Account existingAccount = accountService.getAccountByUsername(inputAccount.getUsername());
        if (existingAccount == null || !existingAccount.getPassword().equals(inputAccount.getPassword())) {
            // Return a 401 Unauthorized response.
            ctx.status(401).json(mapResponse(""));
            return;
        }

        // Authenticate the user.
        authenticate(ctx, inputAccount);
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
            boolean deleted = messageService.deleteMessageById(messageId);

            if (deleted) {
                // Respond with a JSON object indicating success
                ctx.status(200).json(new ResponseMessage("Message deleted successfully"));
            } else {
                // Respond with a JSON object indicating failure
                ctx.status(404).json(new ResponseMessage("Message not found"));
            }
        } catch (NumberFormatException e) {
            // Handle the case where the message ID is not a valid integer
            ctx.status(400).json(new ResponseMessage("Invalid message ID"));
        } catch (Exception e) {
            log.warn(e.getMessage());
            ctx.status(500).json(new ResponseMessage("Server error"));
        }
    }

}
