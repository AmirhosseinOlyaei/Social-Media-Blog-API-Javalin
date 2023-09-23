package DAO;

import java.util.List;

import Model.Account;

public interface AccountDAO {
    List<Account> getAllAccounts();

    Account getAccountById(int id);

    Account getAccountByUsername(String username); // New method

    boolean insertAccount(Account account);

    boolean updateAccount(Account account);

    boolean deleteAccount(int id);

    Account saveAccount(Account account);

    // ... any other required operations
}
