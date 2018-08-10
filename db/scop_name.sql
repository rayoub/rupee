
CREATE TABLE scop_name
( 
    scop_name VARCHAR NOT NULL,
    description VARCHAR NOT NULL
);

CREATE UNIQUE INDEX idx_scop_name_unique ON scop_name (scop_name);
