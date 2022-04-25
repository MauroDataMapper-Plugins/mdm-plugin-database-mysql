package uk.ac.ox.softeng.maurodatamapper.plugins.database.mysql.calculation

import uk.ac.ox.softeng.maurodatamapper.plugins.database.DatabaseDataModelWithSamplingImporterProviderServiceParameters
import uk.ac.ox.softeng.maurodatamapper.plugins.database.calculation.SamplingStrategy

/**
 * No Sampling inside MySQL so we create a class but leave the canSample as "false"
 * @since 25/04/2022
 */
class MySqlSamplingStrategy extends SamplingStrategy {

    MySqlSamplingStrategy(String schema, String table,
                          DatabaseDataModelWithSamplingImporterProviderServiceParameters samplingImporterProviderServiceParameters) {
        super(schema, table, samplingImporterProviderServiceParameters)
    }
}
