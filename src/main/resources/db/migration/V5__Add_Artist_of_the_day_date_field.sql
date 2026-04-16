ALTER TABLE ARTIST_ALIAS ADD COLUMN ARTIST_OF_THE_DAY_DATE DATE;

-- H2 allows multiple Nulls in a unique index, so we can create a unique index on the ARTIST_OF_THE_DAY_DATE column to ensure that only one artist can be assigned to a specific date. This will prevent duplicate entries for the same date.
-- for other dbs we might need to change the index.
-- e.g.: adding a where clause to exclude null values `WHERE ARTIST_OF_THE_DAY_DATE IS NOT NULL;`
CREATE UNIQUE INDEX IF NOT EXISTS idx_artist_of_the_day_date
    ON ARTIST_ALIAS (ARTIST_OF_THE_DAY_DATE);