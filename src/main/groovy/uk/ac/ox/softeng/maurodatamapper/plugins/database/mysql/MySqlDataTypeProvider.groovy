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

// @implementationStatic
class MySqlDataTypeProvider implements DefaultDataTypeProvider {

    @Override
    String getDisplayName() {
        'MySQL DataTypes'
    }

    @Override
    List<DefaultDataType> getDefaultListOfDataTypes() {
        [
            [label: 'char', description: 'a fixed length string (can contain letters, numbers, and special characters). the size parameter specifies the column length' +
                                         ' in characters - can be from 0 to 255. default is 1'],
            [label: 'varchar', description: 'a variable length string (can contain letters, numbers, and special characters). the size parameter specifies the maximum' +
                                            ' column length in characters - can be from 0 to 65535'],
            [label: 'binary', description: 'equal to char(), but stores binary byte strings. the size parameter specifies the column length in bytes. default is 1'],
            [label: 'varbinary', description: 'equal to varchar(), but stores binary byte strings. the size parameter specifies the maximum column length in bytes.'],
            [label: 'tinyblob', description: 'for blobs (binary large objects). max length: 255 bytes'],
            [label: 'tinytext', description: 'holds a string with a maximum length of 255 characters'],
            [label: 'text', description: 'holds a string with a maximum length of 65,535 bytes'],
            [label: 'blob', description: 'for blobs (binary large objects). holds up to 65,535 bytes of data'],
            [label: 'mediumtext', description: 'holds a string with a maximum length of 16,777,215 characters'],
            [label: 'mediumblob', description: 'for blobs (binary large objects). holds up to 16,777,215 bytes of data'],
            [label: 'longtext', description: 'holds a string with a maximum length of 4,294,967,295 characters'],
            [label: 'longblob', description: 'for blobs (binary large objects). holds up to 4,294,967,295 bytes of data'],
            [label: 'enum', description: 'a string object that can have only one value, chosen from a list of possible values. you can list up to ' +
                                         '65535 values in an enum list. if a value is inserted that is not in the list, a blank value will be ' +
                                         'inserted. the values are sorted in the order you enter them'],
            [label: 'set', description: 'a string object that can have 0 or more values, chosen from a list of possible values. you can list up to 64' +
                                        ' values in a set list'],
            [label: 'bit', description: 'a bit-value type. the number of bits per value is specified in size. the size parameter can hold a value from 1 to 64. the ' +
                                        'default value for size is 1.'],
            [label: 'tinyint', description: 'a very small integer. signed range is from -128 to 127. unsigned range is from 0 to 255. the size parameter specifies the' +
                                            ' maximum display width (which is 255)'],
            [label: 'bool', description: 'zero is considered as false, nonzero values are considered as true.'],
            [label: 'boolean', description: 'equal to bool'],
            [label: 'smallint', description: 'a small integer. signed range is from -32768 to 32767. unsigned range is from 0 to 65535. the size parameter specifies ' +
                                             'the maximum display width (which is 255)'],
            [label: 'mediumint', description: 'a medium integer. signed range is from -8388608 to 8388607. unsigned range is from 0 to 16777215. the size parameter ' +
                                              'specifies the maximum display width (which is 255)'],
            [label: 'int', description: 'a medium integer. signed range is from -2147483648 to 2147483647. unsigned range is from 0 to 4294967295. the size parameter ' +
                                        'specifies the maximum display width (which is 255)'],
            [label: 'integer', description: 'equal to int'],
            [label: 'bigint', description: 'a large integer. signed range is from -9223372036854775808 to 9223372036854775807. unsigned range is from 0 to ' +
                                           '18446744073709551615. the size parameter specifies the maximum display width (which is 255)'],
            [label: 'float', description: 'a floating point number. the total number of digits is specified in size. the number of digits after the decimal point ' +
                                          'is specified in the d parameter. this syntax is deprecated in mysql 8.0.17, and it will be removed in future mysql ' +
                                          'versions'],
            [label: 'float(p)', description: 'a floating point number. mysql uses the p value to determine whether to use float or double for the resulting data type. if p ' +
                                             'is from 0 to 24, the data type becomes float(). if p is from 25 to 53, the data type becomes double()'],
            [label: 'double', description: 'a normal-size floating point number. the total number of digits is specified in size. the number of digits after the ' +
                                           'decimal point is specified in the d parameter'],
            [label: 'double precision', description: ''],
            [label: 'decimal', description: 'an exact fixed-point number. the total number of digits is specified in size. the number of digits after the decimal ' +
                                            'point is specified in the d parameter. the maximum number for size is 65. the maximum number for d is 30. the default ' +
                                            'value for size is 10. the default value for d is 0.'],
            [label: 'dec', description: 'equal to decimal(size,d)'],
            [label: 'date', description: 'a date. format: yyyy-mm-dd. the supported range is from \'1000-01-01\' to \'9999-12-31\''],
            [label: 'datetime', description: 'a date and time combination. format: yyyy-mm-dd hh:mm:ss. the supported range is from \'1000-01-01 00:00:00\' to ' +
                                             '\'9999-12-31 23:59:59\'. adding default and on update in the column definition to get automatic initialization and ' +
                                             'updating to the current date and time'],
            [label: 'timestamp', description: 'a timestamp. timestamp values are stored as the number of seconds since the unix epoch (\'1970-01-01 00:00:00\' utc). ' +
                                              'format: yyyy-mm-dd hh:mm:ss. the supported range is from \'1970-01-01 00:00:01\' utc to \'2038-01-09 03:14:07\' utc. ' +
                                              'automatic initialization and updating to the current date and time can be specified using default current_timestamp and ' +
                                              'on update current_timestamp in the column definition'],
            [label: 'time', description: 'a time. format: hh:mm:ss. the supported range is from \'-838:59:59\' to \'838:59:59\''],
            [label: 'year', description: 'a year in four-digit format. values allowed in four-digit format: 1901 to 2155, and 0000. MySQL 8.0 does not support year in ' +
                                         'two-digit format.']


        ].collect {Map<String, String> properties -> new DefaultDataType(new PrimitiveType(properties))}
    }

    @Override
    String getVersion() {
        getClass().getPackage().getSpecificationVersion() ?: 'SNAPSHOT'
    }
}
