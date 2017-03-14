package services;

import sandbox.SqlSandboxUtils;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by willpride on 3/14/17.
 */
public abstract class BaseStorageManager implements ConnectionHandler{

    abstract String getUsername();
    abstract String getDatabasePath();
    abstract void setConnection(Connection connection);
    abstract Connection connection();


    @Override
    public Connection getConnection() {
        try {
            if (connection() == null || connection().isClosed()) {
                DataSource dataSource = SqlSandboxUtils.getDataSource(getUsername(), getDatabasePath());
                setConnection(dataSource.getConnection());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return connection();
    }

    public void setAutoCommit(boolean autoCommit) {
        try {
            connection().setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void commit() {
        try {
            connection().commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void closeConnection() {
        System.out.println("Closing connection " + connection());
        try {
            if(connection() != null && !connection().isClosed()) {
                connection().close();
                setConnection(null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
