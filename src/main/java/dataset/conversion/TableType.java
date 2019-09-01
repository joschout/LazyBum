package dataset.conversion;

/**
 * Created by joschout.
 */
public enum TableType {
    TARGET_TABLE("targettable.arff"), ONEBM_TABLE("onebmtable.arff");

    private final String filename;

    TableType(String filename) {
        this.filename = filename;
    }

    public String getARFFFileName() {
        return filename;
    }

    @Override
    public String toString(){
        switch (this){
            case ONEBM_TABLE:
                return "onebmtable";
            case TARGET_TABLE:
                return "targettable";
            default:
                return "undefined_option";
        }
    }

}
