

CREATE OR REPLACE FUNCTION get_probabilities(rows_per_band INTEGER, desired_similarity DOUBLE PRECISION)
RETURNS TABLE (
    number_of_bands INTEGER, 
    probability_of_a_match DOUBLE PRECISION
)
AS $$
DECLARE
    max_number_of_bands INTEGER := FLOOR(100.0 / rows_per_band)::INTEGER;
BEGIN

    RAISE NOTICE '%', max_number_of_bands;

    RETURN QUERY
    WITH bands AS
    (
        SELECT
            n
        FROM
            GENERATE_SERIES(1,max_number_of_bands) AS a(n)
    )
    SELECT
        n AS number_of_bands,
        1 - (1 - desired_similarity^rows_per_band)^n AS prob_of_a_match
    FROM
        bands
    ORDER BY
        n;

END;
$$LANGUAGE plpgsql;


