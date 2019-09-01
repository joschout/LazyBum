package database;

import org.jooq.SQLDialect;

public class DialectManager {

    public static SQLDialect parse(String dialectString){
        String lowerCaseDialectString = dialectString.toLowerCase();
        if(lowerCaseDialectString.startsWith("postgres")){
            return SQLDialect.POSTGRES;
        }
        if(lowerCaseDialectString.startsWith("mysql")){
            return SQLDialect.MYSQL;
        }

        throw new IllegalArgumentException("Unsupported sql dialect: " + dialectString);
    }
}
