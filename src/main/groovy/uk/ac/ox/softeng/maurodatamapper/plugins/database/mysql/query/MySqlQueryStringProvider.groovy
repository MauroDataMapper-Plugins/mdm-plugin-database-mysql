/*
 * Copyright 2020-2022 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
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
package uk.ac.ox.softeng.maurodatamapper.plugins.database.mysql.query

import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.plugins.database.calculation.SamplingStrategy
import uk.ac.ox.softeng.maurodatamapper.plugins.database.query.QueryStringProvider
import uk.ac.ox.softeng.maurodatamapper.plugins.database.summarymetadata.AbstractIntervalHelper

import java.time.format.DateTimeFormatter

/**
 * @since 25/04/2022
 */
class MySqlQueryStringProvider extends QueryStringProvider {

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
    String columnRangeDistributionQueryString(SamplingStrategy samplingStrategy, DataType dataType, AbstractIntervalHelper intervalHelper, String columnName, String tableName,
                                              String schemaName) {
        List<String> selects = intervalHelper.intervals.collect {
            "SELECT '${it.key}' AS interval_label, ${formatDataType(dataType, it.value.aValue)} AS interval_start, ${formatDataType(dataType, it.value.bValue)} AS " +
            "interval_end "
        }

        rangeDistributionQueryString(selects, columnName, tableName)
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

    /**
     * The MySQL identifier quote character is the backtick (`). See
     * https://dev.mysql.com/doc/refman/8.0/en/identifiers.html
     */
    @Override
    String escapeIdentifier(String identifier) {
        "`${identifier}`"
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
        if (isColumnForDateSummary(dataType)) {
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

    boolean isColumnForDateSummary(DataType dataType) {
        dataType.domainType == 'PrimitiveType' && ["DATE", "DATETIME", "TIMESTAMP"].contains(dataType.label.toUpperCase())
    }
}
