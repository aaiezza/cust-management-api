-- V1__initial_schema.sql

-- Create Customer Table
CREATE TABLE customer (
  customer_id UUID PRIMARY KEY,
  full_name VARCHAR(150) NOT NULL,
  preferred_name VARCHAR(50) NOT NULL,
  email_address VARCHAR(100) UNIQUE NOT NULL,
  phone_number VARCHAR(15) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  deleted_at TIMESTAMP WITH TIME ZONE
);

-- Indexes for performance
CREATE INDEX idx_customer_email ON customer (email_address);
CREATE INDEX idx_customer_phone ON customer (phone_number);
