package utils;

import java.util.ArrayList;
import java.util.List;

public class NameGenerator {
    
    private String nameStem;
    private int counter;
    private final int initalCounterValue;


    public NameGenerator(String nameStem, int initialCounterValue) {
        this.nameStem = nameStem;
        this.counter = initialCounterValue;
        this.initalCounterValue = initialCounterValue;
    }

    public NameGenerator() {
        this("name", 0);
    }

    public String getNewName(){
        String newName = nameStem + counter;
        counter++;
        return newName;
    }

    public List<String> getAllGeneratedNames(){
        List<String> allGeneratedNames = new ArrayList<>();

        for (int i = initalCounterValue; i < counter; i++) {
            String generatedName = nameStem + i;
            allGeneratedNames.add(generatedName);
        }
        return allGeneratedNames;
    }
}
