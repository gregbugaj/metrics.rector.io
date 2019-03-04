-- INSERT INTO message(logdate, application, probe, probe_type, value_str, value_num, pid, source_address, metric_type) VALUES ('%s', '%s', '%s', '%s', '%s', %s, %s, '%s', '%s')
-- http://stackoverflow.com/questions/17267417/how-to-upsert-merge-insert-on-duplicate-update-in-postgresql
-- This is on Pre 9.5 version so we use this
DROP FUNCTION upsert_message( TIMESTAMP,  TEXT,  TEXT,  TEXT,  TEXT,  BIGINT,  BIGINT,  TEXT,  TEXT);

CREATE FUNCTION upsert_message(logdate TIMESTAMP, application TEXT, probe TEXT, probe_type TEXT, value_str TEXT, value_num BIGINT, pid BIGINT, source_address TEXT, metric_type TEXT)
    RETURNS VOID
    LANGUAGE plpgsql
AS $$
BEGIN
    LOOP
        -- first try to update

	UPDATE message SET 
		logdate = logdate,
		application = application,
		probe = probe,
		probe_type = probe_type,
		value_str = value_str,
		value_num = value_num,
		pid = pid,
		source_address = source_address,
		metric_type = metric_type	
	WHERE  application = application AND probe = probe AND metric_type = metric_type;

	-- check if the row is found
        IF FOUND THEN
            RETURN;
        END IF;

        -- not found so insert the row
        BEGIN
            
	    INSERT INTO message(logdate, application, probe, probe_type, value_str, value_num, pid, source_address, metric_type) 
	    VALUES (logdate, application, probe, probe_type, value_str, value_num, pid, source_address, metric_type);
	    
            RETURN;
            EXCEPTION WHEN unique_violation THEN
                -- do nothing and loop
        END;
    END LOOP;
END;
$$;