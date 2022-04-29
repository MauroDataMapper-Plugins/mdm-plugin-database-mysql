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
package uk.ac.ox.softeng.maurodatamapper.plugins.database.mysql.calculation


import uk.ac.ox.softeng.maurodatamapper.plugins.database.calculation.SamplingStrategy

/**
 * No Sampling inside MySQL so we create a class but leave the canSample as "false"
 * @since 25/04/2022
 */
class MySqlSamplingStrategy extends SamplingStrategy {

    MySqlSamplingStrategy(String schema, String table) {
        super(schema, table)
    }
}
