-- V1__initial_schema.sql
-- Initial database schema for Concert Ticket Booking System

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Venues Table
CREATE TABLE venues (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255),
    address TEXT,
    capacity INTEGER
);

-- 2. Events Table
CREATE TABLE events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    venue_id UUID REFERENCES venues(id),
    name VARCHAR(255),
    description TEXT,
    event_date TIMESTAMPTZ,
    timezone VARCHAR(255),
    status VARCHAR(255),
    artist VARCHAR(255)
);

-- 3. Ticket Categories Table
CREATE TABLE ticket_categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    event_id UUID REFERENCES events(id),
    name VARCHAR(255),
    price DECIMAL(19, 2),
    total_allocation INTEGER,
    available_stock INTEGER,
    version INTEGER DEFAULT 0,
    CONSTRAINT chk_stock_positive CHECK (available_stock >= 0)
);

-- 4. Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    full_name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    role VARCHAR(255)
);

-- 5. Bookings Table
CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID,
    ticket_category_id UUID REFERENCES ticket_categories(id),
    quantity INTEGER,
    total_amount DECIMAL(19, 2),
    status VARCHAR(255),
    idempotency_key VARCHAR(255) UNIQUE,
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ
);

-- 6. Payments Table
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id UUID REFERENCES bookings(id),
    amount DECIMAL(19, 2),
    currency VARCHAR(255),
    payment_method VARCHAR(255),
    gateway_transaction_id VARCHAR(255),
    paid_at TIMESTAMPTZ
);

-- 7. Ledger Entries Table (Immutable Journal)
CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id UUID NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    type VARCHAR(10) NOT NULL,
    concert_id UUID,
    recorded_at TIMESTAMPTZ
);

-- Indexes for performance
CREATE INDEX idx_events_venue ON events(venue_id);
CREATE INDEX idx_ticket_categories_event ON ticket_categories(event_id);
CREATE INDEX idx_bookings_user ON bookings(user_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_expires ON bookings(expires_at);
CREATE INDEX idx_payments_booking ON payments(booking_id);
CREATE INDEX idx_ledger_booking ON ledger_entries(booking_id);
