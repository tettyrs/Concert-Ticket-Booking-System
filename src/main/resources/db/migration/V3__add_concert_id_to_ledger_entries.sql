-- V3__add_concert_id_to_ledger_entries.sql
-- Add missing concert_id column to ledger_entries table
-- This column was missing in older versions of the schema

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='ledger_entries' AND column_name='concert_id') THEN
        ALTER TABLE ledger_entries ADD COLUMN concert_id UUID;
    END IF;
END $$;
