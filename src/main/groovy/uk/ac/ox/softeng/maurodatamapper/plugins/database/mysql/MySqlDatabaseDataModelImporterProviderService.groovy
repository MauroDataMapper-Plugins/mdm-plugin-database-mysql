/*
 * Copyright 2020 University of Oxford
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

import uk.ac.ox.softeng.maurodatamapper.plugins.database.AbstractDatabaseDataModelImporterProviderService
import uk.ac.ox.softeng.maurodatamapper.plugins.database.RemoteDatabaseDataModelImporterProviderService

import java.sql.Connection
import java.sql.PreparedStatement

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
        '1.0.0-SNAPSHOT'
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
        SELECT
          table_name,
          index_name,
          unique_index,
          primary_index,
          clustered,
          array_to_string(array_agg(column_name), ', ') AS column_names
        FROM (
               SELECT
                 t.relname         AS table_name,
                 i.relname         AS index_name,
                 a.attname         AS column_name,
                 ix.indisunique    AS unique_index,
                 ix.indisprimary   AS primary_index,
                 ix.indisclustered AS clustered,
                 unnest(ix.indkey) AS unn,
                 a.attnum
               FROM pg_catalog.pg_index ix
                 LEFT JOIN pg_catalog.pg_class t ON ix.indrelid = t.oid
                 LEFT JOIN pg_catalog.pg_class i ON ix.indexrelid = i.oid
                 LEFT JOIN pg_catalog.pg_attribute a ON (t.oid = a.attrelid AND a.attnum = ANY (ix.indkey))
                 LEFT JOIN pg_catalog.pg_namespace ns ON t.relnamespace = ns.oid
               WHERE ns.nspname = ?
               ORDER BY
                 t.relname,
                 i.relname,
                 generate_subscripts(ix.indkey, 1)) sb
        WHERE unn = attnum
        GROUP BY table_name, index_name, unique_index, primary_index, clustered;
        '''.stripIndent()
    }

    @Override
    String getForeignKeyInformationQueryString() {
        '''
        SELECT
          tc.constraint_name,
          tc.table_name,
          kcu.column_name,
          ccu.table_name  AS reference_table_name,
          ccu.column_name AS reference_column_name
        FROM
          information_schema.table_constraints AS tc
          JOIN information_schema.key_column_usage AS kcu
            ON tc.constraint_name = kcu.constraint_name
          JOIN information_schema.constraint_column_usage AS ccu
            ON ccu.constraint_name = tc.constraint_name
        WHERE constraint_type = 'FOREIGN KEY' AND tc.constraint_schema = ?;
        '''.stripIndent()
    }

    @Override
    String getDatabaseStructureQueryString() {
        '''
        SELECT *
        FROM information_schema.columns
        WHERE table_schema NOT IN ('pg_catalog','information_schema');
        '''.stripIndent()
    }

    @Override
    PreparedStatement prepareCoreStatement(Connection connection, MySqlDatabaseDataModelImporterProviderServiceParameters parameters) {
        if (!parameters.schemaNames) return super.prepareCoreStatement(connection, parameters)
        final List<String> names = parameters.schemaNames.split(',') as List<String>
        final PreparedStatement statement = connection.prepareStatement(
            """SELECT * FROM information_schema.columns WHERE table_schema IN (${names.collect {'?'}.join(',')});""")
        names.eachWithIndex {String name, int i -> statement.setString(i + 1, name)}
        statement
    }
}
