package org.fluffybunnyninjas.fireball;


import com.amazonaws.regions.Region;
import com.amazonaws.services.dynamodbv2.local.shared.access.LocalDBClient;
import com.amazonaws.services.dynamodbv2.local.shared.access.sqlite.SQLiteDBAccess;

import java.io.File;
import java.math.BigInteger;
import java.util.Random;

public class LocalDynamoDB extends LocalDBClient {

    private final File databaseFile;
    private final boolean deleteOnShutdown;

    public LocalDynamoDB() {
        this(new BigInteger(130, new Random()).toString(32), true);
    }

    public LocalDynamoDB(String filename, boolean deleteOnShutdown) {
        // TODO: Replace with H2 implementation
        super(new SQLiteDBAccess(filename));
        // Schedule removal of the temporary database
        this.databaseFile = new File(filename);
        this.deleteOnShutdown = deleteOnShutdown;
        if(deleteOnShutdown) {
            databaseFile.deleteOnExit();
        }
    }

    @Override
    public void setEndpoint(String endpoint) throws IllegalArgumentException {
        // LocalDBClient throws as error here, we just want to ignore it
    }

    @Override
    public void setRegion(Region region) throws IllegalArgumentException {
        // LocalDBClient throws as error here, we just want to ignore it
    }

    @Override
    public void shutdown() {
        super.shutdown();
        if(deleteOnShutdown) {
            databaseFile.delete();
        }
    }
}
