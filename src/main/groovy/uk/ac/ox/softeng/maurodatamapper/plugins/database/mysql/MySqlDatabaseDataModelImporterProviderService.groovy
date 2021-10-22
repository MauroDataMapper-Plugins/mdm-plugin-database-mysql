/*
 * Copyright 2020-2021 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package uk.ac.ox.softeng.maurodatamapper.plugins.database.mysql

import grails.util.Pair
import uk.ac.ox.softeng.maurodatamapper.api.exception.ApiException
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.facet.SummaryMetadata
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataElement
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.EnumerationType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.enumeration.EnumerationValue
import uk.ac.ox.softeng.maurodatamapper.plugins.database.AbstractDatabaseDataModelImporterProviderService
import uk.ac.ox.softeng.maurodatamapper.plugins.database.RemoteDatabaseDataModelImporterProviderService
import uk.ac.ox.softeng.maurodatamapper.plugins.database.SamplingStrategy
import uk.ac.ox.softeng.maurodatamapper.plugins.database.summarymetadata.AbstractIntervalHelper

import groovy.json.JsonOutput
import uk.ac.ox.softeng.maurodatamapper.plugins.database.summarymetadata.SummaryMetadataHelper
import uk.ac.ox.softeng.maurodatamapper.security.User
import uk.ac.ox.softeng.maurodatamapper.util.Utils

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.time.format.DateTimeFormatter

// @CompileStatic
class MySqlDatabaseDataModelImporterProviderService
    extends AbstractDatabaseDataModelImporterProviderService<MySqlDatabaseDataModelImporterProviderServiceParameters>
    implements RemoteDatabaseDataModelImporterProviderService {

    @Override
    String getDisplayName() {
        'MySQL Importer'
    }

    @Override
    String getVersion() {
        getClass().getPackage().getSpecificationVersion() ?: 'SNAPSHOT'
    }

    @Override
    Set<String> getKnownMetadataKeys() {
        ['character_maximum_length', 'character_octet_length', 'character_set_catalog', 'character_set_name', 'character_set_schema',
         'collation_catalog', 'collation_name', 'collation_schema', 'column_default', 'datetime_precision', 'domain_catalog', 'domain_name',
         'domain_schema', 'dtd_identifier', 'generation_expression', 'identity_cycle', 'identity_generation', 'identity_increment',
         'identity_maximum', 'identity_minimum', 'identity_start', 'interval_precision', 'interval_type', 'is_generated', 'is_identity',
         'is_nullable', 'is_self_referencing', 'is_updatable', 'maximum_cardinality', 'numeric_precision_radix', 'numeric_precision',
         'numeric_scale', 'ordinal_position', 'scope_catalog', 'scope_name', 'scope_schema', 'udt_catalog', 'udt_name', 'udt_schema',
         'primary_index[]', 'unique_index[]', 'index[]', 'unique[]', 'primary_key[]', 'foreign_key[]'] as Set<String>
    }

    @Override
    Boolean allowsExtraMetadataKeys() {
        true
    }

    @Override
    String getIndexInformationQueryString() {
        '''
       SELECT TABLE_NAME,
       INDEX_NAME,
       not(NON_UNIQUE) as unique_index,
       ( INDEX_NAME like 'primary') as primary_index,
       ( INDEX_NAME like 'primary') as clustered,
       GROUP_CONCAT(column_name ORDER BY seq_in_index) AS column_names
        FROM
            INFORMATION_SCHEMA.STATISTICS
        WHERE
            TABLE_SCHEMA = ?
        GROUP BY 1,2,3,4
        '''.stripIndent()
    }

    @Override
    String getForeignKeyInformationQueryString() {
        '''
        SELECT
          tc.constraint_name AS constraint_name,
          tc.table_name AS table_name,
          kcu.column_name AS column_name,
          kcu.referenced_table_name  AS reference_table_name,
          kcu.referenced_column_name AS reference_column_name
        FROM
          information_schema.table_constraints AS tc
          JOIN information_schema.key_column_usage AS kcu
            ON tc.constraint_name = kcu.constraint_name
        WHERE constraint_type = 'FOREIGN KEY' AND tc.constraint_schema = ?;
        '''.stripIndent()
    }

    @Override
    String getDatabaseStructureQueryString() {
        // In MySQL database and schema names are synonymous so we need can't just query by database
        null
    }

    @Override
    PreparedStatement prepareCoreStatement(Connection connection, MySqlDatabaseDataModelImporterProviderServiceParameters parameters) {
        // In MySQL database and schema names are synonymous so we need can't just query by database
        final List<String> names = parameters.databaseNames.split(',') as List<String>
        final PreparedStatement statement = connection.prepareStatement(
            "SELECT * FROM information_schema.columns WHERE table_schema IN (${names.collect {'?'}.join(',')});")
        names.eachWithIndex {String name, int i -> statement.setString(i + 1, name)}
        statement
    }

    @Override
    void addIndexInformation(DataModel dataModel, Connection connection) throws ApiException, SQLException {
        if (!indexInformationQueryString) return
        final List<Map<String, Object>> results = executePreparedStatement(dataModel, connection, indexInformationQueryString)
        results.groupBy {it.table_name}.each {tableName, rows ->
            final DataClass tableClass = dataModel.dataClasses.find {(it.label == tableName as String)}
            if (!tableClass) {
                log.warn 'Could not add indexes as DataClass for table {} does not exist', tableName
                return
            }

            List<Map> indexes = rows.collect {row ->
                [name          : (row.index_name as String).trim(),
                 columns       : (row.column_names as String).trim(),
                 primaryIndex  : getBooleanValue(row.primary_index),
                 uniqueIndex   : getBooleanValue(row.unique_index ),
                 clusteredIndex: getBooleanValue(row.clustered),
                ]
            }

            tableClass.addToMetadata(namespace, 'indexes', JsonOutput.prettyPrint(JsonOutput.toJson(indexes)), dataModel.createdBy)
        }
    }

    @Override
    void addForeignKeyInformation(DataModel dataModel, Connection connection) throws ApiException, SQLException {
        if (!foreignKeyInformationQueryString) return
        final List<Map<String, Object>> results = executePreparedStatement(dataModel, connection,
                                                                           foreignKeyInformationQueryString)

        results.each {Map<String, Object> row ->
            final DataClass tableClass = dataModel.dataClasses.find {(it.label == row.table_name as String)}
            final DataClass foreignTableClass = dataModel.dataClasses.find {DataClass dataClass -> dataClass.label == row.reference_table_name}
            DataType dataType

            if (foreignTableClass) {
                dataType = referenceTypeService.findOrCreateDataTypeForDataModel(
                    dataModel, "${foreignTableClass.label}Type", "Linked to DataElement [${row.reference_column_name}]",
                    dataModel.createdBy, foreignTableClass)
                dataModel.addToDataTypes dataType
            } else {
                dataType = primitiveTypeService.findOrCreateDataTypeForDataModel(
                    dataModel, "${row.reference_table_name}Type",
                    "Missing link to foreign key table [${row.reference_table_name}.${row.reference_column_name}]",
                    dataModel.createdBy)
            }

            final DataElement columnElement = tableClass.findDataElement(row.column_name as String)
            columnElement.dataType = dataType
            columnElement.addToMetadata(namespace, "foreign_key_name", row.constraint_name as String, dataModel.createdBy)
            columnElement.addToMetadata(namespace, "foreign_key_columns", row.reference_column_name as String, dataModel.createdBy)
        }

    }

   /**
    * The MySQL identifier quote character is the backtick (`). See
    * https://dev.mysql.com/doc/refman/8.0/en/identifiers.html
    */
    @Override
    String escapeIdentifier(String identifier) {
        "`${identifier}`"
    }

    @Override
    boolean isColumnPossibleEnumeration(DataType dataType) {
        dataType.domainType == 'PrimitiveType' && ["CHAR", "VARCHAR"].contains(dataType.label.toUpperCase())
    }

    @Override
    void updateDataModelWithEnumerationsAndSummaryMetadata(User user, MySqlDatabaseDataModelImporterProviderServiceParameters parameters, DataModel dataModel, Connection connection) {
        log.info('Starting enumeration and summary metedata detection')
        long startTime = System.currentTimeMillis()
        dataModel.childDataClasses.each { DataClass tableClass ->
                SamplingStrategy samplingStrategy = getSamplingStrategy(parameters)

                if (!samplingStrategy.canSample() && samplingStrategy.approxCount > samplingStrategy.threshold) {
                    log.info("Not calculating enumerations or summary metadata for ${samplingStrategy.tableType} ${tableClass.label} with approx rowcount ${samplingStrategy.approxCount} and threshold ${samplingStrategy.threshold}")
                } else {
                    tableClass.dataElements.each { DataElement de ->
                        DataType dt = de.dataType

                        //Enumeration detection
                        if (parameters.detectEnumerations && isColumnPossibleEnumeration(dt)) {
                            int countDistinct = getCountDistinctColumnValues(connection, samplingStrategy, de.label, tableClass.label)
                            if (countDistinct > 0 && countDistinct <= (parameters.maxEnumerations ?: MAX_ENUMERATIONS)) {
                                EnumerationType enumerationType = enumerationTypeService.findOrCreateDataTypeForDataModel(dataModel, de.label, de.label, user)

                                final List<Map<String, Object>> results = getDistinctColumnValues(connection, samplingStrategy, de.label, tableClass.label)

                                replacePrimitiveTypeWithEnumerationType(dataModel, de, dt, enumerationType, results)
                            }

                            if (parameters.calculateSummaryMetadata) {
                                //Count enumeration values
                                Map<String, Long> enumerationValueDistribution = getEnumerationValueDistribution(connection, samplingStrategy, de.label, tableClass.label)
                                if (enumerationValueDistribution) {
                                    String description = 'Enumeration Value Distribution'
                                    if (samplingStrategy.useSampling()) {
                                        description = "Estimated Enumeration Value Distribution (calculated by sampling ${samplingStrategy.percentage}% of rows)"
                                    }
                                    SummaryMetadata enumerationSummaryMetadata = SummaryMetadataHelper.createSummaryMetadataFromMap(user, de.label, description, enumerationValueDistribution)
                                    de.addToSummaryMetadata(enumerationSummaryMetadata);
                                }
                            }
                        }

                        //Summary metadata on dates and numbers
                        if (parameters.calculateSummaryMetadata && (isColumnForDateSummary(dt) || isColumnForDecimalSummary(dt) || isColumnForIntegerSummary(dt) || isColumnForLongSummary(dt))) {
                            Pair minMax = getMinMaxColumnValues(connection, samplingStrategy, de.label, tableClass.label)

                            //aValue is the MIN, bValue is the MAX. If they are not null then calculate the range etc...
                            if (!(minMax.aValue == null) && !(minMax.bValue == null)) {
                                AbstractIntervalHelper intervalHelper = getIntervalHelper(dt, minMax)

                                Map<String, Long> valueDistribution = getColumnRangeDistribution(connection, samplingStrategy, dt, intervalHelper, de.label, tableClass.label)
                                if (valueDistribution) {
                                    String description = 'Value Distribution';
                                    if (samplingStrategy.useSampling()) {
                                        description = "Estimated Value Distribution (calculated by sampling ${samplingStrategy.percentage}% of rows)"
                                    }
                                    SummaryMetadata summaryMetadata = SummaryMetadataHelper.createSummaryMetadataFromMap(user, de.label, description, valueDistribution)
                                    de.addToSummaryMetadata(summaryMetadata);
                                }
                            }
                        }
                    }
                }
        }

        log.info('Finished enumeration and summary metadata detection in {}', Utils.timeTaken(startTime))
    }

    @Override
    String enumerationValueDistributionQueryString(SamplingStrategy samplingStrategy,
                                                   String columnName,
                                                   String tableName,
                                                   String schemaName) {

        String sql = """
        SELECT ${escapeIdentifier(tableName)}.${escapeIdentifier(columnName)} AS enumeration_value,
        COUNT(*) AS enumeration_count
        FROM ${escapeIdentifier(tableName)} 
        ${samplingStrategy.samplingClause()}
        GROUP BY ${escapeIdentifier(tableName)}.${escapeIdentifier(columnName)}
        ORDER BY ${escapeIdentifier(tableName)}.${escapeIdentifier(columnName)}
        """

        sql.stripIndent()
    }

    @Override
    boolean isColumnForDateSummary(DataType dataType) {
        dataType.domainType == 'PrimitiveType' && ["DATE", "DATETIME", "TIMESTAMP"].contains(dataType.label.toUpperCase())
    }

    @Override
    boolean isColumnForDecimalSummary(DataType dataType) {
        dataType.domainType == 'PrimitiveType' && ["DECIMAL", "NUMERIC"].contains(dataType.label.toUpperCase())
    }

    @Override
    boolean isColumnForIntegerSummary(DataType dataType) {
        dataType.domainType == 'PrimitiveType' && ["INTEGER", "INT", "SMALLINT", "TINYINT", "MEDIUMINT"].contains(dataType.label.toUpperCase())
    }

    @Override
    boolean isColumnForLongSummary(DataType dataType) {
        dataType.domainType == 'PrimitiveType' && ["BIGINT"].contains(dataType.label.toUpperCase())
    }

    @Override
    String columnRangeDistributionQueryString(DataType dataType, AbstractIntervalHelper intervalHelper, String columnName, String tableName, String schemaName) {
        List<String> selects = intervalHelper.intervals.collect {
            "SELECT '${it.key}' AS interval_label, ${formatDataType(dataType, it.value.aValue)} AS interval_start, ${formatDataType(dataType, it.value.bValue)} AS interval_end "
        }

        rangeDistributionQueryString(selects, columnName, tableName)
    }

    /**
     * If dataType represents a date then return a string which uses the Oracle TO_DATE function to
     * convert the value to a date, otherwise just return the value as a string
     *
     * @param dataType
     * @param value
     * @return fragment of query like STR_TO_DATE('2020-08-15 23:18:00', '%Y-%m-%d %h:%i:%s')
     * or a string
     */
    String formatDataType(DataType dataType, Object value) {
        if (isColumnForDateSummary(dataType)){
            "STR_TO_DATE('${DateTimeFormatter.ISO_LOCAL_DATE.format(value)} ${DateTimeFormatter.ISO_LOCAL_TIME.format(value)}', '%Y-%m-%d %H:%i:%s')"
        } else {
            "${value}"
        }
    }

    /**
     * Returns a String that looks, for example, like this:
     * WITH mdm_interval AS (
     *   SELECT '0 - 100' AS interval_label, 0 AS interval_start, 100 AS interval_end
     *   UNION
     *   SELECT '100 - 200' AS interval_label, 100 AS interval_start, 200 AS interval_end
     * )
     * SELECT interval_label, COUNT(`my_column`) AS interval_count
     * FROM mdm_interval
     * LEFT JOIN
     * `my_table` ON `my_table`.`my_column` >= mdm_interval.interval_start
     * AND `my_table` ON `my_table`.`my_column`< mdm_interval.interval_end
     * GROUP BY interval_label, interval_start
     * ORDER BY interval_start ASC;
     *
     * @param tableName
     * @param columnName
     * @param selects
     * @return Query string for intervals, using Oracle SQL
     */
    private String rangeDistributionQueryString(List<String> selects, String columnName, String tableName) {
        String intervals = selects.join(" UNION ")

        String sql = "WITH mdm_interval AS (${intervals})" +
                """
        SELECT interval_label, COUNT(${escapeIdentifier(columnName)}) AS interval_count
        FROM mdm_interval
        LEFT JOIN
        ${escapeIdentifier(tableName)} 
        ON ${escapeIdentifier(tableName)}.${escapeIdentifier(columnName)} >= mdm_interval.interval_start 
        AND ${escapeIdentifier(tableName)}.${escapeIdentifier(columnName)} < mdm_interval.interval_end
        GROUP BY interval_label, interval_start
        ORDER BY interval_start ASC
        """

        sql.stripIndent()
    }

    List<Map<String, Object>> executePreparedStatement(DataModel dataModel, Connection connection,
                                                       String queryString) throws ApiException, SQLException {
        List<Map<String, Object>> results = null
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(queryString)
            preparedStatement.setString(1, dataModel.label)
            results = executeStatement(preparedStatement)
            preparedStatement.close()
        } catch (SQLException e) {
            if (e.message.contains('Invalid object name \'information_schema.table_constraints\'')) {
                log.warn 'No table_constraints available for {}', dataModel.label
            } else throw e
        }
        results
    }
}
