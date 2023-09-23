package Controller;

import DAO.AccountDAO;
import DAO.AccountDAOImpl;
import DAO.MessageDAO;
import DAO.MessageDAOImpl;
import Model.Account;
import Model.Message;
import Model.ResponseMessage;
import Service.AccountService;
import Service.MessageService;
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
        Message message = ctx.bodyAsClass(Message.class);
        validateMessage(ctx, message);
        if (messageService.addMessage(message)) {
            ctx.json(message);
        } else {
            sendErrorResponse(ctx, 500, "Failed to post message",
                    String.format("Failed to post message for user with ID %d", message.getPosted_by()));
        }
    }

    private void validateMessage(Context ctx, Message message) {
        String text = message.getMessage_text();
        if (text == null || text.trim().isEmpty()) {
            sendErrorResponse(ctx, 400, "Message text cannot be blank");
        } else if (text.length() > 254) {
            sendErrorResponse(ctx, 400, "Message text exceeds 254 characters");
        } else if (accountService.getAccountById(message.getPosted_by()) == null) {
            sendErrorResponse(ctx, 400, "User not found in the database",
                    String.format("Attempt to post by non-existent user with ID %d", message.getPosted_by()));
        }
    }

    private void registerUser(Context ctx) {
        try {
            Account account = ctx.bodyAsClass(Account.class);
            validateAccount(ctx, account);
            if (accountService.checkIfUserExists(account.getUsername())) {
                ctx.status(400).result(""); // Respond with 400 status and empty body.
            } else {
                Account createdAccount = accountService.createAccount(account);
                if (createdAccount != null) {
                    ctx.status(200).json(createdAccount); // Respond with 200 and the created account.
                } else {
                    sendErrorResponse(ctx, 500, "Failed to register user",
                            String.format("Failed to register user with username %s", account.getUsername()));
                }
            }
        } catch (IllegalArgumentException e) {
            // If the exception's message is meaningful, you can log it.
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

    private void loginUser(Context ctx) {
        Account inputAccount = ctx.bodyAsClass(Account.class);
        if (isNullOrBlank(inputAccount.getUsername()) || isNullOrBlank(inputAccount.getPassword())) {
            sendErrorResponse(ctx, 400, "Invalid input");
        } else {
            authenticate(ctx, inputAccount);
        }
    }

    private void authenticate(Context ctx, Account inputAccount) {
        Account existingAccount = accountService.getAccountByUsername(inputAccount.getUsername());
        if (existingAccount == null || !existingAccount.getPassword().equals(inputAccount.getPassword())) {
            sendErrorResponse(ctx, 401, "Invalid username or password",
                    String.format("Invalid login attempt for username %s", inputAccount.getUsername()));
        } else {
            ctx.status(200).json(mapResponse("Login successful"));
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

}
