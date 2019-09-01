package dataset;

import config.ImproperProgramConfigurationException;
import config.ProgramConfiguration;
import database.DatabaseConnection;
import database.JOOQDatabaseInteractor;
import learning.testing.ExistenceTestCreationDecider;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.io.IOException;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by joschout.
 */
public class CategoricFieldUniqueValueCountController implements ExistenceTestCreationDecider {

    public static Set<Integer> fieldTypesTreatedAsCategorical;
    static {
        fieldTypesTreatedAsCategorical = new HashSet<>();
        fieldTypesTreatedAsCategorical.add(SQLDataType.VARCHAR.getSQLType());
        fieldTypesTreatedAsCategorical.add(SQLDataType.DATE.getSQLType());
    }

    public static DecimalFormat df = new DecimalFormat("##.###");
    static {
        df.setRoundingMode(RoundingMode.CEILING);
    }

    private Map<String, Map<String, Integer>> tableToFieldNameToCountDistinctMap;

    private Map<String, Integer> tableToNbOfRowsMap;

    private double relativeThreshold;
    private int absoluteThreshold;

    public JOOQDatabaseInteractor jooqDatabaseInteractor;

    public CategoricFieldUniqueValueCountController(JOOQDatabaseInteractor jooqDatabaseInteractor,
                                                    double relativeThreshold,
                                                    int absoluteThreshold) {
        this.jooqDatabaseInteractor = jooqDatabaseInteractor;
        this.tableToFieldNameToCountDistinctMap = new HashMap<>();
        this.tableToNbOfRowsMap = new HashMap<>();

        this.relativeThreshold = relativeThreshold;
        this.absoluteThreshold = absoluteThreshold;

        List<Table<?>> tables = jooqDatabaseInteractor.getTables();

        for (Table<?> table : tables) {
            this.add(table);
        }


    }

    private void addCount(Table table, Field categoricalField){
        Record1<Integer> integerRecord1 = jooqDatabaseInteractor.getDslContext()
                .select(DSL.countDistinct(categoricalField))
                .from(table)
                .fetchOne();

        int nbOfValues = (int) integerRecord1.get(0);

        String tableName = table.getName();
        Map<String, Integer> categoricalFieldNameToDistinctValueCountMap
                = tableToFieldNameToCountDistinctMap.computeIfAbsent(tableName, key -> new HashMap<>());
        String fieldName = categoricalField.getName();
        categoricalFieldNameToDistinctValueCountMap.put(fieldName, nbOfValues);
    }


    private void addCount(Table table, List<Field> multipleCategoricalFields){


        List<AggregateFunction> aggregateFunctions = new ArrayList<>(multipleCategoricalFields.size());
        for (Field categoricalField : multipleCategoricalFields) {
            aggregateFunctions.add(DSL.countDistinct(categoricalField));
        }


        SelectJoinStep<Record> query = jooqDatabaseInteractor.getDslContext()
                .select(aggregateFunctions)
                .from(table);
//        System.out.println(query.getSQL() + "\n");

        Record record = query
                .fetchAny();

//        System.out.println(record);

//        for (Record record : records) {
//            System.out.println(record);
//        }

        String tableName = table.getName();

        for (int i = 0; i < multipleCategoricalFields.size(); i++) {
            Field categoricalField = multipleCategoricalFields.get(i);
            int nbOfValues = (int) record.get(i);

            Map<String, Integer> categoricalFieldNameToDistinctValueCountMap
                    = tableToFieldNameToCountDistinctMap.computeIfAbsent(tableName, key -> new HashMap<>());
            String fieldName = categoricalField.getName();
            categoricalFieldNameToDistinctValueCountMap.put(fieldName, nbOfValues);
        }
    }

    @Override
    public String toString() {

        String str = "";

        for (String tableName : tableToFieldNameToCountDistinctMap.keySet()) {
            str += tableName + ":\n";
            Map<String, Integer> categoricalFieldNameToDistinctValueCountMap
                    = tableToFieldNameToCountDistinctMap.get(tableName);
            if(categoricalFieldNameToDistinctValueCountMap == null){
                throw new IllegalStateException("did not expect the map for table " + tableName + " to be null at this point");
            }

            int nbOfRows = tableToNbOfRowsMap.get(tableName);
            for (String categoricalFieldName : categoricalFieldNameToDistinctValueCountMap.keySet()) {
                int nbOfUniqueValues = categoricalFieldNameToDistinctValueCountMap.get(categoricalFieldName);

                str += "\t"+ categoricalFieldName + ": " + nbOfUniqueValues + " unique values in " + nbOfRows + " rows"
                        + " (" + df.format((double)nbOfUniqueValues / nbOfRows * 100.0) + "%), ";
            }

            str += "\n\n";

        }
        return str;
    }

