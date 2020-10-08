
DO $$

    DECLARE p_benchmark VARCHAR := 'casp_d250'; 
    DECLARE p_version VARCHAR := 'casp_chain_v08_28_2020'; 
    DECLARE p_search_type VARCHAR := 'contained_in';
    DECLARE p_sort_by INTEGER := 1; -- tm_q_tm_score 
    DECLARE p_limit INTEGER := 100; 

BEGIN

    DROP TABLE IF EXISTS figure_table;
   
    CREATE TABLE figure_table AS 
        WITH rupee AS
        (
            SELECT 'All' AS app, db_id_1 AS db_id, tm_q_tm_score AS score FROM get_rupee_results(p_benchmark, p_version, 'all_aligned', p_search_type, p_sort_by, p_limit) WHERE n = 1
        ),
        mtm AS
        (   
            SELECT 'mTM-align' AS app, db_id_1 AS db_id, tm_q_tm_score AS score FROM get_mtm_results(p_benchmark, p_version, p_limit) WHERE n = 1
        ),
        combined AS
        (
            SELECT
                r.db_id,
                r.app AS rupee_app,
                r.score AS rupee_score,
                m.app AS mtm_app,
                m.score AS mtm_score
            FROM
                rupee r
                INNER JOIN mtm m
                    ON r.db_id = m.db_id
        )
        SELECT
            rupee_score,
            mtm_score
        FROM
            combined
        ORDER BY 
            GREATEST(rupee_score, mtm_score);

END $$;
    
SELECT * FROM figure_table;

