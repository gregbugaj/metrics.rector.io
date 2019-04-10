
DROP TABLE IF EXISTS "tracked_events";

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS tracked_events
(
    time            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW()),
    event           TIMESTAMP WITH TIME ZONE NOT NULL,
    application     TEXT,                                 -- Application name
    UUID            UUID DEFAULT uuid_generate_v4(),      -- Unique ID for the event tracked
    probe           TEXT,                                 -- Probe for the monitor
    probe_type      TEXT,                                 -- Probe type
    value_str       TEXT,                                 -- String value
    value_num       DOUBLE PRECISION,                     -- Numeric value
    source_address  TEXT,                                 -- Source of the event
    metric_type     TEXT,                                 --
    data            JSONB                                 -- JSON Payload if any
);


-- This creates a hypertable that is partitioned by time
--   using the values in the `time` column.

SELECT create_hypertable('tracked_events', 'time');
