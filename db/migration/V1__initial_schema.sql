-- V1__initial_schema.sql

-- Create Customer Table
CREATE TABLE customer (
    customer_dbid BIGINT GENERATED ALWAYS AS IDENTITY
  , customer_id UUID NOT NULL
  , full_name VARCHAR(150) NOT NULL
  , preferred_name VARCHAR(50) NOT NULL
  , email_address VARCHAR(100) NOT NULL
  , phone_number VARCHAR(15) NOT NULL
  , created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
  , updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
  , deleted_at TIMESTAMP WITH TIME ZONE
  , CONSTRAINT pk_customer_dbid PRIMARY KEY (customer_dbid)
  , CONSTRAINT ux_customer_id UNIQUE (customer_id)
);

-- Indexes for performance
CREATE UNIQUE INDEX ux_customer_email_address ON customer(email_address) WHERE deleted_at IS NULL;
CREATE INDEX ix_customer_phone ON customer (phone_number);
