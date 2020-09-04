package blok2.daos.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Abstract class used for all the database DAOs. Implements universal functionalities.
 */
@Service
@ConfigurationProperties(prefix = "db")
public class ADB {
    private String url;
    private String username;
    private String password;

    @Autowired
    private DatabaseScriptsConfiguration databaseScriptsConfiguration;

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void dropSchema() {
        try {
            Connection conn = getConnection();
            executeQueriesFromFile(databaseScriptsConfiguration.getDropSchema(), conn);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void createSchema() {
        try {
            Connection conn = getConnection();
            executeQueriesFromFile(databaseScriptsConfiguration.getDropSchema(), conn);
            executeQueriesFromFile(databaseScriptsConfiguration.getCreateSchema(), conn);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    private void executeQueriesFromFile(String path, Connection conn) throws SQLException {
        ArrayList<String> queries = createQueries(path);
        Statement stm = conn.createStatement();
        for (String query : queries) {
            stm.execute(query);
        }
    }

    private ArrayList<String> createQueries(String path) {
        String queryLine;
        StringBuffer sBuffer = new StringBuffer();
        ArrayList<String> listOfQueries = new ArrayList<String>();

        try {
            FileReader fr = new FileReader(new File(path));
            BufferedReader br = new BufferedReader(fr);

            //read the SQL file line by line
            while ((queryLine = br.readLine()) != null) {
                // ignore comments beginning with #
                int indexOfCommentSign = queryLine.indexOf('#');
                if (indexOfCommentSign != -1) {
                    if (queryLine.startsWith("#")) {
                        queryLine = "";
                    } else
                        queryLine = queryLine.substring(0, indexOfCommentSign);
                }
                // ignore comments beginning with --
                indexOfCommentSign = queryLine.indexOf("--");
                if (indexOfCommentSign != -1) {
                    if (queryLine.startsWith("--")) {
                        queryLine = "";
                    } else
                        queryLine = queryLine.substring(0, indexOfCommentSign);
                }
                // ignore comments surrounded by /* */
                indexOfCommentSign = queryLine.indexOf("/*");
                if (indexOfCommentSign != -1) {
                    if (queryLine.startsWith("#")) {
                        queryLine = "";
                    } else
                        queryLine = queryLine.substring(0, indexOfCommentSign);

                    sBuffer.append(queryLine + " ");
                    // ignore all characters within the comment
                    do {
                        queryLine = br.readLine();
                    }
                    while (queryLine != null && !queryLine.contains("*/"));
                    indexOfCommentSign = queryLine.indexOf("*/");
                    if (indexOfCommentSign != -1) {
                        if (queryLine.endsWith("*/")) {
                            queryLine = "";
                        } else
                            queryLine = (queryLine.substring(indexOfCommentSign + 2, queryLine.length() - 1));
                    }
                }

                //  the + " " is necessary, because otherwise the content before and after a line break are concatenated
                // like e.g. a.xyz FROM becomes a.xyzFROM otherwise and can not be executed
                if (queryLine != null)
                    sBuffer.append(queryLine + " ");
            }
            br.close();

            // here is our splitter ! We use ";" as a delimiter for each request
            String[] splittedQueries = sBuffer.toString().split(";");

            // filter out empty statements
            for (int i = 0; i < splittedQueries.length; i++) {
                if (!splittedQueries[i].trim().equals("") && !splittedQueries[i].trim().equals("\t")) {
                    listOfQueries.add(new String(splittedQueries[i]));
                }
            }
        } catch (Exception e) {
            System.out.println("*** Error : " + e.toString());
            System.out.println("*** ");
            System.out.println("*** Error : ");
            e.printStackTrace();
            System.out.println("################################################");
            System.out.println(sBuffer.toString());
        }
        return listOfQueries;
    }
}


