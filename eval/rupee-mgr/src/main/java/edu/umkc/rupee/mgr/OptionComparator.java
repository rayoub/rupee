package edu.umkc.rupee.mgr;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.Option;

public class OptionComparator implements Comparator<Option> {
 
    private static final Map<String, Integer> _map = new HashMap<String, Integer>();
    
    static {
        _map.put("i",1);
        _map.put("h",2);
        _map.put("a",3);
        _map.put("t",4);
        _map.put("f",5);
        _map.put("c",6);
        _map.put("s",7);
        _map.put("u",8);
        _map.put("d",9);
        _map.put("?",10);
    }

    public int compare(Option x, Option y) {
        return Integer.compare(_map.get(x.getOpt()), _map.get(y.getOpt()));
    }
}
