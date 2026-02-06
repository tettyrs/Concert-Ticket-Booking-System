-- V2__seed_data.sql
-- Comprehensive seed data for Concert Ticket Booking System
-- Updated: 2026-02-06

-- 1. Insert sample venues
INSERT INTO venues (id, name, address, capacity)
VALUES 
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890'::uuid, 'Jakarta International Stadium', 'Jl. Gelora No.1, Jakarta Pusat', 50000),
    ('b2c3d4e5-f6a7-8901-bcde-f12345678901'::uuid, 'Bandung Convention Center', 'Jl. Asia Afrika No.8, Bandung', 10000),
    ('c3d4e5f6-a7b8-9012-cdef-123456789012'::uuid, 'Surabaya Grand Hall', 'Jl. Pemuda No.15, Surabaya', 15000),
    ('d4e5f6a7-b8c9-0123-def1-234567890123'::uuid, 'Bali International Convention Center', 'Jl. Nusa Dua, Bali', 8000)
ON CONFLICT (id) DO NOTHING;

-- 2. Insert sample events
INSERT INTO events (id, venue_id, name, description, event_date, timezone, status, artist)
VALUES 
    ('e5f6a7b8-c9d0-1234-ef12-345678901234'::uuid, 
     'a1b2c3d4-e5f6-7890-abcd-ef1234567890'::uuid,
     'Rock Festival 2026',
     'Annual rock music festival featuring international artists',
     '2026-06-15 19:00:00+07'::timestamptz,
     'Asia/Jakarta',
     'UPCOMING',
     'Various Rock Artists'),
    ('f6a7b8c9-d0e1-2345-f123-456789012345'::uuid,
     'b2c3d4e5-f6a7-8901-bcde-f12345678901'::uuid,
     'Jazz Night 2026',
     'Evening of smooth jazz performances',
     '2026-07-20 20:00:00+07'::timestamptz,
     'Asia/Jakarta',
     'UPCOMING',
     'The Jazz Ensemble'),
    ('a7b8c9d0-e1f2-3456-1234-567890123456'::uuid,
     'c3d4e5f6-a7b8-9012-cdef-123456789012'::uuid,
     'Pop Concert Spectacular',
     'Biggest pop concert of the year',
     '2026-08-10 18:00:00+07'::timestamptz,
     'Asia/Jakarta',
     'UPCOMING',
     'Pop Star Collective'),
    ('b8c9d0e1-f2a3-4567-2345-678901234567'::uuid,
     'd4e5f6a7-b8c9-0123-def1-234567890123'::uuid,
     'Electronic Music Festival',
     'Three-day electronic music extravaganza',
     '2026-09-05 16:00:00+07'::timestamptz,
     'Asia/Jakarta',
     'UPCOMING',
     'DJ Masters')
ON CONFLICT (id) DO NOTHING;

-- 3. Insert ticket categories
INSERT INTO ticket_categories (id, event_id, name, price, total_allocation, available_stock, version)
VALUES 
    -- Rock Festival 2026
    ('c9d0e1f2-a3b4-5678-3456-789012345678'::uuid,
     'e5f6a7b8-c9d0-1234-ef12-345678901234'::uuid,
     'VIP',
     5000000.00,
     500,
     500,
     0),
    ('d0e1f2a3-b4c5-6789-4567-890123456789'::uuid,
     'e5f6a7b8-c9d0-1234-ef12-345678901234'::uuid,
     'REGULAR',
     2000000.00,
     2000,
     2000,
     0),
    ('e1f2a3b4-c5d6-7890-5678-901234567890'::uuid,
     'e5f6a7b8-c9d0-1234-ef12-345678901234'::uuid,
     'FESTIVAL PASS',
     1000000.00,
     5000,
     5000,
     0),
    
    -- Jazz Night 2026
    ('f2a3b4c5-d6e7-8901-6789-012345678901'::uuid,
     'f6a7b8c9-d0e1-2345-f123-456789012345'::uuid,
     'VIP',
     3000000.00,
     200,
     200,
     0),
    ('a3b4c5d6-e7f8-9012-7890-123456789012'::uuid,
     'f6a7b8c9-d0e1-2345-f123-456789012345'::uuid,
     'REGULAR',
     1500000.00,
     800,
     800,
     0),
    
    -- Pop Concert Spectacular
    ('b4c5d6e7-f8a9-0123-8901-234567890123'::uuid,
     'a7b8c9d0-e1f2-3456-1234-567890123456'::uuid,
     'PLATINUM',
     6000000.00,
     300,
     300,
     0),
    ('c5d6e7f8-a9b0-1234-9012-345678901234'::uuid,
     'a7b8c9d0-e1f2-3456-1234-567890123456'::uuid,
     'GOLD',
     3500000.00,
     1000,
     1000,
     0),
    ('d6e7f8a9-b0c1-2345-0123-456789012345'::uuid,
     'a7b8c9d0-e1f2-3456-1234-567890123456'::uuid,
     'SILVER',
     1800000.00,
     3000,
     3000,
     0),
    
    -- Electronic Music Festival
    ('e7f8a9b0-c1d2-3456-1234-567890123456'::uuid,
     'b8c9d0e1-f2a3-4567-2345-678901234567'::uuid,
     '3-DAY VIP PASS',
     4500000.00,
     400,
     400,
     0),
    ('f8a9b0c1-d2e3-4567-2345-678901234567'::uuid,
     'b8c9d0e1-f2a3-4567-2345-678901234567'::uuid,
     '3-DAY REGULAR PASS',
     2500000.00,
     1500,
     1500,
     0),
    ('a9b0c1d2-e3f4-5678-3456-789012345678'::uuid,
     'b8c9d0e1-f2a3-4567-2345-678901234567'::uuid,
     'SINGLE DAY PASS',
     1000000.00,
     3000,
     3000,
     0)
ON CONFLICT (id) DO NOTHING;

-- 4. Insert sample users
INSERT INTO users (id, username, password, full_name, email, role)
VALUES 
    -- Admin users
    ('b0c1d2e3-f4a5-6789-4567-890123456789'::uuid,
     'admin',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'System Administrator',
     'admin@concert.com',
     'ADMIN'),
    ('c1d2e3f4-a5b6-7890-5678-901234567890'::uuid,
     'superadmin',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Super Administrator',
     'superadmin@concert.com',
     'ADMIN'),
    
    -- Regular users
    ('d2e3f4a5-b6c7-8901-6789-012345678901'::uuid,
     'customer1',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'John Doe',
     'john.doe@example.com',
     'USER'),
    ('e3f4a5b6-c7d8-9012-7890-123456789012'::uuid,
     'customer2',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Jane Smith',
     'jane.smith@example.com',
     'USER'),
    ('f4a5b6c7-d8e9-0123-8901-234567890123'::uuid,
     'customer3',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Bob Johnson',
     'bob.johnson@example.com',
     'USER')
ON CONFLICT (id) DO NOTHING;

-- Add indexes for better query performance (if not already in V1)
CREATE INDEX IF NOT EXISTS idx_events_status ON events(status);
CREATE INDEX IF NOT EXISTS idx_events_date ON events(event_date);
CREATE INDEX IF NOT EXISTS idx_ticket_categories_available ON ticket_categories(available_stock);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
