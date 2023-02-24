/*
 * Copyright 2020-2023 University of Oxford and NHS England
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
package uk.ac.ox.softeng.maurodatamapper.plugins.database.mysql.calculation

import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.plugins.database.DatabaseDataModelImporterProviderServiceParameters
import uk.ac.ox.softeng.maurodatamapper.plugins.database.calculation.CalculationStrategy

/**
 * @since 25/04/2022
 */
class MySqlCalculationStrategy extends CalculationStrategy {

    MySqlCalculationStrategy(DatabaseDataModelImporterProviderServiceParameters parameters) {
        super(parameters)
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
    boolean isColumnPossibleEnumeration(DataType dataType) {
        dataType.domainType == 'PrimitiveType' && ["CHAR", "VARCHAR"].contains(dataType.label.toUpperCase())
    }
}
