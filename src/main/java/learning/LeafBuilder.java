package learning;

import dataset.Example;

import java.util.List;

public class LeafBuilder {

    public NodeInfo build(List<Example> instances){
        return new NodeInfo(instances);
    }
}
