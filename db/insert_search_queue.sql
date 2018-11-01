
CREATE OR REPLACE FUNCTION insert_search_queue(
    p_user_id VARCHAR, 
    p_db_type INTEGER,
    p_search_filter INTEGER,
    p_search_by INTEGER,
    p_db_id VARCHAR,
    p_upload_id INTEGER,
    p_sort_by INTEGER,
    p_max_records INTEGER,
    p_status VARCHAR
)
RETURNS VOID
AS $$
BEGIN

    INSERT INTO search_queue (
        user_id, 
        db_type,
        search_filter,
        search_by,
        db_id,
        upload_id,
        sort_by,
        max_records,
        status
    )
    VALUES (
        p_user_id, 
        p_db_type,
        p_search_filter,
        p_search_by,
        p_db_id,
        p_upload_id,
        p_sort_by,
        p_max_records,
        p_status
    ); 

END;
$$ LANGUAGE plpgsql;
