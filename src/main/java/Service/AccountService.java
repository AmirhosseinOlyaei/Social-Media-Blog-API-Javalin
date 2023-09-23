package Service;

import java.util.List;

import DAO.AccountDAO;
import Model.Account;

public class AccountService {

    private AccountDAO accountDAO;

    public AccountService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    /**
     * Fetches all accounts from the database.
     * 
     * @return List of all accounts.
     */
    public List<Account> getAllAccounts() {
        return accountDAO.getAllAccounts();
    }

    /**
     * Checks if a user with the given username already exists.
     * 
     * @param username The username to check.
     * @return true if the user exists, false otherwise.
     */
    public boolean checkIfUserExists(String username) {
        return accountDAO.getAccountByUsername(username) != null;
    }

    /**
     * Creates an account. If the username already exists, it returns null.
     * 
     * @param account The account object to create.
     * @return The created account or null if it couldn't be created.
     */
    public Account createAccount(Account account) {
        if (!checkIfUserExists(account.getUsername())) {
            return accountDAO.saveAccount(account);
        }
        return null; // User with the same username already exists
    }

    /**
     * Fetches an account based on the username.
     * 
     * @param username The username to look for.
     * @return The account associated with the given username or null if not found.
     */
    public Account getAccountByUsername(String username) {
        return accountDAO.getAccountByUsername(username);
    }

    /**
     * Fetches an account based on its ID.
     * 
     * @param id The ID of the account.
     * @return The account with the given ID or null if not found.
     */
    public Account getAccountById(int id) {
        return accountDAO.getAccountById(id);
    }

    // ... other methods that might include business logic, validation, etc.
}
