package database;

import config.ProgramConfiguration;
import config.ProgramConfigurationOption;
import org.jooq.*;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.util.List;

public class JOOQDatabaseInteractor {


    private ProgramConfiguration programConfiguration;

    private DSLContext dslContext;
    private Schema schema;

    public JOOQDatabaseInteractor(Connection databaseConnection, ProgramConfiguration programConfiguration) {

        dslContext = DSL.using(
                databaseConnection,
                DialectManager.parse(programConfiguration.getConfigurationOption(ProgramConfigurationOption.SQL_DIALECT)),
                new Settings()
                        .withRenderFormatted(true)
                        .withAttachRecords(false)
        );

        //TODO: https://stackoverflow.com/questions/29919830/jooq-add-schema-name-to-all-tables

        refreshSchema(programConfiguration);
        this.programConfiguration = programConfiguration;
    }


    public Schema refreshSchema(ProgramConfiguration programConfiguration){
        Meta meta = dslContext.meta();

        Catalog catalog = meta.getCatalog(
                programConfiguration.getConfigurationOption(ProgramConfigurationOption.CATALOG)
        );
        schema = catalog.getSchema(
            programConfiguration.getConfigurationOption(ProgramConfigurationOption.SCHEMA)
        );
        return schema;
    }

    public Schema refreshSchema(){
        return refreshSchema(programConfiguration);
    }


    public List<Table<?>> getTables(){
        return schema.getTables();
    }


    public Table getTableByName(String name){
        return schema.getTable(name);
    }

    public DSLContext getDslContext() {
        return dslContext;
    }

    public Schema getSchema() {
        return schema;
    }

    public Table getTableRepresentationWithSchemaQualifiedName(String tableName){
        return DSL.table(DSL.name(schema.getName(), tableName));
    }
}
