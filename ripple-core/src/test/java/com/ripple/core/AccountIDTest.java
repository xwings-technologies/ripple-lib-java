package com.ripple.core;

import com.ripple.core.coretypes.AccountID;
import com.ripple.crypto.ecdsa.IKeyPair;
import com.ripple.crypto.ecdsa.Seed;
import com.ripple.encodings.base58.EncodingFormatException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AccountIDTest {

    private String randomXqvWyhPcWjBE7nawXLTKH5YLNmSc = "randomXqvWyhPcWjBE7nawXLTKH5YLNmSc";

    @Test
    public void testAddress() {
        AccountID account = AccountID.fromSeedString(TestFixtures.master_seed);
        assertEquals(TestFixtures.master_seed_address, account.address);

        account = AccountID.fromSeedString("saNaxmLMGZcCeyaAGkMK4EJAJC4By");
        assertEquals("rshBYF3tBD5Sy76yr7izy7ePSwB6t6zi8e", account.address);

        IKeyPair keyPair = Seed.fromBase58("ssDnkh5Gbfp5ELmAMzzEKAobXgbYG").keyPair();
        assertEquals("03F4B50B1D51A1EBA90A0FAF0ABF7EF1E37E21FC3371C773D2EC55731151A39371", keyPair.pubHex());
    }

    @Test
    public void testBlackHoleAddy() {
        AccountID.fromAddress(randomXqvWyhPcWjBE7nawXLTKH5YLNmSc);
    }

    @Test(expected = EncodingFormatException.class)
    public void testBlackHoleAddyCheckSumFail() {
        AccountID.fromAddress("R" + randomXqvWyhPcWjBE7nawXLTKH5YLNmSc.substring(1));
    }

    @Test
    public void testHashCode() {
        AccountID a1 = AccountID.fromAddress(randomXqvWyhPcWjBE7nawXLTKH5YLNmSc);
        AccountID a2 = AccountID.fromAddress(randomXqvWyhPcWjBE7nawXLTKH5YLNmSc);
        assertEquals(a1.hashCode(), a2.hashCode());
    }
}
