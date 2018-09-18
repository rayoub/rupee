
CREATE OR REPLACE FUNCTION update_scop_pairs(p_tab scop_pair ARRAY)
RETURNS VOID
AS $$
BEGIN

    UPDATE scop_pair s
    SET tm_score = p.tm_score
    FROM UNNEST(p_tab) p
    WHERE s.sid = p.sid;

END;
$$ LANGUAGE plpgsql;
