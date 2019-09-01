package learning.testing;

import org.jooq.Field;
import org.jooq.Table;

/**
 * Created by joschout.
 */
public interface ExistenceTestCreationDecider {

    public boolean shouldCreateExistenceTestForField(Table table, Field field);

}
