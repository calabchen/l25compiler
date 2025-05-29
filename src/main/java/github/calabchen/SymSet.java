package github.calabchen;

import java.util.BitSet;


public class SymSet extends BitSet {

    private static final long serialVersionUID = 8136959240158320958L;

    public SymSet(int nbits) {
        super(nbits);
    }

    public void set(Symbol s) {
        set(s.ordinal());
    }

    public boolean get(Symbol s) {
        return get(s.ordinal());
    }
}
