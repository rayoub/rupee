
WITH grouped AS
(
    SELECT
        pdb_id,
        chain_id,
        COUNT(*) AS residue_count
    FROM
        residue
    GROUP BY
        pdb_id,
        chain_id
)
SELECT 
    pdb_id,
    chain_id,
    residue_count
FROM 
    grouped
WHERE
    residue_count > 20
ORDER BY
    pdb_id,
    chain_id;
    

