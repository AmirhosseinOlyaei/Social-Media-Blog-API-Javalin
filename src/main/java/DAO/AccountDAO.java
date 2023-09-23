package DAO;

import java.util.List;
import Model.Account;

public interface AccountDAO {

    /**
     * Retrieves all accounts from the data source.
     * 
     * @return a list of accounts.
     */
    List<Account> getAllAccounts();

    /**
     * Retrieves an account based on its ID.
     * 
     * @param id the ID of the desired account.
     * @return the account if found; null otherwise.
     */
    Account getAccountById(int id);

    /**
     * Retrieves an account based on its username.
     * 
     * @param username the username of the desired account.
     * @return the account if found; null otherwise.
     */
    Account getAccountByUsername(String username);

    /**
     * Inserts a new account into the data source.
     * 
     * @param account the account to be inserted.
     * @return true if the operation was successful; false otherwise.
     */
    boolean insertAccount(Account account);

    /**
     * Updates an existing account in the data source.
     * 
     * @param account the account with updated details.
     * @return true if the operation was successful; false otherwise.
     */
    boolean updateAccount(Account account);

    /**
     * Deletes an account from the data source.
     * 
     * @param id the ID of the account to be deleted.
     * @return true if the operation was successful; false otherwise.
     */
    boolean deleteAccount(int id);

    /**
     * Saves (inserts or updates) an account in the data source.
     * 
     * @param account the account to be saved.
     * @return the saved account.
     */
    Account saveAccount(Account account);

    // ... any other required operations
}
