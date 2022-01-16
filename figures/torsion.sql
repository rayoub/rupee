
WITH sample AS
(
    SELECT
        *
    FROM
        residue_sample
    LIMIT 10000
)
SELECT
    domain_id,
    atom_number,
    phi,
    psi,
    CASE
        WHEN sse IN ('G','H','I') THEN 'Helix'
        WHEN sse = 'T' THEN 'Turn'
        WHEN sse = 'E' THEN 'Strand'
        WHEN sse = 'B' THEN 'Bridge'
        WHEN sse = 'S' THEN 'Bend'
        ELSE 'Coil' -- C
    END AS sse,
    region,
    CASE
        WHEN sse IN ('G','H','I','T') THEN 'Helix'
        WHEN sse IN ('E','B') THEN 'Strand'
        ELSE 'Bend/Coil' -- C and S
    END AS plot
FROM
    sample;

