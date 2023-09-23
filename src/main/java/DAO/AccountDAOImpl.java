package DAO;

import Model.Account;
import Util.ConnectionUtil;
import java.sql.*;
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
        this.connection = ConnectionUtil.getConnection();
    }

    @Override
    public List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(GET_ALL_ACCOUNTS);

            while (rs.next()) {
                Account account = mapResultSetToAccount(rs);
                accounts.add(account);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt);
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
                account = mapResultSetToAccount(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt);
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
                account = mapResultSetToAccount(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt);
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
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources(null, pstmt);
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
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources(null, pstmt);
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
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources(null, pstmt);
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
                    account.setAccount_id(generatedKeys.getInt(1));
                    return account;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt);
        }
        return null; // Failed to save the account
    }

    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setAccount_id(rs.getInt(COLUMN_ACCOUNT_ID));
        account.setUsername(rs.getString(COLUMN_USERNAME));
        account.setPassword(rs.getString(COLUMN_PASSWORD));
        return account;
    }

    private void closeResources(ResultSet rs, Statement stmt) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
