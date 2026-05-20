-- Migration to add message column to enrollments table
-- This adds the additional message field from enrollment form

-- Add message column
ALTER TABLE enrollments
ADD COLUMN IF NOT EXISTS message VARCHAR(250);
