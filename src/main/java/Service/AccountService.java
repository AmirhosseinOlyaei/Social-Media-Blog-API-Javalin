package Service;

import java.util.List;

import DAO.AccountDAO;
import Model.Account;

public class AccountService {

    private AccountDAO accountDAO;

    public AccountService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    public List<Account> getAllAccounts() {
        return accountDAO.getAllAccounts();
    }

    // Method to check if a user with the given username already exists
    public boolean checkIfUserExists(String username) {
        Account existingAccount = accountDAO.getAccountByUsername(username);
        return existingAccount != null;
    }

    public Account createAccount(Account account) {
        if (checkIfUserExists(account.getUsername())) {
            return null; // User with the same username already exists
        }
        return accountDAO.saveAccount(account); // This will now return the saved Account or null if not saved.
    }

    // ... other methods that might include business logic, validation, etc.
}
