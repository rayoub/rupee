
CREATE TABLE ecod_domain
(
    ecod_sid SERIAL,
    ecod_id VARCHAR NOT NULL,
    pdb_id VARCHAR NOT NULL,
    x VARCHAR NOT NULL,
    h VARCHAR NOT NULL,
    t VARCHAR NOT NULL,
    f VARCHAR NOT NULL,
    architecture VARCHAR NOT NULL,
    x_description VARCHAR NOT NULL,
    h_description VARCHAR NOT NULL,
    t_description VARCHAR NOT NULL,
    f_description VARCHAR NOT NULL,
    sort_key VARCHAR NOT NULL DEFAULT ''
);

CREATE UNIQUE INDEX idx_ecod_domain_unique ON ecod_domain (ecod_sid);
