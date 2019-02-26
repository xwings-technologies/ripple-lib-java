package com.ripple.core.types.known.tx.signed;

import com.ripple.core.coretypes.*;
import com.ripple.core.coretypes.hash.HalfSha512;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.HashPrefix;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.fields.Field;
import com.ripple.core.serialized.BytesList;
import com.ripple.core.serialized.MultiSink;
import com.ripple.core.serialized.enums.TransactionType;
import com.ripple.core.types.known.tx.Transaction;
import com.ripple.crypto.ecdsa.IKeyPair;
import com.ripple.crypto.ecdsa.Seed;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class SignedTransaction {
    private SignedTransaction(Transaction of) {
        // TODO: is this just over kill ?
        txn = (Transaction) STObject.translate.fromBytes(of.toBytes());
    }

    // This will eventually be private
    @Deprecated
    public SignedTransaction() {}

    public Transaction txn;
    public Hash256 hash;

    public byte[] signingData;
    public byte[] previousSigningData;
    public String tx_blob;


    public static SignedTransaction fromTx(Transaction tx) {
        return new SignedTransaction(tx);
    }

    public void sign(String base58Secret) {
        sign(Seed.fromBase58(base58Secret).keyPair());
    }

    public void sign(IKeyPair keyPair) {
        prepare(keyPair, null, null, null);
    }

    public void signFor(String account, String base58Secret) {
        signFor(account, Seed.fromBase58(base58Secret).keyPair());
    }

    public void signFor(String account, IKeyPair keyPair) {
        prepareFor(account, keyPair);
    }

    public void prepare(IKeyPair keyPair,
                        Amount fee,
                        UInt32 Sequence,
                        UInt32 lastLedgerSequence) {

        Blob pubKey = new Blob(keyPair.canonicalPubBytes());

        // This won't always be specified
        if (lastLedgerSequence != null) {
            txn.put(UInt32.LastLedgerSequence, lastLedgerSequence);
        }
        if (Sequence != null) {
            txn.put(UInt32.Sequence, Sequence);
        }
        if (fee != null) {
            txn.put(Amount.Fee, fee);
        }

        txn.signingPubKey(pubKey);

        if (Transaction.CANONICAL_FLAG_DEPLOYED) {
            txn.setCanonicalSignatureFlag();
        }

        txn.checkFormat();
        signingData = txn.signingData();
        if (previousSigningData != null && Arrays.equals(signingData, previousSigningData)) {
            return;
        }
        try {
            txn.txnSignature(new Blob(keyPair.signMessage(signingData)));

            BytesList blob = new BytesList();
            HalfSha512 id = HalfSha512.prefixed256(HashPrefix.transactionID);

            txn.toBytesSink(new MultiSink(blob, id));
            tx_blob = blob.bytesHex();
            hash = id.finish();
        } catch (Exception e) {
            // electric paranoia
            previousSigningData = null;
            throw new RuntimeException(e);
        } /*else {*/
        previousSigningData = signingData;
        // }
    }

    public void prepareFor(String account, IKeyPair keyPair) {
        AccountID newAccount = AccountID.fromAddress(account);
        Blob pubKey = new Blob(keyPair.canonicalPubBytes());

        txn.signingPubKey(Blob.fromBytes("".getBytes()));

        txn.checkFormat();
        signingData = txn.signingForData(newAccount);
        if (previousSigningData != null && Arrays.equals(signingData, previousSigningData)) {
            return;
        }
        try {
            Blob txnSignature = new Blob(keyPair.signMessage(signingData));

            STObject signer = new STObject();
            signer.put(AccountID.Account, newAccount);
            signer.put(Blob.SigningPubKey, pubKey);
            signer.put(Blob.TxnSignature, txnSignature);
            STObject signerWrapper = new STObject();
            signerWrapper.put(STObject.Signer, signer);
            STArray signers = txn.get(STArray.Signers);
            if (signers == null) {
                signers = new STArray();
                txn.put(STArray.Signers, signers);
            }
            signers.add(signerWrapper);
            Collections.sort(signers, new Comparator<STObject>() {
                @Override
                public int compare(STObject o1, STObject o2) {
                    AccountID a1 = o1.get(STObject.Signer).get(AccountID.Account);
                    AccountID a2 = o2.get(STObject.Signer).get(AccountID.Account);

                    return a1.compareTo(a2);
                }
            });

            BytesList blob = new BytesList();
            HalfSha512 id = HalfSha512.prefixed256(HashPrefix.transactionID);

            txn.toBytesSink(new MultiSink(blob, id));
            tx_blob = blob.bytesHex();
            hash = id.finish();
            txn.put(Field.hash, hash);
        } catch (Exception e) {
            // electric paranoia
            previousSigningData = null;
            throw new RuntimeException(e);
        } /*else {*/
        previousSigningData = signingData;
        // }
    }

    public TransactionType transactionType() {
        return txn.transactionType();
    }
}
