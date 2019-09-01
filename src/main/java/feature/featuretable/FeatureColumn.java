package feature.featuretable;

import feature.tranform.FieldTransformerEnumInterface;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FeatureColumn<T> {


    private Field field;

    private FieldTransformerEnumInterface transformation;

    private List<T> featureValuePerInstance;

    public FeatureColumn(Field field, FieldTransformerEnumInterface transformation){
        this.field = field;
        this.transformation = transformation;
        featureValuePerInstance = new ArrayList<>();
    }

    public void add(T featureValue){



        featureValuePerInstance.add(featureValue);
    }

    public Optional<FieldTransformerEnumInterface> getTransformation() {
        return Optional.ofNullable(transformation);
    }

    public Field getOriginalField() {
        return field;
    }

    public List<T> getFeatureValuePerInstance() {
        return featureValuePerInstance;
    }

    public T getFeatureValueForInstance(int instanceIndex){
        return featureValuePerInstance.get(instanceIndex);
    }

    public int getNbOfFeatureValues(){
        return featureValuePerInstance.size();
    }

    public int maxStringLength(){
        int max = 0;
        for(T featureValue: featureValuePerInstance){
            int strLength = "null".length();
            if(featureValue != null){
                strLength = featureValue.toString().length();
            }
            if(strLength > max){
                max = strLength;
            }
        }
        return max;
    }


    public String getName(){
        if(transformation != null){
            return transformation.toString() +"_" + field.getName();
        } else{
            return field.getName();
        }
    }

    public Field getAsField(){
        if(transformation != null){
            String fieldName = this.getName().replace(" ", "_").replace("\u2203", "EXISTS");

            return DSL.field(fieldName, this.transformation.getSqlDataType());



//            return DSL.field(this.getName(), this.transformation.getSqlDataType());
        } else{
            return field;
        }
    }


}
