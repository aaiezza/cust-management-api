-- V2__refactor_customer_schema.sql

-- Step 1: Create new versioned tables
CREATE TABLE customer_full_name_version (
                                            customer_full_name_id UUID PRIMARY KEY,
                                            customer_id UUID NOT NULL,
                                            full_name VARCHAR(150) NOT NULL,
                                            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE customer_preferred_name_version (
                                                 customer_preferred_name_id UUID PRIMARY KEY,
                                                 customer_id UUID NOT NULL,
                                                 preferred_name VARCHAR(50) NOT NULL,
                                                 created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE customer_email_version (
                                        customer_email_id UUID PRIMARY KEY,
                                        customer_id UUID NOT NULL,
                                        email_address VARCHAR(100) NOT NULL,
                                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE customer_phone_list_version (
                                             customer_phone_list_version_id UUID PRIMARY KEY,
                                             customer_id UUID NOT NULL,
                                             created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE customer_phone_list_entry (
                                           customer_phone_list_entry_id UUID PRIMARY KEY,
                                           customer_phone_list_version_id UUID NOT NULL,
                                           phone_number VARCHAR(15) NOT NULL,
                                           created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Step 2: Update the existing customer table
CREATE TABLE customer_new (
                              customer_id UUID PRIMARY KEY,
                              customer_full_name_id UUID NOT NULL,
                              customer_preferred_name_id UUID NOT NULL,
                              customer_email_id UUID NOT NULL,
                              customer_phone_list_version_id UUID NOT NULL,
                              created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Add new data
INSERT INTO customer_new (customer_id, customer_full_name_id, customer_preferred_name_id, customer_email_id, customer_phone_list_version_id, created_at)
SELECT
    customer_id,
    -- Generate UUIDs for the versioned data
    gen_random_uuid() AS customer_full_name_id,
    gen_random_uuid() AS customer_preferred_name_id,
    gen_random_uuid() AS customer_email_id,
    gen_random_uuid() AS customer_phone_list_version_id,
    created_at
FROM customer;

-- Migrate data to new versioned tables
INSERT INTO customer_full_name_version (customer_full_name_id, customer_id, full_name, created_at)
SELECT customer_full_name_id, customer_id, full_name, created_at FROM customer_new;

INSERT INTO customer_preferred_name_version (customer_preferred_name_id, customer_id, preferred_name, created_at)
SELECT customer_preferred_name_id, customer_id, preferred_name, created_at FROM customer_new;

INSERT INTO customer_email_version (customer_email_id, customer_id, email_address, created_at)
SELECT customer_email_id, customer_id, email_address, created_at FROM customer_new;

INSERT INTO customer_phone_list_version (customer_phone_list_version_id, customer_id, created_at)
SELECT customer_phone_list_version_id, customer_id, created_at FROM customer_new;

INSERT INTO customer_phone_list_entry (customer_phone_list_entry_id, customer_phone_list_version_id, phone_number, created_at)
SELECT gen_random_uuid(), customer_phone_list_version_id, phone_number, created_at FROM customer;

-- Step 3: Drop old table and rename new table
DROP TABLE customer CASCADE;
ALTER TABLE customer_new RENAME TO customer;

-- Step 4: Add foreign key constraints to the new table
ALTER TABLE customer
    ADD CONSTRAINT fk_customer_full_name_id FOREIGN KEY (customer_full_name_id)
        REFERENCES customer_full_name_version (customer_full_name_id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE customer
    ADD CONSTRAINT fk_customer_preferred_name_id FOREIGN KEY (customer_preferred_name_id)
        REFERENCES customer_preferred_name_version (customer_preferred_name_id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE customer
    ADD CONSTRAINT fk_customer_email_id FOREIGN KEY (customer_email_id)
        REFERENCES customer_email_version (customer_email_id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE customer
    ADD CONSTRAINT fk_customer_phone_list_version_id FOREIGN KEY (customer_phone_list_version_id)
        REFERENCES customer_phone_list_version (customer_phone_list_version_id) DEFERRABLE INITIALLY DEFERRED;

-- Step 5: Create triggers and materialized views
CREATE MATERIALIZED VIEW latest_customer_email AS
SELECT
    customer_email_id,
    customer_id,
    email_address,
    created_at
FROM customer_email_version
WHERE (customer_id, created_at) IN (
    SELECT customer_id, MAX(created_at)
    FROM customer_email_version
    GROUP BY customer_id
);

CREATE UNIQUE INDEX unique_latest_email ON latest_customer_email (email_address);

CREATE OR REPLACE FUNCTION validate_latest_versions()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.customer_full_name_id != (
        SELECT customer_full_name_id
        FROM customer_full_name_version
        WHERE customer_id = NEW.customer_id
        ORDER BY created_at DESC
        LIMIT 1
    ) THEN
        RAISE EXCEPTION 'customer_full_name_id must reference the latest version for customer_id.';
END IF;

    IF NEW.customer_preferred_name_id != (
        SELECT customer_preferred_name_id
        FROM customer_preferred_name_version
        WHERE customer_id = NEW.customer_id
        ORDER BY created_at DESC
        LIMIT 1
    ) THEN
        RAISE EXCEPTION 'customer_preferred_name_id must reference the latest version for customer_id.';
END IF;

    IF NEW.customer_email_id != (
        SELECT customer_email_id
        FROM customer_email_version
        WHERE customer_id = NEW.customer_id
        ORDER BY created_at DESC
        LIMIT 1
    ) THEN
        RAISE EXCEPTION 'customer_email_id must reference the latest version for customer_id.';
END IF;

    IF NEW.customer_phone_list_version_id != (
        SELECT customer_phone_list_version_id
        FROM customer_phone_list_version
        WHERE customer_id = NEW.customer_id
        ORDER BY created_at DESC
        LIMIT 1
    ) THEN
        RAISE EXCEPTION 'customer_phone_list_version_id must reference the latest version for customer_id.';
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER validate_latest_versions_trigger
    BEFORE INSERT OR UPDATE ON customer
                         FOR EACH ROW
                         EXECUTE FUNCTION validate_latest_versions();
