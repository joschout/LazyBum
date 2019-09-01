package learning.testing;

import java.util.Iterator;

public abstract class TestGenerator implements Iterator<NodeTest> {



    public static boolean VERBOSE = false;

//    public TestGenerator(){
//        System.out.println("ONLY FOR TESTING PURPOSES, NOT TO BE USED IN PRODUCTION");
//    }


    public abstract boolean hasNext();

    public abstract NodeTest next();




}
