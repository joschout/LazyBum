package lazybum.main.setup;

import lazybum.ExtendEndingTablesUsedInDTTestStrategy;
import lazybum.LazyOneBMExtensionStrategy;
import lazybum.NeverExtendStrategy;
import lazybum.TraversalGraphExtensionStrategy;

public enum TraversalGraphExtensionStrategyEnum {
    LAZY_ONEBM, DTTEST_BASED, NEVER_EXTEND;

    public TraversalGraphExtensionStrategy getStrategy(){
        switch (this) {
            case LAZY_ONEBM:
                return new LazyOneBMExtensionStrategy();
            case DTTEST_BASED:
                return new ExtendEndingTablesUsedInDTTestStrategy();
            case NEVER_EXTEND:
                return new NeverExtendStrategy();
        }
        return null;
    }

}
