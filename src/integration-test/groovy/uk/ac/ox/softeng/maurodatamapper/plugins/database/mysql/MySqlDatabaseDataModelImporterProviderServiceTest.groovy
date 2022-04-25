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

import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataElement
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.EnumerationType
import uk.ac.ox.softeng.maurodatamapper.plugins.database.mysql.parameters.MySqlDatabaseDataModelImporterProviderServiceParameters

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

@Slf4j
@Integration
@Rollback
class MySqlDatabaseDataModelImporterProviderServiceTest extends BaseDatabasePluginTest<MySqlDatabaseDataModelImporterProviderServiceParameters,
    MySqlDatabaseDataModelImporterProviderService> {

    MySqlDatabaseDataModelImporterProviderService mySqlDatabaseDataModelImporterProviderService

    @Override
    String getDatabasePortPropertyName() {
        'jdbc.port'
    }

    @Override
    int getDefaultDatabasePort() {
        3306
    }

    @Override
    MySqlDatabaseDataModelImporterProviderService getImporterInstance() {
        mySqlDatabaseDataModelImporterProviderService
    }

    @Override
    MySqlDatabaseDataModelImporterProviderServiceParameters createDatabaseImportParameters() {
        new MySqlDatabaseDataModelImporterProviderServiceParameters().tap {
            databaseNames = 'core'
            databaseUsername = 'maurodatamapper'
            databasePassword = 'MauroDataMapper1234'
        }
    }


    void 'test Import Simple Database'() {
        given:
        setupData()

        when:
        final DataModel dataModel = importDataModelAndRetrieveFromDatabase(
            createDatabaseImportParameters(databaseHost, databasePort)
                .tap {databaseNames = 'metadata_simple'}
        )

        List<String> defaultDataTypeLabels = importerInstance.defaultDataTypeProvider.defaultListOfDataTypes.collect {it.label}

        then:
        assertEquals 'Default DT Provider', 34, defaultDataTypeLabels.size()
        log.warn '{}', dataModel.primitiveTypes.findAll {!(it.label in defaultDataTypeLabels)}
        assertEquals 'Database/Model name', 'metadata_simple', dataModel.label
        assertEquals 'Number of columntypes/datatypes', 35, dataModel.dataTypes?.size()
        assertTrue 'All primitive DTs map to a default DT', dataModel.primitiveTypes.findAll {!(it.label in defaultDataTypeLabels)}.isEmpty()
        assertEquals 'Number of primitive types', 34, dataModel.dataTypes.findAll {it.domainType == 'PrimitiveType'}.size()
        assertEquals 'Number of reference types', 1, dataModel.dataTypes.findAll {it.domainType == 'ReferenceType'}.size()
        assertEquals 'Number of tables/dataclasses', 5, dataModel.dataClasses?.size()
        assertEquals 'Number of child tables/dataclasses', 5, dataModel.childDataClasses?.size()

        when:
        final Set<DataClass> dataClasses = dataModel.dataClasses

        // Tables
        final DataClass metadataTable = dataClasses.find {it.label == 'metadata'}

        then:
        assertEquals 'Metadata Number of columns/dataElements', 10, metadataTable.dataElements.size()
        assertEquals 'Metadata Number of metadata', 1, metadataTable.metadata.size()

        assertTrue 'MD All metadata values are valid', metadataTable.metadata.every {it.value && it.key != it.value}

        when:
        List<Map> indexesInfo = new JsonSlurper().parseText(metadataTable.metadata.find {it.key == 'indexes'}.value) as List<Map>

        then:
        assertEquals('MD Index count', 4, indexesInfo.size())

        assertEquals 'MD Primary indexes', 1, indexesInfo.findAll {it.primaryIndex && it.clusteredIndex}.size()
        assertEquals 'MD Unique indexes', 2, indexesInfo.findAll {it.uniqueIndex}.size()
        assertEquals 'MD indexes', 2, indexesInfo.findAll {!it.uniqueIndex && !it.primaryIndex}.size()

        when:
        final Map multipleColIndex = indexesInfo.find {it.name == 'unique_item_id_namespace_key'}

        then:
        assertNotNull 'Should have multi column index', multipleColIndex
        assertEquals 'Correct order of columns', 'catalogue_item_id,namespace,key', multipleColIndex.columns

        when:
        final DataClass ciTable = dataClasses.find {it.label == 'catalogue_item'}

        then:
        assertEquals 'CI Number of columns/dataElements', 10, ciTable.dataElements.size()
        assertEquals 'CI Number of metadata', 1, ciTable.metadata.size()

        assertTrue 'CI All metadata values are valid', ciTable.metadata.every {it.value && it.key != it.value}

        when:
        indexesInfo = new JsonSlurper().parseText(ciTable.metadata.find {it.key == 'indexes'}.value) as List<Map>

        then:
        assertEquals('CI Index count', 3, indexesInfo.size())

        assertEquals 'CI Primary indexes', 1, indexesInfo.findAll {it.primaryIndex && it.clusteredIndex}.size()
        assertEquals 'CI indexes', 2, indexesInfo.findAll {!it.uniqueIndex && !it.primaryIndex}.size()

        when:
        final DataClass cuTable = dataClasses.find {it.label == 'catalogue_user'}

        then:
        assertEquals 'CU Number of columns/dataElements', 18, cuTable.dataElements.size()
        assertEquals 'CU Number of metadata', 1, cuTable.metadata.size()

        assertTrue 'CU All metadata values are valid', cuTable.metadata.every {it.value && it.key != it.value}

        when:
        indexesInfo = new JsonSlurper().parseText(cuTable.metadata.find {it.key == 'indexes'}.value) as List<Map>

        then:
        assertEquals('CU Index count', 3, indexesInfo.size())

        assertEquals 'CI Primary indexes', 1, indexesInfo.findAll {it.primaryIndex && it.clusteredIndex}.size()
        assertEquals 'CI Unique indexes', 2, indexesInfo.findAll {it.uniqueIndex}.size()
        assertEquals 'CI indexes', 1, indexesInfo.findAll {!it.uniqueIndex && !it.primaryIndex}.size()

        // Columns
        assertTrue 'Metadata all elements required', metadataTable.dataElements.every {it.minMultiplicity == 1}
        assertEquals 'CI mandatory elements', 9, ciTable.dataElements.count {it.minMultiplicity == 1}
        assertEquals 'CI optional element description', 0, ciTable.findDataElement('description').minMultiplicity
        assertEquals 'CU mandatory elements', 10, cuTable.dataElements.count {it.minMultiplicity == 1}
    }

    void 'EV : test Import Simple Database With Enumerations'() {
        given:
        setupData()

        when:
        final DataModel dataModel = importDataModelAndRetrieveFromDatabase(
            createDatabaseImportParameters(databaseHost, databasePort)
                .tap {
                    databaseNames = 'metadata_simple'
                    detectEnumerations = true
                    maxEnumerations = 20
                }
        )

        List<String> defaultDataTypeLabels = importerInstance.defaultDataTypeProvider.defaultListOfDataTypes.collect {it.label}

        then:
        assertEquals 'Default DT Provider', 34, defaultDataTypeLabels.size()

        assertEquals 'Database/Model name', 'metadata_simple', dataModel.label
        assertEquals 'Number of columntypes/datatypes', 38, dataModel.dataTypes?.size()
        assertTrue 'All primitive DTs map to a default DT', dataModel.primitiveTypes.findAll {!(it.label in defaultDataTypeLabels)}.isEmpty()
        assertEquals 'Number of primitive types', 34, dataModel.dataTypes.findAll {it.domainType == 'PrimitiveType'}.size()
        assertEquals 'Number of reference types', 1, dataModel.dataTypes.findAll {it.domainType == 'ReferenceType'}.size()
        assertEquals 'Number of enumeration types', 3, dataModel.dataTypes.findAll {it.domainType == 'EnumerationType'}.size()
        assertEquals 'Number of tables/dataclasses', 5, dataModel.dataClasses?.size()
        assertEquals 'Number of child tables/dataclasses', 5, dataModel.childDataClasses?.size()

        when:
        final Set<DataClass> dataClasses = dataModel.dataClasses

        // Tables
        final DataClass metadataTable = dataClasses.find {it.label == 'metadata'}

        then:
        assertEquals 'Metadata Number of columns/dataElements', 10, metadataTable.dataElements.size()
        assertEquals 'Metadata Number of metadata', 1, metadataTable.metadata.size()

        assertTrue 'MD All metadata values are valid', metadataTable.metadata.every {it.value && it.key != it.value}

        when:
        List<Map> indexesInfo = new JsonSlurper().parseText(metadataTable.metadata.find {it.key == 'indexes'}.value) as List<Map>

        then:
        assertEquals('MD Index count', 4, indexesInfo.size())

        assertEquals 'MD Primary indexes', 1, indexesInfo.findAll {it.primaryIndex && it.clusteredIndex}.size()
        assertEquals 'MD Unique indexes', 2, indexesInfo.findAll {it.uniqueIndex}.size()
        assertEquals 'MD indexes', 2, indexesInfo.findAll {!it.uniqueIndex && !it.primaryIndex}.size()

        when:
        final Map multipleColIndex = indexesInfo.find {it.name == 'unique_item_id_namespace_key'}

        then:
        assertNotNull 'Should have multi column index', multipleColIndex
        assertEquals 'Correct order of columns', 'catalogue_item_id,namespace,key', multipleColIndex.columns

        when:
        final DataClass ciTable = dataClasses.find {it.label == 'catalogue_item'}

        then:
        assertEquals 'CI Number of columns/dataElements', 10, ciTable.dataElements.size()
        assertEquals 'CI Number of metadata', 1, ciTable.metadata.size()

        assertTrue 'CI All metadata values are valid', ciTable.metadata.every {it.value && it.key != it.value}

        when:
        indexesInfo = new JsonSlurper().parseText(ciTable.metadata.find {it.key == 'indexes'}.value) as List<Map>

        then:
        assertEquals('CI Index count', 3, indexesInfo.size())

        assertEquals 'CI Primary indexes', 1, indexesInfo.findAll {it.primaryIndex && it.clusteredIndex}.size()
        assertEquals 'CI indexes', 2, indexesInfo.findAll {!it.uniqueIndex && !it.primaryIndex}.size()

        when:
        final DataClass cuTable = dataClasses.find {it.label == 'catalogue_user'}

        then:
        assertEquals 'CU Number of columns/dataElements', 18, cuTable.dataElements.size()
        assertEquals 'CU Number of metadata', 1, cuTable.metadata.size()

        assertTrue 'CU All metadata values are valid', cuTable.metadata.every {it.value && it.key != it.value}

        when:
        indexesInfo = new JsonSlurper().parseText(cuTable.metadata.find {it.key == 'indexes'}.value) as List<Map>

        then:
        assertEquals('CU Index count', 3, indexesInfo.size())

        assertEquals 'CI Primary indexes', 1, indexesInfo.findAll {it.primaryIndex && it.clusteredIndex}.size()
        assertEquals 'CI Unique indexes', 2, indexesInfo.findAll {it.uniqueIndex}.size()
        assertEquals 'CI indexes', 1, indexesInfo.findAll {!it.uniqueIndex && !it.primaryIndex}.size()

        // Columns
        assertTrue 'Metadata all elements required', metadataTable.dataElements.every {it.minMultiplicity == 1}
        assertEquals 'CI mandatory elements', 9, ciTable.dataElements.count {it.minMultiplicity == 1}
        assertEquals 'CI optional element description', 0, ciTable.findDataElement('description').minMultiplicity
        assertEquals 'CU mandatory elements', 10, cuTable.dataElements.count {it.minMultiplicity == 1}

        when:
        final DataClass organisationTable = dataClasses.find {it.label == 'organisation'}

        then:
        assertEquals 'Organisation Number of columns/dataElements', 6, organisationTable.dataElements.size()
        assertEquals 'Organisation Number of metadata', 1, organisationTable.metadata.size()
        assertEquals 'DomainType of the DataType for org_code', 'EnumerationType', organisationTable.findDataElement('org_code').dataType.domainType
        assertEquals 'DomainType of the DataType for org_name', 'PrimitiveType', organisationTable.findDataElement('org_name').dataType.domainType
        assertEquals 'DomainType of the DataType for org_char', 'EnumerationType', organisationTable.findDataElement('org_char').dataType.domainType
        assertEquals 'DomainType of the DataType for description', 'PrimitiveType', organisationTable.findDataElement('description').dataType.domainType
        assertEquals 'DomainType of the DataType for org_type', 'EnumerationType', organisationTable.findDataElement('org_type').dataType.domainType
        assertEquals 'DomainType of the DataType for id', 'PrimitiveType', organisationTable.findDataElement('id').dataType.domainType

        when:
        final EnumerationType orgCodeEnumerationType = organisationTable.findDataElement('org_code').dataType

        then:
        assertEquals 'Number of enumeration values for org_code', 4, orgCodeEnumerationType.enumerationValues.size()
        assertNotNull 'Enumeration value found', orgCodeEnumerationType.enumerationValues.find {it.key == 'CODEZ'}
        assertNotNull 'Enumeration value found', orgCodeEnumerationType.enumerationValues.find {it.key == 'CODEY'}
        assertNotNull 'Enumeration value found', orgCodeEnumerationType.enumerationValues.find {it.key == 'CODEX'}
        assertNotNull 'Enumeration value found', orgCodeEnumerationType.enumerationValues.find {it.key == 'CODER'}
        assertNull 'Not an expected value', orgCodeEnumerationType.enumerationValues.find {it.key == 'CODEP'}

        when:
        final EnumerationType orgTypeEnumerationType = organisationTable.findDataElement('org_type').dataType

        then:
        assertEquals 'Number of enumeration values for org_type', 3, orgTypeEnumerationType.enumerationValues.size()
        assertNotNull 'Enumeration value found', orgTypeEnumerationType.enumerationValues.find {it.key == 'TYPEA'}
        assertNotNull 'Enumeration value found', orgTypeEnumerationType.enumerationValues.find {it.key == 'TYPEB'}
        assertNotNull 'Enumeration value found', orgTypeEnumerationType.enumerationValues.find {it.key == 'TYPEC'}
        assertNull 'Not an expected value', orgTypeEnumerationType.enumerationValues.find {it.key == 'TYPEZ'}

        when:
        final EnumerationType orgCharEnumerationType = organisationTable.findDataElement('org_char').dataType

        then:
        assertEquals 'Number of enumeration values for org_char', 3, orgCharEnumerationType.enumerationValues.size()
        assertNotNull 'Enumeration   value found', orgCharEnumerationType.enumerationValues.find {it.key == 'CHAR1'}
        assertNotNull 'Enumeration value found', orgCharEnumerationType.enumerationValues.find {it.key == 'CHAR2'}
        assertNotNull 'Enumeration value found', orgCharEnumerationType.enumerationValues.find {it.key == 'CHAR3'}
        assertNull 'Not an expected value', orgCharEnumerationType.enumerationValues.find {it.key == 'CHAR4'}
    }

    void 'SM01 : test Import Simple Database With Summary Metadata'() {
        given:
        setupData()

        when:
        final DataModel dataModel = importDataModelAndRetrieveFromDatabase(
            createDatabaseImportParameters(databaseHost, databasePort)
                .tap {
                    databaseNames = 'metadata_simple'
                    detectEnumerations = true
                    maxEnumerations = 20
                    calculateSummaryMetadata = true
                }
        )

        final Set<DataClass> dataClasses = dataModel.dataClasses
        final DataClass sampleTable = dataClasses.find {it.label == 'sample'}

        then:
        assertEquals 'Sample Number of columns/dataElements', 11, sampleTable.dataElements.size()

        when:
        final DataElement id = sampleTable.dataElements.find {it.label == "id"}

        then:
        //Expect id to have contiguous values from 1 to 201
        assertEquals 'reportValue for id',
                     '{"0 - 20":19,"20 - 40":20,"40 - 60":20,"60 - 80":20,"80 - 100":20,"100 - 120":20,"120 - 140":20,"140 - 160":20,"160 - 180":20,"180 - 200":20,"200 - ' +
                     '220":2}',
                     id.summaryMetadata[0].summaryMetadataReports[0].reportValue

        when:
        //sample_tinyint
        final DataElement sample_tinyint = sampleTable.dataElements.find {it.label == "sample_tinyint"}

        then:
        assertEquals 'reportValue for sample_tinyint',
                     '{"0 - 10":19,"10 - 20":20,"20 - 30":20,"30 - 40":20,"40 - 50":20,"50 - 60":20,"60 - 70":20,"70 - 80":20,"80 - 90":20,"90 - 100":20,"100 - 110":2}',
                     sample_tinyint.summaryMetadata[0].summaryMetadataReports[0].reportValue

        when:
        //sample_smallint
        final DataElement sample_smallint = sampleTable.dataElements.find {it.label == "sample_smallint"}

        then:
        assertEquals 'reportValue for sample_smallint',
                     '{"-100 - -80":20,"-80 - -60":20,"-60 - -40":20,"-40 - -20":20,"-20 - 0":20,"0 - 20":20,"20 - 40":20,"40 - 60":20,"60 - 80":20,"80 - 100":20,"100 - ' +
                     '120":1}',
                     sample_smallint.summaryMetadata[0].summaryMetadataReports[0].reportValue

        when:
        //sample_mediumint
        final DataElement sample_mediumint = sampleTable.dataElements.find {it.label == "sample_mediumint"}

        then:
        assertEquals 'reportValue for sample_mediumint',
                     '{"0 - 1000":63,"1000 - 2000":26,"2000 - 3000":20,"3000 - 4000":18,"4000 - 5000":14,"5000 - 6000":14,"6000 - 7000":12,"7000 - 8000":12,"8000 - ' +
                     '9000":10,"9000 - 10000":10,"10000 - 11000":2}',
                     sample_mediumint.summaryMetadata[0].summaryMetadataReports[0].reportValue

        when:
        //sample_int
        final DataElement sample_int = sampleTable.dataElements.find {it.label == "sample_int"}

        then:
        assertEquals 'reportValue for sample_int',
                     '{"0 - 500":63,"500 - 1000":26,"1000 - 1500":20,"1500 - 2000":18,"2000 - 2500":14,"2500 - 3000":14,"3000 - 3500":12,"3500 - 4000":12,"4000 - 4500":10,' +
                     '"4500 - 5000":10,"5000 - 5500":2}',
                     sample_int.summaryMetadata[0].summaryMetadataReports[0].reportValue

        when:
        //sample_bigint
        final DataElement sample_bigint = sampleTable.dataElements.find {it.label == "sample_bigint"}

        then:
        assertEquals 'reportValue for sample_bigint',
                     '{"-1000000 - -800000":8,"-800000 - -600000":8,"-600000 - -400000":11,"-400000 - -200000":15,"-200000 - 0":58,"0 - 200000":59,"200000 - 400000":15,' +
                     '"400000 - 600000":11,"600000 - 800000":8,"800000 - 1000000":7,"1000000 - 1200000":1}',
                     sample_bigint.summaryMetadata[0].summaryMetadataReports[0].reportValue

        when:
        //sample_decimal
        final DataElement sample_decimal = sampleTable.dataElements.find {it.label == "sample_decimal"}

        then:
        assertEquals 'reportValue for sample_decimal',
                     '{"0.00 - 1000000.00":83,"1000000.00 - 2000000.00":36,"2000000.00 - 3000000.00":26,"3000000.00 - 4000000.00":22,"4000000.00 - 5000000.00":20,"5000000.' +
                     '00 - 6000000.00":14}',
                     sample_decimal.summaryMetadata[0].summaryMetadataReports[0].reportValue

        when:
        //sample_numeric
        final DataElement sample_numeric = sampleTable.dataElements.find {it.label == "sample_numeric"}

        then:
        assertEquals 'reportValue for sample_numeric',
                     '{"-10.00 - -8.00":6,"-8.00 - -6.00":9,"-6.00 - -4.00":11,"-4.00 - -2.00":15,"-2.00 - 0.00":59,"0.00 - 2.00":60,"2.00 - 4.00":15,"4.00 - 6.00":11,"6.00' +
                     ' - 8.00":9,"8.00 - 10.00":6}',
                     sample_numeric.summaryMetadata[0].summaryMetadataReports[0].reportValue

        when:
        //sample_date
        final DataElement sample_date = sampleTable.dataElements.find {it.label == "sample_date"}

        then:
        assertEquals 'reportValue for sample_date',
                     '{"May 2020":8,"Jun 2020":30,"Jul 2020":31,"Aug 2020":31,"Sept 2020":30,"Oct 2020":31,"Nov 2020":30,"Dec 2020":10}',
                     sample_date.summaryMetadata[0].summaryMetadataReports[0].reportValue

        when:
        //sample_datetime
        final DataElement sample_datetime = sampleTable.dataElements.find {it.label == "sample_datetime"}

        then:
        assertEquals 'reportValue for sample_datetime',
                     '{"2012 - 2014":20,"2014 - 2016":24,"2016 - 2018":24,"2018 - 2020":24,"2020 - 2022":24,"2022 - 2024":24,"2024 - 2026":24,"2026 - 2028":24,"2028 - ' +
                     '2030":13}',
                     sample_datetime.summaryMetadata[0].summaryMetadataReports[0].reportValue

        when:
        //sample_timestamp
        final DataElement sample_timestamp = sampleTable.dataElements.find {it.label == "sample_timestamp"}

        then:
        assertEquals 'reportValue for sample_timestamp',
                     '{"27/08/2020":4,"28/08/2020":24,"29/08/2020":24,"30/08/2020":24,"31/08/2020":24,"01/09/2020":24,"02/09/2020":24,"03/09/2020":24,"04/09/2020":24,' +
                     '"05/09/2020":5}',
                     sample_timestamp.summaryMetadata[0].summaryMetadataReports[0].reportValue

        checkOrganisationSummaryMetadata(dataModel)

    }

    private void checkOrganisationSummaryMetadata(DataModel dataModel) {
        final Set<DataClass> dataClasses = dataModel.dataClasses
        final DataClass organisationTable = dataClasses.find {it.label == 'organisation'}

        //Map of column name to expected summary metadata description:reportValue. Expect exact counts.
        Map<String, Map<String, String>> expectedColumns = [
                "org_code": ['Enumeration Value Distribution':'{"CODER":2,"CODEX":19,"CODEY":9,"CODEZ":11}'],
                "org_type": ['Enumeration Value Distribution':'{"TYPEA":17,"TYPEB":22,"TYPEC":2}'],
                "org_char": ['Enumeration Value Distribution':'{"NULL":1,"CHAR1":7,"CHAR2":13,"CHAR3":20}']
        ]

        expectedColumns.each {columnName, expectedReport ->
            DataElement de = organisationTable.dataElements.find{it.label == columnName}
            assertEquals 'One summaryMetadata', expectedReport.size(), de.summaryMetadata.size()

            expectedReport.each {expectedReportDescription, expectedReportValue ->
                assertEquals "Description of summary metadatdata for ${columnName}", expectedReportDescription, de.summaryMetadata[0].description
                assertEquals "Value of summary metadatdata for ${columnName}", expectedReportValue, de.summaryMetadata[0].summaryMetadataReports[0].reportValue
            }
        }

        //All data element summary metadata should also have been added to the data class
        organisationTable.dataElements.each {dataElement ->
            dataElement.summaryMetadata?.each {dataElementSummaryMetadata ->
                assert 'dataElement summaryMetadata is also on the dataClass',
                        organisationTable.summaryMetadata.find{organisationTableSummaryMetadata ->
                    organisationTableSummaryMetadata.description == dataElementSummaryMetadata.description &&
                    organisationTableSummaryMetadata.summaryMetadataReports[0].reportValue == dataElementSummaryMetadata.summaryMetadataReports[0].reportValue
                }
            }
        }
    }
}
