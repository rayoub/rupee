
CREATE OR REPLACE FUNCTION get_search_queue (
    p_user_id VARCHAR
)
RETURNS TABLE (
    user_id VARCHAR,
    search_hash VARCHAR,
    db_type INTEGER,
    search_filter INTEGER,
    search_by INTEGER,
    db_id VARCHAR,
    upload_id INTEGER,
    sort_by INTEGER,
    max_records INTEGER,
    status VARCHAR,
    inserted_on TIMESTAMP(0)
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        q.user_id,
        q.search_hash,
        q.db_type,
        q.search_filter,
        q.search_by,
        q.db_id,
        q.upload_id,
        q.sort_by,
        q.max_records,
        q.status,
        q.inserted_on
    FROM
        search_queue q
    WHERE  
        q.user_id = p_user_id
    ORDER BY
        q.inserted_on DESC;

END;
$$LANGUAGE plpgsql;


