
CREATE TABLE cath_name
( 
    cath_name VARCHAR NOT NULL,
    cath_id VARCHAR NOT NULL,
    description VARCHAR NOT NULL
);

CREATE UNIQUE INDEX idx_cath_name_unique ON cath_name (cath_name);
