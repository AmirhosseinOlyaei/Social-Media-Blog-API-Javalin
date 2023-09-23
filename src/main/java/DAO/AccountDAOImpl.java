package DAO;

import Model.Account;
import Util.ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AccountDAOImpl implements AccountDAO {

    private static final String COLUMN_ACCOUNT_ID = "account_id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    private static final String GET_ALL_ACCOUNTS = "SELECT * FROM account";
    private static final String GET_ACCOUNT_BY_ID = "SELECT * FROM account WHERE account_id = ?";
    private static final String GET_ACCOUNT_BY_USERNAME = "SELECT * FROM account WHERE username = ?";
    private static final String INSERT_ACCOUNT = "INSERT INTO account(username, password) VALUES (?, ?)";
    private static final String UPDATE_ACCOUNT = "UPDATE account SET username = ?, password = ? WHERE account_id = ?";
    private static final String DELETE_ACCOUNT = "DELETE FROM account WHERE account_id = ?";

    private Connection connection;

    public AccountDAOImpl() {
        connection = ConnectionUtil.getConnection();
    }

    @Override
    public List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(GET_ALL_ACCOUNTS)) {

            while (rs.next()) {
                Account account = new Account();
                account.setAccount_id(rs.getInt(COLUMN_ACCOUNT_ID));
                account.setUsername(rs.getString(COLUMN_USERNAME));
                account.setPassword(rs.getString(COLUMN_PASSWORD));
                accounts.add(account);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accounts;
    }

    @Override
    public Account getAccountById(int id) {
        Account account = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = connection.prepareStatement(GET_ACCOUNT_BY_ID);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                account = new Account();
                account.setAccount_id(rs.getInt(COLUMN_ACCOUNT_ID));
                account.setUsername(rs.getString(COLUMN_USERNAME));
                account.setPassword(rs.getString(COLUMN_PASSWORD));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return account;
    }

    @Override
    public Account getAccountByUsername(String username) {
        Account account = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = connection.prepareStatement(GET_ACCOUNT_BY_USERNAME);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                account = new Account();
                account.setAccount_id(rs.getInt(COLUMN_ACCOUNT_ID));
                account.setUsername(rs.getString(COLUMN_USERNAME));
                account.setPassword(rs.getString(COLUMN_PASSWORD));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return account;
    }

    @Override
    public boolean insertAccount(Account account) {
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(INSERT_ACCOUNT);
            pstmt.setString(1, account.getUsername());
            pstmt.setString(2, account.getPassword());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean updateAccount(Account account) {
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(UPDATE_ACCOUNT);
            pstmt.setString(1, account.getUsername());
            pstmt.setString(2, account.getPassword());
            pstmt.setInt(3, account.getAccount_id());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean deleteAccount(int id) {
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(DELETE_ACCOUNT);
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Account saveAccount(Account account) {
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(INSERT_ACCOUNT, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, account.getUsername());
            pstmt.setString(2, account.getPassword());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 1) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    account.setAccount_id(generatedKeys.getInt(1)); // Assuming account_id is the first column in the
                                                                    // table
                    return account;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null; // Failed to save the account
    }

}
