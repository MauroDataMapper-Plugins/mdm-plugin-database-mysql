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
package uk.ac.ox.softeng.maurodatamapper.plugins.database.mysql

import uk.ac.ox.softeng.maurodatamapper.api.exception.ApiException
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.provider.DefaultDataTypeProvider
import uk.ac.ox.softeng.maurodatamapper.plugins.database.AbstractDatabaseDataModelImporterProviderService
import uk.ac.ox.softeng.maurodatamapper.plugins.database.RemoteDatabaseDataModelImporterProviderService
import uk.ac.ox.softeng.maurodatamapper.plugins.database.calculation.CalculationStrategy
import uk.ac.ox.softeng.maurodatamapper.plugins.database.calculation.SamplingStrategy
import uk.ac.ox.softeng.maurodatamapper.plugins.database.mysql.calculation.MySqlCalculationStrategy
import uk.ac.ox.softeng.maurodatamapper.plugins.database.mysql.calculation.MySqlSamplingStrategy
import uk.ac.ox.softeng.maurodatamapper.plugins.database.mysql.parameters.MySqlDatabaseDataModelImporterProviderServiceParameters
import uk.ac.ox.softeng.maurodatamapper.plugins.database.mysql.query.MySqlQueryStringProvider
import uk.ac.ox.softeng.maurodatamapper.plugins.database.query.QueryStringProvider
import uk.ac.ox.softeng.maurodatamapper.security.User
import uk.ac.ox.softeng.maurodatamapper.util.Utils

import org.springframework.beans.factory.annotation.Autowired

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException

class MySqlDatabaseDataModelImporterProviderService
    extends AbstractDatabaseDataModelImporterProviderService<MySqlDatabaseDataModelImporterProviderServiceParameters>
    implements RemoteDatabaseDataModelImporterProviderService {

    @Autowired
    MySqlDataTypeProviderService mySqlDataTypeProvider

    @Override
    String getDisplayName() {
        'MySQL Importer'
    }

    @Override
    String getVersion() {
        getClass().getPackage().getSpecificationVersion() ?: 'SNAPSHOT'
    }

    @Override
    Boolean handlesContentType(String contentType) {
        false
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
    DefaultDataTypeProvider getDefaultDataTypeProvider() {
        mySqlDataTypeProvider
    }

    @Override
    QueryStringProvider createQueryStringProvider() {
        return new MySqlQueryStringProvider()
    }

    @Override
    SamplingStrategy createSamplingStrategy(String schema, String table, MySqlDatabaseDataModelImporterProviderServiceParameters parameters) {
        new MySqlSamplingStrategy(schema, table)
    }

    @Override
    CalculationStrategy createCalculationStrategy(MySqlDatabaseDataModelImporterProviderServiceParameters parameters) {
        new MySqlCalculationStrategy(parameters)
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
        if (!queryStringProvider.indexInformationQueryString) return
        addIndexInformation(dataModel, null, connection)
    }

    @Override
    void addForeignKeyInformation(DataModel dataModel, Connection connection) throws ApiException, SQLException {
        if (!queryStringProvider.foreignKeyInformationQueryString) return
        addForeignKeyInformation(dataModel, null, connection)
    }

    @Override
    void updateDataModelWithEnumerationsAndSummaryMetadata(User user, MySqlDatabaseDataModelImporterProviderServiceParameters parameters, DataModel dataModel,
                                                           Connection connection) {
        log.debug('Starting enumeration and summary metadata detection')
        long startTime = System.currentTimeMillis()
        CalculationStrategy calculationStrategy = createCalculationStrategy(parameters)
        dataModel.childDataClasses.sort().each {DataClass tableClass ->
            log.trace('Checking {} for possible enumerations and summary metadata', tableClass.label)
            SamplingStrategy samplingStrategy = createSamplingStrategy(null, tableClass.label, parameters)
            if (samplingStrategy.requiresTableType()) {
                samplingStrategy.tableType = getTableType(connection, tableClass.label, null, dataModel.label)
            }
            try {
                // If SS needs the approx count then make the query, this can take a long time hence the reason to check if we need it
                samplingStrategy.approxCount = samplingStrategy.requiresApproxCount() ? getApproxCount(connection, tableClass.label, null) : -1
                if (samplingStrategy.dataExists()) {
                    calculateEnumerationsAndSummaryMetadata(dataModel, null, tableClass, calculationStrategy, samplingStrategy, connection, user)
                } else {
                    log.warn('Not calculating enumerations and summary metadata in {} as the table contains no data', tableClass.label)
                }

            } catch (SQLException exception) {
                log.warn('Could not perform enumeration or summary metadata detection on {} because of {}', tableClass.label, exception.message)
            }
        }
        log.debug('Finished enumeration and summary metadata detection in {}', Utils.timeTaken(startTime))
    }
}
