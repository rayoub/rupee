
CREATE OR REPLACE FUNCTION expire_search_queue(p_expiration TIMESTAMP) 
RETURNS VOID
AS $$
BEGIN

    DELETE FROM search_queue WHERE status = 'completed' AND inserted_on < p_expiration;

    DELETE FROM search_result WHERE search_id NOT IN (SELECT DISTINCT search_id FROM search_queue); 

END;
$$ LANGUAGE plpgsql;
