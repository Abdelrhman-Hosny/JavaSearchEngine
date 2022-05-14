package Database;
import java.sql.*;
import com.microsoft.sqlserver.jdbc.SQLServerDriver;

public class BaseDAO {
    
    // Connecting to database.
    protected static final String JDBC_DRIVER = ("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    protected static final String DB_URL = "jdbc:sqlserver://dell-PC\\SQLEXPRESS:1433;databaseName=Indexer;encrypt=false";
    protected static final String USER = "sa";
    protected static final String PASS = "ahmed";
    protected static Connection connection ;
    
    public BaseDAO(){
        // establish connection on construction
        establishConnection();
    }
    
    protected static void establishConnection() {
        
        try {
            // checking for jdbc driver 
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }

        try {
            // establishing connection with databse
            connection = DriverManager.getConnection(DB_URL,USER,PASS);
            System.out.println("Connected to database...");
        }
        catch (SQLException e) {
            // Handle any errors that may have occurred.
            e.printStackTrace();
        }
    }

    public int CloseConnection() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Connection closed successfully");
            }
            return 1;
        } catch (SQLException se) {
            // error occured while closing the connection
            se.printStackTrace();
        }
        return 0;
    }

    
}