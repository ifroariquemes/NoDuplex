package com.noduplex.dao;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import org.controlsfx.dialog.Dialogs;
import org.ini4j.Ini;

/**
 * Connect database and use it with less experience, just edit the ini file and
 * let the magic begin
 * <p>
 * How to use:
 * <p>
 * <code>
 * Database db = Database.getInstance();<br>
 * db.prepare("INSERT INTO table (column) VALUES (?)");<br>
 * db.setString(1, "the value");<br>
 * db.execute();
 * </code>
 *
 * @author natanaelsimoes
 *
 */
public class Database {

    private static Database instance;

    private Connection conn;
    private PreparedStatement pstm;
    private String query;

    /**
     * Starts getting configuration file NoDuplex.ini with database properties
     * If you don't have one, create like following at same folder of .jar:
     * <p>
     * [Database] Driver = postgres<br>
     * Host = localhost<br>
     * Port = 5432<br>
     * Base = noduplex<br>
     * User = postgres<br>
     * Pass = yourPass
     *
     * @throws ClassNotFoundException
     */
    public Database() throws ClassNotFoundException {
        try {
            String absolutePath = new File(".").getAbsolutePath();
            File f = new File(absolutePath.substring(0, absolutePath.length() - 1) + "NoDuplex.ini");
            Ini ini = new Ini(f);
            Class.forName(String.format("org.%s.Driver", ini.get("Database", "Driver")));
            String url = String.format("jdbc:%s://%s:%s/%s",
                    ini.get("Database", "Driver"),
                    ini.get("Database", "Host"),
                    ini.get("Database", "Port"),
                    ini.get("Database", "Base")
            );
            String user = ini.get("Database", "User");
            String pass = ini.get("Database", "Pass");
            this.conn = DriverManager.getConnection(url, user, pass);
        } catch (IOException e) {
            Dialogs.create().title("Erro de configuração").masthead("Arquivo de configuração NoDuplex.ini não foi encontrado").showException(e);
            System.exit(0);
        } catch (SQLException e) {
            Dialogs.create().title("Erro de configuração").masthead("Ocorreu um erro durante a conexão com o banco de dados. Por favor, verifique o arquivo NoDuplex.ini e tente novamente.").showException(e);
            System.exit(0);
        }
    }

    /**
     * This class is auto-magically instantiate, so use it instead of doing it
     * by your self, so please save some time and your pc memory, no thanks
     * needed
     *
     * @return the singleton
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws IOException
     */
    public static synchronized Database getInstance() throws ClassNotFoundException, SQLException, IOException {
        if (instance == null) {
            return new Database();
        }
        return instance;
    }

    /**
     * Prepares a query for future executing, use it ONLY IF you have setted a
     * query before
     *
     * @throws SQLException
     */
    public void prepare() throws SQLException {
        if (!this.query.equals("")) {
            this.pstm = conn.prepareStatement(this.query);
        } else {
            throw new SQLException("Can't prepare a statement without a query, bro");
        }
    }

    /**
     * Prepares a query for future executing
     *
     * @param query the query
     * @throws SQLException
     */
    public void prepare(String query) throws SQLException {
        setQuery(query);
        prepare();
    }

    /**
     * Sets a query to do something on our database
     *
     * @param query the query
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Sets a long variable in a prepared statement
     *
     * @param i the index
     * @param l the value
     * @throws SQLException
     */
    public void setLong(int i, Long l) throws SQLException {
        this.pstm.setLong(i, l);
    }

    /**
     * Sets a string variable in a prepared statement
     *
     * @param i the index
     * @param string the value
     * @throws SQLException
     */
    public void setString(int i, String string) throws SQLException {
        this.pstm.setString(i, string);
    }

    /**
     * Executes a prepared statement
     *
     * @return number of affected row
     * @throws SQLException
     */
    public int execute() throws SQLException {
        try {            
            this.pstm.execute();
            return this.pstm.getUpdateCount();
        } finally {
            this.pstm.close();
        }
    }

    /**
     * Executes a prepared statement then returns the selected data
     *
     * @return the data
     * @throws SQLException
     */
    public ResultSet select() throws SQLException {        
        return this.pstm.executeQuery();
    }

}
