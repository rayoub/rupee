
CREATE OR REPLACE FUNCTION get_alignment_scores (p_version VARCHAR, p_db_id VARCHAR DEFAULT NULL)
RETURNS SETOF alignment_scores
AS $$
BEGIN

    RETURN QUERY
    SELECT
        *
    FROM
        alignment_scores 
    WHERE
        version = p_version
        AND (p_db_id IS NULL OR db_id_1 = p_db_id);

END;
$$LANGUAGE plpgsql;


