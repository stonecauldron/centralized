package centralized;

/**
 * Created by noodle on 05.11.16.
 * simple container that store 2 values as a Pair
 */
public class Pair<T,V> {

    private T t;
    private V v;

    public Pair(T t, V v){
        this.t = t;
        this.v = v;
    }

    public T _1(){
        return t;
    }

    public V _2(){
        return v;
    }

}
