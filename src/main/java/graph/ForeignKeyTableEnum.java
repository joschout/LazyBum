package graph;

import java.io.Serializable;

public enum ForeignKeyTableEnum implements Serializable {
    FOREIGN_KEY_TABLE, PRIMARY_KEY_TABLE;


    public static ForeignKeyTableEnum other(ForeignKeyTableEnum foreignKeyTableEnum){
        if(foreignKeyTableEnum == ForeignKeyTableEnum.FOREIGN_KEY_TABLE){
            return ForeignKeyTableEnum.PRIMARY_KEY_TABLE;
        } else{
            return ForeignKeyTableEnum.FOREIGN_KEY_TABLE;
        }
    }


}
