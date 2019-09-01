package learning.testing;

import org.jooq.Field;
import org.jooq.Table;

/**
 * Created by joschout.
 */
public class DefaultExistenceTestCreationDecider implements ExistenceTestCreationDecider {


    private boolean defaultDecision;

    public DefaultExistenceTestCreationDecider(boolean defaultDecision) {
        this.defaultDecision = defaultDecision;
    }

    @Override
    public boolean shouldCreateExistenceTestForField(Table table, Field field) {
        return this.defaultDecision;
    }
}
