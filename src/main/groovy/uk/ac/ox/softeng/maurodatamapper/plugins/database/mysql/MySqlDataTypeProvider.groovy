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


import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.PrimitiveType
import uk.ac.ox.softeng.maurodatamapper.datamodel.provider.DefaultDataTypeProvider
import uk.ac.ox.softeng.maurodatamapper.datamodel.rest.transport.DefaultDataType

// @CompileStatic
class MySqlDataTypeProvider implements DefaultDataTypeProvider {

    @Override
    String getDisplayName() {
        'MySQL DataTypes'
    }

    @Override
    List<DefaultDataType> getDefaultListOfDataTypes() {
        [[label: 'bigint', description: 'signed eight-byte integer'],
         [label: 'bigserial', description: 'autoincrementing eight-byte integer'],
         [label: 'bit [ [n) ]', description: 'fixed-length bit string'],
         [label: 'bit varying [ [n) ]', description: 'variable-length bit string'],
         [label: 'boolean', description: 'logical Boolean [true/false)'],
         [label: 'box', description: 'rectangular box on a plane'],
         [label: 'bytea', description: 'binary data ["byte array")'],
         [label: 'character [ [n) ]', description: 'fixed-length character string'],
         [label: 'character varying', description: 'variable-length character string'],
         [label: 'character varying [ [n) ]', description: 'variable-length character string'],
         [label: 'cidr', description: 'IPv4 or IPv6 network address'],
         [label: 'circle', description: 'circle on a plane'],
         [label: 'date', description: 'calendar date [year, month, day)'],
         [label: 'double precision', description: 'double precision floating-point number [8 bytes)'],
         [label: 'inet', description: 'IPv4 or IPv6 host address'],
         [label: 'integer', description: 'signed four-byte integer'],
         [label: 'interval [ fields ] [ [p) ]', description: 'time span'],
         [label: 'json', description: 'textual JSON data'],
         [label: 'jsonb', description: 'binary JSON data, decomposed'],
         [label: 'line', description: 'infinite line on a plane'],
         [label: 'lseg', description: 'line segment on a plane'],
         [label: 'macaddr', description: 'MAC [Media Access Control) address'],
         [label: 'money', description: 'currency amount'],
         [label: 'numeric [ [p, s) ]', description: 'exact numeric of selectable precision'],
         [label: 'path', description: 'geometric path on a plane'],
         [label: 'pg_lsn', description: 'PostgreSQL Log Sequence Number'],
         [label: 'point', description: 'geometric point on a plane'],
         [label: 'polygon', description: 'closed geometric path on a plane'],
         [label: 'real', description: 'single precision floating-point number [4 bytes)'],
         [label: 'smallint', description: 'signed two-byte integer'],
         [label: 'smallserial', description: 'autoincrementing two-byte integer'],
         [label: 'serial', description: 'autoincrementing four-byte integer'],
         [label: 'text', description: 'variable-length character string'],
         [label: 'time without time zone', description: 'time of day [no time zone)'],
         [label: 'time with time zone', description: 'time of day, including time zone'],
         [label: 'timestamp without time zone', description: 'date and time [no time zone)'],
         [label: 'timestamp with time zone', description: 'date and time, including time zone'],
         [label: 'time [ [p) ] [ without time zone ]', description: 'time of day [no time zone)'],
         [label: 'time [ [p) ] with time zone', description: 'time of day, including time zone'],
         [label: 'timestamp [ [p) ] [ without time zone ]', description: 'date and time [no time zone)'],
         [label: 'timestamp [ [p) ] with time zone', description: 'date and time, including time zone'],
         [label: 'tsquery', description: 'text search query'],
         [label: 'tsvector', description: 'text search document'],
         [label: 'txid_snapshot', description: 'user-level transaction ID snapshot'],
         [label: 'uuid', description: 'universally unique identifier'],
         [label: 'xml', description: 'XML data'],
        ].collect {Map<String, String> properties -> new DefaultDataType(new PrimitiveType(properties))}
    }

    @Override
    String getVersion() {
        getClass().getPackage().getSpecificationVersion() ?: 'SNAPSHOT'
    }
}