    private void countNbOfRows(Table table){
        Record1<Integer> integerRecord1 = jooqDatabaseInteractor.getDslContext()
                .select(DSL.count())
                .from(table)
                .fetchOne();
        int nbOfRows = (int) integerRecord1.get(0);
        tableToNbOfRowsMap.put(table.getName(), nbOfRows);

    }



    public int getNbOfDistinctValues(Table table, Field field){
        String tableName = table.getName();
        String fieldName = field.getName();
        Map<String, Integer> categoricalFieldNameToDistinctValueCountMap
                = tableToFieldNameToCountDistinctMap.get(tableName);
        if(categoricalFieldNameToDistinctValueCountMap == null){
            throw new IllegalStateException("I do not have statistics for table "  + tableName);
        }
        Integer nbOfDistinctValues = categoricalFieldNameToDistinctValueCountMap.get(fieldName);
        if(nbOfDistinctValues == null){
            throw new IllegalStateException("I do not have statistics for field " + fieldName + " of table " + tableName);
        }
        return nbOfDistinctValues;

    }

    public int getNbOfRows(Table table){
        String tableName = table.getName();
        Integer nbOfRows = tableToNbOfRowsMap.get(tableName);
        if(nbOfRows == null){
            throw new IllegalStateException("I do not have a row count for table " + tableName);
        }
        return nbOfRows;
    }


    public double getDistinctnessFraction(Table table, Field field){

        int nbOfDistinctValues = this.getNbOfDistinctValues(table, field);
        int nbOfRows = getNbOfRows(table);

        return (double)nbOfDistinctValues/nbOfRows;
    }

    public boolean hasSmallRelativeFrequency(Table table, Field field){
        double distinctnessFraction = this.getDistinctnessFraction(table, field);
        return distinctnessFraction < this.relativeThreshold;
    }

    public boolean hasSmallAboluteFrequency(Table table, Field field){
        int nbOfDistinctValues = this.getNbOfDistinctValues(table, field);
        return nbOfDistinctValues < this.absoluteThreshold;

    }


    public boolean shouldCreateExistenceTestForField(Table table, Field field){
        return hasSmallRelativeFrequency(table, field) && hasSmallAboluteFrequency(table, field);
    }





    public static boolean shouldBeTreatedAsCategorical(Field field){

        return fieldTypesTreatedAsCategorical.contains(
                field.getDataType().getSQLType());
//        return field.getDataType().getSQLType()== SQLDataType.VARCHAR.getSQLType();
    }

    private void add(Table table){
        Field[] fields = table.fields();
        List<Field> categoricalFields = new ArrayList<>();

        for (Field field : fields) {
            if(shouldBeTreatedAsCategorical(field)){
                categoricalFields.add(field);
            }
        }

        if(! categoricalFields.isEmpty()){
            this.addCount(table, categoricalFields);
            this.countNbOfRows(table);
        }
    }


    public static void main(String[] args) {
        String propertiesPath = "data/webkb/config.properties";

        Logger logger = Logger.getLogger("m");


        try {
            ProgramConfiguration programConfiguration = new ProgramConfiguration(propertiesPath);
            Connection databaseConnection = DatabaseConnection.getDatabaseConnection(programConfiguration);

            JOOQDatabaseInteractor jooqDatabaseInteractor = new JOOQDatabaseInteractor(
                    databaseConnection,
                    programConfiguration
            );

            double relativeThreshold = 0.2;
            int absoluteThreshold = 40;
            CategoricFieldUniqueValueCountController controller
                    = new CategoricFieldUniqueValueCountController(jooqDatabaseInteractor, relativeThreshold, absoluteThreshold);

            System.out.println(controller.toString());

            List<Table<?>> tables = jooqDatabaseInteractor.getTables();
            for (Table<?> table : tables) {
                Field[] fields = table.fields();
                System.out.println(table.getName());
                for (Field field : fields) {
                    if(shouldBeTreatedAsCategorical(field)){
                        boolean shouldFieldBeUsedForExistenceTesting = controller.shouldCreateExistenceTestForField(table, field);
                        if(shouldFieldBeUsedForExistenceTesting){
                            System.out.println("\t" + field.getName());
                        }

                    }
                }

            }


        } catch (ImproperProgramConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
