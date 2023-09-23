package Controller;

import java.util.List;

import DAO.AccountDAO;
import DAO.AccountDAOImpl;
import DAO.MessageDAO;
import DAO.MessageDAOImpl;
import Model.Account;
import Model.Message;
import Service.AccountService;
import Service.MessageService;
import io.javalin.Javalin;
import io.javalin.http.Context;

/**
 * TODO: You will need to write your own endpoints and handlers for your
 * controller. The endpoints you will need can be
 * found in readme.md as well as the test cases. You should
 * refer to prior mini-project labs and lecture materials for guidance on how a
 * controller may be built.
 */
public class SocialMediaController {

    private AccountService accountService;
    private MessageService messageService;

    public SocialMediaController() {
        AccountDAO accountDAO = new AccountDAOImpl();
        this.accountService = new AccountService(accountDAO);

        MessageDAO messageDAO = new MessageDAOImpl();
        this.messageService = new MessageService(messageDAO);
    }

    // ... your endpoints using the service layer methods

    /**
     * In order for the test cases to work, you will need to write the endpoints in
     * the startAPI() method, as the test
     * suite must receive a Javalin object from this method.
     * 
     * @return a Javalin app object which defines the behavior of the Javalin
     *         controller.
     */

    public Javalin startAPI() {
        Javalin app = Javalin.create();
        app.get("/accounts", this::getAllAccounts);
        app.get("/messages/{id}", this::getMessageById);
        app.post("/messages", this::postMessage);
        app.post("/register", this::registerUser); // Added this line for the /register endpoint

        return app;
    }

    private void getAllAccounts(Context context) {
        List<Account> accounts = accountService.getAllAccounts();
        context.json(accounts);
    }

    private void getMessageById(Context context) {
        int id = Integer.parseInt(context.pathParam("id"));
        Message message = messageService.getMessageById(id);
        if (message != null) {
            context.json(message);
        } else {
            context.status(404).json("Message not found");
        }
    }

    private void postMessage(Context context) {
        Message message = context.bodyAsClass(Message.class);
        if (messageService.addMessage(message)) {
            context.status(201).json("Message posted successfully");
        } else {
            context.status(500).json("Failed to post message");
        }
    }

    // New method to handle registration
    private void registerUser(Context context) {
        Account account = context.bodyAsClass(Account.class);

        if (account.getUsername().isEmpty()) {
            context.status(400).json("Username cannot be empty");
            return;
        }

        if (account.getPassword().length() < 4) {
            context.status(400).json("Password should be at least 4 characters");
            return;
        }

        if (accountService.checkIfUserExists(account.getUsername())) {
            context.status(400).json("Username already exists");
            return;
        }

        Account createdAccount = accountService.createAccount(account);
        if (createdAccount != null) {
            context.status(200).json(createdAccount);
        } else {
            context.status(500).json("Failed to register user");
        }
    }
}
