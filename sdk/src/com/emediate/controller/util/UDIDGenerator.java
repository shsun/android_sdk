package com.emediate.controller.util;

/**
 * Class which generates a unique UDID which can be used as an user-id when
 * making requests to the Ad-Service.
 * 
 * @author Fredrik Hyttnäs-Lenngren
 * 
 */
public class UDIDGenerator {

    /**
     * Generate a new UDID
     * 
     * @return the udid
     */
    public final String generateUDID() {
	final long unixTimestamp = System.currentTimeMillis() / 1000l;

	return unixTimestamp + "" + getRandomNumber(100000000, 999999999);
    }

    /**
     * Generate a random number between <code>min</code> (inclusive) and
     * <code>max</code> (exclusive)
     * 
     * @param min
     *            minimum (inclusive)
     * @param max
     *            maximum (exclusive)
     * @return the random number
     */
    public int getRandomNumber(int min, int max) {
	return (int) Math.floor(Math.random() * (max - min + 1)) + min;
    }
}
