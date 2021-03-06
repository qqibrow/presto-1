/*
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
 */
package io.prestosql.plugin.accumulo.io;

import com.google.common.collect.ImmutableList;
import io.prestosql.plugin.accumulo.conf.AccumuloConfig;
import io.prestosql.plugin.accumulo.model.AccumuloColumnHandle;
import io.prestosql.plugin.accumulo.model.AccumuloSplit;
import io.prestosql.spi.connector.ColumnHandle;
import io.prestosql.spi.connector.ConnectorRecordSetProvider;
import io.prestosql.spi.connector.ConnectorSession;
import io.prestosql.spi.connector.ConnectorSplit;
import io.prestosql.spi.connector.ConnectorTransactionHandle;
import io.prestosql.spi.connector.RecordSet;
import org.apache.accumulo.core.client.Connector;

import javax.inject.Inject;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Implementation of a ConnectorRecordSetProvider for Accumulo. Generates {@link AccumuloRecordSet} objects for a provided split.
 *
 * @see AccumuloRecordSet
 * @see AccumuloRecordCursor
 */
public class AccumuloRecordSetProvider
        implements ConnectorRecordSetProvider
{
    private final Connector connector;
    private final String username;

    @Inject
    public AccumuloRecordSetProvider(
            Connector connector,
            AccumuloConfig config)
    {
        this.connector = requireNonNull(connector, "connector is null");
        this.username = requireNonNull(config, "config is null").getUsername();
    }

    @Override
    public RecordSet getRecordSet(ConnectorTransactionHandle transactionHandle, ConnectorSession session, ConnectorSplit split, List<? extends ColumnHandle> columns)
    {
        requireNonNull(split, "split is null");
        requireNonNull(columns, "columns is null");

        // Convert split
        AccumuloSplit accSplit = (AccumuloSplit) split;

        // Convert all columns handles
        ImmutableList.Builder<AccumuloColumnHandle> handles = ImmutableList.builder();
        for (ColumnHandle handle : columns) {
            handles.add((AccumuloColumnHandle) handle);
        }

        // Return new record set
        return new AccumuloRecordSet(connector, session, accSplit, username, handles.build());
    }
}
