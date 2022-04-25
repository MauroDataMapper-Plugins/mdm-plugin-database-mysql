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
