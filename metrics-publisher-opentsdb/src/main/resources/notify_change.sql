CREATE OR REPLACE FUNCTION notify_change() RETURNS TRIGGER AS $$
DECLARE
  id bigint;

    BEGIN
	
        --PERFORM pg_notify('message_channel', 'Payload x1');
          -- Data never will be removed except during dev
	  IF TG_OP = 'DELETE' OR TG_OP = 'TRUNCATE' THEN  
	    RETURN OLD;
	  END IF;


	  IF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
	    id = NEW.id;
	  ELSE
	    id = OLD.id;
	  END IF;

	PERFORM pg_notify('message_channel', json_build_object('table', TG_TABLE_NAME, 'id', id, 'type', TG_OP, 'row', row_to_json(NEW))::text);

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;



CREATE TRIGGER table_change 
    AFTER INSERT OR UPDATE OR DELETE ON message
    FOR EACH ROW EXECUTE PROCEDURE notify_change();