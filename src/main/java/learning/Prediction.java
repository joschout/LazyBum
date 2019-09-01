package learning;

import com.google.common.base.Objects;

import java.io.Serializable;

public class Prediction implements Serializable {

    private static final long serialVersionUID = -4824818673316792797L;
    public Object value;

    public Prediction(Object value) {
        this.value = value;
    }

    public String toString(){
        if(value != null){
            return value.toString();
        }
        return "NULL";
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prediction that = (Prediction) o;
        return Objects.equal(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
