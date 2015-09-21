/*
 * Copyright (c) 2015, Spiideo
 */

package se.raneland.fireball

import com.amazonaws.services.dynamodbv2.model.AttributeDefinition
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.KeyType
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType

/**
 * Created by raniz on 2015-09-21.
 */
class FireballSpecification extends spock.lang.Specification {

    def fireball = new Fireball()

    def "That a database can be created"() {
        when: "A table is created and then all tables are listed"
        fireball.createTable(
                [new AttributeDefinition("hash", ScalarAttributeType.S),
                 new AttributeDefinition("range", ScalarAttributeType.N)],
                "test-table",
                [new KeySchemaElement("hash", KeyType.HASH),
                 new KeySchemaElement("range", KeyType.RANGE)],
                new ProvisionedThroughput(5, 5)
        )
        def listing = fireball.listTables()

        then: "The created table is in the list"
        listing.tableNames == ["test-table"]
    }

    def "That a created database can be described"() {
        given: "A table"
        fireball.createTable(
                [new AttributeDefinition("hash", ScalarAttributeType.S),
                 new AttributeDefinition("range", ScalarAttributeType.N)],
                "test-table",
                [new KeySchemaElement("hash", KeyType.HASH),
                 new KeySchemaElement("range", KeyType.RANGE)],
                new ProvisionedThroughput(5, 5)
        )
        when: "The table is described"
        def table = fireball.describeTable("test-table")?.table

        then: "The created table is in the list"
        table
        table.tableName == "test-table"
        table.attributeDefinitions == [new AttributeDefinition("hash", ScalarAttributeType.S),
            new AttributeDefinition("range", ScalarAttributeType.N)]
        table.keySchema == [new KeySchemaElement("hash", KeyType.HASH),
                 new KeySchemaElement("range", KeyType.RANGE)]
        // We don't care about provisioned throughput since DynamoDBLocal doesn't respect it
    }
}
