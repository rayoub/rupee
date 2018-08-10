
CREATE OR REPLACE FUNCTION insert_logs (p_tab log ARRAY)
RETURNS VOID
AS $$
BEGIN

	INSERT INTO log (level, exception, message)
	SELECT
        level,
        exception,
        message
	FROM
		UNNEST(p_tab);

END;
$$ LANGUAGE plpgsql;

