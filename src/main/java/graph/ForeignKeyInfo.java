package graph;

import com.google.common.base.Objects;
import config.ImproperProgramConfigurationException;
import config.ProgramConfiguration;
import database.DatabaseConnection;
import database.JOOQDatabaseInteractor;
import org.jgrapht.Graph;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ForeignKeyInfo extends KeyInfo {


    private static final long serialVersionUID = 7191257799255606876L;
    public KeyInfo primaryKey;

//    private ForeignKeyInfo(){
//        super();
//    }

    public ForeignKeyInfo(ForeignKey foreignKey){
        super(foreignKey);

        this.primaryKey = new KeyInfo(foreignKey.getKey());
    }

    public KeyInfo getKey(){
        return primaryKey;
    }



    public Condition getJoinCondition() throws InvalidKeyInfoException {
        List<Field> primaryKeyFields = this.getKey().getFields();
        List<Field> foreignKeyFields = this.getFields();

        if(primaryKeyFields.size() != foreignKeyFields.size()){
            throw new InvalidKeyInfoException("The ForeignKeyInfo object does not contain an equal amount of fields for the foreign and primary keys");
        }

        Map<Field<?>, Field<?>> fieldMap = new HashMap<>();
        for (int i = 0; i < primaryKeyFields.size(); i++) {
            fieldMap.put(primaryKeyFields.get(i), foreignKeyFields.get(i));
        }
        return DSL.condition(fieldMap);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ForeignKeyInfo that = (ForeignKeyInfo) o;
        return Objects.equal(primaryKey, that.primaryKey);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), primaryKey);
    }

    public static void main(String[] args) {
//        String propertiesPath = args[0];
        String propertiesPath = "data/uw_cse/config.properties";

        Logger logger = Logger.getLogger("m");


        try {
            ProgramConfiguration programConfiguration = new ProgramConfiguration(propertiesPath);
            Connection databaseConnection = DatabaseConnection.getDatabaseConnection(programConfiguration);

            JOOQDatabaseInteractor jooqDatabaseInteractor = new JOOQDatabaseInteractor(
                    databaseConnection,
                    programConfiguration
            );

            // create the relational graph. This is a DIRECTED graph
            Graph<String, ForeignKeyEdge> relationalGraph = TableGraph.createFrom(jooqDatabaseInteractor.getTables());
            System.out.println(TableGraph.asDOTString(relationalGraph));


//            for (ForeignKeyEdge fkEdge : relationalGraph.edgeSet()) {
//                ForeignKey foreignKey = fkEdge.getForeignKey();
//
//                ForeignKeyInfo foreignKeyInfo = new ForeignKeyInfo(foreignKey);
//
//                printOutEqualityTest("getName() equality", foreignKey.getName(), foreignKeyInfo.getName());
//                printOutEqualityTest("tableName eq", foreignKey.getTable().getName(), foreignKeyInfo.getUnqualifiedTableName());
//
//                printOutEqualityTest("tableName qualified eq", foreignKey.getTable().getQualifiedName(), foreignKeyInfo.getQualifiedTableName());
//
//
//                Table tableName = foreignKeyInfo.getTable();
//                System.out.println(tableName);
//
//
//
//                List<TableField> fkFields = (List<TableField>) foreignKey.getFields();
//                String[] fkiUnqualifiedFieldNames = foreignKeyInfo.getUnqualifiedFieldNames();
//
//
//                for (int i = 0; i < fkFields.size(); i++) {
//
//                    Field fkField = fkFields.get(i);
//
//                    printOutEqualityTest("fieldName eq", fkField.getName(), fkiUnqualifiedFieldNames[i]);
//
//                }
//
//
//
//
//            }



        } catch (ImproperProgramConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    private static void printOutEqualityTest(String testName, Object valueFK, Object valueFKC){
        System.out.println(testName +": " + valueFK.equals(valueFKC) +" " + valueFK.toString() + " " + valueFKC.toString());
    }

}
