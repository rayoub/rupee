
CREATE OR REPLACE FUNCTION get_alignment_scores (p_db_id VARCHAR) 
RETURNS SETOF alignment_scores
AS $$
BEGIN

    RETURN QUERY
    SELECT
        *
    FROM
        alignment_scores 
    WHERE
        db_id_1 = p_db_id; 

END;
$$LANGUAGE plpgsql;


