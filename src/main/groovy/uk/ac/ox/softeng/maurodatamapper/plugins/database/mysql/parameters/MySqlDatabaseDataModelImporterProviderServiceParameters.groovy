/*
 * Copyright 2020-2023 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
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
package uk.ac.ox.softeng.maurodatamapper.plugins.database.mysql.parameters


import uk.ac.ox.softeng.maurodatamapper.plugins.database.DatabaseDataModelImporterProviderServiceParameters

import com.mysql.cj.jdbc.MysqlDataSource
import groovy.util.logging.Slf4j

@Slf4j
class MySqlDatabaseDataModelImporterProviderServiceParameters extends DatabaseDataModelImporterProviderServiceParameters<MysqlDataSource> {

    @Override
    MysqlDataSource getDataSource(String databaseName) {
        final MysqlDataSource dataSource = new MysqlDataSource().tap {
            setServerName databaseHost
            setPort databasePort
            setDatabaseName databaseName
            if (databaseSSL) {
                setUseSSL true
                setSslMode 'require'
            }
        }
        log.info 'DataSource connection url: {}', dataSource.url
        dataSource
    }

    @Override
    String getUrl(String databaseName) {
        getDataSource(databaseName).url
    }

    @Override
    String getDatabaseDialect() {
        'MySQL'
    }

    @Override
    int getDefaultPort() {
        3306
    }

    @Override
    boolean shouldImportSchemasAsDataClasses() {
        false
    }
}
