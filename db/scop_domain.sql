
CREATE TABLE scop_domain
(
    scop_sid SERIAL,
    scop_id VARCHAR NOT NULL,
    pdb_id VARCHAR NOT NULL,
    sunid INTEGER NOT NULL,
    cl VARCHAR NOT NULL,
    cf INTEGER NOT NULL,
    sf INTEGER NOT NULL,
    fa INTEGER NOT NULL,
    cl_cf VARCHAR NOT NULL DEFAULT '',
    cl_cf_sf VARCHAR NOT NULL DEFAULT '',
    cl_cf_sf_fa VARCHAR NOT NULL DEFAULT ''
);

CREATE UNIQUE INDEX idx_scop_domain_unique ON scop_domain (scop_sid);
