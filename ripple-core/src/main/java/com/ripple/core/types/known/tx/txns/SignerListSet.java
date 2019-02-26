package com.ripple.core.types.known.tx.txns;

import com.ripple.core.coretypes.STArray;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.fields.Field;
import com.ripple.core.serialized.enums.TransactionType;
import com.ripple.core.types.known.tx.Transaction;

/**
 * Created by ajax.wang on 2/21/2019.
 */
public class SignerListSet extends Transaction {

    public SignerListSet() {
        super(TransactionType.SignerListSet);
    }

    public UInt32 signerQuorum() {
        return get(UInt32.SignerQuorum);
    }

    public void signerQuorum(UInt32 val) {
        put(Field.SignerQuorum, val);
    }

    public STArray signerEntries() {
        return get(STArray.SignerEntries);
    }

    public void signerEntries(STArray val) {
        put(Field.SignerEntries, val);
    }

}
