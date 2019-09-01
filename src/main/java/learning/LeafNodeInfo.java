package learning;

import dataset.Example;

import java.io.Serializable;
import java.util.List;

public abstract class LeafNodeInfo extends NodeInfo implements Serializable {

    private static final long serialVersionUID = -6612797297411829981L;

    public LeafNodeInfo(){
        super();
    }

    public LeafNodeInfo(List<Example> instances) {
        super(instances);
    }

    public abstract Prediction predict();

}
